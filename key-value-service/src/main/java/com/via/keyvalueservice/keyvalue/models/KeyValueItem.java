package com.via.keyvalueservice.keyvalue.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class KeyValueItem {
    private @Id String key;
    private String value;
    private long ticks;
    private boolean deleted;
}
