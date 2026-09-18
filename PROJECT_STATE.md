# Carl — Project State

## Project
- Name: Carl
- Package: com.carl.editor
- Version: 0.1.0
- Current milestone: Redesign Phases 1+2+3 VERIFIED WORKING (app builds and runs). Visual polish is explicitly NOT there yet per direct user feedback ("doesn't look like a professional video editing app at all") — architecture is now right (theme/nav/contextual dock), presentation still looks amateur. This is the current focus, before Phase 4.

## Verification status (VERIFIED / INSPECTED / EXPECTED / FAILED / BLOCKED)
- VERIFIED (user confirmation, 2026-09-18): Phases 1+2+3 (theme, top bar, contextual tool dock) build and run on device. This is the first real confirmation since Phase 1 was implemented.
- VERIFIED (device screenshot, earlier): pre-redesign trim/split/undo-redo/speed/rotate-flip/canvas/color all render and the default PlayerView overlay stays hidden.
- NOT YET VERIFIED: whether rotate/flip/sliders/canvas visually affect output when interacted with; whether they survive trim/split/undo/redo.
- USER FEEDBACK (not a bug report, a design gap): app runs correctly but "doesn't look like a professional video editing app at all." No screenshot provided this round — asked for one to diagnose precisely rather than guessing at scale. Self-audit below lists the most likely concrete causes to fix regardless.

## Self-audit: likely causes of the "not professional" look (pending screenshot confirmation)
- NO ICONS ANYWHERE. Every control (top bar Back/Undo/Redo/Export, tool dock tabs, timeline Play/Split, speed presets) is a plain text TextButton. Real editors are icon-first with text as a secondary label. material-icons-extended is already a dependency (added early in the project) but has never actually been used — this is probably the single biggest visual gap.
- Buttons have no visual weight — flat TextButtons with no background/chip/elevation, so nothing reads as a distinct "tool" vs. plain hyperlink-style text.
- Timeline is a thin single-color bar with tick marks — functionally correct but visually reads as a slider/progress bar, not a timeline (this is already known and scheduled as Phase 4, not a quick fix).
- Typography is still Typography() defaults (documented as deliberate in Theme.kt) — no distinctive type scale yet.
- No spacing/elevation hierarchy between the top bar, preview, timeline, and tool dock sections — they likely blend together without visual separation (dividers, subtle surface elevation, etc.)

## Redesign Phase 3: contextual tool dock (VERIFIED building/running; visual execution flagged as amateur, not yet fixed)
- ToolTab.kt, ToolDock.kt, PreviewScreen.kt — architecture confirmed correct (one panel at a time replaces the old always-visible stack), but this only fixed information architecture, not visual polish

## Redesign Phase 2: navigation shell + top bar (VERIFIED building/running)
- EditorTopBar.kt (Back, project name placeholder, Undo, Redo, Export-disabled), TimelineControls.kt (Undo/Redo removed), onBack wiring in PreviewScreen.kt/MainActivity.kt
- Real bug fixed: previously no way to leave the editor at all

## Redesign Phase 1: real theme system (VERIFIED building/running)
- ui/theme/Theme.kt (CarlTheme, real darkColorScheme), wired into MainActivity.kt
- HomeScreen.kt/PreviewScreen.kt still hardcode Color.Black/White rather than reading theme tokens — unchanged, not urgent

## Fresh repo inspection findings (earlier this session)
- No real theme system existed — fixed (Phase 1)
- No way back out of the editor — fixed (Phase 2)
- Every tool panel always visible at once — fixed (Phase 3)
- No icons anywhere, flat text-only controls — NEW finding this round, not yet fixed
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
1. Real theme — DONE (verified building/running)
2. Navigation shell + top bar — DONE (verified building/running)
3. Contextual tool-dock architecture — DONE (verified building/running)
3.5 VISUAL POLISH PASS — IN PROGRESS (current focus): icons via material-icons-extended, button chip styling, spacing/elevation hierarchy
4. Timeline v2 (thumbnails, zoom, scroll) — next after polish
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
- NEW: material-icons-extended dependency exists but is unused — next visual-polish step should actually use it instead of plain text labels

## Current next step
Get a screenshot of the current app to diagnose the "not professional" look precisely (worked well twice before for finding concrete, fixable issues). In parallel, the self-audit above already identifies the most likely fix: replace plain TextButton text labels with icon+label controls (top bar, tool dock tabs, timeline transport) using the already-present material-icons-extended dependency, plus basic chip/elevation styling for visual weight. This is presentational only — low regression risk to existing working functionality.
