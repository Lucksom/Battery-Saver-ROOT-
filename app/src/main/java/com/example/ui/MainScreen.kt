package com.example.ui

import android.graphics.BlurMaskFilter
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.TweakConfigEntity
import com.example.data.log.LogEntry
import com.example.data.telemetry.DeviceTelemetry
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

// ============================================================================
// EXACT SVG PATHS FROM voltpower_m3_expressive_v2_2d2945d8.html (viewBox 0 0 200 200)
// ============================================================================

// Used in .logo svg.c (line 276), .hero .bg2 (line 304), and .orb svg.c (line 310)
private const val COOKIE_SVG_PATH_DATA =
    "M190.0 100.0 L189.8 102.4 L189.2 104.7 L188.4 107.0 L187.2 109.2 L185.8 111.3 L184.3 113.3 L182.7 115.3 L181.1 117.2 L179.7 119.1 L178.5 121.0 L177.4 122.9 L176.7 124.9 L176.1 127.0 L175.8 129.1 L175.7 131.4 L175.8 133.7 L175.9 136.2 L176.0 138.7 L176.0 141.3 L175.9 143.8 L175.6 146.3 L175.0 148.7 L174.0 150.9 L172.8 152.9 L171.3 154.7 L169.5 156.2 L167.4 157.6 L165.1 158.7 L162.8 159.6 L160.3 160.3 L157.9 161.0 L155.5 161.7 L153.2 162.3 L151.1 163.1 L149.2 164.1 L147.4 165.2 L145.8 166.6 L144.2 168.1 L142.8 169.9 L141.5 171.8 L140.1 173.9 L138.7 176.0 L137.2 178.1 L135.7 180.1 L133.9 181.9 L132.0 183.4 L130.0 184.7 L127.8 185.6 L125.5 186.1 L123.1 186.3 L120.7 186.2 L118.2 185.7 L115.8 185.1 L113.3 184.3 L111.0 183.4 L108.7 182.5 L106.4 181.7 L104.3 181.1 L102.1 180.7 L100.0 180.6 L97.9 180.7 L95.7 181.1 L93.6 181.7 L91.3 182.5 L89.0 183.4 L86.7 184.3 L84.2 185.1 L81.8 185.7 L79.3 186.2 L76.9 186.3 L74.5 186.1 L72.2 185.6 L70.0 184.7 L68.0 183.4 L66.1 181.9 L64.3 180.1 L62.8 178.1 L61.3 176.0 L59.9 173.9 L58.5 171.8 L57.2 169.9 L55.8 168.1 L54.2 166.6 L52.6 165.2 L50.8 164.1 L48.9 163.1 L46.8 162.3 L44.5 161.7 L42.1 161.0 L39.7 160.3 L37.2 159.6 L34.9 158.7 L32.6 157.6 L30.5 156.2 L28.7 154.7 L27.2 152.9 L26.0 150.9 L25.0 148.7 L24.4 146.3 L24.1 143.8 L24.0 141.3 L24.0 138.7 L24.1 136.2 L24.2 133.7 L24.3 131.4 L24.2 129.1 L23.9 127.0 L23.3 124.9 L22.6 122.9 L21.5 121.0 L20.3 119.1 L18.9 117.2 L17.3 115.3 L15.7 113.3 L14.2 111.3 L12.8 109.2 L11.6 107.0 L10.8 104.7 L10.2 102.4 L10.0 100.0 L10.2 97.6 L10.8 95.3 L11.6 93.0 L12.8 90.8 L14.2 88.7 L15.7 86.7 L17.3 84.7 L18.9 82.8 L20.3 80.9 L21.5 79.0 L22.6 77.1 L23.3 75.1 L23.9 73.0 L24.2 70.9 L24.3 68.6 L24.2 66.3 L24.1 63.8 L24.0 61.3 L24.0 58.7 L24.1 56.2 L24.4 53.7 L25.0 51.3 L26.0 49.1 L27.2 47.1 L28.7 45.3 L30.5 43.8 L32.6 42.4 L34.9 41.3 L37.2 40.4 L39.7 39.7 L42.1 39.0 L44.5 38.3 L46.8 37.7 L48.9 36.9 L50.8 35.9 L52.6 34.8 L54.2 33.4 L55.8 31.9 L57.2 30.1 L58.5 28.2 L59.9 26.1 L61.3 24.0 L62.8 21.9 L64.3 19.9 L66.1 18.1 L68.0 16.6 L70.0 15.3 L72.2 14.4 L74.5 13.9 L76.9 13.7 L79.3 13.8 L81.8 14.3 L84.2 14.9 L86.7 15.7 L89.0 16.6 L91.3 17.5 L93.6 18.3 L95.7 18.9 L97.9 19.3 L100.0 19.4 L102.1 19.3 L104.3 18.9 L106.4 18.3 L108.7 17.5 L111.0 16.6 L113.3 15.7 L115.8 14.9 L118.2 14.3 L120.7 13.8 L123.1 13.7 L125.5 13.9 L127.8 14.4 L130.0 15.3 L132.0 16.6 L133.9 18.1 L135.7 19.9 L137.2 21.9 L138.7 24.0 L140.1 26.1 L141.5 28.2 L142.8 30.1 L144.2 31.9 L145.8 33.4 L147.4 34.8 L149.2 35.9 L151.1 36.9 L153.2 37.7 L155.5 38.3 L157.9 39.0 L160.3 39.7 L162.8 40.4 L165.1 41.3 L167.4 42.4 L169.5 43.8 L171.3 45.3 L172.8 47.1 L174.0 49.1 L175.0 51.3 L175.6 53.7 L175.9 56.2 L176.0 58.7 L176.0 61.3 L175.9 63.8 L175.8 66.3 L175.7 68.6 L175.8 70.9 L176.1 73.0 L176.7 75.1 L177.4 77.1 L178.5 79.0 L179.7 80.9 L181.1 82.8 L182.7 84.7 L184.3 86.7 L185.8 88.7 L187.2 90.8 L188.4 93.0 L189.2 95.3 L189.8 97.6Z"

// Used in .hero .bg1 (line 303)
private const val BG1_SVG_PATH_DATA =
    "M190.0 100.0 L189.8 102.4 L189.4 104.7 L188.6 107.0 L187.6 109.2 L186.3 111.4 L184.9 113.4 L183.3 115.4 L181.7 117.4 L180.0 119.2 L178.4 121.0 L176.9 122.8 L175.5 124.5 L174.2 126.3 L173.1 128.1 L172.3 129.9 L171.6 131.9 L171.1 133.9 L170.7 136.0 L170.5 138.3 L170.3 140.6 L170.2 143.0 L170.0 145.5 L169.8 148.0 L169.5 150.5 L169.1 153.0 L168.4 155.4 L167.6 157.7 L166.5 159.9 L165.2 161.9 L163.6 163.6 L161.9 165.2 L159.9 166.5 L157.7 167.6 L155.4 168.4 L153.0 169.1 L150.5 169.5 L148.0 169.8 L145.5 170.0 L143.0 170.2 L140.6 170.3 L138.3 170.5 L136.0 170.7 L133.9 171.1 L131.9 171.6 L129.9 172.3 L128.1 173.1 L126.3 174.2 L124.5 175.5 L122.8 176.9 L121.0 178.4 L119.2 180.0 L117.4 181.7 L115.4 183.3 L113.4 184.9 L111.4 186.3 L109.2 187.6 L107.0 188.6 L104.7 189.4 L102.4 189.8 L100.0 190.0 L97.6 189.8 L95.3 189.4 L93.0 188.6 L90.8 187.6 L88.6 186.3 L86.6 184.9 L84.6 183.3 L82.6 181.7 L80.8 180.0 L79.0 178.4 L77.2 176.9 L75.5 175.5 L73.7 174.2 L71.9 173.1 L70.1 172.3 L68.1 171.6 L66.1 171.1 L64.0 170.7 L61.7 170.5 L59.4 170.3 L57.0 170.2 L54.5 170.0 L52.0 169.8 L49.5 169.5 L47.0 169.1 L44.6 168.4 L42.3 167.6 L40.1 166.5 L38.1 165.2 L36.4 163.6 L34.8 161.9 L33.5 159.9 L32.4 157.7 L31.6 155.4 L30.9 153.0 L30.5 150.5 L30.2 148.0 L30.0 145.5 L29.8 143.0 L29.7 140.6 L29.5 138.3 L29.3 136.0 L28.9 133.9 L28.4 131.9 L27.7 129.9 L26.9 128.1 L25.8 126.3 L24.5 124.5 L23.1 122.8 L21.6 121.0 L20.0 119.2 L18.3 117.4 L16.7 115.4 L15.1 113.4 L13.7 111.4 L12.4 109.2 L11.4 107.0 L10.6 104.7 L10.2 102.4 L10.0 100.0 L10.2 97.6 L10.6 95.3 L11.4 93.0 L12.4 90.8 L13.7 88.6 L15.1 86.6 L16.7 84.6 L18.3 82.6 L20.0 80.8 L21.6 79.0 L23.1 77.2 L24.5 75.5 L25.8 73.7 L26.9 71.9 L27.7 70.1 L28.4 68.1 L28.9 66.1 L29.3 64.0 L29.5 61.7 L29.7 59.4 L29.8 57.0 L30.0 54.5 L30.2 52.0 L30.5 49.5 L30.9 47.0 L31.6 44.6 L32.4 42.3 L33.5 40.1 L34.8 38.1 L36.4 36.4 L38.1 34.8 L40.1 33.5 L42.3 32.4 L44.6 31.6 L47.0 30.9 L49.5 30.5 L52.0 30.2 L54.5 30.0 L57.0 29.8 L59.4 29.7 L61.7 29.5 L64.0 29.3 L66.1 28.9 L68.1 28.4 L70.1 27.7 L71.9 26.9 L73.7 25.8 L75.5 24.5 L77.2 23.1 L79.0 21.6 L80.8 20.0 L82.6 18.3 L84.6 16.7 L86.6 15.1 L88.6 13.7 L90.8 12.4 L93.0 11.4 L95.3 10.6 L97.6 10.2 L100.0 10.0 L102.4 10.2 L104.7 10.6 L107.0 11.4 L109.2 12.4 L111.4 13.7 L113.4 15.1 L115.4 16.7 L117.4 18.3 L119.2 20.0 L121.0 21.6 L122.8 23.1 L124.5 24.5 L126.3 25.8 L128.1 26.9 L129.9 27.7 L131.9 28.4 L133.9 28.9 L136.0 29.3 L138.3 29.5 L140.6 29.7 L143.0 29.8 L145.5 30.0 L148.0 30.2 L150.5 30.5 L153.0 30.9 L155.4 31.6 L157.7 32.4 L159.9 33.5 L161.9 34.8 L163.6 36.4 L165.2 38.1 L166.5 40.1 L167.6 42.3 L168.4 44.6 L169.1 47.0 L169.5 49.5 L169.8 52.0 L170.0 54.5 L170.2 57.0 L170.3 59.4 L170.5 61.7 L170.7 64.0 L171.1 66.1 L171.6 68.1 L172.3 70.1 L173.1 71.9 L174.2 73.7 L175.5 75.5 L176.9 77.2 L178.4 79.0 L180.0 80.8 L181.7 82.6 L183.3 84.6 L184.9 86.6 L186.3 88.6 L187.6 90.8 L188.6 93.0 L189.4 95.3 L189.8 97.6Z"

