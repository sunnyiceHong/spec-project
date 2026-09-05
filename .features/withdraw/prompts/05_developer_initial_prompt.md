# Archived prompt — DEVELOPER phase (05, initial)

You are a Developer. Implement the service for the feature below so it passes all
unit tests.

ARTIFACTS (all on disk; their generation prompts are archived in 01–04):
1. requirement:  .features/withdraw/final/requirement.md
2. feature:      .features/withdraw/final/withdraw.feature
3. test class:   src/test/java/com/example/withdraw/unit/api/service/WithdrawServiceTest.java
4. interface:    src/main/java/com/example/withdraw/api/
5. stub:         src/main/java/com/example/withdraw/application/impl/WithdrawServiceImpl.java

INSTRUCTIONS:
1. Replace the stub `WithdrawServiceImpl` with real logic in the SAME file path.
2. Implement the interface so all unit tests pass.
3. Use Vavr 0.11.0 (`io.vavr`) idioms: `Try`/`Either` for failure paths (not thrown
   checked exceptions), `Option` for null-safety (never `java.util.Optional`), and
   immutable `io.vavr.collection.*` collections. 0.11.0 additions include
   `Try.toEither(...)` and `Either.cond(...)`.
4. Use `BigDecimal` for all monetary calculations.
5. Do NOT modify any file under `api/` or `src/test/`.

OUTPUT & ARCHIVAL:
- Save the implementation to `src/main/java/com/example/withdraw/application/impl/WithdrawServiceImpl.java`.
- Save this exact prompt to `.features/withdraw/prompts/05_developer_initial_prompt.md`.
