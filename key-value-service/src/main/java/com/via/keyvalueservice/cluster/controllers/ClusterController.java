package com.via.keyvalueservice.cluster.controllers;

import com.via.keyvalueservice.cluster.ClusterNodeUpdater;
import com.via.keyvalueservice.cluster.models.ClusterNode;
import com.via.keyvalueservice.cluster.models.ClusterNodeRepository;
import com.via.keyvalueservice.keyvalue.models.KeyValueItem;
import com.via.keyvalueservice.keyvalue.models.KeyValueItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class ClusterController {
    private final ClusterNodeRepository repository;
    private final KeyValueItemRepository itemRepository;

    Logger logger = LoggerFactory.getLogger(ClusterController.class);

    public ClusterController(ClusterNodeRepository repository, KeyValueItemRepository itemRepository) {
        this.repository = repository;
        this.itemRepository = itemRepository;
    }

    @PostMapping("cluster/internal/joining")
    List<ClusterNode> joiningCluster(@RequestParam(name = "host") String joiningHost, @RequestParam("nodeList") Optional<Boolean> listClusterNodes) {
        String decodedHost = joiningHost;
        repository.save(new ClusterNode(decodedHost, 0));

        logger.warn("Host " + joiningHost + " joining in cluster");
        if(listClusterNodes.isPresent() && listClusterNodes.get()) {
            logger.warn("Listing cluster nodes");
            //Get all nodes already in the cluster
            List<ClusterNode> clusterNodes = repository.findAll();
            //It is possible that the joining node is restarting and is already in the cluster
            clusterNodes.removeIf(n -> n.getHostAddress().equals(decodedHost));

            return clusterNodes;
        } else {
            return new ArrayList<>();
        }
    }

    @PostMapping("cluster/internal/leaving")
    void leavingCluster(@RequestParam(name = "host") String leavingHost) {
        logger.warn("Host " + leavingHost + " is leaving the cluster");
        repository.deleteById(leavingHost);
    }

    @PostMapping("cluster/internal/update")
    KeyValueItem updateKeyValueItem(@RequestBody KeyValueItem updatingItem) {
        logger.warn("Updating new value" + updatingItem);
        Optional<KeyValueItem> myItem = itemRepository.findById(updatingItem.getKey());
        //Update this nodes key value item only if it doesn't exist or if the update is fresher
        if(!myItem.isPresent() || updatingItem.getTicks() > myItem.get().getTicks()) {
            itemRepository.save(updatingItem);
            return updatingItem;
        } else {
            logger.warn("My item is newer!");
            return myItem.get();
        }
    }

    @PostMapping("cluster/internal/batchUpdate")
    List<KeyValueItem> batchUpdateKeyValueItem(@RequestBody List<KeyValueItem> updatingItems) {
        List<KeyValueItem> response = new ArrayList<>();
        updatingItems.forEach(updatingItem -> {
            Optional<KeyValueItem> myItem = itemRepository.findById(updatingItem.getKey());
            //Update this nodes key value item only if it doesn't exist or if the update is fresher
            if (!myItem.isPresent() || updatingItem.getTicks() > myItem.get().getTicks()) {
                itemRepository.save(updatingItem);
            } else {
                response.add(myItem.get());
            }
        });
        return response;
    }

    @GetMapping("cluster/internal/batchUpdate")
    List<KeyValueItem> batchGetAllKeyValueItems(@RequestParam(name = "ticks") long toTicks) {
        return itemRepository.findByTicksLessThan(toTicks);
    }
}
