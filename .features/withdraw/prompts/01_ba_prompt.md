# Archived prompt — BA phase (01)

You are a Business Analyst. Convert the raw feature description below into a
structured Requirement Document.

RAW FEATURE DESCRIPTION:
withdraw for the user by api
api need to be called with the following parameters:
- user_id: the unique identifier of the user requesting the withdrawal
- amount: the amount to be withdrawn
- card_number: the card number to which the amount will be withdrawn

need to validate the user_id and amount before making the api call
cannot withdraw if the user_id is invalid or if the amount is less than or equal to zero
card_number should be a valid card number format (e.g., 16 digits for most cards)

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
- Save the final Requirement Document to `.features/withdraw/final/requirement.md`.
- Save this exact prompt to `.features/withdraw/prompts/01_ba_prompt.md`.

---

AMENDMENT (human review, raised during DOMAIN_ARCHITECT review): the requirement was
updated to add a card-ownership rule — a well-formed `card_number` that does not belong
to the `user_id` is rejected with "card not owned" (`CARD_NOT_OWNED`) and no debit. A
matching acceptance criterion, edge case, and platform user story were added.
