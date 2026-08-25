# Carl

A professional mobile video-editing app for Android — built to compete with CapCut on editing power, timeline workflow, and UX, with its own original design identity.

## Status
🚧 Early development — foundation phase complete, no editing features yet.

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

## Development status
See `PROJECT_STATE.md` for current milestone, completed features, and next steps.
