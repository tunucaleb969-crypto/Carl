# Carl — Project State

## Project
- Name: Carl
- Package: com.carl.editor
- Version: 0.1.0
- Current milestone: Redesign Phase 1 (real theme) IMPLEMENTED, unverified. Full plan: 1) theme 2) navigation shell/top bar 3) contextual tool dock 4) timeline v2 5+) audio/text/stickers/filters/transitions/captions/export/persistence.

## Verification status (VERIFIED / INSPECTED / EXPECTED / FAILED / BLOCKED)
- VERIFIED (device screenshot, 2026-09-18): app builds and runs; full trim/split/undo-redo/speed/rotate-flip/canvas/color UI renders correctly; default PlayerView controller overlay stays hidden.
- NOT YET VERIFIED: whether rotate/flip/sliders/canvas visually affect output when interacted with; whether they survive trim/split/undo/redo.
- EXPECTED, NOT BUILT: redesign Phase 1 (CarlTheme). Inspected carefully (small, additive, only touches MainActivity's theme wrapper + one new file) but no CI build run since this change.

## Fresh repo inspection findings (this session, before redesign work started)
- No real theme system existed: MainActivity.kt wrapped everything in a bare `MaterialTheme { }` (Compose's default LIGHT scheme). Every screen individually hardcoded `Surface(color = Color.Black)` + `Color.White` text to fake a dark UI. This mismatch is what caused the earlier invisible-controls bug (content color defaulting to the light scheme's dark-on-light default, rendering dark-on-black). PROJECT_STATE.md previously and incorrectly described this as "Basic dark theme (Theme.Carl)" — that file never existed until this session.
- No navigation library: MainActivity holds one raw `var videoUri` and does `if/else` between HomeScreen and PreviewScreen. No back stack, no project concept.
- PreviewScreen.kt (~380 lines) mixes video engine wiring, all edit state, and every tool panel (trim/speed/rotate/flip/canvas/color) stacked vertically and always visible — the direct cause of the "looks like a settings screen" complaint.
- Timeline is a single draggable bar with tick marks — no thumbnails/waveform/zoom/scroll/tracks.
- No persistence: all edit state lives in Compose `remember`, lost on rotation/process death. No autosave/project list.
- No export pipeline exists yet (preview-only via ClippingConfiguration + setVideoEffects).

## Redesign Phase 1: real theme system (IMPLEMENTED, unverified)
- NEW: ui/theme/Theme.kt — `CarlTheme` composable wrapping MaterialTheme with a real darkColorScheme (primary = existing accent 0xFF00E5A0, background/surface tuned dark, proper onX colors)
- MODIFIED: MainActivity.kt — replaced bare `MaterialTheme { }` with `CarlTheme { }`
- Deliberately minimal blast radius: HomeScreen.kt and PreviewScreen.kt still hardcode Color.Black/Color.White directly rather than reading MaterialTheme.colorScheme — those hardcoded values already match the new scheme, so nothing should visually break, but migrating those screens to read theme tokens is separate follow-up cleanup, not done yet.
- NOT YET BUILT OR TESTED.

## Completed (device-confirmed functionality, from earlier verified rounds)
- Repo + Gradle setup, GitHub Actions CI, app module (Kotlin + Compose, minSdk 26, target/compileSdk 34)
- AndroidManifest.xml — modern per-type media permissions + legacy fallback
- HomeScreen.kt (empty state + "New Project")
- Phase 5: media import (photo/video picker)
- Phase 6: real video preview via Media3 ExoPlayer 1.4.1
- Phase 7: basic timeline (seek, position/duration, play/pause)
- Phase 8: trim (drag either end), split (at playhead), undo/redo — timeline/Clip.kt, EditState.kt, EditHistory.kt, TimelineControls.kt
- Phase 9 step 1: per-clip speed (0.5x-2x presets) via ExoPlayer.playbackParameters
- Invisible-controls bug — fixed, confirmed (root cause now properly understood: see inspection findings above)
- PlayerView default controller overlay — disabled, confirmed hidden in screenshot

## Implemented, renders correctly (per screenshot), behavior not yet exercised
- Phase 9 step 2: whole-video rotate/flip — effects/GlobalTransform.kt + GlobalTransformControls.kt
- Phase 9 step 3: canvas/aspect ratio + background color — canvas/CanvasSettings.kt + CanvasControls.kt
- Phase 9 step 4: whole-video brightness/contrast/saturation — effects/ColorAdjustment.kt + ColorAdjustmentControls.kt

## Redesign scope (from uploaded brief) — full plan
1. Real theme (this step)
2. Navigation shell + top bar (back/project name/undo/redo/export placeholder)
3. Contextual tool-dock architecture (one panel at a time instead of the current always-visible stack)
4. Timeline v2 (thumbnails, zoom, scroll)
5+. Audio, text, stickers, effects, filters, transitions, keyframes architecture, captions, real export (Transformer), project persistence/autosave, accessibility, performance audit, error handling
Biggest structural gap: no CompositionPlayer/Transformer yet — per-clip visual effects, real export, and transitions will need one or both; flagged to user, not yet decided.

## Cleanup done
- timeline/TrimState.kt deleted via a custom Zapier GitHub code action (no built-in delete-file action available)

## Key architecture decisions still in force
- No FFmpeg — Media3 only; minSdk 26; brand-new repo/package (com.carl.editor)
- One coherent step at a time, full-file replacements, files committed directly to GitHub via Zapier
- Global (not per-clip) visual effects for now — per-clip requires CompositionPlayer (experimental at Media3 1.9.0+, pinned to 1.4.1)
- All Media3 @UnstableApi usage requires @OptIn(UnstableApi::class) at the call site
- All interactive text/icon controls use explicit colors; plain text labels preferred over Unicode symbol glyphs
- PlayerView's default controller overlay stays disabled
- Keeping PROJECT_STATE.md name and Phase-numbering (not CARL_PROJECT_STATE.md / M0-M13), per explicit instruction
- New: screens should migrate to MaterialTheme.colorScheme tokens over time instead of hardcoding Color.Black/White — not urgent, current hardcoded values still match

## Current next step
Get a CI build result for redesign Phase 1 (CarlTheme + MainActivity wiring). Should be a very low-risk build (additive theme file + one-line swap), but per this project's verification discipline, it's EXPECTED, not VERIFIED, until a build actually runs. Once confirmed, move to Phase 2 (navigation shell + top bar).
