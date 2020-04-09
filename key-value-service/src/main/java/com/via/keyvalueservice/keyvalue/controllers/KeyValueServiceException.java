package com.via.keyvalueservice.keyvalue.controllers;

public class KeyValueServiceException extends RuntimeException {
    KeyValueServiceException(String key) {
        super("Key: " + key + " doesn't exist");
    }
}
