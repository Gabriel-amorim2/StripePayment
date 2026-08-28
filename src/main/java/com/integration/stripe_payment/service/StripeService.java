package com.integration.stripe_payment.service;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.stereotype.Service;


@Service
public class StripeService {
    public Session createCheckoutSession(SessionCreateParams params, String idepotencyKey) throws StripeException {
        RequestOptions options = RequestOptions.builder().
                setIdempotencyKey(idepotencyKey)
                .build();
        return Session.create(params,options);
    }

}
