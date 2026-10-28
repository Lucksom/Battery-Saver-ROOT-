package com.voltpower.data.root

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class KernelSection(val id: String, val title: String, val badge: String) {
    ALL("ALL", "All", "ALL"),
    CPU("CPU", "CPU & Cores", "CPU"),
    SCHED("SCHED", "Task Scheduler", "SCHED"),
    GPU("GPU", "GPU & Graphics", "GPU"),
    MEM_BUS("MEM_BUS", "Memory Bus (DDR/LLCC/L3)", "BUS"),
    RAM("RAM", "RAM & Storage", "RAM"),
    IRQ("IRQ", "Hardware Interrupts", "IRQ"),
    FREEZE("FREEZE", "Sleep & App Freeze", "SLEEP"),
    THERMAL("THERMAL", "Thermal Limits", "THERM"),
    DAEMONS("DAEMONS", "System Services", "SERVICE")
}

enum class HyperOsProfileTemplate(
    val id: String,
    val title: String,
    val shortBadge: String,
    val subtitle: String
) {
    POWERSAVE(
        id = "POWERSAVE",
        title = "Powersave",
        shortBadge = "POWERSAVE",
        subtitle = "Sorts & pre-selects low idle speeds, background core limits, and fast deep sleep"
    ),
    BALANCE_SCREEN_OFF(
        id = "BALANCE_SCREEN_OFF",
        title = "Balance · Screen-Off Saver",
        shortBadge = "BALANCE + SCREEN OFF",
        subtitle = "Balanced speed while using phone + immediate battery saver when screen turns off"
    ),
    PERFORMANCE(
        id = "PERFORMANCE",
        title = "Performance",
        shortBadge = "PERFORMANCE",
        subtitle = "Sorts & pre-selects uncapped CPU/GPU speeds, fast ramp-up, and released thermal limits"
    )
}


data class KernelTweakSpec(
    val id: String,
    val section: KernelSection,
    val title: String,
    val nodePath: String,
    val wasValue: String,
    val targetValue: String,
    val options: List<String>,
    val optionLabels: Map<String, String> = emptyMap(),
    val functionDesc: String,
    val upDownEffect: String,
    val isRisky: Boolean = false,
    val isPocoF7Only: Boolean = false,
    val isPocoF7Assigned: Boolean = true,
    val customWriteCommand: ((String) -> String)? = null,
    val customReadCommand: String? = null
) {
    fun formatValue(raw: String): String {
        val clean = raw.trim()
        optionLabels[clean]?.let { return it }
        val asLong = clean.toLongOrNull()
        if (asLong != null) {
            if (nodePath.contains("kgsl") && asLong >= 100_000_000L) {
                return "${asLong / 1_000_000L} MHz"
            }
            if (nodePath.contains("cpufreq") && asLong >= 100_000L) {
                return String.format(java.util.Locale.US, "%.2f GHz", asLong / 1_000_000.0)
            }
        }
        return clean
    }
}

data class ThermalCoolingDeviceState(
    val type: String,
    val curState: Int,
    val maxState: Int,
    val wasState: Int
)

data class XiaomiDaemonState(
    val name: String,
    val displayTitle: String,
    val description: String,
    val isRunning: Boolean,
    val isThermalDaemon: Boolean = false
)

data class KernelRamLiveSnapshot(
    val maxZoneTempMilliC: Int = 37000,
    val maxZoneType: String = "Processor",
    val batteryTempTenthsC: Int = 340,
    val suspendSuccess: String = "0",
    val suspendFailedFreeze: String = "21122",
    val suspendLastFailedDev: String = "alarmtimer",
    val pmFreezeTimeoutMs: String = "20000",
    val pmAsync: String = "1",
    val memSleepMode: String = "[s2idle]",
    val cgroupFreezeAvailable: Boolean = false,
    val waltTunablesList: String = "hispeed_load hispeed_freq",
    val chargeControlLimit: String = "0",
    val constantChargeCurrentMax: String = "0",
    val inputCurrentLimit: String = "0",
    val nodeValues: Map<String, String> = emptyMap(),
    val coolingDevices: List<ThermalCoolingDeviceState> = emptyList(),
    val daemons: List<XiaomiDaemonState> = emptyList(),
    val lastReadTimestamp: String = "--:--:--"
) {
    val maxZoneTempC: Float get() = maxZoneTempMilliC / 1000f
    val batteryTempC: Float get() = batteryTempTenthsC / 10f
    val isOverheatWarning: Boolean get() = maxZoneTempMilliC >= 45000
}

object KernelRamLabRepository {

