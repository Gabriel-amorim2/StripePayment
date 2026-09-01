package com.integration.stripe_payment.exception;

public class ErrorCancelEpayment extends  RuntimeException{

    public ErrorCancelEpayment(String message, Throwable cause) {
        super(message, cause);
    }
}
