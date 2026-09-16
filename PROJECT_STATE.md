# Carl — Project State

## Project
- Name: Carl
- Package: com.carl.editor
- Version: 0.1.0
- Current milestone: Phase 8 — Real editing (trim, split, undo/redo) — built, NOT yet verified on device

## Progress
- Overall: Foundation + Home screen + preview + basic timeline complete and device-confirmed. Phase 8 code is in main but unverified.

## Completed
- Repo + Gradle setup (settings.gradle.kts, root build.gradle.kts, gradle.properties)
- GitHub Actions CI (.github/workflows/build.yml)
- App module (app/build.gradle.kts) — Kotlin + Jetpack Compose, minSdk 26, targetSdk/compileSdk 34
- AndroidManifest.xml — modern per-type media permissions + legacy fallback
- Basic dark theme (Theme.Carl)
- MainActivity.kt — launches HomeScreen
- HomeScreen.kt — empty-state UI + "New Project" button, confirmed on device
- Phase 5: Media import — photo/video picker triggered by "New Project"
- Phase 6: Real video preview via Media3 ExoPlayer (pinned to 1.4.1 for compileSdk 34 compat) — PreviewScreen.kt, confirmed on device
- Phase 7: Basic timeline — seek bar, position/duration display, play/pause (TimelineControls.kt), confirmed on device

## In progress / unverified
- Phase 8: Real editing — trim (drag either end of the timeline), split (cuts the clip under the playhead), undo/redo
  - New files: timeline/Clip.kt, timeline/EditState.kt, timeline/EditHistory.kt
  - Rewritten: PreviewScreen.kt, timeline/TimelineControls.kt
  - Approach: clips are represented as a list of source in/out points; ExoPlayer plays them back-to-back as a playlist using MediaItem.ClippingConfiguration (no re-encoding, no FFmpeg) — this is preview-only, final export merging is a later phase
  - timeline/TrimState.kt is now superseded/unused (replaced by EditState) — left in the repo since this workflow has no file-delete action; safe to delete manually whenever convenient
  - NOT YET BUILT OR RUN ON DEVICE — needs a CI build + on-device check before being marked confirmed

## Planned (next)
- Verify Phase 8 builds clean via CI and behaves correctly on the Tecno Spark 5
- Phase 9: Video tools (crop, rotate, flip, speed, etc.)

## Architecture / stack
- Kotlin + Jetpack Compose
- Media engine: Jetpack Media3 (ExoPlayer 1.4.1, playlist + ClippingConfiguration for multi-clip preview)
- Build: AGP 8.2.0, Gradle 8.7 (wrapper generated at CI build time, not committed)
- CI: GitHub Actions only — no local Android Studio/laptop in the workflow

## Test device
- Tecno Spark 5, 2GB RAM, Android 10 (Go edition) — primary real-device test target

## Known bugs
- None logged yet — Phase 8 hasn't been run yet, so it may surface issues (esp. drag responsiveness / clip-boundary UI on a low-RAM device)

## Decisions made (don't change without reason)
- Brand-new repo/package for Carl — not a continuation of the earlier Caleb Media (com.calebmedia) project
- No FFmpeg — Media3 only
- minSdk 26 (not the Media3 floor of 23)
- One coherent step at a time, full-file replacements only, no manual find/edit
- UI split by screen into separate composable files, MainActivity.kt stays thin
- Files committed directly to GitHub via the connected Zapier GitHub integration on this account, rather than pasted for manual copy-paste
- Trim/split use in/out points + Media3 clip playlists for preview; no re-encoding happens until an explicit export step (future phase)

## Current next step
Pull latest `main`, build via CI, install on the Tecno Spark 5, and check: does trim dragging feel responsive, does split at the playhead produce two independently seekable clips, do undo/redo work as expected. Report back any build errors or on-device issues.
