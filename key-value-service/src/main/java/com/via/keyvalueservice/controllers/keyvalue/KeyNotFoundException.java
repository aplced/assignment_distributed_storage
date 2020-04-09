package com.via.keyvalueservice.controllers.keyvalue;

public class KeyNotFoundException extends RuntimeException {
    KeyNotFoundException(String key) {
        super("Key: " + key + " doesn't exist");
    }
}
