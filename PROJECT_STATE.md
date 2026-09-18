# Carl — Project State

## Project
- Name: Carl
- Package: com.carl.editor
- Version: 0.1.0
- Current milestone: Build confirmed GREEN (screenshot evidence). Pivoting to a full editor UI/UX redesign per an uploaded "Ultimate IT & App-Building Master Prompt" while preserving all working functionality below. NOT a rewrite from scratch.

## Verification status (using VERIFIED / INSPECTED / EXPECTED / FAILED / BLOCKED)
- VERIFIED (device screenshot, 2026-09-18): app builds and runs; trim timeline with grip-marked handles renders; Undo/Pause/Split/Redo row renders in white text; speed presets 0.5x/1x/1.5x/2x render with 1x correctly highlighted green; Rotate/Flip H/Flip V row renders under "Rotate / Flip (whole video)" label; Canvas row renders with Original/9:16/1:1/4:5/16:9 and 3 background color swatches (black correctly shown selected); "Color (whole video)" section renders with Brightness/Contrast/Saturation sliders + Reset; default PlayerView controller overlay stays hidden (no rewind/forward/prev/next buttons visible).
- NOT YET VERIFIED (screenshot doesn't show this): whether rotate/flip/sliders/canvas actually change the preview output when interacted with (screenshot shows all at default/no-op values — rotate 0°, sliders at center). Whether these settings survive trim/split/undo/redo without breaking playback is also unconfirmed.

## Completed (device-confirmed functionality, from earlier verified rounds)
- Repo + Gradle setup, GitHub Actions CI, app module (Kotlin + Compose, minSdk 26, target/compileSdk 34)
- AndroidManifest.xml — modern per-type media permissions + legacy fallback
- MainActivity.kt → HomeScreen.kt (empty state + "New Project")
- Phase 5: media import (photo/video picker)
- Phase 6: real video preview via Media3 ExoPlayer 1.4.1
- Phase 7: basic timeline (seek, position/duration, play/pause)
- Phase 8: trim (drag either end), split (at playhead), undo/redo — timeline/Clip.kt, EditState.kt, EditHistory.kt, TimelineControls.kt
- Phase 9 step 1: per-clip speed (0.5x-2x presets) via ExoPlayer.playbackParameters
- Invisible-controls bug (theme-default text color / unsupported Unicode glyphs) — fixed, confirmed
- PlayerView default controller overlay — disabled (useController = false), confirmed hidden in screenshot

## Implemented, renders correctly (per screenshot), behavior not yet exercised
- Phase 9 step 2: whole-video rotate/flip — effects/GlobalTransform.kt + GlobalTransformControls.kt, via ExoPlayer.setVideoEffects (media3-effect 1.4.1 dependency)
- Phase 9 step 3: canvas/aspect ratio + background color — canvas/CanvasSettings.kt + CanvasControls.kt, pure Compose layout
- Phase 9 step 4: whole-video brightness/contrast/saturation — effects/ColorAdjustment.kt + ColorAdjustmentControls.kt, same setVideoEffects mechanism as step 2
- All three previously had a real, identified compile risk (missing @OptIn(UnstableApi) on Media3 effect classes) which was fixed — the successful build in the screenshot is consistent with that fix having worked, though no explicit CI log was shown

## New direction (as of this message): full UI/UX redesign
- User provided an extensive "Claude Ultimate IT & App-Building Master Prompt" (300+ point engineering/verification framework, saved to /preferences.md) plus a Carl-specific redesign brief.
- Redesign brief explicitly requires: keep all existing functionality, do NOT rewrite from scratch, inspect the full repo first, give a technical assessment + roadmap BEFORE writing code, then proceed one file at a time.
- Target end-state: professional editor layout (top bar: back/project name/undo/redo/export; center: large dominant preview; bottom: real multi-track timeline; contextual tool dock below instead of a long vertical settings list — the exact thing the current screenshot shows and the user dislikes).
- Full scope requested (phased, not all at once): editor layout, timeline architecture (thumbnails, multi-clip, waveform, zoom, snapping), per-clip contextual tools, contextual adjust/canvas/speed panels, audio, text, stickers/overlays, effects, filters, transitions, keyframes architecture, captions, real export (resolution/format/progress/cancel, verified playable output), project management/autosave, accessibility, performance auditing, error handling.
- Current architecture (global-only effects via ExoPlayer.setVideoEffects, no CompositionPlayer, no Transformer/export pipeline yet) is the biggest structural gap against this brief — most of Phase 9’s remaining scope (per-clip visual effects, real export, transitions) will eventually need either CompositionPlayer (experimental, needs a Media3 version bump) or Transformer for export. This needs to be surfaced clearly in the assessment/roadmap before implementation starts, not discovered mid-build.

## Cleanup done
- timeline/TrimState.kt deleted via a custom Zapier GitHub code action (no built-in delete-file action available)

## Key architecture decisions still in force
- No FFmpeg — Media3 only; minSdk 26; brand-new repo/package (com.carl.editor), not a continuation of the earlier Caleb Media project
- One coherent step at a time, full-file replacements, files committed directly to GitHub via Zapier
- Global (not per-clip) visual effects for now — per-clip requires CompositionPlayer (experimental at Media3 1.9.0+, we're pinned to 1.4.1) — revisit only as a dedicated decision
- All Media3 @UnstableApi usage requires @OptIn(UnstableApi::class) at the call site
- All interactive text/icon controls use explicit colors, never theme defaults; plain text labels preferred over Unicode symbol glyphs
- PlayerView's default controller overlay stays disabled — TimelineControls owns all transport UI
- Keeping PROJECT_STATE.md name and Phase-numbering (not renaming to CARL_PROJECT_STATE.md / M0-M13), per explicit earlier instruction

## Current next step
Produce the requested technical assessment + phased implementation roadmap for the redesign (no code yet, per the brief's explicit instruction), identifying the highest-priority foundational change to start with. Do not start writing files until that assessment is delivered and the user confirms direction.
