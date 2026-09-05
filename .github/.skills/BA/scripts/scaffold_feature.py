#!/usr/bin/env python3
"""scaffold_feature.py — scaffold a new Spec-Code Agent feature.

Usage:
    python scaffold_feature.py <feature_name>

Creates the per-feature skeleton under .features/<feature_name>/:

    input/requirement_raw.txt   (empty placeholder)
    prompts/                    (empty; holds archived prompts)
    final/                      (empty; holds approved artifacts)
    STATE.md                    (initialized to DRAFT)

Java source + tests live in the ROOT Spring project under src/ and are NOT
created here. This script performs NO LLM calls — only filesystem operations.
"""
import os
import sys
from pathlib import Path

FEATURES_ROOT = Path(".features")


def main():
    if len(sys.argv) != 2:
        print("Usage: python scaffold_feature.py <feature_name>")
        sys.exit(1)

    feature = sys.argv[1].strip()
    if not feature:
        print("Error: feature_name must not be empty.")
        sys.exit(1)

    root = FEATURES_ROOT / feature
    dirs = [
        root / "input",
        root / "prompts",
        root / "final",
    ]

    for d in dirs:
        d.mkdir(parents=True, exist_ok=True)

    raw_txt = root / "input" / "requirement_raw.txt"
    if not raw_txt.exists():
        raw_txt.write_text("# Paste the raw feature description here.\n", encoding="utf-8")

    state_md = root / "STATE.md"
    if not state_md.exists():
        state_md.write_text(
            f"# Feature: {feature}\n\n"
            "## Phase approvals\n\n"
            "> Status: PENDING (not started) · IN_REVIEW (awaiting human approval) · APPROVED (human approved)\n\n"
            "- BA: PENDING\n"
            "- DOMAIN_ARCHITECT: PENDING\n"
            "- TEST_ARCHITECT: PENDING\n"
            "- CONTRACT_STEWARD: PENDING\n"
            "- DEVELOPER: PENDING\n"
            "- TECHLEAD: PENDING\n",
            encoding="utf-8",
        )

    print(f"Scaffolded feature: {root}")
    for d in dirs:
        print(f"  created {d}")
    print(f"  created {raw_txt}")
    print(f"  created {state_md}")


if __name__ == "__main__":
    main()
