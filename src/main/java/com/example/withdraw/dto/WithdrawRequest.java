package com.example.withdraw.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Immutable withdrawal request.
 *
 * <p>Jakarta Validation annotations document the business rules enforced by the
 * service; the service itself performs the same checks defensively so invalid
 * requests are rejected even when called without a bean-validator.</p>
 */
public record WithdrawRequest(
        @NotBlank String userId,
        @NotNull @Positive BigDecimal amount,
        @NotBlank @Pattern(regexp = "\\d{16}") String cardNumber) {
}
