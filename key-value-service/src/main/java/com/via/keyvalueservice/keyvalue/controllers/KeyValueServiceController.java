package com.via.keyvalueservice.keyvalue.controllers;

import com.via.keyvalueservice.cluster.ClusterNodeUpdater;
import com.via.keyvalueservice.keyvalue.models.KeyValueItem;
import com.via.keyvalueservice.keyvalue.models.KeyValueItemRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@Validated
public class KeyValueServiceController {
    private final KeyValueItemRepository keyValueItemRepository;
    private final ClusterNodeUpdater clusterNodeUpdater;

    public KeyValueServiceController(KeyValueItemRepository keyValueItemRepository, ClusterNodeUpdater clusterNodeUpdater) {
        this.keyValueItemRepository = keyValueItemRepository;
        this.clusterNodeUpdater = clusterNodeUpdater;
    }

    @GetMapping("/set")
    void set(@RequestParam(name = "k") @Size(min = 1, max = 64) String key, @RequestParam(name = "v") @Size(min = 1, max = 256) String value) {
        KeyValueItem item = new KeyValueItem(key, value, LocalDateTime.now().toEpochSecond(ZoneOffset.UTC), false);
        clusterNodeUpdater.updateKeyValueItem(item);
    }

    @GetMapping("/is")
    void is(@RequestParam(name = "k") String key) {
        if(keyValueItemRepository.findById(key).orElseThrow(() -> new KeyValueServiceException(key)).isDeleted()){
            throw new KeyValueServiceException(key);
        }
    }

    @GetMapping("/get")
    String get(@RequestParam(name = "k") String key) {
        KeyValueItem item = keyValueItemRepository.findById(key).orElseThrow(() -> new KeyValueServiceException(key));
        if(item.isDeleted()){
            throw new KeyValueServiceException(key);
        }

        return item.getValue();
    }

    @GetMapping("/rm")
    void rm(@RequestParam(name = "k") String key) {
        KeyValueItem item = keyValueItemRepository.findById(key).orElseThrow(() -> new KeyValueServiceException(key));
        item.setDeleted(true);
        clusterNodeUpdater.updateKeyValueItem(item);
    }

    @GetMapping("/clear")
    void clear() {
        List<KeyValueItem> deletedItems = keyValueItemRepository.findAll().stream().map(i -> {
            i.setDeleted(true);
            return i;
        }).collect(Collectors.toList());
        clusterNodeUpdater.updateKeyValueItems(deletedItems);
    }

    //getAll takes an optional page number parameter "p" which selects key value items in batches of 500
    //if the parameter is not supplied the whole data is fetched at once.
    @GetMapping("/getAll")
    List<KeyValueItem> getAll(@RequestParam(name = "p") Optional<Integer> pageNumber) {
        if(pageNumber.isPresent()) {
            Pageable pageable = PageRequest.of(pageNumber.get(), 500);
            return keyValueItemRepository.findAll(pageable).getContent();
        } else {
            return keyValueItemRepository.findAll();
        }
    }

    @GetMapping("/getKeys")
    List<String> getKeys() {
        return keyValueItemRepository.findAll().stream().filter(i -> !i.isDeleted()).map(i -> i.getKey()).collect(Collectors.toList());
    }

    @GetMapping("/getValues")
    List<String> getValues() {
        return keyValueItemRepository.findAll().stream().filter(i -> !i.isDeleted()).map(i -> i.getValue()).collect(Collectors.toList());
    }
}
