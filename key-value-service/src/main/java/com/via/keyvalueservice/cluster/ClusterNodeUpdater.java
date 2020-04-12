package com.via.keyvalueservice.cluster;

import com.via.keyvalueservice.cluster.models.ClusterNode;
import com.via.keyvalueservice.cluster.models.ClusterNodeRepository;
import com.via.keyvalueservice.keyvalue.models.KeyValueItem;
import com.via.keyvalueservice.keyvalue.models.KeyValueItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class ClusterNodeUpdater {
    private final ClusterNodeRepository clusterNodeRepository;
    private final KeyValueItemRepository itemRepository;
    private final String hostIp;
    private static final String PORT = ":8080";

    Logger logger = LoggerFactory.getLogger(ClusterNodeUpdater.class);

    public ClusterNodeUpdater(ClusterNodeRepository repository, KeyValueItemRepository itemRepository) throws UnknownHostException {
        this.clusterNodeRepository = repository;
        this.itemRepository = itemRepository;
        hostIp = InetAddress.getLocalHost().getHostAddress();
    }

    public void joinCluster(String host) {
        testForSelf(host);

        if(clusterNodeRepository.count() > 0){
            throw new AlreadyInClusterException(host);
        }
        long ticksJoining = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        ClusterNode[] clusterNodes = null;
        try {
            WebClient nodeClient = WebClient.create("http://" + host + PORT);
            clusterNodes = nodeClient.post()
                    .uri("/cluster/internal/joining?host=" + hostIp + "&nodeList=true")
                    .retrieve()
                    .bodyToMono(ClusterNode[].class)
                    .block();

            KeyValueItem[] itemsInCluster = nodeClient.get()
                    .uri("/cluster/internal/batchUpdate?ticks=" + ticksJoining)
                    .retrieve()
                    .bodyToMono(KeyValueItem[].class)
                    .block();

            Arrays.stream(itemsInCluster).parallel().forEach(item -> {
                Optional<KeyValueItem> ownItem = itemRepository.findById(item.getKey());
                if(ownItem.isPresent() && ownItem.get().getTicks() > item.getTicks()) {
                    //Uh, oh not handling this corner case...
                    throw new ClusterNodeUpdateCollisionException(ownItem.get(), item, host);
                } else {
                    itemRepository.save(item);
                }
            });
        } catch (Exception ex) {
            logger.warn("Cluster join: " + ex.getMessage());
        }

        if(clusterNodes != null) {
            clusterNodeRepository.save(new ClusterNode(host, 0));
            Arrays.stream(clusterNodes).parallel().forEach(node -> {
                try {
                    WebClient.create("http://" + node.getHostAddress() + PORT).post().uri("cluster/internal/joining?host=" + hostIp);
                    clusterNodeRepository.save(node);
                } catch (Exception ex) {
                    logger.warn("Notify join: " + ex.getMessage());
                }
            });

            List<KeyValueItem> ownItems = itemRepository.findByTicksLessThan(ticksJoining);
            updateKeyValueItems(ownItems);
        }
    }

    public void leaveCluster() {
        clusterNodeRepository.findAll().stream().forEach(node -> {
            WebClient nodeClient = WebClient.create("http://" + node.getHostAddress() + PORT);
            nodeClient.post().uri("cluster/internal/leaving?host=" + hostIp);
        });

        clusterNodeRepository.deleteAll();
    }

    public List<ClusterNode> clusterNodes() {
        return clusterNodeRepository.findAll();
    }

    public void updateKeyValueItem(KeyValueItem updatedItem) {
        clusterNodeRepository.findAll().parallelStream().forEach(node -> {
            WebClient nodeClient = WebClient.create("http://" + node.getHostAddress() + PORT);
            KeyValueItem remoteKeyValueItem = nodeClient
                    .post()
                    .uri("cluster/internal/update").body(BodyInserters.fromValue(updatedItem)).retrieve()
                    .bodyToMono(KeyValueItem.class)
                    .block();

            //A fresher update has already happened, but this node has missed the update
            if(remoteKeyValueItem.getTicks() > updatedItem.getTicks()) {
                throw new ClusterNodeUpdateCollisionException(updatedItem, remoteKeyValueItem, node.getHostAddress());
            }

            node.setLastUpdateSent(updatedItem.getTicks());
            clusterNodeRepository.save(node);
        });

        itemRepository.save(updatedItem);
    }

    public void updateKeyValueItems(List<KeyValueItem> updatedItems) {
        clusterNodeRepository.findAll().parallelStream().forEach(node -> {
            logger.warn("Updating: " + node.getHostAddress() + " with [" + updatedItems + "]");
            WebClient nodeClient = WebClient.create("http://" + node.getHostAddress() + PORT);
            KeyValueItem[] remoteKeyValueItems = nodeClient
                    .post()
                    .uri("cluster/internal/batchUpdate").body(BodyInserters.fromValue(updatedItems)).retrieve()
                    .bodyToMono(KeyValueItem[].class)
                    .block();

            //A fresher update has already happened, but this node has missed the update
            //TODO: Add exception here
            //TODO: record update time
        });

        itemRepository.saveAll(updatedItems);
    }

    private void testForSelf(String host) {
        if(host.equals(hostIp) || host.equals("127.0.0.1")){
            throw new IllegalArgumentException("Can't cluster with self");
        }
    }
}
