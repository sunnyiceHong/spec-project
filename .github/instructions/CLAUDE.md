# Skills Toolkit — AI Instructions

> Entry point for the AI (Claude / Copilot) operating this skill-driven toolkit.
> Read it fully before doing any work. It governs **all** skills under
> `.github/.skills/`, not only a single workflow.

## 1. Always start from the skills registry

Read `.github/instructions/skills.json` first. It is the **registry of every skill**
under `.github/.skills/`, mapping each skill name to its `path` and a short
`description`. It lists all skills — present and future.

To run a skill:

1. Look up its `path` in `skills.json`.
2. Open that Skill markdown file. It is self-contained: role, input, output,
   prompt template(s), scripts, and review gate.

**Never hardcode a skill path** — resolve it from `skills.json`. If a path in
`skills.json` does not exist on disk, stop and tell the human.

## 2. Scripts live next to each skill

A skill's python helpers live in a `scripts/` folder **inside the skill's own
folder**, so a skill and its scripts are always side by side:

```
.github/.skills/<SKILL_NAME>/
├── <SKILL_NAME>_SKILL.md   # the self-contained skill
└── scripts/                # mechanical helper scripts (optional)
    └── <script>.py
```

- Scripts are pure mechanical filesystem helpers — **no LLM calls**.
- Each skill file documents the scripts it uses and their exact usage, e.g.
  `python .github/.skills/<SKILL_NAME>/scripts/<script>.py <args>`.
- To find a skill's scripts: open the skill file, or list
  `.github/.skills/<SKILL_NAME>/scripts/`.

## 3. Skill execution is human-in-the-loop

- Every skill ends at a **review gate**. Do not advance to the next skill until the
  human explicitly approves the output.
- On rejection, re-run the **same** skill with the human's rejection comment
  appended as additional context.

## 4. Prompt archival is mandatory

After each skill, save both:

1. the final artifact → the path the skill file specifies, and
2. the **exact prompt text** that generated it → the archive path the skill file
   specifies.

This enables re-development from the exact prompt that produced any artifact.

## 5. Feature isolation

Artifacts for one feature live under `.features/{feature}/` (its own `input/`,
`prompts/`, `final/`, and state file). Generated Java code goes into the root Spring
project under `src/`.

## 6. Workflows

A **workflow** is an ordered sequence of skills for a specific kind of task. The
spec-code feature-development workflow (`BA → DOMAIN_ARCHITECT → TEST_ARCHITECT →
CONTRACT_STEWARD → DEVELOPER → TECHLEAD`, with its state machine) is documented in
`docs/WORKFLOW.md`. Consult it when running that workflow.

## 7. Language & library conventions (Vavr 0.11.0)

Generated Spring Boot code uses **Vavr 0.11.0** (`io.vavr:vavr`). Every skill that
produces Java (TEST_ARCHITECT, CONTRACT_STEWARD, DEVELOPER, TECHLEAD) must follow
these conventions:

- **Failure paths** use `io.vavr.control.Try` / `Either` — never throw checked
  exceptions from the service interface.
- **Null-safety** uses `io.vavr.control.Option` — never `java.util.Optional`.
- **Collections** are immutable `io.vavr.collection.*` (`List`, `Map`, `Set`, `Seq`).
- **Money** stays `BigDecimal` (never `double`/`Float`).
- 0.11.0 additions available: lazy `For()` comprehension, `Either.cond(...)`,
  `Validation.cond(...)`, `Try.toEither(...)`, and JSpecify null-safety annotations.

The canonical version lives in `pom.xml` (`<vavr.version>0.11.0</vavr.version>`);
do not hardcode a different version in generated code.
