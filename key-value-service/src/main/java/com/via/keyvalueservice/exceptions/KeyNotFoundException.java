package com.via.keyvalueservice.exceptions;

public class KeyNotFoundException extends RuntimeException {
    public KeyNotFoundException(String key) {
        super("Key: " + key + " doesn't exist");
    }
}
