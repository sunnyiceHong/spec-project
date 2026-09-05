package com.example.withdraw.dto;

/**
 * Error body returned over HTTP when a withdrawal is rejected.
 *
 * @param error the {@link com.example.withdraw.entity.WithdrawError} code name
 */
public record WithdrawErrorResponse(String error) {
}
