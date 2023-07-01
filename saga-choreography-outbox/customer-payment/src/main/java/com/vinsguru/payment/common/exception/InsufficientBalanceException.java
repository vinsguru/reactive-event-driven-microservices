package com.vinsguru.payment.common.exception;

public class InsufficientBalanceException extends RuntimeException {

    private static final String MESSAGE = "Customer does not have enough balance";

    public InsufficientBalanceException() {
        super(MESSAGE);
    }

}