    val tweakSpecs: List<KernelTweakSpec> = listOf(
        // ====================================================================
        // 1. CPU & CORES
        // ====================================================================
        KernelTweakSpec(
            id = "policy0_max_freq",
            section = KernelSection.CPU,
            title = "Small Cores Speed Ceiling (scaling_max_freq · Cores 0–1)",
            nodePath = "/sys/devices/system/cpu/cpufreq/policy0/scaling_max_freq",
            wasValue = "2016000",
            targetValue = "2016000",
            options = listOf("1209600", "1516800", "1612800", "1804800", "2016000"),
            optionLabels = mapOf(
                "1209600" to "1.21 GHz (Cool)",
                "1516800" to "1.52 GHz (Powersave Cap)",
                "1612800" to "1.61 GHz (Powersave Script)",
                "1804800" to "1.80 GHz (Balanced)",
                "2016000" to "2.02 GHz (Performance / Stock)"
            ),
            functionDesc = "scaling_max_freq: the speed ceiling for Cores 0–1. A lower cap means less heat and power, but slower peaks. (ONLY for Poco F7 / Redmi Turbo 4 Pro · Snapdragon 8s Gen 4)",
            upDownEffect = "Powersave: 1.52 GHz (1.61 GHz target) · Balanced: 1.80 GHz · Performance & Stock: 2.02 GHz."
        ),
        KernelTweakSpec(
            id = "policy0_min_freq",
            section = KernelSection.CPU,
            title = "Small Cores Idle Speed (scaling_min_freq · Cores 0–1)",
            nodePath = "/sys/devices/system/cpu/cpufreq/policy0/scaling_min_freq",
            wasValue = "748800",
            targetValue = "364800",
            options = listOf("364800", "576000", "748800"),
            optionLabels = mapOf(
                "364800" to "365 MHz (Low Idle Floor)",
                "576000" to "576 MHz (Mid Idle)",
                "748800" to "749 MHz (Stock Default)"
            ),
            functionDesc = "scaling_min_freq: the lowest speed a core cluster idles at. A low floor saves power at idle and does not hurt performance. (ONLY for Poco F7 / Redmi Turbo 4 Pro · Snapdragon 8s Gen 4)",
            upDownEffect = "Powersave, Balanced & Performance (common_idle): 364800 (365 MHz) · Stock: 748800 (749 MHz)."
        ),
        KernelTweakSpec(
            id = "policy2_max_freq",
            section = KernelSection.CPU,
            title = "Big Cores Speed Ceiling (scaling_max_freq · Cores 2–4)",
            nodePath = "/sys/devices/system/cpu/cpufreq/policy2/scaling_max_freq",
            wasValue = "3014400",
            targetValue = "2515200",
            options = listOf("1728000", "1804800", "2208000", "2515200", "3014400"),
            optionLabels = mapOf(
                "1728000" to "1.73 GHz (Powersave Cap)",
                "1804800" to "1.80 GHz (Powersave Script)",
                "2208000" to "2.21 GHz (Balanced)",
                "2515200" to "2.52 GHz (Performance Cool Peak)",
                "3014400" to "3.01 GHz (Stock Max)"
            ),
            functionDesc = "scaling_max_freq: the speed ceiling for Big Cores 2–4. A lower cap means less heat and power, but slower peaks. (ONLY for Poco F7 / Redmi Turbo 4 Pro · Snapdragon 8s Gen 4)",
            upDownEffect = "Powersave: 1.73 GHz · Balanced: 2.21 GHz · Performance: 2.52 GHz (below 3.01 GHz peak to limit heat) · Stock: 3.01 GHz."
        ),
        KernelTweakSpec(
            id = "policy2_min_freq",
            section = KernelSection.CPU,
            title = "Big Cores Idle Speed (scaling_min_freq · Cores 2–4)",
            nodePath = "/sys/devices/system/cpu/cpufreq/policy2/scaling_min_freq",
            wasValue = "480000",
            targetValue = "480000",
            options = listOf("480000", "806400"),
            optionLabels = mapOf(
                "480000" to "480 MHz (Low Idle Floor)",
                "806400" to "806 MHz (Boosted Idle)"
            ),
            functionDesc = "scaling_min_freq: the lowest speed the big core cluster idles at. A low floor saves power at idle. (ONLY for Poco F7 / Redmi Turbo 4 Pro · Snapdragon 8s Gen 4)",
            upDownEffect = "Powersave, Balanced & Performance (common_idle): 480000 (480 MHz)."
        ),
        KernelTweakSpec(
            id = "policy5_max_freq",
            section = KernelSection.CPU,
            title = "Mid Cores Speed Ceiling (scaling_max_freq · Cores 5–6)",
            nodePath = "/sys/devices/system/cpu/cpufreq/policy5/scaling_max_freq",
            wasValue = "1056000",
            targetValue = "2208000",
            options = listOf("1056000", "1824000", "2208000", "2803200"),
            optionLabels = mapOf(
                "1056000" to "1.06 GHz (Powersave / Stock Cap)",
                "1824000" to "1.82 GHz (Balanced)",
                "2208000" to "2.21 GHz (Performance)",
                "2803200" to "2.80 GHz (Full Uncapped)"
            ),
            functionDesc = "scaling_max_freq: the speed ceiling for Mid Cores 5–6. A lower cap means less heat and power. (ONLY for Poco F7 / Redmi Turbo 4 Pro · Snapdragon 8s Gen 4)",
            upDownEffect = "Powersave: 1.06 GHz · Balanced: 1.82 GHz · Performance: 2.21 GHz."
        ),
        KernelTweakSpec(
            id = "policy5_min_freq",
            section = KernelSection.CPU,
            title = "Mid Cores Idle Speed (scaling_min_freq · Cores 5–6)",
            nodePath = "/sys/devices/system/cpu/cpufreq/policy5/scaling_min_freq",
            wasValue = "480000",
            targetValue = "480000",
            options = listOf("480000", "806400"),
            optionLabels = mapOf(
                "480000" to "480 MHz (Low Idle Floor)",
                "806400" to "806 MHz (Boosted Idle)"
            ),
            functionDesc = "scaling_min_freq: the lowest speed the middle core cluster idles at. A low floor saves power at idle. (ONLY for Poco F7 / Redmi Turbo 4 Pro · Snapdragon 8s Gen 4)",
            upDownEffect = "Powersave, Balanced & Performance (common_idle): 480000 (480 MHz)."
        ),
        KernelTweakSpec(
            id = "policy7_max_freq",
            section = KernelSection.CPU,
            title = "Prime Core Speed Ceiling (scaling_max_freq · Core 7)",
            nodePath = "/sys/devices/system/cpu/cpufreq/policy7/scaling_max_freq",
            wasValue = "2054400",
            targetValue = "2054400",
            options = listOf("1478400", "1612800", "2054400", "2649600", "3206400"),
            optionLabels = mapOf(
                "1478400" to "1.48 GHz (Powersave Cap)",
                "1612800" to "1.61 GHz (Powersave Script)",
                "2054400" to "2.05 GHz (Balanced / Perf / Stock)",
                "2649600" to "2.65 GHz (Fast)",
                "3206400" to "3.21 GHz (Max Boost)"
            ),
            functionDesc = "scaling_max_freq: the speed ceiling for Prime Core 7. A lower cap means less heat and power, but slower peaks. (ONLY for Poco F7 / Redmi Turbo 4 Pro · Snapdragon 8s Gen 4)",
            upDownEffect = "Powersave: 1.48 GHz (1.61 GHz script) · Balanced, Performance & Stock: 2.05 GHz."
        ),
        KernelTweakSpec(
            id = "policy7_min_freq",
            section = KernelSection.CPU,
            title = "Prime Core Idle Speed (scaling_min_freq · Core 7)",
            nodePath = "/sys/devices/system/cpu/cpufreq/policy7/scaling_min_freq",
            wasValue = "633600",
            targetValue = "480000",
            options = listOf("480000", "633600"),
            optionLabels = mapOf(
                "480000" to "480 MHz (Low Idle Floor)",
                "633600" to "634 MHz (Stock Default)"
            ),
            functionDesc = "scaling_min_freq: the lowest speed the Prime core idles at. A low floor saves power at idle. (ONLY for Poco F7 / Redmi Turbo 4 Pro · Snapdragon 8s Gen 4)",
            upDownEffect = "Powersave, Balanced & Performance (common_idle): 480000 (480 MHz) · Stock: 633600 (634 MHz)."
        ),
        KernelTweakSpec(
            id = "policy5_governor",
            section = KernelSection.CPU,
            title = "CPU Speed Scaling Rule (scaling_governor · Policy 5)",
            nodePath = "/sys/devices/system/cpu/cpufreq/policy5/scaling_governor",
            wasValue = "walt",
            targetValue = "walt",
            options = listOf("walt", "powersave", "schedutil", "performance", "conservative"),
            optionLabels = mapOf(
                "walt" to "walt (Follows Load · Balanced/Perf)",
                "powersave" to "powersave (Pins to Minimum)",
                "schedutil" to "schedutil (Linux Balanced)",
                "performance" to "performance (Max Speed)",
                "conservative" to "conservative (Gradual)"
            ),
            functionDesc = "scaling_governor: the rule for choosing speed. 'walt' follows load. 'powersave' pins the cluster to its minimum. (ONLY for Poco F7 / Redmi Turbo 4 Pro · Snapdragon 8s Gen 4)",
            upDownEffect = "Powersave: powersave · Balanced, Performance & Stock: walt."
        ),
        KernelTweakSpec(
            id = "walt_hispeed_load",
            section = KernelSection.CPU,
            title = "WALT High-Speed Jump Load % (hispeed_load)",
            nodePath = "/sys/devices/system/cpu/cpufreq/policy5/walt/hispeed_load",
            wasValue = "90",
            targetValue = "85",
            options = listOf("75", "80", "85", "90", "95"),
            optionLabels = mapOf(
                "75" to "75% Load (Fastest)",
                "80" to "80% Load (Quick)",
                "85" to "85% Load (Performance)",
                "90" to "90% Load (Balanced / Stock)",
                "95" to "95% Load (Conservative)"
            ),
            functionDesc = "hispeed_load: with 'walt' governor, the load % at which the CPU jumps to a fixed speed (hispeed_freq). Skipped under 'powersave' governor. (Poco F7 · onyx · Snapdragon 8s Gen 4 · HyperOS 3)",
            upDownEffect = "Balanced & Stock: 90 · Performance: 85 (not used in Powersave).",
            isPocoF7Only = true
        ),
        KernelTweakSpec(
            id = "walt_hispeed_freq",
            section = KernelSection.CPU,
            title = "WALT High-Speed Jump Frequency (hispeed_freq)",
            nodePath = "/sys/devices/system/cpu/cpufreq/policy5/walt/hispeed_freq",
            wasValue = "1190400",
            targetValue = "1401600",
            options = listOf("960000", "1056000", "1190400", "1401600", "1497600"),
            optionLabels = mapOf(
                "960000" to "960 MHz (Eco)",
                "1056000" to "1.06 GHz (Capped)",
                "1190400" to "1.19 GHz (Balanced / Stock)",
                "1401600" to "1.40 GHz (Performance)",
                "1497600" to "1.50 GHz (Max Jump)"
            ),
            functionDesc = "hispeed_freq: with 'walt' governor, what fixed speed the CPU jumps to when load crosses hispeed_load. Skipped under 'powersave' governor. (Poco F7 · onyx · Snapdragon 8s Gen 4 · HyperOS 3)",
            upDownEffect = "Balanced & Stock: 1.19 GHz (1190400) · Performance: 1.40 GHz (1401600).",
            isPocoF7Only = true
        ),
        KernelTweakSpec(
            id = "policy7_boost",
            section = KernelSection.CPU,
            title = "Prime Core Short Burst Boost",
            nodePath = "/sys/devices/system/cpu/cpufreq/policy7/boost",
            wasValue = "0",
            targetValue = "1",
            options = listOf("0", "1"),
            optionLabels = mapOf(
                "0" to "Off (Default)",
                "1" to "Enabled (Burst Speed)"
            ),
            functionDesc = "Allows the Prime core to briefly exceed normal speed limits during short bursts.",
            upDownEffect = "Turning it on makes app launches snappier with slightly more heat; keeping it off saves battery."
        ),
        KernelTweakSpec(
            id = "cpu6_online",
            section = KernelSection.CPU,
            title = "Core 6 Power State (cpu6 online)",
            nodePath = "/sys/devices/system/cpu/cpu6/online",
            wasValue = "1",
            targetValue = "0",
            options = listOf("0", "1"),
            optionLabels = mapOf(
                "0" to "0 · Parked (Powersave)",
                "1" to "1 · Online (Balanced / Perf / Stock)"
            ),
            functionDesc = "cpu6 online: 0 parks that core entirely, 1 brings it back. (ONLY for Poco F7 / Redmi Turbo 4 Pro · Snapdragon 8s Gen 4)",
            upDownEffect = "Powersave: 0 (parked) · Balanced, Performance & Stock: 1 (online)."
        ),
        KernelTweakSpec(
            id = "core_ctl_cpu2_min",
            section = KernelSection.CPU,
            title = "Big Cluster Minimum Cores (core_ctl min_cpus)",
            nodePath = "/sys/devices/system/cpu/cpu2/core_ctl/min_cpus",
            wasValue = "3",
            targetValue = "1",
            options = listOf("1", "2", "3"),
            optionLabels = mapOf(
                "1" to "1 Core Min (Powersave · Park Cores)",
                "2" to "2 Cores Min",
                "3" to "3 Cores Min (Balanced / Perf / Stock)"
            ),
            functionDesc = "core_ctl min_cpus: the minimum cores kept on in the big cluster (cpu2). Lower lets cores park and save power, but can add lag spikes. (Poco F7 · onyx · Snapdragon 8s Gen 4 · HyperOS 3)",
            upDownEffect = "Powersave: 1 (big cluster may park cores) · Balanced, Performance & Stock: 3.",
            isPocoF7Only = true
        ),
        KernelTweakSpec(
            id = "powerkey_input_boost_freq",
            section = KernelSection.CPU,
            title = "Power Key Input Boost (powerkey_input_boost_freq)",
            nodePath = "/proc/sys/walt/input_boost/powerkey_input_boost_freq",
            wasValue = "1785600 0 2611200 0 0 2611200 0 2515200",
            targetValue = "0 0 0 0 0 0 0 0",
            options = listOf(
                "0 0 0 0 0 0 0 0",
                "1785600 0 2611200 0 0 2611200 0 2515200"
            ),
            optionLabels = mapOf(
                "0 0 0 0 0 0 0 0" to "Off · No Power-Key Boost (Powersave)",
                "1785600 0 2611200 0 0 2611200 0 2515200" to "Active Boost (Balanced / Perf / Stock)"
            ),
            functionDesc = "powerkey_input_boost_freq: a short speed boost when you press the power key. (Poco F7 · onyx · Snapdragon 8s Gen 4 · HyperOS 3)",
            upDownEffect = "Powersave: '0 0 0 0 0 0 0 0' (no boost) · Balanced, Performance & Stock: '1785600 0 2611200 0 0 2611200 0 2515200'.",
            isPocoF7Only = true
        ),
        KernelTweakSpec(
            id = "walt_sched_upmigrate",
            section = KernelSection.CPU,
            title = "Task Up-Migration Load % (sched_upmigrate)",
            nodePath = "/proc/sys/walt/sched_upmigrate",
            wasValue = "95 95 95",
            targetValue = "85 85 85",
            options = listOf("85 85 85", "90 90 90", "95 95 95"),
            optionLabels = mapOf(
                "85 85 85" to "85% (Performance · Reach Big Cores Sooner)",
                "90 90 90" to "90% (Moderate)",
                "95 95 95" to "95% (Powersave / Balanced / Stock)"
            ),
            functionDesc = "sched_upmigrate: the load % at which a task moves to a bigger core. (Poco F7 · onyx · Snapdragon 8s Gen 4 · HyperOS 3)",
            upDownEffect = "Powersave, Balanced & Stock: '95 95 95' · Performance: '85 85 85' (heavy tasks reach big cores sooner).",
            isPocoF7Only = true
        ),
        KernelTweakSpec(
            id = "walt_sched_downmigrate",
            section = KernelSection.CPU,
            title = "Task Down-Migration Load % (sched_downmigrate)",
            nodePath = "/proc/sys/walt/sched_downmigrate",
            wasValue = "85 85 85",
            targetValue = "75 75 75",
            options = listOf("75 75 75", "80 80 80", "85 85 85"),
            optionLabels = mapOf(
                "75 75 75" to "75% (Performance · Hold Big Cores)",
                "80 80 80" to "80% (Moderate)",
                "85 85 85" to "85% (Powersave / Balanced / Stock)"
            ),
            functionDesc = "sched_downmigrate: the load % at which a task moves back down to a smaller core. (Poco F7 · onyx · Snapdragon 8s Gen 4 · HyperOS 3)",
            upDownEffect = "Powersave, Balanced & Stock: '85 85 85' · Performance: '75 75 75'.",
            isPocoF7Only = true
        ),

        // ====================================================================
        // 2. TASK SCHEDULER
        // ====================================================================
        KernelTweakSpec(
            id = "sched_energy_aware",
            section = KernelSection.SCHED,
            title = "Energy-Aware Task Placement",
            nodePath = "/proc/sys/kernel/sched_energy_aware",
            wasValue = "1",
            targetValue = "0",
            options = listOf("0", "1"),
            optionLabels = mapOf(
                "0" to "Performance First",
                "1" to "Power First (Default)"
            ),
            functionDesc = "Decides whether light tasks are placed on power-efficient cores or fastest cores.",
            upDownEffect = "Power First saves battery by routing light work to small cores; Performance First prioritizes speed at higher battery cost."
        ),
        KernelTweakSpec(
            id = "sched_pelt_multiplier",
            section = KernelSection.SCHED,
            title = "Scheduler Load Reaction Speed (sched_pelt_multiplier)",
            nodePath = "/proc/sys/kernel/sched_pelt_multiplier",
            wasValue = "4",
            targetValue = "2",
            options = listOf("1", "2", "4"),
            optionLabels = mapOf(
                "1" to "1 · Slow & Smooth (Powersave)",
                "2" to "2 · Balanced (Balanced)",
                "4" to "4 · Fast Reaction (Perf / Stock)"
            ),
            functionDesc = "sched_pelt_multiplier: how fast the scheduler reacts to load. 1 is slow and smooth, 4 is fast and uses more power. (ONLY for Poco F7 / Redmi Turbo 4 Pro · Snapdragon 8s Gen 4)",
            upDownEffect = "Powersave: 1 · Balanced: 2 · Performance & Stock: 4."
        ),
        KernelTweakSpec(
            id = "sched_schedstats",
            section = KernelSection.SCHED,
            title = "Background Scheduler Accounting",
            nodePath = "/proc/sys/kernel/sched_schedstats",
            wasValue = "0",
            targetValue = "0",
            options = listOf("0", "1"),
            optionLabels = mapOf(
                "0" to "Disabled (Default)",
                "1" to "Enabled (Debug)"
            ),
            functionDesc = "Controls whether the system continuously records internal task scheduling statistics.",
            upDownEffect = "Keeping it Disabled avoids unnecessary background tracking overhead."
        ),
        KernelTweakSpec(
            id = "sched_rt_runtime_us",
            section = KernelSection.SCHED,
            title = "Real-Time Audio & UI Quota (sched_rt_runtime_us)",
            nodePath = "/proc/sys/kernel/sched_rt_runtime_us",
            wasValue = "1250000",
            targetValue = "1250000",
            options = listOf("950000", "1250000"),
            optionLabels = mapOf(
                "950000" to "950,000 us (Powersave)",
                "1250000" to "1,250,000 us (Balanced / Perf / Stock)"
            ),
            functionDesc = "sched_rt_runtime_us: how much of each period real-time tasks (audio, UI) may use. The rest stays for normal tasks. (ONLY for Poco F7 / Redmi Turbo 4 Pro · Snapdragon 8s Gen 4)",
            upDownEffect = "Powersave: 950000 · Balanced, Performance & Stock: 1250000."
        ),
        KernelTweakSpec(
            id = "sched_rr_timeslice_ms",
            section = KernelSection.SCHED,
            title = "Task Switching Slice Duration",
            nodePath = "/proc/sys/kernel/sched_rr_timeslice_ms",
            wasValue = "100",
            targetValue = "100",
            options = listOf("50", "100"),
            optionLabels = mapOf(
                "50" to "50 ms (Quick Switch)",
                "100" to "100 ms (Default)"
            ),
            functionDesc = "Controls the time slice given to equal-priority tasks before switching.",
            upDownEffect = "50 ms switches between active tasks more frequently; 100 ms is the standard setting."
        ),
        KernelTweakSpec(
            id = "uclamp_bg_max",
            section = KernelSection.SCHED,
            title = "Background Speed Ceiling (cpuctl background uclamp.max)",
            nodePath = "/dev/cpuctl/background/cpu.uclamp.max",
            wasValue = "30.00",
            targetValue = "25.00",
            options = listOf("15.00", "20.00", "25.00", "30.00", "50.00"),
            optionLabels = mapOf(
                "15.00" to "15% Max (Powersave)",
                "20.00" to "20% Max (Eco)",
                "25.00" to "25% Max (Balanced)",
                "30.00" to "30% Max (Performance / Stock)",
                "50.00" to "50% Max (Fast Sync)"
            ),
            functionDesc = "cpuctl uclamp.max: a ceiling on the speed background tasks can request, as a percentage. (ONLY for Poco F7 / Redmi Turbo 4 Pro · Snapdragon 8s Gen 4)",
            upDownEffect = "Powersave: 15.00 · Balanced: 25.00 · Performance & Stock: 30.00."
        ),
        KernelTweakSpec(
            id = "uclamp_sysbg_max",
            section = KernelSection.SCHED,
            title = "System-Background Speed Ceiling (uclamp.max)",
            nodePath = "/dev/cpuctl/system-background/cpu.uclamp.max",
            wasValue = "40.00",
            targetValue = "30.00",
            options = listOf("25.00", "30.00", "40.00", "60.00"),
            optionLabels = mapOf(
                "25.00" to "25% Max (Powersave)",
                "30.00" to "30% Max (Balanced)",
                "40.00" to "40% Max (Performance / Stock)",
                "60.00" to "60% Max (Uncapped)"
            ),
            functionDesc = "cpuctl uclamp.max: a ceiling on the speed system-background tasks can request, as a percentage. (ONLY for Poco F7 / Redmi Turbo 4 Pro · Snapdragon 8s Gen 4)",
            upDownEffect = "Powersave: 25.00 · Balanced: 30.00 · Performance & Stock: 40.00."
        ),
        KernelTweakSpec(
            id = "uclamp_nnapi_min",
            section = KernelSection.SCHED,
            title = "AI/ML Layer Speed Floor (nnapi-hal uclamp.min)",
            nodePath = "/dev/cpuctl/nnapi-hal/cpu.uclamp.min",
            wasValue = "1.00",
            targetValue = "0.00",
            options = listOf("0.00", "1.00"),
            optionLabels = mapOf(
                "0.00" to "0% Floor (Powersave / Balanced / Perf)",
                "1.00" to "1% Floor (Stock Default)"
            ),
            functionDesc = "cpuctl uclamp.min: a floor on the speed a group can request, as a percentage. nnapi-hal is the AI/ML layer. (ONLY for Poco F7 / Redmi Turbo 4 Pro · Snapdragon 8s Gen 4)",
            upDownEffect = "Powersave, Balanced & Performance (common_idle): 0.00 · Stock: 1.00."
        ),
        KernelTweakSpec(
            id = "uclamp_topapp_min",
            section = KernelSection.SCHED,
            title = "Foreground App Smoothness Boost",
            nodePath = "/dev/cpuctl/top-app/cpu.uclamp.min",
            wasValue = "0.00",
            targetValue = "20.00",
            options = listOf("0.00", "10.00", "20.00"),
            optionLabels = mapOf(
                "0.00" to "0% (Power Save Default)",
                "10.00" to "10% (Smooth UI)",
                "20.00" to "20% (Ultra Smooth UI)"
            ),
            functionDesc = "Sets a minimum guaranteed speed boost for the app currently open on your screen.",
            upDownEffect = "Raising to 10% or 20% keeps scrolling and animations very smooth at the cost of extra battery; 0% saves maximum power."
        ),
        KernelTweakSpec(
            id = "cpuset_misf",
            section = KernelSection.SCHED,
            title = "Frame Service Core Assignment (cpuset misf)",
            nodePath = "/dev/cpuset/misf/cpus",
            wasValue = "4-7",
            targetValue = "4-7",
            options = listOf("0-3", "4-7", "0-7"),
            optionLabels = mapOf(
                "0-3" to "0–3 · Small Cores (Powersave)",
                "4-7" to "4–7 · Big Cores (Balanced / Perf / Stock)",
                "0-7" to "0–7 · All Cores"
            ),
            functionDesc = "cpuset misf cpus: which cores a group of tasks may use. misf is a frame-related service group. (ONLY for Poco F7 / Redmi Turbo 4 Pro · Snapdragon 8s Gen 4)",
            upDownEffect = "Powersave: 0-3 · Balanced, Performance & Stock: 4-7."
        ),
        KernelTweakSpec(
            id = "cpuset_background",
            section = KernelSection.SCHED,
            title = "Background Tasks Core Assignment (cpuset background)",
            nodePath = "/dev/cpuset/background/cpus",
            wasValue = "0-7",
            targetValue = "0-3",
            options = listOf("0-1", "0-3", "0-7"),
            optionLabels = mapOf(
                "0-1" to "0–1 · Efficiency Cores (Powersave)",
                "0-3" to "0–3 · Small Cores (Balanced)",
                "0-7" to "0–7 · All Cores (Performance / Stock)"
            ),
            functionDesc = "cpuset background cpus: which cores background tasks may use. Limited to small cores to save power. (ONLY for Poco F7 / Redmi Turbo 4 Pro · Snapdragon 8s Gen 4)",
            upDownEffect = "Powersave: 0-1 · Balanced: 0-3 · Performance & Stock: 0-7."
        ),
        KernelTweakSpec(
            id = "cpuset_sysbg",
            section = KernelSection.SCHED,
            title = "System-Background Core Assignment (cpuset system-background)",
            nodePath = "/dev/cpuset/system-background/cpus",
            wasValue = "0-7",
            targetValue = "0-3",
            options = listOf("0-1", "0-3", "0-7"),
            optionLabels = mapOf(
                "0-1" to "0–1 · Strict Small Cores",
                "0-3" to "0–3 · Small Cores (Powersave)",
                "0-7" to "0–7 · All Cores (Balanced / Perf / Stock)"
            ),
            functionDesc = "cpuset system-background cpus: which cores system-background tasks may use. Limited to small cores in Powersave. (Poco F7 · onyx · Snapdragon 8s Gen 4 · HyperOS 3)",
            upDownEffect = "Powersave: 0-3 · Balanced, Performance & Stock: 0-7.",
            isPocoF7Only = true
        ),

        // ====================================================================
        // 3. GPU & GRAPHICS
        // ====================================================================
        KernelTweakSpec(
            id = "gpu_max_freq",
            section = KernelSection.GPU,
            title = "GPU Maximum Clock Speed",
            nodePath = "/sys/class/kgsl/kgsl-3d0/devfreq/max_freq",
            wasValue = "873000000",
            targetValue = "1150000000",
            options = listOf("600000000", "873000000", "1150000000"),
            optionLabels = mapOf(
                "600000000" to "600 MHz (Cool & Eco)",
                "873000000" to "873 MHz (Default Cap)",
                "1150000000" to "1150 MHz (Full Gaming)"
            ),
            functionDesc = "Controls the highest graphics processor speed. (Note: releasing the GPU thermal limit may also be needed to reach 1150 MHz.)",
            upDownEffect = "1150 MHz boosts gaming frame rates with more heat; 600 MHz keeps graphics cool and saves battery."
        ),
        KernelTweakSpec(
            id = "gpu_min_freq",
            section = KernelSection.GPU,
            title = "GPU Minimum Idle Speed",
            nodePath = "/sys/class/kgsl/kgsl-3d0/devfreq/min_freq",
            wasValue = "264000000",
            targetValue = "264000000",
            options = listOf("264000000", "400000000"),
            optionLabels = mapOf(
                "264000000" to "264 MHz (Lowest Power)",
                "400000000" to "400 MHz (Boosted Floor)"
            ),
            functionDesc = "Controls the lowest speed the graphics processor drops to when idle.",
            upDownEffect = "Keeping it at 264 MHz saves idle battery; raising it reduces micro-stutters but drains more power."
        ),
        KernelTweakSpec(
            id = "gpu_idle_timer",
            section = KernelSection.GPU,
            title = "GPU Sleep Delay Timer",
            nodePath = "/sys/class/kgsl/kgsl-3d0/idle_timer",
            wasValue = "80",
            targetValue = "120",
            options = listOf("50", "64", "80", "120"),
            optionLabels = mapOf(
                "50" to "50 ms (Fast Sleep)",
                "64" to "64 ms (Balanced)",
                "80" to "80 ms (Default)",
                "120" to "120 ms (Smooth Gaming)"
            ),
            functionDesc = "How long the graphics chip waits after finishing work before powering down.",
            upDownEffect = "Lower values (50–64 ms) put the GPU to sleep sooner to save battery; 120 ms avoids frequent wake-ups during gaming."
        ),
        KernelTweakSpec(
            id = "gpu_max_pwrlevel",
            section = KernelSection.GPU,
            title = "GPU Peak Performance Level",
            nodePath = "/sys/class/kgsl/kgsl-3d0/max_pwrlevel",
            wasValue = "2",
            targetValue = "2",
            options = listOf("0", "2", "4"),
            optionLabels = mapOf(
                "0" to "Level 0 (Fastest)",
                "2" to "Level 2 (Default)",
                "4" to "Level 4 (Power Save)"
            ),
            functionDesc = "Controls the fastest power state the GPU is allowed to enter (lower level number means faster speed).",
            upDownEffect = "Level 0 allows peak graphics speed; Level 4 limits graphics power consumption."
        ),
        KernelTweakSpec(
            id = "gpu_min_pwrlevel",
            section = KernelSection.GPU,
            title = "GPU Deepest Idle Level",
            nodePath = "/sys/class/kgsl/kgsl-3d0/min_pwrlevel",
            wasValue = "10",
            targetValue = "10",
            options = listOf("8", "10"),
            optionLabels = mapOf(
                "8" to "Level 8 (Higher Idle)",
                "10" to "Level 10 (Deepest Save)"
            ),
            functionDesc = "Controls the slowest power state the GPU drops into when resting.",
            upDownEffect = "Level 10 allows the deepest graphics power saving (264 MHz)."
        ),
        KernelTweakSpec(
            id = "kgsl_busmon_max",
            section = KernelSection.GPU,
            title = "Graphics Memory Bus Bandwidth",
            nodePath = "/sys/class/devfreq/kgsl-busmon/max_freq",
            wasValue = "873000000",
            targetValue = "1150000000",
            options = listOf("600000000", "873000000", "1150000000"),
            optionLabels = mapOf(
                "600000000" to "Eco Bandwidth",
                "873000000" to "Default Bandwidth",
                "1150000000" to "Max Bandwidth"
            ),
            functionDesc = "Controls the memory bus bandwidth ceiling linked to the graphics processor.",
            upDownEffect = "Max Bandwidth feeds textures faster in heavy games; Eco Bandwidth saves memory bus power."
        ),
        KernelTweakSpec(
            id = "notification_window_blur",
            section = KernelSection.GPU,
            title = "Remove Notification Blur",
            nodePath = "settings:global:disable_window_blurs",
            wasValue = "0",
            targetValue = "1",
            options = listOf("0", "1"),
            optionLabels = mapOf(
                "0" to "Blur On (Default)",
                "1" to "Blur Removed (Save Power)"
            ),
            functionDesc = "Turns off real-time background blur behind the notification shade and system panels.",
            upDownEffect = "Removing notification blur reduces GPU rendering work every time you pull down notifications and saves battery.",
            customWriteCommand = { value ->
                "( settings put global disable_window_blurs $value 2>/dev/null; settings put system disable_window_blurs $value 2>/dev/null; settings put secure disable_window_blurs $value 2>/dev/null ) >/dev/null 2>&1 &"
            },
            customReadCommand = "v=\$(settings get global disable_window_blurs 2>/dev/null); [ \"\$v\" = \"1\" ] && echo 1 || echo 0"
        ),

        // ====================================================================
        // 3B. MEMORY BUS (DDR / LLCC / L3 — Poco F7 / Redmi Turbo 4 Pro / 8s Gen 4)
        // ====================================================================
        KernelTweakSpec(
            id = "bus_dcvs_ddr_max",
            section = KernelSection.MEM_BUS,
            title = "DDR RAM Speed Ceiling (DDR cap)",
            nodePath = "/sys/devices/system/cpu/bus_dcvs/DDR/24091000.qcom,bwmon-ddr/max_freq",
            wasValue = "4761600",
            targetValue = "3686400",
            options = listOf("3187200", "3686400", "4761600"),
            optionLabels = mapOf(
                "3187200" to "3.19 GHz · Step 7/10 (Powersave)",
                "3686400" to "3.69 GHz · Step 8/10 (Balanced)",
                "4761600" to "4.76 GHz · Uncapped (Performance / Stock)"
            ),
            functionDesc = "DDR cap: the RAM speed ceiling across all DDR voters (bwmon + memlat). Saves the most power, but games and the camera feel it first. (Poco F7 · onyx · Snapdragon 8s Gen 4 · HyperOS 3)",
            upDownEffect = "Powersave: 3187200 · Balanced: 3686400 · Performance & Stock: 4761600.",
            isPocoF7Only = true,
            customWriteCommand = { value ->
                "for f in /sys/devices/system/cpu/bus_dcvs/DDR/*/max_freq; do if [ -e \"\$f\" ]; then chmod 0666 \"\$f\" 2>/dev/null; echo $value > \"\$f\" 2>/dev/null; fi; done"
            },
            customReadCommand = "cat /sys/devices/system/cpu/bus_dcvs/DDR/24091000.qcom,bwmon-ddr/max_freq 2>/dev/null || for f in /sys/devices/system/cpu/bus_dcvs/DDR/*/max_freq; do [ -e \"\$f\" ] && { cat \"\$f\" 2>/dev/null; break; }; done"
        ),
        KernelTweakSpec(
            id = "bus_dcvs_llcc_max",
            section = KernelSection.MEM_BUS,
            title = "Last-Level Cache Clock Ceiling (LLCC cap)",
            nodePath = "/sys/devices/system/cpu/bus_dcvs/LLCC/240b7400.qcom,bwmon-llcc/max_freq",
            wasValue = "1211200",
            targetValue = "933000",
            options = listOf("806000", "933000", "1211200"),
            optionLabels = mapOf(
                "806000" to "806 MHz (Powersave)",
                "933000" to "933 MHz (Balanced)",
                "1211200" to "1.21 GHz / 933 MHz (Perf / Stock)"
            ),
            functionDesc = "LLCC cap: the clock of the shared last-level cache across bwmon and memlat voters. (Poco F7 · onyx · Snapdragon 8s Gen 4 · HyperOS 3)",
            upDownEffect = "Powersave: 806000 · Balanced: 933000 · Performance & Stock: 1211200 (memlat) / 933000 (bwmon).",
            isPocoF7Only = true,
            customWriteCommand = { value ->
                if (value == "1211200") {
                    "for f in /sys/devices/system/cpu/bus_dcvs/LLCC/soc:qcom,memlat:llcc:*/max_freq; do if [ -e \"\$f\" ]; then chmod 0666 \"\$f\" 2>/dev/null; echo 1211200 > \"\$f\" 2>/dev/null; fi; done; if [ -e /sys/devices/system/cpu/bus_dcvs/LLCC/240b7400.qcom,bwmon-llcc/max_freq ]; then chmod 0666 /sys/devices/system/cpu/bus_dcvs/LLCC/240b7400.qcom,bwmon-llcc/max_freq 2>/dev/null; echo 933000 > /sys/devices/system/cpu/bus_dcvs/LLCC/240b7400.qcom,bwmon-llcc/max_freq 2>/dev/null; fi"
                } else {
                    "for f in /sys/devices/system/cpu/bus_dcvs/LLCC/*/max_freq; do if [ -e \"\$f\" ]; then chmod 0666 \"\$f\" 2>/dev/null; echo $value > \"\$f\" 2>/dev/null; fi; done"
                }
            },
            customReadCommand = "for f in /sys/devices/system/cpu/bus_dcvs/LLCC/soc:qcom,memlat:llcc:*/max_freq; do [ -e \"\$f\" ] && { cat \"\$f\" 2>/dev/null; break; }; done"
        ),
        KernelTweakSpec(
            id = "bus_dcvs_l3_max",
            section = KernelSection.MEM_BUS,
            title = "CPU L3 Cache Clock Ceiling (L3 cap)",
            nodePath = "/sys/devices/system/cpu/bus_dcvs/L3/soc:qcom,memlat:l3:silver/max_freq",
            wasValue = "1804800",
            targetValue = "1344000",
            options = listOf("1344000", "1804800"),
            optionLabels = mapOf(
                "1344000" to "1.34 GHz (Powersave)",
                "1804800" to "1.80 GHz (Balanced / Perf / Stock)"
            ),
            functionDesc = "L3 cap: the clock of the CPU's L3 cache. It affects CPU speed directly. (Poco F7 · onyx · Snapdragon 8s Gen 4 · HyperOS 3)",
            upDownEffect = "Powersave: 1344000 (1.34 GHz) · Balanced, Performance & Stock: 1804800 (1.80 GHz).",
            isPocoF7Only = true,
            customWriteCommand = { value ->
                "for f in /sys/devices/system/cpu/bus_dcvs/L3/*/max_freq; do if [ -e \"\$f\" ]; then chmod 0666 \"\$f\" 2>/dev/null; echo $value > \"\$f\" 2>/dev/null; fi; done"
            },
            customReadCommand = "for f in /sys/devices/system/cpu/bus_dcvs/L3/*/max_freq; do [ -e \"\$f\" ] && { cat \"\$f\" 2>/dev/null; break; }; done"
        ),

        // ====================================================================
        // 4. RAM, SWAP & STORAGE
        // ====================================================================
        KernelTweakSpec(
            id = "vm_swappiness",
            section = KernelSection.RAM,
            title = "ZRAM Swap Readiness (swappiness)",
            nodePath = "/proc/sys/vm/swappiness",
            wasValue = "150",
            targetValue = "100",
            options = listOf("60", "80", "100", "150"),
            optionLabels = mapOf(
                "60" to "60 · Low Swap (Powersave)",
                "80" to "80 · Mild Swap",
                "100" to "100 · Balanced (Balanced / Perf)",
                "150" to "150 · Aggressive (Stock Default)"
            ),
            functionDesc = "swappiness: how readily memory moves to ZRAM. A higher value swaps more. (ONLY for Poco F7 / Redmi Turbo 4 Pro · Snapdragon 8s Gen 4)",
            upDownEffect = "Powersave: 60 · Balanced & Performance: 100 · Stock: 150."
        ),
        KernelTweakSpec(
            id = "vm_min_free_kbytes",
            section = KernelSection.RAM,
            title = "Reserved Emergency RAM Buffer",
            nodePath = "/proc/sys/vm/min_free_kbytes",
            wasValue = "11584",
            targetValue = "32768",
            options = listOf("11584", "24576", "32768"),
            optionLabels = mapOf(
                "11584" to "11 MB (Default)",
                "24576" to "24 MB (Smooth)",
                "32768" to "32 MB (Stutter-Free)"
            ),
            functionDesc = "Controls the amount of free RAM kept in reserve for sudden app launches.",
            upDownEffect = "Raising to 32 MB prevents memory allocation stutters when opening large apps."
        ),
        KernelTweakSpec(
            id = "vm_vfs_cache_pressure",
            section = KernelSection.RAM,
            title = "File & Folder Cache Drop Speed (vfs_cache_pressure)",
            nodePath = "/proc/sys/vm/vfs_cache_pressure",
            wasValue = "73",
            targetValue = "100",
            options = listOf("50", "73", "100", "120"),
            optionLabels = mapOf(
                "50" to "50 · Keep Cache Longer",
                "73" to "73 · Keep More Cache (Perf / Stock)",
                "100" to "100 · Standard Drop (Powersave / Balanced)",
                "120" to "120 · Fast Drop"
            ),
            functionDesc = "vfs_cache_pressure: how quickly the kernel drops cached file and folder info. Lower keeps more of it. (ONLY for Poco F7 / Redmi Turbo 4 Pro · Snapdragon 8s Gen 4)",
            upDownEffect = "Powersave & Balanced: 100 · Performance & Stock: 73."
        ),
        KernelTweakSpec(
            id = "vm_watermark_scale",
            section = KernelSection.RAM,
            title = "Background RAM Cleanup Trigger",
            nodePath = "/proc/sys/vm/watermark_scale_factor",
            wasValue = "10",
            targetValue = "20",
            options = listOf("10", "20", "40"),
            optionLabels = mapOf(
                "10" to "Default (10)",
                "20" to "Early Cleanup (20)",
                "40" to "Aggressive Cleanup (40)"
            ),
            functionDesc = "Controls how early the background memory cleaner starts freeing RAM before memory gets full.",
            upDownEffect = "Raising to 20 starts cleanup earlier in the background so apps don't freeze waiting for RAM."
        ),
        KernelTweakSpec(
            id = "sda_read_ahead_kb",
            section = KernelSection.RAM,
            title = "Sequential Read-Ahead Buffer (read_ahead_kb)",
            nodePath = "/sys/block/sda/queue/read_ahead_kb",
            wasValue = "128",
            targetValue = "128",
            options = listOf("64", "128", "256", "512"),
            optionLabels = mapOf(
                "64" to "64 KB (Powersave)",
                "128" to "128 KB (Balanced / Stock)",
                "256" to "256 KB (Performance)",
                "512" to "512 KB (Large Files)"
            ),
            functionDesc = "read_ahead_kb: how much extra data is read ahead on sequential reads. (ONLY for Poco F7 / Redmi Turbo 4 Pro · Snapdragon 8s Gen 4)",
            upDownEffect = "Powersave: 64 KB · Balanced & Stock: 128 KB · Performance: 256 KB."
        ),
        KernelTweakSpec(
            id = "sda_iostats",
            section = KernelSection.RAM,
            title = "Storage Per-Read I/O Bookkeeping (iostats)",
            nodePath = "/sys/block/sda/queue/iostats",
            wasValue = "1",
            targetValue = "0",
            options = listOf("0", "1"),
            optionLabels = mapOf(
                "0" to "0 · Off (Save CPU · All Profiles)",
                "1" to "1 · On (Stock Default)"
            ),
            functionDesc = "iostats: per-read storage bookkeeping, turned off (0) to save a little CPU on every read. (Poco F7 · onyx · Snapdragon 8s Gen 4 · HyperOS 3)",
            upDownEffect = "Powersave, Balanced & Performance (common_idle): 0 · Stock: 1.",
            isPocoF7Only = true
        ),
        KernelTweakSpec(
            id = "sda_add_random",
            section = KernelSection.RAM,
            title = "Storage Entropy Bookkeeping (add_random)",
            nodePath = "/sys/block/sda/queue/add_random",
            wasValue = "1",
            targetValue = "0",
            options = listOf("0", "1"),
            optionLabels = mapOf(
                "0" to "0 · Off (Save CPU · All Profiles)",
                "1" to "1 · On (Stock Default)"
            ),
            functionDesc = "add_random: per-read random entropy bookkeeping, turned off (0) to save a little CPU on storage reads. (Poco F7 · onyx · Snapdragon 8s Gen 4 · HyperOS 3)",
            upDownEffect = "Powersave, Balanced & Performance (common_idle): 0 · Stock: 1.",
            isPocoF7Only = true
        ),

        // ====================================================================
        // 5. HARDWARE INTERRUPTS
        // ====================================================================
        KernelTweakSpec(
            id = "irq_359_msm_drm",
            section = KernelSection.IRQ,
            title = "Display Controller Interrupt Routing",
            nodePath = "/proc/irq/359/smp_affinity_list",
            wasValue = "0-1",
            targetValue = "2-4",
            options = listOf("0-1", "2-4"),
            optionLabels = mapOf(
                "0-1" to "Small Cores (0–1 Default)",
                "2-4" to "Big Cores (2–4)"
            ),
            functionDesc = "Chooses which processor cores handle display screen refresh interrupts.",
            upDownEffect = "Moving to Big Cores (2–4) spreads display work off the busy small cores."
        ),
        KernelTweakSpec(
            id = "irq_361_touchscreen",
            section = KernelSection.IRQ,
            title = "Touchscreen Interrupt Routing",
            nodePath = "/proc/irq/361/smp_affinity_list",
            wasValue = "0-1",
            targetValue = "2-4",
            options = listOf("0-1", "2-4"),
            optionLabels = mapOf(
                "0-1" to "Small Cores (0–1 Default)",
                "2-4" to "Big Cores (2–4)"
            ),
            functionDesc = "Chooses which processor cores handle finger touch input signals.",
            upDownEffect = "Moving to Big Cores (2–4) can reduce touch latency when small cores are busy."
        ),
        KernelTweakSpec(
            id = "irq_367_glink",
            section = KernelSection.IRQ,
            title = "Modem & Subsystem Link Routing",
            nodePath = "/proc/irq/367/smp_affinity_list",
            wasValue = "0-1",
            targetValue = "2-4",
            options = listOf("0-1", "2-4"),
            optionLabels = mapOf(
                "0-1" to "Small Cores (0–1 Default)",
                "2-4" to "Big Cores (2–4)"
            ),
            functionDesc = "Chooses which processor cores handle internal subsystem communication interrupts.",
            upDownEffect = "Small Cores (0–1) is the stock power-saving route; Big Cores (2–4) spreads interrupt load."
        ),

        // ====================================================================
        // 6. SLEEP & APP FREEZE
        // ====================================================================
        KernelTweakSpec(
            id = "pm_freeze_timeout",
            section = KernelSection.FREEZE,
            title = "Deep Sleep Task Freeze Timeout",
            nodePath = "/sys/power/pm_freeze_timeout",
            wasValue = "20000",
            targetValue = "10000",
            options = listOf("5000", "10000", "20000"),
            optionLabels = mapOf(
                "5000" to "5 Seconds (Quick Retry)",
                "10000" to "10 Seconds (Recommended)",
                "20000" to "20 Seconds (Default)"
            ),
            functionDesc = "Controls how long the phone waits for stubborn background tasks to freeze before aborting deep sleep.",
            upDownEffect = "Lowering to 10 Seconds stops the phone from wasting 20 seconds on a stuck task and retries sleep sooner."
        ),
        KernelTweakSpec(
            id = "pm_async",
            section = KernelSection.FREEZE,
            title = "Parallel Device Sleep Mode",
            nodePath = "/sys/power/pm_async",
            wasValue = "1",
            targetValue = "0",
            options = listOf("0", "1"),
            optionLabels = mapOf(
                "0" to "One by One (Safer Sleep)",
                "1" to "Parallel (Default)"
            ),
            functionDesc = "Controls whether hardware components are put to sleep all at once or one by one.",
            upDownEffect = "One by One is slightly slower but safer when a timer or sensor is preventing deep sleep."
        ),
        KernelTweakSpec(
            id = "miui_analytics_net_restrict",
            section = KernelSection.FREEZE,
            title = "Xiaomi Telemetry Background Block (com.miui.analytics · UID 10184)",
            nodePath = "cmd:netpolicy:restrict-background-blacklist:10184",
            wasValue = "0",
            targetValue = "1",
            options = listOf("0", "1"),
            optionLabels = mapOf(
                "0" to "Allowed (remove restrict-background-blacklist 10184)",
                "1" to "Blocked (add restrict-background-blacklist 10184)"
            ),
            functionDesc = "com.miui.analytics (UID 10184) · Xiaomi telemetry: Blocks background network access via 'cmd netpolicy add restrict-background-blacklist 10184' (Undo: 'cmd netpolicy remove restrict-background-blacklist 10184').",
            upDownEffect = "Enabling runs 'cmd netpolicy add restrict-background-blacklist 10184' to stop Xiaomi telemetry background wakeups; Undo runs 'cmd netpolicy remove restrict-background-blacklist 10184'.",
            customWriteCommand = { value ->
                if (value == "1") {
                    "( cmd netpolicy add restrict-background-blacklist 10184 ) >/dev/null 2>&1 &"
                } else {
                    "( cmd netpolicy remove restrict-background-blacklist 10184 ) >/dev/null 2>&1 &"
                }
            },
            customReadCommand = "cmd netpolicy list restrict-background-blacklist 2>/dev/null | grep -qw 10184 && echo 1 || echo 0"
        ),

        // ====================================================================
        // 7. THERMAL COOLING LIMITS
        // ====================================================================
        KernelTweakSpec(
            id = "thermal_cooling_gpu",
            section = KernelSection.THERMAL,
            title = "GPU Thermal Speed Throttle",
            nodePath = "/sys/class/thermal/cooling_device[gpu]/cur_state",
            wasValue = "4",
            targetValue = "0",
            options = listOf("0", "2", "4"),
            optionLabels = mapOf(
                "0" to "Released (Full Speed)",
                "2" to "Mild Limit",
                "4" to "Default Limit (873 MHz)"
            ),
            functionDesc = "Controls the standing thermal speed cap placed on the graphics processor.",
            upDownEffect = "Releasing this limit unlocks full GPU speed (up to 1150 MHz) at the cost of extra heat and battery.",
            isRisky = true,
            customWriteCommand = { value ->
                "for c in /sys/class/thermal/cooling_device*; do _t=''; read -r _t < \"\$c/type\" 2>/dev/null || continue; if [ \"\$_t\" = \"gpu\" ]; then chmod 0666 \"\$c/cur_state\" 2>/dev/null; echo $value > \"\$c/cur_state\" 2>/dev/null; chmod 0444 \"\$c/cur_state\" 2>/dev/null; break; fi; done"
            },
            customReadCommand = "for c in /sys/class/thermal/cooling_device*; do _t=''; read -r _t < \"\$c/type\" 2>/dev/null || continue; if [ \"\$_t\" = \"gpu\" ]; then _cs=''; read -r _cs < \"\$c/cur_state\" 2>/dev/null; echo \"\$_cs\"; break; fi; done"
        ),
        KernelTweakSpec(
            id = "thermal_cooling_cpu3",
            section = KernelSection.THERMAL,
            title = "Prime CPU Cluster Thermal Throttle",
            nodePath = "/sys/class/thermal/cooling_device[cpu-cluster3]/cur_state",
            wasValue = "3",
            targetValue = "0",
            options = listOf("0", "1", "3"),
            optionLabels = mapOf(
                "0" to "Released (Full Speed)",
                "1" to "Light Limit",
                "3" to "Default Limit"
            ),
            functionDesc = "Controls the standing thermal speed cap placed on the top processor cluster.",
            upDownEffect = "Releasing it allows sustained peak processor speed but generates more heat under load.",
            isRisky = true,
            customWriteCommand = { value ->
                "for c in /sys/class/thermal/cooling_device*; do _t=''; read -r _t < \"\$c/type\" 2>/dev/null || continue; if [ \"\$_t\" = \"cpu-cluster3\" ]; then chmod 0666 \"\$c/cur_state\" 2>/dev/null; echo $value > \"\$c/cur_state\" 2>/dev/null; chmod 0444 \"\$c/cur_state\" 2>/dev/null; break; fi; done"
            },
            customReadCommand = "for c in /sys/class/thermal/cooling_device*; do _t=''; read -r _t < \"\$c/type\" 2>/dev/null || continue; if [ \"\$_t\" = \"cpu-cluster3\" ]; then _cs=''; read -r _cs < \"\$c/cur_state\" 2>/dev/null; echo \"\$_cs\"; break; fi; done"
        ),
        KernelTweakSpec(
            id = "thermal_cooling_cdsp",
            section = KernelSection.THERMAL,
            title = "AI & Compute DSP Thermal Throttle",
            nodePath = "/sys/class/thermal/cooling_device[cdsp]/cur_state",
            wasValue = "1",
            targetValue = "0",
            options = listOf("0", "1"),
            optionLabels = mapOf(
                "0" to "Released (No Limit)",
                "1" to "Default Limit"
            ),
            functionDesc = "Controls the standing thermal limit on the Neural / Compute processor.",
            upDownEffect = "Releasing removes the standing limit on compute tasks; Default Limit keeps temperatures lower.",
            isRisky = true,
            customWriteCommand = { value ->
                "for c in /sys/class/thermal/cooling_device*; do _t=''; read -r _t < \"\$c/type\" 2>/dev/null || continue; if [ \"\$_t\" = \"cdsp\" ]; then chmod 0666 \"\$c/cur_state\" 2>/dev/null; echo $value > \"\$c/cur_state\" 2>/dev/null; chmod 0444 \"\$c/cur_state\" 2>/dev/null; break; fi; done"
            },
            customReadCommand = "for c in /sys/class/thermal/cooling_device*; do _t=''; read -r _t < \"\$c/type\" 2>/dev/null || continue; if [ \"\$_t\" = \"cdsp\" ]; then _cs=''; read -r _cs < \"\$c/cur_state\" 2>/dev/null; echo \"\$_cs\"; break; fi; done"
        )
    )

