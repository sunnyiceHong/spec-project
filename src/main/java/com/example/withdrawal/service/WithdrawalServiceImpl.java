package com.example.withdrawal.service;

import com.example.withdrawal.api.InsufficientBalanceException;
import com.example.withdrawal.api.WithdrawRequest;
import com.example.withdrawal.api.WithdrawResponse;
import com.example.withdrawal.api.WithdrawalService;
import com.example.withdrawal.domain.Account;
import com.example.withdrawal.domain.AccountRepository;
import com.example.withdrawal.domain.Transaction;
import com.example.withdrawal.domain.TransactionRepository;
import com.example.withdrawal.domain.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * The "Driver Agent" implementation of {@link WithdrawalService}.
 *
 * <p>This is the only class containing business logic. It satisfies the frozen
 * contract and every unit + BDD test without touching anything in the
 * {@code api/} or {@code test/} packages. All monetary arithmetic uses
 * {@link BigDecimal} — never {@code double}/{@code Float}.</p>
 */
@Service
@RequiredArgsConstructor
public class WithdrawalServiceImpl implements WithdrawalService {

    /** Standard withdrawal fee: 1% of the principal. */
    private static final BigDecimal STANDARD_FEE_RATE = new BigDecimal("0.01");

    /** VIP discount: VIP users pay 50% of the standard fee. */
    private static final BigDecimal VIP_DISCOUNT_RATE = new BigDecimal("0.50");

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final int MONEY_SCALE = 2;

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public WithdrawResponse withdraw(WithdrawRequest request) throws InsufficientBalanceException {
        validate(request);

        Account account = accountRepository.findByUserId(request.getUserId());
        if (account == null) {
            throw new IllegalArgumentException("Account not found for user: " + request.getUserId());
        }

        BigDecimal fee = calculateFee(request.getAmount(), account.isVip());
        BigDecimal totalDebit = request.getAmount().add(fee);

        if (account.getBalance().compareTo(totalDebit) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance for user " + request.getUserId()
                            + ": required " + totalDebit + " (principal + fee)"
                            + " but available " + account.getBalance());
        }

        BigDecimal newBalance = account.getBalance().subtract(totalDebit);
        account.setBalance(newBalance);
        accountRepository.save(account);

        Transaction saved = transactionRepository.save(Transaction.builder()
                .userId(request.getUserId())
                .amount(request.getAmount())
                .fee(fee)
                .timestamp(LocalDateTime.now())
                .type(TransactionType.WITHDRAWAL)
                .build());

        return WithdrawResponse.builder()
                .transactionId(saved.getId())
                .feeCharged(fee)
                .newBalance(newBalance)
                .timestamp(saved.getTimestamp())
                .build();
    }

    private void validate(WithdrawRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Withdraw request must not be null");
        }
        if (request.getUserId() == null || request.getUserId().isBlank()) {
            throw new IllegalArgumentException("userId must not be null or blank");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
    }

    private BigDecimal calculateFee(BigDecimal amount, boolean vip) {
        BigDecimal fee = amount.multiply(STANDARD_FEE_RATE);
        if (vip) {
            fee = fee.multiply(VIP_DISCOUNT_RATE);
        }
        return fee.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
