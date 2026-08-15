# Withdrawal Service — Spec-Contract-First TDD/BDD Pair-Agent Demo

A complete **Spring Boot 3.x** project that demonstrates the
**Spec-Contract-First → TDD → BDD → Pair-Agent** workflow using a simple user
withdrawal service as the demo feature.

## Business Rules (the Spec)

1. VIP users get a **50% discount** on withdrawal fees (standard fee = **1%**).
2. Regular users pay the **full 1%** fee.
3. A withdrawal **fails** with `InsufficientBalanceException` when the balance
   cannot cover **principal + fee**.
4. Every successful withdrawal generates a **transaction record**.

## Tech Stack

| Concern           | Choice                              |
| ----------------- | ----------------------------------- |
| Language          | Java 21 (source is Java 17-compatible) |
| Framework         | Spring Boot 3.4.x                   |
| Build             | Maven                               |
| Unit tests        | JUnit 5 + Mockito + AssertJ         |
| BDD               | Cucumber 7.x                        |
| Validation        | Jakarta Bean Validation             |
| Boilerplate       | Lombok                              |
| Functional types  | Vavr 0.10.4                         |

> **Note on Vavr:** `design.md` lists "Vavr 1.11.0", but that version does not
> exist on Maven Central. The latest stable release is **0.10.4**, which is used
> here. Vavr is available for AI-native patterns (`Option`/`Either`/`Try`) but
> is intentionally not required by the thin service implementation.

## Project Structure

```
src/main/java/com/example/withdrawal/
├── WithdrawalApplication.java          # @SpringBootApplication entry point
├── api/                                # STEP 2: the frozen CONTRACT
│   ├── WithdrawRequest.java            #   userId, amount (+ validation)
│   ├── WithdrawResponse.java           #   transactionId, feeCharged, newBalance, timestamp
│   ├── WithdrawalService.java          #   withdraw(...) interface
│   └── InsufficientBalanceException.java
├── domain/                             # STEP 3: domain + repository contracts
│   ├── Account.java                    #   userId, balance, isVip
│   ├── Transaction.java                #   id, userId, amount, fee, timestamp, type
│   ├── TransactionType.java
│   ├── AccountRepository.java
│   └── TransactionRepository.java
├── repository/                         # in-memory (ConcurrentHashMap + AtomicLong)
│   ├── InMemoryAccountRepository.java
│   └── InMemoryTransactionRepository.java
└── service/
    └── WithdrawalServiceImpl.java      # STEP 7: the "Driver Agent" implementation

src/test/java/com/example/withdrawal/
├── unit/api/service/WithdrawalServiceTest.java   # STEP 4: TDD unit tests
└── bdd/
    ├── CucumberTestRunner.java                   # STEP 6: runner
    └── step/WithdrawalStepDefs.java              # STEP 5: BDD glue code

src/test/resources/features/
└── withdrawal.feature                  # STEP 1: the SPEC (Gherkin scenarios)
```

## The Workflow (Spec-Contract-First TDD/BDD)

1. **Spec** — write the Gherkin feature first (`withdrawal.feature`).
2. **Contract** — freeze the API surface in `api/` (DTOs + service interface).
3. **Domain & Repository** — define entities and persistence contracts.
4. **TDD (RED)** — write `WithdrawalServiceTest` against the contract with the
   service implementation left empty; the suite **fails**.
5. **BDD glue** — bind every Given/When/Then step to the real service.
6. **Runner & config** — wire Cucumber into Maven.
7. **Pair Agent (GREEN)** — generate `WithdrawalServiceImpl` until every test
   passes, *without modifying anything in `api/` or `test/`*.

## How to Build & Run

```bash
# Compile only
mvn clean compile

# Run unit tests (JUnit 5 + Mockito) AND BDD scenarios (Cucumber)
mvn test

# Run only the BDD scenarios filtered by tag
mvn test -Dcucumber.filter.tags=@Withdrawal

# Full verification (compile + unit + BDD)
mvn verify
```

Cucumber writes a human-readable report to `target/cucumber-report.html` and a
machine-readable one to `target/cucumber.json`.

## Quality Gates

- ✅ All JUnit tests pass (`mvn test`)
- ✅ All Cucumber scenarios pass (`mvn verify`)
- ✅ Money is always `BigDecimal` — no `double`/`Float`
- ✅ The `api/` and `test/` packages are untouched by the implementation step
- ✅ Null-safe input handling (`null` request, blank `userId`, non-positive amount)
- ✅ Compiles with Java 17+ (no post-17 language features)

## Notes

- No external database is required — repositories are in-memory
  (`ConcurrentHashMap`).
- The demo intentionally stops at the service layer; add a `@RestController`
  with `@Valid` to expose it over HTTP if you need endpoints.
