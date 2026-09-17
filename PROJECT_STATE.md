# Carl — Project State

## Project
- Name: Carl
- Package: com.carl.editor
- Version: 0.1.0
- Current milestone: Phase 8 (trim/split/undo-redo) + Phase 9 step 1 (speed) — DEVICE-CONFIRMED. Deciding scope for Phase 9 step 2 (rotate/flip/crop).

## Progress
- Overall: Foundation, preview, timeline, trim/split/undo-redo, and per-clip speed are all built and confirmed working on the Tecno Spark 5.

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
- Phase 8: Real editing — trim (drag either end of the timeline), split (cuts the clip under the playhead), undo/redo
  - Files: timeline/Clip.kt, timeline/EditState.kt, timeline/EditHistory.kt, PreviewScreen.kt, timeline/TimelineControls.kt
  - Clips are a list of source in/out points; ExoPlayer plays them back-to-back as a playlist using MediaItem.ClippingConfiguration (no re-encoding, no FFmpeg) — preview-only, final export merging is a later phase
- Phase 9 step 1: Per-clip speed control (0.5x/1x/1.5x/2x preset buttons, applies to whichever clip the playhead is in)
  - Clip.kt has a `speed` field; `durationMs` means *timeline* duration (sourceDurationMs / speed), `sourceDurationMs` is the untouched source-space length
  - PreviewScreen applies speed via ExoPlayer.playbackParameters, re-applied on every clip transition and after every seek

## Bug found and fixed during Phase 8/9 verification
- Time-code labels and the Undo/Play/Split/Redo button row were invisible on-device (likely default/unstyled text color resolving to black-on-black, and/or Unicode icon glyphs ✘ ↶ ↷ ⏸ not supported by the device font). Trim handles existed but had no visual affordance suggesting they were draggable.
  - Fix: explicit `Color.White` on all text/icon elements instead of relying on theme defaults; replaced Unicode symbol icons with plain text labels ("Undo", "Play"/"Pause", "Split", "Redo") which render reliably on any font; added a visible grip mark inside each trim handle.
  - Confirmed fixed on device.

## Cleanup done
- timeline/TrimState.kt (superseded by EditState) was deleted from the repo via a custom Zapier GitHub code action (the built-in GitHub connector has no delete-file action, only delete-branch)

## Researched, decision pending: Phase 9 step 2 (rotate / flip / crop)
- ExoPlayer.setVideoEffects(List<Effect>) is the real, Google-documented way to preview visual transforms (rotate, crop, scale, etc.) without exporting — confirmed via official Android developer docs.
- IMPORTANT LIMITATION: per Google's own CompositionPlayer docs, effects set via ExoPlayer.setVideoEffects() apply to **every item in the playlist**, not per clip. True per-clip visual effects (different rotation per clip, etc.) require CompositionPlayer instead.
- CompositionPlayer was only introduced as an **experimental API in Media3 1.9.0** (Dec 2025) — it does not exist in our pinned 1.4.1. Adopting it means a major Media3 version bump (1.4.1 → 1.9.0+), which is a significant, separately-researched decision (compileSdk/API compatibility, ExoPlayer API surface changes, an experimental/unstable API) — not something to bundle into a small feature step.
- There are also open Media3 GitHub issues reporting preview getting "stuck" when setVideoEffects() is called dynamically (i.e. changing effects on an already-prepared player) — safer pattern is likely to rebuild/re-prepare the player when effects change, similar to how the playlist is already rebuilt for trim/split.
- RECOMMENDATION (given this project's Correctness/Stability priority): implement rotate/flip/crop as a **whole-project (global) transform for now, not per-clip** — stays on the stable, non-experimental API, avoids a risky dependency bump. Revisit true per-clip visual effects later once CompositionPlayer matures out of experimental.
- NOT YET IMPLEMENTED — waiting for go-ahead on the global-vs-per-clip scope tradeoff above before writing code.

## Planned (next)
- Decide: global-only rotate/flip/crop now (recommended) vs. bigger Media3 upgrade for per-clip effects later
- Phase 9 remaining after that: resize, reverse, freeze frame, opacity, position, scale, aspect ratios, canvas/background, basic color adjustments

## Architecture / stack
- Kotlin + Jetpack Compose
- Media engine: Jetpack Media3 (ExoPlayer 1.4.1, playlist + ClippingConfiguration for multi-clip preview, PlaybackParameters for per-clip speed)
- Build: AGP 8.2.0, Gradle 8.7 (wrapper generated at CI build time, not committed)
- CI: GitHub Actions only — no local Android Studio/laptop in the workflow

## Test device
- Tecno Spark 5, 2GB RAM, Android 10 (Go edition) — primary real-device test target

## Known bugs
- None currently open. (Invisible-controls bug above was found and fixed in this same milestone.)

## Decisions made (don't change without reason)
- Brand-new repo/package for Carl — not a continuation of the earlier Caleb Media (com.calebmedia) project
- No FFmpeg — Media3 only
- minSdk 26 (not the Media3 floor of 23)
- One coherent step at a time, full-file replacements only, no manual find/edit
- UI split by screen into separate composable files, MainActivity.kt stays thin
- Files committed directly to GitHub via the connected Zapier GitHub integration on this account, rather than pasted for manual copy-paste
- Trim/split/speed use in/out points + per-clip speed multipliers + Media3 clip playlists for preview; no re-encoding happens until an explicit export step (future phase)
- Undo/redo (EditHistory) treats speed changes as full history-tracked edits, same as trim/split
- All interactive text/icon controls must use explicit colors, never rely on theme defaults (root cause of the invisible-controls bug)
- Prefer plain text labels over Unicode symbol icons for controls, for cross-device font reliability, until a proper vector icon set is deliberately added
- Keeping PROJECT_STATE.md name and Phase-numbering scheme (not renaming to CARL_PROJECT_STATE.md / M0-M13), per explicit instruction, despite an uploaded expanded master prompt suggesting that scheme
- Staying on Media3 1.4.1 for now; NOT bumping to 1.9.0+ for CompositionPlayer without a dedicated decision, since that API is still experimental there

## Current next step
Get a decision on the rotate/flip/crop scope tradeoff above, then implement Phase 9 step 2 accordingly (global transform via ExoPlayer.setVideoEffects(), rebuilding the player when effects change).
