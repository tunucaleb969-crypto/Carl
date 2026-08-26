# Carl — Project State

## Project
- Name: Carl
- Package: com.carl.editor
- Version: 0.1.0
- Current milestone: Phase 5 — Media Import (starting)

## Progress
- Overall: Foundation + Home screen complete, both confirmed working on device

## Completed
- Repo + Gradle setup (settings.gradle.kts, root build.gradle.kts, gradle.properties)
- GitHub Actions CI (.github/workflows/build.yml)
- App module (app/build.gradle.kts) — Kotlin + Jetpack Compose, minSdk 26, targetSdk/compileSdk 34
- AndroidManifest.xml — modern per-type media permissions + legacy fallback
- Basic dark theme (Theme.Carl)
- MainActivity.kt — now launches HomeScreen
- HomeScreen.kt — empty-state UI ("No projects yet" + "New Project" button), confirmed on device

## In progress
- None (Phase 4 just finished)

## Planned (next)
- Phase 5: Media import — photo/video picker triggered by "New Project"
- Phase 6: Preview (add Media3 ExoPlayer dependency + basic playback)

## Architecture / stack
- Kotlin + Jetpack Compose
- Media engine: Jetpack Media3 (Transformer, effect, ExoPlayer, CompositionPlayer) — not yet added as a dependency, planned for Phase 6
- Build: AGP 8.2.0, Gradle 8.7 (wrapper generated at CI build time, not committed)
- CI: GitHub Actions only — no local Android Studio/laptop in the workflow

## Test device
- Tecno Spark 5, 2GB RAM, Android 10 (Go edition) — primary real-device test target

## Known bugs
- None yet

## Decisions made (don't change without reason)
- Brand-new repo/package for Carl — not a continuation of the earlier Caleb Media (com.calebmedia) project
- No FFmpeg — Media3 only
- minSdk 26 (not the Media3 floor of 23)
- One file at a time, full-file replacements only, no manual find/edit
- UI split by screen into separate composable files (e.g. HomeScreen.kt), MainActivity.kt stays thin

## Current next step
Start Phase 5: media picker so "New Project" actually opens the device's photo/video picker.

## Completed
- Phase 6: Real video preview via Media3 ExoPlayer (pinned to 1.4.1 for compileSdk 34 compat) — PreviewScreen.kt plays picked video, confirmed on device

## Current next step
Start Phase 7: basic timeline (show a scrubbable progress bar / seek control first, then trim).
## Completed
- Phase 7: Basic timeline — seek bar, position/duration display, play/pause (TimelineControls.kt), confirmed on device

## Current next step
Start Phase 8: real editing — trim and split, with undo/redo.
