# DOMAIN_ARCHITECT_SKILL — Domain Architect (BDD Specialist)

> Skill-driven phase. **Human-in-the-loop.** Triggered only after BA_SKILL is
> approved.

## Role

Domain Architect — converts an approved Requirement Document into a standard
Gherkin `.feature` file (the executable spec).

## Trigger

`BA_SKILL` completed and approved.

## Input

The approved `.features/{feature}/final/requirement.md`.

## Output

A Gherkin `.feature` file at `.features/{feature}/final/{feature}.feature` with:

- Feature description
- Background (if applicable)
- Multiple Scenarios covering **happy path**, **exceptions**, and **boundaries**
- Given-When-Then steps with **concrete numerical assertions**
- A **consistent step vocabulary** (same phrasing reused across scenarios) so the
  Cucumber step definitions in the TEST_ARCHITECT phase bind cleanly

> This `.feature` is the **executable spec**: TEST_ARCHITECT copies it to the test
> classpath and binds each step to the service via Cucumber. It is not just
> documentation.

## Prompt Template (copy-paste into Claude / Copilot)

````markdown
You are a Domain Architect specializing in BDD (Gherkin). Convert the approved
Requirement Document below into a Gherkin `.feature` file.

APPROVED REQUIREMENT DOCUMENT:
<PASTE THE CONTENTS OF .features/{feature}/final/requirement.md HERE>

INSTRUCTIONS:
1. Parse the Requirement Document.
2. Generate comprehensive Gherkin scenarios. Cover AT LEAST:
   - 1 positive / happy-path scenario
   - 1 negative / exception scenario
   - 1 boundary scenario
3. Write Given-When-Then steps with concrete numerical assertions (exact amounts,
   fees, balances) — no vague wording.
4. If the requirement is ambiguous, ask up to 3 clarifying questions BEFORE
   producing the final file. Do not guess silently.

OUTPUT & ARCHIVAL:
- Save the final `.feature` file to `.features/{feature}/final/{feature}.feature`.
- Save this exact prompt to `.features/{feature}/prompts/02_domain_architect_prompt.md`.
````

## Review Gate (human)

1. Human reads `.features/{feature}/final/{feature}.feature`.
2. **APPROVED** → set `STATE.md` to `DOMAIN_REVIEW` (resolved) and proceed to
   `TEST_ARCHITECT`.
3. **REJECTED** → human writes a comment; re-run this same skill with the comment
   appended as additional context.

## Archival

- Final artifact → `.features/{feature}/final/{feature}.feature`
- Prompt used → `.features/{feature}/prompts/02_domain_architect_prompt.md`
