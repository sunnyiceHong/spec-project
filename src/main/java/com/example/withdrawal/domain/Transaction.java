package com.example.withdrawal.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Record of a money movement. Created for every successful withdrawal.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    private String id;
    private String userId;
    private BigDecimal amount;
    private BigDecimal fee;
    private LocalDateTime timestamp;
    private TransactionType type;
}
