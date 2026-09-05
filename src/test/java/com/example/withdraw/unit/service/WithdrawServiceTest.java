package com.example.withdraw.unit.service;

import com.example.withdraw.dto.WithdrawRequest;
import com.example.withdraw.dto.WithdrawResponse;
import com.example.withdraw.entity.Account;
import com.example.withdraw.entity.TransactionStatus;
import com.example.withdraw.entity.WithdrawError;
import com.example.withdraw.repository.AccountRepository;
import com.example.withdraw.service.impl.WithdrawServiceImpl;
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
