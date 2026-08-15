# Project Status & Handoff Summary

> Feed this file back to Claude (e.g. "read PROJECT_STATUS.md and continue") to
> resume work on this project with full context. Last updated: **2026-08-15**.

---

## 1. One-line summary

A complete Spring Boot 3.4 / Java 21 Maven demo that demonstrates the
**Spec-Contract-First → TDD → BDD → Pair-Agent** workflow using a **user
withdrawal service**. All tests green, pushed to GitHub as 3 PR-merge commits.

## 2. Location & remote

- **Local path:** `D:\workspace\java\spec-project`
- **Git remote (SSH):** `git@github.com:sunnyiceHong/spec-project.git`
- **Web:** `https://github.com/sunnyiceHong/spec-project`
- **Main branch:** `main` (contains the fully-merged, green project)
- **Feature branches (all pushed):**
  - `feature/contract-and-domain` — PR #1 foundation
  - `feature/spec-and-tests` — PR #2 spec + tests (RED)
  - `feature/implementation` — PR #3 implementation (GREEN)

## 3. Environment

Windows 10 · Java 21.0.6 (Oracle) · Maven 3.9.9 · git 2.48.1 · **no `gh` CLI**
· SSH auth to GitHub works (user `sunnyiceHong`).

## 4. The Spec (business rules)

1. VIP users get 50% discount on withdrawal fees (standard fee = 1%).
2. Regular users pay the full 1% fee.
3. Withdrawal fails with `InsufficientBalanceException` when balance cannot
   cover **principal + fee**.
4. Every successful withdrawal generates a transaction record.

## 5. Tech stack (as actually implemented)

| Concern | Choice | Notes |
|---|---|---|
| Language | Java 21 | source kept 17-compatible (no post-17 features) |
| Framework | Spring Boot 3.4.1 | parent `spring-boot-starter-parent` |
| Build | Maven | `artifactId=withdrawal-service`, `groupId=com.example` |
| Unit tests | JUnit 5 + Mockito + AssertJ | via `spring-boot-starter-test` |
| BDD | Cucumber 7.18.1 | `cucumber-java` + `cucumber-junit` (JUnit 4 runner) |
| JUnit4 bridge | `junit-vintage-engine` | needed to run the Cucumber runner beside JUnit 5 |
| Validation | Jakarta Bean Validation | `spring-boot-starter-validation` |
| Boilerplate | Lombok | `@Data`, `@Builder`, `@RequiredArgsConstructor`, `@NoArgsConstructor`, `@AllArgsConstructor` |
| Functional | Vavr **0.10.4** | design.md said "1.11.0" which does not exist (see §8) |

## 6. File map (what lives where)

```
src/main/java/com/example/withdrawal/
├── WithdrawalApplication.java          # @SpringBootApplication
├── api/                                # FROZEN CONTRACT (do not modify without re-freezing tests)
│   ├── WithdrawRequest.java            #   userId, amount (+ @NotBlank/@NotNull/@DecimalMin)
│   ├── WithdrawResponse.java           #   transactionId, feeCharged, newBalance, timestamp
│   ├── WithdrawalService.java          #   withdraw(request) interface
│   └── InsufficientBalanceException.java  # extends RuntimeException
├── domain/
│   ├── Account.java                    #   userId, balance (BigDecimal), isVip (boolean)
│   ├── Transaction.java                #   id, userId, amount, fee, timestamp, type
│   ├── TransactionType.java            #   enum: WITHDRAWAL
│   ├── AccountRepository.java          #   findByUserId + save
│   └── TransactionRepository.java      #   save only
├── repository/
│   ├── InMemoryAccountRepository.java  #   ConcurrentHashMap keyed by userId
│   └── InMemoryTransactionRepository.java # ConcurrentHashMap + AtomicLong id gen
└── service/
    └── WithdrawalServiceImpl.java      # ONLY class with business logic (Driver Agent)

src/test/java/com/example/withdrawal/
├── unit/api/service/WithdrawalServiceTest.java  # 5 Mockito tests (RED→GREEN)
└── bdd/
    ├── CucumberTestRunner.java                   # @RunWith(Cucumber.class) + @CucumberOptions
    └── step/WithdrawalStepDefs.java              # real service, in-memory repos

src/test/resources/features/withdrawal.feature     # 4 @Withdrawal scenarios
src/main/resources/application.yml                 # app name, port 8080, log level
```

## 7. Test / build status (verified)

- `mvn clean compile` → **BUILD SUCCESS**
- `mvn test` → **Tests run: 9, Failures: 0, Errors: 0** (5 unit + 4 Cucumber)
- `mvn verify` → **exit 0**
- `mvn test -Dcucumber.filter.tags=@Withdrawal` → BDD-only (all 4 scenarios match the tag)

The RED→GREEN flow was demonstrated: with the empty stub, `mvn test` failed as
intended (9 failing), then went green after the implementation.

## 8. Decisions & deviations from design.md (important to remember)

1. **Vavr version:** design.md lists "Vavr 1.11.0" — that version does not
   exist on Maven Central. Used the latest stable **0.10.4**. Documented in
   `pom.xml` and `README.md`. Vavr is on the classpath but unused by the thin
   service (kept intentionally simple).
2. **Java version:** design.md tech stack says "Java 21+", but a quality gate
   says "compile with Java 17". Chose **Java 21** as the target (matches the
   installed JDK) and kept the source 17-compatible, satisfying both in spirit.
3. **PRs in GitHub UI:** no `gh` CLI or API token exists (SSH only), so PRs
   could not be opened in the GitHub web UI. Instead the work was committed to 3
   feature branches and merged into `main` with `--no-ff`, so history reads as
   3 PR merges. To open *live* PRs, install/authenticate `gh` (or provide a
   token), then `gh pr create` from each feature branch.
4. **Cucumber runner style:** uses the JUnit 4 `@RunWith(Cucumber.class)` +
   `@CucumberOptions` (matches design.md wording) with `junit-vintage-engine`.
   The runner class is named `CucumberTestRunner`, which does NOT match
   Surefire's default `*Test` pattern, so `pom.xml` adds an explicit
   `<include>**/CucumberTestRunner.java</include>`.
5. **No `@RestController`:** design.md says HTTP endpoints are optional and the
   service layer suffices. Skipped the controller. `WithdrawalServiceImpl` and
   the in-memory repos are already `@Service`/`@Repository` beans, so a
   controller can be added with zero changes to existing code.
6. **Transaction id ownership:** the id is generated by
   `InMemoryTransactionRepository.save()` (AtomicLong), and the service reads it
   back from the returned `Transaction` — not generated in the service. Unit
   tests stub `save()` to echo back the transaction with an id.

## 9. Suggested next steps (if continuing)

- Add a `@RestController` + `@RestControllerAdvice` (map
  `InsufficientBalanceException` → 400/422) and `@Valid` on the request body.
- Add a Spring Boot integration test (`@SpringBootTest`) wiring the real beans.
- Move fee rates (1% / 50%) into `application.yml` via `@ConfigurationProperties`
  instead of hardcoded constants.
- Add more scenarios: unknown user, zero/negative amount, rounding cases.
- Add CI (GitHub Actions) running `mvn verify` on push/PR.
- (Optional) migrate Cucumber from JUnit 4 runner to the JUnit 5 platform engine.
- Open the 3 live PRs in the GitHub UI once `gh`/token is available.

## 10. Key commands

```bash
cd "D:/workspace/java/spec-project"
mvn clean compile                          # compile
mvn test                                   # unit + BDD
mvn test -Dcucumber.filter.tags=@Withdrawal  # BDD only
mvn verify                                 # full gate
```
