package com.via.keyvalueservice.cluster;

public class AlreadyInClusterException extends RuntimeException {
    AlreadyInClusterException(String host) {
        super("Already in a cluster. Won't join: " + host);
    }
}