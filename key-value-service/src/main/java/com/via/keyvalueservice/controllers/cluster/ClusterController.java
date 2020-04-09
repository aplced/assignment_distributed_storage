package com.via.keyvalueservice.controllers.cluster;

import com.via.keyvalueservice.models.cluster.ClusterNode;
import com.via.keyvalueservice.models.cluster.ClusterNodeRepository;
import com.via.keyvalueservice.models.keyvalue.KeyValueItem;
import com.via.keyvalueservice.models.keyvalue.KeyValueItemRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class ClusterController {
    private final List<WebClient> clusterNodes = new ArrayList<>();
    private final ClusterNodeRepository repository;
    private final KeyValueItemRepository itemRepository;

    public ClusterController(ClusterNodeRepository repository, KeyValueItemRepository itemRepository) {
        this.repository = repository;
        this.itemRepository = itemRepository;
    }

    @PostMapping("cluster/internal/joining")
    List<ClusterNode> joiningCluster(@RequestParam(name = "host") String joiningHost) {
        //Get all nodes already in the cluster
        List<ClusterNode> clusterNodes = repository.findAll();
        //It is possible that the joining node is restarting and is already in the cluster
        clusterNodes.removeIf(n -> n.getHostAddress().equals(joiningHost));

        repository.save(new ClusterNode(joiningHost, 0));

        return clusterNodes;
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
}
