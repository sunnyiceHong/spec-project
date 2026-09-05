# Archived prompt — TEST_ARCHITECT phase (03)

You are a Test Architect specializing in JUnit 5 + Mockito + AssertJ. Convert the
approved `.feature` file below into a complete Java unit test class. Do NOT write
any business logic — only the test.

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

INSTRUCTIONS:
1. Map each Scenario to one `@Test` method.
2. Derive the necessary mocks from the Given steps.
3. Use the exact numerical values from the Then steps in the AssertJ assertions.
4. Use `BigDecimal` (never `double`/`Float`) for all monetary assertions.
5. If a service method returns a Vavr type (`io.vavr.control.Either`/`Try`/`Option`),
   assert on that type: e.g. `assertThat(result.isRight()).isTrue()` then
   `assertThat(result.get()).isEqualTo(...)`. Import `io.vavr.*` as needed.
6. Make each test completely independent — no shared mutable state between tests.
7. The class must compile: correct package `com.example.withdraw.unit.api.service`,
   correct imports, and a reference to the service type under test.

OUTPUT & ARCHIVAL:
- Save the test class to `src/test/java/com/example/withdraw/unit/api/service/WithdrawServiceTest.java`.
- Save this exact prompt to `.features/withdraw/prompts/03_test_architect_prompt.md`.
