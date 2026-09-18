# Carl — Project State

## Project
- Name: Carl
- Package: com.carl.editor
- Version: 0.1.0
- Current milestone: Redesign Phases 1 (theme) + 2 (navigation shell/top bar) IMPLEMENTED, unverified (no CI build run since Phase 1). Full plan: 1) theme 2) nav shell/top bar 3) contextual tool dock 4) timeline v2 5+) audio/text/stickers/filters/transitions/captions/export/persistence.

## Verification status (VERIFIED / INSPECTED / EXPECTED / FAILED / BLOCKED)
- VERIFIED (device screenshot, 2026-09-18): pre-redesign app builds and runs; full trim/split/undo-redo/speed/rotate-flip/canvas/color UI renders correctly; default PlayerView controller overlay stays hidden.
- NOT YET VERIFIED: whether rotate/flip/sliders/canvas visually affect output when interacted with; whether they survive trim/split/undo/redo.
- EXPECTED, NOT BUILT: redesign Phase 1 (CarlTheme) and Phase 2 (EditorTopBar + onBack wiring). Both inspected carefully but no CI build has run since Phase 1 started, so this is now two unverified changes stacked - next CI build result covers both.

## Redesign Phase 2: navigation shell + top bar (IMPLEMENTED, unverified)
- NEW: EditorTopBar.kt — Back, project name (placeholder "Untitled Project" — no naming/persistence exists yet), Undo, Redo, Export (visibly disabled — no export pipeline exists, so per the "no fake buttons" rule it's rendered dimmed/non-clickable rather than wired to do nothing)
- MODIFIED: TimelineControls.kt — removed canUndo/canRedo/onUndo/onRedo (moved to top bar, avoids duplicate controls); row now only has Play/Pause + Split
- MODIFIED: PreviewScreen.kt — added `onBack` param, renders EditorTopBar first, updated TimelineControls call to match its new (smaller) parameter list
- MODIFIED: MainActivity.kt — passes `onBack = { videoUri = null }` to PreviewScreen
- REAL BUG FIXED BY THIS CHANGE (found during inspection, not previously logged): there was previously NO way to leave the editor and return to Home once a video was picked — no back button existed anywhere. This is now fixed as a side effect of adding the top bar, not just cosmetic.
- NOT YET BUILT OR TESTED.

## Redesign Phase 1: real theme system (IMPLEMENTED, unverified)
- NEW: ui/theme/Theme.kt — `CarlTheme` composable wrapping MaterialTheme with a real darkColorScheme (primary = existing accent 0xFF00E5A0, background/surface tuned dark, proper onX colors)
- MODIFIED: MainActivity.kt — replaced bare `MaterialTheme { }` with `CarlTheme { }`
- HomeScreen.kt and PreviewScreen.kt still hardcode Color.Black/Color.White directly rather than reading MaterialTheme.colorScheme — those hardcoded values already match the new scheme, migrating those screens to theme tokens is separate follow-up cleanup, not done yet.

## Fresh repo inspection findings (this session, before redesign work started)
- No real theme system existed — root cause of the earlier invisible-controls bug, now properly understood (see Phase 1 above).
- No navigation library, and (newly found) no way back out of the editor at all — fixed in Phase 2 above.
- PreviewScreen.kt mixes video engine wiring, all edit state, and every tool panel stacked vertically and always visible — the direct cause of the "looks like a settings screen" complaint. NOT yet fixed — that's Phase 3 (contextual tool dock).
- Timeline is a single draggable bar with tick marks — no thumbnails/waveform/zoom/scroll/tracks. Phase 4.
- No persistence: all edit state lives in Compose `remember`, lost on rotation/process death. No autosave/project list, no real project name (top bar currently shows a placeholder).
- No export pipeline exists yet (preview-only via ClippingConfiguration + setVideoEffects).

## Completed (device-confirmed functionality, from earlier verified rounds, pre-redesign)
- Repo + Gradle setup, GitHub Actions CI, app module (Kotlin + Compose, minSdk 26, target/compileSdk 34)
- AndroidManifest.xml — modern per-type media permissions + legacy fallback
- Phase 5: media import (photo/video picker)
- Phase 6: real video preview via Media3 ExoPlayer 1.4.1
- Phase 7: basic timeline (seek, position/duration, play/pause)
- Phase 8: trim (drag either end), split (at playhead), undo/redo — timeline/Clip.kt, EditState.kt, EditHistory.kt, TimelineControls.kt
- Phase 9 step 1: per-clip speed (0.5x-2x presets) via ExoPlayer.playbackParameters
- Invisible-controls bug — fixed, confirmed
- PlayerView default controller overlay — disabled, confirmed hidden in screenshot

## Implemented, renders correctly (per screenshot), behavior not yet exercised
- Phase 9 step 2: whole-video rotate/flip — effects/GlobalTransform.kt + GlobalTransformControls.kt
- Phase 9 step 3: canvas/aspect ratio + background color — canvas/CanvasSettings.kt + CanvasControls.kt
- Phase 9 step 4: whole-video brightness/contrast/saturation — effects/ColorAdjustment.kt + ColorAdjustmentControls.kt

## Redesign scope (from uploaded brief) — full plan
1. Real theme — DONE (unverified)
2. Navigation shell + top bar — DONE (unverified)
3. Contextual tool-dock architecture (one panel at a time instead of the current always-visible stack) — NEXT
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
- Screens should migrate to MaterialTheme.colorScheme tokens over time instead of hardcoding Color.Black/White — not urgent
- Visible-but-disabled is the pattern for not-yet-implemented controls (Export button), never a button wired to do nothing
- Undo/Redo live only in EditorTopBar now, not duplicated in TimelineControls

## Current next step
Get a CI build result covering Phase 1 + Phase 2 together (theme + top bar + onBack wiring + TimelineControls signature change). This is a more meaningful build than Phase 1 alone — it touches 4 files including a removed-parameter change that must compile cleanly across all call sites. Once confirmed, move to Phase 3 (contextual tool dock — the change that most directly addresses the original "looks like a settings screen" complaint).
