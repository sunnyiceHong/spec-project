# Spec-Code Agent — Workflow Architecture

## 1. The 6-phase workflow

```
 ┌──────────────────────────────────────────────────────────────────────────┐
 │  BA ──► DOMAIN_ARCHITECT ──► TEST_ARCHITECT ──► CONTRACT_STEWARD         │
 │  │            │                    │                   │                 │
 │  requirement  .feature          step defs          interface+DTOs        │
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
| 3 | TEST_ARCHITECT | Cucumber BDD | `.feature` | step definitions + runner (RED) |
| 4 | CONTRACT_STEWARD | API Designer | `.feature` + step defs | interface + DTOs + stub |
| 5 | DEVELOPER | Implementer | all artifacts | `{Feature}ServiceImpl.java` (GREEN) |
| 6 | TECHLEAD | Approver | all artifacts | PR review report |

## 2. State Management — per-phase approval ledger

Each feature tracks **every phase's approval** in `.features/{feature}/STATE.md`:

```markdown
# Feature: {feature}

## Phase approvals

- BA: APPROVED
- DOMAIN_ARCHITECT: APPROVED
- TEST_ARCHITECT: IN_REVIEW
- CONTRACT_STEWARD: PENDING
- DEVELOPER: PENDING
- TECHLEAD: PENDING
```

Each phase has one of four statuses:

- `PENDING` — not started yet.
- `RERUN` — redo this phase (regenerate its artifact); the agent treats it like
  `PENDING` for scheduling. Set a phase (and everything downstream of it) to `RERUN` to
  rebuild it.
- `IN_REVIEW` — the phase's artifact is written, awaiting human approval (the current
  review gate; at most one phase is `IN_REVIEW` at a time).
- `APPROVED` — the human approved this phase's output.

Phases are ordered `BA → DOMAIN_ARCHITECT → TEST_ARCHITECT → CONTRACT_STEWARD →
DEVELOPER → TECHLEAD`. The next phase to run is the earliest `PENDING`/`RERUN` phase;
phases before it are `APPROVED`. When all six are `APPROVED`, the feature is approved
and ready to merge via a PR from `feature/{feature}` → `main` (`MERGED` is set by the
human after merging).

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

## 7. Branching & Merge Discipline

The workflow uses a **feature branch** per feature and merges only via **PR at the end**,
never per phase.

1. **Scaffold → create the branch.** When a feature starts, create `feature/{feature}`
   (e.g. `git checkout -b feature/withdraw`). All generated code and phase commits live
   on this branch — nothing is committed to `main` during development.
2. **Each phase is a WIP commit** on the branch (a checkpoint, not a merge). A phase's
   `APPROVED` status is a **review gate**, not a merge gate — intermediate artifacts
   (RED step defs, stub impls) are not shippable.
3. **Open the PR only when all six phases are `APPROVED`** and `mvn test` is green
   (TECHLEAD `RECOMMEND_MERGE`). The PR target is `feature/{feature}` → `main`.
4. **Merging is a human decision.** After merge, optionally run
   `python .github/.skills/TECHLEAD/scripts/archive_feature.py <feature_name>`.

Why not merge per phase: after TEST_ARCHITECT the build is RED (does not compile); after
CONTRACT_STEWARD the service is a stub that throws `UnsupportedOperationException`.
Only after DEVELOPER + TECHLEAD is the branch complete and green.
