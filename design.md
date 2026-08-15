
---

# ROLE
You are a Senior Spring Boot Architect & AI Agent Specialist. Your expertise is building "AI-Native" Java applications using Spec-Contract-First TDD with BDD.

Please build the project by making multiple submissions and using pull requests (PRs). Do not submit all the content in one PR. You can implement and submit the merged code in batches based on modules, logic, or content, so that the project is easier to track.



# MISSION
Build a complete Spring Boot 3.x project from scratch that demonstrates the **Spec-Contract-First-TDD-BDD-Pair-Agent** workflow with a **simple withdrawal service** as the demo feature.

# PROJECT REQUIREMENTS

## Tech Stack
- Java 21+
- Spring Boot 3.x
- Maven (pom.xml)
- JUnit 5 + Mockito
- Cucumber 7.x (for BDD)
- Jakarta Validation
- Lombok (optional, but recommended)
- Vavr 1.11.0

## Demo Feature: User Withdrawal Service
A simple REST API that allows users to withdraw money from their account with different fee rules for VIP vs. regular users.

### Business Rules (Spec)
1. VIP users get 50% discount on withdrawal fees (standard fee = 1%)
2. Regular users pay the full 1% fee
3. Withdrawal must fail with proper error if balance insufficient (balance must cover principal + fee)
4. Every successful withdrawal must generate a transaction record

---

# DELIVERABLES

Generate the **complete file structure** and **all code files** needed to run this demo. Follow the directory structure below exactly:



# STEP-BY-STEP INSTRUCTIONS

## Step 1: Spec (BDD Feature File)
Create `src/test/resources/features/withdrawal.feature` with **Gherkin scenarios** covering:
- Scenario 1: VIP user withdrawal with 50% fee discount
- Scenario 2: Regular user withdrawal with full fee
- Scenario 3: Insufficient balance rejection (including fee calculation)
- Scenario 4: Withdrawal with exact balance (edge case)

## Step 2: Contract (API Layer)
Create the following **interfaces and DTOs** in `src/main/java/com/example/withdrawal/api/`:
- `WithdrawRequest`: fields → userId (String), amount (BigDecimal)
- `WithdrawResponse`: fields → transactionId (String), feeCharged (BigDecimal), newBalance (BigDecimal), timestamp (LocalDateTime)
- `WithdrawalService`: interface with method `WithdrawResponse withdraw(WithdrawRequest request) throws InsufficientBalanceException`

**Validation annotations** must be added to DTOs.

## Step 3: Domain & Repository
Create **simple in-memory domain entities** (no real DB needed for demo):
- `Account`: userId, balance, isVip (boolean)
- `Transaction`: id, userId, amount, fee, timestamp, type
- `AccountRepository`: interface with methods `findByUserId(String)` and `save(Account)`
- `TransactionRepository`: interface with `save(Transaction)` method

**Use `ConcurrentHashMap` as in-memory store** for simplicity.

## Step 4: TDD (Unit Tests)
Create `src/test/java/com/example/withdrawal/unit/api/service/WithdrawalServiceTest.java` with **JUnit 5 + Mockito** tests that:
- Mock `AccountRepository` and `TransactionRepository`
- Write at least 4 test methods covering all scenarios from the .feature file
- **DO NOT implement the service logic** — leave the `WithdrawalServiceImpl` class empty (just return null or throw `UnsupportedOperationException`)
- These tests must **FAIL (RED)** when run initially

## Step 5: BDD Glue Code (Cucumber Step Definitions)
Create `src/test/java/com/example/withdrawal/bdd/step/WithdrawalStepDefs.java` that:
- Binds every Given/When/Then from the .feature file
- Uses the **real `WithdrawalServiceImpl`** (which will be implemented by AI later)
- Step methods should call the actual service and store results for assertions

## Step 6: Runner & Configuration
- Create `CucumberTestRunner.java` with `@CucumberOptions` pointing to the features folder
- Add Cucumber dependencies to `pom.xml`
- Configure `application.yml` with basic Spring settings

## Step 7: README
Create a `README.md` explaining:
- How to run unit tests: `mvn test`
- How to run BDD tests: `mvn test -Dcucumber.filter.tags=@Withdrawal`
- The philosophy: "Spec-Contract-First — write tests first, let AI generate implementation"

---

# THE "PAIR AGENT" SIMULATION

After generating all the above, **simulate the Driver Agent** by generating the actual implementation:

**Generate `WithdrawalServiceImpl.java`** that:
1. Implements `WithdrawalService`
2. Passes **ALL** unit tests (both JUnit and Cucumber)
3. Uses `BigDecimal` for all monetary calculations (NEVER `double`)
4. Applies VIP discount logic correctly
5. Throws `InsufficientBalanceException` with clear message when balance insufficient
6. Saves transaction records on success
7. Has cyclomatic complexity ≤ 5

---

# QUALITY GATES (Hard Constraints)

1. **All JUnit tests must pass** (mvn test)
2. **All Cucumber scenarios must pass** (mvn verify)
3. **No usage of `double` or `Float`** for money — use `BigDecimal` only
4. **No modification** to any file in `api/` or `test/` packages
5. **Null safety**: handle null inputs gracefully
6. **Code must compile** with Java 17 without errors

---

# OUTPUT FORMAT

For EACH file you create, output:
1. **Full file path** as a comment at the top
2. **Complete file content** (no placeholders or "..." — include everything needed to run)





---

# CONSTRAINTS & TIPS

- Use **Lombok** (`@Data`, `@Builder`) to reduce boilerplate
- Use **AssertJ** for fluent assertions in tests
- Use **Mockito** `@ExtendWith(MockitoExtension.class)` for unit tests
- The `WithdrawalServiceImpl` should be the **ONLY class with business logic** — keep it thin
- Add `@Service` and `@RequiredArgsConstructor` to the implementation
- For the in-memory repositories, use `ConcurrentHashMap` with `AtomicLong` for ID generation

---

# DELIVERY CHECKLIST

Before you finish, verify:
- [ ] `mvn clean compile` succeeds
- [ ] `mvn test` shows RED initially (if implementation is empty) — but after Step 7, all GREEN
- [ ] All BDD scenarios are mapped to step definitions
- [ ] The project runs without external database dependencies
- [ ] README contains clear instructions

---

# FINAL NOTE

This is a **demonstration project** — keep it simple but complete. Focus on showing the workflow clearly. The withdrawal service doesn't need real HTTP endpoints (just the service layer is sufficient), but you MAY add a `@RestController` as a bonus if it doesn't complicate things.

**Generate everything now. Start with pom.xml, then the directory structure, then populate all files in order.**