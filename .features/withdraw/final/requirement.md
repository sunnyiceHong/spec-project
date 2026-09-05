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
