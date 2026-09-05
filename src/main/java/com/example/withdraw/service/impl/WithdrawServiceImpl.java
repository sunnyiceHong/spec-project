package com.example.withdraw.service.impl;

import com.example.withdraw.dto.WithdrawRequest;
import com.example.withdraw.dto.WithdrawResponse;
import com.example.withdraw.entity.Account;
import com.example.withdraw.entity.TransactionStatus;
import com.example.withdraw.entity.WithdrawError;
import com.example.withdraw.repository.AccountRepository;
import com.example.withdraw.service.WithdrawService;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Real implementation of the withdrawal service.
 *
 * <p>Validates the request before any account lookup or debit, verifies the card
 * belongs to the user, then debits the account by the withdrawn amount and returns
 * an approved transaction. All failure paths use {@link Either#left(Object)}.</p>
 */
@Service
public class WithdrawServiceImpl implements WithdrawService {

    private static final String CARD_NUMBER_PATTERN = "\\d{16}";

    private final AccountRepository accountRepository;

    public WithdrawServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Either<WithdrawError, WithdrawResponse> withdraw(WithdrawRequest request) {
        if (request == null) {
            return Either.left(WithdrawError.INVALID_USER);
        }

        String userId = request.userId();
        BigDecimal amount = request.amount();
        String cardNumber = request.cardNumber();

        if (isBlank(userId)) {
            return Either.left(WithdrawError.INVALID_USER);
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Either.left(WithdrawError.INVALID_AMOUNT);
        }
        if (isBlank(cardNumber) || !cardNumber.matches(CARD_NUMBER_PATTERN)) {
            return Either.left(WithdrawError.INVALID_CARD_NUMBER);
        }

        Option<Account> accountOption = accountRepository.findById(userId);
        if (accountOption.isEmpty()) {
            return Either.left(WithdrawError.INVALID_USER);
        }

        Account account = accountOption.get();

        if (account.cardNumber() == null || !account.cardNumber().equals(cardNumber)) {
            return Either.left(WithdrawError.CARD_NOT_OWNED);
        }

        BigDecimal balance = account.balance();
        if (balance == null || amount.compareTo(balance) > 0) {
            return Either.left(WithdrawError.INSUFFICIENT_FUNDS);
        }

        BigDecimal newBalance = balance.subtract(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) == 0) {
            newBalance = BigDecimal.ZERO;
        }
        accountRepository.save(new Account(userId, newBalance, cardNumber));

        return Either.right(new WithdrawResponse(
                UUID.randomUUID().toString(),
                TransactionStatus.APPROVED,
                amount,
                cardNumber));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
