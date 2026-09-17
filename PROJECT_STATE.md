# Carl — Project State

## Project
- Name: Carl
- Package: com.carl.editor
- Version: 0.1.0
- Current milestone: Phase 9 step 1 — Per-clip speed control — CI build GREEN, on-device behavior check still pending. Phase 8 in the same state (builds clean, not yet run on device).

## Progress
- Overall: Foundation + Home screen + preview + basic timeline complete and device-confirmed. Phase 8 (trim/split/undo-redo) and Phase 9 step 1 (speed) compile clean via CI as of the latest commit, but neither has been exercised on the Tecno Spark 5 yet — a green build proves the code is well-formed, not that the features behave correctly at runtime.

## Completed (device-confirmed)
- Repo + Gradle setup (settings.gradle.kts, root build.gradle.kts, gradle.properties)
- GitHub Actions CI (.github/workflows/build.yml)
- App module (app/build.gradle.kts) — Kotlin + Jetpack Compose, minSdk 26, targetSdk/compileSdk 34
- AndroidManifest.xml — modern per-type media permissions + legacy fallback
- Basic dark theme (Theme.Carl)
- MainActivity.kt — launches HomeScreen
- HomeScreen.kt — empty-state UI + "New Project" button
- Phase 5: Media import — photo/video picker triggered by "New Project"
- Phase 6: Real video preview via Media3 ExoPlayer (pinned to 1.4.1 for compileSdk 34 compat) — PreviewScreen.kt
- Phase 7: Basic timeline — seek bar, position/duration display, play/pause (TimelineControls.kt)

## Builds clean (CI green) / on-device behavior unverified
- Phase 8: Real editing — trim (drag either end of the timeline), split (cuts the clip under the playhead), undo/redo
  - Files: timeline/Clip.kt, timeline/EditState.kt, timeline/EditHistory.kt, PreviewScreen.kt, timeline/TimelineControls.kt
  - Approach: clips are a list of source in/out points; ExoPlayer plays them back-to-back as a playlist using MediaItem.ClippingConfiguration (no re-encoding, no FFmpeg) — preview-only, final export merging is a later phase
- Phase 9 step 1: Per-clip speed control (0.5x/1x/1.5x/2x preset buttons, applies to whichever clip the playhead is in)
  - Clip.kt now has a `speed` field; `durationMs` means *timeline* duration (sourceDurationMs / speed), while `sourceDurationMs` is the untouched source-space length
  - EditState.splitAt, clipIndexAt, clipStartOnTimeline all operate in timeline-space and convert to source-space via each clip's speed where needed
  - PreviewScreen applies speed via ExoPlayer.playbackParameters, re-applied on every clip transition (each clip can have a different speed) and after every seek
  - CI BUILD IS GREEN. Still needs an on-device pass: trim feel, split correctness, undo/redo, and speed transitions haven't actually been exercised yet.

## Cleanup done
- timeline/TrimState.kt (superseded by EditState) was deleted from the repo via a custom Zapier GitHub code action (the built-in GitHub connector has no delete-file action, only delete-branch)

## Planned (next)
- Install the latest green build on the Tecno Spark 5 and verify Phase 8 + Phase 9 step 1 behavior (they share the same files, so one test pass covers both)
- Phase 9 remaining: crop, rotate, flip, resize, reverse, freeze frame, opacity, position, scale, aspect ratios, canvas/background, basic color adjustments

## Architecture / stack
- Kotlin + Jetpack Compose
- Media engine: Jetpack Media3 (ExoPlayer 1.4.1, playlist + ClippingConfiguration for multi-clip preview, PlaybackParameters for per-clip speed)
- Build: AGP 8.2.0, Gradle 8.7 (wrapper generated at CI build time, not committed)
- CI: GitHub Actions only — no local Android Studio/laptop in the workflow

## Test device
- Tecno Spark 5, 2GB RAM, Android 10 (Go edition) — primary real-device test target

## Known bugs
- None logged yet — Phase 8 and Phase 9 step 1 build clean but haven't been run on device, so testing may still surface issues (esp. drag responsiveness / clip-boundary UI / speed-transition smoothness on a low-RAM device)

## Decisions made (don't change without reason)
- Brand-new repo/package for Carl — not a continuation of the earlier Caleb Media (com.calebmedia) project
- No FFmpeg — Media3 only
- minSdk 26 (not the Media3 floor of 23)
- One coherent step at a time, full-file replacements only, no manual find/edit
- UI split by screen into separate composable files, MainActivity.kt stays thin
- Files committed directly to GitHub via the connected Zapier GitHub integration on this account, rather than pasted for manual copy-paste
- Trim/split/speed use in/out points + per-clip speed multipliers + Media3 clip playlists for preview; no re-encoding happens until an explicit export step (future phase)
- Undo/redo (EditHistory) treats speed changes as full history-tracked edits, same as trim/split
- Keeping PROJECT_STATE.md name and Phase-numbering scheme (not renaming to CARL_PROJECT_STATE.md / M0-M13, per explicit instruction, despite an uploaded expanded master prompt suggesting that scheme)

## Current next step
Install the latest build (CI green) on the Tecno Spark 5 and check: does trim dragging feel responsive, does split at the playhead produce two independently seekable clips, do undo/redo work as expected, does changing a clip's speed preset actually speed up/slow down preview playback at the right point with position/duration numbers staying accurate. Report back any on-device issues (a green CI build only proves it compiles, not that it behaves correctly).
