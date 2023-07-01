package com.vinsguru.payment.application.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class Customer {

    @Id
    private Integer id;
    private String name;
    private Integer balance;

}
