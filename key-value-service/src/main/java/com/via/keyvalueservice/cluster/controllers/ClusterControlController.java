package com.via.keyvalueservice.cluster.controllers;

import com.via.keyvalueservice.cluster.models.ClusterNode;
import com.via.keyvalueservice.cluster.models.ClusterNodeRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ClusterControlController {
    private final ClusterNodeRepository repository;

    public ClusterControlController(ClusterNodeRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/cluster/join")
    void joinCLuster(@RequestParam(name = "host") String hostToJoin) {
        if(repository.count() > 0){
            throw new AlreadyInClusterException(hostToJoin);
        }

        repository.save(new ClusterNode(hostToJoin, 0));
    }

    @PostMapping("/cluster/leave")
    void leaveCluster() {
        repository.deleteAll();
    }

    @GetMapping("/cluster/list")
    List<ClusterNode> listCluster() {
        return repository.findAll();
    }
}
