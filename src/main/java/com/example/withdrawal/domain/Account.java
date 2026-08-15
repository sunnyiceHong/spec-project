package com.example.withdrawal.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Simple in-memory account domain entity (no real database needed for the demo).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    private String userId;
    private BigDecimal balance;
    private boolean isVip;
}
