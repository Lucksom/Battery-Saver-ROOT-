# VoltPower ⚡

> **Material 3 Expressive Root Battery Saver & Kernel Performance Manager**  
> Built with Jetpack Compose, dynamic OKLCH Monet v2 20-role color palettes, expressive SVG scalloped geometry, and 100% volatile RAM kernel optimization.

---

## 📱 Material 3 Expressive Interface Preview

<div align="center">
  <table>
    <tr>
      <td align="center" width="33%">
        <b>Overview &amp; Expressive Hero</b><br/>
        <img src="docs/screenshot_dashboard.svg" width="230" alt="VoltPower Expressive Overview" />
      </td>
      <td align="center" width="33%">
        <b>Profile Tweaks &amp; HyperOS</b><br/>
        <img src="docs/screenshot_powersave.svg" width="230" alt="VoltPower Profile Tweaks" />
      </td>
      <td align="center" width="33%">
        <b>Sysfs Stream &amp; Root Console</b><br/>
        <img src="docs/screenshot_advanced.svg" width="230" alt="VoltPower Sysfs Stream Console" />
      </td>
    </tr>
  </table>
</div>

---

## ✨ Material 3 Expressive Design System

- **Rotating Scalloped Cookie Orb & Logo**: Custom 200×200 SVG scalloped cookie polygons (`COOKIE_SVG_PATH_DATA` & `BG1_SVG_PATH_DATA`) with continuous smooth rotation and tap-to-pause physics.
- **Quadratic Bezier Wave Progress Bar**: Animated sinusoidal wave battery indicator (`M 0 7 Q 6 0 12 7 T 24 7 ...`) with continuous phase shifting.
- **OKLCH Monet v2 Engine (1 Hue → 20 Roles)**: Real-time perceptual OKLCH palette generator supporting 6 conic-swatch seed presets (`Wallpaper`, `Mint`, `Iris`, `Ocean`, `Citrus`, `Rose`), a 7-block live tonal strip, and 3 theme modes (`System`, `Light`, `Dark`).
- **3-Tab Floating Glassmorphic Pill Navigation**: Elevated capsule navigation bar with spring-animated indicator pills across **Overview**, **Tweaks**, and **Console**.

---

## 🚀 Core Features

### 📊 Tab 1: Overview (`Overview` Tab)
- **Expressive Battery & RAM Engine Hero Card**: Displays live charging/discharging wattage (`W`), battery percentage inside a rotating scalloped orb, estimated time to full, `VOLT` / `AMPS` / `TEMP` mini telemetry pills, and a one-tap **RAM Engine** master pill toggle.
- **Connected Power Profile Selector**: Morphing 3-profile selector (**Powersave**, **Balanced**, **Performance**) with an expandable **Governor Tier** segmented bar (`Lite`, `Extreme`, `Ultra`) when Performance is active.
- **Bento Telemetry Grid**:
  - **Octa-Core Live Usage Equalizer**: 8-bar real-time per-core frequency usage visualizer (`C0`–`C7`) with active governor and cluster clock pills (strictly gated by root access).
  - **2×2 Hardware Bento**: Live **GPU** frequency & load (`MHz`), internal **Storage** used/total (`GB` & `%`), **Thermal** sensor with gradient thermometer bar (`°C`), and battery design **Capacity** (`mAh`).
- **Applied Activity Timeline**: Real-time summary of recently applied tweaks with one-tap navigation to the full console.

### 🎛️ Tab 2: Profile-Connected Tweaks (`Tweaks` Tab)
- **Real-Time Search & Profile Switcher**: Instant search filtering across active profile tweaks and direct switching between **Powersave**, **Balance**, and **Performance** tweak sets.
- **Powersave & Unified HyperOS Battery Saver**:
  - Standard Powersave CPU & Adreno throttling, low-battery big-core parking slider (`5%`–`60%`), Doze & standby workqueues, and UFS I/O batching.
  - **HyperOS Battery Saver Master Toggle**: Switches to dedicated Xiaomi/HyperOS power saving controls (PowerKeeper SuperSave, Aurogon process freezer, SuperPowerClean RAM purge, AOD suppression, Xiaomi THP touch doze, 5G modem power saving, Control Center blur removal, and AMOLED dark mode).
- **Balance Profile Tweaks**: Adjustable CPU peak frequency cap slider (`50%`–`90%`), asymmetric Schedutil rate limits, CFS scheduler tuning, 3-tier RAM/ZRAM scaling, adaptive storage I/O, TCP BBR congestion control, screen-off deep sleep daemon, and cached process limit.
- **Performance Mode Tiers (`Lite` · `Extreme` · `Ultra`)**: Connected tier selector with dynamic governor & clock locks, all-8-cores online enforcement, SchedTune/uclamp thread boosting, AdrenoBoost & force no-nap, DDR bus bandwidth locking, and 512KB multi-queue storage tuning.
- **Floating Revert Stock FAB**: One-tap restoration of all factory kernel parameters from the immutable stock backup snapshot.

### 💻 Tab 3: Sysfs Stream & Terminal (`Console` Tab)
- **Live Execution Stream**: Clean, human-readable log stream of applied profile and tweak actions with timestamps and a blinking terminal prompt (`❯ ▊`).
- **Immutable Stock Backup & Safe Revert**: Automatically snapshots stock kernel nodes to `/sdcard/Download/Stock_Kernel_Backup.sh` before applying volatile RAM tweaks.

---

## 📂 Project Architecture (`com.voltpower`)

```text
app/src/main/java/com/voltpower/
├── MainActivity.kt                        # Edge-to-edge Compose host activity
├── data/
│   ├── apps/AppInfoRepository.kt          # Installed package & permanent system protection discovery
│   ├── db/                                # Room persistence (AppDatabase, TweakConfigDao, TweakConfigEntity)
│   ├── log/LogRepository.kt               # Real-time human-readable tweak execution log stream
│   ├── root/RootBridge.kt                 # Root shell engine, sysfs batch reader & stock backup manager
│   └── telemetry/TelemetryRepository.kt   # BatteryManager, 8-core CPU, GPU, thermal & StorageStats polling
└── ui/
    ├── MainScreen.kt                      # Material 3 Expressive UI, OKLCH Monet v2 engine & 3-tab layout
    ├── MainViewModel.kt                   # Reactive state management & profile/tier orchestration
    └── theme/                             # Bundled Google Fonts (Space Grotesk, Plus Jakarta Sans, JetBrains Mono)
```

---

## 🛠️ Root Requirements & Automated Releases

- Compatible with **Magisk**, **KernelSU**, and **APatch** (`uid=0` root shell), with graceful read-only non-root telemetry fallback.
- GitHub Actions workflow (`.github/workflows/build-apk.yml`) automatically builds signed Release APKs and publishes incremented release tags (`v1.0.x`) on push to `main` or `master`.

