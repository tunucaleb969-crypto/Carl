# Carl — Project State

## Project
- Name: Carl
- Package: com.carl.editor
- Version: 0.1.0
- Current milestone: Visual polish pass IMPLEMENTED (icons + surface depth), unverified. Studied how professional mobile editors (CapCut and similar) achieve their look: icon-first controls, pill-styled primary actions, layered surface backgrounds — applied with Carl's own accent color, not a copied look. Next: get this built/tested before Phase 4.

## Verification status (VERIFIED / INSPECTED / EXPECTED / FAILED / BLOCKED)
- VERIFIED (user confirmation, 2026-09-18): Phases 1+2+3 (theme, top bar, contextual tool dock) build and run on device.
- VERIFIED (device screenshot, earlier): pre-redesign trim/split/undo-redo/speed/rotate-flip/canvas/color all render and the default PlayerView overlay stays hidden.
- EXPECTED, NOT BUILT: visual polish pass (icons + surface backgrounds, this round). Reasoned from a self-audit + research into professional editor UI conventions, but not yet compiled or run.
- NOT YET VERIFIED: whether rotate/flip/sliders/canvas visually affect output when interacted with; whether they survive trim/split/undo/redo.

## Visual polish pass (IMPLEMENTED, unverified) — "how did CapCut do it"
Researched actual professional mobile editor UI patterns before changing anything (not copying CapCut's exact look — applying the same underlying interaction pattern with Carl's own accent green, per the "own identity" rule): icon-first controls instead of text links, a pill-shaped primary action for Export, icon+label bottom-dock tabs with an accent highlight on the active tab, and layered surface backgrounds separating chrome from the video canvas.
- EditorTopBar.kt: Back/Undo/Redo are now icon buttons (Icons.AutoMirrored.Filled.ArrowBack, Icons.Filled.Undo/Redo); Export is a pill-shaped disabled button (icon + label) instead of plain disabled text; bar now has a surface (0xFF121212) background instead of blending into the black canvas
- TimelineControls.kt: Play/Pause and Split are now icon buttons (PlayArrow/Pause/ContentCut); Column now has a surface background
- ToolTab.kt: shortened "Rotate/Flip" label to "Transform" to fit under an icon
- ToolDock.kt: tabs are now icon-over-label (RotateRight/AspectRatio/Tune icons), active tab gets a rounded accent-tinted highlight background instead of just colored text; dock has a surface background
- GlobalTransformControls.kt: Rotate/Flip H/Flip V buttons now show an icon (RotateRight/Flip) alongside their text
- Icon names verified against real, long-established Material icon identifiers before use (rotate_right, aspect_ratio, content_cut, tune, undo, redo, file_upload all confirmed to exist) — avoiding a repeat of the earlier @OptIn-style "looked right, didn't compile" mistake
- material-icons-extended dependency (present since early in the project, previously unused) is now actually used
- NOT YET BUILT OR TESTED

## Self-audit findings from before this pass (now addressed above, pending verification)
- No icons anywhere — addressed above
- Buttons had no visual weight (flat text, no chip/background) — addressed via pill Export + accent-highlighted active tool tab
- No spacing/elevation hierarchy between sections — partially addressed via surface backgrounds on top bar / timeline / tool dock (video canvas stays pure black, chrome is now a visibly distinct dark gray)
- STILL NOT ADDRESSED: Timeline still reads as a thin slider bar, not a real timeline (Phase 4, thumbnails/waveform/zoom/scroll — bigger, separate effort). Typography still Material3 defaults.

## Redesign Phase 3: contextual tool dock (VERIFIED building/running; now also has icons per above)
- ToolTab.kt, ToolDock.kt, PreviewScreen.kt — architecture confirmed correct

## Redesign Phase 2: navigation shell + top bar (VERIFIED building/running; now also has icons per above)
- EditorTopBar.kt, TimelineControls.kt (Undo/Redo removed), onBack wiring in PreviewScreen.kt/MainActivity.kt
- Real bug fixed: previously no way to leave the editor at all

