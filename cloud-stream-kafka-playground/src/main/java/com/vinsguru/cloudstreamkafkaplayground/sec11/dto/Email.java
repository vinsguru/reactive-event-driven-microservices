package com.vinsguru.cloudstreamkafkaplayground.sec11.dto;

import com.vinsguru.cloudstreamkafkaplayground.sec11.dto.ContactMethod;

public record Email(String email) implements ContactMethod {
    @Override
    public void contact() {
        System.out.println("contacting via " + email);
    }
}
