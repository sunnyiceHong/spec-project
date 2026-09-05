# Project Status & Handoff Summary

> Feed this file back to Claude (e.g. "read PROJECT_STATUS.md and continue") to
> resume work on this project with full context. Last updated: **2026-09-05**.

---

## 1. One-line summary

This repo is now the **Spec-Code Agent toolkit** — a **skill-driven,
human-in-the-loop** multi-agent system that orchestrates the full lifecycle of
Spring Boot feature development. (The project **pivoted away** from the earlier
"user withdrawal service" demo; that feature is abandoned — ignore it.)

## 2. Core concept (get this right, it drives everything)

- The **toolkit is the agent**. It *generates* Spring Boot code as its output; the
  Spring project is **not** the agent.
- **Skill-driven, not script-driven**: all intelligence lives in markdown **Skill**
  files (each = role + input/output schema + copy-paste prompt template + review
  gate). Python scripts are **mechanical only** (filesystem ops, **no LLM calls**).
- **Human-in-the-loop**: the agent never auto-advances. Every phase ends at a
  human review gate; rejection loops the feature back to that phase with the
  comment as new context.
- **Prompt archival is mandatory**: every phase saves the exact prompt used (so any
  artifact can be re-developed later).

## 3. The 6-role workflow + state machine

```
BA → DOMAIN_ARCHITECT → TEST_ARCHITECT → CONTRACT_STEWARD → DEVELOPER → TECHLEAD
```

| # | Skill | Role | Produces |
|---|---|---|---|
| 1 | BA | Business Analyst | structured `requirement.md` |
| 2 | DOMAIN_ARCHITECT | BDD Specialist | Gherkin `.feature` |
| 3 | TEST_ARCHITECT | Cucumber BDD | step defs + runner (RED) |
| 4 | CONTRACT_STEWARD | API Designer | interface + DTOs + stub |
| 5 | DEVELOPER | Implementer | `ServiceImpl` (GREEN) |
| 6 | TECHLEAD | Final Approver | PR review report |

State machine (per feature, in `STATE.md`):
`DRAFT → BA_REVIEW → DOMAIN_REVIEW → TEST_REVIEW → CONTRACT_REVIEW → DEV_REVIEW → TECHLEAD_REVIEW → APPROVED → MERGED`

## 4. File map (what lives where — all built this session)

```
.github/
├── instructions/
│   ├── skills.json          # GENERIC registry: skill name → {path, description}
│   └── CLAUDE.md            # GENERIC operating manual (how to use any skill)
└── .skills/
    ├── BA/                  BA_SKILL.md + scripts/scaffold_feature.py
    ├── DOMAIN_ARCHITECT/    DOMAIN_ARCHITECT_SKILL.md
    ├── TEST_ARCHITECT/      TEST_ARCHITECT_SKILL.md
    ├── CONTRACT_STEWARD/    CONTRACT_STEWARD_SKILL.md
    ├── DEVELOPER/           DEVELOPER_SKILL.md
    └── TECHLEAD/            TECHLEAD_SKILL.md + scripts/archive_feature.py

docs/
├── WORKFLOW.md              # SPEC-CODE specifics: 6-phase diagram, state, rejection loop
└── FEATURE_TEMPLATE.md      # BA starting template

README.md                    # toolkit quickstart (rewritten)

src/                         # the ROOT Spring Boot project the agent writes code into
├── main/java/com/example/withdrawal/WithdrawalApplication.java  # leftover skeleton
└── main/resources/application.yml                                # leftover skeleton

.features/                   # per-feature root (empty until scaffolded; scaffold creates:
                             #   {feature}/input/requirement_raw.txt, prompts/, final/, STATE.md)
```

## 5. Key conventions & decisions (NON-OBVIOUS — read before continuing)

1. **Layout hierarchy**: `skills.json` = generic registry · `CLAUDE.md` = generic
   rules · `docs/WORKFLOW.md` = spec-code workflow specifics. Adding a new skill =
   drop a folder in `.github/.skills/<NAME>/` + add ONE entry to `skills.json`
   (no edit to `CLAUDE.md`).
