# TECHLEAD_SKILL — Tech Lead (Final Approver / PR Reviewer)

> Skill-driven phase. **Human-in-the-loop.** Triggered only after DEVELOPER_SKILL is
> completed and tests are green.

## Role

Tech Lead — reviews all generated artifacts and produces a PR Review Report with a
final **RECOMMEND_MERGE** or **RECOMMEND_REJECT** recommendation.

## Trigger

`DEVELOPER_SKILL` completed and tests are green.

## Input

All generated artifacts: requirement, `.feature`, test class, contracts, and
implementation.

## Output

A PR Review Report at `.features/{feature}/final/techlead_report.md`:

- **Architecture assessment** (layers, dependencies)
- **Design pattern evaluation** (appropriate patterns used?)
- **Risk assessment** (potential production issues)
- **Performance considerations** (N+1 queries, inefficient algorithms)
- **Final recommendation:** `RECOMMEND_MERGE` or `RECOMMEND_REJECT` with detailed
  justification.

## Prompt Template (copy-paste into Claude / Copilot)

````markdown
You are a senior Tech Lead reviewing a PR for the feature below. Produce a PR Review
Report.

ARTIFACTS (paste ALL of these):
1. requirement:  .features/{feature}/final/requirement.md
2. feature:      .features/{feature}/final/{feature}.feature
3. test class:   src/test/java/com/example/{feature}/unit/service/{Feature}ServiceTest.java
4. controller:   src/main/java/com/example/{feature}/controller/{Feature}Controller.java
5. contracts:    src/main/java/com/example/{feature}/{dto,entity,repository,service}/
6. implementation: src/main/java/com/example/{feature}/service/impl/{Feature}ServiceImpl.java

INSTRUCTIONS:
1. Assess architecture: layers, dependencies, coupling.
2. Evaluate design-pattern usage (appropriate? over-engineered?).
3. Identify risks in production (edge cases, null-safety, concurrency).
4. Check performance (inefficient algorithms, redundant work).
5. Verify Vavr 0.11.0 usage is idiomatic and consistent with the contract: `Option`
   for null-safety, `Try`/`Either` for failure paths, immutable `io.vavr.collection.*`
   collections, and no mixing with `java.util.Optional` where the contract uses Vavr.
6. Verify the implementation follows the contract exactly.
7. End with a final recommendation: RECOMMEND_MERGE or RECOMMEND_REJECT, with
   detailed justification. If rejecting, give actionable, specific feedback.

OUTPUT & ARCHIVAL:
- Save the report to `.features/{feature}/final/techlead_report.md`.
- Save this exact prompt to `.features/{feature}/prompts/06_techlead_prompt.md`.
````

## Review Gate (human Tech Lead)

The human reads the AI-generated report and makes the final decision:

- **APPROVED** → set `STATE.md` to `APPROVED`, then open a PR from
  `feature/{feature}` → `main` and merge (`MERGED` once merged). Optionally run
  `archive_feature.py` to archive `prompts/` and `final/` for re-development.
- **REJECTED** → set `STATE.md` back to `DEV_REVIEW`; return to DEVELOPER_SKILL with
  the human's comments as additional context, and the loop repeats.

## Archival

- Final report → `.features/{feature}/final/techlead_report.md`
- Prompt used → `.features/{feature}/prompts/06_techlead_prompt.md`
- Script → `python .github/.skills/TECHLEAD/scripts/archive_feature.py <feature_name>` (post-merge)
