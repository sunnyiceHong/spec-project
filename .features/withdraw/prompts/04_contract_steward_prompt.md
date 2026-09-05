# Archived prompt — CONTRACT_STEWARD phase (04)

You are a Contract Steward / API Designer. Design the API contract for the feature
described below, plus a compilable stub implementation.

APPROVED FEATURE FILE:
Feature: Withdraw funds to a bank card

  As a customer
  I want to withdraw an amount to my bank card
  So that I can move money out of my account

  Background:
    Given the account store has user "user-1" with balance 100.00
    And user "user-1" owns card "1234567812345678"

  Scenario: Successful withdrawal debits the account and returns an approved transaction
    Given user "user-1" exists with balance 100.00
    When I withdraw 30.00 to card "1234567812345678" for user "user-1"
    Then the withdrawal is approved
    And the response amount is 30.00
    And the response card number is "1234567812345678"
    And the response has a non-blank transaction id
    And the account "user-1" balance is debited to 70.00

  Scenario: Withdrawal with an unknown user is rejected
    Given no user with id "unknown-user" exists
    When I withdraw 30.00 to card "1234567812345678" for user "unknown-user"
    Then the withdrawal is rejected with "INVALID_USER"
    And no account is debited

  Scenario: Withdrawal with a blank user id is rejected
    When I withdraw 30.00 to card "1234567812345678" for user "   "
    Then the withdrawal is rejected with "INVALID_USER"
    And no account is debited

  Scenario: Withdrawal with a null user id is rejected
    When I withdraw 30.00 to card "1234567812345678" for user null
    Then the withdrawal is rejected with "INVALID_USER"
    And no account is debited

  Scenario: Withdrawal with a zero amount is rejected
    Given user "user-1" exists with balance 100.00
    When I withdraw 0.00 to card "1234567812345678" for user "user-1"
    Then the withdrawal is rejected with "INVALID_AMOUNT"
    And no account is debited

  Scenario: Withdrawal with a negative amount is rejected
    Given user "user-1" exists with balance 100.00
    When I withdraw -5.00 to card "1234567812345678" for user "user-1"
    Then the withdrawal is rejected with "INVALID_AMOUNT"
    And no account is debited

  Scenario: Withdrawal with a null amount is rejected
    When I withdraw null to card "1234567812345678" for user "user-1"
    Then the withdrawal is rejected with "INVALID_AMOUNT"
    And no account is debited

  Scenario: Withdrawal with a too-short card number is rejected
    Given user "user-1" exists with balance 100.00
    When I withdraw 30.00 to card "1234" for user "user-1"
    Then the withdrawal is rejected with "INVALID_CARD_NUMBER"
    And no account is debited

  Scenario: Withdrawal with a non-numeric card number is rejected
    Given user "user-1" exists with balance 100.00
    When I withdraw 30.00 to card "123456781234567a" for user "user-1"
    Then the withdrawal is rejected with "INVALID_CARD_NUMBER"
    And no account is debited

  Scenario: Withdrawal with a 15-digit card number is rejected
    Given user "user-1" exists with balance 100.00
    When I withdraw 30.00 to card "123456781234567" for user "user-1"
    Then the withdrawal is rejected with "INVALID_CARD_NUMBER"
    And no account is debited

  Scenario: Withdrawal with a null card number is rejected
    Given user "user-1" exists with balance 100.00
    When I withdraw 30.00 to card null for user "user-1"
    Then the withdrawal is rejected with "INVALID_CARD_NUMBER"
    And no account is debited

  Scenario: Withdrawal to a card not owned by the user is rejected
    Given user "user-1" exists with balance 100.00
    When I withdraw 30.00 to card "9999999999999999" for user "user-1"
    Then the withdrawal is rejected with "CARD_NOT_OWNED"
    And no account is debited

  Scenario: Withdrawal to a card owned by a different user is rejected
    Given user "user-1" exists with balance 100.00
    And user "user-2" owns card "9999999999999999"
    When I withdraw 30.00 to card "9999999999999999" for user "user-1"
    Then the withdrawal is rejected with "CARD_NOT_OWNED"
    And no account is debited

  Scenario: Withdrawal exceeding the balance is rejected as insufficient funds
    Given user "user-1" exists with balance 100.00
    When I withdraw 100.01 to card "1234567812345678" for user "user-1"
    Then the withdrawal is rejected with "INSUFFICIENT_FUNDS"
    And no account is debited

  Scenario: Withdrawal exactly equal to the balance succeeds and zeroes the balance
    Given user "user-1" exists with balance 100.00
    When I withdraw 100.00 to card "1234567812345678" for user "user-1"
    Then the withdrawal is approved
    And the account "user-1" balance is debited to 0.00