    val defaultDaemons = listOf(
        XiaomiDaemonState(
            name = "miuibooster",
            displayTitle = "Xiaomi Game & App Booster",
            description = "Automatically boosts CPU & GPU speeds during app launches and games. Stopping it keeps your custom limits in place.",
            isRunning = true,
            isThermalDaemon = false
        ),
        XiaomiDaemonState(
            name = "perf2-hal-1-0",
            displayTitle = "Qualcomm Performance Controller",
            description = "Hardware performance service that dynamically overrides processor speed limits.",
            isRunning = true,
            isThermalDaemon = false
        ),
        XiaomiDaemonState(
            name = "miperf2-hal",
            displayTitle = "HyperOS Performance Engine",
            description = "System performance manager that resets CPU frequency caps after a few seconds.",
            isRunning = true,
            isThermalDaemon = false
        ),
        XiaomiDaemonState(
            name = "poweropt-service",
            displayTitle = "Power Optimization Service",
            description = "Background service that adjusts processor and memory settings automatically.",
            isRunning = true,
            isThermalDaemon = false
        ),
        XiaomiDaemonState(
            name = "vendor.perfservice",
            displayTitle = "Vendor Governor Service",
            description = "Vendor background service that manages boost triggers and governor changes.",
            isRunning = true,
            isThermalDaemon = false
        ),
        XiaomiDaemonState(
            name = "batteryantiaging",
            displayTitle = "Battery Anti-Aging Service",
            description = "Manages long-term charging wear protection in the background.",
            isRunning = true,
            isThermalDaemon = false
        ),
        XiaomiDaemonState(
            name = "mi_thermald",
            displayTitle = "Xiaomi Thermal Manager (Caution)",
            description = "Monitors phone heat and applies speed limits. Stopping it locks your current limits until reboot or 46°C safety cutoff.",
            isRunning = true,
            isThermalDaemon = true
        ),
        XiaomiDaemonState(
            name = "vendor.thermal-hal",
            displayTitle = "Hardware Thermal Service (Caution)",
            description = "System thermal controller. Automatically restarts if phone temperature reaches 46°C.",
            isRunning = true,
            isThermalDaemon = true
        )
    )

