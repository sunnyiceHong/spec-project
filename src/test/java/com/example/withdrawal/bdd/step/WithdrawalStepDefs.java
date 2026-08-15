package com.example.withdrawal.bdd.step;

import com.example.withdrawal.api.InsufficientBalanceException;
import com.example.withdrawal.api.WithdrawRequest;
import com.example.withdrawal.api.WithdrawResponse;
import com.example.withdrawal.api.WithdrawalService;
import com.example.withdrawal.domain.Account;
import com.example.withdrawal.domain.AccountRepository;
import com.example.withdrawal.domain.TransactionRepository;
import com.example.withdrawal.repository.InMemoryAccountRepository;
import com.example.withdrawal.repository.InMemoryTransactionRepository;
import com.example.withdrawal.service.WithdrawalServiceImpl;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Cucumber step definitions bound to {@code withdrawal.feature}.
 *
 * <p>These steps use the <em>real</em> {@link WithdrawalServiceImpl} backed by
 * the in-memory repositories, so they exercise the full service behaviour end
 * to end (BDD), rather than mocking anything.</p>
 */
public class WithdrawalStepDefs {

    private AccountRepository accountRepository;
    private TransactionRepository transactionRepository;
    private WithdrawalService withdrawalService;

    private String currentUserId;
    private WithdrawResponse lastResponse;
    private InsufficientBalanceException lastException;

    @Before
    public void setUp() {
        accountRepository = new InMemoryAccountRepository();
        transactionRepository = new InMemoryTransactionRepository();
        withdrawalService = new WithdrawalServiceImpl(accountRepository, transactionRepository);
        currentUserId = null;
        lastResponse = null;
        lastException = null;
    }

    @Given("a {word} user {string} with balance {bigdecimal}")
    public void aUserWithBalance(String userType, String userId, BigDecimal balance) {
        boolean vip = "VIP".equalsIgnoreCase(userType);
        currentUserId = userId;
        accountRepository.save(
                Account.builder().userId(userId).balance(balance).isVip(vip).build());
    }

    @When("the user withdraws {bigdecimal}")
    public void theUserWithdraws(BigDecimal amount) {
        WithdrawRequest request = WithdrawRequest.builder()
                .userId(currentUserId)
                .amount(amount)
                .build();
        try {
            lastResponse = withdrawalService.withdraw(request);
        } catch (InsufficientBalanceException ex) {
            lastException = ex;
        }
    }

    @Then("the transaction succeeds with fee {bigdecimal}")
    public void theTransactionSucceedsWithFee(BigDecimal expectedFee) {
        assertThat(lastException)
                .as("expected no InsufficientBalanceException but got: %s", lastException)
                .isNull();
        assertThat(lastResponse).isNotNull();
        assertThat(lastResponse.getFeeCharged()).isEqualByComparingTo(expectedFee);
    }

    @And("the new balance is {bigdecimal}")
    public void theNewBalanceIs(BigDecimal expectedBalance) {
        assertThat(lastResponse).isNotNull();
        assertThat(lastResponse.getNewBalance()).isEqualByComparingTo(expectedBalance);
    }

    @Then("the withdrawal is rejected with insufficient balance")
    public void theWithdrawalIsRejectedWithInsufficientBalance() {
        assertThat(lastException)
                .as("expected an InsufficientBalanceException")
                .isNotNull();
    }

    @And("a transaction record is created")
    public void aTransactionRecordIsCreated() {
        assertThat(lastResponse).isNotNull();
        assertThat(lastResponse.getTransactionId()).isNotBlank();
    }
}
