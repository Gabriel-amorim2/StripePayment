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


@Service
public class PaymentService {

    private ParamsBuilder params;
    private StripeService stripeService;
    private RepositoryPayment repo;

    public PaymentService(ParamsBuilder params, StripeService stripeService, RepositoryPayment repo) {
        this.params = params;
        this.stripeService = stripeService;
        this.repo = repo;
    }

    public StripeResponse createPayment(PaymantRequest request) {

      String idepotency = request.getName()+ "_" + request.getAmount()+ System.currentTimeMillis()/1000;
        Optional<Payment> existing = repo.findByIdepotency(idepotency);

        if (existing.isPresent()){
            return StripeResponse.builder()
                    .mensage("Existing payment")
                    .status(existing.get().getStatus())
                    .sesionID(existing.get().getSessionId())
                    .build();
        }

        try {

            SessionCreateParams paramsCreate = params.params(request);
            Session session = stripeService.createCheckoutSession(paramsCreate,idepotency);

            Payment payment = new Payment();
            payment.setSessionId(session.getId());
            payment.setPaymentIntentId(session.getPaymentIntent());
            payment.setAmount(request.getAmount());
            payment.setCorrecy(request.getCorrecy());
            payment.setQuantity(request.getQuantity());
            payment.setName(request.getName());
            payment.setStatus(PaymentStatus.APROVADO);

            payment.setIdepotency(idepotency);


            repo.save(payment);


            return StripeResponse.builder()
                    .status(PaymentStatus.APROVADO)
                    .mensage("Sessão de pagamento criada")
                    .sesionID(session.getId())
                    .sessionUrl(session.getUrl())
                    .build();

        } catch (StripeException e) {
            Payment payment= new Payment();
            payment.setAmount(request.getAmount());
            payment.setCorrecy(request.getCorrecy());
            payment.setName(request.getName());
            payment.setStatus(PaymentStatus.RECUSADO);

            repo.save(payment);

            return StripeResponse.builder()
                    .status(PaymentStatus.RECUSADO)
                    .mensage("Erro ao criar sessão: " + e.getMessage())
                    .build();
        }

    }
}


