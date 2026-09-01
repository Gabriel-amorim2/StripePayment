package com.integration.stripe_payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class PaymantRequest {
    @Positive
    private Long amount;
    @NotBlank
    private String name;
    @Positive
    private Long quantity;
    @NotBlank
    private String correcy;
}
