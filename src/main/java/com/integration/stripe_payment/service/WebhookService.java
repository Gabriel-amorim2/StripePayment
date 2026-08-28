package com.integration.stripe_payment.service;

import com.integration.stripe_payment.exception.ErroEvent;
import com.integration.stripe_payment.exception.EventDataDeserializationException;
import com.integration.stripe_payment.model.PaymentStatus;
import com.integration.stripe_payment.repository.RepositoryPayment;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class WebhookService {

    @Value("${STRIPE_WEBHOOK_SECRET}")
    private String endpointSecret;

    private final RepositoryPayment paymentRepository;

    public WebhookService(RepositoryPayment paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public void processWebhookEvent(String payload, String sigHeader) {

        Event event;
        try {
            event = Webhook.constructEvent(
                    payload, sigHeader, endpointSecret
            );
        } catch (Exception e) {
            throw new ErroEvent(e.getMessage());
        }

        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = dataObjectDeserializer.getObject().orElseGet(() ->
        {
            try {
                return dataObjectDeserializer.deserializeUnsafe();
            } catch (EventDataDeserializationException | EventDataObjectDeserializationException e) {
                throw new RuntimeException("Falha ao desserializar payload do Stripe", e);
            }});

        switch (event.getType()) {
            case "checkout.session.completed" -> {
                if (stripeObject instanceof Session session) {
                    checkoutSuccess(session);
                }
            }
            case "payment_intent.payment_failed" -> {
                if (stripeObject instanceof PaymentIntent paymentIntent) {
                    paymentFailed(paymentIntent);
                }

            }
            case "checkout.session.expired" -> {
                if (stripeObject instanceof Session session) {
                    String paymentIdStr = session.getMetadata() != null
                            ? session.getMetadata().get("payment_id")
                            : null;

                    atualizarStatusPagamento(paymentIdStr, PaymentStatus.CANCELADO);
                }
            }
            default -> System.out.println("Evento não tratado: " + event.getType());
        }
    }

    public void cancelPayment(String sessionId) {
        try {
            Session session = Session.retrieve(sessionId);
            session.expire();
        } catch (StripeException e) {
            throw new RuntimeException("Erro ao expirar a sessão de checkout no Stripe",e);
        }
    }


    private void checkoutSuccess(Session session) {
        String paymentIdStr = session.getMetadata() != null ? session.getMetadata().get("payment_id") : null;
        atualizarStatusPagamento(paymentIdStr, PaymentStatus.APROVADO);
    }

    private void paymentFailed(PaymentIntent paymentIntent) {
        String paymentIdStr = paymentIntent.getMetadata() != null ? paymentIntent.getMetadata().get("payment_id") : null;
        atualizarStatusPagamento(paymentIdStr, PaymentStatus.RECUSADO);
    }

    private void atualizarStatusPagamento(String paymentIdStr, PaymentStatus status) {
        if (paymentIdStr != null && !paymentIdStr.isBlank()) {
            paymentRepository.findById(UUID.fromString(paymentIdStr))
                    .ifPresent(payment -> {
                        payment.setStatus(status);
                        paymentRepository.save(payment);
                    });
        }
    }
}
