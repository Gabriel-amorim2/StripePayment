package com.integration.stripe_payment.exception;

public class ErrorEvent extends RuntimeException {
    public ErrorEvent(String message, Throwable cause) {
        super(message,cause);
    }
}
