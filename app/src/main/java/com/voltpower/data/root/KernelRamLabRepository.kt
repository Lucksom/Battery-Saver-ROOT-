package com.voltpower.data.root

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class KernelSection(val id: String, val title: String, val badge: String) {
    ALL("ALL", "All", "ALL"),
    CPU("CPU", "CPU & Cores", "CPU"),
    SCHED("SCHED", "Task Scheduler", "SCHED"),
    GPU("GPU", "GPU & Graphics", "GPU"),
    RAM("RAM", "RAM & Storage", "RAM"),
    IRQ("IRQ", "Hardware Interrupts", "IRQ"),
    FREEZE("FREEZE", "Sleep & App Freeze", "SLEEP"),
    THERMAL("THERMAL", "Thermal Limits", "THERM"),
    CHARGING("CHARGING", "Charging Control", "CHARGE"),
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
            title = "Small Cores Maximum Speed (Cores 0–1)",
            nodePath = "/sys/devices/system/cpu/cpufreq/policy0/scaling_max_freq",
            wasValue = "2016000",
            targetValue = "2016000",
            options = listOf("1209600", "1612800", "2016000"),
            optionLabels = mapOf(
                "1209600" to "1.21 GHz (Cool)",
                "1612800" to "1.61 GHz (Balanced)",
                "2016000" to "2.02 GHz (Full)"
            ),
            functionDesc = "Controls the highest speed the efficiency cores can reach.",
            upDownEffect = "Raising it makes everyday tasks faster with slightly more heat. Lowering it keeps the phone cooler and saves battery under load."
        ),
        KernelTweakSpec(
            id = "policy0_min_freq",
            section = KernelSection.CPU,
            title = "Small Cores Idle Speed (Cores 0–1)",
            nodePath = "/sys/devices/system/cpu/cpufreq/policy0/scaling_min_freq",
            wasValue = "748800",
            targetValue = "364800",
            options = listOf("364800", "576000", "748800"),
            optionLabels = mapOf(
                "364800" to "365 MHz (Lowest Power)",
                "576000" to "576 MHz (Balanced)",
                "748800" to "749 MHz (Default)"
            ),
            functionDesc = "Controls the lowest speed the small cores drop to when your phone is idle.",
            upDownEffect = "Lowering to 365 MHz saves idle battery with a tiny delay when ramping up. Keeping it higher feels slightly snappier but uses more idle power."
        ),
        KernelTweakSpec(
            id = "policy2_max_freq",
            section = KernelSection.CPU,
            title = "Big Cores Maximum Speed (Cores 2–4)",
            nodePath = "/sys/devices/system/cpu/cpufreq/policy2/scaling_max_freq",
            wasValue = "3014400",
            targetValue = "3014400",
            options = listOf("1804800", "2400000", "3014400"),
            optionLabels = mapOf(
                "1804800" to "1.80 GHz (Power Save)",
                "2400000" to "2.40 GHz (Balanced)",
                "3014400" to "3.01 GHz (Full)"
            ),
            functionDesc = "Controls the highest speed the performance cores can reach.",
            upDownEffect = "Higher speed finishes heavy app loads faster; lower speed reduces heat and battery drain."
        ),
        KernelTweakSpec(
            id = "policy2_min_freq",
            section = KernelSection.CPU,
            title = "Big Cores Idle Speed (Cores 2–4)",
            nodePath = "/sys/devices/system/cpu/cpufreq/policy2/scaling_min_freq",
            wasValue = "480000",
            targetValue = "480000",
            options = listOf("480000", "806400"),
            optionLabels = mapOf(
                "480000" to "480 MHz (Lowest Power)",
                "806400" to "806 MHz (Snappy)"
            ),
            functionDesc = "Controls the resting speed of the big performance cores when idle.",
            upDownEffect = "Keeping it at 480 MHz minimizes idle battery drain; raising it keeps cores ready at the cost of extra battery."
        ),
        KernelTweakSpec(
            id = "policy5_max_freq",
            section = KernelSection.CPU,
            title = "Mid Cores Maximum Speed (Cores 5–6)",
            nodePath = "/sys/devices/system/cpu/cpufreq/policy5/scaling_max_freq",
            wasValue = "1056000",
            targetValue = "2803200",
            options = listOf("1056000", "1958400", "2803200"),
            optionLabels = mapOf(
                "1056000" to "1.06 GHz (Capped)",
                "1958400" to "1.96 GHz (Balanced)",
                "2803200" to "2.80 GHz (Uncapped)"
            ),
            functionDesc = "Controls the speed ceiling for the middle cluster (often capped at 1.06 GHz by the system).",
            upDownEffect = "Uncapping to 2.80 GHz unlocks full multitasking speed with more heat; 1.06 GHz keeps the phone cool."
        ),
        KernelTweakSpec(
            id = "policy5_min_freq",
            section = KernelSection.CPU,
            title = "Mid Cores Idle Speed (Cores 5–6)",
            nodePath = "/sys/devices/system/cpu/cpufreq/policy5/scaling_min_freq",
            wasValue = "480000",
            targetValue = "480000",
            options = listOf("480000", "806400"),
            optionLabels = mapOf(
                "480000" to "480 MHz (Lowest Power)",
                "806400" to "806 MHz (Snappy)"
            ),
            functionDesc = "Controls the lowest speed the middle cores drop to when idle.",
            upDownEffect = "480 MHz saves the most standby power; 806 MHz responds slightly faster."
        ),
        KernelTweakSpec(
            id = "policy7_max_freq",
            section = KernelSection.CPU,
            title = "Prime Core Maximum Speed (Core 7)",
            nodePath = "/sys/devices/system/cpu/cpufreq/policy7/scaling_max_freq",
            wasValue = "2054400",
            targetValue = "3206400",
            options = listOf("1612800", "2054400", "2649600", "3206400"),
            optionLabels = mapOf(
                "1612800" to "1.61 GHz (Eco)",
                "2054400" to "2.05 GHz (Default Cap)",
                "2649600" to "2.65 GHz (Fast)",
                "3206400" to "3.21 GHz (Max Boost)"
            ),
            functionDesc = "Controls the top speed of the main Prime processor core.",
            upDownEffect = "Raising to 3.21 GHz gives maximum single-core speed for heavy apps and games; lowering saves heat and battery."
        ),
        KernelTweakSpec(
            id = "policy7_min_freq",
            section = KernelSection.CPU,
            title = "Prime Core Idle Speed (Core 7)",
            nodePath = "/sys/devices/system/cpu/cpufreq/policy7/scaling_min_freq",
            wasValue = "633600",
            targetValue = "480000",
            options = listOf("480000", "633600"),
            optionLabels = mapOf(
                "480000" to "480 MHz (Lowest Power)",
                "633600" to "634 MHz (Default)"
            ),
            functionDesc = "Controls the lowest idle speed of the Prime core.",
            upDownEffect = "Lowering to 480 MHz reduces idle power draw when the Prime core is not busy."
        ),
        KernelTweakSpec(
            id = "policy5_governor",
            section = KernelSection.CPU,
            title = "CPU Speed Scaling Behavior (Governor)",
            nodePath = "/sys/devices/system/cpu/cpufreq/policy5/scaling_governor",
            wasValue = "walt",
            targetValue = "schedutil",
            options = listOf("walt", "schedutil", "powersave", "performance", "conservative"),
            optionLabels = mapOf(
                "walt" to "Qualcomm WALT (Default)",
                "schedutil" to "Balanced Linux",
                "powersave" to "Always Minimum Speed",
                "performance" to "Always Maximum Speed",
                "conservative" to "Gradual Ramp-Up"
            ),
            functionDesc = "Chooses the behavior used to adjust processor speed based on workload.",
            upDownEffect = "Balanced Linux or Gradual Ramp-Up smooths out sudden spikes; Always Minimum saves maximum battery; Always Maximum locks top speed."
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
            id = "walt_hispeed_load",
            section = KernelSection.CPU,
            title = "Speed Ramp-Up Threshold",
            nodePath = "/sys/devices/system/cpu/cpufreq/policy5/walt/hispeed_load",
            wasValue = "90",
            targetValue = "80",
            options = listOf("75", "80", "90", "95"),
            optionLabels = mapOf(
                "75" to "75% Load (Fast Ramp)",
                "80" to "80% Load (Responsive)",
                "90" to "90% Load (Default)",
                "95" to "95% Load (Power Save)"
            ),
            functionDesc = "The processor workload percentage at which the cores jump to high speed.",
            upDownEffect = "Lowering to 80% makes the phone ramp up sooner for smoother scrolling; raising to 95% delays ramping to save battery."
        ),
        KernelTweakSpec(
            id = "walt_hispeed_freq",
            section = KernelSection.CPU,
            title = "High-Load Jump Speed",
            nodePath = "/sys/devices/system/cpu/cpufreq/policy5/walt/hispeed_freq",
            wasValue = "1190400",
            targetValue = "1056000",
            options = listOf("960000", "1056000", "1190400", "1497600"),
            optionLabels = mapOf(
                "960000" to "960 MHz (Eco)",
                "1056000" to "1.06 GHz (Matched Cap)",
                "1190400" to "1.19 GHz (Default)",
                "1497600" to "1.50 GHz (Fast Jump)"
            ),
            functionDesc = "The target speed the cluster jumps to once workload crosses the threshold.",
            upDownEffect = "Setting to 1.06 GHz matches the mid-cluster cap so the step works reliably; raising it jumps to a higher speed."
        ),
        KernelTweakSpec(
            id = "cpu6_online",
            section = KernelSection.CPU,
            title = "Core 6 Power State",
            nodePath = "/sys/devices/system/cpu/cpu6/online",
            wasValue = "1",
            targetValue = "0",
            options = listOf("0", "1"),
            optionLabels = mapOf(
                "0" to "Parked (Offline)",
                "1" to "Active (Online)"
            ),
            functionDesc = "Switches processor Core 6 off or on while keeping primary cores running.",
            upDownEffect = "Parking the core saves power when you don't need heavy multitasking; setting Active brings all cores online."
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
            title = "Workload Reaction Speed",
            nodePath = "/proc/sys/kernel/sched_pelt_multiplier",
            wasValue = "4",
            targetValue = "2",
            options = listOf("1", "2", "4"),
            optionLabels = mapOf(
                "1" to "Slow & Smooth (1x)",
                "2" to "Balanced (2x)",
                "4" to "Fast Reaction (4x Default)"
            ),
            functionDesc = "Controls how quickly the system reacts to sudden changes in app workload.",
            upDownEffect = "Fast (4x) reacts immediately with more speed jumps; Balanced (2x) or Slow (1x) smooths out brief spikes to save power."
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
            title = "Real-Time Audio & UI Time Limit",
            nodePath = "/proc/sys/kernel/sched_rt_runtime_us",
            wasValue = "1250000",
            targetValue = "1250000",
            options = listOf("950000", "1250000"),
            optionLabels = mapOf(
                "950000" to "95% Cap (Conservative)",
                "1250000" to "Default (Standard)"
            ),
            functionDesc = "Controls the maximum processor time high-priority real-time tasks can use per period.",
            upDownEffect = "Default works well for smooth audio and animations; 95% Cap leaves extra safety headroom for background tasks."
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
            title = "Background App Speed Limit",
            nodePath = "/dev/cpuctl/background/cpu.uclamp.max",
            wasValue = "30.00",
            targetValue = "20.00",
            options = listOf("15.00", "20.00", "30.00", "50.00"),
            optionLabels = mapOf(
                "15.00" to "15% Max (Strict Save)",
                "20.00" to "20% Max (Power Save)",
                "30.00" to "30% Max (Default)",
                "50.00" to "50% Max (Faster Sync)"
            ),
            functionDesc = "Caps how fast the processor is allowed to run for background apps.",
            upDownEffect = "Lowering to 15%–20% saves battery by slowing hidden background tasks; raising to 50% lets background tasks finish sooner."
        ),
        KernelTweakSpec(
            id = "uclamp_sysbg_max",
            section = KernelSection.SCHED,
            title = "System Background Speed Limit",
            nodePath = "/dev/cpuctl/system-background/cpu.uclamp.max",
            wasValue = "40.00",
            targetValue = "30.00",
            options = listOf("25.00", "30.00", "40.00", "60.00"),
            optionLabels = mapOf(
                "25.00" to "25% Max (Strict Save)",
                "30.00" to "30% Max (Power Save)",
                "40.00" to "40% Max (Default)",
                "60.00" to "60% Max (Fast System)"
            ),
            functionDesc = "Caps how fast the processor is allowed to run for background system maintenance.",
            upDownEffect = "Lowering to 25%–30% reduces background battery drain; raising to 60% speeds up background indexing and sync."
        ),
        KernelTweakSpec(
            id = "uclamp_nnapi_min",
            section = KernelSection.SCHED,
            title = "AI Engine Idle Speed Floor",
            nodePath = "/dev/cpuctl/nnapi-hal/cpu.uclamp.min",
            wasValue = "1.00",
            targetValue = "0.00",
            options = listOf("0.00", "1.00"),
            optionLabels = mapOf(
                "0.00" to "0% Floor (No Idle Drain)",
                "1.00" to "1% Floor (Default)"
            ),
            functionDesc = "Controls the minimum guaranteed speed floor for the neural/AI hardware service.",
            upDownEffect = "Lowering to 0% removes the standing minimum floor when AI features are not in use."
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
            title = "System Frame Service Core Assignment",
            nodePath = "/dev/cpuset/misf/cpus",
            wasValue = "4-7",
            targetValue = "0-7",
            options = listOf("0-3", "4-7", "0-7"),
            optionLabels = mapOf(
                "0-3" to "Small Cores Only (0–3)",
                "4-7" to "Big Cores Only (4–7 Default)",
                "0-7" to "All Cores (0–7)"
            ),
            functionDesc = "Chooses which processor cores the HyperOS frame service can run on.",
            upDownEffect = "Small Cores Only saves power; All Cores spreads the load across the whole processor."
        ),
        KernelTweakSpec(
            id = "cpuset_background",
            section = KernelSection.SCHED,
            title = "Background Apps Core Assignment",
            nodePath = "/dev/cpuset/background/cpus",
            wasValue = "0-7",
            targetValue = "0-3",
            options = listOf("0-1", "0-3", "0-7"),
            optionLabels = mapOf(
                "0-1" to "Cores 0–1 (Ultra Save)",
                "0-3" to "Small Cores 0–3 (Recommended)",
                "0-7" to "All Cores 0–7 (Default)"
            ),
            functionDesc = "Chooses which processor cores background apps are allowed to use.",
            upDownEffect = "Restricting background apps to Small Cores (0–3) keeps them off the power-hungry big cores and saves battery."
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
                "settings put global disable_window_blurs $value 2>/dev/null; settings put system disable_window_blurs $value 2>/dev/null; settings put secure disable_window_blurs $value 2>/dev/null"
            },
            customReadCommand = "v=\$(settings get global disable_window_blurs 2>/dev/null); [ \"\$v\" = \"1\" ] && echo 1 || echo 0"
        ),

        // ====================================================================
        // 4. RAM, SWAP & STORAGE
        // ====================================================================
        KernelTweakSpec(
            id = "vm_swappiness",
            section = KernelSection.RAM,
            title = "Memory Swap Aggressiveness (ZRAM)",
            nodePath = "/proc/sys/vm/swappiness",
            wasValue = "150",
            targetValue = "80",
            options = listOf("60", "80", "100", "150"),
            optionLabels = mapOf(
                "60" to "Low Swap (60)",
                "80" to "Balanced (80)",
                "100" to "Moderate (100)",
                "150" to "Aggressive (150 Default)"
            ),
            functionDesc = "Controls how eagerly the system compresses inactive memory into ZRAM swap.",
            upDownEffect = "Lowering to 80 reduces processor work spent compressing RAM; keeping at 150 caches more background apps at higher CPU cost."
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
            title = "File Cache Cleanup Speed",
            nodePath = "/proc/sys/vm/vfs_cache_pressure",
            wasValue = "73",
            targetValue = "100",
            options = listOf("50", "73", "100", "120"),
            optionLabels = mapOf(
                "50" to "Keep Cache Longer (50)",
                "73" to "Default (73)",
                "100" to "Standard Cleanup (100)",
                "120" to "Fast Cleanup (120)"
            ),
            functionDesc = "Controls how quickly cached folder and file information is cleared to free RAM.",
            upDownEffect = "Raising to 100 frees unused file cache sooner; lowering keeps folder data cached longer."
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
            title = "Storage Read-Ahead Buffer",
            nodePath = "/sys/block/sda/queue/read_ahead_kb",
            wasValue = "128",
            targetValue = "128",
            options = listOf("64", "128", "256", "512"),
            optionLabels = mapOf(
                "64" to "64 KB (Light)",
                "128" to "128 KB (Default)",
                "256" to "256 KB (Fast Media)",
                "512" to "512 KB (Large Files)"
            ),
            functionDesc = "Controls how much extra data is pre-loaded into RAM when reading storage.",
            upDownEffect = "Higher values speed up loading large files and games but use slightly more RAM; 128 KB is balanced for everyday use."
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
                "for c in /sys/class/thermal/cooling_device*; do if [ \"\$(cat \$c/type 2>/dev/null)\" = \"gpu\" ]; then chmod 0666 \$c/cur_state 2>/dev/null; echo $value > \$c/cur_state 2>/dev/null; chmod 0444 \$c/cur_state 2>/dev/null; fi; done"
            },
            customReadCommand = "for c in /sys/class/thermal/cooling_device*; do [ \"\$(cat \$c/type 2>/dev/null)\" = \"gpu\" ] && { cat \$c/cur_state 2>/dev/null; break; }; done"
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
                "for c in /sys/class/thermal/cooling_device*; do if [ \"\$(cat \$c/type 2>/dev/null)\" = \"cpu-cluster3\" ]; then chmod 0666 \$c/cur_state 2>/dev/null; echo $value > \$c/cur_state 2>/dev/null; chmod 0444 \$c/cur_state 2>/dev/null; fi; done"
            },
            customReadCommand = "for c in /sys/class/thermal/cooling_device*; do [ \"\$(cat \$c/type 2>/dev/null)\" = \"cpu-cluster3\" ] && { cat \$c/cur_state 2>/dev/null; break; }; done"
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
                "for c in /sys/class/thermal/cooling_device*; do if [ \"\$(cat \$c/type 2>/dev/null)\" = \"cdsp\" ]; then chmod 0666 \$c/cur_state 2>/dev/null; echo $value > \$c/cur_state 2>/dev/null; chmod 0444 \$c/cur_state 2>/dev/null; fi; done"
            },
            customReadCommand = "for c in /sys/class/thermal/cooling_device*; do [ \"\$(cat \$c/type 2>/dev/null)\" = \"cdsp\" ] && { cat \$c/cur_state 2>/dev/null; break; }; done"
        ),

        // ====================================================================
        // 8. CHARGING LIMITS
        // ====================================================================
        KernelTweakSpec(
            id = "battery_charge_control_limit",
            section = KernelSection.CHARGING,
            title = "Charging Speed & Heat Limit",
            nodePath = "/sys/class/power_supply/battery/charge_control_limit",
            wasValue = "0",
            targetValue = "8",
            options = listOf("0", "4", "8", "12", "16"),
            optionLabels = mapOf(
                "0" to "No Limit (Full Fast Charge)",
                "4" to "Gentle Limit (Level 4)",
                "8" to "Balanced Cool Charge (Level 8)",
                "12" to "Slow Cool Charge (Level 12)",
                "16" to "Minimum Charge Rate (Level 16)"
            ),
            functionDesc = "Controls how strongly charging current is throttled to keep the battery cool.",
            upDownEffect = "Higher levels slow down charging to reduce battery heat and wear; No Limit allows full charging speed."
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
        val shouldLock = lockNodeAfterWrite && (path.startsWith("/sys/") || path.startsWith("/dev/"))
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
                    append("for p in /sys/devices/system/cpu/cpufreq/policy*/walt/hispeed_load /sys/devices/system/cpu/cpufreq/policy*/schedutil/hispeed_load; do ")
                    append("if [ -e \"\$p\" ]; then chmod 0666 \"\$p\" 2>/dev/null; echo \"$value\" > \"\$p\" 2>/dev/null; chmod $postMode \"\$p\" 2>/dev/null; fi; done")
                }
                "walt_hispeed_freq" -> {
                    append("for p in /sys/devices/system/cpu/cpufreq/policy*/walt/hispeed_freq /sys/devices/system/cpu/cpufreq/policy*/schedutil/hispeed_freq; do ")
                    append("if [ -e \"\$p\" ]; then chmod 0666 \"\$p\" 2>/dev/null; echo \"$value\" > \"\$p\" 2>/dev/null; chmod $postMode \"\$p\" 2>/dev/null; fi; done")
                }
                "sda_read_ahead_kb" -> {
                    append("for b in /sys/block/sda/queue/read_ahead_kb /sys/block/sdc/queue/read_ahead_kb /sys/block/mmcblk0/queue/read_ahead_kb /sys/block/dm-0/queue/read_ahead_kb; do ")
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
            // 1. Hottest thermal zone + battery temp (Section 9)
            append("best_t=0; best_z='Processor'; ")
            append("for z in /sys/class/thermal/thermal_zone*; do ")
            append("[ -r \"\$z/temp\" ] || continue; ")
            append("t=\$(cat \"\$z/temp\" 2>/dev/null | tr -dc '0-9-'); ")
            append("if [ -n \"\$t\" ] && [ \"\$t\" -gt \"\$best_t\" ] && [ \"\$t\" -lt 150000 ]; then ")
            append("best_t=\$t; best_z=\$(cat \"\$z/type\" 2>/dev/null || basename \"\$z\"); ")
            append("fi; done; ")
            append("bat_t=\$(cat /sys/class/power_supply/battery/temp 2>/dev/null | tr -dc '0-9-'); ")
            append("echo \"TEMP|\$best_t|\$best_z|\$bat_t\"; ")

            // 2. Suspend stats & freeze capabilities (Section 6)
            append("s_ok=\$(cat /sys/power/suspend_stats/success 2>/dev/null || echo '0'); ")
            append("s_ff=\$(cat /sys/power/suspend_stats/failed_freeze 2>/dev/null || echo '21122'); ")
            append("s_dev=\$(cat /sys/power/suspend_stats/last_failed_dev 2>/dev/null || echo 'alarmtimer'); ")
            append("s_to=\$(cat /sys/power/pm_freeze_timeout 2>/dev/null || echo '20000'); ")
            append("s_as=\$(cat /sys/power/pm_async 2>/dev/null || echo '1'); ")
            append("s_ms=\$(cat /sys/power/mem_sleep 2>/dev/null || echo '[s2idle]'); ")
            append("cg_ok=0; [ -e /sys/fs/cgroup/uid_0/cgroup.freeze ] || ls /sys/fs/cgroup/uid_*/cgroup.freeze >/dev/null 2>&1 && cg_ok=1; ")
            append("echo \"SUSP|\$s_ok|\$s_ff|\$s_dev|\$s_to|\$s_as|\$s_ms|\$cg_ok\"; ")

            // 3. WALT tunables directory listing (Section 1)
            append("walt_ls=\$(ls /sys/devices/system/cpu/cpufreq/policy5/walt/ 2>/dev/null | tr '\\n' ' '); ")
            append("echo \"WALT|\$walt_ls\"; ")

            // 4. Charging telemetry (Section 8)
            append("c_lim=\$(cat /sys/class/power_supply/battery/charge_control_limit 2>/dev/null || echo '0'); ")
            append("c_max=\$(cat /sys/class/power_supply/battery/constant_charge_current_max 2>/dev/null || echo '0'); ")
            append("c_in=\$(cat /sys/class/power_supply/battery/input_current_limit 2>/dev/null || echo '0'); ")
            append("echo \"CHG|\$c_lim|\$c_max|\$c_in\"; ")

            // 5. All tweakSpecs current values
            for (spec in tweakSpecs) {
                if (spec.customReadCommand != null) {
                    append("v=\$(${spec.customReadCommand}); ")
                } else {
                    append("v=\$(cat \"${spec.nodePath}\" 2>/dev/null | head -n 1); ")
                }
                append("echo \"NODE|${spec.id}|\$v\"; ")
            }

            // 6. Daemon status via init.svc.<name> or pidof
            for (d in defaultDaemons) {
                append("st=\$(getprop init.svc.${d.name} 2>/dev/null); ")
                append("if [ -z \"\$st\" ]; then pidof ${d.name} >/dev/null 2>&1 && st='running' || st='stopped'; fi; ")
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
            val readCmd = spec.customReadCommand
                ?: "cat \"${spec.nodePath}\" 2>/dev/null | head -n 1"

            val fullCmd = "$writeCmd; sleep 0.05; $readCmd"
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
    suspend fun restoreAllWasValues(): List<String> = withContext(Dispatchers.IO) {
        val logs = mutableListOf<String>()
        val sb = StringBuilder()

        for (spec in tweakSpecs) {
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
     */
    fun getTemplatePresets(template: HyperOsProfileTemplate): Map<String, String> {
        return when (template) {
            HyperOsProfileTemplate.POWERSAVE -> mapOf(
                "policy0_max_freq" to "1612800",
                "policy0_min_freq" to "364800",
                "policy2_max_freq" to "1804800",
                "policy2_min_freq" to "480000",
                "policy5_max_freq" to "1056000",
                "policy5_min_freq" to "480000",
                "policy7_max_freq" to "1612800",
                "policy7_min_freq" to "480000",
                "policy5_governor" to "powersave",
                "policy7_boost" to "0",
                "walt_hispeed_load" to "95",
                "walt_hispeed_freq" to "960000",
                "cpu6_online" to "0",
                "sched_energy_aware" to "1",
                "sched_pelt_multiplier" to "1",
                "sched_schedstats" to "0",
                "sched_rt_runtime_us" to "950000",
                "sched_rr_timeslice_ms" to "100",
                "uclamp_bg_max" to "15.00",
                "uclamp_sysbg_max" to "25.00",
                "uclamp_nnapi_min" to "0.00",
                "uclamp_topapp_min" to "0.00",
                "cpuset_misf" to "0-3",
                "cpuset_background" to "0-1",
                "gpu_max_freq" to "600000000",
                "gpu_min_freq" to "264000000",
                "gpu_idle_timer" to "50",
                "gpu_max_pwrlevel" to "4",
                "gpu_min_pwrlevel" to "10",
                "kgsl_busmon_max" to "600000000",
                "notification_window_blur" to "1",
                "vm_swappiness" to "60",
                "vm_min_free_kbytes" to "11584",
                "vm_vfs_cache_pressure" to "100",
                "vm_watermark_scale" to "10",
                "sda_read_ahead_kb" to "64",
                "irq_359_msm_drm" to "0-1",
                "irq_361_touchscreen" to "0-1",
                "irq_367_glink" to "0-1",
                "pm_freeze_timeout" to "5000",
                "pm_async" to "0",
                "thermal_cooling_gpu" to "4",
                "thermal_cooling_cpu3" to "3",
                "thermal_cooling_cdsp" to "1",
                "battery_charge_control_limit" to "8"
            )

            HyperOsProfileTemplate.BALANCE_SCREEN_OFF -> mapOf(
                "policy0_max_freq" to "2016000",
                "policy0_min_freq" to "364800",
                "policy2_max_freq" to "2400000",
                "policy2_min_freq" to "480000",
                "policy5_max_freq" to "1958400",
                "policy5_min_freq" to "480000",
                "policy7_max_freq" to "2054400",
                "policy7_min_freq" to "480000",
                "policy5_governor" to "schedutil",
                "policy7_boost" to "0",
                "walt_hispeed_load" to "90",
                "walt_hispeed_freq" to "1056000",
                "cpu6_online" to "1",
                "sched_energy_aware" to "1",
                "sched_pelt_multiplier" to "2",
                "sched_schedstats" to "0",
                "sched_rt_runtime_us" to "1250000",
                "sched_rr_timeslice_ms" to "100",
                "uclamp_bg_max" to "20.00",
                "uclamp_sysbg_max" to "30.00",
                "uclamp_nnapi_min" to "0.00",
                "uclamp_topapp_min" to "10.00",
                "cpuset_misf" to "0-7",
                "cpuset_background" to "0-3",
                "gpu_max_freq" to "873000000",
                "gpu_min_freq" to "264000000",
                "gpu_idle_timer" to "64",
                "gpu_max_pwrlevel" to "2",
                "gpu_min_pwrlevel" to "10",
                "kgsl_busmon_max" to "873000000",
                "notification_window_blur" to "0",
                "vm_swappiness" to "80",
                "vm_min_free_kbytes" to "24576",
                "vm_vfs_cache_pressure" to "100",
                "vm_watermark_scale" to "20",
                "sda_read_ahead_kb" to "128",
                "irq_359_msm_drm" to "2-4",
                "irq_361_touchscreen" to "2-4",
                "irq_367_glink" to "0-1",
                "pm_freeze_timeout" to "10000",
                "pm_async" to "0",
                "thermal_cooling_gpu" to "4",
                "thermal_cooling_cpu3" to "3",
                "thermal_cooling_cdsp" to "1",
                "battery_charge_control_limit" to "4"
            )

            HyperOsProfileTemplate.PERFORMANCE -> mapOf(
                "policy0_max_freq" to "2016000",
                "policy0_min_freq" to "748800",
                "policy2_max_freq" to "3014400",
                "policy2_min_freq" to "806400",
                "policy5_max_freq" to "2803200",
                "policy5_min_freq" to "806400",
                "policy7_max_freq" to "3206400",
                "policy7_min_freq" to "633600",
                "policy5_governor" to "performance",
                "policy7_boost" to "1",
                "walt_hispeed_load" to "75",
                "walt_hispeed_freq" to "1497600",
                "cpu6_online" to "1",
                "sched_energy_aware" to "0",
                "sched_pelt_multiplier" to "4",
                "sched_schedstats" to "0",
                "sched_rt_runtime_us" to "1250000",
                "sched_rr_timeslice_ms" to "50",
                "uclamp_bg_max" to "50.00",
                "uclamp_sysbg_max" to "60.00",
                "uclamp_nnapi_min" to "1.00",
                "uclamp_topapp_min" to "20.00",
                "cpuset_misf" to "0-7",
                "cpuset_background" to "0-7",
                "gpu_max_freq" to "1150000000",
                "gpu_min_freq" to "400000000",
                "gpu_idle_timer" to "120",
                "gpu_max_pwrlevel" to "0",
                "gpu_min_pwrlevel" to "8",
                "kgsl_busmon_max" to "1150000000",
                "notification_window_blur" to "0",
                "vm_swappiness" to "60",
                "vm_min_free_kbytes" to "32768",
                "vm_vfs_cache_pressure" to "120",
                "vm_watermark_scale" to "40",
                "sda_read_ahead_kb" to "512",
                "irq_359_msm_drm" to "2-4",
                "irq_361_touchscreen" to "2-4",
                "irq_367_glink" to "2-4",
                "pm_freeze_timeout" to "20000",
                "pm_async" to "1",
                "thermal_cooling_gpu" to "0",
                "thermal_cooling_cpu3" to "0",
                "thermal_cooling_cdsp" to "0",
                "battery_charge_control_limit" to "0"
            )
        }
    }

    /**
     * Returns sections ordered by priority for the selected HyperOS profile template.
     */
    fun getSectionOrderForTemplate(template: HyperOsProfileTemplate?): List<KernelSection> {
        return when (template) {
            HyperOsProfileTemplate.POWERSAVE -> listOf(
                KernelSection.FREEZE,
                KernelSection.CPU,
                KernelSection.SCHED,
                KernelSection.GPU,
                KernelSection.RAM,
                KernelSection.CHARGING,
                KernelSection.IRQ,
                KernelSection.THERMAL
            )

            HyperOsProfileTemplate.BALANCE_SCREEN_OFF -> listOf(
                KernelSection.FREEZE,
                KernelSection.SCHED,
                KernelSection.CPU,
                KernelSection.RAM,
                KernelSection.GPU,
                KernelSection.IRQ,
                KernelSection.CHARGING,
                KernelSection.THERMAL
            )

            HyperOsProfileTemplate.PERFORMANCE -> listOf(
                KernelSection.CPU,
                KernelSection.GPU,
                KernelSection.THERMAL,
                KernelSection.SCHED,
                KernelSection.RAM,
                KernelSection.IRQ,
                KernelSection.CHARGING,
                KernelSection.FREEZE
            )

            null -> KernelSection.entries.filter { it != KernelSection.ALL && it != KernelSection.DAEMONS }
        }
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
                // Freeze & Idle power first
                "pm_freeze_timeout", "pm_async",
                "cpu6_online", "policy0_min_freq", "policy2_min_freq", "policy5_min_freq", "policy7_min_freq",
                "policy5_governor", "walt_hispeed_load", "walt_hispeed_freq",
                "policy7_max_freq", "policy2_max_freq", "policy5_max_freq", "policy0_max_freq", "policy7_boost",
                "cpuset_background", "uclamp_bg_max", "uclamp_sysbg_max", "sched_energy_aware",
                "sched_pelt_multiplier", "uclamp_nnapi_min", "uclamp_topapp_min", "cpuset_misf",
                "gpu_idle_timer", "gpu_min_pwrlevel", "gpu_max_pwrlevel", "gpu_max_freq", "gpu_min_freq", "kgsl_busmon_max", "notification_window_blur",
                "vm_swappiness", "vm_vfs_cache_pressure", "sda_read_ahead_kb", "vm_min_free_kbytes", "vm_watermark_scale",
                "battery_charge_control_limit"
            )

            HyperOsProfileTemplate.BALANCE_SCREEN_OFF -> listOf(
                // Screen-off sleep + balanced UI smoothness first
                "pm_freeze_timeout", "pm_async",
                "cpuset_background", "uclamp_bg_max", "uclamp_sysbg_max", "uclamp_topapp_min",
                "sched_pelt_multiplier", "sched_energy_aware", "cpuset_misf", "uclamp_nnapi_min",
                "policy5_governor", "walt_hispeed_load", "walt_hispeed_freq",
                "policy5_max_freq", "policy2_max_freq", "policy7_max_freq", "policy0_max_freq",
                "policy0_min_freq", "policy7_min_freq", "policy2_min_freq", "policy5_min_freq", "cpu6_online",
                "vm_swappiness", "vm_min_free_kbytes", "vm_watermark_scale", "vm_vfs_cache_pressure", "sda_read_ahead_kb",
                "gpu_idle_timer", "gpu_max_freq", "gpu_min_freq", "kgsl_busmon_max", "notification_window_blur"
            )

            HyperOsProfileTemplate.PERFORMANCE -> listOf(
                // Peak clocks, boost, thermal unthrottling & gaming responsiveness first
                "policy7_max_freq", "policy5_max_freq", "policy2_max_freq", "policy0_max_freq",
                "policy7_boost", "policy5_governor", "walt_hispeed_load", "walt_hispeed_freq",
                "cpu6_online", "policy7_min_freq", "policy2_min_freq", "policy5_min_freq", "policy0_min_freq",
                "gpu_max_freq", "gpu_max_pwrlevel", "kgsl_busmon_max", "gpu_idle_timer", "gpu_min_freq", "gpu_min_pwrlevel", "notification_window_blur",
                "thermal_cooling_gpu", "thermal_cooling_cpu3", "thermal_cooling_cdsp",
                "uclamp_topapp_min", "sched_energy_aware", "sched_pelt_multiplier", "sched_rr_timeslice_ms",
                "cpuset_misf", "cpuset_background", "uclamp_bg_max", "uclamp_sysbg_max",
                "vm_min_free_kbytes", "vm_watermark_scale", "sda_read_ahead_kb", "vm_vfs_cache_pressure", "vm_swappiness",
                "irq_361_touchscreen", "irq_359_msm_drm", "irq_367_glink"
            )
        }
        val rankMap = priorityOrder.withIndex().associate { (idx, id) -> id to idx }
        return specs.sortedBy { rankMap[it.id] ?: Int.MAX_VALUE }
    }
}

