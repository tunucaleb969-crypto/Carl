# Carl — Project State

## Project
- Name: Carl
- Package: com.carl.editor
- Version: 0.1.0
- Current milestone: FIRST BUILD FAILURE of this project, caught via CI log screenshot, fixed. A second CI build result is now needed to confirm the fix worked.

## Verification status (VERIFIED / INSPECTED / EXPECTED / FAILED / BLOCKED)
- VERIFIED (user confirmation, 2026-09-18): Phases 1+2+3 (theme, top bar, contextual tool dock) build and run on device.
- VERIFIED (device screenshot, earlier): pre-redesign trim/split/undo-redo/speed/rotate-flip/canvas/color all render and the default PlayerView overlay stays hidden.
- FAILED (CI build log screenshot, 2026-09-18): `:app:compileDebugKotlin` FAILED. Two identical errors in timeline/ClipThumbnailStrip.kt (lines 17 and 51): `Unresolved reference: ContentScale`. Root cause: wrong import - `androidx.compose.ui.graphics.ContentScale` doesn't exist, the real package is `androidx.compose.ui.layout.ContentScale`. FIXED in ClipThumbnailStrip.kt (import corrected), not yet reconfirmed with a new build.
- The visual polish pass (icons) was NOT implicated in this failure - the bug was isolated to the newer, more complex Phase 4a thumbnail code, exactly as flagged as higher-risk beforehand.
- STILL EXPECTED, NOT CONFIRMED: visual polish pass (icons + surface backgrounds) - never got a clean build to verify against, since the build failed on an unrelated file. Will be covered by the next build attempt.
- NOT YET VERIFIED: whether rotate/flip/sliders/canvas visually affect output when interacted with; whether they survive trim/split/undo/redo.

## Build failure #1 (found and fixed)
- Error: `e: file:///.../ClipThumbnailStrip.kt:17:37 Unresolved reference: ContentScale` (and again at line 51)
- Cause: imported `androidx.compose.ui.graphics.ContentScale` instead of the correct `androidx.compose.ui.layout.ContentScale`
- Fix: corrected the import in ClipThumbnailStrip.kt. No other files touched - the rest of Phase 4a (ThumbnailCache.kt, TimelineControls.kt, PreviewScreen.kt) was untouched by this fix since the error was isolated to one bad import in one file.
- NOT YET RE-VERIFIED - needs a fresh CI build to confirm this was the only problem.

## Phase 4a: timeline thumbnails (fix applied, re-verification pending)
Split "Timeline v2" into 4a (thumbnails) and 4b (zoom/scroll, later).
- timeline/ThumbnailCache.kt — generates small (96x96px) preview frames via MediaMetadataRetriever.getScaledFrameAtTime (API 27+, manual-downscale getFrameAtTime fallback for API 26); in-memory cache keyed by clip identity
- timeline/ClipThumbnailStrip.kt — renders one clip's filmstrip; FIXED import bug (ContentScale)
- timeline/TimelineControls.kt — replaced the flat colored bar with a Row of ClipThumbnailStrip per clip, sized via Modifier.weight(clip.durationMs); takes a `uri: Uri` parameter now
- PreviewScreen.kt — passes `uri` into TimelineControls
- Deliberately conservative for the 2GB Tecno Spark 5: 6 thumbnails per clip, 96px bitmaps, cached
- Zoom/scroll (Phase 4b) NOT included in this step

## Visual polish pass (implemented, still unconfirmed by a clean build)
Icon-first controls, pill-shaped Export, icon+label tool-dock tabs with accent highlight, surface backgrounds. Icon names (rotate_right, aspect_ratio, content_cut, tune, undo, redo, file_upload) verified against real Material identifiers before use - none of these were implicated in the build failure above.
- EditorTopBar.kt, TimelineControls.kt, ToolTab.kt, ToolDock.kt, GlobalTransformControls.kt
- material-icons-extended dependency (present since early, previously unused) now actually used

## Self-audit findings (status)
- No icons anywhere — addressed (pending clean-build confirmation)
- Buttons had no visual weight — addressed (pending)
- No spacing/elevation hierarchy — partially addressed (pending)
- Timeline read as a thin slider — addressed via Phase 4a thumbnails (pending); zoom/scroll still pending (4b)
- Typography still Material3 defaults — not addressed, no plan yet

## Redesign Phase 3: contextual tool dock (VERIFIED building/running; now also has icons, pending re-confirmation)
- ToolTab.kt, ToolDock.kt, PreviewScreen.kt — architecture confirmed correct

## Redesign Phase 2: navigation shell + top bar (VERIFIED building/running; now also has icons, pending re-confirmation)
- EditorTopBar.kt, TimelineControls.kt (Undo/Redo removed), onBack wiring
- Real bug fixed: previously no way to leave the editor at all

## Redesign Phase 1: real theme system (VERIFIED building/running)
- ui/theme/Theme.kt (CarlTheme, real darkColorScheme), wired into MainActivity.kt

## Fresh repo inspection findings (earlier this session)
- No real theme system existed — fixed (Phase 1)
- No way back out of the editor — fixed (Phase 2)
- Every tool panel always visible at once — fixed (Phase 3)
- No icons anywhere, flat text-only controls — fixed (visual polish pass), pending re-confirmation
- Timeline was a single flat bar — fixed (Phase 4a), pending re-confirmation; zoom/scroll still pending (4b)
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
3.5 Visual polish pass (icons, surface depth) — DONE, pending re-confirmation after build fix
4a. Timeline thumbnails — DONE, build error found+fixed, pending re-confirmation
4b. Timeline zoom/scroll — next after 4a is verified
5+. Audio, text, stickers, effects, filters, transitions, keyframes, captions, real export (Transformer), persistence, accessibility, performance, error handling
Biggest structural gap: no CompositionPlayer/Transformer yet.

## Cleanup done
- timeline/TrimState.kt deleted via a custom Zapier GitHub code action (no built-in delete-file action available)

## Key architecture decisions still in force
- No FFmpeg — Media3 only; minSdk 26; brand-new repo/package (com.carl.editor)
- Full-file replacements, files committed directly to GitHub via Zapier
- User waived the "one file at a time" pacing rule; "no fake functionality / research before building" rules were NOT waived
- Global (not per-clip) visual effects for now — per-clip requires CompositionPlayer (experimental at Media3 1.9.0+, pinned to 1.4.1)
- All Media3 @UnstableApi usage requires @OptIn(UnstableApi::class) at the call site
- PlayerView's default controller overlay stays disabled
- Keeping PROJECT_STATE.md name and Phase-numbering, per explicit instruction
- Visible-but-disabled is the pattern for not-yet-implemented controls
- Icon-first pattern established (material-icons-extended)
- Surface background (0xFF121212) separates chrome from the pure-black video canvas
- Thumbnail generation via MediaMetadataRetriever, small in-memory cache, conservative (96px, 6 per clip)
- NEW: `androidx.compose.ui.layout.ContentScale` is the correct import (not `androidx.compose.ui.graphics.ContentScale`) — easy mistake to repeat, worth remembering

## Current next step
Re-run the CI build to confirm the ContentScale fix was the only problem. If green: install and check specifically whether thumbnails actually render in the timeline, whether scrolling/recomposition feels smooth on the Tecno Spark 5, and whether the icons from the visual polish pass render correctly (that pass never got a clean build to verify against). If red again: paste the new log - there could be a second, separate issue the first error was masking.
