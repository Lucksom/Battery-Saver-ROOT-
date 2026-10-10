package com.example.data.apps

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class InstalledAppItem(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean,
    val isPermanentlyProtected: Boolean,
    val iconBitmap: Bitmap? = null
)

class AppInfoRepository(private val context: Context) {

    // Hardcoded permanent system components that can NEVER be restricted or terminated
    companion object {
        val PERMANENT_SYSTEM_EXCEPTIONS = setOf(
            "com.mi.android.globallauncher",
            "com.miui.home",
            "com.android.systemui",
            "miui.systemui.plugin",
            "com.google.android.gms",
            "com.android.phone",
            "android"
        )
    }

    suspend fun getInstalledApps(): List<InstalledAppItem> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val packages = try {
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
        } catch (_: Exception) {
            emptyList<ApplicationInfo>()
        }

        val appList = mutableListOf<InstalledAppItem>()

        for (app in packages) {
            val pkg = app.packageName
            val isSystem = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val isPermanent = PERMANENT_SYSTEM_EXCEPTIONS.contains(pkg)
            val label = try {
                pm.getApplicationLabel(app).toString()
            } catch (_: Exception) {
                pkg
            }

            val iconBitmap = try {
                val drawable = pm.getApplicationIcon(app)
                drawableToBitmap(drawable)
            } catch (_: Exception) {
                null
            }

            appList.add(
                InstalledAppItem(
                    packageName = pkg,
                    appName = label,
                    isSystemApp = isSystem,
                    isPermanentlyProtected = isPermanent,
                    iconBitmap = iconBitmap
                )
            )
        }

        // Sort: user apps first alphabetically, then system apps
        appList.sortedWith(
            compareBy<InstalledAppItem> { it.isSystemApp }
                .thenBy { it.appName.lowercase() }
        )
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
        val bitmap = Bitmap.createBitmap(width.coerceIn(48, 192), height.coerceIn(48, 192), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}
