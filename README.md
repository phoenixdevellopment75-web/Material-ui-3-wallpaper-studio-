<div align="center">

# ✦ Material UI 3 Wallpaper Studio

**A tactile, math-driven procedural wallpaper engine & interactive canvas studio built natively for Android.**

[![Release](https://img.shields.io/badge/Release-v2.6.0--beta-4f46e5?style=for-the-badge&logo=github&logoColor=white)](https://github.com/phoenixdevellopment75-web/Material-ui-3-wallpaper-studio-/releases)
[![Android](https://img.shields.io/badge/Android-12%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin%202.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-Apache%202.0-F97316?style=for-the-badge)](LICENSE)

<br/>

```text
 ╭────────────────────────────────────────────────────────────────────────╮
 │  ⚡ 100% On-Device Math  ·  🎨 Material You Dynamic Theming  ·  🔒 Private  │
 ╰────────────────────────────────────────────────────────────────────────╯
```

[Download APK (v2.6.0-beta)](https://github.com/phoenixdevellopment75-web/Material-ui-3-wallpaper-studio-/releases) • [Features](#-features--capabilities) • [Procedural Engines](#-procedural-render-engines) • [Architecture](#-architecture) • [Building](#-getting-started)

---

</div>

## 🎨 Overview

**Wallpaper Studio** is a native Android application designed to generate ultra-sharp, resolution-independent vector wallpapers generated completely on-device.

Instead of downloading heavy static images or burning battery on remote cloud generators, Wallpaper Studio calculates trigonometric curves, Bauhaus grids, fluid camo, and topographic contours using pure mathematical formulas on the hardware-accelerated **Skia 2D Canvas**.

The app is built from the ground up with **Jetpack Compose** and **Material 3 Expressive**, offering dynamic color extraction (`Monet`), spring physics, and an interactive studio canvas for customizing text, shapes, and layers.

---

## ✨ Key Highlights

- **⚡ 100% On-Device Computation:** Every wallpaper is drawn live through algorithmic geometry. No internet connection required, zero latency, and zero telemetry.
- **🎨 Dynamic Material 3 Expressive:** Automatically extracts harmonious accent tones from your active system wallpaper using Android 12+ Monet color engine.
- **📐 Infinite Resolution:** Vector equations scale flawlessly across phones, tablets, foldables, and ultra-wide displays without pixelation.
- **🔋 Battery & Memory Friendly:** Renders on-demand with zero background wake-locks or polling, running smoothly at 60/120 FPS even on 4GB RAM devices.
- **🛠️ Interactive Studio Canvas:** Move, scale, rotate, and style shapes, clovers, polygons, and typography layers with multi-touch gestures.

---

## 🔮 Procedural Render Engines

Wallpaper Studio features **25+ algorithmic generators** that synthesize complex geometry on the fly:

| Category | Engines Included | Description |
| :--- | :--- | :--- |
| **Geometric & Bauhaus** | `BauhausGrid`, `BauhausSemicircle`, `ModularBauhaus`, `MidCenturyGrid`, `Grids` | Bold minimalist geometry inspired by 20th-century modernism and Swiss design. |
| **Organic & Fluid** | `LavaBlob`, `LiquidCamo`, `FlatLiquidCamo`, `HarmonicWave` | Smooth Bézier curves and mathematical waves creating soothing ambient flows. |
| **Architectural & Fluted** | `FlutedGlass`, `FlutedArches`, `NestedArches`, `PaperRidges` | Refractive ribbed textures and architectural depth simulating tactile materials. |
| **Terrain & Topography** | `Topographic`, `Mountain`, `AtmosphericMountain` | Mathematical contour lines and layered elevation silhouettes. |
| **Patterns & Textures** | `DotGrid`, `PolarLabyrinth`, `CrossCurves`, `Slices`, `StackedPills`, `FilmGrain` | Optical illusions, precision dot matrices, and subtle analog film grain overlays. |

---

## 🛠️ Architecture & Tech Stack

```text
app/
├── src/main/java/com/example/
│   ├── engine/          # 25+ Procedural Skia Canvas render engines
│   │   ├── Material3ShapeLibrary.kt
│   │   ├── ProceduralRenderer.kt
│   │   └── WallpaperParams.kt
│   ├── ui/              # Jetpack Compose UI & tactile animations
│   │   ├── CustomStudioCanvas.kt    # Multi-touch interactive studio
│   │   ├── ExpressiveBottomDock.kt  # M3 floating action dock
│   │   ├── MotionPhysics.kt         # Spring damping motion specs
│   │   └── WallpaperViewModel.kt    # State machine & params flow
│   ├── palette/         # Dynamic Monet extraction & custom palette builder
│   ├── export/          # High-resolution PNG/Bitmap background exporter
│   └── math/            # Trigonometric utilities & normalization formulas
```

- **UI Toolkit:** Jetpack Compose (1.6+) & Material Design 3 Expressive
- **Graphics Pipeline:** Hardware-accelerated Skia 2D Canvas (`drawPath`, `drawVertices`, offscreen rendering)
- **Architecture:** MVVM / Unidirectional Data Flow (StateFlow, ViewModel, Coroutines)
- **Persistence:** Jetpack DataStore Preferences for saved styles and presets
- **Target Platform:** Android 12+ (API Level 31+)

---

## 🚀 Getting Started

### 1. Download Latest APK
Grab the pre-compiled APK directly from the releases page:
- **Download:** [Wallpaper Studio Releases](https://github.com/phoenixdevellopment75-web/Material-ui-3-wallpaper-studio-/releases)

### 2. Build from Source
Ensure you have **Android Studio Ladybug (or newer)** and **JDK 17+** installed:

```bash
# Clone the repository
git clone https://github.com/phoenixdevellopment75-web/Material-ui-3-wallpaper-studio-.git
cd Material-ui-3-wallpaper-studio-

# Build debug APK
./gradlew assembleDebug

# Install directly to a connected Android device
./gradlew installDebug
```

---

## 🤝 Contributing

Contributions, feature ideas, and additional procedural math renderers are welcome!
1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/NewProceduralRenderer`)
3. Commit your Changes (`git commit -m 'feat: add voronoi cell procedural renderer'`)
4. Push to the Branch (`git push origin feature/NewProceduralRenderer`)
5. Open a Pull Request

---

## 📜 License & Author

- Developed by **[Phoenix](https://github.com/phoenixdevellopment75-web)**.
- Licensed under the **[Apache License 2.0](LICENSE)**.
