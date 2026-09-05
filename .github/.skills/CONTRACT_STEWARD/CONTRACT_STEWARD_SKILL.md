# CONTRACT_STEWARD_SKILL — Contract Steward (API Designer)

> Skill-driven phase. **Human-in-the-loop.** Triggered only after
> TEST_ARCHITECT_SKILL is approved.

## Role

Contract Steward — designs the **frozen API contract** (service interface + DTOs +
validation) plus a compilable **stub implementation** so the step definitions can compile.

## Trigger

`TEST_ARCHITECT_SKILL` completed and approved.

## Input

The approved `.feature` file and the approved Cucumber step definitions.

## Output

Under `src/main/java/com/example/{feature}/` (Spring-style layers, one package per
responsibility — never one flat package):

- **controller/** — a thin REST `{Feature}Controller` that exposes the API and maps the
  service result to HTTP responses. No business logic here.
- **dto/** — Request / Response / ErrorResponse records with Jakarta Validation
  annotations (`@NotNull`, `@Positive`, `@DecimalMin`, `@DecimalMax`, `@NotBlank`).
- **entity/** — domain types (e.g. `Account`, `TransactionStatus`, `{Feature}Error`).
- **repository/** — the `{Feature}Repository` port interface.
- **service/** — the `{Feature}Service` interface.
- **service/impl/** — a stub `{Feature}ServiceImpl` that throws
  `UnsupportedOperationException` (so the Cucumber step definitions compile). This is the exact class
  the DEVELOPER will later replace with real logic.

### Constraints

- All monetary fields must be `BigDecimal`.
- Validation annotations must match the business rules in the `.feature`.
- Package structure (Spring-style layers): `com.example.{feature}.controller`
  (REST adapter), `com.example.{feature}.dto` (Request/Response/ErrorResponse),
  `com.example.{feature}.entity` (domain types), `com.example.{feature}.repository`
  (port interface), `com.example.{feature}.service` (interface), and
  `com.example.{feature}.service.impl` (stub).
- Use Vavr 0.11.0 (`io.vavr`) for the API surface: methods that can fail return
  `io.vavr.control.Either<..., T>` or `io.vavr.control.Try<T>`; optional values return
  `io.vavr.control.Option<T>` (never `java.util.Optional`); never throw checked
  exceptions from the interface.
- The generated code must compile together with the Cucumber step definitions.

## Prompt Template (copy-paste into Claude / Copilot)

````markdown
You are a Contract Steward / API Designer. Design the API contract for the feature
described below, plus a compilable stub implementation.

APPROVED FEATURE FILE:
<PASTE .features/{feature}/final/{feature}.feature HERE>

APPROVED STEP DEFINITIONS (Cucumber):
<PASTE src/test/java/com/example/{feature}/bdd/{Feature}StepDefinitions.java HERE>

INSTRUCTIONS:
1. Extract nouns as DTOs and verbs as method signatures.
2. Add Jakarta Validation annotations to DTO fields based on the feature's business
   rules. Use `BigDecimal` (never `double`/`Float`) for all monetary fields.
3. Use Vavr 0.11.0 (`io.vavr`) for the API surface: methods that can fail return
   `io.vavr.control.Either<..., T>` or `io.vavr.control.Try<T>`; optional values return
   `io.vavr.control.Option<T>` (never `java.util.Optional`); never throw checked
   exceptions from the interface.
4. Create the service interface in `com.example.{feature}.service`, DTOs (Request /
   Response / ErrorResponse) in `com.example.{feature}.dto`, domain types (e.g.
   `Account`, enums) in `com.example.{feature}.entity`, and the repository port in
   `com.example.{feature}.repository`.
5. Create a thin `{Feature}Controller` in `com.example.{feature}.controller` that
   exposes the API over REST (e.g. `@RestController` + `@PostMapping`) and maps the
   service's `Either` result to HTTP statuses. It delegates to the service interface —
   it must NOT contain business logic.
6. Create a stub `{Feature}ServiceImpl` in `com.example.{feature}.service.impl` that
   implements the interface and throws `UnsupportedOperationException` from every
   method (so the test compiles).
7. Ensure everything compiles together with the Cucumber step definitions.

OUTPUT & ARCHIVAL:
- Save the interface, DTOs, entities, repository port, and controller to their layer
  packages under `src/main/java/com/example/{feature}/`.
- Save the stub to `src/main/java/com/example/{feature}/service/impl/{Feature}ServiceImpl.java`.
- Save this exact prompt to `.features/{feature}/prompts/04_contract_steward_prompt.md`.
````

## Review Gate (human)

1. Human reviews the interface, DTOs, and validation annotations.
2. **APPROVED** → set `STATE.md` to `CONTRACT_REVIEW` (resolved) and proceed to
   `DEVELOPER`.
3. **REJECTED** → human writes a comment; re-run this same skill with the comment
   appended as additional context.

## Archival

- Final artifacts → `src/main/java/com/example/{feature}/{controller,dto,entity,repository,service}/` + stub `.../service/impl/{Feature}ServiceImpl.java`
- Prompt used → `.features/{feature}/prompts/04_contract_steward_prompt.md`
