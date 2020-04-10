package com.via.keyvalueservice.cluster.controllers;

import com.via.keyvalueservice.cluster.ClusterNodeUpdater;
import com.via.keyvalueservice.cluster.models.ClusterNode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ClusterControlController {
    private final ClusterNodeUpdater clusterNodeUpdater;

    public ClusterControlController(ClusterNodeUpdater clusterNodeUpdater) {
        this.clusterNodeUpdater = clusterNodeUpdater;
    }

    @PostMapping("/cluster/join")
    void joinCLuster(@RequestParam(name = "host") String hostToJoin) {
        clusterNodeUpdater.joinCluster(hostToJoin);
    }

    @PostMapping("/cluster/leave")
    void leaveCluster() {
        clusterNodeUpdater.leaveCluster();
    }

    @GetMapping("/cluster/list")
    List<ClusterNode> listCluster() {
        return clusterNodeUpdater.clusterNodes();
    }
}
