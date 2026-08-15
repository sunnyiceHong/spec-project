package com.example.withdrawal.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Contract DTO for a withdrawal request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawRequest {

    @NotBlank(message = "userId must not be blank")
    private String userId;

    @NotNull(message = "amount must not be null")
    @DecimalMin(value = "0.00", inclusive = false, message = "amount must be greater than zero")
    private BigDecimal amount;
}
