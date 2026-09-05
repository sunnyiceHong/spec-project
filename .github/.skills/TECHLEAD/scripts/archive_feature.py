#!/usr/bin/env python3
"""archive_feature.py — archive a Spec-Code Agent feature's prompts and finals.

Usage:
    python archive_feature.py <feature_name>

Moves the current prompts/ and final/ folders into archive/ with a timestamped
suffix, so the feature can be re-developed without losing history:

    .features/<feature>/prompts/  -> .features/<feature>/archive/<ts>_prompts/
    .features/<feature>/final/    -> .features/<feature>/archive/<ts>_final/

Fresh empty prompts/ and final/ folders are then re-created for the next round.
This script performs NO LLM calls — only filesystem operations.
"""
import os
import sys
import time
from pathlib import Path

FEATURES_ROOT = Path(".features")


def main():
    if len(sys.argv) != 2:
        print("Usage: python archive_feature.py <feature_name>")
        sys.exit(1)

    feature = sys.argv[1].strip()
    root = FEATURES_ROOT / feature
    if not root.is_dir():
        print(f"Error: feature not found: {root}")
        sys.exit(1)

    ts = time.strftime("%Y%m%d_%H%M%S")
    archive_dir = root / "archive"
    archive_dir.mkdir(parents=True, exist_ok=True)

    for name in ("prompts", "final"):
        src = root / name
        dst = archive_dir / f"{ts}_{name}"
        if src.is_dir():
            os.rename(src, dst)
            print(f"  moved {src} -> {dst}")
            src.mkdir(parents=True, exist_ok=True)
        else:
            print(f"  skipped (missing): {src}")

    print(f"Archived feature: {root}")


if __name__ == "__main__":
    main()
