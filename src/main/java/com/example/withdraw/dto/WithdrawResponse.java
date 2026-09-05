package com.example.withdraw.dto;

import com.example.withdraw.entity.TransactionStatus;

import java.math.BigDecimal;

/**
 * Immutable record returned for an approved withdrawal.
 */
public record WithdrawResponse(
        String transactionId,
        TransactionStatus status,
        BigDecimal amount,
        String cardNumber) {
}