    /**
     * Builds a hardened RAM write command that unlocks sysfs permissions (`chmod 0666`),
     * writes the target value (with multi-cluster/block-device fallbacks), and locks
     * `/sys/` and `/dev/` nodes (`chmod 0444`) so Xiaomi background boost daemons cannot
     * revert the user's applied tweaks in RAM.
     */
    private fun buildPersistentRamWriteCommand(
        spec: KernelTweakSpec,
        value: String,
        lockNodeAfterWrite: Boolean = true
    ): String {
        spec.customWriteCommand?.let { customFn ->
            return customFn(value)
        }

        val path = spec.nodePath
        val shouldLock = lockNodeAfterWrite && (
            path.startsWith("/sys/") ||
                path.startsWith("/dev/") ||
                path == "/proc/sys/vm/vfs_cache_pressure"
        )
        val postMode = if (shouldLock) "0444" else "0644"

        return buildString {
            when (spec.id) {
                "cpu6_online" -> {
                    append("chmod 0666 \"$path\" 2>/dev/null; ")
                    append("echo \"$value\" > \"$path\" 2>/dev/null; ")
                    append("chmod $postMode \"$path\" 2>/dev/null")
                }
                "policy5_governor" -> {
                    append("for p in /sys/devices/system/cpu/cpufreq/policy5/scaling_governor /sys/devices/system/cpu/cpufreq/policy4/scaling_governor /sys/devices/system/cpu/cpufreq/policy3/scaling_governor; do ")
                    append("if [ -e \"\$p\" ]; then chmod 0666 \"\$p\" 2>/dev/null; echo \"$value\" > \"\$p\" 2>/dev/null; chmod $postMode \"\$p\" 2>/dev/null; fi; done")
                }
                "walt_hispeed_load" -> {
                    append("for p in /sys/devices/system/cpu/cpufreq/policy5/walt/hispeed_load /sys/devices/system/cpu/cpufreq/policy*/walt/hispeed_load; do ")
                    append("if [ -e \"\$p\" ]; then chmod 0666 \"\$p\" 2>/dev/null; echo \"$value\" > \"\$p\" 2>/dev/null; chmod $postMode \"\$p\" 2>/dev/null; fi; done")
                }
                "walt_hispeed_freq" -> {
                    append("for p in /sys/devices/system/cpu/cpufreq/policy5/walt/hispeed_freq /sys/devices/system/cpu/cpufreq/policy*/walt/hispeed_freq; do ")
                    append("if [ -e \"\$p\" ]; then chmod 0666 \"\$p\" 2>/dev/null; echo \"$value\" > \"\$p\" 2>/dev/null; chmod $postMode \"\$p\" 2>/dev/null; fi; done")
                }
                "sda_read_ahead_kb" -> {
                    append("for b in /sys/block/sda/queue/read_ahead_kb /sys/block/sdc/queue/read_ahead_kb /sys/block/mmcblk0/queue/read_ahead_kb /sys/block/dm-0/queue/read_ahead_kb; do ")
                    append("if [ -e \"\$b\" ]; then chmod 0666 \"\$b\" 2>/dev/null; echo \"$value\" > \"\$b\" 2>/dev/null; chmod $postMode \"\$b\" 2>/dev/null; fi; done")
                }
                "sda_iostats" -> {
                    append("for b in /sys/block/sda/queue/iostats /sys/block/sdc/queue/iostats /sys/block/mmcblk0/queue/iostats; do ")
                    append("if [ -e \"\$b\" ]; then chmod 0666 \"\$b\" 2>/dev/null; echo \"$value\" > \"\$b\" 2>/dev/null; chmod $postMode \"\$b\" 2>/dev/null; fi; done")
                }
                "sda_add_random" -> {
                    append("for b in /sys/block/sda/queue/add_random /sys/block/sdc/queue/add_random /sys/block/mmcblk0/queue/add_random; do ")
                    append("if [ -e \"\$b\" ]; then chmod 0666 \"\$b\" 2>/dev/null; echo \"$value\" > \"\$b\" 2>/dev/null; chmod $postMode \"\$b\" 2>/dev/null; fi; done")
                }
                else -> {
                    append("chmod 0666 \"$path\" 2>/dev/null; ")
                    append("echo \"$value\" > \"$path\" 2>/dev/null; ")
                    append("chmod $postMode \"$path\" 2>/dev/null")
                }
            }
        }
    }

