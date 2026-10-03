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

        // 1. 50% CPU Max Frequency Cap
        if (config.cpuFreqCapEnabled) {
            commands.append("""
                for cpu in /sys/devices/system/cpu/cpu*/cpufreq; do
                    max=`cat ${'$'}cpu/cpuinfo_max_freq 2>/dev/null`
                    if [ -n "${'$'}max" ]; then
                        half=${'$'}((max / 2))
                        echo "${'$'}half" > "${'$'}cpu/scaling_max_freq" 2>/dev/null
                    fi
                    echo "100000" > "${'$'}cpu/scaling_min_freq" 2>/dev/null
                done
            """.trimIndent()).append("\n")
            logs.add("CPU Max Frequency capped to 50%")
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
            commands.append("""
                for s in /sys/devices/system/cpu/cpu*/cpufreq/schedutil /sys/devices/system/cpu/cpufreq/schedutil; do
                    [ -d "${'$'}s" ] || continue
                    echo 8000 > "${'$'}s/up_rate_limit_us" 2>/dev/null
                    echo 32000 > "${'$'}s/down_rate_limit_us" 2>/dev/null
                    echo 99 > "${'$'}s/hispeed_load" 2>/dev/null
                    echo 0 > "${'$'}s/iowait_boost_enable" 2>/dev/null
                done
            """.trimIndent()).append("\n")
            logs.add("Schedutil rate limits & 99% hispeed threshold applied")
        } else {
            commands.append("""
                restore_from_backup "schedutil/" "for s in /sys/devices/system/cpu/cpu*/cpufreq/schedutil; do [ -d \"${'$'}s\" ] || continue; echo 500 > \"${'$'}s/up_rate_limit_us\" 2>/dev/null; echo 20000 > \"${'$'}s/down_rate_limit_us\" 2>/dev/null; echo 80 > \"${'$'}s/hispeed_load\" 2>/dev/null; echo 1 > \"${'$'}s/iowait_boost_enable\" 2>/dev/null; done"
            """.trimIndent()).append("\n")
            logs.add("Schedutil rate limits restored from stock backup snapshot")
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
        if (config.gpuPowerLimitEnabled) {
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

        // 6. Storage & I/O Queue
        if (config.storageIoQueueEnabled) {
            commands.append("""
                for q in /sys/block/*/queue; do
                    [ -d "${'$'}q" ] || continue
                    echo 128 > "${'$'}q/read_ahead_kb" 2>/dev/null
                    echo 64 > "${'$'}q/nr_requests" 2>/dev/null
                    echo 0 > "${'$'}q/iostats" 2>/dev/null
                done
            """.trimIndent()).append("\n")
            logs.add("I/O queue tuned (128KB read-ahead, 64 queue depth)")
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

        // 8. Virtual Memory (Dirty writeback delay & ZRAM)
        if (config.vmDirtyWritebackEnabled) {
            commands.append("""
                echo 50 > /proc/sys/vm/dirty_ratio 2>/dev/null
                echo 5 > /proc/sys/vm/dirty_background_ratio 2>/dev/null
                echo 3000 > /proc/sys/vm/dirty_writeback_centisecs 2>/dev/null
                echo 3000 > /proc/sys/vm/dirty_expire_centisecs 2>/dev/null
                echo 100 > /proc/sys/vm/swappiness 2>/dev/null
                echo 50 > /proc/sys/vm/vfs_cache_pressure 2>/dev/null
            """.trimIndent()).append("\n")
            logs.add("VM writeback extended to 30s")
        } else {
            commands.append("""
                restore_from_backup "/proc/sys/vm/" "echo 20 > /proc/sys/vm/dirty_ratio 2>/dev/null; echo 10 > /proc/sys/vm/dirty_background_ratio 2>/dev/null; echo 500 > /proc/sys/vm/dirty_writeback_centisecs 2>/dev/null; echo 60 > /proc/sys/vm/swappiness 2>/dev/null; echo 100 > /proc/sys/vm/vfs_cache_pressure 2>/dev/null"
            """.trimIndent()).append("\n")
            logs.add("VM dirty writeback restored from stock backup snapshot")
        }

        // 9. TCP BBR & Fast Open
        if (config.tcpBbrCongestionEnabled) {
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

        val execResult = execute(commands.toString())
        if (execResult.success) {
            logs.add("All selected powersave tweaks successfully written to RAM.")
        } else {
            logs.add("Execution completed with notices: ${execResult.stderr.take(100)}")
        }

        logs
    }

    private fun getBatteryCapacity(context: Context): Int {
        val nodeVal = File("/sys/class/power_supply/battery/capacity").takeIf { it.exists() }?.readText()?.trim()?.toIntOrNull()
        if (nodeVal != null) return nodeVal

        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        return bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 50
    }
}
