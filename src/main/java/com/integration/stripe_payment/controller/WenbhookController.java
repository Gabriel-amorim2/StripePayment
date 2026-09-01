package com.integration.stripe_payment.controller;


import com.integration.stripe_payment.service.WebhookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
public class WenbhookController {

    private final WebhookService  webhookService;

    public WenbhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeEvent(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
            webhookService.processWebhookEvent(payload, sigHeader);
            return ResponseEntity.ok("Stripe successful");
    }

    @PostMapping("/cancelSession/{paymentId}")
    public ResponseEntity<String> cancelSession(@PathVariable String paymentId) {
            webhookService.cancelPayment(paymentId);
            return ResponseEntity.ok("Cancel successful");
    }

}

