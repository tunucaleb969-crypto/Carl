# Carl — Project State

## Project
- Name: Carl
- Package: com.carl.editor
- Version: 0.1.0
- Current milestone: Redesign Phases 1 (theme) + 2 (top bar) + 3 (contextual tool dock) IMPLEMENTED, all unverified — no CI build run since Phase 1 started. Full plan: 1) theme 2) nav shell/top bar 3) contextual tool dock 4) timeline v2 5+) audio/text/stickers/filters/transitions/captions/export/persistence.

## Verification status (VERIFIED / INSPECTED / EXPECTED / FAILED / BLOCKED)
- VERIFIED (device screenshot, 2026-09-18): pre-redesign app builds and runs; full trim/split/undo-redo/speed/rotate-flip/canvas/color UI renders correctly; default PlayerView controller overlay stays hidden.
- NOT YET VERIFIED: whether rotate/flip/sliders/canvas visually affect output when interacted with; whether they survive trim/split/undo/redo.
- EXPECTED, NOT BUILT: redesign Phases 1, 2, and 3 (theme, top bar, tool dock). All inspected carefully but NO CI build has run since Phase 1 started — three unverified changes are now stacked. This is a real, acknowledged risk: if the build fails, isolating which phase caused it is harder with three stacked than it would have been with one. A build result is needed before Phase 4.

## Redesign Phase 3: contextual tool dock (IMPLEMENTED, unverified)
- NEW: ToolTab.kt — enum of which panel is active (TRANSFORM/CANVAS/COLOR)
- NEW: ToolDock.kt — tab row + renders only the selected tab's panel below it; reuses GlobalTransformControls/CanvasControls/ColorAdjustmentControls UNCHANGED internally, only how/when they're shown has changed
- MODIFIED: PreviewScreen.kt — replaced the always-visible vertical stack of all three panels with a single ToolDock call; added `selectedTool` state
- This is the change that most directly addresses the original "looks like a settings screen" complaint — previously all three panels + timeline + speed were always visible at once; now only one tool panel shows at a time, tap again to collapse
- NOT YET BUILT OR TESTED

## Redesign Phase 2: navigation shell + top bar (IMPLEMENTED, unverified)
- NEW: EditorTopBar.kt — Back, project name (placeholder "Untitled Project"), Undo, Redo, Export (visibly disabled — no export pipeline exists yet, so per "no fake buttons" it's dimmed/non-clickable rather than wired to do nothing)
- MODIFIED: TimelineControls.kt — removed canUndo/canRedo/onUndo/onRedo (moved to top bar); row now only has Play/Pause + Split
- MODIFIED: PreviewScreen.kt, MainActivity.kt — onBack wiring
- REAL BUG FIXED (found during inspection): there was previously no way to leave the editor and return to Home once a video was picked. Fixed as a side effect of adding the top bar.

## Redesign Phase 1: real theme system (IMPLEMENTED, unverified)
- NEW: ui/theme/Theme.kt — `CarlTheme`, a real darkColorScheme (primary = accent 0xFF00E5A0)
- MODIFIED: MainActivity.kt — replaced bare `MaterialTheme { }` with `CarlTheme { }`
- HomeScreen.kt / PreviewScreen.kt still hardcode Color.Black/White directly rather than reading theme tokens — values already match, migration is separate follow-up cleanup

## Fresh repo inspection findings (this session, before redesign work started)
- No real theme system existed — root cause of the earlier invisible-controls bug. Fixed in Phase 1.
- No navigation library, and no way back out of the editor at all. Fixed in Phase 2.
- PreviewScreen.kt mixed video engine wiring, all edit state, and every tool panel stacked vertically and always visible. Fixed in Phase 3.
- Timeline is a single draggable bar with tick marks — no thumbnails/waveform/zoom/scroll/tracks. Phase 4, not started.
- No persistence: all edit state lives in Compose `remember`, lost on rotation/process death. No autosave/project list, no real project name.
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
3. Contextual tool-dock architecture — DONE (unverified)
4. Timeline v2 (thumbnails, zoom, scroll) — NEXT
5+. Audio, text, stickers, effects, filters, transitions, keyframes architecture, captions, real export (Transformer), project persistence/autosave, accessibility, performance audit, error handling
Biggest structural gap: no CompositionPlayer/Transformer yet — per-clip visual effects, real export, and transitions will need one or both; flagged to user, not yet decided.

## Cleanup done
- timeline/TrimState.kt deleted via a custom Zapier GitHub code action (no built-in delete-file action available)

## Key architecture decisions still in force
- No FFmpeg — Media3 only; minSdk 26; brand-new repo/package (com.carl.editor)
- Full-file replacements, files committed directly to GitHub via Zapier
- User has explicitly waived the "one file at a time" pacing rule from their own master prompt for this project (told to "continue with 20 files" and to disregard that line) — but the "no fake functionality / research before building" rules were NOT waived and still apply; files are batched only when they're genuinely one coherent, already-understood change, not padded with unresearched features
- Global (not per-clip) visual effects for now — per-clip requires CompositionPlayer (experimental at Media3 1.9.0+, pinned to 1.4.1)
- All Media3 @UnstableApi usage requires @OptIn(UnstableApi::class) at the call site
- All interactive text/icon controls use explicit colors; plain text labels preferred over Unicode symbol glyphs
- PlayerView's default controller overlay stays disabled
- Keeping PROJECT_STATE.md name and Phase-numbering (not CARL_PROJECT_STATE.md / M0-M13), per explicit instruction
- Visible-but-disabled is the pattern for not-yet-implemented controls (Export button), never a button wired to do nothing
- Undo/Redo live only in EditorTopBar now
- Contextual tool dock (ToolDock/ToolTab) shows one panel at a time, replacing the old always-visible stack

## Current next step
Get a CI build result covering Phases 1+2+3 together (theme, top bar, onBack wiring, TimelineControls signature change, ToolDock). This is the highest-priority next action — three unverified phases are now stacked. Once confirmed (or once a build error is reported so it can be fixed), move to Phase 4 (timeline v2: thumbnails/zoom/scroll), which needs its own research pass before implementation (thumbnail generation strategy, performance on the 2GB Tecno Spark 5).
