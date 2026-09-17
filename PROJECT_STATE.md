# Carl — Project State

## Project
- Name: Carl
- Package: com.carl.editor
- Version: 0.1.0
- Current milestone: Phase 8 + Phase 9 step 1 (speed) DEVICE-CONFIRMED. Phase 9 step 2 (rotate/flip) and step 3 (canvas/aspect ratio) are implemented but UNVERIFIED — neither has been built or run yet.

## Progress
- Overall: Foundation, preview, timeline, trim/split/undo-redo, and per-clip speed are all built and confirmed working on the Tecno Spark 5. Rotate/flip (global) and canvas/aspect-ratio are freshly implemented on top of that and awaiting a CI build + device check.

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
  - Files: timeline/Clip.kt, timeline/EditState.kt, timeline/EditHistory.kt, timeline/TimelineControls.kt
  - Clips are a list of source in/out points; ExoPlayer plays them back-to-back as a playlist using MediaItem.ClippingConfiguration (no re-encoding, no FFmpeg) — preview-only, final export merging is a later phase
- Phase 9 step 1: Per-clip speed control (0.5x/1x/1.5x/2x preset buttons, applies to whichever clip the playhead is in)
  - Clip.kt has a `speed` field; `durationMs` means *timeline* duration (sourceDurationMs / speed), `sourceDurationMs` is the untouched source-space length
  - Applies speed via ExoPlayer.playbackParameters, re-applied on every clip transition and after every seek

## Implemented, NOT YET BUILT OR TESTED
- Phase 9 step 2: Whole-video rotate/flip (NOT per-clip — see decision below)
  - New: effects/GlobalTransform.kt (rotation + flipH/flipV → ScaleAndRotateTransformation), effects/GlobalTransformControls.kt (UI, explicitly labeled "whole video")
  - New dependency added: androidx.media3:media3-effect:1.4.1 (matches pinned exoplayer/ui version)
  - PreviewScreen calls exoPlayer.setVideoEffects(...) before setMediaItems()+prepare(), inside the same rebuild path used for clip-list changes (never hot-swapped on an already-prepared player, due to known Media3 stability issues with that pattern)
  - RISK FLAGGED TO USER: this is a new dependency + a less-battle-tested ExoPlayer API than anything used so far. Needs both a CI build (may fail to compile/resolve) and a device check (rotate 90°/270° changes the video's effective width/height — unconfirmed whether PlayerView resizes correctly).
- Phase 9 step 3: Canvas / aspect ratio (Original, 9:16, 1:1, 4:5, 16:9) + background color (black/white/dark gray)
  - New: canvas/CanvasSettings.kt, canvas/CanvasControls.kt
  - Pure Compose layout (Box + aspectRatio modifier + PlayerView RESIZE_MODE_FIT) — deliberately does NOT touch the video-effects pipeline above, so it carries none of that risk and can be verified independently
  - LOWER RISK than step 2, but still unverified on device

## Bug found and fixed during Phase 8/9 verification
- Time-code labels and the Undo/Play/Split/Redo button row were invisible on-device (likely default/unstyled text color resolving to black-on-black, and/or Unicode icon glyphs not supported by the device font). Trim handles existed but had no visual affordance suggesting they were draggable.
  - Fix: explicit `Color.White` on all text/icon elements instead of relying on theme defaults; replaced Unicode symbol icons with plain text labels ("Undo", "Play"/"Pause", "Split", "Redo"); added a visible grip mark inside each trim handle.
  - Confirmed fixed on device.

## Cleanup done
- timeline/TrimState.kt (superseded by EditState) was deleted from the repo via a custom Zapier GitHub code action (the built-in GitHub connector has no delete-file action, only delete-branch)

## Decision made: Phase 9 step 2 scope (rotate/flip/crop)
- ExoPlayer.setVideoEffects(List<Effect>) is the real, Google-documented way to preview visual transforms without exporting.
- LIMITATION: effects set this way apply to every item in the playlist, not per clip. True per-clip visual effects require CompositionPlayer, which is only an experimental API as of Media3 1.9.0 (Dec 2025) — not present at our pinned 1.4.1, and adopting it would mean a major, separately-researched version bump.
- There are also open Media3 GitHub issues about setVideoEffects() causing stuck playback when swapped dynamically on an already-prepared player.
- DECISION: implemented as a whole-project (global) transform for now, not per-clip. Stays on the stable, non-experimental 1.4.1 API. Revisit true per-clip visual effects later if/when CompositionPlayer matures out of experimental.

## Planned (next)
- Build via CI and verify Phase 9 steps 2 and 3 on the Tecno Spark 5 before adding anything further. Specifically check:
  - Does the app still build/run at all with the new media3-effect dependency?
  - Rotate 90°/270°: does the preview resize correctly (width/height swap)?
  - Flip H/V: does it visibly flip?
  - Aspect ratio presets: does the video actually letterbox/pillarbox onto the chosen ratio with the selected background color?
  - Do rotate/flip and canvas settings survive trim/split/undo/redo/speed changes without breaking playback?
- Phase 9 remaining after that: resize, reverse, freeze frame, opacity, position, scale, basic color adjustments

## Architecture / stack
- Kotlin + Jetpack Compose
- Media engine: Jetpack Media3 (ExoPlayer 1.4.1, media3-effect 1.4.1, playlist + ClippingConfiguration for multi-clip preview, PlaybackParameters for per-clip speed, setVideoEffects for whole-video rotate/flip)
- Build: AGP 8.2.0, Gradle 8.7 (wrapper generated at CI build time, not committed)
- CI: GitHub Actions only — no local Android Studio/laptop in the workflow

## Test device
- Tecno Spark 5, 2GB RAM, Android 10 (Go edition) — primary real-device test target

## Known bugs
- None currently open on confirmed features. Phase 9 steps 2/3 are unverified and may surface issues on first build/test.

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
- Keeping PROJECT_STATE.md name and Phase-numbering scheme (not renaming to CARL_PROJECT_STATE.md / M0-M13), per explicit instruction
- Staying on Media3 1.4.1 for now; NOT bumping to 1.9.0+ for CompositionPlayer without a dedicated decision
- Rotate/flip is global (whole-video), not per-clip, per the decision above
- Canvas/aspect-ratio is implemented as pure Compose layout, deliberately independent of the ExoPlayer video-effects pipeline

## Current next step
Build via CI (this changed build.gradle.kts — higher chance of a real build failure than previous steps) and test rotate/flip + canvas on the Tecno Spark 5. Report build result and on-device behavior before moving to the next Phase 9 item.