## Redesign Phase 1: real theme system (VERIFIED building/running)
- ui/theme/Theme.kt (CarlTheme, real darkColorScheme), wired into MainActivity.kt
- HomeScreen.kt/PreviewScreen.kt still hardcode Color.Black/White rather than reading theme tokens — unchanged, not urgent

## Fresh repo inspection findings (earlier this session)
- No real theme system existed — fixed (Phase 1)
- No way back out of the editor — fixed (Phase 2)
- Every tool panel always visible at once — fixed (Phase 3)
- No icons anywhere, flat text-only controls — fixed (visual polish pass above), pending verification
- Timeline is a single bar, no thumbnails/waveform/zoom/scroll/tracks — Phase 4, not started
- No persistence, no real project name, no export pipeline — later phases, not started

## Completed (device-confirmed functionality, pre-redesign)
- Repo + Gradle setup, GitHub Actions CI, app module (Kotlin + Compose, minSdk 26, target/compileSdk 34)
- AndroidManifest.xml — modern per-type media permissions + legacy fallback
- Phase 5: media import; Phase 6: real preview (Media3 ExoPlayer 1.4.1); Phase 7: basic timeline
- Phase 8: trim/split/undo-redo; Phase 9 step 1: per-clip speed
- Invisible-controls bug — fixed, confirmed; PlayerView default overlay — disabled, confirmed hidden

## Implemented, renders correctly (per screenshot), behavior not yet exercised
- Phase 9 step 2: whole-video rotate/flip; step 3: canvas/aspect ratio + background color; step 4: brightness/contrast/saturation

## Redesign scope (from uploaded brief) — full plan
1. Real theme — DONE (verified)
2. Navigation shell + top bar — DONE (verified)
3. Contextual tool-dock architecture — DONE (verified)
3.5 Visual polish pass (icons, surface depth) — DONE, unverified (this round)
4. Timeline v2 (thumbnails, zoom, scroll) — next after this is verified
5+. Audio, text, stickers, effects, filters, transitions, keyframes, captions, real export (Transformer), persistence, accessibility, performance, error handling
Biggest structural gap: no CompositionPlayer/Transformer yet — per-clip visual effects, real export, and transitions will need one or both; flagged, not yet decided.

## Cleanup done
- timeline/TrimState.kt deleted via a custom Zapier GitHub code action (no built-in delete-file action available)

## Key architecture decisions still in force
- No FFmpeg — Media3 only; minSdk 26; brand-new repo/package (com.carl.editor)
- Full-file replacements, files committed directly to GitHub via Zapier
- User waived the "one file at a time" pacing rule for this project; "no fake functionality / research before building" rules were NOT waived and still apply
- Global (not per-clip) visual effects for now — per-clip requires CompositionPlayer (experimental at Media3 1.9.0+, pinned to 1.4.1)
- All Media3 @UnstableApi usage requires @OptIn(UnstableApi::class) at the call site
- PlayerView's default controller overlay stays disabled
- Keeping PROJECT_STATE.md name and Phase-numbering, per explicit instruction
- Visible-but-disabled is the pattern for not-yet-implemented controls, never a button wired to do nothing
- NEW: icon usage established (material-icons-extended) — icon-first pattern for transport/tab controls, text-first still fine for numeric presets (speed) and body copy
- NEW: surface background (0xFF121212) used to visually separate chrome (top bar, timeline, tool dock) from the pure-black video canvas

## Current next step
Build and verify the visual polish pass on device. Specifically check: do the new icons actually render (icon names were verified against real Material icon identifiers, but this hasn't been compiled yet), does the top bar/timeline/tool dock now read as distinct "chrome" against the black preview, does the pill-styled Export button look right, does the active tool tab's highlight look right. A screenshot would be ideal to confirm before moving to Phase 4.