2. **Scripts are colocated** with their skill: `.github/.skills/<NAME>/scripts/`.
   `scaffold_feature.py` → BA phase (kickoff); `archive_feature.py` → TECHLEAD
   phase (post-merge).
3. **skills.json must stay pure ASCII** — Windows Python defaults to GBK; non-ASCII
   (arrows/em-dashes) caused a `UnicodeDecodeError` when json-parsed. Use `->`/`-`.
4. **Feature input folder is `input/` (singular)**, not `inputs/`.
5. **Package layout (Spring-style layers)**: generated Java goes under
   `com.example.{feature}` split by responsibility — `controller/` (thin REST adapter),
   `dto/` (request/response/error), `entity/` (domain types), `repository/` (port),
   `service/` (interface), `service/impl/` (implementation). Contract Steward writes
   the stub at `service/impl/{Feature}ServiceImpl.java` — the SAME path the Developer
   later replaces — and the controller delegates to the service (no business logic in
   the controller). Never dump everything in one flat package.
6. **`archive_feature.py` imports `time`** (stdlib) for its timestamp suffix — a
   deliberate deviation from design.md's "only os/pathlib/sys" (timestamp needs it).
7. **Claude Code does NOT auto-load `.github/instructions/CLAUDE.md`** (that path is
   a GitHub Copilot agent-mode convention). For Claude Code to auto-load, add a root
   `CLAUDE.md` that points to it — NOT done yet.
8. **Leftover withdrawal code**: `src/main/java/com/example/withdrawal/` and
   `pom.xml` metadata still say "withdrawal-service" (`name`, `<description>`, Vavr
   0.10.4, Cucumber 7.18.1). Kept only as a Spring skeleton — not cleaned up yet.
9. **design.md is local-only** (in `.gitignore`) and was fully rewritten to be the
   Spec-Code Agent spec on 2026-08-25.

## 6. Verification done this session

- ✅ All 12 toolkit files created.
- ✅ `skills.json` validates as JSON (ASCII-safe).
- ✅ Both `.py` scripts pass `python -m py_compile`.
- ✅ `scaffold_feature.py` smoke-tested (creates `input/`, `prompts/`, `final/`,
  `STATE.md`), then the test folder was removed.
- ⚠️ **Nothing is committed yet** — all work is uncommitted on `main`.

## 7. Open items / suggested next steps

- [ ] Commit the toolkit (the user has not asked to commit yet).
- [ ] (Optional) add a root `CLAUDE.md` pointing to `.github/instructions/CLAUDE.md`
      so Claude Code auto-loads the instructions.
- [ ] Build one **worked example feature** (e.g. `payment` or `withdraw`) end-to-end
      through all 6 skills to validate the toolkit.
- [ ] Clean up leftover withdrawal code + `pom.xml` metadata (name/description) if
      the Spring skeleton should be feature-agnostic.
- [ ] Decide whether `skills.json` needs a `group` field to route non-spec-code
      skills (currently the workflow order lives only in `CLAUDE.md`/`WORKFLOW.md`).
- [ ] (Optional) add CI (GitHub Actions).

## 8. Key commands

```bash
# Scaffold a new feature (creates .features/<name>/ skeleton)
python .github/.skills/BA/scripts/scaffold_feature.py <feature_name>

# Archive a feature's prompts/ + final/ (post-merge, for re-development)
python .github/.skills/TECHLEAD/scripts/archive_feature.py <feature_name>

# Spring build (root project)
mvn clean compile
mvn test
```

## 9. How to operate the toolkit (quick refresher)

1. `scaffold_feature.py <name>` → creates `.features/<name>/`.
2. Read `skills.json` → resolve the skill's `path` → open the Skill markdown.
3. Copy its Prompt Template into Claude/Copilot, get output, save artifact + prompt.
4. Human approves/rejects → advance or loop back.
5. After TECHLEAD approves + merge → `archive_feature.py <name>`.
