You are a Developer. The implementation failed `mvn test`. Fix it using the error log.

MAVEN ERROR LOG:
[ERROR] Tests run: 13, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 0.915 s <<< FAILURE! -- in com.example.withdraw.unit.api.service.WithdrawServiceTest
[ERROR] com.example.withdraw.unit.api.service.WithdrawServiceTest.withdrawalExactlyEqualToBalanceSucceedsAndZeroesBalance -- Time elapsed: 0.021 s <<< FAILURE!
Argument(s) are different! Wanted:
accountRepository.save(
    Account[userId=user-1, balance=0]
);
-> at com.example.withdraw.unit.api.service.WithdrawServiceTest.withdrawalExactlyEqualToBalanceSucceedsAndZeroesBalance(WithdrawServiceTest.java:184)
Actual invocations have different arguments:
accountRepository.findById(
    "user-1"
);
-> at com.example.withdraw.application.impl.WithdrawServiceImpl.withdraw(WithdrawServiceImpl.java:55)
accountRepository.save(
    Account[userId=user-1, balance=0.00]
);
-> at com.example.withdraw.application.impl.WithdrawServiceImpl.withdraw(WithdrawServiceImpl.java:66)
[ERROR] Failures:
[ERROR]   WithdrawServiceTest.withdrawalExactlyEqualToBalanceSucceedsAndZeroesBalance:184
[ERROR] Tests run: 13, Failures: 1, Errors: 0, Skipped: 0
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-surefire-plugin:3.5.2:test (default-test) on project spec-project: There are test failures.

CURRENT IMPLEMENTATION:
package com.example.withdraw.application.impl;

import com.example.withdraw.api.Account;
import com.example.withdraw.api.AccountRepository;
import com.example.withdraw.api.TransactionStatus;
import com.example.withdraw.api.WithdrawError;
import com.example.withdraw.api.WithdrawRequest;
import com.example.withdraw.api.WithdrawResponse;
import com.example.withdraw.api.WithdrawService;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

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
        BigDecimal balance = account.balance();
        if (balance == null || amount.compareTo(balance) > 0) {
            return Either.left(WithdrawError.INSUFFICIENT_FUNDS);
        }

        accountRepository.save(new Account(userId, balance.subtract(amount)));

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

INSTRUCTIONS:
1. Diagnose the failure(s) from the log.
2. Produce a fixed version of the implementation. Do NOT modify `api/` or `src/test/`.
   Keep Vavr 0.11.0 idioms (`Try`/`Either`/`Option`, immutable `io.vavr.collection.*`
   collections).
3. Explain, in 1-3 bullets, what you changed and why.

OUTPUT & ARCHIVAL:
- Overwrite `src/main/java/com/example/withdraw/application/impl/WithdrawServiceImpl.java` with the fixed version.
- Save this exact prompt (including the log) to `.features/withdraw/prompts/05_developer_fix_v1_prompt.md`.
