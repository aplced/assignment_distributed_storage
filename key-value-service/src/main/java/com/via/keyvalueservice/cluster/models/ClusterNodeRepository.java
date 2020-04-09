package com.via.keyvalueservice.cluster.models;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClusterNodeRepository extends JpaRepository<ClusterNode, String> {
}
