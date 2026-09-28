# CARL Engineering Research

Last refreshed: 2026-09-28

## Purpose
This is Carl's durable engineering knowledge base. Refresh it before major platform/API decisions so development does not depend on chat memory.

## Live baseline
- Kotlin + Jetpack Compose + Material 3.
- Java/JVM target 17.
- AGP 8.2.0 with Gradle 8.2.
- compileSdk/targetSdk 34.
- Media3 modules pinned to 1.4.1.
- GitHub Actions runs unit tests and assembles a debug APK.
- Unified project state/history, timeline clip operations, global transform/crop/color state, and a Transformer export foundation exist.
- Repository search found no Room, DataStore, serialization, or project save/reopen layer.

## Android 16 / API 36
Android Developers documents API 36 as Android 16. Targeting API 36 brings behavior changes including enforced edge-to-edge and predictive-back requirements. Google Play requires new apps and app updates to target API 36 or higher from August 31, 2026.
The live project is still on target/compile 34.

## Toolchain finding
AGP 8.2.0 supports up to API 34. Current Android documentation lists AGP 8.9.1 as the minimum for API 36, while AGP 8.10 and 8.13 support API 36; AGP 8.13 requires Gradle 8.13 and JDK 17.
Decision: migrate build tooling and compile/target SDK as one coherent batch. Do not upgrade Media3 at the same time unless compatibility is explicitly verified.

## Edge-to-edge
Android Developers recommends enableEdgeToEdge plus deliberate WindowInsets handling. safeDrawing is the general protection for interactive UI. Carl's Compose screens and embedded PlayerView must be checked after the migration.
Decision: treat edge-to-edge as part of the API 36 migration, including HomeScreen, editor chrome, timeline/tool dock, export UI, and PlayerView.

## Adaptive UI
Android recommends window-size classes based on available window space rather than device-model checks. Carl should adapt to compact, medium, expanded and larger windows, including tablets, foldables and resizable desktop windows.
Decision: keep window-size logic centralized and preserve the same editing state across layouts.

## Media3
Current official Media3 documentation uses 1.11.1 in Transformer examples, while the live project remains on 1.4.1.
Composition supports EditedMediaItem-level effects and composition-level effects. CompositionPlayer is designed for real-time preview of a Composition and can preview per-item effects, but current documentation still describes it as an early preview/experimental API.
Decision: keep renderer abstractions separate from the editing model. Do not blindly upgrade Media3 just to obtain CompositionPlayer, and do not claim that CompositionPlayer solves all future multi-track/PIP cases.

## Transformer/export
Current Transformer documentation supports trimming, video effects, image inputs, audio processing, and Composition export. Composition documentation still lists limitations such as crossfading video/audio tracks.
Decision: every visible editing feature needs a real export path or must remain clearly disabled/deferred. Preview-only playback parameters must never be presented as export support.

## Photo Picker URI persistence
Android's Photo Picker can provide a URI grant for the selected media. When a provider supports persistable permissions, Carl can request a persisted read grant with ContentResolver.takePersistableUriPermission(). Providers may reject persistence, so this must be best-effort and the project must retain enough source identity to relink later.

Decision: request a persistable read grant at import time, but do not treat it as proof that the media will remain available forever. The future project format must store source URI plus stable source metadata and provide a relink path.

## Project persistence
Android documentation positions DataStore for small settings/typed objects and Room for larger or relational datasets with partial updates and referential integrity. Carl's future project model will contain clips, tracks, effects, assets, keyframes, text and recovery metadata.
Decision: do not use DataStore as the primary editor-project database. Move toward a versioned project format plus a persistent asset/project index, with atomic autosave and migrations.

## Testing
CI currently runs testDebugUnitTest and assembleDebug. This does not prove real playback, export on hardware, codec compatibility, Android 15/16 edge-to-edge behavior, adaptive layouts, or long-running export behavior.
Decision: distinguish CI verification from device verification in all project documentation.

## Known risks
- Project persistence/autosave/recovery is not implemented.
- Asset identity and URI relinking are not implemented.
- The editor remains fundamentally single-source/single-track.
- Per-clip state is modeled, while the current ExoPlayer preview path still applies global effects.
- Canvas/background presentation is not fully baked into export.
- Recent preview/timeline changes still need physical-device verification.
- Current build tooling is below the documented minimum for API 36.

## Next research
1. Verify exact AGP/Gradle/Kotlin/Compose compatibility before API 36 migration.
2. Refresh Media3 compatibility before adopting CompositionPlayer.
3. Design versioned project schema and asset identity.
4. Research Photo Picker URI persistence/relinking.
5. Research current Android rules for long-running export.
6. Research codec/device capability detection and HDR/SDR behavior.

## Primary sources
- https://developer.android.com/about/versions/16/behavior-changes-16
- https://developer.android.com/about/versions/16/setup-sdk
- https://developer.android.com/google/play/requirements/target-sdk
- https://developer.android.com/build/releases/about-agp
- https://developer.android.com/build/releases/agp-8-13-0-release-notes
- https://developer.android.com/develop/ui/compose/system/setup-e2e
- https://developer.android.com/develop/ui/compose/system/insets
- https://developer.android.com/develop/adaptive-apps/guides/support-different-display-sizes
- https://developer.android.com/develop/adaptive-apps/guides/use-window-size-classes
- https://developer.android.com/jetpack/androidx/releases/media3
- https://developer.android.com/media/media3/transformer/composition
- https://developer.android.com/media/media3/transformer/compositionplayer
- https://developer.android.com/media/media3/transformer/getting-started
- https://developer.android.com/topic/libraries/architecture/datastore