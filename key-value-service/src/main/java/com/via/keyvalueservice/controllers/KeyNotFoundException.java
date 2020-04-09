package com.via.keyvalueservice.controllers;

public class KeyNotFoundException extends RuntimeException {
    KeyNotFoundException(String key) {
        super("Key: " + key + " doesn't exist");
    }
}
