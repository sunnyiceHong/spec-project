# DEVELOPER_SKILL — Developer (Implementation Agent)

> Skill-driven phase. **Human-in-the-loop.** Triggered only after
> CONTRACT_STEWARD_SKILL is approved.

## Role

Developer — replaces the stub with the **real implementation** that satisfies every
Gherkin scenario in the Cucumber BDD suite (`mvn test` green). Includes a built-in
**self-healing loop** (fix-from-logs) and a **navigator review** (code quality).

## Trigger

`CONTRACT_STEWARD_SKILL` completed and approved.

## Input

All prior artifacts: requirement, `.feature`, Cucumber step definitions + runner, and
contracts (interface, DTOs, stub).

## Output

A complete implementation
`src/main/java/com/example/{feature}/service/impl/{Feature}ServiceImpl.java`:

- Implements the service interface.
- Passes **all** Gherkin scenarios (Cucumber) and unit tests when `mvn test` is run.
- Uses `BigDecimal` for all calculations (never `double`/`Float`).
- Handles null inputs gracefully.
- Contains proper exception handling.
- Clean, readable code (no duplication).

## Prompt Template A — initial implementation (copy-paste into Claude / Copilot)

````markdown
You are a Developer. Implement the service for the feature below so it satisfies every
Gherkin scenario (BDD) in the Cucumber suite.

ARTIFACTS (paste ALL of these):
1. requirement:  .features/{feature}/final/requirement.md
2. feature:      .features/{feature}/final/{feature}.feature
3. step defs:    src/test/java/com/example/{feature}/bdd/{Feature}StepDefinitions.java
4. interface:    src/main/java/com/example/{feature}/service/
5. stub:         src/main/java/com/example/{feature}/service/impl/{Feature}ServiceImpl.java

INSTRUCTIONS:
1. Replace the stub `{Feature}ServiceImpl` with real logic in the SAME file path.
2. Implement the interface so every Gherkin scenario (Cucumber) passes.
3. Use Vavr 0.11.0 (`io.vavr`) idioms: `Try`/`Either` for failure paths (not thrown
   checked exceptions), `Option` for null-safety (never `java.util.Optional`), and
   immutable `io.vavr.collection.*` collections. 0.11.0 additions include
   `Try.toEither(...)` and `Either.cond(...)`.
4. Use `BigDecimal` for all monetary calculations.
5. Do NOT modify any file under `dto/`, `entity/`, `repository/`, `service/` (the
   contract) or `src/test/`.

OUTPUT & ARCHIVAL:
- Save the implementation to `src/main/java/com/example/{feature}/service/impl/{Feature}ServiceImpl.java`.
- Save this exact prompt to `.features/{feature}/prompts/05_developer_initial_prompt.md`.
````

## Self-Healing Loop (max 5 iterations)

1. Generate the initial implementation (Prompt A).
2. Human runs `mvn test` and captures the error log.
3. Use **Prompt B** to fix from the logs.
4. Repeat until tests pass — **max 5 iterations**.

### Prompt Template B — fix from logs

````markdown
You are a Developer. The implementation failed `mvn test`. Fix it using the error log.

MAVEN ERROR LOG:
<PASTE THE FULL mvn test OUTPUT HERE>

CURRENT IMPLEMENTATION:
<PASTE src/main/java/com/example/{feature}/service/impl/{Feature}ServiceImpl.java HERE>

INSTRUCTIONS:
1. Diagnose the failure(s) from the log.
2. Produce a fixed version of the implementation. Do NOT modify `dto/`, `entity/`,
   `repository/`, `service/` (the contract) or `src/test/`.
   Keep Vavr 0.11.0 idioms (`Try`/`Either`/`Option`, immutable `io.vavr.collection.*`
   collections).
3. Explain, in 1-3 bullets, what you changed and why.

OUTPUT & ARCHIVAL:
- Overwrite `src/main/java/com/example/{feature}/service/impl/{Feature}ServiceImpl.java` with the fixed version.
- Save this exact prompt (including the log) to `.features/{feature}/prompts/05_developer_fix_vN_prompt.md` (increment N per iteration).
````

## Navigator Review (after tests pass)

After tests are green, use **Prompt C** for a code-quality review.

### Prompt Template C — code quality review

````markdown
You are a senior code reviewer (Navigator). Review the implementation below for
design patterns, performance, and null-safety. Do NOT change behavior or break tests.

IMPLEMENTATION:
<PASTE src/main/java/com/example/{feature}/service/impl/{Feature}ServiceImpl.java HERE>

INSTRUCTIONS:
1. Review for design patterns, performance, and null-safety. Verify Vavr 0.11.0 usage
   is idiomatic: `Option` over null checks, `Try`/`Either` over try/catch, immutable
   `io.vavr.collection.*` collections, and no `java.util.Optional` leaking in.
2. If issues are found, provide a refactored version (behavior-preserving).
3. If no issues, say so explicitly.

OUTPUT & ARCHIVAL:
- If refactored, overwrite the implementation with the refactored version.
- Save this exact prompt to `.features/{feature}/prompts/05_developer_review_prompt.md`.
````

## Review Gate (human)

1. Human reviews the implementation and confirms `mvn test` is green.
2. **APPROVED** → set `STATE.md` to `DEV_REVIEW` (resolved) and proceed to `TECHLEAD`.
3. **REJECTED** → human writes a comment; re-run from Prompt B with the comment + new
   logs as context.

## Archival

- Final implementation → `src/main/java/com/example/{feature}/service/impl/{Feature}ServiceImpl.java`
- Prompts used → `.features/{feature}/prompts/05_developer_*.md` (initial, fix_vN, review)
