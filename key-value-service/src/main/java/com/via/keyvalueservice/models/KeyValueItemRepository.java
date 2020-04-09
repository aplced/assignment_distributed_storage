package com.via.keyvalueservice.models;

import org.springframework.data.jpa.repository.JpaRepository;

public interface KeyValueItemRepository extends JpaRepository<KeyValueItem, String> {
}
