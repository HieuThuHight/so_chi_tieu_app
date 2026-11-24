# Liquid Glass integration notes

This update keeps the existing Glass Finance screens, ViewModels, Room database, settings, and navigation logic. It upgrades the reusable glass surfaces in `ui/components/GlassComponents.kt`.

## What changed

- Added Kyant0 Backdrop (`io.github.kyant0:backdrop:1.0.6`) and Shapes (`io.github.kyant0:shapes:1.2.0`).
- Matched the Compose BOM to `2025.11.00` (Compose 1.9.4), which is the verified compatibility combination for Backdrop 1.0.6 with `compileSdk` 36.
- `GlassBackground` now records the animated gradient/orb background into a shared `LayerBackdrop`. The backdrop source is a sibling of the app content, avoiding a source/surface feedback loop.
- `GlassCard` and `GlassNavBar` now sample that backdrop and apply vibrancy, blur, lens refraction, tint, and highlight. Existing click handlers, selection animation, and glass-opacity setting remain in place.
- Existing fallback drawing remains available if a glass component is previewed outside `GlassBackground`.

## Important scope note

The current shared backdrop records the app's animated background (gradient and ambient orbs). It does not yet capture arbitrary scrolling card contents behind every card. This is a safe first integration that provides real backdrop refraction without restructuring every screen. A second pass can give selected overlays and floating controls a screen-content backdrop if needed.

## Build

Open the project in Android Studio and run **Sync Project with Gradle Files**, then **Build > Make Project**. The first sync needs network access to Maven Central.

The automated build could not be completed in the editing environment because it could not reach `services.gradle.org` to download the Gradle 9.4.1 distribution. Please treat this archive as an integration patch awaiting an Android Studio build check, not as a verified release APK.

## License note

The integration uses the Backdrop and Shapes libraries as external dependencies; Backdrop is Apache-2.0. No SimpMusic source files were copied into Glass Finance.
