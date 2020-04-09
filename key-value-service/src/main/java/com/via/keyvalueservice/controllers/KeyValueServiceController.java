package com.via.keyvalueservice.controllers;

import com.via.keyvalueservice.models.KeyValueItem;
import com.via.keyvalueservice.models.KeyValueItemRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Size;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@Validated
public class KeyValueServiceController {
    private final KeyValueItemRepository keyValueItemRepository;

    public KeyValueServiceController(KeyValueItemRepository keyValueItemRepository) {
        this.keyValueItemRepository = keyValueItemRepository;
    }

    @GetMapping("/set")
    void set(@RequestParam(name = "k") @Size(min = 1, max = 64) String key, @RequestParam(name = "v") @Size(min = 1, max = 256) String value) {
        KeyValueItem item = new KeyValueItem(key, value);
        keyValueItemRepository.save(item);
    }

    @GetMapping("/is")
    void is(@RequestParam(name = "k") String key) {
        keyValueItemRepository.findById(key).orElseThrow(() -> new KeyNotFoundException(key));
    }

    @GetMapping("/get")
    String get(@RequestParam(name = "k") String key) {
        return keyValueItemRepository.findById(key).orElseThrow(() -> new KeyNotFoundException(key)).getValue();
    }

    @GetMapping("/rm")
    void rm(@RequestParam(name = "k") String key) {
        KeyValueItem item = keyValueItemRepository.findById(key).orElseThrow(() -> new KeyNotFoundException(key));
        keyValueItemRepository.delete(item);
    }

    @GetMapping("/clear")
    void clear() {
        keyValueItemRepository.deleteAll();
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
        return keyValueItemRepository.findAll().stream().map(i -> i.getKey()).collect(Collectors.toList());
    }

    @GetMapping("/getValues")
    List<String> getValues() {
        return keyValueItemRepository.findAll().stream().map(i -> i.getValue()).collect(Collectors.toList());
    }
}
