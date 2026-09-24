<div align="center">

# ✦ Wallpaper Studio

**A tactile, math-driven procedural wallpaper engine and interactive canvas studio built natively for Android.**

[![Release](https://img.shields.io/badge/Release-v2.6.0--beta-6366f1?style=for-the-badge&logo=github&logoColor=white)](https://github.com/phoenixdevellopment75-web/Material-ui-3-wallpaper-studio-/releases/tag/Wallpaper-Studio-betav2.6.0)
[![Android](https://img.shields.io/badge/Android-12%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Compose%20%26%20M3E-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-Apache%202.0-F97316?style=for-the-badge)](LICENSE)

<p align="center">
  <b>100% On-Device Math</b> • <b>Zero Remote Latency</b> • <b>Battery Efficient</b> • <b>Dynamic Theming</b>
</p>

---

</div>

## 🎨 Overview

**Wallpaper Studio** delivers ultra-sharp, resolution-independent vector wallpapers generated completely on-device. 

Bypassing remote asset fetching, bloated image caches, and slow cloud models, Wallpaper Studio computes complex trigonometric geometries and fluid dynamics directly on hardware-accelerated **Skia 2D Canvas**. The entire pipeline is heavily profiled to maintain smooth 60/120 FPS performance and a tiny memory footprint—running seamlessly even on modest 4GB RAM hardware.

---

## ✨ Key Highlights

- **⚡ Hardware-Accelerated Math:** Real-time procedural rendering via Skia graphics pipelines with zero network overhead.
- **🪄 Material 3 Expressive UI:** Built with modern Jetpack Compose foundations, tactile spring physics, and dynamic color extraction (`Monet`).
- **📐 Infinite Resolution:** Vector and formula-based rendering guarantees zero pixelation on any screen density or aspect ratio.
- **🔋 Battery-Conscious Engine:** Generates on-demand without background wake-locks or unnecessary CPU polling.

---

## 🛠️ Tech Stack & Architecture

- **UI Layer:** Jetpack Compose, Material Design 3 Expressive, Compose Animation primitives.
- **Graphics Pipeline:** Android Canvas (`drawPath`, `drawVertices`), hardware Skia shading.
- **Architecture:** Modern Android Architecture (MVVM / MVI) with unidirectional data flow (UDF).
- **Target SDK:** Android 12+ (API Level 31 and above).

---

## 🚀 Getting Started

### Download APK
Grab the latest build directly from GitHub Releases:
- **Latest Release:** [Wallpaper Studio v2.6.0-beta](https://github.com/phoenixdevellopment75-web/Material-ui-3-wallpaper-studio-/releases/tag/Wallpaper-Studio-betav2.6.0)

### Clone & Build Locally
```bash
git clone [https://github.com/phoenixdevellopment75-web/Material-ui-3-wallpaper-studio-.git](https://github.com/phoenixdevellopment75-web/Material-ui-3-wallpaper-studio-.git)
cd Material-ui-3-wallpaper-studio-
./gradlew assembleDebug
