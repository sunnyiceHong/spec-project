---
description: Run the Spec-Code workflow for a feature one phase at a time (BA → DOMAIN_ARCHITECT → TEST_ARCHITECT → CONTRACT_STEWARD → DEVELOPER → TECHLEAD), pausing for human review after each phase
argument-hint: [feature-name]
context: default
agent: spec-code
---

Run one phase of the Spec-Code feature-development workflow for the feature named
`$ARGUMENTS`. Read the per-phase approval ledger in `.features/<feature>/STATE.md` to
determine which phase is next, execute exactly ONE phase (auto-implementing it), then
stop and ask the human to review and confirm before continuing.

To approve and continue, re-run `/spec-code <feature-name>` with the feature name
alone. To request changes, re-run `/spec-code <feature-name> <your feedback>`.
