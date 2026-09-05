# Archived prompt — DEVELOPER phase (05, navigator review)

You are a senior code reviewer (Navigator). Review the implementation below for
design patterns, performance, and null-safety. Do NOT change behavior or break tests.

IMPLEMENTATION:
src/main/java/com/example/withdraw/application/impl/WithdrawServiceImpl.java

INSTRUCTIONS:
1. Review for design patterns, performance, and null-safety. Verify Vavr 0.11.0 usage
   is idiomatic: `Option` over null checks, `Try`/`Either` over try/catch, immutable
   `io.vavr.collection.*` collections, and no `java.util.Optional` leaking in.
2. If issues are found, provide a refactored version (behavior-preserving).
3. If no issues, say so explicitly.

REVIEW OUTCOME:
No issues found. The implementation uses `Either` for every failure path, `Option` for
the account lookup (no `java.util.Optional`), `BigDecimal` for money, defensive null
checks, and constructor injection. Card ownership is checked after account lookup and
before the balance check, matching the requirement. Tests are green (14/14, BUILD
SUCCESS). No refactor applied.

OUTPUT & ARCHIVAL:
- Save this exact prompt to `.features/withdraw/prompts/05_developer_review_prompt.md`.
