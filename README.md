# Spec-Code Agent

A **skill-driven, human-in-the-loop** multi-agent toolkit that orchestrates the full
lifecycle of **Spring Boot feature development**. Each role in the workflow is a
self-contained **Skill** (a markdown document with role definition, input/output
schemas, a copy-paste prompt, and a review gate) — the intelligence lives in the
prompts, not in glue code.

> This repo is **the agent** (the orchestration system). It *generates* Spring Boot
> code as its output — the Spring project is **not** the agent.

## The 6 roles

| # | Skill | Role | Produces |
|---|---|---|---|
| 1 | `BA` | Business Analyst | structured `requirement.md` |
| 2 | `DOMAIN_ARCHITECT` | BDD Specialist | Gherkin `.feature` file |
| 3 | `TEST_ARCHITECT` | JUnit 5 + Mockito | unit test class (RED, no logic) |
| 4 | `CONTRACT_STEWARD` | API Designer | service interface + DTOs + stub |
| 5 | `DEVELOPER` | Implementer | `{Feature}ServiceImpl` (GREEN) |
| 6 | `TECHLEAD` | Final Approver | PR review report |

## The workflow

```
BA → DOMAIN_ARCHITECT → TEST_ARCHITECT → CONTRACT_STEWARD → DEVELOPER → TECHLEAD
```

Each phase ends at a **human review gate**. The agent does **not** advance to the next
phase until the human explicitly approves. On rejection, the feature loops back to
that phase and the human's comment is fed into the next execution.

## How it works

- Every Skill generates **prompts** that get **archived per feature** under
  `.features/{feature}/prompts/`, so any artifact can be re-developed later from the
  exact prompt that produced it.
- **Scripts are mechanical only** (`scaffold_feature.py`, `archive_feature.py`) — no
  LLM calls; all "thinking" happens in the Skill prompts.
- The AI resolves every skill's path from `.github/instructions/skills.json`
  (the generic registry of all skills), following `.github/instructions/CLAUDE.md`.
  Each Skill file documents its own scripts.

## Quickstart

1. **Scaffold a feature:**
   ```bash
   python .github/.skills/BA/scripts/scaffold_feature.py payment
   ```
   This creates `.features/payment/` with `input/`, `prompts/`, `final/`, and
   `STATE.md`.

2. **Start the BA phase.** Open `.github/.skills/BA/BA_SKILL.md`, copy the
   **Prompt Template**, paste it into Claude/Copilot (with your raw description in
   `input/requirement_raw.txt`), get the output, and save it as instructed.

3. **Review the output.** Approve or reject it. On rejection, re-run the same Skill
   with your comment as extra context.

4. **Proceed to the next Skill** (`DOMAIN_ARCHITECT` → `TEST_ARCHITECT` →
   `CONTRACT_STEWARD` → `DEVELOPER` → `TECHLEAD`), repeating steps 2–3 each time.

5. **After merge**, optionally archive the feature's prompts/finals:
   ```bash
   python .github/.skills/TECHLEAD/scripts/archive_feature.py payment
   ```

## Layout

```
.github/
├── instructions/
│   ├── skills.json        # registry: skill name → path + description
│   └── CLAUDE.md          # operating contract for the AI
└── .skills/
    ├── BA/                BA_SKILL.md + scripts/scaffold_feature.py
    ├── DOMAIN_ARCHITECT/  DOMAIN_ARCHITECT_SKILL.md
    ├── TEST_ARCHITECT/    TEST_ARCHITECT_SKILL.md
    ├── CONTRACT_STEWARD/  CONTRACT_STEWARD_SKILL.md
    ├── DEVELOPER/         DEVELOPER_SKILL.md
    └── TECHLEAD/          TECHLEAD_SKILL.md + scripts/archive_feature.py

docs/
├── WORKFLOW.md            # architecture, state machine, rejection loop
└── FEATURE_TEMPLATE.md    # BA starting template

.features/{feature}/       # per-feature artifacts (feature isolation)
├── input/requirement_raw.txt
├── prompts/               # archived prompts
├── final/                 # approved artifacts
└── STATE.md

src/                       # the Spring Boot project the agent writes code into
```

See `docs/WORKFLOW.md` for the full architecture.
