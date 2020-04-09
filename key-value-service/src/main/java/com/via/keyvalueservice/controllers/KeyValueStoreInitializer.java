package com.via.keyvalueservice.controllers;

import com.via.keyvalueservice.models.KeyValueItem;
import com.via.keyvalueservice.models.KeyValueItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

//Only used for test initialization of the repository with 1000000 items
@Component
public class KeyValueStoreInitializer {

    @Value( "${dbtest.initialize}" )
    Boolean initialize;

    @Autowired
    private KeyValueItemRepository repository;

    @PostConstruct
    public void init() {
        //If the items are present in the repository don't bother to fill them up again
        if(initialize && !repository.findById("1_key").isPresent()) {
            KeyValueItem item = new KeyValueItem();
            for (int i = 0; i < 1000000; i++) {
                item.setKey(i + "_key");
                item.setValue(i + "_value");
                repository.save(item);
            }
        }
    }
}