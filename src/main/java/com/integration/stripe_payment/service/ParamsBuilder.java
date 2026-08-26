package com.integration.stripe_payment.service;

import com.integration.stripe_payment.dto.PaymantRequest;
import com.stripe.exception.StripeException;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.stereotype.Component;

@Component
public class ParamsBuilder {

    public SessionCreateParams params(PaymantRequest request) throws StripeException {
        SessionCreateParams.LineItem.PriceData.ProductData productData = SessionCreateParams.LineItem.PriceData.ProductData.builder()
                .setName(request.getName())
                .build();

        SessionCreateParams.LineItem.PriceData priceData = SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency(request.getCorrecy() == null ? "BRL" : request.getCorrecy())
                .setUnitAmount(request.getAmount())
                .setProductData(productData)
                .build();

        SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                .setQuantity(request.getQuantity())
                .setPriceData(priceData)
                .build();

        return SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:8080/success")
                .setCancelUrl("http://localhost:8080/cancel")
                .addLineItem(lineItem)
                .build();


    }

}
