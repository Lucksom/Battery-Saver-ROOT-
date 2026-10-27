# ⚡ HyperVolt (`HyperVolt-Android-HyperOS`)

<p align="center">
  <b>Material 3 Expressive · Monet Oklab Dynamic Color Engine · 100% Volatile RAM Root Kernel & Battery Controller for Android & Xiaomi HyperOS</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Android-8.0%2B%20(API%2026%2B)-B7F397?style=for-the-badge&logo=android&logoColor=0A2006" alt="Android 8.0+" />
  <img src="https://img.shields.io/badge/Kotlin-2.0%20%2B%20Jetpack%20Compose-D8E7CB?style=for-the-badge&logo=kotlin&logoColor=151E13" alt="Kotlin + Compose" />
  <img src="https://img.shields.io/badge/Material%203-Expressive%20%2B%20Monet%20Oklab-A0CFD0?style=for-the-badge&logo=materialdesign&logoColor=002021" alt="Material 3 Expressive" />
  <img src="https://img.shields.io/badge/Root-Magisk%20%7C%20KernelSU%20%7C%20APatch-FFB4AB?style=for-the-badge&logo=linux&logoColor=690005" alt="Root Access" />
</p>

---

## ✨ Overview

**HyperVolt** (`HyperVolt-Android-HyperOS`) is a next-generation **Root Battery Saver, HyperOS Hardware Tuner, & Kernel Performance Manager** built with **Jetpack Compose** and **Material 3 Expressive** guidelines.

Every hardware optimization, CPU/GPU clock cap, task scheduler limit, and thermal control is executed **100% in volatile RAM (`/sys`, `/proc`, `/dev/cpuctl`, `/dev/cpuset`)**—never modifying `/system`, `/vendor`, or boot partitions. Any setting can be reverted in a single tap, and a simple device reboot always restores 100% factory stock state.

---

## 🎨 Material 3 Expressive UI & Monet Engine

* **Rotating Scalloped Cookie Geometry**: Custom hardware-accelerated Canvas shapes with smooth rotation and spring physics (`dampingRatio = 0.58f`, `Spring.StiffnessMedium`).
* **Oklab Perceptual Monet Palette Sheet**: Tap the top-right **Palette** button to switch between **6 Monet seed colors** (*Volt Lime*, *Hyper Cyan*, *Solar Amber*, *Coral Pulse*, *Violet Core*, *Rose Quartz*) and **Auto / Dark / Light** modes.
* **4-Tab Floating Glassmorphic Navigation Bar**:
  1. **Dashboard**: Hero Power Status card, 3-way Active Profile morphing selector, 8-Core live CPU frequency bars, Thermal & Capacity bento cards, and recent Applied Activity timeline.
  2. **Tweaks**: Profile-aware granular toggles for **Powersave**, **HyperOS Power Saver**, **Balance**, and **Performance** (*Lite / Extreme / Ultra*), plus the **Background Exception List**.
  3. **HyperOS**: Dedicated **HyperOS Hardware Tuning** tab with **Profile Templates** (*Powersave*, *Balance · Screen-Off Saver*, *Performance*), profile-sorted hardware controls, live RAM/thermal service toggles, and a **Multi-Select App Freezer** synced with the HyperOS Battery Saver exception list.
  4. **Console**: Real-time root execution log terminal with category filters (`ALL`, `TWEAK`, `PROFILE`, `REVERT`, `ERROR`).

---

## 🚀 Key Features by Tab

### 1. 🏠 Dashboard Tab
* **Hero Power Status Card**: Displays active power profile, applied tweak count, and live power state.
* **3-Way Morphing Profile Selector**: Switch between **Powersave**, **Balance**, and **Performance** with spring-animated pill geometry.
* **Live 8-Core CPU Telemetry Grid**: Real-time per-core frequencies (`policy0`, `policy2`, `policy5`, `policy7`), online/parked status, and active scaling governor pill (`walt`, `schedutil`, `powersave`, `performance`).
* **Thermal & Design Capacity Bento Row**: Live battery temperature (`°C`) with gradient thermometer bar and design capacity (`mAh`).

