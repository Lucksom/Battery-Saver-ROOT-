# VoltPower ⚡

> **Comprehensive Android Optimizer & Hardware Performance Manager**  
> Engineered with MIUIX design principles, root-level performance controls, and intelligent system resource optimization.

---

## 📱 Interface Preview

<div align="center">
  <table>
    <tr>
      <td align="center" width="33%">
        <b>Dashboard &amp; Telemetry</b><br/>
        <img src="docs/screenshot_dashboard.svg" width="180" alt="VoltPower Dashboard" />
      </td>
      <td align="center" width="33%">
        <b>System &amp; Display Tweaks</b><br/>
        <img src="docs/screenshot_powersave.svg" width="180" alt="System Tweaks" />
      </td>
      <td align="center" width="33%">
        <b>Kernel &amp; Whitelist</b><br/>
        <img src="docs/screenshot_advanced.svg" width="180" alt="Kernel &amp; Whitelist" />
      </td>
    </tr>
  </table>
</div>

---

## 🚀 Features Overview

### 📊 Real-Time Hardware Telemetry & Diagnostics
- **Dynamic Battery Arc**: Live percentage indicator with real-time gradient gauge.
- **Charging & Discharging Speed**: Instantaneous power telemetry in mA and Watts with speed rating badges.
- **Bus Voltage & Voltage Metering**: Total millivolt bus voltage monitoring.
- **Thermal Monitor**: Real-time battery and hardware thermal monitoring with safety zone indicators.
- **Octa-Core CPU Telemetry**: Live frequencies and utilization meters across Little, Mid, and Prime cores.
- **GPU & Memory Status**: Real-time Adreno GPU frequency states and active ZRAM compression status.

### ⚡ System & Display Optimization
- **Universal Refresh Rate Lock**: Force and lock display refresh rates at 60Hz, 90Hz, 120Hz, or Dynamic mode without OS overrides.
- **Remove Control Center Blur**: Remove real-time background Gaussian blur on HyperOS / MIUI Control Center to eliminate GPU overhead and save battery (`disable_window_blurs 1`).
- **Theme Preservation & Monet**: Retain preferred light or dark themes and Monet wallpaper palette integration with Material 3.0.
- **Sensor & Radio Power Management**: Throttle unnecessary background Bluetooth and Wi-Fi scanning during standby.
- **Modern Radio Optimization**: Intelligent 5G SA and NSA modem sleep when high data bandwidth is not needed.

### ⚙️ Kernel, CPU & Memory Tuning
- **CPU Governor Rate Limiting**: Stabilize governor frequency ramp-ups to prevent micro-stutters and unnecessary power spikes.
- **Touch Boost Clamping**: Prevent excessive multi-core CPU clock bursts on minor touch gestures for cooler operation.
- **Dynamic Storage FSync**: In-memory write buffer acceleration during active screen usage with automated safe disk flushing on screen-off.
- **Storage I/O Scheduler**: Optimized queue depths and scheduler policies for ultra-fast application launching.
- **ZRAM & Memory Management**: Enhanced swap compression algorithms and memory watermarks to keep background apps responsive.

### 🛡️ Background Management & App Whitelist
- **Background Task Suspension**: Deep application freezing that eliminates rogue background CPU drain while preserving system responsiveness.
- **Automated Deep Sleep**: Triggers instant power-saving sleep states the moment the display turns off.
- **Touch Digitizer Idle Sleep**: Engages hardware touch controller low-power sleep while the screen is inactive.
- **App Exemption Whitelist**: Ensure critical messaging, alarms, and navigation apps remain active with prioritized pinned selection.

### 🎯 Profiles & Live Execution Logs
- **Instant Profile Switcher**: One-tap switching between **Powersave**, **Balanced**, **Performance**, and **Custom** presets.
- **Master Optimization Switch**: Instantly toggle all optimization engines in memory without altering baseline configurations.
- **Live Execution Inspector**: Real-time unified live tweak execution card on Dashboard with exact tweak names, applied values, execution status, and timestamps.
- **Stock Settings Restore**: Safe one-tap revert to original system defaults at any time.

---

## 🛠️ Root Requirements

VoltPower functions with root access:
- Compatible with **Magisk**, **KernelSU**, and **APatch**.
- Automatically detects root privileges with real-time status indicators and non-root monitoring fallback.

---

## 💖 Acknowledgements & Credits

Special credit to the **[Miuix](https://github.com/compose-miuix-ui/miuix)** project (`compose-miuix-ui/miuix`) for providing the design principles and UI component styling concepts used throughout this application.

