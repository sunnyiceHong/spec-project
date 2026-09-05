You are a senior Tech Lead reviewing a PR for the feature below. Produce a PR Review
Report.

ARTIFACTS (paste ALL of these):
1. requirement:  .features/withdraw/final/requirement.md
2. feature:      .features/withdraw/final/withdraw.feature
3. step defs:    src/test/java/com/example/withdraw/bdd/WithdrawStepDefinitions.java
4. runner:       src/test/java/com/example/withdraw/bdd/WithdrawCucumberRunner.java
5. controller:   src/main/java/com/example/withdraw/controller/WithdrawController.java
6. contracts:    src/main/java/com/example/withdraw/{dto,entity,repository,service}/
7. implementation: src/main/java/com/example/withdraw/service/impl/WithdrawServiceImpl.java

ARTIFACT CONTENTS:

--- requirement.md ---
# Requirement Document — Withdraw

## Feature Name

Withdraw (card payout)

## Business Goal

Allow a user to withdraw funds to a bank card via an API call. The API must validate
the request (user, amount, card) and reject invalid or unaffordable withdrawals, so no
money moves unless the request is fully valid and covered by the account balance.

## User Stories

- As a customer, I want to withdraw an amount to my card so that I can move money out
  of my account.
- As a customer, I want to be told exactly why my withdrawal failed so that I can fix
  my request.
- As the platform, I want to reject withdrawals that exceed the balance so that an
  account can never be debited into a negative balance.
- As the platform, I want to reject withdrawals to cards not owned by the user so that
  money can never be sent to someone else's card.

## Acceptance Criteria

1. Given an existing `user_id`, an `amount` greater than zero and not exceeding the
   account balance, and a 16-digit `card_number` owned by the user, the API returns a
   transaction record containing a unique `transaction_id`, `status = APPROVED`, the
   `amount`, and the `card_number`.
2. The account balance is debited by exactly the withdrawn amount on success.
3. If `user_id` is null, blank, or does not exist in the account store, the API rejects
   the request with an "invalid user" error and debits nothing.
4. If `amount` is null or less than or equal to zero, the API rejects with an "invalid
   amount" error and debits nothing.
5. If `card_number` is null, blank, or not a 16-digit numeric string, the API rejects
   with an "invalid card number" error and debits nothing.
6. If `card_number` is well-formed but does not belong to the `user_id`, the API rejects
   with a "card not owned" error and debits nothing.
7. If `amount` exceeds the account balance, the API rejects with an "insufficient
   funds" error and debits nothing.
8. Validation happens before any debit — a failed request has no side effect.

## Edge Cases

- `amount = 0` → rejected (not positive).
- `amount` exactly equal to the balance → succeeds; resulting balance is `0`.
- `amount = balance + 0.01` → rejected (insufficient funds).
- `card_number` with 15 or 17 digits → rejected.
- `card_number` containing non-digit characters → rejected.
- `user_id` empty string or whitespace-only → rejected.
- `card_number` null or empty → rejected.
- Well-formed `card_number` not owned by the user → rejected (card not owned).

## Non-functional Requirements

- Monetary values use `BigDecimal`; never `double`/`float`.
- Request/response DTOs are immutable.
- Error handling is functional (Vavr 0.11.0 `Either`/`Try`/`Option`); the service
  interface does not throw checked exceptions and does not return `null`.
- Each `transaction_id` is unique across withdrawals.
- A failed validation performs no I/O side effects (no debit).

--- withdraw.feature ---
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

--- step defs ---
(see src/test/java/com/example/withdraw/bdd/WithdrawStepDefinitions.java)

--- runner ---
(see src/test/java/com/example/withdraw/bdd/WithdrawCucumberRunner.java)

--- controller ---
(see src/main/java/com/example/withdraw/controller/WithdrawController.java)

--- contracts ---
(see src/main/java/com/example/withdraw/{dto,entity,repository,service}/: WithdrawService,
WithdrawRequest, WithdrawResponse, WithdrawErrorResponse, Account, AccountRepository,
InMemoryAccountRepository, WithdrawError, TransactionStatus)

--- implementation ---
(see src/main/java/com/example/withdraw/service/impl/WithdrawServiceImpl.java)

INSTRUCTIONS:
1. Assess architecture: layers, dependencies, coupling.
2. Evaluate design-pattern usage (appropriate? over-engineered?).
3. Identify risks in production (edge cases, null-safety, concurrency).
4. Check performance (inefficient algorithms, redundant work).
5. Verify Vavr 0.11.0 usage is idiomatic and consistent with the contract: `Option`
   for null-safety, `Try`/`Either` for failure paths, immutable `io.vavr.collection.*`
   collections, and no mixing with `java.util.Optional` where the contract uses Vavr.
6. Verify the implementation follows the contract exactly.
7. End with a final recommendation: RECOMMEND_MERGE or RECOMMEND_REJECT, with
   detailed justification. If rejecting, give actionable, specific feedback.

OUTPUT & ARCHIVAL:
- Save the report to `.features/withdraw/final/techlead_report.md`.
- Save this exact prompt to `.features/withdraw/prompts/06_techlead_prompt.md`.
