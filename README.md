# ✦ Wallpaper Studio

> A tactile, math-driven procedural wallpaper engine and interactive canvas studio built natively for Android using Jetpack Compose and Material 3 Expressive.

[![Release](https://img.shields.io/badge/Release-v2.4.1--beta-blue.svg)](https://github.com/phoenixdevellopment75-web/Material-ui-3-wallpaper-studio-/releases/tag/Wallpaper-Studio-betav2.4.1)
[![Platform](https://img.shields.io/badge/Platform-Android%2012%2B-green.svg)](https://android.com)
[![License](https://img.shields.io/badge/License-Apache%202.0-orange.svg)](LICENSE)

---

## 🎨 Overview

**Wallpaper Studio** delivers resolution-independent, vector-based wallpapers generated 100% on-device. Instead of loading remote raster images or relying on slow cloud AI image models, Wallpaper Studio calculates geometric forms directly on hardware-accelerated Skia 2D Canvas. It is optimized to stay lightweight, battery-friendly, and responsive on devices with 4GB RAM.

---

## 📱 Features

### 📐 Procedural Vector Engines
* **Bauhaus Semicircles:** Disciplined $4 \times 7$ grid layout rendering crisp semicircles, quadrant arcs, and solid disks.
* **Fluted Labyrinth Arches:** Concentric, non-intersecting quarter-arcs with tangent architectural line tracks.
* **Harmonic Waves & Ridges:** Smooth parametric S-curves and atmospheric mountain contours.
* **Topographic Contours:** Organic elevation maps with customizable stroke weights.
* **Liquid Camo / Lava:** Flowing cubic Bézier ribbons with continuous tangent curves.

### 🛠 Freeform Studio Canvas
* **Material 3 Geometry Primitives:** Place and manipulate dynamic tokens including Clovers, Sunburst badges, Semicircles, and Squircles via `androidx.graphics.shapes`.
* **Shape Transformation:** Touch-driven 360° rotation, proportion-locked scaling, and layer reordering.
* **Shape Tuning & Optics:** Dedicated sliders for shape opacity (0% to 100%), elevation drop shadows, and soft-edge glow.
* **Auto-Arrange:** Procedurally generate balanced, non-overlapping shape layouts with a single tap while keeping every shape editable.
* **Tactile Floating Dock:** Ergonomic 4-capsule bottom navigation with fluid spring physics inspired by PixelPlayer.

### 🌗 System & Visual Integration
* **Material You Dynamic Theming:** Automatically derives harmonious HCT tonal palettes from your system wallpaper (Android 12+ Monet).
* **Manual Theme Mode:** Seamlessly switch between System Default, Pure Light, and Deep Dark modes.
* **Adaptive Themed Icon:** Native 4-point astroid star token with Android 13+ monochrome tinting support.
* **Tactile Return Motion:** Nested push/pop spring transitions with depth scaling.
* **Optional AI Color Synthesis:** 100% offline and private by default, with an optional BYOK (Bring-Your-Own-Key) toggle for Gemini, OpenAI, or OpenRouter palette generation.

---

## ⚠️ Beta v2.4.1 Status & Known Issues

This project is in active beta testing. While the core UI and rendering pipeline are fully operational, the following items are currently being refined:

* **Generator Palettes:** A few procedural engines (Lava and Fluted Arches) may occasionally default to baseline tones; full real-time palette reactivity is being finalized.
* **Studio Slider Tuning:** Opacity and soft-edge glow values on complex multi-segment shapes may exhibit slight visual clipping under certain Android GPU drivers.
* **In-App Blur Settings:** Experimental runtime blur filters have been temporarily replaced with solid, high-contrast Material 3 opaque surfaces to prevent UI ghosting.

---

## 📲 Quick Install

Download the latest pre-compiled build directly from GitHub:

1. Go to the [Beta v2.4.1 Release Page](https://github.com/phoenixdevellopment75-web/Material-ui-3-wallpaper-studio-/releases/tag/Wallpaper-Studio-betav2.4.1).
2. Download `app-debug.apk` (or `app-release.apk`).
3. Install and run on any device running **Android 12 (API 31)** or higher.

---

## 🏗 Building Locally

### Prerequisites
* **Android Studio:** Ladybug (2024.2.1) or newer
* **JDK:** Version 21 (Temurin or JetBrains Runtime)
* **Gradle:** 8.8+ / Android Gradle Plugin 8.8+

### Setup Commands
```bash
# Clone the repository
git clone [https://github.com/phoenixdevellopment75-web/Material-ui-3-wallpaper-studio-.git](https://github.com/phoenixdevellopment75-web/Material-ui-3-wallpaper-studio-.git)
cd Material-ui-3-wallpaper-studio-

# Make gradlew executable
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug
