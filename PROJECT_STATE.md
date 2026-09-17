# Carl — Project State

## Project
- Name: Carl
- Package: com.carl.editor
- Version: 0.1.0
- Current milestone: Phase 8 + Phase 9 step 1 (speed) DEVICE-CONFIRMED. Steps 2 (rotate/flip), 3 (canvas), 4 (color adjust) implemented, UNVERIFIED. A likely compile-breaking bug from step 2 (missing @OptIn(UnstableApi)) was found and fixed before any build was confirmed either way.

## Progress
- Overall: Foundation, preview, timeline, trim/split/undo-redo, and per-clip speed are all built and confirmed working on the Tecno Spark 5. Rotate/flip, canvas/aspect-ratio, and color adjustment (brightness/contrast/saturation) are freshly implemented on top of that and awaiting a first successful CI build + device check.

## Completed (device-confirmed)
- Repo + Gradle setup (settings.gradle.kts, root build.gradle.kts, gradle.properties)
- GitHub Actions CI (.github/workflows/build.yml)
- App module (app/build.gradle.kts) — Kotlin + Jetpack Compose, minSdk 26, targetSdk/compileSdk 34
- AndroidManifest.xml — modern per-type media permissions + legacy fallback
- Basic dark theme (Theme.Carl)
- MainActivity.kt — launches HomeScreen
- HomeScreen.kt — empty-state UI + "New Project" button
- Phase 5: Media import — photo/video picker triggered by "New Project"
- Phase 6: Real video preview via Media3 ExoPlayer (pinned to 1.4.1 for compileSdk 34 compat) — PreviewScreen.kt. PlayerView's default playback-controls overlay (rewind/forward/prev/next) has been disabled (useController = false) since it duplicated our custom TimelineControls and looked like a generic media player rather than an editor. This fix is bundled with unverified work below, so it isn't separately confirmed on device yet either.
- Phase 7: Basic timeline — seek bar, position/duration display, play/pause (TimelineControls.kt)
- Phase 8: Real editing — trim (drag either end of the timeline), split (cuts the clip under the playhead), undo/redo
  - Files: timeline/Clip.kt, timeline/EditState.kt, timeline/EditHistory.kt, timeline/TimelineControls.kt
  - Clips are a list of source in/out points; ExoPlayer plays them back-to-back as a playlist using MediaItem.ClippingConfiguration (no re-encoding, no FFmpeg) — preview-only, final export merging is a later phase
- Phase 9 step 1: Per-clip speed control (0.5x/1x/1.5x/2x preset buttons, applies to whichever clip the playhead is in)
  - Clip.kt has a `speed` field; `durationMs` means *timeline* duration (sourceDurationMs / speed), `sourceDurationMs` is the untouched source-space length
  - Applies speed via ExoPlayer.playbackParameters, re-applied on every clip transition and after every seek

## Implemented, NOT YET BUILT OR TESTED (a first build has not yet been confirmed green)
- Phase 9 step 2: Whole-video rotate/flip (NOT per-clip — see decision below)
  - effects/GlobalTransform.kt (rotation + flipH/flipV → ScaleAndRotateTransformation), effects/GlobalTransformControls.kt (UI, labeled "whole video")
  - New dependency: androidx.media3:media3-effect:1.4.1 (matches pinned exoplayer/ui version)
- Phase 9 step 3: Canvas / aspect ratio (Original, 9:16, 1:1, 4:5, 16:9) + background color (black/white/dark gray)
  - canvas/CanvasSettings.kt, canvas/CanvasControls.kt — pure Compose layout, does not touch the video-effects pipeline, lower risk than steps 2/4
- Phase 9 step 4: Whole-video color adjustment — brightness/contrast/saturation sliders
  - effects/ColorAdjustment.kt (Brightness/Contrast/HslAdjustment), effects/ColorAdjustmentControls.kt (3 sliders + Reset, labeled "whole video")
  - Same scope limitation as step 2 (global, not per-clip) and same underlying setVideoEffects() mechanism
- PreviewScreen.kt: calls exoPlayer.setVideoEffects(globalTransform.toEffects() + colorAdjustment.toEffects()) before setMediaItems()+prepare(), inside the same rebuild path used for clip-list changes (never hot-swapped on an already-prepared player, per known Media3 stability issues with that pattern)

