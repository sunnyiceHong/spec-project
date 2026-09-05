# TEST_ARCHITECT_SKILL — Test Architect (Cucumber BDD)

> Skill-driven phase. **Human-in-the-loop.** Triggered only after
> DOMAIN_ARCHITECT_SKILL is approved.

## Role

Test Architect — turns the approved Gherkin `.feature` into an **executable Cucumber
BDD harness**: step definitions that bind each Given/When/Then step to the
(soon-to-exist) service, plus a Cucumber runner. **Writes the BDD harness only; NO
business logic.**

## Trigger

`DOMAIN_ARCHITECT_SKILL` completed and approved.

## Input

The approved `.features/{feature}/final/{feature}.feature`.

## Output

Under `src/test/`:

- `src/test/resources/features/{feature}.feature` — a copy of the approved Gherkin
  file (Cucumber reads features from the classpath).
- `src/test/java/com/example/{feature}/bdd/{Feature}StepDefinitions.java` — step
  definitions mapping each unique Given/When/Then step to the service.
- `src/test/java/com/example/{feature}/bdd/{Feature}CucumberRunner.java` — the
  JUnit4 Cucumber runner (`@RunWith(Cucumber.class)` + `@CucumberOptions`).

- Step definitions drive the **real service** through a **fresh in-memory
  repository per scenario** (`@Before`): Given steps stage account state, When steps
  invoke the service and capture its `Either` result, Then steps assert with AssertJ.
  **No Spring context, no mocks** — true BDD.
- The harness references types that do not exist yet (service interface, DTOs,
  entities, repository port), so it is **RED** (does not compile) until the
  CONTRACT_STEWARD phase creates them.

## Prompt Template (copy-paste into Claude / Copilot)

````markdown
You are a Test Architect specializing in Cucumber BDD. Convert the approved Gherkin
`.feature` file below into an executable Cucumber test harness. Do NOT write any
business logic — only step definitions and the runner.

APPROVED FEATURE FILE:
<PASTE THE CONTENTS OF .features/{feature}/final/{feature}.feature HERE>

INSTRUCTIONS:
1. Copy the `.feature` to `src/test/resources/features/{feature}.feature`.
2. Write `{Feature}StepDefinitions` in package `com.example.{feature}.bdd`:
   - One method per unique Given/When/Then step, using Cucumber expressions
     (`{string}`, `{bigdecimal}`, `{int}`, `{word}`).
   - Given steps stage account/repository state; When steps invoke the service and
     capture its `Either` result; Then steps assert the outcome with AssertJ.
   - Wire to the real service over a fresh in-memory repository per scenario
     (`@Before`) — no mocks, no Spring context.
3. Write `{Feature}CucumberRunner` in package `com.example.{feature}.bdd` with
   `@RunWith(Cucumber.class)` and `@CucumberOptions(features = "classpath:features",
   glue = "com.example.{feature}.bdd")`.
4. Reference the (not-yet-existing) service interface, DTOs, entities, and repository
   port — so the harness is RED until the contract phase creates them.
5. Use `BigDecimal` (never `double`/`Float`) for all monetary assertions.

OUTPUT & ARCHIVAL:
- Save the harness to `src/test/resources/features/{feature}.feature` and
  `src/test/java/com/example/{feature}/bdd/`.
- Save this exact prompt to `.features/{feature}/prompts/03_test_architect_prompt.md`.
````

## Review Gate (human)

1. Human reads the step definitions + runner (and, optionally, runs `mvn test` to see
   the harness fail to compile — RED).
2. **APPROVED** → set `STATE.md` to `TEST_REVIEW` (resolved) and proceed to
   `CONTRACT_STEWARD`.
3. **REJECTED** → human writes a comment; re-run this same skill with the comment
   appended as additional context.

## Archival

- Final artifacts → `src/test/resources/features/{feature}.feature` +
  `src/test/java/com/example/{feature}/bdd/`
- Prompt used → `.features/{feature}/prompts/03_test_architect_prompt.md`
