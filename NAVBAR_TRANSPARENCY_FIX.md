# Navigation bar transparency and Liquid Glass tuning

- Removed Android navigation-bar contrast scrim (light/dark scrim now transparent).
- Disabled navigation-bar contrast enforcement on Android 10+ so the system does not add a gray/white strip behind the pill.
- Applied clipping before and after the backdrop draw modifier.
- Reduced opaque surface tint and adjusted blur/lens so backdrop color and refraction are more visible.
- Kept long-press drag navigation and tap selection.

Build was not run in this environment; verify with Android Studio / Gradle Sync.
