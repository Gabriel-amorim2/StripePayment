package com.integration.stripe_payment.service;

import com.integration.stripe_payment.dto.PaymantRequest;
import com.integration.stripe_payment.dto.StripeResponse;
import com.integration.stripe_payment.model.Payment;
import com.integration.stripe_payment.model.PaymentStatus;
import com.integration.stripe_payment.repository.RepositoryPayment;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID;


@Service
public class PaymentService {

    private final ParamsBuilder params;
    private final StripeService stripeService;
    private final RepositoryPayment repo;

    public PaymentService(ParamsBuilder params, StripeService stripeService, RepositoryPayment repo) {
        this.params = params;
        this.stripeService = stripeService;
        this.repo = repo;
    }

    public StripeResponse createPayment(PaymantRequest request, String idepotency) {

        Optional<Payment> existing = repo.findByIdepotency(idepotency);
        if (existing.isPresent()){
            return StripeResponse.builder()
                    .mensage("Existing payment")
                    .status(existing.get().getStatus())
                    .sesionID(existing.get().getSessionId())
                    .build();
        }

        try {
            UUID generetePaymentId = UUID.randomUUID();
            SessionCreateParams paramsCreate = params.params(request, generetePaymentId);
            Session session = stripeService.createCheckoutSession(paramsCreate,idepotency);
            Payment payment = getPayment(request, idepotency, session, generetePaymentId );

            repo.save(payment);

            return StripeResponse.builder()
                    .status(PaymentStatus.PENDENTE)
                    .mensage("Sessão de pagamento criada")
                    .sesionID(session.getId())
                    .sessionUrl(session.getUrl())
                    .build();

        } catch (StripeException e) {
            Payment payment = new Payment();
            payment.setAmount(request.getAmount());
            payment.setCorrecy(request.getCorrecy());
            payment.setName(request.getName());

            repo.save(payment);

            return StripeResponse.builder()
                    .mensage("Erro ao criar sessão: " + e.getMessage())
                    .build();
        }

    }


    private static Payment getPayment(PaymantRequest request, String idepotency, Session session, UUID generetePaymentId) {
        Payment payment = new Payment();
        payment.setId(generetePaymentId);
        payment.setSessionId(session.getId());
        payment.setAmount(request.getAmount());
        payment.setCorrecy(request.getCorrecy());
        payment.setQuantity(request.getQuantity());
        payment.setName(request.getName());
        payment.setSessionUrl(session.getUrl());
        payment.setStatus(PaymentStatus.PENDENTE);
        payment.setIdepotency(idepotency);
        return payment;
    }


}