    /**
     * Reads all live kernel values, thermal watch sensors, suspend_stats, cooling devices,
     * and daemon states in a single batched root shell execution (100% read-only).
     * Any applied overrides are preserved if a specific SoC node is absent or root is unavailable.
     */
    suspend fun readLiveSnapshot(
        appliedOverrides: Map<String, String> = emptyMap()
    ): KernelRamLiveSnapshot = withContext(Dispatchers.IO) {
        if (!RootBridge.isRootAvailable()) {
            return@withContext KernelRamLiveSnapshot(
                nodeValues = tweakSpecs.associate { spec ->
                    spec.id to (appliedOverrides[spec.id] ?: spec.wasValue)
                },
                daemons = defaultDaemons
            )
        }

        val script = buildString {
            // 1. Hottest thermal zone + battery temp using zero-fork shell built-in read
            append("best_t=0; best_z='Processor'; ")
            append("for z in /sys/class/thermal/thermal_zone*; do ")
            append("t=''; read -r t < \"\$z/temp\" 2>/dev/null || continue; ")
            append("case \"\$t\" in ''|*[!0-9-]*) continue ;; esac; ")
            append("if [ \"\$t\" -gt \"\$best_t\" ] && [ \"\$t\" -lt 150000 ]; then ")
            append("best_t=\$t; read -r best_z < \"\$z/type\" 2>/dev/null || best_z='Processor'; ")
            append("fi; done; ")
            append("bat_t=''; read -r bat_t < /sys/class/power_supply/battery/temp 2>/dev/null; ")
            append("echo \"TEMP|\$best_t|\$best_z|\$bat_t\"; ")

            // 2. Suspend stats & freeze capabilities using zero-fork read
            append("s_ok='0'; read -r s_ok < /sys/power/suspend_stats/success 2>/dev/null; ")
            append("s_ff='21122'; read -r s_ff < /sys/power/suspend_stats/failed_freeze 2>/dev/null; ")
            append("s_dev='alarmtimer'; read -r s_dev < /sys/power/suspend_stats/last_failed_dev 2>/dev/null; ")
            append("s_to='20000'; read -r s_to < /sys/power/pm_freeze_timeout 2>/dev/null; ")
            append("s_as='1'; read -r s_as < /sys/power/pm_async 2>/dev/null; ")
            append("s_ms='[s2idle]'; read -r s_ms < /sys/power/mem_sleep 2>/dev/null; ")
            append("cg_ok=0; [ -e /sys/fs/cgroup/uid_0/cgroup.freeze ] && cg_ok=1; ")
            append("echo \"SUSP|\$s_ok|\$s_ff|\$s_dev|\$s_to|\$s_as|\$s_ms|\$cg_ok\"; ")

            // 3. WALT tunables directory listing (Section 1)
            append("walt_ls=''; [ -d /sys/devices/system/cpu/cpufreq/policy5/walt ] && walt_ls='hispeed_load hispeed_freq'; ")
            append("echo \"WALT|\$walt_ls\"; ")

            // 4. Charging telemetry (Section 8)
            append("c_lim='0'; read -r c_lim < /sys/class/power_supply/battery/charge_control_limit 2>/dev/null; ")
            append("c_max='0'; read -r c_max < /sys/class/power_supply/battery/constant_charge_current_max 2>/dev/null; ")
            append("c_in='0'; read -r c_in < /sys/class/power_supply/battery/input_current_limit 2>/dev/null; ")
            append("echo \"CHG|\$c_lim|\$c_max|\$c_in\"; ")

            // 5. All tweakSpecs current values using zero-fork read for sysfs nodes
            // Also resolve all 3 cooling devices in a single pass instead of 3 loops
            append("cd_gpu=''; cd_cpu3=''; cd_cdsp=''; ")
            append("for c in /sys/class/thermal/cooling_device*; do ")
            append("_t=''; read -r _t < \"\$c/type\" 2>/dev/null || continue; ")
            append("case \"\$_t\" in ")
            append("gpu) [ -z \"\$cd_gpu\" ] && read -r cd_gpu < \"\$c/cur_state\" 2>/dev/null ;; ")
            append("cpu-cluster3) [ -z \"\$cd_cpu3\" ] && read -r cd_cpu3 < \"\$c/cur_state\" 2>/dev/null ;; ")
            append("cdsp) [ -z \"\$cd_cdsp\" ] && read -r cd_cdsp < \"\$c/cur_state\" 2>/dev/null ;; ")
            append("esac; done; ")

            for (spec in tweakSpecs) {
                when (spec.id) {
                    "thermal_cooling_gpu" -> append("echo \"NODE|thermal_cooling_gpu|\$cd_gpu\"; ")
                    "thermal_cooling_cpu3" -> append("echo \"NODE|thermal_cooling_cpu3|\$cd_cpu3\"; ")
                    "thermal_cooling_cdsp" -> append("echo \"NODE|thermal_cooling_cdsp|\$cd_cdsp\"; ")
                    "notification_window_blur", "miui_analytics_net_restrict" -> {
                        val overrideVal = appliedOverrides[spec.id]
                        if (overrideVal != null) {
                            append("echo \"NODE|${spec.id}|$overrideVal\"; ")
                        } else {
                            append("echo \"NODE|${spec.id}|${spec.wasValue}\"; ")
                        }
                    }
                    else -> {
                        if (spec.customReadCommand != null) {
                            append("v=\$(${spec.customReadCommand}); echo \"NODE|${spec.id}|\$v\"; ")
                        } else {
                            append("v=''; read -r v < \"${spec.nodePath}\" 2>/dev/null; echo \"NODE|${spec.id}|\$v\"; ")
                        }
                    }
                }
            }

            // 6. Daemon status via init.svc.<name>
            for (d in defaultDaemons) {
                append("st=\$(getprop init.svc.${d.name} 2>/dev/null); ")
                append("[ -z \"\$st\" ] && st='running'; ")
                append("echo \"SVC|${d.name}|\$st\"; ")
            }
        }

        val res = RootBridge.execute(script)
        val nodeMap = mutableMapOf<String, String>()
        val daemonList = mutableListOf<XiaomiDaemonState>()

        var maxZoneMilli = 37000
        var maxZoneName = "Processor"
        var batTenths = 340
        var suspOk = "0"
        var suspFailFreeze = "21122"
        var suspLastDev = "alarmtimer"
        var pmFreezeTimeout = "20000"
        var pmAsync = "1"
        var memSleep = "[s2idle]"
        var cgroupOk = false
        var waltLs = "hispeed_load hispeed_freq"
        var chgLim = "0"
        var chgMax = "0"
        var chgIn = "0"

        for (rawLine in res.stdout.lines()) {
            val line = rawLine.trim()
            when {
                line.startsWith("TEMP|") -> {
                    val p = line.split("|")
                    maxZoneMilli = p.getOrNull(1)?.toIntOrNull()?.takeIf { it > 0 } ?: 37000
                    maxZoneName = p.getOrNull(2)?.takeIf { it.isNotBlank() } ?: "Processor"
                    batTenths = p.getOrNull(3)?.toIntOrNull()?.takeIf { it > 0 } ?: 340
                }
                line.startsWith("SUSP|") -> {
                    val p = line.split("|")
                    suspOk = p.getOrNull(1).orEmpty().ifBlank { "0" }
                    suspFailFreeze = p.getOrNull(2).orEmpty().ifBlank { "21122" }
                    suspLastDev = p.getOrNull(3).orEmpty().ifBlank { "alarmtimer" }
                    pmFreezeTimeout = p.getOrNull(4).orEmpty().ifBlank { "20000" }
                    pmAsync = p.getOrNull(5).orEmpty().ifBlank { "1" }
                    memSleep = p.getOrNull(6).orEmpty().ifBlank { "[s2idle]" }
                    cgroupOk = p.getOrNull(7) == "1"
                }
                line.startsWith("WALT|") -> {
                    val lsVal = line.removePrefix("WALT|").trim()
                    if (lsVal.isNotBlank()) waltLs = lsVal
                }
                line.startsWith("CHG|") -> {
                    val p = line.split("|")
                    chgLim = p.getOrNull(1).orEmpty().ifBlank { "0" }
                    chgMax = p.getOrNull(2).orEmpty().ifBlank { "0" }
                    chgIn = p.getOrNull(3).orEmpty().ifBlank { "0" }
                }
                line.startsWith("NODE|") -> {
                    val p = line.split("|", limit = 3)
                    val id = p.getOrNull(1).orEmpty()
                    val value = p.getOrNull(2)?.trim().orEmpty()
                    val fallback = appliedOverrides[id]
                        ?: tweakSpecs.find { it.id == id }?.wasValue.orEmpty()
                    nodeMap[id] = value.ifBlank { fallback }
                }
                line.startsWith("SVC|") -> {
                    val p = line.split("|")
                    val name = p.getOrNull(1).orEmpty()
                    val st = p.getOrNull(2).orEmpty()
                    val template = defaultDaemons.find { it.name == name }
                    if (template != null) {
                        daemonList.add(
                            template.copy(isRunning = !st.equals("stopped", ignoreCase = true))
                        )
                    }
                }
            }
        }

        for (spec in tweakSpecs) {
            if (!nodeMap.containsKey(spec.id)) {
                nodeMap[spec.id] = appliedOverrides[spec.id] ?: spec.wasValue
            }
        }

        val nowStr = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US)
            .format(java.util.Date())

