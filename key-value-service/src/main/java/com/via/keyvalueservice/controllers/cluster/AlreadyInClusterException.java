package com.via.keyvalueservice.controllers.cluster;

public class AlreadyInClusterException extends RuntimeException {
    AlreadyInClusterException(String host) {
        super("Already in a cluster. Won't join: " + host);
    }
}
