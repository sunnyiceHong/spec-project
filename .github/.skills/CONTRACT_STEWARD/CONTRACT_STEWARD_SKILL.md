# CONTRACT_STEWARD_SKILL — Contract Steward (API Designer)

> Skill-driven phase. **Human-in-the-loop.** Triggered only after
> TEST_ARCHITECT_SKILL is approved.

## Role

Contract Steward — designs the **frozen API contract** (service interface + DTOs +
validation) plus a compilable **stub implementation** so the tests can compile.

## Trigger

`TEST_ARCHITECT_SKILL` completed and approved.

## Input

The approved `.feature` file and the approved test class.

## Output

Under `src/main/java/com/example/{feature}/`:

- **Service interface** — method signatures (extract nouns → DTOs, verbs → methods).
- **DTOs** — Request and Response objects with Jakarta Validation annotations
  (`@NotNull`, `@Positive`, `@DecimalMin`, `@DecimalMax`, `@NotBlank`).
- **Stub `ServiceImpl`** at
  `src/main/java/com/example/{feature}/application/impl/{Feature}ServiceImpl.java`
  that throws `UnsupportedOperationException` (so the test class compiles). This is
  the exact class the DEVELOPER will later replace with real logic.

### Constraints

- All monetary fields must be `BigDecimal`.
- Validation annotations must match the business rules in the `.feature`.
- Package structure: `com.example.{feature}.api.*` (interface + DTOs) and
  `com.example.{feature}.application.impl.*` (stub).
- The generated code must compile together with the test class.

## Prompt Template (copy-paste into Claude / Copilot)

````markdown
You are a Contract Steward / API Designer. Design the API contract for the feature
described below, plus a compilable stub implementation.

APPROVED FEATURE FILE:
<PASTE .features/{feature}/final/{feature}.feature HERE>

APPROVED TEST CLASS:
<PASTE src/test/java/com/example/{feature}/unit/api/service/{Feature}ServiceTest.java HERE>

INSTRUCTIONS:
1. Extract nouns as DTOs and verbs as method signatures.
2. Add Jakarta Validation annotations to DTO fields based on the feature's business
   rules. Use `BigDecimal` (never `double`/`Float`) for all monetary fields.
3. Create the service interface and DTOs in package `com.example.{feature}.api`.
4. Create a stub `{Feature}ServiceImpl` in package
   `com.example.{feature}.application.impl` that implements the interface and throws
   `UnsupportedOperationException` from every method (so the test compiles).
5. Ensure everything compiles together with the test class.

OUTPUT & ARCHIVAL:
- Save interface + DTOs to `src/main/java/com/example/{feature}/api/`.
- Save the stub to `src/main/java/com/example/{feature}/application/impl/{Feature}ServiceImpl.java`.
- Save this exact prompt to `.features/{feature}/prompts/04_contract_steward_prompt.md`.
````

## Review Gate (human)

1. Human reviews the interface, DTOs, and validation annotations.
2. **APPROVED** → set `STATE.md` to `CONTRACT_REVIEW` (resolved) and proceed to
   `DEVELOPER`.
3. **REJECTED** → human writes a comment; re-run this same skill with the comment
   appended as additional context.

## Archival

- Final artifact → `src/main/java/com/example/{feature}/api/` + stub `.../application/impl/{Feature}ServiceImpl.java`
- Prompt used → `.features/{feature}/prompts/04_contract_steward_prompt.md`
