# BA_SKILL — Business Analyst

> Skill-driven phase. **Human-in-the-loop.** Do not start DOMAIN_ARCHITECT until
> the human approves this phase's output.

## Role

Business Analyst — turns a raw natural-language feature description into a
structured, unambiguous Requirement Document.

## Trigger

The user provides a raw feature description.

## Input

Raw requirement text. Expect it at `.features/{feature}/input/requirement_raw.txt`
(created by `scaffold_feature.py`).

## Output

A structured Requirement Document (markdown) at
`.features/{feature}/final/requirement.md`, containing:

- **Feature Name**
- **Business Goal**
- **User Stories** (As a… I want… So that…)
- **Acceptance Criteria** (bullet list, testable)
- **Edge Cases** (explicitly listed)
- **Non-functional requirements** (performance, security, etc.)

## Prompt Template (copy-paste into Claude / Copilot)

````markdown
You are a Business Analyst. Convert the raw feature description below into a
structured Requirement Document.

RAW FEATURE DESCRIPTION:
<PASTE THE CONTENTS OF .features/{feature}/input/requirement_raw.txt HERE>

INSTRUCTIONS:
1. Produce a Requirement Document in markdown with exactly these sections:
   - Feature Name
   - Business Goal
   - User Stories (As a... I want... So that...)
   - Acceptance Criteria (bullet list)
   - Edge Cases (explicitly listed)
   - Non-functional requirements
2. If the raw description is ambiguous, ask up to 3 clarifying questions BEFORE
   producing the final document. Do not guess silently.
3. Make every acceptance criterion testable and concrete — use numbers, not vague
   words like "fast", "cheap", or "reasonable".

OUTPUT & ARCHIVAL:
- Save the final Requirement Document to `.features/{feature}/final/requirement.md`.
- Save this exact prompt to `.features/{feature}/prompts/01_ba_prompt.md`.
````

## Review Gate (human)

1. Human reads `.features/{feature}/final/requirement.md`.
2. **APPROVED** → set `.features/{feature}/STATE.md` to `BA_REVIEW` (resolved) and
   proceed to `DOMAIN_ARCHITECT`.
3. **REJECTED** → human writes a comment; re-run this same skill with the comment
   appended to the raw description as additional context.

## Archival

- Final artifact → `.features/{feature}/final/requirement.md`
- Prompt used → `.features/{feature}/prompts/01_ba_prompt.md`
