package com.via.keyvalueservice.controllers;

import com.via.keyvalueservice.models.KeyValueItem;
import com.via.keyvalueservice.models.KeyValueItemRepository;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Size;

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
}
