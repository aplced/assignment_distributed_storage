package com.via.keyvalueservice.cluster.controllers;

import com.via.keyvalueservice.cluster.models.ClusterNode;
import com.via.keyvalueservice.cluster.models.ClusterNodeRepository;
import com.via.keyvalueservice.keyvalue.models.KeyValueItem;
import com.via.keyvalueservice.keyvalue.models.KeyValueItemRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class ClusterController {
    private final ClusterNodeRepository repository;
    private final KeyValueItemRepository itemRepository;

    public ClusterController(ClusterNodeRepository repository, KeyValueItemRepository itemRepository) {
        this.repository = repository;
        this.itemRepository = itemRepository;
    }

    @PostMapping("cluster/internal/joining")
    List<ClusterNode> joiningCluster(@RequestParam(name = "host") String joiningHost, @RequestParam("nodeList") Optional<Boolean> listClusterNodes) {

        repository.save(new ClusterNode(joiningHost, 0));

        if(listClusterNodes.isPresent() && listClusterNodes.get()) {
            //Get all nodes already in the cluster
            List<ClusterNode> clusterNodes = repository.findAll();
            //It is possible that the joining node is restarting and is already in the cluster
            clusterNodes.removeIf(n -> n.getHostAddress().equals(joiningHost));

            return clusterNodes;
        } else {
            return new ArrayList<>();
        }
    }

    @PostMapping("cluster/internal/leaving")
    void leavingCluster(@RequestParam(name = "host") String leavingHost) {
        repository.deleteById(leavingHost);
    }

    @PostMapping("cluster/internal/update")
    KeyValueItem updateKeyValueItem(@RequestBody KeyValueItem updatingItem) {
        Optional<KeyValueItem> myItem = itemRepository.findById(updatingItem.getKey());
        //Update this nodes key value item only if it doesn't exist or if the update is fresher
        if(!myItem.isPresent() || updatingItem.getTicks() > myItem.get().getTicks()) {
            itemRepository.save(updatingItem);
            return updatingItem;
        } else {
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
}
