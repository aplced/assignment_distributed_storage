package com.via.keyvalueservice.models;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface KeyValueItemRepository extends MongoRepository<KeyValueItem, String> {
}
