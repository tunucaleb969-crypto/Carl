# Carl — Continuation Guide

This is the durable entry point for future Carl development sessions.

## Source of truth
- Live repository: tunucaleb969-crypto/Carl
- Main branch: main
- Detailed project state: PROJECT_STATE.md
- Historical/current state companion: CARL_PROJECT_STATE.md
- Engineering research knowledge base: docs/CARL_ENGINEERING_RESEARCH.md

Do not rely on chat memory for project continuity.

## Current verified checkpoint
As of 2026-09-28:
- Commit 6129eb4c9b4f847e4cb627600f4121dea9c939a8 passed GitHub Actions run 178.
- The following test commit 958b02570987310324e9af6025299afc395e1985 is current main after the project-state test batch; its CI status must be checked before that batch is called verified.
- Timeline state/history, per-clip edit-state foundations, and Transformer export foundations are implemented.
- Real-device verification remains separate from CI.

## Required continuation workflow
1. Inspect live main.
2. Read this file plus PROJECT_STATE.md and CARL_PROJECT_STATE.md.
3. Read/update docs/CARL_ENGINEERING_RESEARCH.md before major platform/API decisions.
4. Work in coherent feature batches, not one-file drops.
5. Build/test the entire batch.
6. Fix failures before stacking more work.
7. Update project documentation.
8. Commit completed work.
9. Check CI/status.
10. Continue to the next high-value task without waiting unnecessarily.

## Current high-priority work
1. Confirm latest CI after the project-state test batch.
2. Complete and verify Android API 36/toolchain migration as a dedicated batch.
3. Continue project safety: versioned project format, asset identity, autosave/recovery, and URI persistence.
4. Continue timeline/render/export parity.
5. Add adaptive UI and accessibility coverage.
6. Expand into larger multi-track/effects/audio/text systems after core safety is stable.

## Engineering rules
- Never claim device testing without an actual device/emulator.
- Never expose a control without a real implementation.
- Never use preview-only behavior as fake export support.
- Preserve working architecture; extend it.
- Keep research and decisions in the repository.
- Never commit secrets.