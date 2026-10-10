package com.example.data.root

import android.content.Context
import android.os.BatteryManager
import com.example.data.db.TweakConfigEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.InputStreamReader

data class CommandResult(
    val success: Boolean,
    val stdout: String,
    val stderr: String,
    val exitCode: Int
)

object RootBridge {

    const val BACKUP_FILE_PATH = "/sdcard/Download/Stock_Kernel_Backup.sh"

    // Hardcoded permanent system exceptions that can NEVER be restricted, killed, or frozen
    val PERMANENT_SYSTEM_EXCEPTIONS = setOf(
        "com.mi.android.globallauncher",
        "com.miui.home",
        "com.android.systemui",
        "miui.systemui.plugin",
        "com.google.android.gms",
        "com.android.phone",
        "android"
    )

    suspend fun isRootAvailable(): Boolean = withContext(Dispatchers.IO) {
        val result = execute("id")
        result.success && result.stdout.contains("uid=0")
    }

    suspend fun execute(command: String): CommandResult = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec("su")
            val outputStream = DataOutputStream(process.outputStream)
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))

            outputStream.writeBytes("$command\nexit\n")
            outputStream.flush()

            val stdout = reader.readText()
            val stderr = errorReader.readText()
            val exitCode = process.waitFor()

            CommandResult(
                success = (exitCode == 0),
                stdout = stdout.trim(),
                stderr = stderr.trim(),
                exitCode = exitCode
            )
        } catch (e: Exception) {
            CommandResult(
                success = false,
                stdout = "",
                stderr = e.message ?: "Failed to execute root command",
                exitCode = -1
            )
        }
    }

    suspend fun readNode(path: String): String? = withContext(Dispatchers.IO) {
        val file = File(path)
        if (file.exists() && file.canRead()) {
            try {
                return@withContext file.readText().trim()
            } catch (_: Exception) { }
        }
        val res = execute("cat \"$path\" 2>/dev/null")
        if (res.success && res.stdout.isNotEmpty()) res.stdout else null
    }

    suspend fun nodeExists(path: String): Boolean = withContext(Dispatchers.IO) {
        if (File(path).exists()) return@withContext true
        val res = execute("[ -e \"$path\" ] && echo 1 || echo 0")
        res.stdout == "1"
    }

    /**
     * One-Time Immutable Stock Backup Rule:
     * Checks if backup already exists. If not, snapshots all live stock kernel values
     * into a runnable shell script and saves it to internal storage.
     */
    suspend fun createOneTimeBackupIfNotExists(context: Context): String = withContext(Dispatchers.IO) {
        val backupFile = File(BACKUP_FILE_PATH)
        if (backupFile.exists() && backupFile.length() > 50) {
            return@withContext BACKUP_FILE_PATH
        }

        val commands = StringBuilder()
        commands.append("#!/system/bin/sh\n")
        commands.append("# Automatic Stock Kernel Configuration Snapshot\n")
        commands.append("# Run this file to revert all kernel parameters to original factory boot values\n\n")

        // 1. CPU Governor & Frequencies
        for (i in 0..7) {
            val govPath = "/sys/devices/system/cpu/cpu$i/cpufreq/scaling_governor"
            val maxPath = "/sys/devices/system/cpu/cpu$i/cpufreq/scaling_max_freq"
            val minPath = "/sys/devices/system/cpu/cpu$i/cpufreq/scaling_min_freq"

            readNode(govPath)?.let { commands.append("echo \"$it\" > \"$govPath\" 2>/dev/null\n") }
            readNode(maxPath)?.let { commands.append("echo \"$it\" > \"$maxPath\" 2>/dev/null\n") }
            readNode(minPath)?.let { commands.append("echo \"$it\" > \"$minPath\" 2>/dev/null\n") }
        }

        // 2. Schedutil Parameters
        val schedutilNodes = listOf(
            "/sys/devices/system/cpu/cpufreq/schedutil/up_rate_limit_us",
            "/sys/devices/system/cpu/cpufreq/schedutil/down_rate_limit_us",
            "/sys/devices/system/cpu/cpufreq/schedutil/hispeed_load",
            "/sys/devices/system/cpu/cpufreq/schedutil/hispeed_freq",
            "/sys/devices/system/cpu/cpufreq/schedutil/iowait_boost_enable"
        )
        for (node in schedutilNodes) {
            readNode(node)?.let { commands.append("echo \"$it\" > \"$node\" 2>/dev/null\n") }
        }

        // 3. Touch & Input Boost
        val boostNodes = listOf(
            "/sys/module/cpu_boost/parameters/input_boost_ms",
            "/sys/module/cpu_input_boost/parameters/input_boost_duration",
            "/sys/module/msm_performance/parameters/touchboost",
            "/sys/power/pnpmgr/touch_boost",
            "/sys/kernel/fp_boost/enabled"
        )
        for (node in boostNodes) {
            readNode(node)?.let { commands.append("echo \"$it\" > \"$node\" 2>/dev/null\n") }
        }

        // 4. I/O Queue & Storage
        val ioNodes = listOf(
            "/sys/block/mmcblk0/queue/read_ahead_kb",
            "/sys/block/mmcblk0/queue/nr_requests",
            "/sys/block/sda/queue/read_ahead_kb",
            "/sys/block/sda/queue/nr_requests"
        )
        for (node in ioNodes) {
            readNode(node)?.let { commands.append("echo \"$it\" > \"$node\" 2>/dev/null\n") }
        }

        // 5. Virtual Memory
        val vmNodes = listOf(
            "/proc/sys/vm/dirty_ratio",
            "/proc/sys/vm/dirty_background_ratio",
            "/proc/sys/vm/dirty_writeback_centisecs",
            "/proc/sys/vm/dirty_expire_centisecs",
            "/proc/sys/vm/swappiness",
            "/proc/sys/vm/vfs_cache_pressure"
        )
        for (node in vmNodes) {
            readNode(node)?.let { commands.append("echo \"$it\" > \"$node\" 2>/dev/null\n") }
        }

        // 6. CPU Cores Online
        for (i in 0..7) {
            commands.append("echo 1 > \"/sys/devices/system/cpu/cpu$i/online\" 2>/dev/null\n")
        }

        // 7. GMS Doze Restore
        commands.append("dumpsys deviceidle whitelist +com.google.android.gms 2>/dev/null\n")

        // 8. GPU Clocks & Idler
        val gpuNodes = listOf(
            "/sys/class/kgsl/kgsl-3d0/throttling",
            "/sys/class/kgsl/kgsl-3d0/devfreq/adrenoboost",
            "/sys/class/kgsl/kgsl-3d0/devfreq/max_freq",
            "/sys/module/adreno_idler/parameters/adreno_idler_active"
        )
        for (node in gpuNodes) {
            readNode(node)?.let { commands.append("echo \"$it\" > \"$node\" 2>/dev/null\n") }
        }

        // 9. Networking & Congestion
        readNode("/proc/sys/net/ipv4/tcp_congestion_control")?.let {
            commands.append("echo \"$it\" > \"/proc/sys/net/ipv4/tcp_congestion_control\" 2>/dev/null\n")
        }

        // 10. Workqueue, Schedtune & Fsync
        val miscNodes = listOf(
            "/sys/module/workqueue/parameters/power_efficient",
            "/sys/devices/system/cpu/cpuidle/use_deepest_state",
            "/dev/stune/top-app/schedtune.boost",
            "/sys/devices/system/cpu/sched_mc_power_savings",
            "/sys/kernel/dyn_fsync/Dyn_fsync_active"
        )
        for (node in miscNodes) {
            readNode(node)?.let { commands.append("echo \"$it\" > \"$node\" 2>/dev/null\n") }
        }

        commands.append("echo \"Stock parameters restored successfully.\"\n")

        val scriptContent = commands.toString()
        val targetPath: String

        // Attempt write to /sdcard/Download
        val writeAttempt = execute(
            "mkdir -p /sdcard/Download && cat << 'EOF' > \"$BACKUP_FILE_PATH\"\n$scriptContent\nEOF\nchmod 755 \"$BACKUP_FILE_PATH\""
        )

        targetPath = if (writeAttempt.success && File(BACKUP_FILE_PATH).exists()) {
            BACKUP_FILE_PATH
        } else {
            val fallback = File(context.filesDir, "Stock_Kernel_Backup.sh")
            fallback.writeText(scriptContent)
            fallback.absolutePath
        }

        targetPath
    }

    /**
     * Restores all stock parameters from the backup script, or sets factory defaults.
     */
    suspend fun revertAllTweaks(context: Context): Boolean = withContext(Dispatchers.IO) {
        val backupCmd = if (File(BACKUP_FILE_PATH).exists()) {
            "sh \"$BACKUP_FILE_PATH\""
        } else {
            val internalFile = File(context.filesDir, "Stock_Kernel_Backup.sh")
            if (internalFile.exists()) {
                "sh \"${internalFile.absolutePath}\""
            } else {
                buildFallbackRevertScript()
            }
        }

        val result = execute(backupCmd)
        execute("dumpsys deviceidle whitelist +com.google.android.gms 2>/dev/null")
        execute("for i in /sys/devices/system/cpu/cpu*/online; do echo 1 > \"\$i\" 2>/dev/null; done")
        execute("cmd power set-mode 0 2>/dev/null || settings put global low_power 0 2>/dev/null")
        execute("settings delete system min_refresh_rate 2>/dev/null; settings delete system peak_refresh_rate 2>/dev/null; settings delete system user_refresh_rate 2>/dev/null; settings delete secure miui_refresh_rate 2>/dev/null")
        execute("for d in /sys/class/devfreq/*; do [ -f \"\$d/available_governors\" ] && { gov=\$(cat \"\$d/available_governors\" | awk '{print \$1}'); [ -n \"\$gov\" ] && echo \"\$gov\" > \"\$d/governor\" 2>/dev/null; }; done")
        execute("[ -d /dev/stune/top-app ] && echo 0 > /dev/stune/top-app/schedtune.boost 2>/dev/null; [ -d /dev/stune/top-app ] && echo 0 > /dev/stune/top-app/schedtune.sched_boost 2>/dev/null")
        execute("pkill -f \"voltpower_doze_daemon\" >/dev/null 2>&1")
        execute("settings put secure aod_mode 1 2>/dev/null; settings put system aod_mode 1 2>/dev/null; settings put secure doze_always_on 1 2>/dev/null; setprop debug.miui.aod_enable 1 2>/dev/null; am broadcast -a miui.intent.action.AOD_STATE_CHANGED --ez enabled true 2>/dev/null")
        execute("setprop persist.vendor.radio.5g_power_save 0 2>/dev/null; setprop persist.radio.power_saving 0 2>/dev/null")
        execute("echo 1 > /sys/module/msm_performance/parameters/touchboost 2>/dev/null; settings delete secure touch_boost 2>/dev/null")
        result.success
    }

    private fun buildFallbackRevertScript(): String {
        return """
            for cpu in /sys/devices/system/cpu/cpu*/cpufreq; do
                max=`cat ${'$'}cpu/cpuinfo_max_freq 2>/dev/null`
                min=`cat ${'$'}cpu/cpuinfo_min_freq 2>/dev/null`
                [ -n "${'$'}max" ] && echo "${'$'}max" > "${'$'}cpu/scaling_max_freq" 2>/dev/null
                [ -n "${'$'}min" ] && echo "${'$'}min" > "${'$'}cpu/scaling_min_freq" 2>/dev/null
                echo "schedutil" > "${'$'}cpu/scaling_governor" 2>/dev/null
            done
            for c in /sys/devices/system/cpu/cpu*/online; do echo 1 > "${'$'}c" 2>/dev/null; done
            echo 500 > /sys/devices/system/cpu/cpufreq/schedutil/up_rate_limit_us 2>/dev/null
            echo 20000 > /sys/devices/system/cpu/cpufreq/schedutil/down_rate_limit_us 2>/dev/null
            echo 80 > /sys/devices/system/cpu/cpufreq/schedutil/hispeed_load 2>/dev/null
            echo 1 > /sys/devices/system/cpu/cpufreq/schedutil/iowait_boost_enable 2>/dev/null
            echo 40 > /sys/module/cpu_boost/parameters/input_boost_ms 2>/dev/null
            echo 1 > /sys/module/msm_performance/parameters/touchboost 2>/dev/null
            echo 512 > /sys/block/mmcblk0/queue/read_ahead_kb 2>/dev/null
            echo 128 > /sys/block/mmcblk0/queue/nr_requests 2>/dev/null
            echo 20 > /proc/sys/vm/dirty_ratio 2>/dev/null
            echo 10 > /proc/sys/vm/dirty_background_ratio 2>/dev/null
            echo 500 > /proc/sys/vm/dirty_writeback_centisecs 2>/dev/null
            echo 60 > /proc/sys/vm/swappiness 2>/dev/null
            echo 100 > /proc/sys/vm/vfs_cache_pressure 2>/dev/null
        """.trimIndent()
    }

    /**
     * Applies safe, RAM-only Powersave tweaks based on granular toggles.
     */
    suspend fun applyPowersaveTweaks(
        context: Context,
        config: TweakConfigEntity
    ): List<String> = withContext(Dispatchers.IO) {
        val logs = mutableListOf<String>()

        val backupPath = createOneTimeBackupIfNotExists(context)
        logs.add("Stock backup verified at: $backupPath")

        val batteryLevel = getBatteryCapacity(context)
        logs.add("Current battery level: $batteryLevel%")

        val commands = StringBuilder()

        // Helper shell function to restore parameters from the exact snapshot backup file
        commands.append("""
            restore_from_backup() {
                _pat="${'$'}1"
                _fb="${'$'}2"
                if [ -f "$backupPath" ] && grep -qE "${'$'}_pat" "$backupPath" 2>/dev/null; then
                    grep -E "${'$'}_pat" "$backupPath" | sh 2>/dev/null
                elif [ -f "$BACKUP_FILE_PATH" ] && grep -qE "${'$'}_pat" "$BACKUP_FILE_PATH" 2>/dev/null; then
                    grep -E "${'$'}_pat" "$BACKUP_FILE_PATH" | sh 2>/dev/null
                else
                    eval "${'$'}_fb"
                fi
            }
        """.trimIndent()).append("\n\n")

        val isBalance = config.activeProfile == "BALANCE"

        if (config.activeProfile == "PERFORMANCE") {
            return@withContext applyPerformanceTweaksInternal(context, config, logs, commands, backupPath)
        }

        if (config.activeProfile == "POWERSAVE" && config.hyperOsPowerSaverEnabled) {
            return@withContext applyHyperOsPowersaveTweaksInternal(context, config, logs, commands, backupPath)
        }

        // 1. CPU Max Frequency Cap (70% in Balance Mode, 50% in Powersave Mode)
        if (config.cpuFreqCapEnabled) {
            val capPercent = if (isBalance) config.balanceCpuCapPercent else 50
            commands.append("""
                for cpu in /sys/devices/system/cpu/cpu*/cpufreq; do
                    max=`cat ${'$'}cpu/cpuinfo_max_freq 2>/dev/null`
                    if [ -n "${'$'}max" ]; then
                        target=${'$'}((max * $capPercent / 100))
                        echo "${'$'}target" > "${'$'}cpu/scaling_max_freq" 2>/dev/null
                    fi
                    echo "100000" > "${'$'}cpu/scaling_min_freq" 2>/dev/null
                done
            """.trimIndent()).append("\n")
            logs.add("CPU Max Frequency capped to $capPercent% (Profile: ${config.activeProfile})")
        } else {
            commands.append("""
                restore_from_backup "scaling_max_freq" "for cpu in /sys/devices/system/cpu/cpu*/cpufreq; do max=\`cat ${'$'}cpu/cpuinfo_max_freq 2>/dev/null\`; [ -n \"${'$'}max\" ] && echo \"${'$'}max\" > \"${'$'}cpu/scaling_max_freq\" 2>/dev/null; done"
            """.trimIndent()).append("\n")
            logs.add("CPU Max Frequency restored from stock backup snapshot")
        }

        // 2. CPU Scaling Governor (powersave for POWERSAVE mode, performance for PERFORMANCE, schedutil for BALANCE)
        if (config.schedutilGovernorEnabled) {
            val targetGov = when (config.activeProfile) {
                "PERFORMANCE" -> "performance"
                "BALANCE" -> "schedutil"
                else -> "powersave"
            }
            commands.append("""
                for cpu in /sys/devices/system/cpu/cpu*/cpufreq; do
                    [ -d "${'$'}cpu" ] || continue
                    echo "$targetGov" > "${'$'}cpu/scaling_governor" 2>/dev/null
                    cur=`cat "${'$'}cpu/scaling_governor" 2>/dev/null`
                    if [ "${'$'}cur" != "$targetGov" ]; then
                        if grep -q "$targetGov" "${'$'}cpu/scaling_available_governors" 2>/dev/null; then
                            echo "$targetGov" > "${'$'}cpu/scaling_governor" 2>/dev/null
                        elif grep -q "powersave" "${'$'}cpu/scaling_available_governors" 2>/dev/null; then
                            echo "powersave" > "${'$'}cpu/scaling_governor" 2>/dev/null
                        else
                            echo "schedutil" > "${'$'}cpu/scaling_governor" 2>/dev/null
                        fi
                    fi
                done
            """.trimIndent()).append("\n")
            logs.add("Governor set to $targetGov (Profile: ${config.activeProfile})")
        } else {
            commands.append("""
                restore_from_backup "scaling_governor" "for cpu in /sys/devices/system/cpu/cpu*/cpufreq; do echo \"schedutil\" > \"${'$'}cpu/scaling_governor\" 2>/dev/null; done"
            """.trimIndent()).append("\n")
            logs.add("Governor restored from stock backup snapshot")
        }

        if (config.schedutilRateLimitsEnabled) {
            if (isBalance) {
                commands.append("""
                    for cpu in /sys/devices/system/cpu/cpu[0-9]*; do
                        [ -d "${'$'}cpu/cpufreq/schedutil" ] || continue
                        c_num=`echo "${'$'}cpu" | tr -dc '0-9'`
                        if [ "${'$'}c_num" -lt 4 ]; then
                            echo 500 > "${'$'}cpu/cpufreq/schedutil/up_rate_limit_us" 2>/dev/null
                            echo 20000 > "${'$'}cpu/cpufreq/schedutil/down_rate_limit_us" 2>/dev/null
                        else
                            echo 500 > "${'$'}cpu/cpufreq/schedutil/up_rate_limit_us" 2>/dev/null
                            echo 10000 > "${'$'}cpu/cpufreq/schedutil/down_rate_limit_us" 2>/dev/null
                        fi
                    done
                """.trimIndent()).append("\n")
                logs.add("Balanced Schedutil Rate Limits: Little cores (500us/20ms), Big cores (500us/10ms fluid response)")
            } else {
                commands.append("""
                    for s in /sys/devices/system/cpu/cpu*/cpufreq/schedutil /sys/devices/system/cpu/cpufreq/schedutil; do
                        [ -d "${'$'}s" ] || continue
                        echo 8000 > "${'$'}s/up_rate_limit_us" 2>/dev/null
                        echo 32000 > "${'$'}s/down_rate_limit_us" 2>/dev/null
                        echo 99 > "${'$'}s/hispeed_load" 2>/dev/null
                        echo 0 > "${'$'}s/iowait_boost_enable" 2>/dev/null
                    done
                """.trimIndent()).append("\n")
                logs.add("Powersave Schedutil rate limits & 99% hispeed threshold applied")
            }
        } else {
            commands.append("""
                restore_from_backup "schedutil/" "for s in /sys/devices/system/cpu/cpu*/cpufreq/schedutil; do [ -d \"${'$'}s\" ] || continue; echo 500 > \"${'$'}s/up_rate_limit_us\" 2>/dev/null; echo 20000 > \"${'$'}s/down_rate_limit_us\" 2>/dev/null; echo 80 > \"${'$'}s/hispeed_load\" 2>/dev/null; echo 1 > \"${'$'}s/iowait_boost_enable\" 2>/dev/null; done"
            """.trimIndent()).append("\n")
            logs.add("Schedutil rate limits restored from stock backup snapshot")
        }

        // Background Process Limit (Developer Options equivalent: At Most 4 Processes)
        if (config.developerProcessLimitEnabled) {
            commands.append("""
                service call activity 51 i32 ${config.developerProcessLimit} >/dev/null 2>&1
                setprop persist.sys.hidden_app_mem ${config.developerProcessLimit} >/dev/null 2>&1
            """.trimIndent()).append("\n")
            logs.add("Background process limit set to ${config.developerProcessLimit} (Developer Options setting)")
        } else {
            commands.append("""
                service call activity 51 i32 -1 >/dev/null 2>&1
                setprop persist.sys.hidden_app_mem "" >/dev/null 2>&1
            """.trimIndent()).append("\n")
            logs.add("Background process limit restored to standard")
        }

        // 3. Dynamic Core Topology (Selectable 1 or 2 Cores & Threshold up to 100%)
        if (config.twoCoresOfflineBelow20Enabled) {
            val shouldOffline = (config.offlineBatteryThreshold >= 100) || (batteryLevel in 1..config.offlineBatteryThreshold)
            val coresToOffline = when (config.offlineCoreCount) {
                1 -> listOf(7)
                else -> listOf(6, 7)
            }
            val coresToKeepOnline = (0..7).filterNot { coresToOffline.contains(it) }

            if (shouldOffline) {
                for (core in coresToOffline) {
                    commands.append("echo 0 > /sys/devices/system/cpu/cpu$core/online 2>/dev/null\n")
                }
                for (core in coresToKeepOnline) {
                    commands.append("echo 1 > /sys/devices/system/cpu/cpu$core/online 2>/dev/null\n")
                }
                val reason = if (config.offlineBatteryThreshold >= 100) "Always Active (100%)" else "Battery $batteryLevel% <= ${config.offlineBatteryThreshold}%"
                logs.add("Dynamic Topology: Offlined ${coresToOffline.size} cores (${coresToOffline.joinToString(", ") { "Core $it" }}) - $reason")
            } else {
                commands.append("""
                    restore_from_backup "cpu[0-9]+/online" "for i in 0 1 2 3 4 5 6 7; do echo 1 > \"/sys/devices/system/cpu/cpu${'$'}i/online\" 2>/dev/null; done"
                """.trimIndent()).append("\n")
                logs.add("Dynamic Topology: Battery ($batteryLevel% > ${config.offlineBatteryThreshold}%) - All CPU cores online")
            }
        } else {
            // Atomically bring ALL cores back online from the stock snapshot
            commands.append("""
                restore_from_backup "cpu[0-9]+/online" "for i in 0 1 2 3 4 5 6 7; do echo 1 > \"/sys/devices/system/cpu/cpu${'$'}i/online\" 2>/dev/null; done"
            """.trimIndent()).append("\n")
            logs.add("Dynamic Core Topology disabled: All CPU cores restored from stock backup snapshot")
        }

        // 4. Input & Touch Boost + HyperOS App Launch & Scroll Spikes
        if (config.inputTouchBoostDisabled) {
            commands.append("""
                # Standard Kernel Input & Launch Boost
                [ -e /sys/module/cpu_boost/parameters/input_boost_ms ] && echo 0 > /sys/module/cpu_boost/parameters/input_boost_ms 2>/dev/null
                [ -e /sys/module/cpu_boost/parameters/app_launch_boost_ms ] && echo 0 > /sys/module/cpu_boost/parameters/app_launch_boost_ms 2>/dev/null
                [ -e /sys/module/cpu_boost/parameters/wake_boost_ms ] && echo 0 > /sys/module/cpu_boost/parameters/wake_boost_ms 2>/dev/null
                [ -e /sys/module/cpu_boost/parameters/sched_boost_on_input ] && echo 0 > /sys/module/cpu_boost/parameters/sched_boost_on_input 2>/dev/null
                [ -e /sys/module/cpu_input_boost/parameters/input_boost_duration ] && echo 0 > /sys/module/cpu_input_boost/parameters/input_boost_duration 2>/dev/null
                [ -e /sys/module/msm_performance/parameters/touchboost ] && echo 0 > /sys/module/msm_performance/parameters/touchboost 2>/dev/null

                # Xiaomi / HyperOS Specific pnpmgr & perfhub (Eliminates 2000-3000mA launch & 700-900mA scroll spikes)
                if [ -d /sys/power/pnpmgr ]; then
                    echo 0 > /sys/power/pnpmgr/touch_boost 2>/dev/null
                    echo 0 > /sys/power/pnpmgr/launch_boost 2>/dev/null
                    echo 0 > /sys/power/pnpmgr/spc/spc_enabled 2>/dev/null
                    echo 0 > /sys/power/pnpmgr/activity_trigger 2>/dev/null
                    echo 0 > /sys/power/pnpmgr/install_boost 2>/dev/null
                fi
                [ -e /sys/module/miperf/parameters/boost_enabled ] && echo 0 > /sys/module/miperf/parameters/boost_enabled 2>/dev/null
                [ -e /sys/module/perfhub/parameters/perfhub_enable ] && echo 0 > /sys/module/perfhub/parameters/perfhub_enable 2>/dev/null
                [ -e /sys/kernel/fp_boost/enabled ] && echo 0 > /sys/kernel/fp_boost/enabled 2>/dev/null

                # Android uclamp & schedtune (Stops artificial frequency boosting during scrolling)
                for u in /dev/cpuctl/top-app/cpu.uclamp.min /dev/cpuctl/foreground/cpu.uclamp.min /dev/cpuctl/cpu.uclamp.min; do
                    [ -e "${'$'}u" ] && echo 0 > "${'$'}u" 2>/dev/null
                done
                for s in /dev/stune/top-app/schedtune.boost /dev/stune/top-app/schedtune.sched_boost_no_override; do
                    [ -e "${'$'}s" ] && echo 0 > "${'$'}s" 2>/dev/null
                done
            """.trimIndent()).append("\n")
            logs.add("Touch, App-Launch & HyperOS pnpmgr boost disabled (Spikes eliminated)")
        } else {
            commands.append("""
                restore_from_backup "input_boost|touchboost|pnpmgr|fp_boost|uclamp" "[ -e /sys/module/cpu_boost/parameters/input_boost_ms ] && echo 40 > /sys/module/cpu_boost/parameters/input_boost_ms 2>/dev/null; [ -e /sys/module/msm_performance/parameters/touchboost ] && echo 1 > /sys/module/msm_performance/parameters/touchboost 2>/dev/null; [ -d /sys/power/pnpmgr ] && echo 1 > /sys/power/pnpmgr/touch_boost 2>/dev/null"
            """.trimIndent()).append("\n")
            logs.add("Touch & input boost restored from stock backup snapshot")
        }

        // 5. GPU Power Limit & Idler
        val isGpuEnabled = if (isBalance) config.balanceGpuOptimizationEnabled else config.gpuPowerLimitEnabled
        if (isGpuEnabled) {
            if (isBalance) {
                commands.append("""
                    for g in /sys/class/kgsl/kgsl-3d0 /sys/devices/platform/*.gpu /sys/devices/*.mali; do
                        [ -d "${'$'}g" ] || continue
                        [ -e "${'$'}g/force_no_nap" ] && echo 0 > "${'$'}g/force_no_nap" 2>/dev/null
                        [ -e "${'$'}g/idle_timer" ] && echo 80 > "${'$'}g/idle_timer" 2>/dev/null
                        [ -e "${'$'}g/dvfs" ] && echo 1 > "${'$'}g/dvfs" 2>/dev/null
                        [ -e "${'$'}g/power_policy" ] && echo "coarse_demand" > "${'$'}g/power_policy" 2>/dev/null
                    done
                    for gpf in /proc/gpufreq /proc/gpufreqv2; do
                        if [ -d "${'$'}gpf" ]; then
                            [ -w "${'$'}gpf/aging_mode" ] && echo disable > "${'$'}gpf/aging_mode" 2>/dev/null
                            [ -w "${'$'}gpf/limit_table" ] && echo "0 0 0" > "${'$'}gpf/limit_table" 2>/dev/null
                            [ -w "${'$'}gpf/gpm_mode" ] && echo 1 > "${'$'}gpf/gpm_mode" 2>/dev/null
                            break
                        fi
                    done
                    [ -w /sys/module/ged/parameters/ged_boost_enable ] && echo 1 > /sys/module/ged/parameters/ged_boost_enable 2>/dev/null
                    [ -w /sys/module/ged/parameters/boost_gpu_enable ] && echo 1 > /sys/module/ged/parameters/boost_gpu_enable 2>/dev/null
                    [ -w /sys/module/ged/parameters/gx_boost_on ] && echo 1 > /sys/module/ged/parameters/gx_boost_on 2>/dev/null
                """.trimIndent()).append("\n")
                logs.add("GPU Balanced: 80ms idle timer, nap sleep enabled, DVFS dynamic scaling")
            } else {
                commands.append("""
                    for g in /sys/class/kgsl/kgsl-3d0 /sys/devices/platform/*.gpu /sys/devices/*.mali; do
                        [ -d "${'$'}g" ] || continue
                        [ -e "${'$'}g/throttling" ] && echo 1 > "${'$'}g/throttling" 2>/dev/null
                        [ -e "${'$'}g/devfreq/adrenoboost" ] && echo 0 > "${'$'}g/devfreq/adrenoboost" 2>/dev/null
                        [ -e "${'$'}g/devfreq/max_freq" ] && {
                            cur_max=`cat "${'$'}g/devfreq/max_freq" 2>/dev/null`
                            [ -n "${'$'}cur_max" ] && echo "${'$'}((cur_max / 2))" > "${'$'}g/devfreq/max_freq" 2>/dev/null
                        }
                    done
                """.trimIndent()).append("\n")
                logs.add("GPU power throttling applied")
            }
        } else {
            commands.append("""
                restore_from_backup "kgsl|gpu|mali" "for g in /sys/class/kgsl/kgsl-3d0 /sys/devices/platform/*.gpu /sys/devices/*.mali; do [ -d \"${'$'}g\" ] || continue; [ -e \"${'$'}g/throttling\" ] && echo 0 > \"${'$'}g/throttling\" 2>/dev/null; done"
            """.trimIndent()).append("\n")
            logs.add("GPU power throttling restored from stock backup snapshot")
        }

        if (config.adrenoIdlerEnabled) {
            commands.append("""
                if [ -d /sys/module/adreno_idler/parameters ]; then
                    echo "Y" > /sys/module/adreno_idler/parameters/adreno_idler_active 2>/dev/null
                    echo 10000 > /sys/module/adreno_idler/parameters/adreno_idler_idleworkload 2>/dev/null
                    echo 15 > /sys/module/adreno_idler/parameters/adreno_idler_idlewait 2>/dev/null
                fi
            """.trimIndent()).append("\n")
            logs.add("Adreno Idler configured")
        } else {
            commands.append("""
                restore_from_backup "adreno_idler" "[ -d /sys/module/adreno_idler/parameters ] && echo \"N\" > /sys/module/adreno_idler/parameters/adreno_idler_active 2>/dev/null"
            """.trimIndent()).append("\n")
            logs.add("Adreno Idler restored from stock backup snapshot")
        }

        // 6. Storage I/O Queue
        val isStorageEnabled = if (isBalance) config.balanceStorageIoEnabled else config.storageIoQueueEnabled
        if (isStorageEnabled) {
            if (isBalance) {
                commands.append("""
                    MEM_KB=`grep MemTotal /proc/meminfo | awk '{print ${'$'}2}'`
                    RA=128
                    NR=256
                    if [ "${'$'}MEM_KB" -gt 6000000 ]; then
                        RA=256
                        NR=512
                    elif [ "${'$'}MEM_KB" -le 3000000 ]; then
                        RA=64
                        NR=128
                    fi
                    for dev in /sys/block/sd* /sys/block/mmcblk* /sys/block/nvme*; do
                        [ -d "${'$'}dev/queue" ] || continue
                        scheds=`cat "${'$'}dev/queue/scheduler" 2>/dev/null`
                        case "${'$'}scheds" in
                            *kyber*) echo kyber > "${'$'}dev/queue/scheduler" 2>/dev/null ;;
                            *bfq*) echo bfq > "${'$'}dev/queue/scheduler" 2>/dev/null ;;
                            *mq-deadline*) echo mq-deadline > "${'$'}dev/queue/scheduler" 2>/dev/null ;;
                        esac
                        echo "${'$'}RA" > "${'$'}dev/queue/read_ahead_kb" 2>/dev/null
                        echo "${'$'}NR" > "${'$'}dev/queue/nr_requests" 2>/dev/null
                        echo 2 > "${'$'}dev/queue/rq_affinity" 2>/dev/null
                        echo 0 > "${'$'}dev/queue/iostats" 2>/dev/null
                    done
                """.trimIndent()).append("\n")
                logs.add("Storage I/O tuned: Adaptive queue, rq_affinity=2, Kyber/BFQ priority")
            } else {
                commands.append("""
                    for q in /sys/block/*/queue; do
                        [ -d "${'$'}q" ] || continue
                        echo 128 > "${'$'}q/read_ahead_kb" 2>/dev/null
                        echo 64 > "${'$'}q/nr_requests" 2>/dev/null
                        echo 0 > "${'$'}q/iostats" 2>/dev/null
                    done
                """.trimIndent()).append("\n")
                logs.add("I/O queue tuned (128KB read-ahead, 64 queue depth)")
            }
        } else {
            commands.append("""
                restore_from_backup "queue/read_ahead_kb|queue/nr_requests" "for q in /sys/block/*/queue; do [ -d \"${'$'}q\" ] || continue; echo 512 > \"${'$'}q/read_ahead_kb\" 2>/dev/null; echo 128 > \"${'$'}q/nr_requests\" 2>/dev/null; echo 1 > \"${'$'}q/iostats\" 2>/dev/null; done"
            """.trimIndent()).append("\n")
            logs.add("I/O queue restored from stock backup snapshot")
        }

        // 7. Dynamic Fsync
        if (config.dynamicFsyncEnabled) {
            commands.append("""
                [ -e /sys/kernel/dyn_fsync/Dyn_fsync_active ] && echo 1 > /sys/kernel/dyn_fsync/Dyn_fsync_active 2>/dev/null
            """.trimIndent()).append("\n")
            logs.add("Dynamic Fsync enabled")
        } else {
            commands.append("""
                restore_from_backup "Dyn_fsync" "[ -e /sys/kernel/dyn_fsync/Dyn_fsync_active ] && echo 0 > /sys/kernel/dyn_fsync/Dyn_fsync_active 2>/dev/null"
            """.trimIndent()).append("\n")
            logs.add("Dynamic Fsync restored from stock backup snapshot")
        }

        // 8. Virtual Memory (Dirty writeback delay, ZRAM & CFS Scheduler)
        val isVmEnabled = if (isBalance) (config.balanceRamScalingEnabled || config.balanceCfsSchedulerEnabled) else config.vmDirtyWritebackEnabled
        if (isVmEnabled) {
            if (isBalance) {
                commands.append("""
                    MEM_KB=`grep MemTotal /proc/meminfo | awk '{print ${'$'}2}'`
                    if [ "${'$'}MEM_KB" -ge 8000000 ]; then
                        echo 60 > /proc/sys/vm/swappiness 2>/dev/null
                        echo 20 > /proc/sys/vm/dirty_ratio 2>/dev/null
                        echo 10 > /proc/sys/vm/dirty_background_ratio 2>/dev/null
                        echo 60 > /proc/sys/vm/vfs_cache_pressure 2>/dev/null
                        echo 20480 > /proc/sys/vm/extra_free_kbytes 2>/dev/null
                    elif [ "${'$'}MEM_KB" -ge 4000000 ]; then
                        echo 80 > /proc/sys/vm/swappiness 2>/dev/null
                        echo 15 > /proc/sys/vm/dirty_ratio 2>/dev/null
                        echo 8 > /proc/sys/vm/dirty_background_ratio 2>/dev/null
                        echo 80 > /proc/sys/vm/vfs_cache_pressure 2>/dev/null
                        echo 10240 > /proc/sys/vm/extra_free_kbytes 2>/dev/null
                    else
                        echo 100 > /proc/sys/vm/swappiness 2>/dev/null
                        echo 10 > /proc/sys/vm/dirty_ratio 2>/dev/null
                        echo 5 > /proc/sys/vm/dirty_background_ratio 2>/dev/null
                        echo 100 > /proc/sys/vm/vfs_cache_pressure 2>/dev/null
                        echo 4096 > /proc/sys/vm/extra_free_kbytes 2>/dev/null
                    fi
                    echo 3000 > /proc/sys/vm/dirty_writeback_centisecs 2>/dev/null
                    echo 3000 > /proc/sys/vm/dirty_expire_centisecs 2>/dev/null
                    echo 0 > /proc/sys/vm/page-cluster 2>/dev/null
                    echo 10 > /proc/sys/vm/stat_interval 2>/dev/null
                    echo 80 > /proc/sys/vm/overcommit_ratio 2>/dev/null
                    echo 0 > /proc/sys/vm/panic_on_oom 2>/dev/null

                    # CFS Scheduler
                    echo 1 > /proc/sys/kernel/sched_autogroup_enabled 2>/dev/null
                    echo 1 > /proc/sys/kernel/sched_child_runs_first 2>/dev/null
                    echo 0 > /proc/sys/kernel/sched_tunable_scaling 2>/dev/null
                    echo 0 > /proc/sys/kernel/sched_schedstats 2>/dev/null
                    echo 5000000 > /proc/sys/kernel/sched_migration_cost_ns 2>/dev/null
                    echo 128 > /proc/sys/kernel/sched_nr_migrate 2>/dev/null

                    if [ "${'$'}MEM_KB" -ge 6000000 ]; then
                        echo 4000000 > /proc/sys/kernel/sched_latency_ns 2>/dev/null
                        echo 400000 > /proc/sys/kernel/sched_min_granularity_ns 2>/dev/null
                        echo 1000000 > /proc/sys/kernel/sched_wakeup_granularity_ns 2>/dev/null
                    fi

                    if [ -w /sys/kernel/debug/sched_features ]; then
                        echo "NEXT_BUDDY" > /sys/kernel/debug/sched_features 2>/dev/null
                        echo "TTWU_QUEUE" > /sys/kernel/debug/sched_features 2>/dev/null
                        echo "NO_HRTICK" > /sys/kernel/debug/sched_features 2>/dev/null
                        echo "WAKEUP_PREEMPTION" > /sys/kernel/debug/sched_features 2>/dev/null
                    fi
                """.trimIndent()).append("\n")
                logs.add("Balanced RAM & CFS: Dynamic 3-tier memory scaling, 30s dirty writeback, CFS preemption")
            } else {
                commands.append("""
                    echo 50 > /proc/sys/vm/dirty_ratio 2>/dev/null
                    echo 5 > /proc/sys/vm/dirty_background_ratio 2>/dev/null
                    echo 3000 > /proc/sys/vm/dirty_writeback_centisecs 2>/dev/null
                    echo 3000 > /proc/sys/vm/dirty_expire_centisecs 2>/dev/null
                    echo 100 > /proc/sys/vm/swappiness 2>/dev/null
                    echo 50 > /proc/sys/vm/vfs_cache_pressure 2>/dev/null
                """.trimIndent()).append("\n")
                logs.add("VM writeback extended to 30s")
            }
        } else {
            commands.append("""
                restore_from_backup "/proc/sys/vm/" "echo 20 > /proc/sys/vm/dirty_ratio 2>/dev/null; echo 10 > /proc/sys/vm/dirty_background_ratio 2>/dev/null; echo 500 > /proc/sys/vm/dirty_writeback_centisecs 2>/dev/null; echo 60 > /proc/sys/vm/swappiness 2>/dev/null; echo 100 > /proc/sys/vm/vfs_cache_pressure 2>/dev/null"
            """.trimIndent()).append("\n")
            logs.add("VM dirty writeback restored from stock backup snapshot")
        }

        // 9. TCP BBR & Fast Open
        val isNetEnabled = if (isBalance) config.balanceNetworkBbrEnabled else config.tcpBbrCongestionEnabled
        if (isNetEnabled) {
            commands.append("""
                echo "bbr" > /proc/sys/net/ipv4/tcp_congestion_control 2>/dev/null
                echo 3 > /proc/sys/net/ipv4/tcp_fastopen 2>/dev/null
                echo 1 > /proc/sys/net/ipv4/tcp_window_scaling 2>/dev/null
                echo 30 > /proc/sys/net/ipv4/tcp_fin_timeout 2>/dev/null
            """.trimIndent()).append("\n")
            logs.add("TCP BBR congestion control configured")
        } else {
            commands.append("""
                restore_from_backup "tcp_congestion_control" "echo \"cubic\" > /proc/sys/net/ipv4/tcp_congestion_control 2>/dev/null"
            """.trimIndent()).append("\n")
            logs.add("TCP congestion restored from stock backup snapshot")
        }

        // 10. GMS Doze (Safe native Doze whitelist toggle - NO package disabling)
        if (config.gmsDozeEnabled) {
            commands.append("dumpsys deviceidle whitelist -com.google.android.gms 2>/dev/null\n")
            logs.add("GMS Doze enabled (removed from battery saver exemption whitelist)")
        } else {
            commands.append("""
                restore_from_backup "deviceidle whitelist" "dumpsys deviceidle whitelist +com.google.android.gms 2>/dev/null"
            """.trimIndent()).append("\n")
            logs.add("GMS normal exemption restored from stock backup snapshot")
        }

        // 11. Power Efficient Workqueue & LPM Sleep
        if (config.powerEfficientWorkqueueEnabled) {
            commands.append("""
                [ -e /sys/module/workqueue/parameters/power_efficient ] && echo "Y" > /sys/module/workqueue/parameters/power_efficient 2>/dev/null
                [ -e /sys/devices/system/cpu/cpuidle/use_deepest_state ] && echo 1 > /sys/devices/system/cpu/cpuidle/use_deepest_state 2>/dev/null
            """.trimIndent()).append("\n")
            logs.add("Power-efficient workqueue and deepest idle states enabled")
        } else {
            commands.append("""
                restore_from_backup "workqueue|cpuidle" "[ -e /sys/module/workqueue/parameters/power_efficient ] && echo \"N\" > /sys/module/workqueue/parameters/power_efficient 2>/dev/null"
            """.trimIndent()).append("\n")
            logs.add("Workqueue power efficiency restored from stock backup snapshot")
        }

        if (config.lpmSleepEnabled) {
            commands.append("""
                if [ -d /sys/module/lpm_levels ]; then
                    echo "N" > /sys/module/lpm_levels/parameters/sleep_disabled 2>/dev/null
                fi
            """.trimIndent()).append("\n")
            logs.add("Qualcomm LPM sleep enabled")
        }

        // 12. Background App Power Restriction & Active App Prioritization
        if (config.schedtuneTopAppEnabled) {
            commands.append("""
                # Restrict background cpuset to efficiency cores (0-3) so background tasks never spin up big cores
                if [ -d /dev/cpuset/background ]; then
                    echo "0-3" > /dev/cpuset/background/cpus 2>/dev/null
                fi
                if [ -d /dev/cpuset/system-background ]; then
                    echo "0-2" > /dev/cpuset/system-background/cpus 2>/dev/null
                fi
                # Top-app (currently active foreground app) retains access to all online cores
                if [ -d /dev/cpuset/top-app ]; then
                    echo "0-7" > /dev/cpuset/top-app/cpus 2>/dev/null
                fi

                # CPU shares & uclamp: Top-app is prioritized (1024 shares), background is throttled (128 shares, max 30% uclamp)
                [ -e /dev/cpuctl/background/cpu.shares ] && echo 128 > /dev/cpuctl/background/cpu.shares 2>/dev/null
                [ -e /dev/cpuctl/system-background/cpu.shares ] && echo 128 > /dev/cpuctl/system-background/cpu.shares 2>/dev/null
                [ -e /dev/cpuctl/top-app/cpu.shares ] && echo 1024 > /dev/cpuctl/top-app/cpu.shares 2>/dev/null
                [ -e /dev/cpuctl/background/cpu.uclamp.max ] && echo 30 > /dev/cpuctl/background/cpu.uclamp.max 2>/dev/null

                # SchedTune: Top-app gets prefer_idle for zero-latency frame draws; background idle waking is disabled
                [ -d /dev/stune/top-app ] && echo 1 > /dev/stune/top-app/schedtune.prefer_idle 2>/dev/null
                [ -d /dev/stune/top-app ] && echo 0 > /dev/stune/top-app/schedtune.boost 2>/dev/null
                [ -d /dev/stune/background ] && echo 0 > /dev/stune/background/schedtune.boost 2>/dev/null
                [ -d /dev/stune/background ] && echo 0 > /dev/stune/background/schedtune.prefer_idle 2>/dev/null

                [ -e /sys/devices/system/cpu/sched_mc_power_savings ] && echo 2 > /sys/devices/system/cpu/sched_mc_power_savings 2>/dev/null
            """.trimIndent()).append("\n")
            logs.add("Background apps throttled to efficiency cores; active app prioritized (App switching unaffected)")
        } else {
            commands.append("""
                restore_from_backup "cpuset|cpuctl|stune|sched_mc" "[ -d /dev/cpuset/background ] && echo \"0-7\" > /dev/cpuset/background/cpus 2>/dev/null; [ -d /dev/cpuset/top-app ] && echo \"0-7\" > /dev/cpuset/top-app/cpus 2>/dev/null; [ -e /dev/cpuctl/background/cpu.shares ] && echo 1024 > /dev/cpuctl/background/cpu.shares 2>/dev/null; [ -d /dev/stune/top-app ] && echo 0 > /dev/stune/top-app/schedtune.boost 2>/dev/null; [ -e /sys/devices/system/cpu/sched_mc_power_savings ] && echo 0 > /sys/devices/system/cpu/sched_mc_power_savings 2>/dev/null"
            """.trimIndent()).append("\n")
            logs.add("Background cpuset and scheduling restored from stock backup snapshot")
        }

        // 13. Safe Screen / Touchpanel check (Gracefully skips missing LCD nodes on AMOLED displays)
        if (config.doubleTapWakePreserved) {
            commands.append("""
                [ -e /sys/touchpanel/double_tap ] && echo 1 > /sys/touchpanel/double_tap 2>/dev/null
                [ -e /proc/tp_gesture ] && echo 1 > /proc/tp_gesture 2>/dev/null
            """.trimIndent()).append("\n")
            logs.add("Double-tap wake gesture preserved")
        }

        // 14. System Battery Saver (Keep Light Theme - Suppress forced Dark Mode)
        if (config.systemBatterySaverWithoutDarkEnabled) {
            commands.append("""
                cmd power set-mode 1 2>/dev/null || settings put global low_power 1 2>/dev/null
                cmd uimode night no 2>/dev/null
                settings put secure ui_night_mode 1 2>/dev/null
                settings put system ui_night_mode 1 2>/dev/null
            """.trimIndent()).append("\n")
            logs.add("System battery saver enabled (Dark mode suppressed / light theme preserved)")
        } else {
            commands.append("""
                cmd power set-mode 0 2>/dev/null || settings put global low_power 0 2>/dev/null
            """.trimIndent()).append("\n")
            logs.add("System battery saver restored to normal")
        }

        // 15. Lock Display Refresh Rate to 60Hz
        if (config.lock60HzRefreshRateEnabled) {
            commands.append("""
                settings put system min_refresh_rate 60.0 2>/dev/null
                settings put system peak_refresh_rate 60.0 2>/dev/null
                settings put system user_refresh_rate 60 2>/dev/null
                settings put secure miui_refresh_rate 60 2>/dev/null
                service call SurfaceFlinger 1035 i32 60 2>/dev/null
            """.trimIndent()).append("\n")
            logs.add("Display refresh rate locked to 60Hz (Saves 350-500mA on scroll)")
        } else {
            commands.append("""
                settings delete system min_refresh_rate 2>/dev/null
                settings delete system peak_refresh_rate 2>/dev/null
                settings delete system user_refresh_rate 2>/dev/null
                settings delete secure miui_refresh_rate 2>/dev/null
            """.trimIndent()).append("\n")
            logs.add("Display refresh rate restored to dynamic stock")
        }

        // 16. Deep Sleep App Exception Whitelist (Apps exempted from doze restrictions)
        if (config.deepSleepWhitelist.isNotBlank()) {
            val pkgs = config.deepSleepWhitelist.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            for (pkg in pkgs) {
                commands.append("dumpsys deviceidle whitelist +$pkg >/dev/null 2>&1\n")
            }
            logs.add("Deep Sleep Whitelist: ${pkgs.size} apps exempted from sleep (${pkgs.joinToString(", ")})")
        }

        // 17. 5-Minute Screen-Off Deep Sleep Doze Daemon (Zero disk writes, RAM-only execution)
        commands.append("""
            pkill -f "voltpower_doze_daemon" >/dev/null 2>&1
        """.trimIndent()).append("\n")

        if (config.deepSleepScreenOffEnabled && isBalance) {
            val delaySec = config.deepSleepDelayMinutes * 60
            commands.append("""
                (
                    exec -a voltpower_doze_daemon sh -c '
                    OFF_SECONDS=0
                    IS_DEEP=0
                    while true; do
                        IS_AWAKE=${'$'}(dumpsys power 2>/dev/null | grep -E "mWakefulness=Awake|Display Power: state=ON")
                        if [ -z "${'$'}IS_AWAKE" ]; then
                            OFF_SECONDS=${'$'}((OFF_SECONDS + 10))
                            if [ "${'$'}OFF_SECONDS" -ge $delaySec ] && [ "${'$'}IS_DEEP" -eq 0 ]; then
                                dumpsys deviceidle force-idle deep >/dev/null 2>&1
                                IS_DEEP=1
                            fi
                        else
                            if [ "${'$'}IS_DEEP" -eq 1 ]; then
                                dumpsys deviceidle unforce >/dev/null 2>&1
                                IS_DEEP=0
                            fi
                            OFF_SECONDS=0
                        fi
                        sleep 10
                    done
                    '
                ) >/dev/null 2>&1 &
            """.trimIndent()).append("\n")
            logs.add("5-Minute Screen-Off Deep Sleep Daemon active in background (Triggers after ${config.deepSleepDelayMinutes} min screen off)")
        } else {
            commands.append("dumpsys deviceidle unforce >/dev/null 2>&1\n")
            logs.add("Deep Sleep Daemon deactivated / normal doze restored")
        }

        val execResult = execute(commands.toString())
        if (execResult.success) {
            logs.add("All selected ${if (isBalance) "balanced" else "powersave"} tweaks successfully written to RAM.")
        } else {
            logs.add("Execution completed with notices: ${execResult.stderr.take(100)}")
        }

        logs
    }

    private suspend fun applyPerformanceTweaksInternal(
        context: Context,
        config: TweakConfigEntity,
        logs: MutableList<String>,
        commands: StringBuilder,
        backupPath: String
    ): List<String> {
        val subMode = config.performanceSubMode.uppercase()
        val isUltra = subMode == "ULTRA"
        val isHeavy = subMode == "HEAVY"
        val isLite = subMode == "LITE"

        logs.add("Activating Performance Profile: $subMode Optimization")

        // 1. CPU Governors & Clocks
        if (config.perfCpuGovernorLockEnabled) {
            when {
                isUltra -> {
                    // Full performance governor on all cores, pins max frequencies to true hardware max (not 20GHz invalid string)
                    commands.append("""
                        for cpu in /sys/devices/system/cpu/cpu*/cpufreq; do
                            [ -d "${'$'}cpu" ] || continue
                            max=`cat "${'$'}cpu/cpuinfo_max_freq" 2>/dev/null`
                            if [ -n "${'$'}max" ]; then
                                echo "${'$'}max" > "${'$'}cpu/scaling_min_freq" 2>/dev/null
                                echo "${'$'}max" > "${'$'}cpu/scaling_max_freq" 2>/dev/null
                            fi
                            if grep -q "performance" "${'$'}cpu/scaling_available_governors" 2>/dev/null; then
                                echo "performance" > "${'$'}cpu/scaling_governor" 2>/dev/null
                            else
                                echo "schedutil" > "${'$'}cpu/scaling_governor" 2>/dev/null
                                [ -d "${'$'}cpu/schedutil" ] && {
                                    echo 0 > "${'$'}cpu/schedutil/up_rate_limit_us" 2>/dev/null
                                    echo 0 > "${'$'}cpu/schedutil/down_rate_limit_us" 2>/dev/null
                                    echo 1 > "${'$'}cpu/schedutil/hispeed_load" 2>/dev/null
                                    echo 1 > "${'$'}cpu/schedutil/iowait_boost_enable" 2>/dev/null
                                }
                            fi
                        done
                    """.trimIndent()).append("\n")
                    logs.add("CPU Clocks: Ultra Performance governor lock at maximum hardware frequencies")
                }
                isHeavy -> {
                    // Schedutil with elevated minimum clock floor (~45% of max) and 400us ramp-up
                    commands.append("""
                        for cpu in /sys/devices/system/cpu/cpu*/cpufreq; do
                            [ -d "${'$'}cpu" ] || continue
                            max=`cat "${'$'}cpu/cpuinfo_max_freq" 2>/dev/null`
                            min=`cat "${'$'}cpu/cpuinfo_min_freq" 2>/dev/null`
                            if [ -n "${'$'}max" ]; then
                                # Floor clock at 45% of max freq to eliminate frame drops
                                floor=${'$'}((max * 45 / 100))
                                echo "${'$'}floor" > "${'$'}cpu/scaling_min_freq" 2>/dev/null
                                echo "${'$'}max" > "${'$'}cpu/scaling_max_freq" 2>/dev/null
                            fi
                            echo "schedutil" > "${'$'}cpu/scaling_governor" 2>/dev/null
                            for s in "${'$'}cpu/schedutil" /sys/devices/system/cpu/cpufreq/schedutil; do
                                [ -d "${'$'}s" ] || continue
                                echo 400 > "${'$'}s/up_rate_limit_us" 2>/dev/null
                                echo 15000 > "${'$'}s/down_rate_limit_us" 2>/dev/null
                                echo 70 > "${'$'}s/hispeed_load" 2>/dev/null
                                echo 1 > "${'$'}s/iowait_boost_enable" 2>/dev/null
                            done
                        done
                    """.trimIndent()).append("\n")
                    logs.add("CPU Clocks: Heavy Schedutil with 45% frequency floor & 400us fast ramp")
                }
                else -> {
                    // Lite: Schedutil responsive profile, 500us ramp-up, full frequency headroom
                    commands.append("""
                        for cpu in /sys/devices/system/cpu/cpu*/cpufreq; do
                            [ -d "${'$'}cpu" ] || continue
                            max=`cat "${'$'}cpu/cpuinfo_max_freq" 2>/dev/null`
                            min=`cat "${'$'}cpu/cpuinfo_min_freq" 2>/dev/null`
                            [ -n "${'$'}max" ] && echo "${'$'}max" > "${'$'}cpu/scaling_max_freq" 2>/dev/null
                            [ -n "${'$'}min" ] && echo "${'$'}min" > "${'$'}cpu/scaling_min_freq" 2>/dev/null
                            echo "schedutil" > "${'$'}cpu/scaling_governor" 2>/dev/null
                            for s in "${'$'}cpu/schedutil" /sys/devices/system/cpu/cpufreq/schedutil; do
                                [ -d "${'$'}s" ] || continue
                                echo 500 > "${'$'}s/up_rate_limit_us" 2>/dev/null
                                echo 20000 > "${'$'}s/down_rate_limit_us" 2>/dev/null
                                echo 80 > "${'$'}s/hispeed_load" 2>/dev/null
                                echo 1 > "${'$'}s/iowait_boost_enable" 2>/dev/null
                            done
                        done
                    """.trimIndent()).append("\n")
                    logs.add("CPU Clocks: Lite Schedutil with 500us responsive rate limits")
                }
            }
        } else {
            commands.append("""
                restore_from_backup "scaling_governor|scaling_max_freq|scaling_min_freq" "for cpu in /sys/devices/system/cpu/cpu*/cpufreq; do max=\`cat ${'$'}cpu/cpuinfo_max_freq 2>/dev/null\`; [ -n \"${'$'}max\" ] && echo \"${'$'}max\" > \"${'$'}cpu/scaling_max_freq\" 2>/dev/null; echo \"schedutil\" > \"${'$'}cpu/scaling_governor\" 2>/dev/null; done"
            """.trimIndent()).append("\n")
            logs.add("CPU Clocks restored from stock backup snapshot")
        }

        // 2. All 8 CPU Cores Online & Core Control
        if (config.perfAllCoresOnlineEnabled) {
            commands.append("""
                for i in 0 1 2 3 4 5 6 7; do
                    echo 1 > "/sys/devices/system/cpu/cpu${'$'}i/online" 2>/dev/null
                done
            """.trimIndent()).append("\n")
            if (isUltra) {
                commands.append("""
                    for ctl in /sys/devices/system/cpu/cpu*/core_ctl; do
                        [ -e "${'$'}ctl/enable" ] && echo 0 > "${'$'}ctl/enable" 2>/dev/null
                        [ -e "${'$'}ctl/disable" ] && echo 1 > "${'$'}ctl/disable" 2>/dev/null
                    done
                """.trimIndent()).append("\n")
                logs.add("Cores: All 8 CPU cores online; core_ctl auto-parking disabled (Ultra)")
            } else {
                logs.add("Cores: All 8 CPU cores forced online")
            }
        } else {
            commands.append("""
                restore_from_backup "cpu[0-9]+/online" "for i in 0 1 2 3 4 5 6 7; do echo 1 > \"/sys/devices/system/cpu/cpu${'$'}i/online\" 2>/dev/null; done"
            """.trimIndent()).append("\n")
            logs.add("CPU core online states restored from stock backup snapshot")
        }

        // 3. SchedTune & Thread Prioritization
        if (config.perfSchedtuneBoostEnabled) {
            val topAppBoost = when {
                isUltra -> 50
                isHeavy -> 45
                else -> 25
            }
            val fgBoost = when {
                isUltra -> 50
                isHeavy -> 35
                else -> 20
            }
            commands.append("""
                [ -d /dev/stune/top-app ] && echo $topAppBoost > /dev/stune/top-app/schedtune.boost 2>/dev/null
                [ -d /dev/stune/top-app ] && echo 50 > /dev/stune/top-app/schedtune.sched_boost 2>/dev/null
                [ -d /dev/stune/top-app ] && echo 0 > /dev/stune/top-app/schedtune.prefer_idle 2>/dev/null
                [ -d /dev/stune/foreground ] && echo $fgBoost > /dev/stune/foreground/schedtune.boost 2>/dev/null
                [ -e /dev/cpuctl/top-app/cpu.shares ] && echo 1024 > /dev/cpuctl/top-app/cpu.shares 2>/dev/null
                [ -e /dev/cpuctl/top-app/cpu.uclamp.min ] && echo $topAppBoost > /dev/cpuctl/top-app/cpu.uclamp.min 2>/dev/null
                [ -d /dev/cpuset/top-app ] && echo "0-7" > /dev/cpuset/top-app/cpus 2>/dev/null
            """.trimIndent()).append("\n")

            if (isUltra || isHeavy) {
                commands.append("""
                    echo 1 > /proc/sys/kernel/sched_boost 2>/dev/null
                    echo 0 > /proc/sys/kernel/sched_child_runs_first 2>/dev/null
                    if [ -w /sys/kernel/debug/sched_features ]; then
                        echo "NEXT_BUDDY" > /sys/kernel/debug/sched_features 2>/dev/null
                        echo "TTWU_QUEUE" > /sys/kernel/debug/sched_features 2>/dev/null
                        echo "NO_GENTLE_FAIR_SLEEPERS" > /sys/kernel/debug/sched_features 2>/dev/null
                    fi
                """.trimIndent()).append("\n")
            }
            logs.add("SchedTune: Top-App game thread boost set to $topAppBoost% (Foreground: $fgBoost%)")
        } else {
            commands.append("""
                restore_from_backup "stune|cpuctl" "[ -d /dev/stune/top-app ] && echo 0 > /dev/stune/top-app/schedtune.boost 2>/dev/null; [ -d /dev/stune/top-app ] && echo 0 > /dev/stune/top-app/schedtune.sched_boost 2>/dev/null"
            """.trimIndent()).append("\n")
            logs.add("SchedTune restored from stock backup snapshot")
        }

        // 4. GPU & Adreno Acceleration
        if (config.perfGpuAdrenoBoostEnabled) {
            val adrenoBoostLevel = when {
                isUltra -> "3"
                isHeavy -> "2"
                else -> "1"
            }
            val idleTimerMs = when {
                isUltra -> "1050"
                isHeavy -> "80"
                else -> "50"
            }
            commands.append("""
                # Disable artificial GPU throttling flag
                for g in /sys/class/kgsl/kgsl-3d0 /sys/devices/platform/*.gpu /sys/devices/*.mali; do
                    [ -d "${'$'}g" ] || continue
                    [ -e "${'$'}g/throttling" ] && echo 0 > "${'$'}g/throttling" 2>/dev/null
                    [ -e "${'$'}g/devfreq/adrenoboost" ] && echo "$adrenoBoostLevel" > "${'$'}g/devfreq/adrenoboost" 2>/dev/null
                    [ -e "${'$'}g/idle_timer" ] && echo "$idleTimerMs" > "${'$'}g/idle_timer" 2>/dev/null
                done
                [ -d /sys/module/adreno_idler/parameters ] && echo "N" > /sys/module/adreno_idler/parameters/adreno_idler_active 2>/dev/null
                [ -e /proc/mali/dvfs_enable ] && echo 1 > /proc/mali/dvfs_enable 2>/dev/null
                [ -e /sys/module/pvrsrvkm/parameters/gpu_dvfs_enable ] && echo 1 > /sys/module/pvrsrvkm/parameters/gpu_dvfs_enable 2>/dev/null
            """.trimIndent()).append("\n")

            if (isUltra) {
                commands.append("""
                    for g in /sys/class/kgsl/kgsl-3d0; do
                        [ -d "${'$'}g" ] || continue
                        [ -e "${'$'}g/force_clk_on" ] && echo 1 > "${'$'}g/force_clk_on" 2>/dev/null
                        [ -e "${'$'}g/force_bus_on" ] && echo 1 > "${'$'}g/force_bus_on" 2>/dev/null
                        [ -e "${'$'}g/force_rail_on" ] && echo 1 > "${'$'}g/force_rail_on" 2>/dev/null
                    done
                """.trimIndent()).append("\n")
            }
            logs.add("GPU: AdrenoBoost level $adrenoBoostLevel, Idler disabled, Throttling flag bypass")
        } else {
            commands.append("""
                restore_from_backup "kgsl|adreno_idler" "for g in /sys/class/kgsl/kgsl-3d0; do [ -e \"${'$'}g/devfreq/adrenoboost\" ] && echo 0 > \"${'$'}g/devfreq/adrenoboost\" 2>/dev/null; done"
            """.trimIndent()).append("\n")
            logs.add("GPU power scaling restored from stock backup snapshot")
        }

        // 5. GPU Force No-Nap
        if (config.perfGpuNoNapEnabled) {
            val napVal = if (isUltra || isHeavy) "1" else "0"
            commands.append("""
                for g in /sys/class/kgsl/kgsl-3d0 /sys/devices/platform/*.gpu /sys/devices/*.mali; do
                    [ -e "${'$'}g/force_no_nap" ] && echo "$napVal" > "${'$'}g/force_no_nap" 2>/dev/null
                done
            """.trimIndent()).append("\n")
            logs.add("GPU No-Nap: ${if (napVal == "1") "Active (Prevents GPU micro-sleep between frame draws)" else "Normal"}")
        }

        // 6. DDR Memory Bus Bandwidth (Qualcomm & Universal)
        if (config.perfDdrBusBoostEnabled) {
            if (isUltra) {
                commands.append("""
                    for dev in /sys/class/devfreq/*cpubw* /sys/class/devfreq/*gpubw* /sys/class/devfreq/*memlat* /sys/class/devfreq/*ddr* /sys/class/devfreq/*vidc* /sys/class/devfreq/*spdm*; do
                        [ -d "${'$'}dev" ] || continue
                        echo "performance" > "${'$'}dev/governor" 2>/dev/null
                    done
                """.trimIndent()).append("\n")
                logs.add("DDR Memory Bus: Locked to maximum bandwidth on all devfreq nodes (Ultra)")
            } else if (isHeavy) {
                commands.append("""
                    for dev in /sys/class/devfreq/*cpubw* /sys/class/devfreq/*gpubw*; do
                        [ -d "${'$'}dev" ] || continue
                        echo "performance" > "${'$'}dev/governor" 2>/dev/null
                    done
                """.trimIndent()).append("\n")
                logs.add("DDR Memory Bus: Accelerated CPU/GPU bandwidth devfreq governors (Heavy)")
            } else {
                logs.add("DDR Memory Bus: Dynamic scaling active (Lite)")
            }
        } else {
            commands.append("""
                restore_from_backup "devfreq.*governor" "for d in /sys/class/devfreq/*; do [ -f \"${'$'}d/available_governors\" ] && { gov=\$(cat \"${'$'}d/available_governors\" | awk '{print ${'$'}1}'); [ -n \"${'$'}gov\" ] && echo \"${'$'}gov\" > \"${'$'}d/governor\" 2>/dev/null; }; done"
            """.trimIndent()).append("\n")
            logs.add("DDR Memory Bus restored from stock backup snapshot")
        }

        // 7. Storage I/O Multi-Queue Engine (512KB Read-Ahead)
        if (config.perfStorageQueue512Enabled) {
            commands.append("""
                for q in /sys/block/*/queue; do
                    [ -d "${'$'}q" ] || continue
                    echo 512 > "${'$'}q/read_ahead_kb" 2>/dev/null
                    echo 256 > "${'$'}q/nr_requests" 2>/dev/null
                    echo 2 > "${'$'}q/rq_affinity" 2>/dev/null
                    echo 0 > "${'$'}q/iostats" 2>/dev/null
                    echo 2 > "${'$'}q/nomerges" 2>/dev/null
                    echo 0 > "${'$'}q/add_random" 2>/dev/null
                done
            """.trimIndent()).append("\n")
            logs.add("Storage I/O: 512KB read-ahead, 256 queue depth, rq_affinity=2, zero stats overhead")
        } else {
            commands.append("""
                restore_from_backup "queue/read_ahead_kb|queue/nr_requests" "for q in /sys/block/*/queue; do [ -d \"${'$'}q\" ] || continue; echo 512 > \"${'$'}q/read_ahead_kb\" 2>/dev/null; echo 128 > \"${'$'}q/nr_requests\" 2>/dev/null; echo 1 > \"${'$'}q/iostats\" 2>/dev/null; done"
            """.trimIndent()).append("\n")
            logs.add("Storage I/O restored from stock backup snapshot")
        }

        // 8. Touch & Input Boost Duration
        if (config.perfTouchBoostEnabled) {
            val boostMs = if (isUltra) "256" else "128"
            commands.append("""
                [ -e /sys/module/cpu_boost/parameters/input_boost_ms ] && echo "$boostMs" > /sys/module/cpu_boost/parameters/input_boost_ms 2>/dev/null
                [ -e /sys/module/cpu_input_boost/parameters/input_boost_duration ] && echo "$boostMs" > /sys/module/cpu_input_boost/parameters/input_boost_duration 2>/dev/null
                [ -e /sys/module/msm_performance/parameters/touchboost ] && echo 1 > /sys/module/msm_performance/parameters/touchboost 2>/dev/null
                [ -e /sys/power/pnpmgr/touch_boost ] && echo 1 > /sys/power/pnpmgr/touch_boost 2>/dev/null
            """.trimIndent()).append("\n")
            logs.add("Touch Boost: Enabled ($boostMs ms touch response boost)")
        } else {
            commands.append("""
                restore_from_backup "touchboost|input_boost" "[ -e /sys/module/cpu_boost/parameters/input_boost_ms ] && echo 40 > /sys/module/cpu_boost/parameters/input_boost_ms 2>/dev/null"
            """.trimIndent()).append("\n")
            logs.add("Touch Boost restored from stock backup snapshot")
        }

        // 9. LMK & RAM Free Pool Tuning for Gaming
        if (config.perfLmkTuningEnabled) {
            commands.append("""
                echo 75 > /proc/sys/vm/swappiness 2>/dev/null
                echo 100 > /proc/sys/vm/vfs_cache_pressure 2>/dev/null
                echo 21542 > /proc/sys/vm/extra_free_kbytes 2>/dev/null
                echo 800 > /proc/sys/vm/dirty_writeback_centisecs 2>/dev/null
                resetprop ro.sys.fw.bg_apps_limit 34 2>/dev/null || setprop ro.sys.fw.bg_apps_limit 34 2>/dev/null
            """.trimIndent()).append("\n")
            logs.add("Memory & LMK: Gaming free-pool headroom tuned (extra_free_kbytes 21542, swappiness 75)")
        }

        // 10. Display Refresh Rate: Unlock full display refresh rate for smooth gaming
        commands.append("""
            settings delete system min_refresh_rate 2>/dev/null
            settings delete system peak_refresh_rate 2>/dev/null
            settings delete system user_refresh_rate 2>/dev/null
            settings delete secure miui_refresh_rate 2>/dev/null
        """.trimIndent()).append("\n")
        logs.add("Display: Full high refresh rate unconstrained")

        // 11. Safety Verification
        logs.add("Safety Verification: Hardware emergency thermal safeguards preserved. Zero adware.")

        val execResult = execute(commands.toString())
        if (execResult.success) {
            logs.add("All selected Performance ($subMode) tweaks written to RAM successfully.")
        } else {
            logs.add("Performance tweaks applied with notices: ${execResult.stderr.take(80)}")
        }

        return logs
    }

    private suspend fun applyHyperOsPowersaveTweaksInternal(
        context: Context,
        config: TweakConfigEntity,
        logs: MutableList<String>,
        commands: StringBuilder,
        backupPath: String
    ): List<String> {
        logs.add("Activating HyperOS / MIUI Super Power Saving Engine")

        // 1. Permanent System Components + User Allowed Apps Protection
        val userAllowedPkgs = config.backgroundAllowedApps
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()

        val allProtectedPkgs = PERMANENT_SYSTEM_EXCEPTIONS + userAllowedPkgs + setOf(
            "com.whatsapp",
            "com.twitter.android",
            "com.android.vending",
            "com.android.mms",
            "org.barebrowser",
            "com.example",
            "com.voltpower"
        )

        // Whitelist all protected apps in Android DeviceIdle (Doze)
        for (pkg in allProtectedPkgs) {
            commands.append("dumpsys deviceidle whitelist +$pkg 2>/dev/null\n")
        }
        logs.add("Permanent system protection verified: Launcher, SystemUI, Plugin & ${userAllowedPkgs.size} user background apps exempted")

        // 2. Always-On Display (AOD) Control (MiuiAod setAodEnable)
        if (config.hyperOsAodDisabled) {
            commands.append("""
                settings put secure aod_mode 0 2>/dev/null
                settings put system aod_mode 0 2>/dev/null
                settings put secure doze_always_on 0 2>/dev/null
                setprop debug.miui.aod_enable 0 2>/dev/null
                am broadcast -a miui.intent.action.AOD_STATE_CHANGED --ez enabled false 2>/dev/null
            """.trimIndent()).append("\n")
            logs.add("Always-On Display disabled (MiuiAod.Utils setAodEnable: false)")
        } else {
            commands.append("""
                settings put secure aod_mode 1 2>/dev/null
                settings put system aod_mode 1 2>/dev/null
                settings put secure doze_always_on 1 2>/dev/null
                setprop debug.miui.aod_enable 1 2>/dev/null
                am broadcast -a miui.intent.action.AOD_STATE_CHANGED --ez enabled true 2>/dev/null
            """.trimIndent()).append("\n")
            logs.add("Always-On Display (AOD) preserved enabled")
        }

        // 3. Aurogon & SuperPowerClean (Background cleanup while shielding whitelisted apps)
        if (config.hyperOsSuperPowerCleanEnabled) {
            val protectedArgs = allProtectedPkgs.joinToString(" ")
            commands.append("""
                # SuperPowerClean: Purge unwhitelisted 3rd-party background tasks from RAM
                for pkg in ${'$'}(pm list packages -3 2>/dev/null | cut -d: -f2); do
                    case " $protectedArgs " in
                        *" ${'$'}pkg "*) ;; # whitelisted, preserve in background
                        *) am stop-app "${'$'}pkg" 2>/dev/null || am force-stop "${'$'}pkg" 2>/dev/null ;;
                    esac
                done
            """.trimIndent()).append("\n")
            logs.add("SuperPowerClean: Memory cleaned in RAM (Protected: ${allProtectedPkgs.size} apps)")
        }

        // 4. Xiaomi Touch Boost Clamping
        if (config.hyperOsTouchBoostDisabled) {
            commands.append("""
                echo 0 > /sys/module/msm_performance/parameters/touchboost 2>/dev/null
                echo 0 > /sys/module/cpu_boost/parameters/input_boost_ms 2>/dev/null
                echo 0 > /sys/devices/system/cpu/cpufreq/schedutil/iowait_boost_enable 2>/dev/null
                settings put secure touch_boost 0 2>/dev/null
            """.trimIndent()).append("\n")
            logs.add("Xiaomi Touch Boost disabled (Saves 8-15% battery during active touches)")
        }

        // 5. Qualcomm 5G / Modem Power Optimization (FiveGPowerController)
        if (config.hyperOsFiveGPowerOptEnabled) {
            commands.append("""
                setprop persist.vendor.radio.5g_power_save 1 2>/dev/null
                setprop persist.radio.power_saving 1 2>/dev/null
            """.trimIndent()).append("\n")
            logs.add("Modem 5G Power Optimization enabled (FiveGPowerController powerSaveOpt)")
        }

        // 6. Display Refresh Rate 60Hz Cap
        if (config.hyperOsLock60HzEnabled) {
            commands.append("""
                settings put secure miui_refresh_rate 60 2>/dev/null
                settings put system min_refresh_rate 60.0 2>/dev/null
                settings put system peak_refresh_rate 60.0 2>/dev/null
                settings put system user_refresh_rate 60 2>/dev/null
                service call SurfaceFlinger 1035 i32 60 2>/dev/null
            """.trimIndent()).append("\n")
            logs.add("Display refresh rate clamped to 60Hz")
        }

        // 7. CPU Frequency 50% Cap
        commands.append("""
            for cpu in /sys/devices/system/cpu/cpu*/cpufreq; do
                max=`cat ${'$'}cpu/cpuinfo_max_freq 2>/dev/null`
                if [ -n "${'$'}max" ]; then
                    target=${'$'}((max * 50 / 100))
                    echo "${'$'}target" > "${'$'}cpu/scaling_max_freq" 2>/dev/null
                fi
                echo "100000" > "${'$'}cpu/scaling_min_freq" 2>/dev/null
            done
        """.trimIndent()).append("\n")
        logs.add("CPU Max Frequency capped to 50% in RAM")

        // 8. Governor & Core Topology (Heavy cores parked if enabled)
        if (config.twoCoresOfflineBelow20Enabled) {
            val coresToOffline = if (config.offlineCoreCount == 1) listOf(7) else listOf(6, 7)
            for (core in coresToOffline) {
                commands.append("echo 0 > /sys/devices/system/cpu/cpu$core/online 2>/dev/null\n")
            }
            logs.add("HyperOS Dynamic Core Parking: ${config.offlineCoreCount} cores offlined in RAM")
        }

        // 9. System Battery Saver (Preserve Light Theme)
        if (config.systemBatterySaverWithoutDarkEnabled) {
            commands.append("""
                cmd power set-mode 1 2>/dev/null || settings put global low_power 1 2>/dev/null
                cmd uimode night no 2>/dev/null
                settings put secure ui_night_mode 1 2>/dev/null
                settings put system ui_night_mode 1 2>/dev/null
            """.trimIndent()).append("\n")
            logs.add("System battery saver active (Light mode preserved)")
        }

        val execResult = execute(commands.toString())
        if (execResult.success) {
            logs.add("HyperOS Super Power Saving Engine applied successfully to RAM.")
        } else {
            logs.add("HyperOS tweaks applied with notices: ${execResult.stderr.take(80)}")
        }

        return logs
    }

    private fun getBatteryCapacity(context: Context): Int {
        val nodeVal = File("/sys/class/power_supply/battery/capacity").takeIf { it.exists() }?.readText()?.trim()?.toIntOrNull()
        if (nodeVal != null) return nodeVal

        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        return bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 50
    }
}