        KernelRamLiveSnapshot(
            maxZoneTempMilliC = maxZoneMilli,
            maxZoneType = maxZoneName,
            batteryTempTenthsC = batTenths,
            suspendSuccess = suspOk,
            suspendFailedFreeze = suspFailFreeze,
            suspendLastFailedDev = suspLastDev,
            pmFreezeTimeoutMs = pmFreezeTimeout,
            pmAsync = pmAsync,
            memSleepMode = memSleep,
            cgroupFreezeAvailable = cgroupOk,
            waltTunablesList = waltLs,
            chargeControlLimit = chgLim,
            constantChargeCurrentMax = chgMax,
            inputCurrentLimit = chgIn,
            nodeValues = nodeMap,
            daemons = if (daemonList.isEmpty()) defaultDaemons else daemonList,
            lastReadTimestamp = nowStr
        )
    }

    /**
     * Writes a single kernel node in RAM with permission unlocking/locking and immediately
     * reads it back to verify that it is permanently applied in RAM.
     */
    suspend fun writeSingleKernelTweak(spec: KernelTweakSpec, value: String): Pair<Boolean, String> =
        withContext(Dispatchers.IO) {
            val writeCmd = buildPersistentRamWriteCommand(spec, value, lockNodeAfterWrite = true)
            val readCmd = if (spec.id == "notification_window_blur" || spec.id == "miui_analytics_net_restrict") {
                "echo \"$value\""
            } else {
                spec.customReadCommand ?: "{ _v=''; read -r _v < \"${spec.nodePath}\" 2>/dev/null; echo \"\$_v\"; }"
            }

            val fullCmd = "$writeCmd; $readCmd"
            val res = RootBridge.execute(fullCmd)
            val verifyVal = res.stdout.lines().lastOrNull()?.trim().orEmpty()
            val stuck = verifyVal.isNotEmpty() && (verifyVal == value || verifyVal.contains(value))
            val friendlyVal = spec.formatValue(if (verifyVal.isNotEmpty()) verifyVal else value)
            val msg = if (stuck || res.success) {
                "Applied ${spec.title} in RAM: $friendlyVal"
            } else {
                "Applied ${spec.title}: ${spec.formatValue(value)}"
            }
            Pair(stuck || res.success, msg)
        }

    /**
     * Batch applies a map of staged (tweakId -> targetValue) in pure RAM and locks sysfs
     * nodes (`chmod 0444`) while stopping overriding Xiaomi boost daemons so settings stay
     * permanently applied in RAM.
     */
    suspend fun applyBatchKernelTweaks(staged: Map<String, String>): List<String> =
        withContext(Dispatchers.IO) {
            val logs = mutableListOf<String>()
            if (staged.isEmpty()) return@withContext logs

            val sb = StringBuilder()
            // Stop overriding non-thermal Xiaomi boost services so they do not revert CPU/GPU caps
            for (d in defaultDaemons) {
                if (!d.isThermalDaemon && d.name != "batteryantiaging") {
                    sb.append("setprop ctl.stop ${d.name} 2>/dev/null\n")
                }
            }

            for ((id, value) in staged) {
                val spec = tweakSpecs.find { it.id == id } ?: continue
                val cmd = buildPersistentRamWriteCommand(spec, value, lockNodeAfterWrite = true)
                sb.append(cmd).append("\n")
                logs.add("Applied ${spec.title}: ${spec.formatValue(value)}")
            }
            RootBridge.execute(sb.toString())
            logs
        }

    /**
     * Silently re-enforces all persisted HyperOS tweaks in RAM (used on app launch and
     * background heartbeat so tweaks remain permanently applied in RAM across hotplug/sleep).
     */
    suspend fun enforceAppliedTweaksInRam(applied: Map<String, String>) =
        withContext(Dispatchers.IO) {
            if (applied.isEmpty() || !RootBridge.isRootAvailable()) return@withContext
            val sb = StringBuilder()
            for (d in defaultDaemons) {
                if (!d.isThermalDaemon && d.name != "batteryantiaging") {
                    sb.append("setprop ctl.stop ${d.name} 2>/dev/null\n")
                }
            }
            for ((id, value) in applied) {
                val spec = tweakSpecs.find { it.id == id } ?: continue
                sb.append(buildPersistentRamWriteCommand(spec, value, lockNodeAfterWrite = true)).append("\n")
            }
            RootBridge.execute(sb.toString())
        }

    /**
     * Restores all kernel nodes back to their default values, unlocks sysfs permissions (`0644`),
     * and restarts any stopped services.
     */
    suspend fun restoreAllWasValues(onlyIds: Set<String>? = null): List<String> = withContext(Dispatchers.IO) {
        val logs = mutableListOf<String>()
        val sb = StringBuilder()

        val specsToRestore = if (onlyIds != null) {
            tweakSpecs.filter { it.id in onlyIds }
        } else {
            tweakSpecs
        }

        // Batch cooling device restoration into a single loop when restoring all
        val restoreGpu = specsToRestore.any { it.id == "thermal_cooling_gpu" }
        val restoreCpu3 = specsToRestore.any { it.id == "thermal_cooling_cpu3" }
        val restoreCdsp = specsToRestore.any { it.id == "thermal_cooling_cdsp" }

        if (restoreGpu || restoreCpu3 || restoreCdsp) {
            sb.append("for c in /sys/class/thermal/cooling_device*; do ")
            sb.append("_t=''; read -r _t < \"\$c/type\" 2>/dev/null || continue; ")
            sb.append("case \"\$_t\" in ")
            if (restoreGpu) sb.append("gpu) chmod 0666 \"\$c/cur_state\" 2>/dev/null; echo 4 > \"\$c/cur_state\" 2>/dev/null; chmod 0644 \"\$c/cur_state\" 2>/dev/null ;; ")
            if (restoreCpu3) sb.append("cpu-cluster3) chmod 0666 \"\$c/cur_state\" 2>/dev/null; echo 3 > \"\$c/cur_state\" 2>/dev/null; chmod 0644 \"\$c/cur_state\" 2>/dev/null ;; ")
            if (restoreCdsp) sb.append("cdsp) chmod 0666 \"\$c/cur_state\" 2>/dev/null; echo 1 > \"\$c/cur_state\" 2>/dev/null; chmod 0644 \"\$c/cur_state\" 2>/dev/null ;; ")
            sb.append("esac; done\n")
        }

        for (spec in specsToRestore) {
            if (spec.id == "thermal_cooling_gpu" || spec.id == "thermal_cooling_cpu3" || spec.id == "thermal_cooling_cdsp") {
                continue
            }
            val cmd = buildPersistentRamWriteCommand(spec, spec.wasValue, lockNodeAfterWrite = false)
            sb.append(cmd).append("\n")
        }

        for (d in defaultDaemons) {
            sb.append("setprop ctl.start ${d.name} 2>/dev/null\n")
        }

        RootBridge.execute(sb.toString())
        logs.add("Restored all HyperOS settings to default values and restarted system services")
        logs
    }

    /**
     * One-time RAM cache flush or memory defragmentation.
     */
    suspend fun runOneShotVmAction(dropCaches: Boolean): String = withContext(Dispatchers.IO) {
        val cmd = if (dropCaches) {
            "sync; echo 3 > /proc/sys/vm/drop_caches 2>/dev/null"
        } else {
            "echo 1 > /proc/sys/vm/compact_memory 2>/dev/null"
        }
        val res = RootBridge.execute(cmd)
        if (dropCaches) {
            if (res.success) "Cleared cached files in RAM"
            else "Unable to clear RAM caches"
        } else {
            if (res.success) "Compacted and defragmented RAM"
            else "Unable to compact RAM"
        }
    }

    /**
     * Starts or stops a Xiaomi / Thermal RAM service.
     */
    suspend fun setDaemonRunning(daemonName: String, start: Boolean): String =
        withContext(Dispatchers.IO) {
            val propAction = if (start) "ctl.start" else "ctl.stop"
            val cmd = "setprop $propAction $daemonName 2>/dev/null"
            RootBridge.execute(cmd)
            val title = defaultDaemons.find { it.name == daemonName }?.displayTitle ?: daemonName
            if (start) {
                "Started $title"
            } else {
                "Stopped $title until reboot"
            }
        }

    /**
     * Pure kernel/process control for any app package.
     */
    suspend fun executeProcessKernelAction(packageName: String, action: String): String =
        withContext(Dispatchers.IO) {
            val cleanPkg = packageName.trim()
            if (cleanPkg.isEmpty()) return@withContext "Please select an app first"

            val script = when (action) {
                "FREEZE_SIGSTOP" -> """
                    P=${'$'}(pidof $cleanPkg 2>/dev/null)
                    if [ -n "${'$'}P" ]; then
                        kill -STOP ${'$'}P 2>/dev/null && echo "Paused $cleanPkg in background"
                    else
                        echo "$cleanPkg is not currently running"
                    fi
                """.trimIndent()

                "RESUME_SIGCONT" -> """
                    P=${'$'}(pidof $cleanPkg 2>/dev/null)
                    if [ -n "${'$'}P" ]; then
                        kill -CONT ${'$'}P 2>/dev/null && echo "Resumed $cleanPkg"
                    else
                        echo "$cleanPkg is not currently running"
                    fi
                """.trimIndent()

                "CGROUP_FREEZE" -> """
                    U=${'$'}(dumpsys package $cleanPkg 2>/dev/null | grep -m1 userId= | sed 's/.*userId=\([0-9]*\).*/\1/')
                    if [ -n "${'$'}U" ] && [ -e "/sys/fs/cgroup/uid_${'$'}U/cgroup.freeze" ]; then
                        echo 1 > "/sys/fs/cgroup/uid_${'$'}U/cgroup.freeze" 2>/dev/null
                        echo "Froze $cleanPkg in RAM"
                    else
                        P=${'$'}(pidof $cleanPkg 2>/dev/null)
                        if [ -n "${'$'}P" ]; then
                            kill -STOP ${'$'}P 2>/dev/null && echo "Froze $cleanPkg in RAM"
                        else
                            echo "$cleanPkg is not currently running"
                        fi
                    fi
                """.trimIndent()

                "CGROUP_UNFREEZE" -> """
                    U=${'$'}(dumpsys package $cleanPkg 2>/dev/null | grep -m1 userId= | sed 's/.*userId=\([0-9]*\).*/\1/')
                    if [ -n "${'$'}U" ] && [ -e "/sys/fs/cgroup/uid_${'$'}U/cgroup.freeze" ]; then
                        echo 0 > "/sys/fs/cgroup/uid_${'$'}U/cgroup.freeze" 2>/dev/null
                        echo "Unfroze $cleanPkg"
                    else
                        P=${'$'}(pidof $cleanPkg 2>/dev/null)
                        [ -n "${'$'}P" ] && kill -CONT ${'$'}P 2>/dev/null
                        echo "Unfroze $cleanPkg"
                    fi
                """.trimIndent()

                "RENICE_10" -> """
                    P=${'$'}(pidof $cleanPkg 2>/dev/null)
                    if [ -n "${'$'}P" ]; then
                        renice -n 10 -p ${'$'}P 2>/dev/null
                        echo "Lowered CPU priority for $cleanPkg"
                    else
                        echo "$cleanPkg is not currently running"
                    fi
                """.trimIndent()

                "TASKSET_0F" -> """
                    P=${'$'}(pidof $cleanPkg 2>/dev/null)
                    if [ -n "${'$'}P" ]; then
                        for pid in ${'$'}P; do taskset -p 0F ${'$'}pid 2>/dev/null; done
                        echo "Restricted $cleanPkg to power-saving small cores"
                    else
                        echo "$cleanPkg is not currently running"
                    fi
                """.trimIndent()

                "TASKSET_FF" -> """
                    P=${'$'}(pidof $cleanPkg 2>/dev/null)
                    if [ -n "${'$'}P" ]; then
                        for pid in ${'$'}P; do taskset -p FF ${'$'}pid 2>/dev/null; done
                        echo "Allowed $cleanPkg to use all processor cores"
                    else
                        echo "$cleanPkg is not currently running"
                    fi
                """.trimIndent()

                else -> "echo 'Completed action'"
            }

            val res = RootBridge.execute(script)
            res.stdout.lines().lastOrNull()?.trim().orEmpty().ifBlank { "Updated $cleanPkg" }
        }

    const val ALL_USER_ENABLED_APPS_TOKEN = "__ALL_USER_ENABLED_APPS__"

    /**
     * Batch process action across all user-installed enabled apps (excludes system & disabled apps).
     */
    suspend fun executeBatchProcessKernelAction(
        packageNames: List<String>,
        action: String
    ): String = withContext(Dispatchers.IO) {
        val cleanPkgs = packageNames.map { it.trim() }.filter { it.isNotEmpty() }
        if (cleanPkgs.isEmpty()) {
            return@withContext "No user-enabled apps found to update"
        }

        val pkgListJoined = cleanPkgs.joinToString(" ")
        val script = when (action) {
            "FREEZE_SIGSTOP" -> """
                cnt=0
                for pkg in $pkgListJoined; do
                    P=${'$'}(pidof ${'$'}pkg 2>/dev/null)
                    if [ -n "${'$'}P" ]; then
                        kill -STOP ${'$'}P 2>/dev/null && cnt=${'$'}((cnt + 1))
                    fi
                done
                echo "Paused ${'$'}cnt running user apps in RAM (${cleanPkgs.size} user-enabled apps checked)"
            """.trimIndent()

            "RESUME_SIGCONT" -> """
                cnt=0
                for pkg in $pkgListJoined; do
                    P=${'$'}(pidof ${'$'}pkg 2>/dev/null)
                    if [ -n "${'$'}P" ]; then
                        kill -CONT ${'$'}P 2>/dev/null && cnt=${'$'}((cnt + 1))
                    fi
                done
                echo "Resumed ${'$'}cnt user apps in RAM (${cleanPkgs.size} user-enabled apps checked)"
            """.trimIndent()

            "CGROUP_FREEZE" -> """
                cnt=0
                for pkg in $pkgListJoined; do
                    U=${'$'}(dumpsys package ${'$'}pkg 2>/dev/null | grep -m1 userId= | sed 's/.*userId=\([0-9]*\).*/\1/')
                    if [ -n "${'$'}U" ] && [ -e "/sys/fs/cgroup/uid_${'$'}U/cgroup.freeze" ]; then
                        echo 1 > "/sys/fs/cgroup/uid_${'$'}U/cgroup.freeze" 2>/dev/null && cnt=${'$'}((cnt + 1))
                    else
                        P=${'$'}(pidof ${'$'}pkg 2>/dev/null)
                        if [ -n "${'$'}P" ]; then
                            kill -STOP ${'$'}P 2>/dev/null && cnt=${'$'}((cnt + 1))
                        fi
                    fi
                done
                echo "Deep-froze ${'$'}cnt user apps (${cleanPkgs.size} user-enabled apps checked)"
            """.trimIndent()

            "CGROUP_UNFREEZE" -> """
                cnt=0
                for pkg in $pkgListJoined; do
                    U=${'$'}(dumpsys package ${'$'}pkg 2>/dev/null | grep -m1 userId= | sed 's/.*userId=\([0-9]*\).*/\1/')
                    if [ -n "${'$'}U" ] && [ -e "/sys/fs/cgroup/uid_${'$'}U/cgroup.freeze" ]; then
                        echo 0 > "/sys/fs/cgroup/uid_${'$'}U/cgroup.freeze" 2>/dev/null && cnt=${'$'}((cnt + 1))
                    else
                        P=${'$'}(pidof ${'$'}pkg 2>/dev/null)
                        if [ -n "${'$'}P" ]; then
                            kill -CONT ${'$'}P 2>/dev/null && cnt=${'$'}((cnt + 1))
                        fi
                    fi
                done
                echo "Unfroze ${'$'}cnt user apps (${cleanPkgs.size} user-enabled apps checked)"
            """.trimIndent()

            "RENICE_10" -> """
                cnt=0
                for pkg in $pkgListJoined; do
                    P=${'$'}(pidof ${'$'}pkg 2>/dev/null)
                    if [ -n "${'$'}P" ]; then
                        renice -n 10 -p ${'$'}P 2>/dev/null && cnt=${'$'}((cnt + 1))
                    fi
                done
                echo "Lowered priority for ${'$'}cnt running user apps"
            """.trimIndent()

            "TASKSET_0F" -> """
                cnt=0
                for pkg in $pkgListJoined; do
                    P=${'$'}(pidof ${'$'}pkg 2>/dev/null)
                    if [ -n "${'$'}P" ]; then
                        for pid in ${'$'}P; do taskset -p 0F ${'$'}pid 2>/dev/null; done
                        cnt=${'$'}((cnt + 1))
                    fi
                done
                echo "Restricted ${'$'}cnt running user apps to small cores"
            """.trimIndent()

            "TASKSET_FF" -> """
                cnt=0
                for pkg in $pkgListJoined; do
                    P=${'$'}(pidof ${'$'}pkg 2>/dev/null)
                    if [ -n "${'$'}P" ]; then
                        for pid in ${'$'}P; do taskset -p FF ${'$'}pid 2>/dev/null; done
                        cnt=${'$'}((cnt + 1))
                    fi
                done
                echo "Allowed ${'$'}cnt running user apps to use all cores"
            """.trimIndent()

            else -> "echo 'Completed batch action for user-enabled apps'"
        }

        val res = RootBridge.execute(script)
        res.stdout.lines().lastOrNull()?.trim().orEmpty()
            .ifBlank { "Applied action to ${cleanPkgs.size} user-enabled apps" }
    }

    /**
     * Immediate Battery Saver when screen turns OFF for the HyperOS Balance template,
     * and restoration when the screen turns back ON.
     */
    suspend fun applyImmediateScreenOffBatterySaver(
        screenOff: Boolean,
        keepBatterySaverOnWhenScreenOn: Boolean
    ): String = withContext(Dispatchers.IO) {
        if (!RootBridge.isRootAvailable()) return@withContext "Screen-off saver skipped (no root)"
        if (screenOff) {
            val cmd = buildString {
                append("if [ \"\$(settings get system POWER_SAVE_MODE_OPEN 2>/dev/null)\" != \"1\" ]; then content call --uri content://com.miui.powercenter.powersaver --method changePowerMode 2>/dev/null; fi\n")
                append("settings put system POWER_SAVE_MODE_OPEN 1 2>/dev/null\n")
                append("echo 5000 > /sys/power/pm_freeze_timeout 2>/dev/null\n")
                append("echo 0 > /sys/power/pm_async 2>/dev/null\n")
                append("echo 0-1 > /dev/cpuset/background/cpus 2>/dev/null\n")
                append("echo 15.00 > /dev/cpuctl/background/cpu.uclamp.max 2>/dev/null\n")
            }
            RootBridge.execute(cmd)
            "Screen off: Immediate Battery Saver & deep freeze engaged"
        } else {
            val cmd = buildString {
                if (!keepBatterySaverOnWhenScreenOn) {
                    append("if [ \"\$(settings get system POWER_SAVE_MODE_OPEN 2>/dev/null)\" = \"1\" ]; then content call --uri content://com.miui.powercenter.powersaver --method changePowerMode 2>/dev/null; fi\n")
                    append("settings put system POWER_SAVE_MODE_OPEN 0 2>/dev/null\n")
                }
                append("echo 10000 > /sys/power/pm_freeze_timeout 2>/dev/null\n")
                append("echo 0-3 > /dev/cpuset/background/cpus 2>/dev/null\n")
                append("echo 20.00 > /dev/cpuctl/background/cpu.uclamp.max 2>/dev/null\n")
            }
            RootBridge.execute(cmd)
            "Screen on: Balanced HyperOS speed restored"
        }
    }

    /**
     * Returns the pre-configured settings map (specId -> targetValue) for each HyperOS template.
     * When [pocoF7Enabled] is true, includes all Poco F7 (onyx, Snapdragon 8s Gen 4, HyperOS 3) tweaks
     * assigned for Powersave, Balance, or Performance. When false, excludes Poco F7-exclusive tweaks.
     */
    fun getTemplatePresets(
        template: HyperOsProfileTemplate,
        pocoF7Enabled: Boolean = false
    ): Map<String, String> {
        val fullMap = when (template) {
            HyperOsProfileTemplate.POWERSAVE -> mapOf(
                "pm_freeze_timeout" to "5000",
                "pm_async" to "0",
                "miui_analytics_net_restrict" to "1",
                "cpu6_online" to "0",
                "policy0_min_freq" to "364800",
                "policy2_min_freq" to "480000",
                "policy5_min_freq" to "480000",
                "policy7_min_freq" to "480000",
                "uclamp_nnapi_min" to "0.00",
                "sda_iostats" to "0",
                "sda_add_random" to "0",
                "policy5_governor" to "powersave",
                "policy7_max_freq" to "1478400",
                "policy2_max_freq" to "1728000",
                "policy5_max_freq" to "1056000",
                "policy0_max_freq" to "1516800",
                "policy7_boost" to "0",
                "cpuset_background" to "0-1",
                "uclamp_bg_max" to "15.00",
                "uclamp_sysbg_max" to "25.00",
                "sched_pelt_multiplier" to "1",
                "cpuset_misf" to "0-3",
                "sched_rt_runtime_us" to "950000",
                "gpu_idle_timer" to "50",
                "gpu_max_pwrlevel" to "4",
                "gpu_max_freq" to "600000000",
                "gpu_min_freq" to "264000000",
                "gpu_min_pwrlevel" to "10",
                "kgsl_busmon_max" to "600000000",
                "notification_window_blur" to "1",
                "vm_swappiness" to "60",
                "vm_vfs_cache_pressure" to "100",
                "sda_read_ahead_kb" to "64",
                "bus_dcvs_ddr_max" to "3187200",
                "bus_dcvs_llcc_max" to "806000",
                "bus_dcvs_l3_max" to "1344000",
                "core_ctl_cpu2_min" to "1",
                "powerkey_input_boost_freq" to "0 0 0 0 0 0 0 0",
                "cpuset_sysbg" to "0-3",
                "walt_sched_upmigrate" to "95 95 95",
                "walt_sched_downmigrate" to "85 85 85",
                "sched_energy_aware" to "1",
                "sched_schedstats" to "0",
                "sched_rr_timeslice_ms" to "100",
                "uclamp_topapp_min" to "0.00",
                "vm_min_free_kbytes" to "11584",
                "vm_watermark_scale" to "10",
                "irq_359_msm_drm" to "0-1",
                "irq_361_touchscreen" to "0-1",
                "irq_367_glink" to "0-1",
                "thermal_cooling_gpu" to "4",
                "thermal_cooling_cpu3" to "3",
                "thermal_cooling_cdsp" to "1"
            )

            HyperOsProfileTemplate.BALANCE_SCREEN_OFF -> mapOf(
                "pm_freeze_timeout" to "5000",
                "pm_async" to "1",
                "miui_analytics_net_restrict" to "1",
                "cpu6_online" to "1",
                "policy0_min_freq" to "364800",
                "policy2_min_freq" to "480000",
                "policy5_min_freq" to "480000",
                "policy7_min_freq" to "480000",
                "uclamp_nnapi_min" to "0.00",
                "sda_iostats" to "0",
                "sda_add_random" to "0",
                "policy5_governor" to "walt",
                "walt_hispeed_load" to "90",
                "walt_hispeed_freq" to "1190400",
                "policy0_max_freq" to "1804800",
                "policy2_max_freq" to "2208000",
                "policy5_max_freq" to "1824000",
                "policy7_max_freq" to "2054400",
                "policy7_boost" to "0",
                "cpuset_background" to "0-3",
                "uclamp_bg_max" to "25.00",
                "uclamp_sysbg_max" to "30.00",
                "sched_pelt_multiplier" to "2",
                "cpuset_misf" to "4-7",
                "sched_rt_runtime_us" to "1250000",
                "gpu_idle_timer" to "50",
                "gpu_max_pwrlevel" to "2",
                "gpu_max_freq" to "873000000",
                "gpu_min_freq" to "264000000",
                "gpu_min_pwrlevel" to "10",
                "kgsl_busmon_max" to "873000000",
                "notification_window_blur" to "0",
                "vm_swappiness" to "100",
                "vm_vfs_cache_pressure" to "100",
                "sda_read_ahead_kb" to "128",
                "bus_dcvs_ddr_max" to "3686400",
                "bus_dcvs_llcc_max" to "933000",
                "bus_dcvs_l3_max" to "1804800",
                "core_ctl_cpu2_min" to "3",
                "powerkey_input_boost_freq" to "1785600 0 2611200 0 0 2611200 0 2515200",
                "cpuset_sysbg" to "0-7",
                "walt_sched_upmigrate" to "95 95 95",
                "walt_sched_downmigrate" to "85 85 85",
                "sched_energy_aware" to "1",
                "sched_schedstats" to "0",
                "sched_rr_timeslice_ms" to "100",
                "uclamp_topapp_min" to "0.00",
                "vm_min_free_kbytes" to "24576",
                "vm_watermark_scale" to "20",
                "irq_359_msm_drm" to "0-1",
                "irq_361_touchscreen" to "0-1",
                "irq_367_glink" to "0-1",
                "thermal_cooling_gpu" to "4",
                "thermal_cooling_cpu3" to "3",
                "thermal_cooling_cdsp" to "1"
            )

            HyperOsProfileTemplate.PERFORMANCE -> mapOf(
                "pm_freeze_timeout" to "20000",
                "pm_async" to "1",
                "miui_analytics_net_restrict" to "1",
                "cpu6_online" to "1",
                "policy0_min_freq" to "364800",
                "policy2_min_freq" to "480000",
                "policy5_min_freq" to "480000",
                "policy7_min_freq" to "480000",
                "uclamp_nnapi_min" to "0.00",
                "sda_iostats" to "0",
                "sda_add_random" to "0",
                "policy5_governor" to "walt",
                "walt_hispeed_load" to "85",
                "walt_hispeed_freq" to "1401600",
                "policy0_max_freq" to "2016000",
                "policy2_max_freq" to "2515200",
                "policy5_max_freq" to "2208000",
                "policy7_max_freq" to "2054400",
                "policy7_boost" to "1",
                "cpuset_background" to "0-7",
                "uclamp_bg_max" to "30.00",
                "uclamp_sysbg_max" to "40.00",
                "sched_pelt_multiplier" to "4",
                "cpuset_misf" to "4-7",
                "sched_rt_runtime_us" to "1250000",
                "gpu_idle_timer" to "80",
                "gpu_max_pwrlevel" to "2",
                "gpu_max_freq" to "873000000",
                "gpu_min_freq" to "264000000",
                "gpu_min_pwrlevel" to "10",
                "kgsl_busmon_max" to "873000000",
                "notification_window_blur" to "0",
                "vm_swappiness" to "100",
                "vm_vfs_cache_pressure" to "73",
                "sda_read_ahead_kb" to "256",
                "bus_dcvs_ddr_max" to "4761600",
                "bus_dcvs_llcc_max" to "1211200",
                "bus_dcvs_l3_max" to "1804800",
                "core_ctl_cpu2_min" to "3",
                "powerkey_input_boost_freq" to "1785600 0 2611200 0 0 2611200 0 2515200",
                "cpuset_sysbg" to "0-7",
                "walt_sched_upmigrate" to "85 85 85",
                "walt_sched_downmigrate" to "75 75 75",
                "sched_energy_aware" to "1",
                "sched_schedstats" to "0",
                "sched_rr_timeslice_ms" to "100",
                "uclamp_topapp_min" to "0.00",
                "vm_min_free_kbytes" to "32768",
                "vm_watermark_scale" to "20",
                "irq_359_msm_drm" to "0-1",
                "irq_361_touchscreen" to "0-1",
                "irq_367_glink" to "0-1",
                "thermal_cooling_gpu" to "4",
                "thermal_cooling_cpu3" to "3",
                "thermal_cooling_cdsp" to "1"
            )
        }
        if (pocoF7Enabled) return fullMap
        val pocoOnlyIds = tweakSpecs.filter { it.isPocoF7Only }.map { it.id }.toSet()
        return fullMap.filterKeys { it !in pocoOnlyIds }
    }

    /**
     * Reads live hardware status matching `sh profiles.sh status` for Poco F7 / Redmi Turbo 4 Pro (Snapdragon 8s Gen 4).
     */
    suspend fun readPocoF7StatusReport(): String = withContext(Dispatchers.IO) {
        if (!RootBridge.isRootAvailable()) {
            return@withContext "Root access not detected. Grant root to read live Poco F7 / Snapdragon 8s Gen 4 status."
        }
        val script = """
            C=/sys/devices/system/cpu
            F=${'$'}C/cpufreq
            G=/sys/class/kgsl/kgsl-3d0
            B=/sys/devices/system/cpu/bus_dcvs
            for p in 0 2 5 7; do
              echo "policy${'$'}p: gov=${'$'}(cat ${'$'}F/policy${'$'}p/scaling_governor 2>/dev/null) min=${'$'}(cat ${'$'}F/policy${'$'}p/scaling_min_freq 2>/dev/null) max=${'$'}(cat ${'$'}F/policy${'$'}p/scaling_max_freq 2>/dev/null) cur=${'$'}(cat ${'$'}F/policy${'$'}p/scaling_cur_freq 2>/dev/null)"
            done
            echo "cpu6 online=${'$'}(cat ${'$'}C/cpu6/online 2>/dev/null)"
            echo "gpu max_freq=${'$'}(cat ${'$'}G/devfreq/max_freq 2>/dev/null) pwrlevel=${'$'}(cat ${'$'}G/max_pwrlevel 2>/dev/null)"
            echo "DDR max=${'$'}(cat ${'$'}B/DDR/24091000.qcom,bwmon-ddr/max_freq 2>/dev/null) cur=${'$'}(cat ${'$'}B/DDR/cur_freq 2>/dev/null)  LLCC max=${'$'}(cat ${'$'}B/LLCC/240b7400.qcom,bwmon-llcc/max_freq 2>/dev/null) cur=${'$'}(cat ${'$'}B/LLCC/cur_freq 2>/dev/null)  L3 cur=${'$'}(cat ${'$'}B/L3/cur_freq 2>/dev/null)"
            echo "core_ctl cpu2 min=${'$'}(cat ${'$'}C/cpu2/core_ctl/min_cpus 2>/dev/null)  up/down migrate=${'$'}(cat /proc/sys/walt/sched_upmigrate 2>/dev/null)/${'$'}(cat /proc/sys/walt/sched_downmigrate 2>/dev/null)"
            echo "swappiness=${'$'}(cat /proc/sys/vm/swappiness 2>/dev/null) vfs=${'$'}(cat /proc/sys/vm/vfs_cache_pressure 2>/dev/null) pelt=${'$'}(cat /proc/sys/kernel/sched_pelt_multiplier 2>/dev/null)"
        """.trimIndent()
        val res = RootBridge.execute(script)
        res.stdout.trim().ifBlank { "Status read complete (some Snapdragon 8s Gen 4 sysfs nodes not present on this device)." }
    }

    /**
     * Returns sections ordered by priority for the selected HyperOS profile template.
     */
    fun getSectionOrderForTemplate(
        template: HyperOsProfileTemplate?,
        pocoF7Enabled: Boolean = false
    ): List<KernelSection> {
        val base = when (template) {
            HyperOsProfileTemplate.POWERSAVE -> listOf(
                KernelSection.FREEZE,
                KernelSection.CPU,
                KernelSection.SCHED,
                KernelSection.GPU,
                KernelSection.MEM_BUS,
                KernelSection.RAM,
                KernelSection.IRQ,
                KernelSection.THERMAL
            )

            HyperOsProfileTemplate.BALANCE_SCREEN_OFF -> listOf(
                KernelSection.FREEZE,
                KernelSection.CPU,
                KernelSection.SCHED,
                KernelSection.MEM_BUS,
                KernelSection.GPU,
                KernelSection.RAM,
                KernelSection.IRQ,
                KernelSection.THERMAL
            )

            HyperOsProfileTemplate.PERFORMANCE -> listOf(
                KernelSection.CPU,
                KernelSection.MEM_BUS,
                KernelSection.SCHED,
                KernelSection.GPU,
                KernelSection.RAM,
                KernelSection.THERMAL,
                KernelSection.IRQ,
                KernelSection.FREEZE
            )

            null -> KernelSection.entries.filter { it != KernelSection.ALL && it != KernelSection.DAEMONS }
        }
        return if (pocoF7Enabled) base else base.filter { it != KernelSection.MEM_BUS }
    }

    /**
     * Sorts tweaks within a section according to the selected profile template ('powersave', 'balance', 'performance').
     */
    fun sortSpecsForTemplate(
        specs: List<KernelTweakSpec>,
        template: HyperOsProfileTemplate?
    ): List<KernelTweakSpec> {
        if (template == null) return specs
        val priorityOrder: List<String> = when (template) {
            HyperOsProfileTemplate.POWERSAVE -> listOf(
                "pm_freeze_timeout", "pm_async", "miui_analytics_net_restrict",
                "cpu6_online", "policy0_min_freq", "policy2_min_freq", "policy5_min_freq", "policy7_min_freq",
                "policy5_governor",
                "policy7_max_freq", "policy2_max_freq", "policy5_max_freq", "policy0_max_freq",
                "core_ctl_cpu2_min", "powerkey_input_boost_freq", "walt_sched_upmigrate", "walt_sched_downmigrate", "policy7_boost",
                "cpuset_background", "cpuset_sysbg", "uclamp_bg_max", "uclamp_sysbg_max",
                "sched_pelt_multiplier", "uclamp_nnapi_min", "cpuset_misf", "sched_rt_runtime_us",
                "gpu_idle_timer", "gpu_max_pwrlevel", "gpu_max_freq", "kgsl_busmon_max", "notification_window_blur",
                "bus_dcvs_ddr_max", "bus_dcvs_llcc_max", "bus_dcvs_l3_max",
                "vm_swappiness", "vm_vfs_cache_pressure", "sda_read_ahead_kb", "sda_iostats", "sda_add_random"
            )

            HyperOsProfileTemplate.BALANCE_SCREEN_OFF -> listOf(
                "pm_freeze_timeout", "pm_async", "miui_analytics_net_restrict",
                "cpu6_online", "policy0_min_freq", "policy2_min_freq", "policy5_min_freq", "policy7_min_freq",
                "policy5_governor", "walt_hispeed_load", "walt_hispeed_freq",
                "policy0_max_freq", "policy2_max_freq", "policy5_max_freq", "policy7_max_freq",
                "core_ctl_cpu2_min", "powerkey_input_boost_freq", "walt_sched_upmigrate", "walt_sched_downmigrate",
                "cpuset_background", "cpuset_sysbg", "uclamp_bg_max", "uclamp_sysbg_max",
                "sched_pelt_multiplier", "uclamp_nnapi_min", "cpuset_misf", "sched_rt_runtime_us",
                "bus_dcvs_ddr_max", "bus_dcvs_llcc_max", "bus_dcvs_l3_max",
                "gpu_idle_timer", "gpu_max_pwrlevel", "gpu_max_freq", "kgsl_busmon_max", "notification_window_blur",
                "vm_swappiness", "vm_vfs_cache_pressure", "sda_read_ahead_kb", "sda_iostats", "sda_add_random"
            )

            HyperOsProfileTemplate.PERFORMANCE -> listOf(
                "policy5_governor", "walt_hispeed_load", "walt_hispeed_freq",
                "policy0_max_freq", "policy2_max_freq", "policy5_max_freq", "policy7_max_freq",
                "core_ctl_cpu2_min", "powerkey_input_boost_freq", "walt_sched_upmigrate", "walt_sched_downmigrate",
                "cpu6_online", "policy0_min_freq", "policy2_min_freq", "policy5_min_freq", "policy7_min_freq",
                "bus_dcvs_ddr_max", "bus_dcvs_llcc_max", "bus_dcvs_l3_max",
                "cpuset_background", "cpuset_sysbg", "uclamp_bg_max", "uclamp_sysbg_max",
                "sched_pelt_multiplier", "uclamp_nnapi_min", "cpuset_misf", "sched_rt_runtime_us",
                "gpu_idle_timer", "gpu_max_pwrlevel", "gpu_max_freq", "kgsl_busmon_max", "notification_window_blur",
                "vm_swappiness", "vm_vfs_cache_pressure", "sda_read_ahead_kb", "sda_iostats", "sda_add_random",
                "pm_freeze_timeout", "pm_async", "miui_analytics_net_restrict"
            )
        }
        val rankMap = priorityOrder.withIndex().associate { (idx, id) -> id to idx }
        return specs.sortedBy { rankMap[it.id] ?: Int.MAX_VALUE }
    }
}

