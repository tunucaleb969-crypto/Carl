# CARL PROJECT STATE

Last updated: 2026-09-28

## Current milestone

Phase A — Timeline foundation, with speed/export verified; Phase B transform/crop implementation is in progress.

The timeline clip-management milestone is implemented. Variable-speed timeline coverage and the Media3 1.4.1 speed-export implementation both have successful GitHub Actions checkpoints.

## Last verified code checkpoint

- Commit: 9fb30a89c866a1478e9fff7356172f9d739eff81
- Crop unit coverage checkpoint: GitHub Actions run 142 succeeded.
- Current transform batch is under verification; the first test run exposed effect-construction failures and those failures are being fixed before the batch is marked complete.

## Current Phase B progress

- Global crop model and controls are wired into the contextual tool dock, preview, and export effect pipeline.
- Global crop bounds have unit coverage.
- Global transform now includes zoom and pan state with clamped ranges and contextual controls.
- Transform zoom/pan wiring is present in preview and export effect construction.
- The first transform verification exposed runtime effect-construction failures in unit tests; the implementation was adjusted to stay within the pinned Media3 1.4.1 transform API and is awaiting the replacement CI checkpoint.

## Last verified code checkpoint

- Commit: b75e0275d8be05675603d05285d6e0876944cee7
- Branch: main
- GitHub Actions run: 131
- Run ID: 36443943638
- Unit tests: success
- Debug APK build: success
- Verification: GitHub Actions `testDebugUnitTest` and `assembleDebug` both completed successfully.
- Device verification: not performed.

## Completed timeline work

- Clip selection with selected-state UI.
- Delete selected clip.
- Duplicate selected clip with a new clip identity.
- Reorder selected clips earlier/later.
- Undo/redo through the existing EditHistory state system.
- Preview playlist follows the ordered clip state.
- Export receives the ordered committed clip list.
- Playhead/selection synchronization.
- Selecting a clip seeks to its timeline start.
- Timeline zoom from 1x to 4x.
- Horizontal timeline scrolling while zoomed.
- Playhead auto-scrolls toward the visible working area during playback.
- Unit tests for core EditState clip deletion, duplication, reordering, invalid operations, split behavior, speed duration, and speed validation.
- Undo/redo history tests covering undo, redo, redo-branch clearing, no-op edits, and sync behavior.

## Current architecture

- Preview/player: `PreviewScreen.kt`
- Top editor actions: `EditorTopBar.kt`
- Timeline UI: `timeline/TimelineControls.kt`
- Timeline state/model: `timeline/EditState.kt`
- Undo/redo: `timeline/EditHistory.kt`
- Export: `export/ExportEngine.kt`
- Export UI: `export/ExportProgressDialog.kt`

Keep the existing architecture and extend it rather than rebuilding working systems.

## Export limitation

The current export pipeline primarily concatenates trimmed clips.

Preview/export behavior now implemented in code:
- playback speed per clip
- global rotate/flip
- brightness
- contrast
- saturation

Still preview/layout-only:
- canvas/background presentation

Do not describe those preview effects as fully rendered export features until export parity is implemented.

The export success UI currently reports app-storage output; open/share output actions are still a future milestone.

## Known verification limitations

- GitHub Actions verifies the debug APK build.
- The current workflow does not constitute real-device/emulator verification.
- Device playback/export behavior still needs verification on Android hardware.

## Next logical milestone

Move into Phase B while preserving the existing timeline architecture.

Immediate Phase B work:
1. Add crop/pan/zoom data and controls with a clear scope boundary between global and per-clip effects.
2. Preserve preview/export parity for every new effect before calling it complete.
3. Add unit tests for new edit-state transformations and keep CI green.
4. Device verification remains outstanding.

## Future roadmap

### Phase B — Core clip editing
- Crop
- Pan/zoom
- Per-clip rotation/flip
- Per-clip brightness/contrast/saturation
- Opacity
- Freeze frame
- Reverse where technically practical
- Better speed controls and speed curves where feasible

### Phase C — Visual effects
- Filters
- Adjustments
- Transitions
- Blur/background effects
- Vignette
- Per-clip effects

### Phase D — Audio
- Original audio controls
- Per-clip volume/mute
- External audio tracks
- Multiple audio layers
- Audio trimming/fades
- Voice-over where architecture supports it
- Audio/video synchronization

### Phase E — Text and overlays
- Text layers and typography
- Stickers
- Image overlays
- Picture-in-picture
- Layer ordering
- Animation

### Phase F — Advanced editing
- Keyframes
- Masks where feasible
- Advanced transitions
- Compound/grouped clips
- Advanced timeline interactions

### Phase G — Project system
- Save/reopen projects
- Metadata
- Autosave/recovery
- URI permission persistence
- Draft thumbnails
- Project cleanup

### Phase H — Export and delivery
- Resolution/frame-rate selection
- Quality/bitrate controls
- Accurate export progress
- Cancellation/retry
- Open/share exported video
- Media-library integration
- Preview/export parity

### Phase I — Reliability and polish
- Unit/integration tests
- Large/long-video testing
- Multiple-clip testing
- Permission/URI testing
- Low-memory testing
- Cancellation/error recovery
- Accessibility
- Theme consistency
- Performance profiling
- Crash prevention

## Continuation rules

- Inspect the live repository before every meaningful change.
- Fetch current file versions before editing them.
- Preserve unrelated work.
- Make small coherent commits.
- Fix broken builds before stacking new features.
- Never claim a feature is tested unless the relevant verification actually ran.
- Never claim device verification without a real device/emulator test.
- Keep preview, timeline, history, persistence, and export behavior aligned.
