# Carl — Project State

## Project
- Name: Carl
- Package: com.carl.editor
- Version: 0.1.0
- Current milestone: Phase 3 — Project Foundation

## Progress
- Overall: Foundation complete, first green build confirmed on device

## Completed
- Repo + Gradle setup (settings.gradle.kts, root build.gradle.kts, gradle.properties)
- GitHub Actions CI (.github/workflows/build.yml) — generates Gradle wrapper at build time, builds debug APK, uploads as artifact
- App module (app/build.gradle.kts) — Kotlin + Jetpack Compose, minSdk 26, targetSdk/compileSdk 34
- AndroidManifest.xml — modern per-type media permissions (READ_MEDIA_VIDEO/IMAGES/AUDIO) + legacy fallback
- Basic dark theme (Theme.Carl)
- MainActivity.kt — minimal Compose screen, confirmed running on device

## In progress
- None (foundation phase just finished)

## Planned (next)
- README.md
- Phase 4: Home screen (empty state, "new project")
- Phase 5: Media import (photo/video picker)
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
- minSdk 26 (not the Media3 floor of 23) — matches realistic device support without extra complexity
- One file at a time, full-file replacements only, no manual find/edit

## Current next step
Add README.md, then start Phase 4 (Home screen).
