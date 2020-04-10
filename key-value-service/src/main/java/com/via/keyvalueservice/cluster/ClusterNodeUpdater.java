package com.via.keyvalueservice.cluster;

import com.via.keyvalueservice.cluster.models.ClusterNode;
import com.via.keyvalueservice.cluster.models.ClusterNodeRepository;
import com.via.keyvalueservice.keyvalue.models.KeyValueItem;
import com.via.keyvalueservice.keyvalue.models.KeyValueItemRepository;
import org.apache.catalina.Cluster;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ClusterNodeUpdater {
    private final ClusterNodeRepository clusterNodeRepository;
    private final KeyValueItemRepository itemRepository;
    private final String hostIp;
    private static final String PORT = ":8080";

    public ClusterNodeUpdater(ClusterNodeRepository repository, KeyValueItemRepository itemRepository) throws UnknownHostException {
        this.clusterNodeRepository = repository;
        this.itemRepository = itemRepository;
        hostIp = InetAddress.getLocalHost().getHostAddress();
    }

    @Scheduled(fixedDelay = 1000, initialDelay = 1000)
    public void updateNodesTask() {
    }

    public void joinCluster(String host) {
        if(clusterNodeRepository.count() > 0){
            throw new AlreadyInClusterException(host);
        }

        clusterNodeRepository.save(new ClusterNode(host, 0));

        WebClient nodeClient = WebClient.create(host + PORT);
        ClusterNode[] clusterNodes = nodeClient.post()
                .uri("cluster/internal/update?host=" + hostIp)
                .retrieve()
                .bodyToMono(ClusterNode[].class)
                .block();

        Arrays.stream(clusterNodes).forEach(node -> clusterNodeRepository.save(node));
    }

    public void leaveCluster() {
        clusterNodeRepository.findAll().stream().forEach(node -> {
            WebClient nodeClient = WebClient.create(node.getHostAddress() + PORT);
            nodeClient.post().uri("cluster/internal/update?host=" + hostIp);
        });

        clusterNodeRepository.deleteAll();
    }

    public List<ClusterNode> clusterNodes() {
        return clusterNodeRepository.findAll();
    }

    public void updateKeyValueItem(KeyValueItem updatedItem) {
        clusterNodeRepository.findAll().parallelStream().forEach(node -> {
            WebClient nodeClient = WebClient.create(node.getHostAddress() + PORT);
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
            WebClient nodeClient = WebClient.create(node.getHostAddress() + PORT);
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
}
