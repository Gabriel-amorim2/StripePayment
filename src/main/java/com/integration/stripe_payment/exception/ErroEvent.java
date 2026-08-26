package com.integration.stripe_payment.exception;

public class ErroEvent extends RuntimeException {
    public ErroEvent(String message) {
        super(message);
    }
}
