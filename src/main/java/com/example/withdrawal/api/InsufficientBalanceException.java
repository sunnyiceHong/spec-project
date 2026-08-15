package com.example.withdrawal.api;

/**
 * Thrown when a withdrawal cannot proceed because the account balance is
 * insufficient to cover the principal plus the withdrawal fee.
 *
 * <p>Declared on the {@link WithdrawalService} contract. It extends
 * {@link RuntimeException} so callers are not forced to wrap it, while still
 * being an explicit part of the API surface.</p>
 */
public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(String message) {
        super(message);
    }
}
