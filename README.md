# Carl

A professional Android video editor focused on a strong mobile editing workflow, adaptive UI, and a scalable Media3-based media engine.

## Status
🚧 Active foundation development. Timeline editing, preview effects, crop, transform, color adjustment, speed controls, and Transformer export are implemented and continuously verified by CI.

## Engineering direction
Carl is being developed feature-batch by feature-batch. The architecture is being hardened for per-clip effects, multi-track editing, audio, captions, transitions, keyframes, adaptive phone/tablet/foldable layouts, device capability-aware rendering, reliable export, project persistence, and recovery.

The implementation is capability-based rather than tied to specific phone models. Android's current adaptive guidance recommends responsive layouts based on available window size and runtime capabilities rather than device allowlists.

## Tech stack
- Kotlin + Jetpack Compose
- Jetpack Media3 (Transformer, effect, ExoPlayer, CompositionPlayer) — media engine
- Gradle (AGP 8.2.0), built via GitHub Actions only — no local Android Studio in this workflow

## Project structure
- app/src/main/java/com/carl/editor/ — Kotlin source
- app/src/main/res/ — resources (themes, strings, layouts)
- app/src/main/AndroidManifest.xml
- app/build.gradle.kts
- build.gradle.kts — root build config
- settings.gradle.kts
- gradle.properties
- .github/workflows/build.yml — CI: builds debug APK on every push
- PROJECT_STATE.md — current progress, next steps (read this first)

## How to build
Builds run automatically via GitHub Actions on every push to `main`. Check the **Actions** tab after committing — download the debug APK from the workflow run's artifacts.

No local build steps required; this project is developed entirely through GitHub's web editor + Actions.

## Continuity
- project.md is the new-chat continuation entry point.
- PROJECT_STATE.md and CARL_PROJECT_STATE.md remain the detailed progress records.
- docs/CARL_ENGINEERING_RESEARCH.md is the durable research knowledge base.

## Development status
See `PROJECT_STATE.md` for current milestone, completed features, and next steps.
