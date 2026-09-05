# Spec-Code Agent — Workflow Architecture

## 1. The 6-phase workflow

```
 ┌──────────────────────────────────────────────────────────────────────────┐
 │  BA ──► DOMAIN_ARCHITECT ──► TEST_ARCHITECT ──► CONTRACT_STEWARD         │
 │  │            │                    │                   │                 │
 │  requirement  .feature          unit tests         interface+DTOs        │
 │  .md          (Gherkin)         (RED)              + stub impl            │
 │                                                                          │
 │  DEVELOPER ──► TECHLEAD ──► APPROVED ──► MERGED                           │
 │  │               │                                                        │
 │  impl (GREEN)    PR review report                                         │
 └──────────────────────────────────────────────────────────────────────────┘
```

Each phase produces one artifact, ends in a **human review gate**, and only proceeds
on explicit human approval.

| # | Skill | Role | Input | Output |
|---|---|---|---|---|
| 1 | BA | Business Analyst | raw description | `requirement.md` |
| 2 | DOMAIN_ARCHITECT | BDD Specialist | `requirement.md` | `{feature}.feature` |
| 3 | TEST_ARCHITECT | JUnit+Mockito | `.feature` | `{Feature}ServiceTest.java` (RED) |
| 4 | CONTRACT_STEWARD | API Designer | `.feature` + test | interface + DTOs + stub |
| 5 | DEVELOPER | Implementer | all artifacts | `{Feature}ServiceImpl.java` (GREEN) |
| 6 | TECHLEAD | Approver | all artifacts | PR review report |

## 2. State Management

Each feature tracks its state in `.features/{feature}/STATE.md`:

```
DRAFT → BA_REVIEW → DOMAIN_REVIEW → TEST_REVIEW → CONTRACT_REVIEW
      → DEV_REVIEW → TECHLEAD_REVIEW → APPROVED → MERGED
```

- A `*_REVIEW` state means "this phase produced output, now awaiting human approval."
- On approval, the state advances to the next phase's work.
- On rejection, the state stays in the same `*_REVIEW` and the feature loops back.

## 3. Rejection Loop

If any review fails, the feature goes **back to the corresponding phase**. The
human's rejection comment is fed back into the **same** Skill's next execution as
additional context — so the re-run is more informed, not a blind retry.

```
phase produces artifact ──► human review ──► approved? ──yes──► next phase
                                   │
                                  no
                                   ▼
                    human comment → re-run SAME skill ──► review again
```

## 4. Prompt Archival

For each feature, every **final prompt** (the exact text that generated the approved
artifact) is saved to `.features/{feature}/prompts/`. Approved artifacts go to
`.features/{feature}/final/` (or `src/` for code).

This enables **re-development**: a developer can open
`.features/{feature}/prompts/05_developer_initial_prompt.md` and see exactly how the
final implementation was generated.

## 5. Skill Composition

Each Skill (markdown document) is self-contained and contains five parts:

1. **Role Definition** — what this agent is and does.
2. **Input Schema** — what it consumes and from where.
3. **Output Schema** — what it produces and where it is saved.
4. **Prompt Template** — the full copy-paste prompt the human runs in Claude/Copilot.
5. **Review Gate** — how the human approves or rejects, and the rejection loop.

The `scripts/` folder inside a Skill (if present) holds that phase's mechanical
helpers (no LLM calls).

## 6. Skill resolution

The AI reads `.github/instructions/skills.json` to resolve every skill's `path` —
it is a generic registry of all skills under `.github/.skills/`. Each Skill file is
self-contained and documents its own scripts.
`.github/instructions/CLAUDE.md` is the operating contract — see it for the rules.
