package com.integration.stripe_payment.service;

import com.integration.stripe_payment.exception.ErroEvent;
import com.integration.stripe_payment.model.PaymentStatus;
import com.integration.stripe_payment.repository.RepositoryPayment;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

@Service
public class WebhookService {

    @Value("${STRIPE_WEBHOOK_SECRET}")
    private String endpointSecret;

    private final RepositoryPayment paymentRepository;

    public WebhookService(RepositoryPayment paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public void eventStripe (String payload, String sigHeader){
        Event event;
        try {
            event = Webhook.constructEvent(
                    payload, sigHeader, endpointSecret
            );
        } catch (Exception e) {
           throw new ErroEvent(e.getMessage());
        }


        switch (event.getType()) {

            case "payment_intent.succeeded":
                PaymentSucceeded(event);
                break;
            case "payment_intent.payment_failed":
                PaymentFailed(event);
                break;
            default:
                break;
        }

    }

    private void PaymentSucceeded(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject().orElseThrow();


        paymentRepository.findByPaymentIntentId(paymentIntent.getId())
                .ifPresent(payment -> {
                    payment.setStatus(PaymentStatus.APROVADO);
                    paymentRepository.save(payment);
                });
    }

    private void PaymentFailed(Event event) {
        PaymentIntent piFailed = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject().orElseThrow();

        paymentRepository.findByPaymentIntentId(piFailed.getId())
                .ifPresent(payment -> {
                    payment.setStatus(PaymentStatus.RECUSADO);
                    paymentRepository.save(payment);
                });
    }

}
