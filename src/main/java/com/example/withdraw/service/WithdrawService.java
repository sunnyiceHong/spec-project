package com.example.withdraw.service;

import com.example.withdraw.dto.WithdrawRequest;
import com.example.withdraw.dto.WithdrawResponse;
import com.example.withdraw.entity.WithdrawError;
import io.vavr.control.Either;

/**
 * Frozen API contract for the withdrawal feature.
 *
 * <p>The method returns an {@link Either} of a domain error or an approved
 * response; it never returns {@code null} and never throws a checked exception.</p>
 */
public interface WithdrawService {

    Either<WithdrawError, WithdrawResponse> withdraw(WithdrawRequest request);
}
