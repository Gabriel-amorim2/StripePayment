package com.integration.stripe_payment.exception;

public class PaymentNotFound extends RuntimeException {

    public PaymentNotFound(String message, Throwable cause) {
        super(message,cause);
    }
}
