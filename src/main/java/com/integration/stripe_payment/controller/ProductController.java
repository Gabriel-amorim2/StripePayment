package com.integration.stripe_payment.controller;

import com.integration.stripe_payment.dto.PaymantRequest;
import com.integration.stripe_payment.dto.StripeResponse;
import com.integration.stripe_payment.model.Payment;
import com.integration.stripe_payment.repository.RepositoryPayment;
import com.integration.stripe_payment.service.PaymentService;
import com.stripe.exception.StripeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product/v1")
public class ProductController {

    private PaymentService stripeService;
    private RepositoryPayment repositoryPayment;

    public ProductController(PaymentService stripeService, RepositoryPayment repositoryPayment) {
        this.stripeService = stripeService;
        this.repositoryPayment = repositoryPayment;
    }

    @PostMapping("/chekcout")
    public ResponseEntity<StripeResponse> checkout(@RequestBody PaymantRequest request) throws StripeException {
        StripeResponse response = stripeService.createPayment(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPayment(@PathVariable("id") Long id) {
        return repositoryPayment.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


}
