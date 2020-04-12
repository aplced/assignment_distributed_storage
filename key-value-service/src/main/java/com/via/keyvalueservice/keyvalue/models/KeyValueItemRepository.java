package com.via.keyvalueservice.keyvalue.models;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KeyValueItemRepository extends JpaRepository<KeyValueItem, String> {
    List<KeyValueItem> findByTicksLessThan(long ticks);
}
