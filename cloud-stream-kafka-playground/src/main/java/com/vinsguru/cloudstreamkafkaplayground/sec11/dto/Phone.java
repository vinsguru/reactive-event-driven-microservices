package com.vinsguru.cloudstreamkafkaplayground.sec11.dto;

public record Phone(int number) implements ContactMethod {
    @Override
    public void contact() {
        System.out.println("contacting via " + number);
    }
}
