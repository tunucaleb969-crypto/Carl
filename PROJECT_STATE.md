# Carl — Project State

## Project
- Name: Carl
- Package: com.carl.editor
- Version: 0.1.0
- Current milestone: Phase 4a (timeline thumbnails) IMPLEMENTED, unverified, stacked on top of the also-unverified visual polish pass. Two unverified rounds in a row now — both need a build/device check before anything else.

## Verification status (VERIFIED / INSPECTED / EXPECTED / FAILED / BLOCKED)
- VERIFIED (user confirmation, 2026-09-18): Phases 1+2+3 (theme, top bar, contextual tool dock) build and run on device.
- VERIFIED (device screenshot, earlier): pre-redesign trim/split/undo-redo/speed/rotate-flip/canvas/color all render and the default PlayerView overlay stays hidden.
- EXPECTED, NOT BUILT: visual polish pass (icons + surface backgrounds). Reasoned carefully, icon names verified against real Material identifiers, but never compiled.
- EXPECTED, NOT BUILT: Phase 4a (clip thumbnail filmstrip). New, more complex code (bitmap generation/caching, coroutine-driven UI) — higher chance of a real bug than the visual polish pass.
- NOT YET VERIFIED: whether rotate/flip/sliders/canvas visually affect output when interacted with; whether they survive trim/split/undo/redo.

## Phase 4a: timeline thumbnails (IMPLEMENTED, unverified)
Split "Timeline v2" into 4a (thumbnails, this round) and 4b (zoom/scroll, later) — matches "build in phases" rather than doing both at once.
- NEW: timeline/ThumbnailCache.kt — generates small (96x96px) preview frames via MediaMetadataRetriever.getScaledFrameAtTime (API 27+, with a manual-downscale getFrameAtTime fallback for API 26 since that's our minSdk); simple in-memory cache keyed by clip identity so scrolling/recomposition doesn't regenerate frames
- NEW: timeline/ClipThumbnailStrip.kt — renders one clip's filmstrip (Row of frames, dark gray placeholder for any frame that hasn't loaded or failed to extract)
- MODIFIED: timeline/TimelineControls.kt — replaced the flat colored bar with a Row of ClipThumbnailStrip per clip, sized proportionally via Modifier.weight(clip.durationMs); now takes a `uri: Uri` parameter to fetch frames; track height increased 48dp→56dp, filmstrip 40dp tall, boundary markers/playhead/trim handles drawn on top unchanged
- MODIFIED: PreviewScreen.kt — passes `uri` into the TimelineControls call
- Deliberately conservative for the 2GB Tecno Spark 5: fixed 6 thumbnails per clip, small 96px bitmaps, cached (not regenerated per recomposition/scroll)
- Zoom/scroll (Phase 4b) NOT included in this step — separate, later effort
- NOT YET BUILT OR TESTED

## Visual polish pass (IMPLEMENTED, unverified) — "how did CapCut do it" (from previous round, still unbuilt)
Icon-first controls, pill-shaped Export, icon+label tool-dock tabs with accent highlight, surface backgrounds separating chrome from the video canvas. Icon names (rotate_right, aspect_ratio, content_cut, tune, undo, redo, file_upload) verified against real Material identifiers before use.
- EditorTopBar.kt, TimelineControls.kt, ToolTab.kt, ToolDock.kt, GlobalTransformControls.kt all touched
- material-icons-extended dependency (present since early, previously unused) is now actually used
- NOT YET BUILT OR TESTED (now stacked under Phase 4a above — two rounds of unverified work)

## Self-audit findings (status)
- No icons anywhere — addressed (visual polish pass, unverified)
- Buttons had no visual weight — addressed (unverified)
- No spacing/elevation hierarchy — partially addressed (unverified)
- Timeline read as a thin slider, not a real timeline — addressed via Phase 4a thumbnails (unverified); zoom/scroll still pending (4b)
- Typography still Material3 defaults — not addressed, no plan yet

## Redesign Phase 3: contextual tool dock (VERIFIED building/running; now also has icons, unverified)
- ToolTab.kt, ToolDock.kt, PreviewScreen.kt — architecture confirmed correct

## Redesign Phase 2: navigation shell + top bar (VERIFIED building/running; now also has icons, unverified)
- EditorTopBar.kt, TimelineControls.kt (Undo/Redo removed), onBack wiring
- Real bug fixed: previously no way to leave the editor at all

## Redesign Phase 1: real theme system (VERIFIED building/running)
- ui/theme/Theme.kt (CarlTheme, real darkColorScheme), wired into MainActivity.kt

## Fresh repo inspection findings (earlier this session)
- No real theme system existed — fixed (Phase 1)
- No way back out of the editor — fixed (Phase 2)
- Every tool panel always visible at once — fixed (Phase 3)
- No icons anywhere, flat text-only controls — fixed (visual polish pass), pending verification
- Timeline was a single flat bar — fixed (Phase 4a), pending verification; zoom/scroll still pending (4b)
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
3.5 Visual polish pass (icons, surface depth) — DONE, unverified
4a. Timeline thumbnails — DONE, unverified (this round)
4b. Timeline zoom/scroll — next after 4a is verified
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
- Icon-first pattern established (material-icons-extended) for transport/tab controls
- Surface background (0xFF121212) separates chrome from the pure-black video canvas
- NEW: thumbnail generation via MediaMetadataRetriever, small in-memory cache keyed by clip identity, deliberately conservative (96px, 6 per clip) for the 2GB test device

## Current next step
Get a CI build + device check covering BOTH unverified rounds together (visual polish + Phase 4a thumbnails) — they're stacked with no build in between, which is more risk than this project has carried before. Priorities to check: does it compile at all (Phase 4a introduces new Bitmap/coroutine code, a real step up in complexity from a two-file icon swap); do thumbnails actually appear in the timeline; does scrolling/recomposition feel smooth or janky on the Tecno Spark 5; do the icons render. A screenshot would help a lot here.
