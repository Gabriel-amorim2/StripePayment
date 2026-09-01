package com.integration.stripe_payment.controller;

import com.integration.stripe_payment.dto.PaymantRequest;
import com.integration.stripe_payment.dto.StripeResponse;
import com.integration.stripe_payment.model.Payment;
import com.integration.stripe_payment.repository.RepositoryPayment;
import com.integration.stripe_payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product/v1")
public class ProductController {

    private final PaymentService paymentService;
    private final RepositoryPayment repositoryPayment;

    public ProductController(PaymentService stripeService, RepositoryPayment repositoryPayment) {
        this.paymentService = stripeService;
        this.repositoryPayment = repositoryPayment;
    }

    @PostMapping("/chekcout")
    public ResponseEntity<StripeResponse> checkout(@Valid @RequestParam String indepotency, @RequestBody PaymantRequest request) {
        StripeResponse response = paymentService.createPayment(request, indepotency);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPayment(@PathVariable("id") Long id) {
        return repositoryPayment.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
