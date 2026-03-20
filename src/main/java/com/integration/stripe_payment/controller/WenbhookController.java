package com.integration.stripe_payment.controller;

import com.integration.stripe_payment.model.PaymentStatus;
import com.integration.stripe_payment.repository.RepositoryPayment;
import com.stripe.model.Event;

import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wenbhook")
public class WenbhookController {
    private final RepositoryPayment paymentRepository;

    public WenbhookController(RepositoryPayment paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeEvent(@RequestBody String payload,@RequestHeader("Stripe-Signature") String sigHeader) {
        String endpointSecret = "";
        Event event;
        try {
            event = Webhook.constructEvent(
                    payload, sigHeader, endpointSecret
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ivalid signature");
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

        return ResponseEntity.ok("Recieved");
    }



    private void PaymentSucceeded(Event event) {
        PaymentIntent pi = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject().orElseThrow();


        paymentRepository.findBySessionUrl(pi.getId())
                .ifPresent(payment -> {
                    payment.setStatus(PaymentStatus.APROVADO);
                    paymentRepository.save(payment);
                });
    }

    private void PaymentFailed(Event event) {
        PaymentIntent piFailed = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject().orElseThrow();

        paymentRepository.findBySessionUrl(piFailed.getId())
                .ifPresent(payment -> {
                    payment.setStatus(PaymentStatus.RECUSADO);
                    paymentRepository.save(payment);
                });
    }

}

