package com.example.withdrawal.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Contract DTO for a successful withdrawal result.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawResponse {

    private String transactionId;
    private BigDecimal feeCharged;
    private BigDecimal newBalance;
    private LocalDateTime timestamp;
}
