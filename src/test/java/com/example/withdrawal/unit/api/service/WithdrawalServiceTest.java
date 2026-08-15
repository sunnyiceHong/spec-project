package com.example.withdrawal.unit.api.service;

import com.example.withdrawal.api.InsufficientBalanceException;
import com.example.withdrawal.api.WithdrawRequest;
import com.example.withdrawal.api.WithdrawResponse;
import com.example.withdrawal.domain.Account;
import com.example.withdrawal.domain.AccountRepository;
import com.example.withdrawal.domain.Transaction;
import com.example.withdrawal.domain.TransactionRepository;
import com.example.withdrawal.domain.TransactionType;
import com.example.withdrawal.service.WithdrawalServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD unit tests for the withdrawal service.
 *
 * <p>These tests are written against the frozen contract (Spec-Contract-First)
 * and drive the "Driver Agent" implementation. They start RED when the service
 * implementation is an empty stub.</p>
 */
@ExtendWith(MockitoExtension.class)
class WithdrawalServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private WithdrawalServiceImpl withdrawalService;

    @Test
    void vipUserGets50PercentFeeDiscount() {
        when(accountRepository.findByUserId("u-vip")).thenReturn(account("u-vip", "1000.00", true));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId("txn-1");
            return t;
        });

        WithdrawResponse response = withdrawalService.withdraw(request("u-vip", "100.00"));

        assertThat(response.getFeeCharged()).isEqualByComparingTo("0.50");
        assertThat(response.getNewBalance()).isEqualByComparingTo("899.50");
        assertThat(response.getTransactionId()).isEqualTo("txn-1");
        assertThat(response.getTimestamp()).isNotNull();

        Transaction captured = captureTransaction();
        assertThat(captured.getAmount()).isEqualByComparingTo("100.00");
        assertThat(captured.getFee()).isEqualByComparingTo("0.50");
        assertThat(captured.getUserId()).isEqualTo("u-vip");
        assertThat(captured.getType()).isEqualTo(TransactionType.WITHDRAWAL);
    }

    @Test
    void regularUserPaysFullFee() {
        when(accountRepository.findByUserId("u-reg")).thenReturn(account("u-reg", "1000.00", false));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId("txn-2");
            return t;
        });

        WithdrawResponse response = withdrawalService.withdraw(request("u-reg", "200.00"));

        assertThat(response.getFeeCharged()).isEqualByComparingTo("2.00");
        assertThat(response.getNewBalance()).isEqualByComparingTo("798.00");
    }

    @Test
    void insufficientBalanceRejectedIncludingFee() {
        when(accountRepository.findByUserId("u-poor")).thenReturn(account("u-poor", "50.00", false));

        assertThatThrownBy(() -> withdrawalService.withdraw(request("u-poor", "50.00")))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("Insufficient balance");

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void withdrawalWithExactBalanceSucceeds() {
        when(accountRepository.findByUserId("u-exact")).thenReturn(account("u-exact", "100.50", true));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId("txn-4");
            return t;
        });

        WithdrawResponse response = withdrawalService.withdraw(request("u-exact", "100.00"));

        assertThat(response.getFeeCharged()).isEqualByComparingTo("0.50");
        assertThat(response.getNewBalance()).isEqualByComparingTo("0.00");
    }

    @Test
    void nullRequestIsRejected() {
        assertThatThrownBy(() -> withdrawalService.withdraw(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("request");
    }

    private WithdrawRequest request(String userId, String amount) {
        return WithdrawRequest.builder().userId(userId).amount(new BigDecimal(amount)).build();
    }

    private Account account(String userId, String balance, boolean vip) {
        return Account.builder().userId(userId).balance(new BigDecimal(balance)).isVip(vip).build();
    }

    private Transaction captureTransaction() {
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        return captor.getValue();
    }
}
