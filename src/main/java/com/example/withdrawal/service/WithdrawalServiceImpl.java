package com.example.withdrawal.service;

import com.example.withdrawal.api.InsufficientBalanceException;
import com.example.withdrawal.api.WithdrawRequest;
import com.example.withdrawal.api.WithdrawResponse;
import com.example.withdrawal.api.WithdrawalService;
import com.example.withdrawal.domain.AccountRepository;
import com.example.withdrawal.domain.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * TDD RED stub.
 *
 * <p>The contract (api) and the tests (unit + BDD) are frozen first; this class
 * is intentionally left empty so the test suite starts RED. The "Driver Agent"
 * provides the real implementation in the next step, without touching any file
 * in the {@code api/} or {@code test/} packages.</p>
 */
@Service
@RequiredArgsConstructor
public class WithdrawalServiceImpl implements WithdrawalService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public WithdrawResponse withdraw(WithdrawRequest request) throws InsufficientBalanceException {
        // RED phase — no business logic yet.
        throw new UnsupportedOperationException("Implementation pending — Driver Agent to implement");
    }
}
