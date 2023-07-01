package com.vinsguru.inventory.application.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class Product {

    @Id
    private Integer id;
    private String description;
    private Integer availableQuantity;

}
