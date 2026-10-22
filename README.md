# VoltPower ⚡

> **Material 3 Expressive Root Battery Saver & Kernel Performance Manager**  
> Built with Jetpack Compose, dynamic OKLCH Monet color palettes, expressive SVG geometry, and safe volatile RAM kernel optimization.

---

## 📱 Material 3 Expressive Interface Preview

<div align="center">
  <table>
    <tr>
      <td align="center" width="33%">
        <b>Home &amp; Expressive Hero</b><br/>
        <img src="docs/screenshot_dashboard.svg" width="230" alt="VoltPower Expressive Dashboard" />
      </td>
      <td align="center" width="33%">
        <b>CPU Clusters &amp; Thermal</b><br/>
        <img src="docs/screenshot_powersave.svg" width="230" alt="CPU Clusters and Thermal Engine" />
      </td>
      <td align="center" width="33%">
        <b>System Tweaks &amp; Root Console</b><br/>
        <img src="docs/screenshot_advanced.svg" width="230" alt="System Tweaks and Root Console" />
      </td>
    </tr>
  </table>
</div>

---

## ✨ Material 3 Expressive Design System

- **Rotating Scalloped Cookie Orb & Logo**: Custom 200×200 SVG scalloped cookie polygons (`COOKIE_SVG_PATH_DATA` & `HERO_BG1_SVG_PATH_DATA`) with continuous smooth rotation and tap-to-pause physics.
- **Quadratic Bezier Wave Progress Bar**: Animated sinusoidal wave battery indicator (`M 0 16 Q ... T ...`) with phase shifting and dynamic thumb tracking.
- **OKLCH Dynamic Monet Engine**: Real-time perceptual OKLCH palette generator supporting 6 expressive seed accents (Emerald, Ocean, Violet, Amber, Rose, Cyan), System Android 12+ dynamic wallpaper colors, Dark/Light mode, and High-Contrast mode.
- **5-Tab Floating Pill Navigation Bar**: Elevated capsule navigation bar with spring-animated indicator pills across **Home**, **CPU**, **Apps**, **System**, and **Logs**.

---

## 🚀 Core Features

### 📊 Live Hardware Telemetry & Hero Card
- **Expressive Battery Hero Card**: Displays live current flow in `mA`, charging/discharging state, estimated runtime, voltage, battery health percentage, and cycle count inside an animated Material 3 Expressive container.
- **Quick Profiles (2×2 Grid)**: One-tap morphing profile cards for **Eco** (`Powersave`), **Balanced** (`Daily driver`), **Performance** (`Fast UI`), and **Gaming** (`Max clocks`).
- **Live Telemetry (2×2 Grid)**: Instantaneous **Temperature**, **CPU Frequency**, **RAM & ZRAM**, and **Active Governor / Deep Sleep** metrics.

### ⚙️ CPU Clusters, Governor & Thermal Tuning (`CPU` Tab)
- **Interactive Governor Selector**: Switch live between `schedutil`, `powersave`, `performance`, and `walt` governors.
- **Per-Cluster Frequency Sliders**: Independent frequency ceiling controls for Little Cluster (Cores 0–3) and Big/Prime Cluster (Cores 4–7).
- **Dynamic Core Topology**: Interactive core online/offline toggles (`C0`–`C7`) and automatic low-battery core parking.
- **Thermal & Boost Controls**: Toggle Input Touch Boost suppression, Adreno GPU power throttling & Idler, Schedutil rate limiting, and Power-Efficient Workqueues.

### ❄️ App Freezer & Whitelist (`Apps` Tab)
- **Searchable Package Manager**: Filter installed user and system packages with real-time search.
- **Background App Suspension**: Freeze background-draining apps or exempt critical messaging and navigation apps from Doze and HyperOS cleaner sweeps.

### 🛠️ System, Display & I/O Tweaks (`System` Tab)
- **Universal Display Refresh Rate Lock**: Force `60 Hz`, `90 Hz`, `120 Hz`, or `Dynamic` refresh rate across AOSP, MIUI, and HyperOS.
- **HyperOS / MIUI Super Power Engine**: Toggle Control Center Gaussian blur removal (`disable_window_blurs 1`), AOD suppression, 5G SA/NSA modem power saving, and Aurogon freezer integration.
- **Storage, VM & Network Tuning**: Configure I/O scheduler read-ahead queues, Dynamic Fsync, VM dirty writeback intervals, TCP BBR congestion control, and GMS Doze enforcement.

### 💻 Real-Time Root Console (`Logs` Tab)
- **Live Execution Stream**: Inspect every `su` command, sysfs write, applied parameter value, and timestamp in real time.
- **Immutable Stock Backup & Safe Revert**: Automatically snapshots stock kernel nodes to `/sdcard/Download/Stock_Kernel_Backup.sh` before applying volatile RAM tweaks, allowing one-tap restoration at any time.

---

## 📂 Project Architecture (`com.voltpower`)

```text
app/src/main/java/com/voltpower/
├── MainActivity.kt                  # Edge-to-edge Compose host activity
├── data/
│   ├── apps/AppInfoRepository.kt    # Installed package & whitelist discovery
│   ├── db/                          # Room persistence (AppDatabase, DAO, TweakConfigEntity)
│   ├── log/LogRepository.kt         # Real-time root command & tweak execution log stream
│   ├── root/RootBridge.kt           # Root shell engine, sysfs reader & stock backup manager
│   └── telemetry/TelemetryRepository.kt # BatteryManager, CPU core & thermal sysfs polling
└── ui/
    ├── MainScreen.kt                # Material 3 Expressive UI, OKLCH Monet engine & animations
    ├── MainViewModel.kt             # Reactive state management & profile orchestration
    └── theme/                       # Typography (Space Grotesk, Plus Jakarta Sans, JetBrains Mono) & M3 Theme
```

---

## 🛠️ Root Requirements & Automated Releases

- Compatible with **Magisk**, **KernelSU**, and **APatch** (`uid=0` root shell), with graceful non-root telemetry fallback.
- GitHub Actions workflow (`.github/workflows/build-apk.yml`) automatically builds signed Debug and Release APKs and publishes a new incremented release tag (`v1.0.x`) on every push to `main` or `master`.

---

## 💖 Acknowledgements & Credits

Special credit to **[Miuix](https://github.com/compose-miuix-ui/miuix)** (`compose-miuix-ui/miuix`) and **Material 3 Expressive** design principles for inspiring the component styling and expressive motion architecture used in this project.
