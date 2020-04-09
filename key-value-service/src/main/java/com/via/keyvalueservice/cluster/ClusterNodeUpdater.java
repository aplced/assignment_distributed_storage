package com.via.keyvalueservice.cluster;

import com.via.keyvalueservice.cluster.models.ClusterNodeRepository;
import com.via.keyvalueservice.keyvalue.models.KeyValueItem;
import com.via.keyvalueservice.keyvalue.models.KeyValueItemRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Component
public class ClusterNodeUpdater {
    private final List<WebClient> clusterNodes = new ArrayList<>();
    private final ClusterNodeRepository clusterNodeRepository;
    private final KeyValueItemRepository itemRepository;

    public ClusterNodeUpdater(ClusterNodeRepository repository, KeyValueItemRepository itemRepository) {
        this.clusterNodeRepository = repository;
        this.itemRepository = itemRepository;
    }

    @Scheduled(fixedDelay = 1000, initialDelay = 1000)
    public void updateNodesTask() {
    }

    public void updateKeyValueItem(KeyValueItem updatedItem) {
        clusterNodeRepository.findAll().stream().forEach(node -> {
            WebClient nodeClient = WebClient.create(node.getHostAddress() + ":8080");
            KeyValueItem remoteKeyValueItem = nodeClient
                    .post()
                    .uri("cluster/internal/update").body(BodyInserters.fromValue(updatedItem)).retrieve()
                    .bodyToMono(KeyValueItem.class)
                    .block();

            //A fresher update has already happened, but this node has missed the update
            if(remoteKeyValueItem.getTicks() > updatedItem.getTicks()) {
                throw new ClusterNodeUpdateCollisionException(updatedItem, remoteKeyValueItem, node.getHostAddress());
            }
        });

        itemRepository.save(updatedItem);
    }

    public void updateKeyValueItems(List<KeyValueItem> updatedItems) {
        clusterNodeRepository.findAll().stream().forEach(node -> {
            WebClient nodeClient = WebClient.create(node.getHostAddress() + ":8080");
            KeyValueItem remoteKeyValueItem = nodeClient
                    .post()
                    .uri("cluster/internal/batchUpdate").body(BodyInserters.fromValue(updatedItems)).retrieve()
                    .bodyToMono(KeyValueItem.class)
                    .block();

            //A fresher update has already happened, but this node has missed the update
            //TODO: Add exception here
        });

        itemRepository.saveAll(updatedItems);
    }
}
