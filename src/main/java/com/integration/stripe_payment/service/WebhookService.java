package com.integration.stripe_payment.service;

import com.integration.stripe_payment.exception.*;
import com.integration.stripe_payment.model.PaymentStatus;
import com.integration.stripe_payment.repository.RepositoryPayment;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
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
                    payload, sigHeader, endpointSecret);

        } catch (SignatureVerificationException e) {
            throw new ErrorEvent("assinatura do webhook invalida", e);
        }

        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = dataObjectDeserializer.getObject().orElseGet(() ->
        {
            try {
                return dataObjectDeserializer.deserializeUnsafe();
            } catch (EventDataObjectDeserializationException e) {
                throw new EventDataDeserializationException("Falha ao desserializar payload do Stripe", e);
            }});

        switch (event.getType()) {
            case "checkout.session.completed" -> {
                if (stripeObject instanceof Session session) {
                   atualizarStatusPagamento( checkoutSuccess(session), PaymentStatus.APROVADO);
                }
            }
            case "payment_intent.payment_failed" -> {
                if (stripeObject instanceof PaymentIntent paymentIntent) {
                  atualizarStatusPagamento(paymentFailed(paymentIntent), PaymentStatus.RECUSADO);
                }

            }
            case "checkout.session.expired" -> {
                if (stripeObject instanceof Session session) {
                   atualizarStatusPagamento(checkoutSuccess(session), PaymentStatus.CANCELADO);
                }
            }
            default -> throw new Unaddressedevent("Evento Stripe não tratado");
        }
    }

    public void cancelPayment(String sessionId) {
        try {
            Session session = Session.retrieve(sessionId);
            session.expire();
        } catch (StripeException e) {
            throw new ErrorCancelEpayment("Erro ao expirar a sessão de checkout no Stripe",e);
        }
    }


    private String checkoutSuccess(Session session) {
       return session.getMetadata() != null ? session.getMetadata().get("payment_id") : null;

    }

    private String paymentFailed(PaymentIntent paymentIntent) {
      return paymentIntent.getMetadata() != null ? paymentIntent.getMetadata().get("payment_id") : null;

    }

    private void atualizarStatusPagamento(String paymentIdStr, PaymentStatus status) {
        if (paymentIdStr != null && !paymentIdStr.isBlank()) {
            try {
                paymentRepository.findById(UUID.fromString(paymentIdStr))
                        .ifPresent(payment -> {
                            payment.setStatus(status);
                            paymentRepository.save(payment);
                        });
            }catch (NullPointerException e){
                throw new PaymentNotFound("Erro ao tentar atualizar o status do pagamento na base de dados", e);
            }

        }
    }
}
