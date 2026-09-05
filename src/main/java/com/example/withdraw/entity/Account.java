package com.example.withdraw.entity;

import java.math.BigDecimal;

/**
 * Immutable account snapshot used by the account store.
 *
 * <p>{@code cardNumber} is the single bank card owned by this account; the
 * withdrawal service uses it to verify that a requested card belongs to the
 * user before debiting.</p>
 */
public record Account(String userId, BigDecimal balance, String cardNumber) {
}