private val BaseCookiePath: Path by lazy {
    PathParser().parsePathString(COOKIE_SVG_PATH_DATA).toPath()
}

private val BaseBg1Path: Path by lazy {
    PathParser().parsePathString(BG1_SVG_PATH_DATA).toPath()
}

private fun createScaledPath(basePath: Path, widthPx: Float, heightPx: Float): Path {
    val scaled = Path()
    scaled.addPath(basePath)
    val matrix = Matrix()
    matrix.scale(widthPx / 200f, heightPx / 200f)
    scaled.transform(matrix)
    return scaled
}

// ============================================================================
// EXACT OKLCH -> sRGB MONET ENGINE (1:1 with HTML CSS lines 12-33 & 82-87)
// ============================================================================

private fun oklch(l: Float, c: Float, hDeg: Float): Color {
    val hRad = (hDeg * PI / 180.0).toFloat()
    val a = c * cos(hRad)
    val b = c * sin(hRad)

    val lPrime = l + 0.39633778f * a + 0.21580376f * b
    val mPrime = l - 0.10556135f * a - 0.06385417f * b
    val sPrime = l - 0.08948418f * a - 1.2914855f * b

    val lCubed = lPrime * lPrime * lPrime
    val mCubed = mPrime * mPrime * mPrime
    val sCubed = sPrime * sPrime * sPrime

    val rLin = +4.0767417f * lCubed - 3.3077116f * mCubed + 0.23096993f * sCubed
    val gLin = -1.268438f * lCubed + 2.6097574f * mCubed - 0.3413194f * sCubed
    val bLin = -0.0041960863f * lCubed - 0.7034186f * mCubed + 1.7076147f * sCubed

    fun gamma(x: Float): Float {
        val clamped = x.coerceIn(0f, 1f)
        return if (clamped <= 0.0031308f) {
            12.92f * clamped
        } else {
            1.055f * clamped.pow(1f / 2.4f) - 0.055f
        }
    }

    return Color(
        red = gamma(rLin).coerceIn(0f, 1f),
        green = gamma(gLin).coerceIn(0f, 1f),
        blue = gamma(bLin).coerceIn(0f, 1f),
        alpha = 1f
    )
}

private data class MonetRolePalette(
    val primary: Color,
    val onPrimary: Color,
    val pc: Color,
    val onPc: Color,
    val sec: Color,
    val sc: Color,
    val onSc: Color,
    val tert: Color,
    val onTert: Color,
    val tc: Color,
    val onTc: Color,
    val bg: Color,
    val s1: Color,
    val s2: Color,
    val s3: Color,
    val s4: Color,
    val onS: Color,
    val onSv: Color,
    val outline: Color,
    val err: Color
)

private val LocalMonetRoles = staticCompositionLocalOf {
    buildMonetRolesFromHue(262f, isDark = false)
}

private fun buildMonetRolesFromHue(h: Float, isDark: Boolean): MonetRolePalette {
    val th = (h + 65f) % 360f
    return if (!isDark) {
        MonetRolePalette(
            primary = oklch(0.50f, 0.17f, h),
            onPrimary = oklch(0.99f, 0.01f, h),
            pc = oklch(0.90f, 0.07f, h),
            onPc = oklch(0.26f, 0.10f, h),
            sec = oklch(0.50f, 0.06f, h),
            sc = oklch(0.91f, 0.04f, h),
            onSc = oklch(0.27f, 0.05f, h),
            tert = oklch(0.52f, 0.12f, th),
            onTert = oklch(0.99f, 0.01f, h),
            tc = oklch(0.91f, 0.06f, th),
            onTc = oklch(0.27f, 0.08f, th),
            bg = oklch(0.983f, 0.009f, h),
            s1 = oklch(0.968f, 0.013f, h),
            s2 = oklch(0.950f, 0.016f, h),
            s3 = oklch(0.930f, 0.019f, h),
            s4 = oklch(0.910f, 0.022f, h),
            onS = oklch(0.21f, 0.02f, h),
            onSv = oklch(0.45f, 0.028f, h),
            outline = oklch(0.60f, 0.02f, h),
            err = oklch(0.52f, 0.19f, 27f)
        )
    } else {
        MonetRolePalette(
            primary = oklch(0.82f, 0.12f, h),
            onPrimary = oklch(0.29f, 0.09f, h),
            pc = oklch(0.38f, 0.11f, h),
            onPc = oklch(0.92f, 0.05f, h),
            sec = oklch(0.80f, 0.05f, h),
            sc = oklch(0.36f, 0.05f, h),
            onSc = oklch(0.91f, 0.035f, h),
            tert = oklch(0.82f, 0.10f, th),
            onTert = oklch(0.29f, 0.07f, th),
            tc = oklch(0.38f, 0.08f, th),
            onTc = oklch(0.92f, 0.05f, th),
            bg = oklch(0.16f, 0.013f, h),
            s1 = oklch(0.20f, 0.015f, h),
            s2 = oklch(0.235f, 0.017f, h),
            s3 = oklch(0.275f, 0.019f, h),
            s4 = oklch(0.315f, 0.021f, h),
            onS = oklch(0.93f, 0.01f, h),
            onSv = oklch(0.79f, 0.022f, h),
            outline = oklch(0.60f, 0.02f, h),
            err = oklch(0.78f, 0.13f, 25f)
        )
    }
}

private fun MonetRolePalette.toMaterialColorScheme(isDark: Boolean): ColorScheme {
    return if (isDark) {
        darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = pc,
            onPrimaryContainer = onPc,
            secondary = sec,
            onSecondary = onPrimary,
            secondaryContainer = sc,
            onSecondaryContainer = onSc,
            tertiary = tert,
            onTertiary = onTert,
            tertiaryContainer = tc,
            onTertiaryContainer = onTc,
            background = bg,
            onBackground = onS,
            surface = s1,
            onSurface = onS,
            surfaceVariant = s3,
            onSurfaceVariant = onSv,
            surfaceContainerLowest = bg,
            surfaceContainerLow = s1,
            surfaceContainer = s2,
            surfaceContainerHigh = s3,
            surfaceContainerHighest = s4,
            outline = outline,
            error = err
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = pc,
            onPrimaryContainer = onPc,
            secondary = sec,
            onSecondary = onPrimary,
            secondaryContainer = sc,
            onSecondaryContainer = onSc,
            tertiary = tert,
            onTertiary = onTert,
            tertiaryContainer = tc,
            onTertiaryContainer = onTc,
            background = bg,
            onBackground = onS,
            surface = s1,
            onSurface = onS,
            surfaceVariant = s3,
            onSurfaceVariant = onSv,
            surfaceContainerLowest = bg,
            surfaceContainerLow = s1,
            surfaceContainer = s2,
            surfaceContainerHigh = s3,
            surfaceContainerHighest = s4,
            outline = outline,
            error = err
        )
    }
}

private enum class ExpressiveTab(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    OVERVIEW("Overview", Icons.Outlined.Dashboard, Icons.Outlined.Dashboard),
    TWEAKS("Tweaks", Icons.Outlined.Tune, Icons.Filled.Tune),
    CONSOLE("Console", Icons.Outlined.Terminal, Icons.Filled.Terminal)
}

private enum class MonetSeedPreset(
    val id: String,
    val label: String,
    val hue: Float,
    val sliceA: Color,
    val sliceB: Color,
    val sliceC: Color
) {
    WALLPAPER("WALLPAPER", "Wallpaper", 262f, oklch(0.70f, 0.15f, 262f), oklch(0.52f, 0.06f, 262f), oklch(0.84f, 0.09f, 327f)),
    MINT("MINT", "Mint", 158f, oklch(0.76f, 0.17f, 158f), oklch(0.54f, 0.06f, 158f), oklch(0.84f, 0.09f, 223f)),
    IRIS("IRIS", "Iris", 300f, oklch(0.66f, 0.18f, 300f), oklch(0.50f, 0.06f, 300f), oklch(0.84f, 0.09f, 5f)),
    OCEAN("OCEAN", "Ocean", 225f, oklch(0.70f, 0.14f, 225f), oklch(0.52f, 0.05f, 225f), oklch(0.84f, 0.09f, 290f)),
    CITRUS("CITRUS", "Citrus", 80f, oklch(0.82f, 0.16f, 80f), oklch(0.58f, 0.06f, 80f), oklch(0.84f, 0.10f, 145f)),
    ROSE("ROSE", "Rose", 8f, oklch(0.72f, 0.17f, 8f), oklch(0.55f, 0.06f, 8f), oklch(0.85f, 0.09f, 73f))
}

private enum class TweakCategoryFilter(val id: String, val label: String) {
    ALL("ALL", "All"),
    CPU("CPU", "CPU & GPU"),
    BAT("BAT", "Battery"),
    OS("OS", "HyperOS"),
    IO("IO", "Display & I/O")
}

private data class TweakItemSpec(
    val key: String,
    val category: TweakCategoryFilter,
    val title: String,
    val subtitle: String,
    val sysfsCode: String,
    val icon: ImageVector,
    val isChecked: (TweakConfigEntity) -> Boolean
)

