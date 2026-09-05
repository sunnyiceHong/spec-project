# TEST_ARCHITECT_SKILL — Test Architect (JUnit 5 + Mockito Specialist)

> Skill-driven phase. **Human-in-the-loop.** Triggered only after
> DOMAIN_ARCHITECT_SKILL is approved.

## Role

Test Architect — converts an approved `.feature` file into a complete JUnit 5 +
Mockito unit test class. **Writes tests only; NO business logic implementation.**

## Trigger

`DOMAIN_ARCHITECT_SKILL` completed and approved.

## Input

The approved `.features/{feature}/final/{feature}.feature`.

## Output

A complete Java unit test class
`src/test/java/com/example/{feature}/unit/api/service/{Feature}ServiceTest.java`:

- JUnit 5 annotations (`@Test`, `@ExtendWith(MockitoExtension.class)`)
- Mockito mocks (`@Mock`, `@InjectMocks`)
- One `@Test` method per Scenario from the `.feature` file
- AssertJ assertions matching the `Then` steps
- **No business logic** — only mocks and assertions
- **Must compile** (correct imports + package structure)

## Prompt Template (copy-paste into Claude / Copilot)

````markdown
You are a Test Architect specializing in JUnit 5 + Mockito + AssertJ. Convert the
approved `.feature` file below into a complete Java unit test class. Do NOT write
any business logic — only the test.

APPROVED FEATURE FILE:
<PASTE THE CONTENTS OF .features/{feature}/final/{feature}.feature HERE>

INSTRUCTIONS:
1. Map each Scenario to one `@Test` method.
2. Derive the necessary mocks from the Given steps.
3. Use the exact numerical values from the Then steps in the AssertJ assertions.
4. Use `BigDecimal` (never `double`/`Float`) for all monetary assertions.
5. Make each test completely independent — no shared mutable state between tests.
6. The class must compile: correct package `com.example.{feature}.unit.api.service`,
   correct imports, and a reference to the service type under test.

OUTPUT & ARCHIVAL:
- Save the test class to `src/test/java/com/example/{feature}/unit/api/service/{Feature}ServiceTest.java`.
- Save this exact prompt to `.features/{feature}/prompts/03_test_architect_prompt.md`.
````

## Review Gate (human)

1. Human reads the generated test class (and, optionally, runs `mvn test` to see it
   compile and fail as expected — RED).
2. **APPROVED** → set `STATE.md` to `TEST_REVIEW` (resolved) and proceed to
   `CONTRACT_STEWARD`.
3. **REJECTED** → human writes a comment; re-run this same skill with the comment
   appended as additional context.

## Archival

- Final artifact → `src/test/java/com/example/{feature}/unit/api/service/{Feature}ServiceTest.java`
- Prompt used → `.features/{feature}/prompts/03_test_architect_prompt.md`
