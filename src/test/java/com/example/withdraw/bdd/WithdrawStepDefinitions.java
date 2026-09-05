package com.example.withdraw.bdd;

import com.example.withdraw.dto.WithdrawRequest;
import com.example.withdraw.dto.WithdrawResponse;
import com.example.withdraw.entity.Account;
import com.example.withdraw.entity.WithdrawError;
import com.example.withdraw.repository.AccountRepository;
import com.example.withdraw.repository.impl.InMemoryAccountRepository;
import com.example.withdraw.service.WithdrawService;
import com.example.withdraw.service.impl.WithdrawServiceImpl;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.vavr.control.Either;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Cucumber step definitions that bind the Gherkin {@code withdraw.feature}
 * scenarios to the real {@link WithdrawService} over an in-memory repository.
 *
 * <p>Each scenario gets a fresh repository + service (see {@link #reset()}), so
 * scenarios are fully isolated. Given steps stage accounts; the When step flushes
 * them into the repository and invokes the service.</p>
 */
public class WithdrawStepDefinitions {

    private final Map<String, Account> accounts = new LinkedHashMap<>();
    private List<Account> seededAccounts = new ArrayList<>();
    private AccountRepository repository;
    private WithdrawService service;
    private Either<WithdrawError, WithdrawResponse> result;

    @Before
    public void reset() {
        accounts.clear();
        seededAccounts = new ArrayList<>();
        repository = new InMemoryAccountRepository();
        service = new WithdrawServiceImpl(repository);
        result = null;
    }

    // --- Given ---

    @Given("the account store has user {string} with balance {bigdecimal}")
    public void accountStoreHasUser(String userId, BigDecimal balance) {
        upsertBalance(userId, balance);
    }

    @Given("user {string} exists with balance {bigdecimal}")
    public void userExistsWithBalance(String userId, BigDecimal balance) {
        upsertBalance(userId, balance);
    }

    @Given("user {string} owns card {string}")
    public void userOwnsCard(String userId, String cardNumber) {
        upsertCard(userId, cardNumber);
    }

    @Given("no user with id {string} exists")
    public void noUserWithId(String userId) {
        accounts.remove(userId);
    }

    // --- When ---

    @When("I withdraw {bigdecimal} to card {string} for user {string}")
    public void withdraw(BigDecimal amount, String cardNumber, String userId) {
        doWithdraw(amount, cardNumber, userId);
    }

    @When("I withdraw null to card {string} for user {string}")
    public void withdrawNullAmount(String cardNumber, String userId) {
        doWithdraw(null, cardNumber, userId);
    }

    @When("I withdraw {bigdecimal} to card null for user {string}")
    public void withdrawNullCard(BigDecimal amount, String userId) {
        doWithdraw(amount, null, userId);
    }

    @When("I withdraw {bigdecimal} to card {string} for user null")
    public void withdrawNullUser(BigDecimal amount, String cardNumber) {
        doWithdraw(amount, cardNumber, null);
    }

    // --- Then ---

    @Then("the withdrawal is approved")
    public void withdrawalApproved() {
        assertThat(result).isNotNull();
        assertThat(result.isRight())
                .as("withdrawal should be approved but was rejected with %s",
                        result.isLeft() ? result.getLeft() : null)
                .isTrue();
    }

    @Then("the withdrawal is rejected with {string}")
    public void withdrawalRejectedWith(String code) {
        assertThat(result).isNotNull();
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo(WithdrawError.valueOf(code));
    }

    @And("the response amount is {bigdecimal}")
    public void responseAmountIs(BigDecimal amount) {
        assertThat(result.get().amount()).isEqualByComparingTo(amount.toPlainString());
    }

    @And("the response card number is {string}")
    public void responseCardNumberIs(String cardNumber) {
        assertThat(result.get().cardNumber()).isEqualTo(cardNumber);
    }

    @And("the response has a non-blank transaction id")
    public void responseHasNonBlankTransactionId() {
        assertThat(result.get().transactionId()).isNotBlank();
    }

    @And("the account {string} balance is debited to {bigdecimal}")
    public void accountBalanceDebitedTo(String userId, BigDecimal balance) {
        Account account = repository.findById(userId).get();
        assertThat(account.balance()).isEqualByComparingTo(balance.toPlainString());
    }

    @And("no account is debited")
    public void noAccountDebited() {
        for (Account seeded : seededAccounts) {
            Account current = repository.findById(seeded.userId()).get();
            assertThat(current.balance())
                    .as("account %s should not be debited", seeded.userId())
                    .isEqualByComparingTo(seeded.balance().toPlainString());
        }
    }

    // --- helpers ---

    private void upsertBalance(String userId, BigDecimal balance) {
        String card = accounts.containsKey(userId) ? accounts.get(userId).cardNumber() : null;
        accounts.put(userId, new Account(userId, balance, card));
    }

    private void upsertCard(String userId, String cardNumber) {
        BigDecimal balance = accounts.containsKey(userId) ? accounts.get(userId).balance() : BigDecimal.ZERO;
        accounts.put(userId, new Account(userId, balance, cardNumber));
    }

    private void doWithdraw(BigDecimal amount, String cardNumber, String userId) {
        accounts.values().forEach(repository::save);
        seededAccounts = new ArrayList<>(accounts.values());
        WithdrawRequest request = new WithdrawRequest(userId, amount, cardNumber);
        result = service.withdraw(request);
    }
}