APPROVED TEST CLASS:
package com.example.withdraw.unit.api.service;

import com.example.withdraw.api.Account;
import com.example.withdraw.api.AccountRepository;
import com.example.withdraw.api.TransactionStatus;
import com.example.withdraw.api.WithdrawError;
import com.example.withdraw.api.WithdrawRequest;
import com.example.withdraw.api.WithdrawResponse;
import com.example.withdraw.application.impl.WithdrawServiceImpl;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WithdrawServiceTest {

    private static final String USER_ID = "user-1";
    private static final String VALID_CARD = "1234567812345678";

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private WithdrawServiceImpl withdrawService;

    @Test
    void successfulWithdrawalDebitsAccountAndReturnsApprovedTransaction() {
        when(accountRepository.findById(USER_ID))
                .thenReturn(Option.some(new Account(USER_ID, new BigDecimal("100.00"), VALID_CARD)));

        Either<WithdrawError, WithdrawResponse> result = withdrawService.withdraw(
                new WithdrawRequest(USER_ID, new BigDecimal("30.00"), VALID_CARD));

        assertThat(result.isRight()).isTrue();
        WithdrawResponse response = result.get();
        assertThat(response.status()).isEqualTo(TransactionStatus.APPROVED);
        assertThat(response.amount()).isEqualByComparingTo("30.00");
        assertThat(response.cardNumber()).isEqualTo(VALID_CARD);
        assertThat(response.transactionId()).isNotBlank();

        verify(accountRepository).save(new Account(USER_ID, new BigDecimal("70.00"), VALID_CARD));
    }

    @Test
    void withdrawalWithUnknownUserIsRejected() {
        when(accountRepository.findById("unknown-user")).thenReturn(Option.none());

        Either<WithdrawError, WithdrawResponse> result = withdrawService.withdraw(
                new WithdrawRequest("unknown-user", new BigDecimal("30.00"), VALID_CARD));

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo(WithdrawError.INVALID_USER);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void withdrawalWithBlankUserIdIsRejected() {
        Either<WithdrawError, WithdrawResponse> result = withdrawService.withdraw(
                new WithdrawRequest("   ", new BigDecimal("30.00"), VALID_CARD));

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo(WithdrawError.INVALID_USER);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void withdrawalWithNullUserIdIsRejected() {
        Either<WithdrawError, WithdrawResponse> result = withdrawService.withdraw(
                new WithdrawRequest(null, new BigDecimal("30.00"), VALID_CARD));

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo(WithdrawError.INVALID_USER);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void withdrawalWithZeroAmountIsRejected() {
        Either<WithdrawError, WithdrawResponse> result = withdrawService.withdraw(
                new WithdrawRequest(USER_ID, BigDecimal.ZERO, VALID_CARD));

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo(WithdrawError.INVALID_AMOUNT);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void withdrawalWithNegativeAmountIsRejected() {
        Either<WithdrawError, WithdrawResponse> result = withdrawService.withdraw(
                new WithdrawRequest(USER_ID, new BigDecimal("-5.00"), VALID_CARD));

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo(WithdrawError.INVALID_AMOUNT);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void withdrawalWithNullAmountIsRejected() {
        Either<WithdrawError, WithdrawResponse> result = withdrawService.withdraw(
                new WithdrawRequest(USER_ID, null, VALID_CARD));

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo(WithdrawError.INVALID_AMOUNT);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void withdrawalWithTooShortCardNumberIsRejected() {
        Either<WithdrawError, WithdrawResponse> result = withdrawService.withdraw(
                new WithdrawRequest(USER_ID, new BigDecimal("30.00"), "1234"));

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo(WithdrawError.INVALID_CARD_NUMBER);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void withdrawalWithNonNumericCardNumberIsRejected() {
        Either<WithdrawError, WithdrawResponse> result = withdrawService.withdraw(
                new WithdrawRequest(USER_ID, new BigDecimal("30.00"), "123456781234567a"));

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo(WithdrawError.INVALID_CARD_NUMBER);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void withdrawalWith15DigitCardNumberIsRejected() {
        Either<WithdrawError, WithdrawResponse> result = withdrawService.withdraw(
                new WithdrawRequest(USER_ID, new BigDecimal("30.00"), "123456781234567"));

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo(WithdrawError.INVALID_CARD_NUMBER);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void withdrawalWithNullCardNumberIsRejected() {
        Either<WithdrawError, WithdrawResponse> result = withdrawService.withdraw(
                new WithdrawRequest(USER_ID, new BigDecimal("30.00"), null));

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo(WithdrawError.INVALID_CARD_NUMBER);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void withdrawalToCardNotOwnedByUserIsRejected() {
        when(accountRepository.findById(USER_ID))
                .thenReturn(Option.some(new Account(USER_ID, new BigDecimal("100.00"), VALID_CARD)));

        Either<WithdrawError, WithdrawResponse> result = withdrawService.withdraw(
                new WithdrawRequest(USER_ID, new BigDecimal("30.00"), "9999999999999999"));

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo(WithdrawError.CARD_NOT_OWNED);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void withdrawalExceedingBalanceIsRejectedAsInsufficientFunds() {
        when(accountRepository.findById(USER_ID))
                .thenReturn(Option.some(new Account(USER_ID, new BigDecimal("100.00"), VALID_CARD)));

        Either<WithdrawError, WithdrawResponse> result = withdrawService.withdraw(
                new WithdrawRequest(USER_ID, new BigDecimal("100.01"), VALID_CARD));

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo(WithdrawError.INSUFFICIENT_FUNDS);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void withdrawalExactlyEqualToBalanceSucceedsAndZeroesBalance() {
        when(accountRepository.findById(USER_ID))
                .thenReturn(Option.some(new Account(USER_ID, new BigDecimal("100.00"), VALID_CARD)));

        Either<WithdrawError, WithdrawResponse> result = withdrawService.withdraw(
                new WithdrawRequest(USER_ID, new BigDecimal("100.00"), VALID_CARD));

        assertThat(result.isRight()).isTrue();
        WithdrawResponse response = result.get();
        assertThat(response.status()).isEqualTo(TransactionStatus.APPROVED);
        assertThat(response.amount()).isEqualByComparingTo("100.00");

        verify(accountRepository).save(new Account(USER_ID, BigDecimal.ZERO, VALID_CARD));
    }
}

INSTRUCTIONS:
1. Extract nouns as DTOs and verbs as method signatures.
2. Add Jakarta Validation annotations to DTO fields based on the feature's business
   rules. Use `BigDecimal` (never `double`/`Float`) for all monetary fields.
3. Use Vavr 0.11.0 (`io.vavr`) for the API surface: methods that can fail return
   `io.vavr.control.Either<..., T>` or `io.vavr.control.Try<T>`; optional values return
   `io.vavr.control.Option<T>` (never `java.util.Optional`); never throw checked
   exceptions from the interface.
4. Create the service interface and DTOs in package `com.example.withdraw.api`.
5. Create a stub `WithdrawServiceImpl` in package
   `com.example.withdraw.application.impl` that implements the interface and throws
   `UnsupportedOperationException` from every method (so the test compiles).
6. Ensure everything compiles together with the test class.

OUTPUT & ARCHIVAL:
- Save interface + DTOs to `src/main/java/com/example/withdraw/api/`.
- Save the stub to `src/main/java/com/example/withdraw/application/impl/WithdrawServiceImpl.java`.
- Save this exact prompt to `.features/withdraw/prompts/04_contract_steward_prompt.md`.
