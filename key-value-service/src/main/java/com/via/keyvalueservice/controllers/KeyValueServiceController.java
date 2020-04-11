package com.via.keyvalueservice.controllers;

import com.via.keyvalueservice.exceptions.KeyNotFoundException;
import com.via.keyvalueservice.models.KeyValueItem;
import com.via.keyvalueservice.repositories.KeyValueItemRepository;
import io.swagger.annotations.*;
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

@Api(value="Employee Management System", description="Operations pertaining to employee in Employee Management Syste")
@RestController
@Validated
public class KeyValueServiceController {
    private final KeyValueItemRepository keyValueItemRepository;

    public KeyValueServiceController(KeyValueItemRepository keyValueItemRepository) {
        this.keyValueItemRepository = keyValueItemRepository;
    }

    @GetMapping("/set")
    @ApiOperation(value = "Creates or updates a key with value")
    void set(
            @ApiParam(name="k", value="String key with max length of 64")
            @RequestParam(name = "k") @Size(min = 1, max = 64) String key,
            @ApiParam(name="v", value="String value with max length of 256")
            @RequestParam(name = "v") @Size(min = 1, max = 256) String value
    ) {
        KeyValueItem item = new KeyValueItem(key, value);
        keyValueItemRepository.save(item);
    }

    @GetMapping("/is")
    @ApiOperation(value = "Checks if key exists")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Key exists"),
            @ApiResponse(code = 404, message = "Key not found")
    })
    void is(
            @ApiParam(name="k", value="String key with max length of 64")
            @RequestParam(name = "k") String key
    ) {
        keyValueItemRepository.findById(key).orElseThrow(() -> new KeyNotFoundException(key));
    }

    @GetMapping("/get")
    @ApiOperation(value = "Gets the value of key")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Value of key", response = String.class),
            @ApiResponse(code = 404, message = "Key not found")
    })
    String get(
            @ApiParam(name="k", value="String key with max length of 64")
            @RequestParam(name = "k") String key
    ) {
        return keyValueItemRepository.findById(key).orElseThrow(() -> new KeyNotFoundException(key)).getValue();
    }

    @GetMapping("/rm")
    @ApiOperation(value = "Deletes key")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully deleted key-value pair"),
            @ApiResponse(code = 404, message = "Key not found")
    })
    void rm(
            @ApiParam(name="k", value="String key with max length of 64")
            @RequestParam(name = "k") String key
    ) {
        KeyValueItem item = keyValueItemRepository.findById(key).orElseThrow(() -> new KeyNotFoundException(key));
        keyValueItemRepository.delete(item);
    }

    @GetMapping("/clear")
    @ApiOperation(value = "Deletes all items")
    void clear() {
        keyValueItemRepository.deleteAll();
    }

    //getAll takes an optional page number parameter "p" which selects key value items in batches of 500
    //if the parameter is not supplied the whole data is fetched at once.
    @GetMapping("/getAll")
    @ApiOperation(value = "Returns an array with all key-value pairs")
    @ApiResponse(code = 200, message = "List of all key-value pairs", response = KeyValueItem[].class)
    List<KeyValueItem> getAll(
            @ApiParam(name="p", value="Optional index of a page with size 500. If not provided whole data set is returned")
            @RequestParam(name = "p") Optional<Integer> pageNumber
    ) {
        if(pageNumber.isPresent()) {
            Pageable pageable = PageRequest.of(pageNumber.get(), 500);
            return keyValueItemRepository.findAll(pageable).getContent();
        } else {
            return keyValueItemRepository.findAll();
        }
    }

    @GetMapping("/getKeys")
    @ApiOperation(value = "Returns an array with all keys")
    @ApiResponse(code = 200, message = "List of all keys", response = String[].class)
    List<String> getKeys(
            @ApiParam(name="p", value="Optional index of a page with size 500. If not provided whole data set is returned")
            @RequestParam(name = "p") Optional<Integer> pageNumber
    ) {
        List<KeyValueItem> keys;
        if(pageNumber.isPresent()) {
            Pageable pageable = PageRequest.of(pageNumber.get(), 500);
            keys = keyValueItemRepository.findAll(pageable).getContent();
        } else {
            keys = keyValueItemRepository.findAll();
        }

        return keys.stream().map(i -> i.getKey()).collect(Collectors.toList());
    }

    @GetMapping("/getValues")
    @ApiOperation(value = "Returns an array with all values")
    @ApiResponse(code = 200, message = "List of all values", response = String[].class)
    List<String> getValues(
            @ApiParam(name="p", value="Optional index of a page with size 500. If not provided whole data set is returned")
            @RequestParam(name = "p") Optional<Integer> pageNumber
    ) {
        List<KeyValueItem> keys;
        if(pageNumber.isPresent()) {
            Pageable pageable = PageRequest.of(pageNumber.get(), 500);
            keys = keyValueItemRepository.findAll(pageable).getContent();
        } else {
            keys = keyValueItemRepository.findAll();
        }

        return keys.stream().map(i -> i.getValue()).collect(Collectors.toList());
    }
}
