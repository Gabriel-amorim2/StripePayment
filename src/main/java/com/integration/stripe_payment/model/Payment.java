package com.integration.stripe_payment.model;


import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    private UUID id;

    private String sessionId;
    @Column(length = 2000)
    private String sessionUrl;
    private String paymentIntentId;

    private Long amount;
    private String name;
    private Long quantity;
    private String correcy;


    @Enumerated(EnumType.STRING)
    private PaymentStatus status;


    @Column(unique = true)
    private String idepotency;


}
