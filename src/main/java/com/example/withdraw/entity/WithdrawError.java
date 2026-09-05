package com.example.withdraw.entity;

/**
 * Domain error codes returned by the withdrawal service on the failure side of
 * its {@link io.vavr.control.Either} result.
 */
public enum WithdrawError {
    INVALID_USER,
    INVALID_AMOUNT,
    INVALID_CARD_NUMBER,
    CARD_NOT_OWNED,
    INSUFFICIENT_FUNDS
}