### 2. 🛠️ Tweaks Tab
* **HyperOS Battery Saver Sub-Engine**: Dedicated Xiaomi/HyperOS power-saving mode featuring:
  * **Disable Always-On Display (AOD)**
  * **HyperOS Aurogon App Freezer**
  * **Super Power Clean Standby**
  * **5G Modem Power Optimization**
  * **Disable HyperOS Touch Boost**
  * **Lock 60Hz Display Refresh Rate**
  * **Ultra SuperSave Black UI Mode**
  * **Background Exception List**: Searchable system & user app picker where checked apps are **pinned at the top** and synced with the HyperOS App Freezer.
* **Profile Coexistence & Auto-Flush Safety**:
  * HyperOS hardware tweaks can be applied alongside **HyperOS Power Saver** in the **Tweaks** tab.
  * Switching to standard **Powersave** (non-HyperOS), **Balance**, or **Performance** in the **Tweaks** tab automatically flushes and reverts HyperOS RAM tweaks back to their default state.

### 3. ⚡ HyperOS Tab (Kernel-Only Hardware Tuning)
* **Persistent Profile Templates**:
  * **Powersave**: Sorts sleep & idle power controls first and pre-stages low idle speeds, parked Core 6, and strict background caps.
  * **Balance · Screen-Off Saver**: Sorts balanced speed & sleep controls first, and arms **Immediate Battery Saver & Deep Freeze** the instant your screen turns off (restoring balanced speed when the screen turns on).
  * **Performance**: Sorts processor, graphics & thermal limits first and pre-stages uncapped CPU/GPU speeds with fast ramp-up.
  * **Persistent Across App Restarts**: Selected template (`Powersave`, `Balance`, `Performance`, or `Custom`) and applied tweaks are persisted in Room DB and automatically re-enforced in RAM when reopening the app.
* **Permanent RAM Enforcement**: Unlocks sysfs permissions (`chmod 0666`), writes target values across CPU/GPU/scheduler/thermal nodes, stops overriding boost services when needed, and locks critical sysfs nodes (`chmod 0444`) so background vendor daemons cannot revert your settings in RAM.
* **Select an App to Freeze (Multi-Select + Synced Exceptions)**:
  * **Select all user enabled apps** checkbox at the top (checks all user-enabled apps at once; excludes disabled & system apps).
  * Per-app **checkboxes** with **checked apps pinned at the top**.
  * **Synced Exceptions List**: Directly synced with the **HyperOS Battery Saver** exception list so protected apps stay consistent across both tabs.
  * **Batch Process Actions**: *Pause App*, *Resume App*, *Deep Freeze*, *Unfreeze*, *Lower Priority*, *Small Cores Only*, and *Allow All Cores*.

### 4. 💻 Console Tab
* **Live Root Command Log**: Timestamped history of every applied tweak, profile transition, and revert action.

---

## 🔒 Safety & Root Architecture

* **100% Volatile RAM Execution**: Writes exclusively to virtual kernel filesystems (`/sys/devices/system/cpu/...`, `/sys/class/kgsl/...`, `/dev/cpuctl/...`, `/dev/cpuset/...`, `/proc/sys/vm/...`).
* **Automatic Stock Backup**: Captures your device's stock values before applying changes so you can revert to stock at any time without rebooting.
* **46°C Hardware Thermal Safety Guard**: Automatically restarts thermal protection services if processor temperature reaches 46°C.

---

## 🛠️ Tech Stack & Build

* **Language**: 100% Kotlin
* **UI Framework**: Jetpack Compose + Material 3 Expressive + Custom Oklab Monet Engine
* **Persistence**: Jetpack Room (`TweakConfigEntity`)
* **Root Execution**: LibSU / `su -c` asynchronous coroutine shell bridge (`RootBridge` & `KernelRamLabRepository`)

```bash
# Build Debug APK
gradle :app:assembleDebug

# Run Unit Tests
gradle :app:testDebugUnitTest
```
