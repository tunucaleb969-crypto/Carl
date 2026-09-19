# Carl — Project State

## Project
- Name: Carl
- Package: com.carl.editor
- Version: 0.1.0
- Current milestone: Black-preview bug found (well-documented Media3 issue) and fixed; error surfacing added; export pipeline started (isolated new files). THREE unverified changes stacked in PreviewScreen.kt (TextureView fix, error banner, plus the visual-polish/thumbnails round before it) — a build + device check is overdue and the top priority.

## Verification status (VERIFIED / INSPECTED / EXPECTED / FAILED / BLOCKED)
- VERIFIED (user confirmation, 2026-09-18): Phases 1+2+3 (theme, top bar, contextual tool dock) build and run on device.
- VERIFIED (device screenshot, earlier): pre-redesign trim/split/undo-redo/speed/rotate-flip/canvas/color all render; default PlayerView overlay stays hidden.
- VERIFIED (device screenshot, 2026-09-18 later): visual polish (icons, pill Export, tool-dock tabs) and Phase 4a thumbnails all RENDER correctly — confirms the ContentScale build fix worked.
- FAILED, THEN DIAGNOSED (device screenshot, 2026-09-18): video preview area was entirely black while adjusting Brightness/Contrast/Saturation sliders. Root cause identified via research: this is a well-documented Media3 bug (androidx/media issues #791, #1620, #1594) — ExoPlayer.setVideoEffects() reliably produces a black screen with PlayerView's default SurfaceView, even with an empty effects list. NOT specific to Carl's code.
- FIX APPLIED, NOT YET VERIFIED: PlayerView now inflated from a new XML layout (res/layout/player_view_texture.xml) with surface_type="texture_view" instead of constructed directly in Kotlin.
- ALSO ADDED, NOT YET VERIFIED: playback error surfacing via Player.Listener.onPlayerError, shown as a visible banner over the preview instead of failing silently.
- NOT YET VERIFIED: whether the TextureView fix actually resolves the black screen; whether rotate/flip/sliders/canvas visually affect output now that video is (hopefully) visible; whether they survive trim/split/undo/redo.
- NEW, EXPECTED NOT VERIFIED: export pipeline (ExportEngine.kt, ExportProgressDialog.kt) — logically validated against Media3's documented Transformer/Composition API shape only, never run.

## Export pipeline (NEW, deliberately isolated from PreviewScreen.kt)
Started building real export via Media3 Transformer as new files only — zero changes to PreviewScreen.kt/EditorTopBar.kt, since those already carry 3 stacked unverified changes. The Export button in the top bar is STILL disabled, not wired up yet.
- NEW dependency: androidx.media3:media3-transformer:1.4.1 (matches pinned exoplayer/effect version)
- NEW: export/ExportEngine.kt — concatenates the current clip list (trim/split preserved via the same MediaItem.ClippingConfiguration approach as preview) into a single MP4 via Transformer + Composition/EditedMediaItemSequence. Progress reported via a Kotlin Flow (Transformer has no push-based progress callback, must be polled — implemented via a polling loop inside callbackFlow).
  - DELIBERATE SCOPE LIMIT: does NOT bake in per-clip speed, rotate/flip, or color adjustments yet. Per-clip speed on export needs a SpeedChangeEffect with real timestamp remapping — reusing ExoPlayer.playbackParameters (preview-only) would silently produce a normal-speed file while claiming to honor the speed setting, which would be a fake feature. Deferred rather than shipped wrong.
  - VERIFICATION STATUS: logically validated against Media3's documented API shape only. NOT run on a device. Explicitly flagged as unconfirmed, especially Transformer's progress-polling/listener-threading behavior.
- NEW: export/ExportProgressDialog.kt — modal showing InProgress (with Cancel)/Success/Failure states. Uses the older Float-parameter LinearProgressIndicator overload deliberately (not the newer lambda-based one), since the project is pinned to material3:1.2.0 and the lambda overload's availability at that version wasn't confirmed — same caution that would have caught the ContentScale mistake earlier.
- NOT YET DONE: wiring the actual Export button (EditorTopBar.kt) to call ExportEngine and show ExportProgressDialog. Deliberately held off until the 3 stacked PreviewScreen.kt changes get a build/device check, to avoid touching that file a 4th time before any of it is confirmed.

## Black-preview bug (found, fixed, unverified)
- Symptom: full black rectangle where video should be, while trim/timeline/effects UI all rendered correctly around it
- Cause: ExoPlayer.setVideoEffects() + PlayerView's default SurfaceView — confirmed via multiple official Google issue reports, not a Carl-specific mistake
- Fix: NEW res/layout/player_view_texture.xml (PlayerView with surface_type="texture_view", use_controller="false"); PreviewScreen.kt now inflates this via LayoutInflater instead of `PlayerView(ctx)` directly
- Tradeoff accepted: TextureView uses more power than SurfaceView and loses some capabilities (HDR, secure DRM output) — acceptable for an editor preview, not final playback
- NOT YET BUILT OR TESTED

## Error surfacing (added, unverified)
- PreviewScreen.kt: new `playerErrorMessage` state, set via `Player.Listener.onPlayerError`, cleared when playback resumes successfully; shown as a visible banner over the preview
- NOT YET BUILT OR TESTED

## Build failure #1 (found and fixed, since verified working)
- `:app:compileDebugKotlin` failed: `Unresolved reference: ContentScale` in ClipThumbnailStrip.kt (wrong import). Fixed and confirmed via a later screenshot.

## Phase 4a: timeline thumbnails (VERIFIED rendering, per screenshot)
- timeline/ThumbnailCache.kt, timeline/ClipThumbnailStrip.kt, timeline/TimelineControls.kt (takes `uri: Uri`), PreviewScreen.kt
- Conservative for the 2GB Tecno Spark 5: 6 thumbnails per clip, 96px bitmaps, cached
- Zoom/scroll (Phase 4b) NOT included

## Visual polish pass (VERIFIED rendering, per screenshot)
Icon-first controls, pill-shaped Export, icon+label tool-dock tabs with accent highlight, surface backgrounds.
- EditorTopBar.kt, TimelineControls.kt, ToolTab.kt, ToolDock.kt, GlobalTransformControls.kt
- material-icons-extended dependency now actually used

## Self-audit findings (status)
- No icons anywhere — fixed, VERIFIED rendering
- Buttons had no visual weight — fixed, VERIFIED rendering
- No spacing/elevation hierarchy — partially addressed, VERIFIED rendering
- Timeline read as a thin slider — fixed via Phase 4a thumbnails, VERIFIED rendering; zoom/scroll still pending (4b)
- Typography still Material3 defaults — not addressed, no plan yet
- Video preview itself was invisible (black) — root-caused and fixed, UNVERIFIED

## Redesign Phase 3: contextual tool dock (VERIFIED building/running + rendering with icons)
## Redesign Phase 2: navigation shell + top bar (VERIFIED building/running + rendering with icons)
- Real bug fixed: previously no way to leave the editor at all
## Redesign Phase 1: real theme system (VERIFIED building/running)

## Fresh repo inspection findings (earlier this session)
- No real theme system existed — fixed (Phase 1)
- No way back out of the editor — fixed (Phase 2)
- Every tool panel always visible at once — fixed (Phase 3)
- No icons anywhere — fixed (visual polish pass), VERIFIED rendering
- Timeline was a single flat bar — fixed (Phase 4a), VERIFIED rendering; zoom/scroll still pending (4b)
- Video preview showed black during effects use — fixed (TextureView), UNVERIFIED
- Errors failed silently — fixed (error banner), UNVERIFIED
- No export pipeline — architecture/engine started (isolated), not wired to UI yet
- No persistence, no real project name — later phases, not started

## Completed (device-confirmed functionality, pre-redesign)
- Repo + Gradle setup, GitHub Actions CI, app module (Kotlin + Compose, minSdk 26, target/compileSdk 34)
- AndroidManifest.xml — modern per-type media permissions + legacy fallback
- Phase 5: media import; Phase 6: real preview (Media3 ExoPlayer 1.4.1); Phase 7: basic timeline
- Phase 8: trim/split/undo-redo; Phase 9 step 1: per-clip speed
- Invisible-controls bug — fixed, confirmed; PlayerView default overlay — disabled, confirmed hidden

## Redesign scope (from uploaded briefs) — full plan
1. Real theme — DONE (verified)
2. Navigation shell + top bar — DONE (verified)
3. Contextual tool-dock architecture — DONE (verified)
3.5 Visual polish pass (icons, surface depth) — DONE (verified rendering)
4a. Timeline thumbnails — DONE (verified rendering)
4a.5 Black-preview fix (TextureView) + error surfacing — DONE, UNVERIFIED (current top priority)
4b. Timeline zoom/scroll — next after 4a.5 is verified
"Export engine + dialog (isolated)" — DONE, UNVERIFIED, NOT WIRED to the UI yet
5+. Audio, text, stickers, effects, filters, transitions, keyframes, captions, per-clip speed/effects on export, persistence, accessibility, performance, error handling
Biggest structural gap: no CompositionPlayer yet (per-clip preview effects); export now has a first real engine but isn't wired to the button.

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
- `androidx.compose.ui.layout.ContentScale` is the correct import (not `androidx.compose.ui.graphics.ContentScale`)
- PlayerView MUST be inflated from res/layout/player_view_texture.xml (surface_type="texture_view"), never constructed directly, whenever ExoPlayer.setVideoEffects() is in use
- Any future ExoPlayer error handling goes through the existing `playerErrorMessage` banner pattern rather than failing silently
- NEW: export uses Media3 Transformer (media3-transformer:1.4.1), NOT FFmpeg; per-clip speed/effects deliberately NOT baked into export yet (would be incorrect without SpeedChangeEffect-based timestamp remapping)
- NEW: when pinned to an older Material3 version, prefer older/more universally-supported API overloads (e.g. Float-param LinearProgressIndicator) over newer ones whose availability at that version isn't confirmed

## Current next step
Two independent things pending, in priority order:
1. (Higher priority, blocking) Get a CI build + device check covering the 3 stacked PreviewScreen.kt changes: (a) does it compile with the new XML layout resource + LayoutInflater usage, (b) does the video preview actually show video now (not black) when rotate/flip/color effects are active, (c) does the error banner behave correctly (hidden during normal playback, visible only on a real error).
2. (Independent, not blocking on #1) Once that's confirmed, wire the Export button: enable it in EditorTopBar.kt, add export-triggering state + ExportEngine call + ExportProgressDialog to PreviewScreen.kt, and get a build to confirm the new media3-transformer dependency resolves correctly.
