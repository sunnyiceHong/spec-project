@Withdrawal
Feature: User Withdrawal Service

  Users can withdraw money from their account. Withdrawal fees differ for VIP
  and regular users, and a withdrawal must be rejected when the balance cannot
  cover the principal plus the fee.

  Scenario: VIP user withdrawal with 50% fee discount
    Given a VIP user "u-vip" with balance 1000.00
    When the user withdraws 100.00
    Then the transaction succeeds with fee 0.50
    And the new balance is 899.50
    And a transaction record is created

  Scenario: Regular user withdrawal with full fee
    Given a regular user "u-reg" with balance 1000.00
    When the user withdraws 200.00
    Then the transaction succeeds with fee 2.00
    And the new balance is 798.00
    And a transaction record is created

  Scenario: Insufficient balance rejection including fee
    Given a regular user "u-poor" with balance 50.00
    When the user withdraws 50.00
    Then the withdrawal is rejected with insufficient balance

  Scenario: Withdrawal with exact balance edge case
    Given a VIP user "u-exact" with balance 100.50
    When the user withdraws 100.00
    Then the transaction succeeds with fee 0.50
    And the new balance is 0.00
    And a transaction record is created
