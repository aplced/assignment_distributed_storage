package com.via.keyvalueservice.models.cluster;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClusterNodeRepository extends JpaRepository<ClusterNode, String> {
}