## Bug found and fixed BEFORE any build confirmation (important - this shipped in the same commit as rotate/flip, unverified)
- Media3's effect classes (ScaleAndRotateTransformation, Brightness, Contrast, HslAdjustment, etc.) are all marked @UnstableApi. In Kotlin this is enforced by `@RequiresOptIn(level = ERROR)` — a genuine **compiler error**, not just a lint warning — at every usage site lacking `@OptIn(UnstableApi::class)`.
- The original rotate/flip commit (GlobalTransform.kt, and PreviewScreen.kt's call to setVideoEffects) did not have this annotation and was therefore very likely failing to compile, regardless of any build result reported around that time.
- Fix: added `@OptIn(UnstableApi::class)` to GlobalTransform.toEffects(), ColorAdjustment.toEffects(), and the PreviewScreen composable itself (covers the setVideoEffects call site).
- STILL NOT CONFIRMED FIXED — needs a CI build to verify.

## Bug found and fixed during Phase 8/9 verification (this one IS device-confirmed)
- Time-code labels and the Undo/Play/Split/Redo button row were invisible on-device (likely default/unstyled text color resolving to black-on-black, and/or Unicode icon glyphs not supported by the device font). Trim handles existed but had no visual affordance suggesting they were draggable.
  - Fix: explicit `Color.White` on all text/icon elements instead of relying on theme defaults; replaced Unicode symbol icons with plain text labels ("Undo", "Play"/"Pause", "Split", "Redo"); added a visible grip mark inside each trim handle.
  - Confirmed fixed on device.

## Cleanup done
- timeline/TrimState.kt (superseded by EditState) was deleted from the repo via a custom Zapier GitHub code action (the built-in GitHub connector has no delete-file action, only delete-branch)

## Decision made: Phase 9 step 2/4 scope (rotate/flip/color — global, not per-clip)
- ExoPlayer.setVideoEffects(List<Effect>) is the real, Google-documented way to preview visual transforms without exporting.
- LIMITATION: effects set this way apply to every item in the playlist, not per clip. True per-clip visual effects require CompositionPlayer, which is only an experimental API as of Media3 1.9.0 (Dec 2025) — not present at our pinned 1.4.1, and adopting it would mean a major, separately-researched version bump.
- There are also open Media3 GitHub issues about setVideoEffects() causing stuck playback when swapped dynamically on an already-prepared player.
- DECISION: implemented rotate/flip and color adjustment as whole-project (global) effects for now, not per-clip. Stays on the stable, non-experimental 1.4.1 API. Revisit true per-clip visual effects later if/when CompositionPlayer matures out of experimental.

## Planned (next)
- Get a first CI build result for everything in the "NOT YET BUILT OR TESTED" section above. This is the highest-priority next step — there is a real, specific reason (missing @OptIn) to suspect earlier attempts failed to compile.
- If green, verify on the Tecno Spark 5:
  - Rotate 90°/270°: does the preview resize correctly (width/height swap)?
  - Flip H/V: does it visibly flip?
  - Aspect ratio presets: does the video letterbox/pillarbox correctly with the selected background color?
  - Brightness/contrast/saturation sliders: do they visibly affect the preview in real time?
  - Does the default PlayerView controller overlay stay gone?
  - Do all of the above survive trim/split/undo/redo/speed changes without breaking playback?
- Phase 9 remaining after that: resize, reverse, freeze frame, opacity, position, scale

## Architecture / stack
- Kotlin + Jetpack Compose
- Media engine: Jetpack Media3 (ExoPlayer 1.4.1, media3-effect 1.4.1, playlist + ClippingConfiguration for multi-clip preview, PlaybackParameters for per-clip speed, setVideoEffects for whole-video rotate/flip/color)
- Build: AGP 8.2.0, Gradle 8.7 (wrapper generated at CI build time, not committed)
- CI: GitHub Actions only — no local Android Studio/laptop in the workflow

## Test device
- Tecno Spark 5, 2GB RAM, Android 10 (Go edition) — primary real-device test target

## Known bugs
- None currently open on confirmed features. Everything in "NOT YET BUILT OR TESTED" above is unverified and a first build has not been confirmed since the @OptIn fix.

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
- Any usage of Media3's @UnstableApi-marked classes (most of the effects package) must have @OptIn(UnstableApi::class) at the call site — root cause of the compile-risk bug above
- PlayerView's default controller overlay is disabled (useController = false) — we own all transport controls via TimelineControls
- Keeping PROJECT_STATE.md name and Phase-numbering scheme (not renaming to CARL_PROJECT_STATE.md / M0-M13), per explicit instruction
- Staying on Media3 1.4.1 for now; NOT bumping to 1.9.0+ for CompositionPlayer without a dedicated decision
- Rotate/flip and color adjustment are global (whole-video), not per-clip, per the decision above
- Canvas/aspect-ratio is implemented as pure Compose layout, deliberately independent of the ExoPlayer video-effects pipeline

## Current next step
Get a CI build result. This is more urgent than usual: there's a specific, identified reason (missing @OptIn(UnstableApi)) to think earlier build attempts around Phase 9 step 2 may have failed to compile, now fixed but unconfirmed. Report build result, then test on the Tecno Spark 5 per the checklist above before moving to the next Phase 9 item.
