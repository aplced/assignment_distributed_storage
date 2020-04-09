package com.via.keyvalueservice.cluster.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ClusterNode {
    private @Id String hostAddress;
    private long lastUpdateSent;
}