@Composable
private fun MonetExpressiveThemeOverlay(
    themeMode: String,
    seedPreset: MonetSeedPreset,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isDark = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }

    val roles = remember(isDark, seedPreset) {
        buildMonetRolesFromHue(seedPreset.hue, isDark)
    }

    val colorScheme = remember(isDark, seedPreset, roles) {
        if (seedPreset == MonetSeedPreset.WALLPAPER && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Merge system dynamic wallpaper scheme if available, while keeping exact Monet v2 roles
            val dyn = if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            roles.toMaterialColorScheme(isDark).copy(
                primary = roles.primary,
                onPrimary = roles.onPrimary,
                primaryContainer = roles.pc,
                onPrimaryContainer = roles.onPc,
                secondaryContainer = roles.sc,
                onSecondaryContainer = roles.onSc,
                tertiary = roles.tert,
                onTertiary = roles.onTert,
                tertiaryContainer = roles.tc,
                onTertiaryContainer = roles.onTc,
                background = roles.bg,
                surface = roles.s1,
                surfaceContainerLow = roles.s1,
                surfaceContainer = roles.s2,
                surfaceContainerHigh = roles.s3,
                surfaceContainerHighest = roles.s4,
                onSurface = roles.onS,
                onSurfaceVariant = roles.onSv,
                outline = dyn.outline
            )
        } else {
            roles.toMaterialColorScheme(isDark)
        }
    }

    CompositionLocalProvider(LocalMonetRoles provides roles) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MaterialTheme.typography,
            content = content
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val config by viewModel.configState.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()
    val isBusy by viewModel.isBusy.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val logs by viewModel.logs.collectAsState()

    var currentTab by rememberSaveable { mutableStateOf(ExpressiveTab.OVERVIEW) }
    var selectedSeed by rememberSaveable { mutableStateOf(MonetSeedPreset.MINT) }
    var showMonetPicker by rememberSaveable { mutableStateOf(false) }

    // Matches HTML <input type="checkbox" id="eng" checked>:
    // Starts true so the Hero card, rotating scalloped cookie orb, .bg1/.bg2, and .wave
    // are immediately vibrant and animating on launch just like the HTML & screenshot.
    var heroVisualActive by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(config.isMasterApplied) {
        if (config.isMasterApplied) {
            heroVisualActive = true
        }
    }

    if (currentTab != ExpressiveTab.OVERVIEW) {
        BackHandler {
            currentTab = ExpressiveTab.OVERVIEW
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            if (it != "Ready") {
                snackbarHostState.showSnackbar(it)
            }
        }
    }

    MonetExpressiveThemeOverlay(
        themeMode = config.themeMode,
        seedPreset = selectedSeed
    ) {
        val roles = LocalMonetRoles.current
        val primaryGlow = roles.primary.copy(alpha = 0.28f)
        val tertiaryGlow = roles.tert.copy(alpha = 0.22f)

        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = roles.bg,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.padding(bottom = 96.dp)
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(roles.bg)
                    .padding(innerPadding)
            ) {
                // Ambient Mesh Glow (.app::before from HTML lines 45-47)
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(520.dp)
                ) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(primaryGlow, Color.Transparent),
                            center = Offset(size.width * 0.9f, 0f),
                            radius = size.width * 0.65f
                        ),
                        center = Offset(size.width * 0.9f, 0f),
                        radius = size.width * 0.65f
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(tertiaryGlow, Color.Transparent),
                            center = Offset(0f, size.height * 0.12f),
                            radius = size.width * 0.6f
                        ),
                        center = Offset(0f, size.height * 0.12f),
                        radius = size.width * 0.6f
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 480.dp)
                        .align(Alignment.TopCenter)
                ) {
                    // Expressive Header + Monet Engine Sheet
                    ExpressiveMonetHeader(
                        isRootGranted = telemetry.isRootGranted,
                        themeMode = config.themeMode,
                        selectedSeed = selectedSeed,
                        showMonetPicker = showMonetPicker,
                        onToggleMonetPicker = { showMonetPicker = !showMonetPicker },
                        onSelectSeed = { selectedSeed = it },
                        onSelectThemeMode = { viewModel.onToggleTheme(it) },
                        onCycleTheme = {
                            val next = when (config.themeMode) {
                                "SYSTEM" -> "LIGHT"
                                "LIGHT" -> "DARK"
                                else -> "SYSTEM"
                            }
                            viewModel.onToggleTheme(next)
                        }
                    )

                    // Active Tab Page
                    Box(modifier = Modifier.weight(1f)) {
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = {
                                fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) togetherWith
                                    fadeOut(spring(stiffness = Spring.StiffnessMedium))
                            },
                            label = "ExpressivePageTransition"
                        ) { targetTab ->
                            when (targetTab) {
                                ExpressiveTab.OVERVIEW -> {
                                    ExpressiveOverviewPage(
                                        telemetry = telemetry,
                                        config = config,
                                        heroVisualActive = heroVisualActive,
                                        recentLogs = logs,
                                        onToggleMaster = {
                                            val nextState = !heroVisualActive
                                            heroVisualActive = nextState
                                            viewModel.onToggleMaster(nextState)
                                        },
                                        onSelectProfile = { viewModel.onSelectProfile(it) },
                                        onSelectPerformanceSubMode = { viewModel.onSelectPerformanceSubMode(it) },
                                        onOpenConsole = { currentTab = ExpressiveTab.CONSOLE }
                                    )
                                }

                                ExpressiveTab.TWEAKS -> {
                                    ExpressiveTweaksPage(
                                        config = config,
                                        onUpdateTweak = { key, enabled -> viewModel.onUpdateTweak(key, enabled) },
                                        onToggleHyperOs = { viewModel.onToggleHyperOsPowerSaver(it) },
                                        onSelectRefreshRate = { viewModel.onSelectRefreshRate(it) },
                                        onUpdateCoreTopology = { cores, threshold ->
                                            viewModel.onUpdateCoreTopology(cores, threshold)
                                        }
                                    )
                                }

                                ExpressiveTab.CONSOLE -> {
                                    ExpressiveConsolePage(
                                        logs = logs,
                                        backupFilePath = config.backupFilePath,
                                        onClearLogs = { viewModel.onClearLogs() }
                                    )
                                }
                            }
                        }
                    }
                }

                // Floating "Revert stock" FAB on Tweaks Tab (.fab from HTML lines 224-226)
                AnimatedVisibility(
                    visible = currentTab == ExpressiveTab.TWEAKS,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(end = 20.dp, bottom = 104.dp)
                ) {
                    Surface(
                        onClick = { viewModel.onRevertToStock() },
                        shape = RoundedCornerShape(22.dp),
                        color = roles.tc,
                        contentColor = roles.onTc,
                        shadowElevation = 10.dp,
                        modifier = Modifier.height(58.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 18.dp, end = 22.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = "Revert stock",
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Revert stock",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Floating Glassmorphic Pill Bottom Navigation (nav .in from HTML lines 227-234)
                ExpressiveFloatingBottomNav(
                    currentTab = currentTab,
                    logCount = if (logs.isEmpty()) 4 else logs.size,
                    onTabSelected = { currentTab = it },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                )

                // Busy Overlay
                if (isBusy) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(30.dp),
                            color = roles.s3,
                            tonalElevation = 8.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(26.dp),
                                    strokeWidth = 3.dp,
                                    color = roles.primary
                                )
                                Column {
                                    Text(
                                        text = "Writing sysfs nodes…",
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = roles.onS
                                    )
                                    Text(
                                        text = "100% volatile RAM transaction",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.sp,
                                        color = roles.onSv
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// EXACT SVG SCALLOPED COOKIE COMPOSABLES (Matches HTML svg.c, .bg1, .bg2, .orb)
// ============================================================================

@Composable
private fun ExactHtmlCookieShape(
    color: Color,
    rotationDegrees: Float,
    modifier: Modifier = Modifier,
    useBg1Path: Boolean = false,
    dropShadowColor: Color? = null
) {
    Canvas(modifier = modifier) {
        val basePath = if (useBg1Path) BaseBg1Path else BaseCookiePath
        val scaledPath = createScaledPath(basePath, size.width, size.height)

        // Optional CSS filter: drop-shadow(0 14px 22px color-mix(in oklab, var(--primary) 38%, transparent))
        // Note: In CSS, filter: drop-shadow(0 14px 22px ...) is applied on the rotating element's screen Y (+14px downwards)
        if (dropShadowColor != null && dropShadowColor.alpha > 0.01f) {
            val shadowOffsetY = 14.dp.toPx()
            val shadowBlur = 22.dp.toPx()
            drawIntoCanvas { canvas ->
                val frameworkPaint = android.graphics.Paint().apply {
                    isAntiAlias = true
                    style = android.graphics.Paint.Style.FILL
                    this.color = dropShadowColor.toArgb()
                    maskFilter = BlurMaskFilter(shadowBlur, BlurMaskFilter.Blur.NORMAL)
                }
                val native = canvas.nativeCanvas
                native.save()
                native.translate(0f, shadowOffsetY)
                native.rotate(rotationDegrees, size.width / 2f, size.height / 2f)
                native.drawPath(scaledPath.asAndroidPath(), frameworkPaint)
                native.restore()
            }
        }

        rotate(degrees = rotationDegrees, pivot = Offset(size.width / 2f, size.height / 2f)) {
            drawPath(path = scaledPath, color = color)
        }
    }
}

// ============================================================================
// HEADER + MONET ENGINE SHEET (Matches HTML <header> & .pal lines 56-95, 274-298)
// ============================================================================

@Composable
private fun ExpressiveMonetHeader(
    isRootGranted: Boolean,
    themeMode: String,
    selectedSeed: MonetSeedPreset,
    showMonetPicker: Boolean,
    onToggleMonetPicker: () -> Unit,
    onSelectSeed: (MonetSeedPreset) -> Unit,
    onSelectThemeMode: (String) -> Unit,
    onCycleTheme: () -> Unit
) {
    val roles = LocalMonetRoles.current
    val infiniteTransition = rememberInfiniteTransition(label = "HeaderAnimations")
    val logoRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "LogoSpin24s"
    )
    // CSS @keyframes pulse { 0% { box-shadow: 0 0 0 0 primary 50%; opacity: 1 } 50% { box-shadow: 0 0 0 6px transparent; opacity: .5 } }
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "DotPulse2s"
    )

    val palButtonCorner by animateDpAsState(
        targetValue = if (showMonetPicker) 16.dp else 23.dp,
        animationSpec = spring(dampingRatio = 0.58f, stiffness = Spring.StiffnessMedium),
        label = "PalCorner"
    )
    val palButtonRotation by animateFloatAsState(
        targetValue = if (showMonetPicker) 90f else 0f,
        animationSpec = spring(dampingRatio = 0.58f, stiffness = Spring.StiffnessMedium),
        label = "PalRotation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Rotating cookie logo (.logo 48x48px + bolt 22x22px)
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                ExactHtmlCookieShape(
                    color = roles.primary,
                    rotationDegrees = logoRotation,
                    modifier = Modifier.fillMaxSize()
                )
                Icon(
                    imageVector = Icons.Filled.Bolt,
                    contentDescription = null,
                    tint = roles.onPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Brand + pulsing root status (.brand & .sub)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "VoltPower",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    letterSpacing = (-1.1).sp,
                    color = roles.onS
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 3.dp)
                ) {
                    Canvas(modifier = Modifier.size(14.dp)) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val dotRadius = 3.5.dp.toPx()
                        val ringPhase = (pulseProgress * 2f).coerceAtMost(1f)
                        val ringRadius = dotRadius + (6.dp.toPx() * ringPhase)
                        val ringAlpha = 0.5f * (1f - ringPhase)
                        if (ringAlpha > 0.01f) {
                            drawCircle(
                                color = roles.primary.copy(alpha = ringAlpha),
                                radius = ringRadius,
                                center = center
                            )
                        }
                        val dotAlpha = if (pulseProgress <= 0.5f) {
                            1f - pulseProgress
                        } else {
                            pulseProgress
                        }
                        drawCircle(
                            color = roles.primary.copy(alpha = dotAlpha.coerceIn(0.5f, 1f)),
                            radius = dotRadius,
                            center = center
                        )
                    }
                    Text(
                        text = "root · volatile RAM",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.5.sp,
                        color = roles.onSv
                    )
                }
            }

            // Palette button (.ib morphing to 16.dp square & rotating 90deg when active)
            Surface(
                onClick = onToggleMonetPicker,
                shape = RoundedCornerShape(palButtonCorner),
                color = if (showMonetPicker) roles.primary else roles.s3.copy(alpha = 0.85f),
                contentColor = if (showMonetPicker) roles.onPrimary else roles.onSv,
                modifier = Modifier
                    .size(46.dp)
                    .rotate(palButtonRotation)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Palette,
                        contentDescription = "Monet palette",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Theme cycle button (.cyc)
            Surface(
                onClick = onCycleTheme,
                shape = CircleShape,
                color = roles.s3.copy(alpha = 0.85f),
                contentColor = roles.onSv,
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when (themeMode) {
                            "LIGHT" -> Icons.Filled.LightMode
                            "DARK" -> Icons.Filled.DarkMode
                            else -> Icons.Filled.Contrast
                        },
                        contentDescription = "Cycle theme",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Expandable Monet Engine Sheet (.pal)
        AnimatedVisibility(
            visible = showMonetPicker,
            enter = expandVertically(spring(dampingRatio = 0.65f)) + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = roles.s2.copy(alpha = 0.94f),
                shadowElevation = 14.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Monet engine",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = roles.onS
                        )
                        Text(
                            text = "OKLCH · 1 HUE → 20 ROLES",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.5.sp,
                            letterSpacing = 1.sp,
                            color = roles.onSv
                        )
                    }

                    // 6 Conic-style Seed Swatches (.seeds)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MonetSeedPreset.entries.forEach { preset ->
                            val isSelected = preset == selectedSeed
                            val cornerPercent by animateFloatAsState(
                                targetValue = if (isSelected) 32f else 50f,
                                animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
                                label = "SeedCorner"
                            )
                            val rotation by animateFloatAsState(
                                targetValue = if (isSelected) 45f else 0f,
                                animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
                                label = "SeedRotation"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .rotate(rotation)
                                    .clip(RoundedCornerShape(percent = cornerPercent.roundToInt()))
                                    .clickable { onSelectSeed(preset) },
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    // Matches CSS: conic-gradient(from 45deg, var(--a) 0 50%, var(--b) 0 75%, var(--c) 0)
                                    // In CSS 0deg = 12 o'clock (-90° in Canvas), so 45deg CSS = -45° in Canvas
                                    drawArc(
                                        color = preset.sliceA,
                                        startAngle = -45f,
                                        sweepAngle = 180f,
                                        useCenter = true
                                    )
                                    drawArc(
                                        color = preset.sliceB,
                                        startAngle = 135f,
                                        sweepAngle = 90f,
                                        useCenter = true
                                    )
                                    drawArc(
                                        color = preset.sliceC,
                                        startAngle = 225f,
                                        sweepAngle = 90f,
                                        useCenter = true
                                    )
                                }
                                val checkScale by animateFloatAsState(
                                    targetValue = if (isSelected) 1f else 0f,
                                    animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium),
                                    label = "SeedCheckScale"
                                )
                                if (checkScale > 0.01f) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = preset.label,
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .scale(checkScale)
                                            .rotate(-rotation)
                                    )
                                }
                            }
                        }
                    }

                    // 7-Block Tonal Strip (.tones)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        val tones = listOf(
                            roles.primary,
                            roles.pc,
                            roles.sc,
                            roles.tert,
                            roles.tc,
                            roles.s4,
                            roles.s2
                        )
                        tones.forEach { toneColor ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(toneColor)
                            )
                        }
                    }

                    // 3-Mode Pill Selector (.modes: System / Light / Dark)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clip(CircleShape)
                            .background(roles.s4)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val modes = listOf("SYSTEM" to "System", "LIGHT" to "Light", "DARK" to "Dark")
                        modes.forEach { (modeKey, modeLabel) ->
                            val selected = themeMode == modeKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .background(if (selected) roles.primary else Color.Transparent)
                                    .clickable { onSelectThemeMode(modeKey) }
                                    .padding(vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = modeLabel,
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (selected) roles.onPrimary else roles.onSv
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// TAB 1: OVERVIEW PAGE (Matches HTML #p1: Hero + Profiles + Bento + Timeline)
// ============================================================================

@Composable
private fun ExpressiveOverviewPage(
    telemetry: DeviceTelemetry,
    config: TweakConfigEntity,
    heroVisualActive: Boolean,
    recentLogs: List<LogEntry>,
    onToggleMaster: () -> Unit,
    onSelectProfile: (String) -> Unit,
    onSelectPerformanceSubMode: (String) -> Unit,
    onOpenConsole: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 14.dp,
            bottom = 136.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Expressive Battery + Master Engine Hero Card (.hero)
        item {
            ExpressiveHeroCard(
                telemetry = telemetry,
                isEngineActive = heroVisualActive,
                activeProfile = config.activeProfile,
                onToggleMaster = onToggleMaster
            )
        }

        // 2. Power Profile + Governor Tier Card (.card .profiles)
        item {
            ExpressivePowerProfileCard(
                activeProfile = config.activeProfile,
                performanceSubMode = config.performanceSubMode,
                onSelectProfile = onSelectProfile,
                onSelectPerformanceSubMode = onSelectPerformanceSubMode
            )
        }

        // 3. Bento Telemetry Grid (.bento: Octa-core Equalizer + GPU + ZRAM + Thermal + Capacity)
        // Strictly NO Battery Health, NO Cycle Count, and NO Average CPU Frequency Graph
        item {
            ExpressiveBentoSection(telemetry = telemetry)
        }

        // 4. Recent Activity Timeline (.tl)
        item {
            ExpressiveTimelineCard(
                recentLogs = recentLogs.take(4),
                onOpenConsole = onOpenConsole
            )
        }
    }
}

// ============================================================================
// HERO CARD (1:1 with HTML .hero, .bg1, .bg2, .orb, .wave, .mini, .engine)
// ============================================================================

@Composable
private fun ExpressiveHeroCard(
    telemetry: DeviceTelemetry,
    isEngineActive: Boolean,
    activeProfile: String,
    onToggleMaster: () -> Unit
) {
    val roles = LocalMonetRoles.current

    // Pausable continuous animation clock matching CSS animation-play-state: running | paused
    // Never snaps back to 0° when paused!
    var elapsedActiveMs by remember { mutableLongStateOf(0L) }
    LaunchedEffect(isEngineActive) {
        if (isEngineActive) {
            var lastFrameMs = -1L
            while (true) {
                withInfiniteAnimationFrameMillis { frameMs ->
                    if (lastFrameMs >= 0L) {
                        val delta = (frameMs - lastFrameMs).coerceIn(0L, 100L)
                        elapsedActiveMs += delta
                    }
                    lastFrameMs = frameMs
                }
            }
        }
    }

    // Exact CSS rotation speeds:
    // .orb svg.c -> 36s linear infinite (360° / 36000ms)
    // .hero .bg1 -> 50s linear infinite (360° / 50000ms)
    // .hero .bg2 -> 40s linear infinite reverse (-360° / 40000ms)
    // .wave svg  -> 1.6s linear infinite slide (-24.dp / 1600ms)
    val orbRotation = ((elapsedActiveMs % 36000L) / 36000f) * 360f
    val bg1Rotation = ((elapsedActiveMs % 50000L) / 50000f) * 360f
    val bg2Rotation = -((elapsedActiveMs % 40000L) / 40000f) * 360f
    val waveSlideFraction = (elapsedActiveMs % 1600L) / 1600f

    val heroContainerColor by animateColorAsState(
        targetValue = if (isEngineActive) roles.pc else roles.s3,
        animationSpec = tween(durationMillis = 450),
        label = "HeroContainerColor"
    )
    val heroContentColor by animateColorAsState(
        targetValue = if (isEngineActive) roles.onPc else roles.onS,
        animationSpec = tween(durationMillis = 450),
        label = "HeroContentColor"
    )
    val orbFillColor by animateColorAsState(
        targetValue = if (isEngineActive) roles.primary else roles.s4,
        animationSpec = tween(durationMillis = 450),
        label = "OrbFillColor"
    )
    val orbTextColor by animateColorAsState(
        targetValue = if (isEngineActive) roles.onPrimary else roles.onSv,
        animationSpec = tween(durationMillis = 450),
        label = "OrbTextColor"
    )
    val knobRotation by animateFloatAsState(
        targetValue = if (isEngineActive) 0f else 180f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "KnobRotation"
    )

    val voltageVolts = if (telemetry.batteryVoltageMv > 0) telemetry.batteryVoltageMv / 1000f else 4.12f
    val currentAmps = if (abs(telemetry.batteryCurrentMa) > 0) abs(telemetry.batteryCurrentMa) / 1000f else 2.9f
    val watts = if (telemetry.chargingWatts > 0.1f) {
        telemetry.chargingWatts
    } else {
        voltageVolts * currentAmps
    }
    val displayLevel = telemetry.batteryLevel.coerceIn(1, 100)

    Surface(
        shape = RoundedCornerShape(44.dp),
        color = heroContainerColor,
        contentColor = heroContentColor,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(44.dp))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Background Scalloped Shapes (.hero .bg1 & .hero .bg2) drawn inside clipped Hero bounds
            // CSS:
            // .hero .bg1 { width: 260px; right: -80px; top: -90px; opacity: .16; fill: var(--primary); animation: spin 50s linear infinite }
            // .hero .bg2 { width: 180px; left: -70px; bottom: -80px; opacity: .1; fill: var(--tert); animation: spin 40s linear infinite reverse }
            val bg1Color = roles.primary.copy(alpha = if (isEngineActive) 0.16f else 0.07f)
            val bg2Color = roles.tert.copy(alpha = if (isEngineActive) 0.10f else 0.07f)

            Canvas(modifier = Modifier.matchParentSize()) {
                val bg1SizePx = 260.dp.toPx()
                val bg1Left = size.width - bg1SizePx + 80.dp.toPx()
                val bg1Top = -90.dp.toPx()
                val bg1Path = createScaledPath(BaseBg1Path, bg1SizePx, bg1SizePx)

                translate(left = bg1Left, top = bg1Top) {
                    rotate(degrees = bg1Rotation, pivot = Offset(bg1SizePx / 2f, bg1SizePx / 2f)) {
                        drawPath(path = bg1Path, color = bg1Color)
                    }
                }

                val bg2SizePx = 180.dp.toPx()
                val bg2Left = -70.dp.toPx()
                val bg2Top = size.height - bg2SizePx + 80.dp.toPx()
                val bg2Path = createScaledPath(BaseCookiePath, bg2SizePx, bg2SizePx)

                translate(left = bg2Left, top = bg2Top) {
                    rotate(degrees = bg2Rotation, pivot = Offset(bg2SizePx / 2f, bg2SizePx / 2f)) {
                        drawPath(path = bg2Path, color = bg2Color)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 20.dp)
            ) {
                // Hero Top Row (.hero-top)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "BATTERY",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.5.sp,
                            letterSpacing = 1.05.sp,
                            color = heroContentColor.copy(alpha = 0.7f)
                        )
                        Text(
                            text = if (isEngineActive) "Charging fast" else "Engine paused",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            letterSpacing = (-0.4).sp,
                            color = heroContentColor,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }

                    // Wattage Pill (.pill)
                    Surface(
                        shape = CircleShape,
                        color = heroContentColor.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Bolt,
                                contentDescription = null,
                                tint = heroContentColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = String.format(Locale.US, "%.1f W", watts),
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = heroContentColor
                            )
                        }
                    }
                }

                // Center Rotating Scalloped Orb (.orb: width 196px, height 196px, margin: 12px auto 6px)
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 6.dp)
                        .size(196.dp)
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    ExactHtmlCookieShape(
                        color = orbFillColor,
                        rotationDegrees = orbRotation,
                        dropShadowColor = if (isEngineActive) roles.primary.copy(alpha = 0.38f) else null,
                        modifier = Modifier.fillMaxSize()
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "$displayLevel",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 64.sp,
                                lineHeight = 58.sp,
                                letterSpacing = (-4.2).sp,
                                color = orbTextColor
                            )
                            Text(
                                text = "%",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = orbTextColor,
                                modifier = Modifier.padding(start = 2.dp, top = 6.dp)
                            )
                        }
                        val minsToFull = ((100 - displayLevel).coerceAtLeast(1) * 1.9f).roundToInt()
                        Text(
                            text = "≈ $minsToFull min to full",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            letterSpacing = 0.4.sp,
                            color = orbTextColor.copy(alpha = 0.85f),
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }

                // Exact Sine Wave Progress Bar (.wave from HTML lines 119-123 & 313)
                // <div class="wave"><div style="--v:78%"><svg viewBox="0 0 760 14"><path d="M0 7 Q6 0 12 7 T24 7 ..."/></svg></div></div>
                val waveStrokeColor = if (isEngineActive) roles.primary else roles.outline
                val trackColor = heroContentColor.copy(alpha = 0.18f)
                val fillFraction = (displayLevel / 100f).coerceIn(0.05f, 1f)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 4.dp, top = 12.dp, bottom = 4.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val midY = size.height / 2f
                        val trackHeight = 4.dp.toPx()

                        // Background 4px centered bar (100% width)
                        drawRoundRect(
                            color = trackColor,
                            topLeft = Offset(0f, midY - trackHeight / 2f),
                            size = Size(size.width, trackHeight),
                            cornerRadius = CornerRadius(trackHeight / 2f, trackHeight / 2f)
                        )

                        // Clipped foreground quadratic Bezier wave (width: var(--v))
                        clipRect(
                            left = 0f,
                            top = 0f,
                            right = size.width * fillFraction,
                            bottom = size.height
                        ) {
                            val unit = size.height / 14f // 1 SVG viewBox unit = 1.dp
                            val wavelength = 24f * unit
                            val halfWave = 12f * unit
                            val quarterWave = 6f * unit
                            val slideOffsetPx = -wavelength * waveSlideFraction

                            val wavePath = Path()
                            var x = slideOffsetPx
                            wavePath.moveTo(x, midY)
                            val maxX = size.width + wavelength * 2f
                            while (x < maxX) {
                                // Q 6 0, 12 7
                                wavePath.quadraticTo(
                                    x + quarterWave,
                                    0f,
                                    x + halfWave,
                                    midY
                                )
                                // T 24 7 (which is Q 18 14, 24 7)
                                wavePath.quadraticTo(
                                    x + halfWave + quarterWave,
                                    size.height,
                                    x + wavelength,
                                    midY
                                )
                                x += wavelength
                            }

                            drawPath(
                                path = wavePath,
                                color = waveStrokeColor,
                                style = Stroke(
                                    width = 4.5f * unit,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                }

                // 3-Column Mini Telemetry Pills (.mini: VOLT / AMPS / TEMP)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HeroMiniStatBox(
                        label = "VOLT",
                        value = String.format(Locale.US, "%.2f V", voltageVolts),
                        contentColor = heroContentColor,
                        modifier = Modifier.weight(1f)
                    )
                    HeroMiniStatBox(
                        label = "AMPS",
                        value = String.format(Locale.US, "%.1f A", currentAmps),
                        contentColor = heroContentColor,
                        modifier = Modifier.weight(1f)
                    )
                    HeroMiniStatBox(
                        label = "TEMP",
                        value = String.format(Locale.US, "%.1f°", telemetry.batteryTempC),
                        contentColor = heroContentColor,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Master RAM Engine Pill Toggle (.engine from HTML lines 128-133 & 317-320)
                val engineBg by animateColorAsState(
                    targetValue = if (isEngineActive) roles.primary else roles.s4,
                    animationSpec = tween(durationMillis = 350),
                    label = "EnginePillBg"
                )
                val engineFg by animateColorAsState(
                    targetValue = if (isEngineActive) roles.onPrimary else roles.onS,
                    animationSpec = tween(durationMillis = 350),
                    label = "EnginePillFg"
                )

                val profileLabel = when (activeProfile) {
                    "POWERSAVE" -> "Powersave"
                    "PERFORMANCE" -> "Performance"
                    else -> "Balanced"
                }

                Surface(
                    onClick = onToggleMaster,
                    shape = CircleShape,
                    color = engineBg,
                    contentColor = engineFg,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .testTag("master_engine_switch")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isEngineActive) "RAM engine active" else "RAM engine standby",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = engineFg
                            )
                            Text(
                                text = if (isEngineActive) {
                                    "$profileLabel mounted · resets on reboot"
                                } else {
                                    "Tap to apply volatile tweaks"
                                },
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                color = engineFg.copy(alpha = 0.8f),
                                modifier = Modifier.padding(top = 1.dp)
                            )
                        }

                        // Rotating circular knob (.knob 52x52px)
                        Surface(
                            shape = CircleShape,
                            color = if (isEngineActive) roles.onPrimary else roles.s2,
                            contentColor = if (isEngineActive) roles.primary else roles.onSv,
                            modifier = Modifier
                                .size(52.dp)
                                .rotate(knobRotation)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Bolt,
                                    contentDescription = "Toggle RAM Engine",
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroMiniStatBox(
    label: String,
    value: String,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = contentColor.copy(alpha = 0.09f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp)
        ) {
            Text(
                text = label,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 9.5.sp,
                letterSpacing = 0.8.sp,
                color = contentColor.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                letterSpacing = (-0.3).sp,
                color = contentColor,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

// ============================================================================
// POWER PROFILE SELECTOR CARD (Matches HTML .profiles + .tier lines 136-149)
// ============================================================================

@Composable
private fun ExpressivePowerProfileCard(
    activeProfile: String,
    performanceSubMode: String,
    onSelectProfile: (String) -> Unit,
    onSelectPerformanceSubMode: (String) -> Unit
) {
    val roles = LocalMonetRoles.current
    Surface(
        shape = RoundedCornerShape(34.dp),
        color = roles.s2,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Power profile",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = roles.onS
                )
                Text(
                    text = "TAP TO SWITCH",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.5.sp,
                    letterSpacing = 1.sp,
                    color = roles.onSv
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExpressiveProfileTile(
                    title = "Powersave",
                    subtitle = "Max efficiency",
                    icon = Icons.Filled.Eco,
                    selected = activeProfile == "POWERSAVE",
                    testTag = "profile_chip_powersave",
                    onClick = { onSelectProfile("POWERSAVE") },
                    modifier = Modifier.weight(1f)
                )
                ExpressiveProfileTile(
                    title = "Balanced",
                    subtitle = "Daily adaptive",
                    icon = Icons.Filled.Contrast,
                    selected = activeProfile == "BALANCE",
                    testTag = "profile_chip_balance",
                    onClick = { onSelectProfile("BALANCE") },
                    modifier = Modifier.weight(1f)
                )
                ExpressiveProfileTile(
                    title = "Performance",
                    subtitle = "Peak boost",
                    icon = Icons.Filled.RocketLaunch,
                    selected = activeProfile == "PERFORMANCE",
                    testTag = "profile_chip_performance",
                    onClick = { onSelectProfile("PERFORMANCE") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Governor Tier Segmented Row (.tier when Performance is active)
            AnimatedVisibility(
                visible = activeProfile == "PERFORMANCE",
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    Text(
                        text = "GOVERNOR TIER",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.5.sp,
                        letterSpacing = 1.sp,
                        color = roles.onSv
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clip(CircleShape)
                            .background(roles.s4)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val tiers = listOf(
                            Triple("LITE", "Lite", "smooth"),
                            Triple("HEAVY", "Heavy", "gaming"),
                            Triple("ULTRA", "Ultra", "max lock")
                        )
                        tiers.forEach { (key, title, sub) ->
                            val isSelected = performanceSubMode == key
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .background(if (isSelected) roles.primary else Color.Transparent)
                                    .clickable { onSelectPerformanceSubMode(key) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = title,
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) roles.onPrimary else roles.onSv
                                    )
                                    Text(
                                        text = sub,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 9.5.sp,
                                        color = if (isSelected) {
                                            roles.onPrimary.copy(alpha = 0.78f)
                                        } else {
                                            roles.onSv.copy(alpha = 0.7f)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpressiveProfileTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val roles = LocalMonetRoles.current
    val topCorner by animateDpAsState(
        targetValue = if (selected) 38.dp else 26.dp,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "ProfileTopCorner"
    )
    val iconCornerPercent by animateFloatAsState(
        targetValue = if (selected) 34f else 50f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "ProfileIconCorner"
    )
    val iconRotation by animateFloatAsState(
        targetValue = if (selected) -8f else 0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "ProfileIconRotation"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.08f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "ProfileIconScale"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(
            topStart = topCorner,
            topEnd = topCorner,
            bottomStart = 26.dp,
            bottomEnd = 26.dp
        ),
        color = if (selected) roles.sc else roles.s1,
        modifier = modifier.testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, end = 6.dp, top = 18.dp, bottom = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(percent = iconCornerPercent.roundToInt()),
                color = if (selected) roles.primary else roles.s4,
                contentColor = if (selected) roles.onPrimary else roles.onSv,
                modifier = Modifier
                    .size(54.dp)
                    .scale(iconScale)
                    .rotate(iconRotation)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    color = if (selected) roles.onSc else roles.onS,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    color = if (selected) roles.onSc.copy(alpha = 0.8f) else roles.onSv,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ============================================================================
// BENTO TELEMETRY GRID (Matches HTML .bento lines 151-170 & 335-351)
// ============================================================================

@Composable
private fun ExpressiveBentoSection(telemetry: DeviceTelemetry) {
    val roles = LocalMonetRoles.current
    val infiniteTransition = rememberInfiniteTransition(label = "EqualizerPulse")
    val eqTimeSec by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 5.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "EqTimeSec"
    )

    // Exact HTML equalizer bar specs: (--a, --b, --d) from lines 340-341
    val eqSpecs = remember {
        listOf(
            Triple(0.34f, 0.62f, 0.2f),
            Triple(0.28f, 0.46f, 1.1f),
            Triple(0.52f, 0.30f, 0.6f),
            Triple(0.24f, 0.40f, 1.8f),
            Triple(0.70f, 0.92f, 0.9f),
            Triple(0.60f, 0.80f, 0.3f),
            Triple(0.86f, 0.58f, 1.4f),
            Triple(0.12f, 0.12f, 0.0f)
        )
    }

    val gpuMhz = Regex("(\\d+)\\s*MHz", RegexOption.IGNORE_CASE)
        .find(telemetry.gpuFreqInfo)?.groupValues?.getOrNull(1) ?: "315"

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Span-2 Card: Octa-core · live Equalizer (.b.w2)
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = roles.s2,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "OCTA-CORE · LIVE",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.5.sp,
                        letterSpacing = 1.sp,
                        color = roles.onSv
                    )
                    Icon(
                        imageVector = Icons.Filled.DeveloperBoard,
                        contentDescription = null,
                        tint = roles.onSv.copy(alpha = 0.6f),
                        modifier = Modifier.size(22.dp)
                    )
                }

                // 8-Bar Live Equalizer (.eq from HTML lines 158-161 & 339-342)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .height(96.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    eqSpecs.forEachIndexed { idx, (a, b, delaySec) ->
                        // 2.6s ease-in-out infinite alternate with animation-delay: calc(var(--d) * -1s)
                        val t = (eqTimeSec + delaySec) % 5.2f
                        val linearPhase = if (t <= 2.6f) t / 2.6f else (5.2f - t) / 2.6f
                        val eased = 0.5f - 0.5f * cos(linearPhase * PI.toFloat())
                        val heightFraction = (a + (b - a) * eased).coerceIn(0.10f, 0.98f)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(heightFraction)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 10.dp,
                                        topEnd = 10.dp,
                                        bottomStart = 6.dp,
                                        bottomEnd = 6.dp
                                    )
                                )
                                .background(if (idx < 4) roles.primary else roles.tert)
                        )
                    }
                }

                // Core indices 0..7 (.eql)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (i in 0..7) {
                        Text(
                            text = i.toString(),
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 9.5.sp,
                            color = roles.onSv,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Governor Pills (.gov)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GovernorPill(
                        text = telemetry.activeGovernor.ifBlank { "schedutil" },
                        highlighted = true
                    )
                    GovernorPill(text = "EAS", highlighted = false)
                    GovernorPill(text = "LITTLE 1.1G", highlighted = false)
                    GovernorPill(text = "PRIME 2.7G", highlighted = false)
                }
            }
        }

        // Row 1 of 2x2 Bento: GPU (.b.p) + ZRAM (.b.t)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // GPU Card (.b.p)
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = roles.pc,
                contentColor = roles.onPc,
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GPU",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.5.sp,
                            letterSpacing = 1.sp,
                            color = roles.onPc.copy(alpha = 0.7f)
                        )
                        Icon(
                            imageVector = Icons.Filled.Speed,
                            contentDescription = null,
                            tint = roles.onPc.copy(alpha = 0.6f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.padding(top = 14.dp)
                    ) {
                        Text(
                            text = gpuMhz,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp,
                            letterSpacing = (-1.2).sp
                        )
                        Text(
                            text = "MHz",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = roles.onPc.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 3.dp, start = 3.dp)
                        )
                    }
                    Text(
                        text = "Adreno · pwrlevel 6",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.5.sp,
                        color = roles.onPc.copy(alpha = 0.75f),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            // ZRAM Card (.b.t)
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = roles.tc,
                contentColor = roles.onTc,
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ZRAM",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.5.sp,
                            letterSpacing = 1.sp,
                            color = roles.onTc.copy(alpha = 0.7f)
                        )
                        Icon(
                            imageVector = Icons.Filled.Memory,
                            contentDescription = null,
                            tint = roles.onTc.copy(alpha = 0.6f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.padding(top = 14.dp)
                    ) {
                        Text(
                            text = "2.1",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp,
                            letterSpacing = (-1.2).sp
                        )
                        Text(
                            text = "/4 GB",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = roles.onTc.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 3.dp, start = 3.dp)
                        )
                    }
                    Text(
                        text = "52% used",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.5.sp,
                        color = roles.onTc.copy(alpha = 0.75f),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }

        // Row 2 of 2x2 Bento: Thermal (.b.s) + Capacity (.b)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Thermal Card (.b.s + .thermo gradient bar)
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = roles.sc,
                contentColor = roles.onSc,
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "THERMAL",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.5.sp,
                            letterSpacing = 1.sp,
                            color = roles.onSc.copy(alpha = 0.7f)
                        )
                        Icon(
                            imageVector = Icons.Filled.Thermostat,
                            contentDescription = null,
                            tint = roles.onSc.copy(alpha = 0.6f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.padding(top = 14.dp)
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.1f", telemetry.batteryTempC),
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp,
                            letterSpacing = (-1.2).sp
                        )
                        Text(
                            text = "°C",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = roles.onSc.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 3.dp, start = 3.dp)
                        )
                    }

                    // Thermometer Bar (.thermo from HTML lines 168-169)
                    val tertColor = roles.tert
                    val primColor = roles.primary
                    val errColor = roles.err
                    val bgThumb = roles.bg
                    val borderThumb = roles.onS

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp)
                            .height(18.dp)
                    ) {
                        val barHeight = 10.dp.toPx()
                        val topY = (size.height - barHeight) / 2f
                        drawRoundRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(tertColor, primColor, errColor)
                            ),
                            topLeft = Offset(0f, topY),
                            size = Size(size.width, barHeight),
                            cornerRadius = CornerRadius(barHeight / 2f, barHeight / 2f)
                        )
                        val thumbX = size.width * 0.34f
                        val centerY = size.height / 2f
                        drawCircle(
                            color = borderThumb,
                            radius = 9.dp.toPx(),
                            center = Offset(thumbX, centerY)
                        )
                        drawCircle(
                            color = bgThumb,
                            radius = 5.dp.toPx(),
                            center = Offset(thumbX, centerY)
                        )
                    }
                }
            }

            // Capacity Card (.b — Strictly NO Battery Health or Cycle Count)
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = roles.s2,
                contentColor = roles.onS,
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CAPACITY",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.5.sp,
                            letterSpacing = 1.sp,
                            color = roles.onSv
                        )
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = null,
                            tint = roles.onSv.copy(alpha = 0.6f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.padding(top = 14.dp)
                    ) {
                        Text(
                            text = "${telemetry.batteryCapacityMah}",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp,
                            letterSpacing = (-1.2).sp
                        )
                        Text(
                            text = "mAh",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = roles.onSv,
                            modifier = Modifier.padding(bottom = 3.dp, start = 3.dp)
                        )
                    }
                    Text(
                        text = "Design capacity",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.5.sp,
                        color = roles.onSv.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GovernorPill(text: String, highlighted: Boolean) {
    val roles = LocalMonetRoles.current
    Surface(
        shape = CircleShape,
        color = if (highlighted) roles.primary else roles.s4,
        contentColor = if (highlighted) roles.onPrimary else roles.onS
    ) {
        Text(
            text = text,
            fontFamily = JetBrainsMonoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
        )
    }
}

// ============================================================================
// RECENT ACTIVITY TIMELINE CARD (Matches HTML .tl lines 171-179 & 353-360)
// ============================================================================

@Composable
private fun ExpressiveTimelineCard(
    recentLogs: List<LogEntry>,
    onOpenConsole: () -> Unit
) {
    val roles = LocalMonetRoles.current
    val entries = if (recentLogs.isNotEmpty()) {
        recentLogs.map {
            TimelinePreviewItem(
                title = it.displayName,
                time = it.timestamp.takeLast(5),
                code = it.command?.takeIf { c -> c.isNotBlank() } ?: "${it.paramName}=${it.paramValue}",
                isError = !it.isSuccess
            )
        }
    } else {
        listOf(
            TimelinePreviewItem("TCP BBR", "12:04", "tcp_congestion_control=bbr", false),
            TimelinePreviewItem("GMS doze", "12:03", "WAKE_LOCK ignore", false),
            TimelinePreviewItem("Adreno idler", "12:03", "node missing · skipped", true)
        )
    }

    Surface(
        onClick = onOpenConsole,
        shape = RoundedCornerShape(34.dp),
        color = roles.s2,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent activity",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = roles.onS
                )
                Surface(
                    shape = CircleShape,
                    color = roles.pc,
                    contentColor = roles.onPc
                ) {
                    Text(
                        text = "live",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
            ) {
                // Vertical connecting line (.tl::before)
                Box(
                    modifier = Modifier
                        .padding(start = 5.dp, top = 6.dp, bottom = 6.dp)
                        .width(2.dp)
                        .matchParentSize()
                        .background(roles.s4)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    entries.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(if (item.isError) roles.err else roles.primary)
                                    .border(2.dp, roles.s2, CircleShape)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.title,
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = roles.onS
                                    )
                                    Text(
                                        text = item.time,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.5.sp,
                                        color = roles.onSv
                                    )
                                }
                                Text(
                                    text = item.code,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp,
                                    color = roles.primary,
                                    modifier = Modifier.padding(top = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class TimelinePreviewItem(
    val title: String,
    val time: String,
    val code: String,
    val isError: Boolean
)

// ============================================================================
// TAB 2: TWEAKS PAGE (Matches HTML #p2: Search + Banner + 2x2 Tiles + Chips + Groups)
// ============================================================================

@Composable
private fun ExpressiveTweaksPage(
    config: TweakConfigEntity,
    onUpdateTweak: (String, Boolean) -> Unit,
    onToggleHyperOs: (Boolean) -> Unit,
    onSelectRefreshRate: (Int) -> Unit,
    onUpdateCoreTopology: (Int, Int) -> Unit
) {
    val roles = LocalMonetRoles.current
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf(TweakCategoryFilter.ALL) }
    var sliderThreshold by remember(config.offlineBatteryThreshold) {
        mutableFloatStateOf(config.offlineBatteryThreshold.toFloat())
    }

    val allTweaks = remember {
        listOf(
            // CPU & Adreno (5 tweaks)
            TweakItemSpec(
                key = "cpuFreqCap",
                category = TweakCategoryFilter.CPU,
                title = "Limit peak frequencies",
                subtitle = "Cap big & prime clusters vs thermal spikes",
                sysfsCode = "cpu*/cpufreq/scaling_max_freq",
                icon = Icons.Filled.Speed,
                isChecked = { it.cpuFreqCapEnabled }
            ),
            TweakItemSpec(
                key = "schedutilGov",
                category = TweakCategoryFilter.CPU,
                title = "Schedutil energy governor",
                subtitle = "Smoother frequency ramps",
                sysfsCode = "schedutil/up_rate_limit_us=4000",
                icon = Icons.Filled.GraphicEq,
                isChecked = { it.schedutilGovernorEnabled }
            ),
            TweakItemSpec(
                key = "inputTouchBoost",
                category = TweakCategoryFilter.CPU,
                title = "Disable touch boost",
                subtitle = "No max-clock spike on every swipe",
                sysfsCode = "msm_performance/touchboost=0",
                icon = Icons.Filled.Visibility,
                isChecked = { it.inputTouchBoostDisabled }
            ),
            TweakItemSpec(
                key = "gpuPowerLimit",
                category = TweakCategoryFilter.CPU,
                title = "GPU power limiter",
                subtitle = "Throttle pwrlevel outside gaming",
                sysfsCode = "kgsl-3d0/default_pwrlevel=6",
                icon = Icons.Filled.DeveloperBoard,
                isChecked = { it.gpuPowerLimitEnabled }
            ),
            TweakItemSpec(
                key = "adrenoIdler",
                category = TweakCategoryFilter.CPU,
                title = "Adreno idler",
                subtitle = "Lowest clock when frame queue idles",
                sysfsCode = "adreno_idler_active=Y",
                icon = Icons.Filled.AcUnit,
                isChecked = { it.adrenoIdlerEnabled }
            ),
            // Doze & power saving (4 tweaks)
            TweakItemSpec(
                key = "gmsDoze",
                category = TweakCategoryFilter.BAT,
                title = "Aggressive GMS doze",
                subtitle = "Restrict Play Services wake locks",
                sysfsCode = "appops set …gms WAKE_LOCK ignore",
                icon = Icons.Filled.DarkMode,
                isChecked = { it.gmsDozeEnabled }
            ),
            TweakItemSpec(
                key = "lpmSleep",
                category = TweakCategoryFilter.BAT,
                title = "Kernel LPM deep sleep",
                subtitle = "Low-power cluster sleep, screen off",
                sysfsCode = "lpm_levels/sleep_disabled=0",
                icon = Icons.Filled.Eco,
                isChecked = { it.lpmSleepEnabled }
            ),
            TweakItemSpec(
                key = "powerEfficientWorkqueue",
                category = TweakCategoryFilter.BAT,
                title = "Power-efficient workqueues",
                subtitle = "Background jobs to LITTLE cluster",
                sysfsCode = "workqueue/power_efficient=Y",
                icon = Icons.Filled.DeveloperBoard,
                isChecked = { it.powerEfficientWorkqueueEnabled }
            ),
            TweakItemSpec(
                key = "backgroundScanning",
                category = TweakCategoryFilter.BAT,
                title = "No Wi-Fi/BLE background scan",
                subtitle = "Stop hidden beacon scans when idle",
                sysfsCode = "wifi_scan_always_enabled 0",
                icon = Icons.Filled.Wifi,
                isChecked = { it.backgroundScanningDisabled }
            ),
            // HyperOS engine (4 tweaks)
            TweakItemSpec(
                key = "hyperOsPowerSaver",
                category = TweakCategoryFilter.OS,
                title = "Extreme standby engine",
                subtitle = "Joyose & PowerKeeper hibernation",
                sysfsCode = "power_supersave_mode_open 1",
                icon = Icons.Filled.Bolt,
                isChecked = { it.hyperOsPowerSaverEnabled }
            ),
            TweakItemSpec(
                key = "hyperOsAurogon",
                category = TweakCategoryFilter.OS,
                title = "Aurogon freezer",
                subtitle = "Freeze cached apps on lock",
                sysfsCode = "persist.sys.miui.freezer=true",
                icon = Icons.Filled.AcUnit,
                isChecked = { it.hyperOsAurogonFreezerEnabled }
            ),
            TweakItemSpec(
                key = "hyperOsFiveG",
                category = TweakCategoryFilter.OS,
                title = "5G smart power opt",
                subtitle = "Fall back to LTE at low demand",
                sysfsCode = "fiveg_power_saving_enable 1",
                icon = Icons.Filled.Wifi,
                isChecked = { it.hyperOsFiveGPowerOptEnabled }
            ),
            TweakItemSpec(
                key = "controlCenterBlur",
                category = TweakCategoryFilter.OS,
                title = "Remove control-center blur",
                subtitle = "Cut real-time blur shader load",
                sysfsCode = "disable_window_blurs 1",
                icon = Icons.Filled.Visibility,
                isChecked = { it.controlCenterBlurEnabled }
            ),
            // Display & I/O (3 toggle tweaks + Peak refresh rate card = 4 tweaks)
            TweakItemSpec(
                key = "storageIo",
                category = TweakCategoryFilter.IO,
                title = "Optimise UFS I/O queue",
                subtitle = "Lower iostats, read_ahead 128 KB",
                sysfsCode = "queue/iostats=0",
                icon = Icons.Filled.Storage,
                isChecked = { it.storageIoQueueEnabled }
            ),
            TweakItemSpec(
                key = "vmDirtyWriteback",
                category = TweakCategoryFilter.IO,
                title = "Defer VM dirty writeback",
                subtitle = "Batch flushes, fewer flash wakeups",
                sysfsCode = "vm.dirty_writeback_centisecs=3000",
                icon = Icons.Filled.Storage,
                isChecked = { it.vmDirtyWritebackEnabled }
            ),
            TweakItemSpec(
                key = "tcpBbr",
                category = TweakCategoryFilter.IO,
                title = "TCP BBR",
                subtitle = "Lower socket latency",
                sysfsCode = "net.ipv4.tcp_congestion_control=bbr",
                icon = Icons.Filled.Wifi,
                isChecked = { it.tcpBbrCongestionEnabled }
            )
        )
    }

    val filteredTweaks = remember(searchQuery, selectedCategory, allTweaks) {
        allTweaks.filter { spec ->
            val matchesCategory = selectedCategory == TweakCategoryFilter.ALL || spec.category == selectedCategory
            val q = searchQuery.trim().lowercase(Locale.US)
            val matchesQuery = q.isEmpty() ||
                spec.title.lowercase(Locale.US).contains(q) ||
                spec.subtitle.lowercase(Locale.US).contains(q) ||
                spec.sysfsCode.lowercase(Locale.US).contains(q)
            matchesCategory && matchesQuery
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 14.dp,
            bottom = 176.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Search Pill (.search)
        item {
            Surface(
                shape = CircleShape,
                color = roles.s3,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = roles.onSv
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search 16 kernel tweaks",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = roles.onSv
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = roles.onS
                            ),
                            cursorBrush = SolidColor(roles.primary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear search",
                            tint = roles.onSv,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { searchQuery = "" }
                        )
                    }
                }
            }
        }

        // 2. 100% Volatile Safety Banner (.banner)
        item {
            Surface(
                shape = RoundedCornerShape(30.dp),
                color = roles.tc,
                contentColor = roles.onTc,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "100% volatile",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Every write resets on reboot",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 12.sp,
                            color = roles.onTc.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // 3. 2x2 Expressive Morphing Quick Tiles (.tiles)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ExpressiveQuickTile(
                        title = "CPU cap",
                        subtitle = if (config.cpuFreqCapEnabled) "Active" else "Off",
                        icon = Icons.Filled.Speed,
                        checked = config.cpuFreqCapEnabled,
                        onToggle = { onUpdateTweak("cpuFreqCap", !config.cpuFreqCapEnabled) },
                        modifier = Modifier.weight(1f)
                    )
                    ExpressiveQuickTile(
                        title = "HyperOS saver",
                        subtitle = if (config.hyperOsPowerSaverEnabled) "Active" else "Off",
                        icon = Icons.Filled.Bolt,
                        checked = config.hyperOsPowerSaverEnabled,
                        onToggle = { onToggleHyperOs(!config.hyperOsPowerSaverEnabled) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ExpressiveQuickTile(
                        title = "GMS doze",
                        subtitle = if (config.gmsDozeEnabled) "Restricted" else "Stock",
                        icon = Icons.Filled.DarkMode,
                        checked = config.gmsDozeEnabled,
                        onToggle = { onUpdateTweak("gmsDoze", !config.gmsDozeEnabled) },
                        modifier = Modifier.weight(1f)
                    )
                    ExpressiveQuickTile(
                        title = "TCP BBR",
                        subtitle = if (config.tcpBbrCongestionEnabled) "BBR" else "Cubic",
                        icon = Icons.Filled.Wifi,
                        checked = config.tcpBbrCongestionEnabled,
                        onToggle = { onUpdateTweak("tcpBbr", !config.tcpBbrCongestionEnabled) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 4. Category Chips (.chips)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TweakCategoryFilter.entries.forEach { cat ->
                    val selected = selectedCategory == cat
                    val scale by animateFloatAsState(
                        targetValue = if (selected) 1.04f else 1f,
                        animationSpec = spring(dampingRatio = 0.55f),
                        label = "ChipScale"
                    )
                    Surface(
                        onClick = { selectedCategory = cat },
                        shape = CircleShape,
                        color = if (selected) roles.onS else roles.s3,
                        contentColor = if (selected) roles.bg else roles.onSv,
                        modifier = Modifier.scale(scale)
                    ) {
                        Text(
                            text = cat.label,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 11.dp)
                        )
                    }
                }
            }
        }

        // 5. Grouped Tweak Sections (.grp)
        val cpuGroup = filteredTweaks.filter { it.category == TweakCategoryFilter.CPU }
        if (cpuGroup.isNotEmpty()) {
            item {
                ExpressiveTweakGroupHeader(title = "CPU & Adreno", countLabel = "5 tweaks")
                ExpressiveGroupedTweakList(
                    items = cpuGroup,
                    config = config,
                    onToggle = { spec, checked -> onUpdateTweak(spec.key, checked) }
                )

                // Park big cores on low battery slider card (.slide)
                Surface(
                    shape = RoundedCornerShape(30.dp),
                    color = roles.s2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Park big cores on low battery",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.5.sp,
                                    color = roles.onS
                                )
                                Text(
                                    text = "Park ${config.offlineCoreCount} cores below ${sliderThreshold.roundToInt()}%",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 12.sp,
                                    color = roles.onSv,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            ExpressiveHtmlSwitch(
                                checked = config.twoCoresOfflineBelow20Enabled,
                                onCheckedChange = { onUpdateTweak("twoCoresOffline", it) }
                            )
                        }
                        Slider(
                            value = sliderThreshold,
                            onValueChange = { sliderThreshold = it },
                            onValueChangeFinished = {
                                onUpdateCoreTopology(config.offlineCoreCount, sliderThreshold.roundToInt())
                            },
                            valueRange = 5f..60f,
                            steps = 10,
                            colors = SliderDefaults.colors(
                                thumbColor = roles.primary,
                                activeTrackColor = roles.primary,
                                inactiveTrackColor = roles.s4
                            ),
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
        }

        val batGroup = filteredTweaks.filter { it.category == TweakCategoryFilter.BAT }
        if (batGroup.isNotEmpty()) {
            item {
                ExpressiveTweakGroupHeader(title = "Doze & power saving", countLabel = "4 tweaks")
                ExpressiveGroupedTweakList(
                    items = batGroup,
                    config = config,
                    onToggle = { spec, checked -> onUpdateTweak(spec.key, checked) }
                )
            }
        }

        val osGroup = filteredTweaks.filter { it.category == TweakCategoryFilter.OS }
        if (osGroup.isNotEmpty()) {
            item {
                ExpressiveTweakGroupHeader(title = "HyperOS engine", countLabel = "4 tweaks")
                ExpressiveGroupedTweakList(
                    items = osGroup,
                    config = config,
                    onToggle = { spec, checked ->
                        if (spec.key == "hyperOsPowerSaver") {
                            onToggleHyperOs(checked)
                        } else {
                            onUpdateTweak(spec.key, checked)
                        }
                    }
                )
            }
        }

        val ioGroup = filteredTweaks.filter { it.category == TweakCategoryFilter.IO }
        if (ioGroup.isNotEmpty()) {
            item {
                ExpressiveTweakGroupHeader(title = "Display & I/O", countLabel = "4 tweaks")
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    // Peak refresh rate card at top of Display & I/O group
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 30.dp,
                            topEnd = 30.dp,
                            bottomStart = 10.dp,
                            bottomEnd = 10.dp
                        ),
                        color = roles.s2,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Peak refresh rate",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = roles.onS
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                                    .clip(CircleShape)
                                    .background(roles.s4)
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val rates = listOf(0 to "Auto", 60 to "60", 90 to "90", 120 to "120")
                                rates.forEach { (hz, label) ->
                                    val selected = config.refreshRateHz == hz
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(CircleShape)
                                            .background(if (selected) roles.primary else Color.Transparent)
                                            .clickable { onSelectRefreshRate(hz) }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (selected) roles.onPrimary else roles.onSv
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Remaining I/O tweak items with 10.dp middle corners and 30.dp bottom corners
                    ioGroup.forEachIndexed { index, spec ->
                        val isLast = index == ioGroup.lastIndex
                        ExpressiveTweakRowItem(
                            spec = spec,
                            checked = spec.isChecked(config),
                            shape = RoundedCornerShape(
                                topStart = 10.dp,
                                topEnd = 10.dp,
                                bottomStart = if (isLast) 30.dp else 10.dp,
                                bottomEnd = if (isLast) 30.dp else 10.dp
                            ),
                            onToggle = { onUpdateTweak(spec.key, it) }
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// CUSTOM M3 EXPRESSIVE SWITCH (.sw from HTML lines 207-211)
// ============================================================================

@Composable
private fun ExpressiveHtmlSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val roles = LocalMonetRoles.current
    val thumbSize by animateDpAsState(
        targetValue = if (checked) 24.dp else 16.dp,
        animationSpec = spring(dampingRatio = 0.58f, stiffness = Spring.StiffnessMedium),
        label = "SwThumbSize"
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 22.dp else 6.dp,
        animationSpec = spring(dampingRatio = 0.58f, stiffness = Spring.StiffnessMedium),
        label = "SwThumbOffset"
    )
    val trackColor by animateColorAsState(
        targetValue = if (checked) roles.primary else roles.s4,
        label = "SwTrackColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (checked) roles.primary else roles.outline,
        label = "SwBorderColor"
    )
    val thumbColor by animateColorAsState(
        targetValue = if (checked) roles.onPrimary else roles.outline,
        label = "SwThumbColor"
    )

    Box(
        modifier = modifier
            .width(52.dp)
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(trackColor)
            .border(width = 2.dp, color = borderColor, shape = RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(thumbSize)
                .clip(CircleShape)
                .background(thumbColor)
        )
    }
}

@Composable
private fun ExpressiveQuickTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val roles = LocalMonetRoles.current
    val topStartBottomEnd by animateDpAsState(
        targetValue = if (checked) 40.dp else 30.dp,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "TileCornerA"
    )
    val topEndBottomStart by animateDpAsState(
        targetValue = if (checked) 18.dp else 30.dp,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "TileCornerB"
    )
    val iconCorner by animateDpAsState(
        targetValue = if (checked) 22.dp else 16.dp,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "TileIconCorner"
    )

    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(
            topStart = topStartBottomEnd,
            topEnd = topEndBottomStart,
            bottomEnd = topStartBottomEnd,
            bottomStart = topEndBottomStart
        ),
        color = if (checked) roles.primary else roles.s3,
        contentColor = if (checked) roles.onPrimary else roles.onS,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(iconCorner),
                color = if (checked) roles.onPrimary else roles.s4,
                contentColor = if (checked) roles.primary else roles.onSv,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Column {
                Text(
                    text = title,
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 11.sp,
                    color = if (checked) roles.onPrimary.copy(alpha = 0.78f) else roles.onSv
                )
            }
        }
    }
}

@Composable
private fun ExpressiveTweakGroupHeader(title: String, countLabel: String) {
    val roles = LocalMonetRoles.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = roles.primary
        )
        Text(
            text = countLabel.uppercase(Locale.US),
            fontFamily = JetBrainsMonoFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.5.sp,
            letterSpacing = 1.sp,
            color = roles.onSv
        )
    }
}

@Composable
private fun ExpressiveGroupedTweakList(
    items: List<TweakItemSpec>,
    config: TweakConfigEntity,
    onToggle: (TweakItemSpec, Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        items.forEachIndexed { index, spec ->
            val topRadius = if (index == 0) 30.dp else 10.dp
            val bottomRadius = if (index == items.lastIndex) 30.dp else 10.dp
            ExpressiveTweakRowItem(
                spec = spec,
                checked = spec.isChecked(config),
                shape = RoundedCornerShape(
                    topStart = topRadius,
                    topEnd = topRadius,
                    bottomStart = bottomRadius,
                    bottomEnd = bottomRadius
                ),
                onToggle = { onToggle(spec, it) }
            )
        }
    }
}

@Composable
private fun ExpressiveTweakRowItem(
    spec: TweakItemSpec,
    checked: Boolean,
    shape: RoundedCornerShape,
    onToggle: (Boolean) -> Unit
) {
    val roles = LocalMonetRoles.current
    val avatarCorner by animateDpAsState(
        targetValue = if (checked) 23.dp else 16.dp,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "AvatarCorner"
    )
    val avatarRotation by animateFloatAsState(
        targetValue = if (checked) 360f else 0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "AvatarRotation"
    )

    Surface(
        onClick = { onToggle(!checked) },
        shape = shape,
        color = roles.s2,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Morphing Avatar Icon Box (.av: 16.dp -> 50% circle + 360deg spin)
            Surface(
                shape = RoundedCornerShape(avatarCorner),
                color = if (checked) roles.primary else roles.sc,
                contentColor = if (checked) roles.onPrimary else roles.onSc,
                modifier = Modifier
                    .size(46.dp)
                    .rotate(avatarRotation)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = spec.icon,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = spec.title,
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.5.sp,
                    color = roles.onS
                )
                Text(
                    text = spec.subtitle,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 12.sp,
                    color = roles.onSv,
                    modifier = Modifier.padding(top = 2.dp)
                )
                AnimatedVisibility(visible = checked) {
                    Text(
                        text = spec.sysfsCode,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.5.sp,
                        color = roles.primary,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            ExpressiveHtmlSwitch(
                checked = checked,
                onCheckedChange = onToggle
            )
        }
    }
}

// ============================================================================
// TAB 3: CONSOLE PAGE (Matches HTML #p3: Sysfs stream card + Terminal .term)
// ============================================================================

@Composable
private fun ExpressiveConsolePage(
    logs: List<LogEntry>,
    backupFilePath: String,
    onClearLogs: () -> Unit
) {
    val roles = LocalMonetRoles.current
    val infiniteTransition = rememberInfiniteTransition(label = "CursorBlink")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CursorAlpha"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 14.dp,
            bottom = 136.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Sysfs stream Header Card
        item {
            Surface(
                shape = RoundedCornerShape(34.dp),
                color = roles.pc,
                contentColor = roles.onPc,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sysfs stream",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "snapshot · $backupFilePath",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.5.sp,
                            color = roles.onPc.copy(alpha = 0.72f),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    if (logs.isNotEmpty()) {
                        Surface(
                            onClick = onClearLogs,
                            shape = CircleShape,
                            color = roles.onPc.copy(alpha = 0.12f),
                            contentColor = roles.onPc
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DeleteSweep,
                                    contentDescription = "Clear logs",
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Clear",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Terminal Card (.term)
        item {
            Surface(
                shape = RoundedCornerShape(34.dp),
                color = roles.s1,
                border = BorderStroke(
                    width = 1.dp,
                    color = roles.onS.copy(alpha = 0.07f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // 3 Colored Window Dots (.dots)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(roles.err)
                        )
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(roles.tert)
                        )
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(roles.primary)
                        )
                    }

                    if (logs.isEmpty()) {
                        TerminalStreamLine(
                            status = "✓ APPLIED",
                            isSuccess = true,
                            time = "12:04:11",
                            param = "tcp_congestion_control",
                            command = "sysctl -w net.ipv4.tcp_congestion_control=bbr"
                        )
                        TerminalStreamLine(
                            status = "✓ APPLIED",
                            isSuccess = true,
                            time = "12:03:52",
                            param = "gms wake_lock",
                            command = "cmd appops set com.google.android.gms WAKE_LOCK ignore"
                        )
                        TerminalStreamLine(
                            status = "✗ SKIPPED",
                            isSuccess = false,
                            time = "12:03:40",
                            param = "adreno_idler",
                            command = "echo Y > /sys/module/adreno_idler/parameters/…"
                        )
                        TerminalStreamLine(
                            status = "✓ APPLIED",
                            isSuccess = true,
                            time = "12:03:12",
                            param = "touchboost",
                            command = "echo 0 > /sys/module/msm_performance/…/touchboost"
                        )
                    } else {
                        logs.forEach { entry ->
                            TerminalStreamLine(
                                status = if (entry.isSuccess) "✓ APPLIED" else "✗ SKIPPED",
                                isSuccess = entry.isSuccess,
                                time = entry.timestamp,
                                param = entry.displayName,
                                command = entry.command?.takeIf { c -> c.isNotBlank() } ?: "${entry.paramName}=${entry.paramValue}"
                            )
                        }
                    }

                    // Blinking terminal prompt cursor (❯ ▊)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            text = "❯",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = roles.primary
                        )
                        Box(
                            modifier = Modifier
                                .width(7.dp)
                                .height(13.dp)
                                .background(roles.primary.copy(alpha = cursorAlpha))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TerminalStreamLine(
    status: String,
    isSuccess: Boolean,
    time: String,
    param: String,
    command: String
) {
    val roles = LocalMonetRoles.current
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = status,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp,
                color = if (isSuccess) roles.primary else roles.err
            )
            Text(
                text = time,
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 11.5.sp,
                color = roles.onS
            )
            Text(
                text = param,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.5.sp,
                color = roles.onSv,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = "  $ $command",
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 11.sp,
            color = roles.onSv,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

// ============================================================================
// FLOATING PILL BOTTOM NAVIGATION (Matches HTML <nav><div class="in">)
// ============================================================================

@Composable
private fun ExpressiveFloatingBottomNav(
    currentTab: ExpressiveTab,
    logCount: Int,
    onTabSelected: (ExpressiveTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val roles = LocalMonetRoles.current
    Surface(
        shape = RoundedCornerShape(36.dp),
        color = roles.s3.copy(alpha = 0.90f),
        border = BorderStroke(
            width = 1.dp,
            color = roles.onS.copy(alpha = 0.08f)
        ),
        shadowElevation = 16.dp,
        tonalElevation = 6.dp,
        modifier = modifier
            .widthIn(max = 456.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExpressiveTab.entries.forEach { tab ->
                val selected = currentTab == tab
                val pillWidth by animateDpAsState(
                    targetValue = if (selected) 78.dp else 66.dp,
                    animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
                    label = "NavPillWidth"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTabSelected(tab) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Box(
                                modifier = Modifier
                                    .width(pillWidth)
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(17.dp))
                                    .background(if (selected) roles.primary else Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.icon,
                                    contentDescription = tab.label,
                                    tint = if (selected) roles.onPrimary else roles.onSv,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            if (tab == ExpressiveTab.CONSOLE && logCount > 0) {
                                Surface(
                                    shape = CircleShape,
                                    color = roles.err,
                                    contentColor = Color.White,
                                    modifier = Modifier.offset(x = 6.dp, y = (-4).dp)
                                ) {
                                    Text(
                                        text = if (logCount > 99) "99+" else logCount.toString(),
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.5.sp,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = tab.label,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = if (selected) roles.onS else roles.onSv
                        )
                    }
                }
            }
        }
    }
}
