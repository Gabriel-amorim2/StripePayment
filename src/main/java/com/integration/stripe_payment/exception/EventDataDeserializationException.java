package com.integration.stripe_payment.exception;

public class EventDataDeserializationException extends RuntimeException {
    public EventDataDeserializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
