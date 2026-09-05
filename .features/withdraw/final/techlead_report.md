# Tech Lead Review — Withdraw (card payout)

**Recommendation:** `RECOMMEND_MERGE`

Reviewed: 2026-09-05 · Build: `mvn test` → **34 tests green**
(15 Cucumber BDD scenarios + 14 service unit tests + 5 controller tests).

## Architecture assessment

- Clean Spring-style layering under `com.example.withdraw`:
  `controller/` (thin REST adapter) · `dto/` (immutable request/response records) ·
  `entity/` (domain records + enums) · `repository/` (port interface + in-memory impl) ·
  `service/` (interface) + `service/impl/` (implementation). No flat-package dumping.
- Correct dependency direction: `WithdrawServiceImpl` depends on the `AccountRepository`
  *port*, not a concrete store; `WithdrawController` depends on the `WithdrawService`
  *interface*. Both are constructor-injected, so both are unit-testable without Spring.
- The controller is a true adapter — it delegates to the service and only maps the
  `Either` result to HTTP. No business logic leaked into the web layer.

## Design-pattern evaluation

- **Ports & adapters** for persistence — appropriate and matches the frozen contract.
- **`Either<WithdrawError, WithdrawResponse>`** as the return type is the right call:
  the failure domain is closed and enumerated, and no checked exceptions or `null`
  leak from the interface.
- Immutable records (`Account`, `WithdrawRequest`, `WithdrawResponse`,
  `WithdrawErrorResponse`) — correct for a value-oriented money domain.
- Vavr 0.11.0 used idiomatically: `Either` for failure, `Option` for the optional
  `findById`, no `java.util.Optional` leakage.

## Risk assessment

1. **Check-then-act race (double debit).** `findById` → validate balance → `save` is not
   atomic. Two concurrent withdrawals against the same account can both read the same
   balance and both debit. The in-memory `ConcurrentHashMap` makes individual `get`/`put`
   thread-safe but does not make the read-modify-write sequence atomic. Acceptable for
   the in-memory demo; a production store needs a CAS/row-lock (e.g.
   `UPDATE ... WHERE balance >= ?` or an optimistic version field).
2. **Bean-validation vs. service-error divergence.** The controller's `@Valid` rejects
   null/blank/negative/format-invalid input with Spring's default 400 body *before* the
   service runs, so those cases return Spring's error structure rather than
   `{"error": "INVALID_AMOUNT"}` — and the granular status mapping in `toHttpStatus`
   only applies to business-rule failures (unknown user, card-not-owned, insufficient
   funds). Functionally correct (still rejected, still no debit), but the HTTP error
   contract is not uniform. Consider a `@ControllerAdvice` to map bean-validation
   failures to `WithdrawErrorResponse` if a consistent body is required.
3. **`TransactionStatus` has a single value (`APPROVED`).** Consistent with the
   `Either`-based design (rejections are not "approved transactions with a failed
   status"), but the enum will need a `DECLINED`/`FAILED` value if the API ever returns
   a `WithdrawResponse` for non-approved outcomes.

## Performance considerations

- Account lookup is O(1) via `ConcurrentHashMap`; no N+1, no redundant I/O.
- `UUID.randomUUID()` per success is negligible.
- The `newBalance.compareTo(ZERO) == 0` normalization avoids negative-zero/scale
  artifacts and keeps the debited balance at a clean `0.00`.

## Contract conformance

- Validation order matches the acceptance criteria — user → amount → card format →
  account existence → card ownership → balance — with **no debit on any failure**
  (all validation precedes the single `save`). Verified by the 15 BDD scenarios, all
  green.

## Final recommendation

**RECOMMEND_MERGE.** The implementation satisfies every acceptance criterion and all 34
tests pass. The three risks above are non-blocking for a demo/feature slice; address
risk #1 (atomic debit) before any multi-instance production deployment.
