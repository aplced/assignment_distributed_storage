package com.via.keyvalueservice.repositories;

import com.via.keyvalueservice.models.KeyValueItem;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface KeyValueItemRepository extends MongoRepository<KeyValueItem, String> {
}
