<div align="center">

# Material UI 3 Wallpaper Studio

**A tactile, math-driven procedural wallpaper engine built natively for Android.**

[![Release](https://img.shields.io/badge/Release-v2.6.0--beta-6366f1?style=for-the-badge&logo=github&logoColor=white)](https://github.com/phoenixdevellopment75-web/Material-ui-3-wallpaper-studio-/releases)
[![Android](https://img.shields.io/badge/Android-12%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin%202.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-Apache%202.0-F97316?style=for-the-badge)](LICENSE)

<br/>

[**⬇ Download APK**](https://github.com/phoenixdevellopment75-web/Material-ui-3-wallpaper-studio-/releases) &nbsp;·&nbsp; [Features](#-features) &nbsp;·&nbsp; [Engines](#-procedural-engines) &nbsp;·&nbsp; [Architecture](#-architecture) &nbsp;·&nbsp; [Build](#-getting-started)

</div>

---

## What is this?

Wallpaper Studio generates ultra-sharp, resolution-independent wallpapers completely on your device — no internet, no cloud, no subscriptions.

Every pattern is calculated live using pure math: trigonometric curves, Bauhaus geometry, fluid camo, and topographic contours rendered through the hardware-accelerated **Skia 2D Canvas**. The result is crisp at any resolution, on any screen size — phones, tablets, foldables.

Built from scratch with **Jetpack Compose** and **Material 3 Expressive**, it feels native, snappy, and alive.

---

## ✨ Features

| | |
|---|---|
| **⚡ 100% On-Device** | Every wallpaper is calculated locally. Zero internet, zero telemetry. |
| **🎨 Material You** | Automatically picks accent colors from your system wallpaper using Android 12 Monet. |
| **📐 Infinite Resolution** | Vector math scales perfectly across all screen sizes — no blurring, ever. |
| **🔋 Battery-Friendly** | Renders on-demand only. No background polling, no wake-locks. Runs smooth at 60/120 FPS. |
| **🖌️ Interactive Studio** | Touch, drag, scale, and rotate shapes, text, and layers directly on the canvas. |
| **🌊 25+ Generators** | A wide library of algorithmic patterns — from calm topographic maps to bold Bauhaus grids. |

---

## 🔮 Procedural Engines

25+ algorithmic generators, each producing geometry from math formulas:

| Style | Engines | What it looks like |
|:---|:---|:---|
| **Geometric & Bauhaus** | `BauhausGrid`, `BauhausSemicircle`, `ModularBauhaus`, `MidCenturyGrid`, `Grids` | Bold minimalist shapes inspired by modernist design |
| **Organic & Fluid** | `LavaBlob`, `LiquidCamo`, `FlatLiquidCamo`, `HarmonicWave` | Smooth Bézier flows and soothing wave patterns |
| **Architectural** | `FlutedGlass`, `FlutedArches`, `NestedArches`, `PaperRidges` | Ribbed textures and depth that simulate real materials |
| **Terrain** | `Topographic`, `Mountain`, `AtmosphericMountain` | Contour maps and layered silhouettes |
| **Patterns & Texture** | `DotGrid`, `PolarLabyrinth`, `CrossCurves`, `Slices`, `StackedPills`, `FilmGrain` | Optical precision grids, analog grain, and geometric illusions |

---

## 🛠️ Architecture

```
app/src/main/java/com/example/
│
├── engine/              ← 25+ Skia canvas render engines
│   ├── ProceduralRenderer.kt
│   ├── Material3ShapeLibrary.kt
│   └── WallpaperParams.kt
│
├── ui/                  ← Jetpack Compose screens & animations
│   ├── CustomStudioCanvas.kt     (multi-touch interactive canvas)
│   ├── ExpressiveBottomDock.kt   (M3 floating action dock)
│   ├── MotionPhysics.kt          (spring damping specs)
│   └── WallpaperViewModel.kt     (state machine & params)
│
├── palette/             ← Monet color extraction & custom palette builder
├── export/              ← High-resolution PNG exporter
└── math/                ← Trig utilities & normalization helpers
```

**Tech stack at a glance:**

- **UI:** Jetpack Compose 1.6+ · Material Design 3 Expressive
- **Graphics:** Hardware-accelerated Skia 2D (`drawPath`, `drawVertices`, offscreen layers)
- **Architecture:** MVVM · StateFlow · ViewModel · Coroutines
- **Storage:** Jetpack DataStore (saved styles & presets)
- **Min SDK:** Android 12 (API 31+)

---

## 🚀 Getting Started

### Download the APK

Grab the latest pre-built release directly:

> **[→ Wallpaper Studio Releases](https://github.com/phoenixdevellopment75-web/Material-ui-3-wallpaper-studio-/releases)**

### Build from Source

You need **Android Studio Ladybug** (or newer) and **JDK 17+**.

```bash
# Clone the repo
git clone https://github.com/phoenixdevellopment75-web/Material-ui-3-wallpaper-studio-.git
cd Material-ui-3-wallpaper-studio-

# Build a debug APK
./gradlew assembleDebug

# Or install directly to a connected device
./gradlew installDebug
```

---

## 🤝 Contributing

Got a cool new procedural renderer idea? Pull requests are welcome.

1. Fork this repo
2. Create a branch — `git checkout -b feature/your-renderer-name`
3. Commit your changes — `git commit -m "feat: add voronoi cell renderer"`
4. Push and open a Pull Request

---

## 📜 License & Author

Made with care by **[Phoenix](https://github.com/phoenixdevellopment75-web)** — a 16-year-old Android developer.

Licensed under the **[Apache License 2.0](LICENSE)**.
