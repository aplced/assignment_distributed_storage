package com.via.keyvalueservice.cluster;

import com.via.keyvalueservice.keyvalue.models.KeyValueItem;

public class ClusterNodeUpdateCollisionException extends RuntimeException {
    ClusterNodeUpdateCollisionException(KeyValueItem localItem, KeyValueItem remoteItem, String remoteHost) {
        super("Trying to update [" + localItem + "] but newer update exists [" + remoteItem + "] on host [" + remoteHost + "]");
    }
}
