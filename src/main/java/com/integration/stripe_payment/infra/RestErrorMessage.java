package com.integration.stripe_payment.infra;

import org.springframework.http.HttpStatus;

import java.time.Instant;


public record RestErrorMessage(int status,String message,String error, Instant time) {

    public RestErrorMessage(HttpStatus status, String message) {
        this(status.value(),status.getReasonPhrase(), message, Instant.now());
    }
}
