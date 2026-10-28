package com.voltpower.ui

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
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.material.icons.filled.PushPin
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.voltpower.data.apps.InstalledAppItem
import com.voltpower.data.db.TweakConfigEntity
import com.voltpower.data.log.LogEntry
import com.voltpower.data.telemetry.DeviceTelemetry
import com.voltpower.ui.theme.JetBrainsMonoFontFamily
import com.voltpower.ui.theme.PlusJakartaSansFontFamily
import com.voltpower.ui.theme.SpaceGroteskFontFamily
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

// ============================================================================
// EXACT SVG PATHS FOR MATERIAL 3 EXPRESSIVE ORB & COOKIE SHAPES (viewBox 0 0 200 200)
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

internal data class MonetRolePalette(
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

internal val LocalMonetRoles = staticCompositionLocalOf {
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
    HYPEROS("HyperOS", Icons.Filled.DeveloperBoard, Icons.Filled.Memory),
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
    val hasUnappliedChanges by viewModel.hasUnappliedChanges.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val kernelSnapshot by viewModel.kernelSnapshot.collectAsState()
    val stagedKernelTweaks by viewModel.stagedKernelTweaks.collectAsState()
    val selectedHyperOsTemplate by viewModel.selectedHyperOsTemplate.collectAsState()
    val isBalanceScreenOffSaverActive by viewModel.isBalanceScreenOffSaverActive.collectAsState()
    val lastKernelActionBanner by viewModel.lastKernelActionBanner.collectAsState()


    var currentTab by rememberSaveable { mutableStateOf(ExpressiveTab.OVERVIEW) }
    var selectedSeed by rememberSaveable { mutableStateOf(MonetSeedPreset.MINT) }
    var showMonetPicker by rememberSaveable { mutableStateOf(false) }

    // Section 9: Live 2-second Temperature Watch polling while HyperOS Kernel tab is open
    LaunchedEffect(currentTab) {
        if (currentTab == ExpressiveTab.HYPEROS) {
            viewModel.refreshKernelSnapshot()
            while (currentTab == ExpressiveTab.HYPEROS) {
                kotlinx.coroutines.delay(2000)
                viewModel.refreshKernelSnapshot()
            }
        }
    }

    var heroVisualActive by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(config.isMasterApplied, telemetry.isRootGranted) {
        heroVisualActive = config.isMasterApplied && telemetry.isRootGranted
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
                                            if (telemetry.isRootGranted) {
                                                heroVisualActive = nextState
                                            }
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
                                        hasUnappliedChanges = hasUnappliedChanges,
                                        installedApps = installedApps,
                                        onSelectProfile = { viewModel.onExploreProfileInTweaks(it) },
                                        onSelectPerformanceSubMode = { viewModel.onExplorePerformanceSubModeInTweaks(it) },
                                        onSetAllCurrentProfileTweaks = { viewModel.onSetAllCurrentProfileTweaks(it) },
                                        onUpdateTweak = { key, enabled -> viewModel.onUpdateTweak(key, enabled) },
                                        onToggleHyperOs = { viewModel.onToggleHyperOsPowerSaver(it) },
                                        onSelectRefreshRate = { viewModel.onSelectRefreshRate(it) },
                                        onUpdateCoreTopology = { cores, threshold ->
                                            viewModel.onUpdateCoreTopology(cores, threshold)
                                        },
                                        onUpdateBalanceCpuCap = { percent ->
                                            viewModel.onUpdateBalanceCpuCap(percent)
                                        },
                                        onUpdateBackgroundAllowedApps = { csv ->
                                            val set = csv.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
                                            viewModel.onUpdateBackgroundAllowedApps(set)
                                        },
                                        onUpdateDeepSleepWhitelist = { csv ->
                                            viewModel.onUpdateDeepSleepWhitelist(csv)
                                        }
                                    )
                                }

                                ExpressiveTab.HYPEROS -> {
                                    val resolvedTemplate = selectedHyperOsTemplate
                                        ?: com.voltpower.data.root.HyperOsProfileTemplate.entries.find {
                                            it.id == config.hyperOsActiveTemplate
                                        }
                                    HyperOsKernelTabScreen(
                                        snapshot = kernelSnapshot,
                                        stagedTweaks = stagedKernelTweaks,
                                        selectedTemplate = resolvedTemplate,
                                        isBalanceScreenOffSaverActive = isBalanceScreenOffSaverActive || config.hyperOsBalanceScreenOffArmed,
                                        lastActionBanner = lastKernelActionBanner,
                                        installedApps = installedApps,
                                        freezeCheckedAppsRaw = config.hyperOsFreezeCheckedApps,
                                        backgroundAllowedAppsCsv = config.backgroundAllowedApps,
                                        onSelectTemplate = { template ->
                                            viewModel.onSelectHyperOsTemplate(template)
                                        },
                                        onStageTweakValue = { id, value ->
                                            viewModel.onStageKernelTweak(id, value)
                                        },
                                        onRunSingleLineNow = { spec, value ->
                                            viewModel.onRunSingleKernelLine(spec, value)
                                        },
                                        onUndoSingleLineToWas = { spec ->
                                            viewModel.onUndoSingleKernelLine(spec)
                                        },
                                        onSwipeApplyStaged = {
                                            viewModel.onSwipeApplyStagedKernelTweaks()
                                        },
                                        onRestoreAllWasValues = {
                                            viewModel.onRestoreAllWasKernelValues()
                                        },
                                        onRefreshSnapshot = {
                                            viewModel.refreshKernelSnapshot()
                                        },
                                        onRunOneShotVmAction = { dropCaches ->
                                            viewModel.onRunOneShotVmAction(dropCaches)
                                        },
                                        onToggleDaemon = { name, start ->
                                            viewModel.onToggleXiaomiDaemon(name, start)
                                        },
                                        onUpdateFreezeCheckedApps = { checkedSet, selectAllUserEnabled ->
                                            viewModel.onUpdateHyperOsFreezeCheckedApps(
                                                checkedApps = checkedSet,
                                                selectAllUserEnabled = selectAllUserEnabled
                                            )
                                        },
                                        onUpdateBackgroundAllowedApps = { csv ->
                                            val set = csv
                                                .split(",")
                                                .map { it.trim() }
                                                .filter { it.isNotEmpty() }
                                                .toSet()
                                            viewModel.onUpdateBackgroundAllowedApps(set)
                                        },
                                        onExecuteProcessAction = { pkg, action ->
                                            viewModel.onExecuteProcessKernelAction(pkg, action)
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

                // Floating "Swipe to Apply" + "Revert stock" Dock on Tweaks Tab
                AnimatedVisibility(
                    visible = currentTab == ExpressiveTab.TWEAKS,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .widthIn(max = 480.dp)
                        .padding(start = 16.dp, end = 16.dp, bottom = 104.dp)
                ) {
                    ExpressiveSwipeToApplyDock(
                        config = config,
                        hasUnappliedChanges = hasUnappliedChanges,
                        onSwipeApply = { viewModel.onSwipeToApplyTweaks() },
                        onRevertStock = { viewModel.onRevertToStock() }
                    )
                }

                // Floating Glassmorphic Pill Bottom Navigation (nav .in from HTML lines 227-234)
                ExpressiveFloatingBottomNav(
                    currentTab = currentTab,
                    logCount = logs.size,
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
                    text = "HyperVolt",
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
                        if (isRootGranted) {
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
                        } else {
                            // No root permission: draw static non-green error indicator dot
                            drawCircle(
                                color = roles.err,
                                radius = dotRadius,
                                center = center
                            )
                        }
                    }
                    Text(
                        text = if (isRootGranted) "root · volatile RAM" else "no root · read-only",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.5.sp,
                        color = if (isRootGranted) roles.onSv else roles.err
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
                        val batteryHeadline = when {
                            telemetry.isCharging && watts >= 15f -> "Charging fast"
                            telemetry.isCharging -> "Charging"
                            isEngineActive -> "Active · Discharging"
                            else -> "Discharging"
                        }
                        Text(
                            text = batteryHeadline,
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
                        val orbSubtitle = if (telemetry.isCharging) {
                            val minsToFull = ((100 - displayLevel).coerceAtLeast(1) * 1.9f).roundToInt()
                            "≈ $minsToFull min to full"
                        } else {
                            "Battery remaining"
                        }
                        Text(
                            text = orbSubtitle,
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
                        val normalizedSubMode = when (performanceSubMode.uppercase()) {
                            "HEAVY", "EXTREME" -> "EXTREME"
                            "ULTRA" -> "ULTRA"
                            else -> "LITE"
                        }
                        val tiers = listOf(
                            Triple("LITE", "Lite", "smooth"),
                            Triple("EXTREME", "Extreme", "gaming"),
                            Triple("ULTRA", "Ultra", "max lock")
                        )
                        tiers.forEach { (key, title, sub) ->
                            val isSelected = normalizedSubMode == key
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
    val hasRoot = telemetry.isRootGranted

    val gpuMhz = if (telemetry.gpuFreqMhz > 0) {
        telemetry.gpuFreqMhz.toString()
    } else if (hasRoot) {
        Regex("(\\d+)\\s*MHz", RegexOption.IGNORE_CASE)
            .find(telemetry.gpuFreqInfo)?.groupValues?.getOrNull(1)
            ?.takeIf { it != "0" } ?: "—"
    } else {
        "—"
    }

    val storageUsedStr = if (telemetry.storageTotalGb > 0f) {
        if (telemetry.storageUsedGb >= 100f) {
            String.format(Locale.US, "%.0f", telemetry.storageUsedGb)
        } else {
            String.format(Locale.US, "%.1f", telemetry.storageUsedGb)
        }
    } else {
        "—"
    }

    val storageTotalStr = if (telemetry.storageTotalGb > 0f) {
        val roundedTotal = telemetry.storageTotalGb.roundToInt()
        if (abs(telemetry.storageTotalGb - roundedTotal) < 0.15f || telemetry.storageTotalGb >= 32f) {
            "/$roundedTotal GB"
        } else {
            String.format(Locale.US, "/%.1f GB", telemetry.storageTotalGb)
        }
    } else {
        " GB"
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Span-2 Card: Octa-core · Real 8-Core Usage Equalizer (Gated by Root Permission)
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
                        text = if (hasRoot) "OCTA-CORE · LIVE USAGE" else "OCTA-CORE · NO ROOT",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.5.sp,
                        letterSpacing = 1.sp,
                        color = if (hasRoot) roles.onSv else roles.err
                    )
                    if (!hasRoot) {
                        Surface(
                            shape = CircleShape,
                            color = roles.err.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = "ROOT REQUIRED",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.5.sp,
                                color = roles.err,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Filled.DeveloperBoard,
                            contentDescription = null,
                            tint = roles.onSv.copy(alpha = 0.6f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // 8-Bar Equalizer calculated strictly from the 8 cores' real usage when root is granted
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .height(96.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    for (idx in 0..7) {
                        val core = telemetry.cpuCores.getOrNull(idx)
                        val targetUsage = if (
                            hasRoot &&
                            core != null &&
                            core.isOnline &&
                            core.maxFreqMhz > 0 &&
                            core.currentFreqMhz > 0
                        ) {
                            (core.currentFreqMhz.toFloat() / core.maxFreqMhz.toFloat()).coerceIn(0.08f, 1.0f)
                        } else {
                            0.06f
                        }

                        val animatedHeight by animateFloatAsState(
                            targetValue = targetUsage,
                            animationSpec = spring(
                                dampingRatio = 0.72f,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            label = "CoreUsageBar_$idx"
                        )

                        val barColor = when {
                            !hasRoot -> roles.s4
                            core == null || !core.isOnline -> roles.s4
                            idx < 4 -> roles.primary
                            else -> roles.tert
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(animatedHeight)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 10.dp,
                                        topEnd = 10.dp,
                                        bottomStart = 6.dp,
                                        bottomEnd = 6.dp
                                    )
                                )
                                .background(barColor)
                        )
                    }
                }

                // Core indices 0..7 + per-core usage % (.eql)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (i in 0..7) {
                        val core = telemetry.cpuCores.getOrNull(i)
                        val pctText = if (
                            hasRoot &&
                            core != null &&
                            core.isOnline &&
                            core.maxFreqMhz > 0 &&
                            core.currentFreqMhz > 0
                        ) {
                            val pct = ((core.currentFreqMhz.toFloat() / core.maxFreqMhz.toFloat()) * 100f)
                                .roundToInt()
                                .coerceIn(1, 100)
                            "${pct}%"
                        } else if (hasRoot && core != null && !core.isOnline) {
                            "OFF"
                        } else {
                            "—"
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "C$i",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 9.5.sp,
                                color = roles.onSv,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = pctText,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.5.sp,
                                color = if (hasRoot) roles.primary else roles.onSv.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Governor Pills (.gov)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (hasRoot) {
                        val littleCore = telemetry.cpuCores.firstOrNull { it.coreIndex < 4 }
                        val bigCore = telemetry.cpuCores.lastOrNull()
                        val littleMhz = littleCore?.currentFreqMhz ?: 0
                        val bigMhz = bigCore?.currentFreqMhz ?: 0
                        GovernorPill(
                            text = telemetry.activeGovernor.ifBlank { "schedutil" },
                            highlighted = true
                        )
                        GovernorPill(text = "LITTLE ${littleMhz}MHz", highlighted = false)
                        GovernorPill(text = "BIG ${bigMhz}MHz", highlighted = false)
                    } else {
                        GovernorPill(
                            text = "Grant root permission for 8-core usage graph",
                            highlighted = false
                        )
                    }
                }
            }
        }

        // Row 1 of 2x2 Bento: GPU (.b.p) + STORAGE (.b.t)
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
                        text = telemetry.gpuSubtitle,
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.5.sp,
                        color = roles.onPc.copy(alpha = 0.75f),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            // STORAGE Card (.b.t) — Replaces ZRAM, shows Used / Total Storage
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
                            text = "STORAGE",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.5.sp,
                            letterSpacing = 1.sp,
                            color = roles.onTc.copy(alpha = 0.7f)
                        )
                        Icon(
                            imageVector = Icons.Filled.Storage,
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
                            text = storageUsedStr,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp,
                            letterSpacing = (-1.2).sp
                        )
                        Text(
                            text = storageTotalStr,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = roles.onTc.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 3.dp, start = 3.dp)
                        )
                    }
                    Text(
                        text = "${telemetry.storageUsedPercent}% used",
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
                    text = "Applied activity",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = roles.onS
                )
                Surface(
                    shape = CircleShape,
                    color = if (recentLogs.isNotEmpty()) roles.pc else roles.s4,
                    contentColor = if (recentLogs.isNotEmpty()) roles.onPc else roles.onSv
                ) {
                    Text(
                        text = if (recentLogs.isNotEmpty()) "${recentLogs.size} applied" else "idle",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }

            if (recentLogs.isEmpty()) {
                Text(
                    text = "No tweaks applied yet · Toggle a profile or tweak to log activity.",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.5.sp,
                    color = roles.onSv,
                    modifier = Modifier.padding(top = 14.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
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
                        recentLogs.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(roles.primary)
                                        .border(2.dp, roles.s2, CircleShape)
                                )
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "APPLIED",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = roles.primary
                                        )
                                        Text(
                                            text = item.displayName,
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            color = roles.onS,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = item.timestamp,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.5.sp,
                                        color = roles.onSv,
                                        modifier = Modifier.padding(start = 8.dp)
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
// TAB 2: TWEAKS PAGE (Connected to Power Profile: Powersave / Balance / Performance)
// ============================================================================

@Composable
private fun ExpressiveTweaksPage(
    config: TweakConfigEntity,
    hasUnappliedChanges: Boolean,
    installedApps: List<InstalledAppItem>,
    onSelectProfile: (String) -> Unit,
    onSelectPerformanceSubMode: (String) -> Unit,
    onSetAllCurrentProfileTweaks: (Boolean) -> Unit,
    onUpdateTweak: (String, Boolean) -> Unit,
    onToggleHyperOs: (Boolean) -> Unit,
    onSelectRefreshRate: (Int) -> Unit,
    onUpdateCoreTopology: (Int, Int) -> Unit,
    onUpdateBalanceCpuCap: (Int) -> Unit,
    onUpdateBackgroundAllowedApps: (String) -> Unit,
    onUpdateDeepSleepWhitelist: (String) -> Unit
) {
    val roles = LocalMonetRoles.current
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var sliderThreshold by remember(config.offlineBatteryThreshold) {
        mutableFloatStateOf(config.offlineBatteryThreshold.toFloat())
    }
    var balanceCapSlider by remember(config.balanceCpuCapPercent) {
        mutableFloatStateOf(config.balanceCpuCapPercent.toFloat())
    }

    val activeProfile = config.activeProfile
    val normalizedPerfTier = when (config.performanceSubMode.uppercase()) {
        "HEAVY", "EXTREME" -> "EXTREME"
        "ULTRA" -> "ULTRA"
        else -> "LITE"
    }

    // 1. Powersave Profile Tweaks (Powersaving tweaks only + 1 unified HyperOS Power Saver card)
    val powersaveCpuTweaks = remember {
        listOf(
            TweakItemSpec(
                key = "cpuFreqCap",
                category = TweakCategoryFilter.CPU,
                title = "Limit peak frequencies",
                subtitle = "Cap big & prime clusters to 50% max clock",
                sysfsCode = "",
                icon = Icons.Filled.Speed,
                isChecked = { it.cpuFreqCapEnabled }
            ),
            TweakItemSpec(
                key = "schedutilGov",
                category = TweakCategoryFilter.CPU,
                title = "Powersave energy governor",
                subtitle = "Prioritize lowest OPP clock states",
                sysfsCode = "",
                icon = Icons.Filled.GraphicEq,
                isChecked = { it.schedutilGovernorEnabled }
            ),
            TweakItemSpec(
                key = "schedutilRate",
                category = TweakCategoryFilter.CPU,
                title = "Powersave governor rate limits",
                subtitle = "8ms up / 32ms down ramp, 99% hispeed load",
                sysfsCode = "",
                icon = Icons.Filled.Speed,
                isChecked = { it.schedutilRateLimitsEnabled }
            ),
            TweakItemSpec(
                key = "inputTouchBoost",
                category = TweakCategoryFilter.CPU,
                title = "Disable touch boost",
                subtitle = "No max-clock spike on every swipe",
                sysfsCode = "",
                icon = Icons.Filled.Visibility,
                isChecked = { it.inputTouchBoostDisabled }
            ),
            TweakItemSpec(
                key = "gpuPowerLimit",
                category = TweakCategoryFilter.CPU,
                title = "GPU power limiter",
                subtitle = "Throttle Adreno clock & power level",
                sysfsCode = "",
                icon = Icons.Filled.DeveloperBoard,
                isChecked = { it.gpuPowerLimitEnabled }
            ),
            TweakItemSpec(
                key = "adrenoIdler",
                category = TweakCategoryFilter.CPU,
                title = "Adreno idler",
                subtitle = "Lowest clock when frame queue idles",
                sysfsCode = "",
                icon = Icons.Filled.AcUnit,
                isChecked = { it.adrenoIdlerEnabled }
            )
        )
    }

    val powersaveDozeTweaks = remember {
        listOf(
            TweakItemSpec(
                key = "gmsDoze",
                category = TweakCategoryFilter.BAT,
                title = "Aggressive GMS doze",
                subtitle = "Restrict Play Services wake locks in standby",
                sysfsCode = "",
                icon = Icons.Filled.DarkMode,
                isChecked = { it.gmsDozeEnabled }
            ),
            TweakItemSpec(
                key = "lpmSleep",
                category = TweakCategoryFilter.BAT,
                title = "Kernel LPM deep sleep",
                subtitle = "Low-power cluster sleep when screen off",
                sysfsCode = "",
                icon = Icons.Filled.Eco,
                isChecked = { it.lpmSleepEnabled }
            ),
            TweakItemSpec(
                key = "powerEfficientWorkqueue",
                category = TweakCategoryFilter.BAT,
                title = "Power-efficient workqueues",
                subtitle = "Pin kernel background jobs to LITTLE cluster",
                sysfsCode = "",
                icon = Icons.Filled.DeveloperBoard,
                isChecked = { it.powerEfficientWorkqueueEnabled }
            ),
            TweakItemSpec(
                key = "schedtuneTopApp",
                category = TweakCategoryFilter.BAT,
                title = "Restrict background cpuset",
                subtitle = "Confine background tasks to cores 0-3",
                sysfsCode = "",
                icon = Icons.Filled.Memory,
                isChecked = { it.schedtuneTopAppEnabled }
            ),
            TweakItemSpec(
                key = "backgroundScanning",
                category = TweakCategoryFilter.BAT,
                title = "Disable Wi-Fi & BLE background scan",
                subtitle = "Stop hidden beacon scans when idle",
                sysfsCode = "",
                icon = Icons.Filled.Wifi,
                isChecked = { it.backgroundScanningDisabled }
            )
        )
    }

    val powersaveIoTweaks = remember {
        listOf(
            TweakItemSpec(
                key = "storageIo",
                category = TweakCategoryFilter.IO,
                title = "Optimise UFS I/O queue",
                subtitle = "128 KB read-ahead, queue depth 64",
                sysfsCode = "",
                icon = Icons.Filled.Storage,
                isChecked = { it.storageIoQueueEnabled }
            ),
            TweakItemSpec(
                key = "dynamicFsync",
                category = TweakCategoryFilter.IO,
                title = "Dynamic Fsync batching",
                subtitle = "Defer disk sync commits while screen is active",
                sysfsCode = "",
                icon = Icons.Filled.Storage,
                isChecked = { it.dynamicFsyncEnabled }
            ),
            TweakItemSpec(
                key = "vmDirtyWriteback",
                category = TweakCategoryFilter.IO,
                title = "Defer VM dirty writeback",
                subtitle = "30s batch flushes, fewer flash wakeups",
                sysfsCode = "",
                icon = Icons.Filled.Memory,
                isChecked = { it.vmDirtyWritebackEnabled }
            ),
            TweakItemSpec(
                key = "controlCenterBlur",
                category = TweakCategoryFilter.IO,
                title = "Remove notification blur",
                subtitle = "Disable real-time GPU window blur on notification shade to save battery",
                sysfsCode = "",
                icon = Icons.Filled.Contrast,
                isChecked = { it.controlCenterBlurEnabled }
            )
        )
    }

    // 1b. HyperOS Battery Saver Tweaks (Shown ONLY when 'HyperOS Battery saver' is enabled, replacing default powersave tweaks)
    val hyperOsTweaks = remember {
        listOf(
            TweakItemSpec(
                key = "systemBatterySaver",
                category = TweakCategoryFilter.BAT,
                title = "HyperOS PowerKeeper SuperSave",
                subtitle = "Extreme standby mode, Audio HAL & Wi-Fi SISO antenna save",
                sysfsCode = "",
                icon = Icons.Filled.Bolt,
                isChecked = { it.systemBatterySaverEnabled }
            ),
            TweakItemSpec(
                key = "hyperOsAurogonFreezer",
                category = TweakCategoryFilter.BAT,
                title = "Aurogon process freezer",
                subtitle = "Freeze background cached processes in standby",
                sysfsCode = "",
                icon = Icons.Filled.AcUnit,
                isChecked = { it.hyperOsAurogonFreezerEnabled }
            ),
            TweakItemSpec(
                key = "hyperOsSuperPowerClean",
                category = TweakCategoryFilter.BAT,
                title = "SuperPowerClean RAM purge",
                subtitle = "Stop unwhitelisted 3rd-party background tasks",
                sysfsCode = "",
                icon = Icons.Filled.Memory,
                isChecked = { it.hyperOsSuperPowerCleanEnabled }
            ),
            TweakItemSpec(
                key = "hyperOsAodDisabled",
                category = TweakCategoryFilter.BAT,
                title = "Disable Always-On Display (AOD)",
                subtitle = "Turn off ambient display during power saving",
                sysfsCode = "",
                icon = Icons.Filled.DarkMode,
                isChecked = { it.hyperOsAodDisabled }
            ),
            TweakItemSpec(
                key = "hyperOsTouchBoostDisabled",
                category = TweakCategoryFilter.CPU,
                title = "Clamp Xiaomi touch & launch boost",
                subtitle = "Eliminate app-launch & scroll frequency spikes",
                sysfsCode = "",
                icon = Icons.Filled.Speed,
                isChecked = { it.hyperOsTouchBoostDisabled }
            ),
            TweakItemSpec(
                key = "touchDozeMode",
                category = TweakCategoryFilter.CPU,
                title = "Xiaomi THP touch doze mode",
                subtitle = "Low-power touch panel scanning when idle",
                sysfsCode = "",
                icon = Icons.Filled.Eco,
                isChecked = { it.touchDozeModeEnabled }
            ),
            TweakItemSpec(
                key = "hyperOsFiveGPowerOpt",
                category = TweakCategoryFilter.BAT,
                title = "5G smart modem power saving",
                subtitle = "Optimize 5G cellular radio power consumption",
                sysfsCode = "",
                icon = Icons.Filled.Wifi,
                isChecked = { it.hyperOsFiveGPowerOptEnabled }
            ),
            TweakItemSpec(
                key = "backgroundScanning",
                category = TweakCategoryFilter.BAT,
                title = "Disable Wi-Fi & BLE background scan",
                subtitle = "Stop hidden beacon scans when idle",
                sysfsCode = "",
                icon = Icons.Filled.Wifi,
                isChecked = { it.backgroundScanningDisabled }
            ),
            TweakItemSpec(
                key = "hyperOsLock60Hz",
                category = TweakCategoryFilter.IO,
                title = "Lock 60Hz MIUI refresh rate",
                subtitle = "Clamp display refresh rate to 60Hz",
                sysfsCode = "",
                icon = Icons.Filled.Tune,
                isChecked = { it.hyperOsLock60HzEnabled }
            ),
            TweakItemSpec(
                key = "controlCenterBlur",
                category = TweakCategoryFilter.BAT,
                title = "Remove notification blur",
                subtitle = "Disable real-time GPU window blur on notification shade to save battery",
                sysfsCode = "",
                icon = Icons.Filled.Contrast,
                isChecked = { it.controlCenterBlurEnabled }
            ),
            TweakItemSpec(
                key = "batterySaverNoDark",
                category = TweakCategoryFilter.BAT,
                title = "Enable system battery saver",
                subtitle = "Activate Android & HyperOS low power mode",
                sysfsCode = "",
                icon = Icons.Filled.Eco,
                isChecked = { it.systemBatterySaverWithoutDarkEnabled }
            ),
            TweakItemSpec(
                key = "systemDarkMode",
                category = TweakCategoryFilter.BAT,
                title = "Force AMOLED dark mode",
                subtitle = "Enable system-wide dark theme for OLED power saving",
                sysfsCode = "",
                icon = Icons.Filled.DarkMode,
                isChecked = { it.systemDarkModeEnabled }
            ),
            TweakItemSpec(
                key = "hyperOsUltraSuperSaveUi",
                category = TweakCategoryFilter.BAT,
                title = "Ultra SuperSave dark wallpaper & panel off",
                subtitle = "Dark simple wallpaper+ turn off notification panel & few control center options.",
                sysfsCode = "",
                icon = Icons.Filled.Visibility,
                isChecked = { it.hyperOsUltraSuperSaveUiEnabled }
            )
        )
    }

    // 2. Balance Profile Tweaks (Balance tweaks only)
    val balanceTweaks = remember {
        listOf(
            TweakItemSpec(
                key = "balanceCpuTweaks",
                category = TweakCategoryFilter.CPU,
                title = "Balanced CPU & asymmetric Schedutil",
                subtitle = "Little 500us/20ms · Big 500us/10ms fluid response",
                sysfsCode = "",
                icon = Icons.Filled.Speed,
                isChecked = { it.balanceCpuTweaksEnabled }
            ),
            TweakItemSpec(
                key = "balanceCfsScheduler",
                category = TweakCategoryFilter.CPU,
                title = "Balanced CFS scheduler tuning",
                subtitle = "Low-latency task wakeup & smooth multitasking",
                sysfsCode = "",
                icon = Icons.Filled.GraphicEq,
                isChecked = { it.balanceCfsSchedulerEnabled }
            ),
            TweakItemSpec(
                key = "balanceRamScaling",
                category = TweakCategoryFilter.BAT,
                title = "Dynamic 3-tier RAM & ZRAM scaling",
                subtitle = "Adaptive memory swappiness, dirty ratio & cache pressure",
                sysfsCode = "",
                icon = Icons.Filled.Memory,
                isChecked = { it.balanceRamScalingEnabled }
            ),
            TweakItemSpec(
                key = "balanceGpuOptimization",
                category = TweakCategoryFilter.CPU,
                title = "Balanced GPU DVFS optimization",
                subtitle = "80ms idle timer, nap sleep enabled, dynamic scaling",
                sysfsCode = "",
                icon = Icons.Filled.DeveloperBoard,
                isChecked = { it.balanceGpuOptimizationEnabled }
            ),
            TweakItemSpec(
                key = "balanceStorageIo",
                category = TweakCategoryFilter.IO,
                title = "Adaptive UFS storage I/O",
                subtitle = "Kyber/BFQ scheduler priority & RAM-scaled read-ahead",
                sysfsCode = "",
                icon = Icons.Filled.Storage,
                isChecked = { it.balanceStorageIoEnabled }
            ),
            TweakItemSpec(
                key = "balanceNetworkBbr",
                category = TweakCategoryFilter.IO,
                title = "Balanced TCP BBR & Fast Open",
                subtitle = "BBR congestion control + TCP Fast Open",
                sysfsCode = "",
                icon = Icons.Filled.Wifi,
                isChecked = { it.balanceNetworkBbrEnabled }
            ),
            TweakItemSpec(
                key = "deepSleepScreenOff",
                category = TweakCategoryFilter.BAT,
                title = "Screen-off deep sleep daemon",
                subtitle = "Force deep doze after ${config.deepSleepDelayMinutes}m screen off",
                sysfsCode = "",
                icon = Icons.Filled.DarkMode,
                isChecked = { it.deepSleepScreenOffEnabled }
            ),
            TweakItemSpec(
                key = "developerProcessLimit",
                category = TweakCategoryFilter.BAT,
                title = "Cached background process limit",
                subtitle = "Cap cached background processes to ${config.developerProcessLimit}",
                sysfsCode = "",
                icon = Icons.Filled.Tune,
                isChecked = { it.developerProcessLimitEnabled }
            )
        )
    }

    // 3. Performance Profile Tweaks (Performance tweaks only, connected to Lite / Extreme / Ultra)
    val performanceTweaks = remember(normalizedPerfTier) {
        val govSub = when (normalizedPerfTier) {
            "ULTRA" -> "Ultra: Performance governor lock at 100% min/max clock"
            "EXTREME" -> "Extreme: Schedutil with 45% clock floor & 400us fast ramp"
            else -> "Lite: Responsive schedutil with 500us ramp & full headroom"
        }
        val coresSub = when (normalizedPerfTier) {
            "ULTRA" -> "Ultra: All 8 cores online + core parking disabled"
            else -> "$normalizedPerfTier: Force all 8 CPU cores online"
        }
        val stuneSub = when (normalizedPerfTier) {
            "ULTRA" -> "Ultra: Top-app 50% boost, Foreground 50%"
            "EXTREME" -> "Extreme: Top-app 45% boost, Foreground 35%"
            else -> "Lite: Top-app 25% boost, Foreground 20% smooth UI"
        }
        val gpuSub = when (normalizedPerfTier) {
            "ULTRA" -> "Ultra: AdrenoBoost 3, 1050ms idle, force rail/clk/bus ON"
            "EXTREME" -> "Extreme: AdrenoBoost 2, 80ms idle, throttling bypass"
            else -> "Lite: AdrenoBoost 1, 50ms idle, Adreno idler off"
        }
        val busSub = when (normalizedPerfTier) {
            "ULTRA" -> "Ultra: All CPU/GPU/DDR memory buses locked to performance"
            "EXTREME" -> "Extreme: CPU & GPU memory bandwidth set to performance"
            else -> "Lite: Dynamic DDR memory bus scaling"
        }
        val touchSub = when (normalizedPerfTier) {
            "ULTRA" -> "Ultra: 256ms ultra-low-latency input & touch boost"
            else -> "$normalizedPerfTier: 128ms responsive touch boost"
        }

        listOf(
            TweakItemSpec(
                key = "perfGovLock",
                category = TweakCategoryFilter.CPU,
                title = "CPU governor & clock boost",
                subtitle = govSub,
                sysfsCode = "",
                icon = Icons.Filled.RocketLaunch,
                isChecked = { it.perfCpuGovernorLockEnabled }
            ),
            TweakItemSpec(
                key = "perfAllCores",
                category = TweakCategoryFilter.CPU,
                title = "Force all 8 CPU cores online",
                subtitle = coresSub,
                sysfsCode = "",
                icon = Icons.Filled.DeveloperBoard,
                isChecked = { it.perfAllCoresOnlineEnabled }
            ),
            TweakItemSpec(
                key = "perfSchedtune",
                category = TweakCategoryFilter.CPU,
                title = "SchedTune & uclamp thread boost",
                subtitle = stuneSub,
                sysfsCode = "",
                icon = Icons.Filled.Speed,
                isChecked = { it.perfSchedtuneBoostEnabled }
            ),
            TweakItemSpec(
                key = "perfTouchBoost",
                category = TweakCategoryFilter.CPU,
                title = "Touch & input boost acceleration",
                subtitle = touchSub,
                sysfsCode = "",
                icon = Icons.Filled.Bolt,
                isChecked = { it.perfTouchBoostEnabled }
            ),
            TweakItemSpec(
                key = "perfGpuBoost",
                category = TweakCategoryFilter.CPU,
                title = "Adreno GPU boost & unthrottle",
                subtitle = gpuSub,
                sysfsCode = "",
                icon = Icons.Filled.GraphicEq,
                isChecked = { it.perfGpuAdrenoBoostEnabled }
            ),
            TweakItemSpec(
                key = "perfGpuNoNap",
                category = TweakCategoryFilter.CPU,
                title = "GPU force no-nap",
                subtitle = "Prevent GPU micro-sleep between frame draws",
                sysfsCode = "",
                icon = Icons.Filled.Visibility,
                isChecked = { it.perfGpuNoNapEnabled }
            ),
            TweakItemSpec(
                key = "perfDdrBus",
                category = TweakCategoryFilter.IO,
                title = "DDR memory bus bandwidth boost",
                subtitle = busSub,
                sysfsCode = "",
                icon = Icons.Filled.Memory,
                isChecked = { it.perfDdrBusBoostEnabled }
            ),
            TweakItemSpec(
                key = "perfStorage512",
                category = TweakCategoryFilter.IO,
                title = "512KB UFS multi-queue boost",
                subtitle = "512KB read-ahead, 256 queue depth, zero stats overhead",
                sysfsCode = "",
                icon = Icons.Filled.Storage,
                isChecked = { it.perfStorageQueue512Enabled }
            ),
            TweakItemSpec(
                key = "perfLmkTuning",
                category = TweakCategoryFilter.IO,
                title = "Gaming RAM free-pool & LMK tuning",
                subtitle = "Expanded free RAM pool headroom, 75% swappiness, 34 bg apps",
                sysfsCode = "",
                icon = Icons.Filled.Tune,
                isChecked = { it.perfLmkTuningEnabled }
            )
        )
    }

    val q = searchQuery.trim().lowercase(Locale.US)
    fun List<TweakItemSpec>.filterByQuery(): List<TweakItemSpec> {
        if (q.isEmpty()) return this
        return filter {
            it.title.lowercase(Locale.US).contains(q) ||
                it.subtitle.lowercase(Locale.US).contains(q) ||
                it.sysfsCode.lowercase(Locale.US).contains(q)
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
            val profileSearchLabel = when (activeProfile) {
                "PERFORMANCE" -> "Search Performance ($normalizedPerfTier) tweaks"
                "BALANCE" -> "Search Balance tweaks"
                else -> "Search Powersave & HyperOS tweaks"
            }
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
                                text = profileSearchLabel,
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

        // 2. Explore & Stage Power Profile Selector Bar (Explore without auto-applying)
        item {
            Surface(
                shape = RoundedCornerShape(30.dp),
                color = roles.s2,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "EXPLORE & STAGE TWEAKS",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            letterSpacing = 1.sp,
                            color = roles.primary
                        )
                        Surface(
                            shape = CircleShape,
                            color = if (hasUnappliedChanges) roles.pc else roles.s4,
                            contentColor = if (hasUnappliedChanges) roles.onPc else roles.onSv
                        ) {
                            Text(
                                text = if (hasUnappliedChanges) "UNAPPLIED · SWIPE BELOW" else "EXPLORE FREELY",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.5.sp,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clip(CircleShape)
                            .background(roles.s4)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val profiles = listOf(
                            "POWERSAVE" to "Powersave",
                            "BALANCE" to "Balance",
                            "PERFORMANCE" to "Performance"
                        )
                        profiles.forEach { (key, label) ->
                            val selected = activeProfile == key
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .background(if (selected) roles.primary else Color.Transparent)
                                    .clickable { onSelectProfile(key) }
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

                    // Quick Select All / Uncheck All row for easy independent exploration
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Toggle any tweak freely, then swipe below to apply",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 11.5.sp,
                            color = roles.onSv,
                            modifier = Modifier.weight(1f)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                onClick = { onSetAllCurrentProfileTweaks(true) },
                                shape = CircleShape,
                                color = roles.s4,
                                contentColor = roles.onS
                            ) {
                                Text(
                                    text = "Select all",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                            Surface(
                                onClick = { onSetAllCurrentProfileTweaks(false) },
                                shape = CircleShape,
                                color = roles.s4,
                                contentColor = roles.onSv
                            ) {
                                Text(
                                    text = "Uncheck all",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ====================================================================
        // PROFILE 1: POWERSAVE TWEAKS + HYPEROS BATTERY SAVER
        // ====================================================================
        if (activeProfile == "POWERSAVE") {
            // Master Toggle Card for HyperOS Battery Saver
            item {
                ExpressiveTweakGroupHeader(
                    title = "HyperOS battery saver",
                    countLabel = if (config.hyperOsPowerSaverEnabled) "enabled" else "disabled"
                )
                Surface(
                    onClick = { onToggleHyperOs(!config.hyperOsPowerSaverEnabled) },
                    shape = RoundedCornerShape(30.dp),
                    color = if (config.hyperOsPowerSaverEnabled) roles.pc else roles.s2,
                    contentColor = if (config.hyperOsPowerSaverEnabled) roles.onPc else roles.onS,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hyperos_unified_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(if (config.hyperOsPowerSaverEnabled) 23.dp else 16.dp),
                            color = if (config.hyperOsPowerSaverEnabled) roles.primary else roles.sc,
                            contentColor = if (config.hyperOsPowerSaverEnabled) roles.onPrimary else roles.onSc,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Bolt,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "HyperOS Battery saver",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Switch to Xiaomi / HyperOS power saving tweaks & toggles",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 12.sp,
                                color = if (config.hyperOsPowerSaverEnabled) {
                                    roles.onPc.copy(alpha = 0.82f)
                                } else {
                                    roles.onSv
                                },
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        ExpressiveHtmlSwitch(
                            checked = config.hyperOsPowerSaverEnabled,
                            onCheckedChange = { onToggleHyperOs(it) }
                        )
                    }
                }
            }

            if (config.hyperOsPowerSaverEnabled) {
                // When HyperOS Battery saver is enabled, show ONLY HyperOS powersave tweak options and toggles
                val hyperOsFiltered = hyperOsTweaks.filterByQuery()
                if (hyperOsFiltered.isNotEmpty()) {
                    item {
                        ExpressiveTweakGroupHeader(
                            title = "HyperOS powersave tweaks",
                            countLabel = "${hyperOsFiltered.size} tweaks"
                        )
                        ExpressiveGroupedTweakList(
                            items = hyperOsFiltered,
                            config = config,
                            onToggle = { spec, checked -> onUpdateTweak(spec.key, checked) }
                        )
                    }
                }

                item {
                    ExpressiveTweakGroupHeader(
                        title = "HyperOS display refresh rate",
                        countLabel = "1 tweak"
                    )
                    ExpressiveRefreshRateAndIoSection(
                        refreshRateHz = config.refreshRateHz,
                        ioItems = emptyList(),
                        config = config,
                        onSelectRefreshRate = onSelectRefreshRate,
                        onUpdateTweak = onUpdateTweak
                    )
                }

                item {
                    ExpressiveBackgroundExceptionListSection(
                        title = "Background exception list",
                        subtitle = "Choose system & user apps exempt from HyperOS freezer & background kill (selected pinned at top)",
                        installedApps = installedApps,
                        selectedAppsCsv = config.backgroundAllowedApps,
                        onUpdateSelectedAppsCsv = onUpdateBackgroundAllowedApps
                    )
                }
            } else {
                // Default Powersave Tweaks (Shown when HyperOS Battery saver is disabled)
                val psCpuFiltered = powersaveCpuTweaks.filterByQuery()
                if (psCpuFiltered.isNotEmpty()) {
                    item {
                        ExpressiveTweakGroupHeader(
                            title = "Powersave · CPU & Adreno",
                            countLabel = "${psCpuFiltered.size + 1} tweaks"
                        )
                        ExpressiveGroupedTweakList(
                            items = psCpuFiltered,
                            config = config,
                            onToggle = { spec, checked -> onUpdateTweak(spec.key, checked) }
                        )

                        // Park big cores on low battery slider card
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

                val psDozeFiltered = powersaveDozeTweaks.filterByQuery()
                if (psDozeFiltered.isNotEmpty()) {
                    item {
                        ExpressiveTweakGroupHeader(
                            title = "Powersave · Doze & standby",
                            countLabel = "${psDozeFiltered.size} tweaks"
                        )
                        ExpressiveGroupedTweakList(
                            items = psDozeFiltered,
                            config = config,
                            onToggle = { spec, checked -> onUpdateTweak(spec.key, checked) }
                        )
                    }
                }

                val psIoFiltered = powersaveIoTweaks.filterByQuery()
                if (psIoFiltered.isNotEmpty()) {
                    item {
                        ExpressiveTweakGroupHeader(
                            title = "Powersave · Display & I/O",
                            countLabel = "${psIoFiltered.size + 1} tweaks"
                        )
                        ExpressiveRefreshRateAndIoSection(
                            refreshRateHz = config.refreshRateHz,
                            ioItems = psIoFiltered,
                            config = config,
                            onSelectRefreshRate = onSelectRefreshRate,
                            onUpdateTweak = onUpdateTweak
                        )
                    }
                }

                item {
                    ExpressiveBackgroundExceptionListSection(
                        title = "Background exception list",
                        subtitle = "Choose system & user apps allowed to run in background during Powersave (selected pinned at top)",
                        installedApps = installedApps,
                        selectedAppsCsv = config.backgroundAllowedApps,
                        onUpdateSelectedAppsCsv = onUpdateBackgroundAllowedApps
                    )
                }
            }
        }

        // ====================================================================
        // PROFILE 2: BALANCE TWEAKS ONLY
        // ====================================================================
        if (activeProfile == "BALANCE") {
            val balFiltered = balanceTweaks.filterByQuery()
            item {
                ExpressiveTweakGroupHeader(
                    title = "Balance · Adaptive daily tweaks",
                    countLabel = "${balFiltered.size + 1} tweaks"
                )

                // Balanced CPU Peak Cap Slider Card
                Surface(
                    shape = RoundedCornerShape(30.dp),
                    color = roles.s2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Balanced CPU peak frequency cap",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.5.sp,
                                    color = roles.onS
                                )
                                Text(
                                    text = "Limit maximum CPU clock to ${balanceCapSlider.roundToInt()}% for smooth efficiency",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 12.sp,
                                    color = roles.onSv,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            Text(
                                text = "${balanceCapSlider.roundToInt()}%",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = roles.primary
                            )
                        }
                        Slider(
                            value = balanceCapSlider,
                            onValueChange = { balanceCapSlider = it },
                            onValueChangeFinished = {
                                onUpdateBalanceCpuCap(balanceCapSlider.roundToInt())
                            },
                            valueRange = 50f..90f,
                            steps = 7,
                            colors = SliderDefaults.colors(
                                thumbColor = roles.primary,
                                activeTrackColor = roles.primary,
                                inactiveTrackColor = roles.s4
                            ),
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }

                if (balFiltered.isNotEmpty()) {
                    ExpressiveGroupedTweakList(
                        items = balFiltered,
                        config = config,
                        onToggle = { spec, checked -> onUpdateTweak(spec.key, checked) }
                    )
                }
            }

            item {
                ExpressiveTweakGroupHeader(
                    title = "Balance · Display refresh rate",
                    countLabel = "1 tweak"
                )
                ExpressiveRefreshRateAndIoSection(
                    refreshRateHz = config.refreshRateHz,
                    ioItems = emptyList(),
                    config = config,
                    onSelectRefreshRate = onSelectRefreshRate,
                    onUpdateTweak = onUpdateTweak
                )
            }

            item {
                ExpressiveBackgroundExceptionListSection(
                    title = "Deep sleep background exception list",
                    subtitle = "Choose system & user apps exempt from screen-off deep sleep & background restriction",
                    installedApps = installedApps,
                    selectedAppsCsv = config.deepSleepWhitelist,
                    onUpdateSelectedAppsCsv = onUpdateDeepSleepWhitelist
                )
            }
        }

        // ====================================================================
        // PROFILE 3: PERFORMANCE MODE (Lite / Extreme / Ultra + Respective Tweaks)
        // ====================================================================
        if (activeProfile == "PERFORMANCE") {
            item {
                ExpressiveTweakGroupHeader(
                    title = "Performance mode tier",
                    countLabel = "Lite · Extreme · Ultra"
                )
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = roles.s2,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Select performance tier",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.5.sp,
                            color = roles.onS
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .clip(CircleShape)
                                .background(roles.s4)
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val tiers = listOf(
                                "LITE" to "Lite",
                                "EXTREME" to "Extreme",
                                "ULTRA" to "Ultra"
                            )
                            tiers.forEach { (key, title) ->
                                val isSelected = normalizedPerfTier == key
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(CircleShape)
                                        .background(if (isSelected) roles.primary else Color.Transparent)
                                        .clickable { onSelectPerformanceSubMode(key) }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = title,
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        color = if (isSelected) roles.onPrimary else roles.onSv
                                    )
                                }
                            }
                        }
                    }
                }
            }

            val perfFiltered = performanceTweaks.filterByQuery()
            if (perfFiltered.isNotEmpty()) {
                item {
                    ExpressiveTweakGroupHeader(
                        title = "Performance ($normalizedPerfTier) tweaks",
                        countLabel = "${perfFiltered.size} tweaks"
                    )
                    ExpressiveGroupedTweakList(
                        items = perfFiltered,
                        config = config,
                        onToggle = { spec, checked -> onUpdateTweak(spec.key, checked) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpressiveBackgroundExceptionListSection(
    title: String,
    subtitle: String,
    installedApps: List<InstalledAppItem>,
    selectedAppsCsv: String,
    onUpdateSelectedAppsCsv: (String) -> Unit
) {
    val roles = LocalMonetRoles.current
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var appSearchQuery by rememberSaveable { mutableStateOf("") }
    var includeSystemApps by rememberSaveable { mutableStateOf(true) }

    val selectedPackages = remember(selectedAppsCsv) {
        selectedAppsCsv
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    // Sort apps so checked/selected apps are ALWAYS pinned at the top, making them easy to uncheck
    val filteredAndPinnedApps = remember(installedApps, selectedPackages, appSearchQuery, includeSystemApps) {
        val q = appSearchQuery.trim().lowercase(Locale.US)
        installedApps
            .filter { app ->
                val isSelected = app.packageName in selectedPackages
                val matchesSystemFilter = includeSystemApps || !app.isSystemApp || isSelected
                val matchesQuery = q.isEmpty() ||
                    app.appName.lowercase(Locale.US).contains(q) ||
                    app.packageName.lowercase(Locale.US).contains(q)
                matchesSystemFilter && matchesQuery
            }
            .sortedWith(
                compareByDescending<InstalledAppItem> { it.packageName in selectedPackages }
                    .thenBy { it.appName.lowercase(Locale.US) }
            )
    }

    fun togglePackage(pkg: String) {
        val updated = selectedPackages.toMutableSet()
        if (pkg in updated) {
            updated.remove(pkg)
        } else {
            updated.add(pkg)
        }
        onUpdateSelectedAppsCsv(updated.joinToString(","))
    }

    ExpressiveTweakGroupHeader(
        title = title,
        countLabel = "${selectedPackages.size} pinned"
    )

    Surface(
        shape = RoundedCornerShape(30.dp),
        color = roles.s2,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("background_exception_list_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (selectedPackages.isNotEmpty()) roles.pc else roles.s4,
                        contentColor = if (selectedPackages.isNotEmpty()) roles.onPc else roles.onSv,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.PushPin,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = roles.onS
                        )
                        Text(
                            text = subtitle,
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 12.sp,
                            color = roles.onSv,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = if (isExpanded) roles.primary else roles.s4,
                    contentColor = if (isExpanded) roles.onPrimary else roles.onS,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clickable { isExpanded = !isExpanded }
                        .testTag("toggle_exception_list_button")
                ) {
                    Text(
                        text = if (isExpanded) "Hide apps" else "Select apps (${selectedPackages.size})",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }

            // Always show pinned/selected apps summary right at the top so user can uncheck in 1 tap even when collapsed
            if (selectedPackages.isNotEmpty()) {
                val pinnedApps = remember(installedApps, selectedPackages) {
                    val mapByPkg = installedApps.associateBy { it.packageName }
                    selectedPackages.map { pkg ->
                        mapByPkg[pkg] ?: InstalledAppItem(
                            packageName = pkg,
                            appName = pkg.substringAfterLast('.').replaceFirstChar { it.uppercase() },
                            isSystemApp = false,
                            isPermanentlyProtected = false,
                            iconBitmap = null
                        )
                    }.sortedBy { it.appName.lowercase(Locale.US) }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PINNED SELECTED APPS (TAP TO UNCHECK)",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.8.sp,
                            color = roles.primary
                        )
                        Text(
                            text = "Clear all",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = roles.err,
                            modifier = Modifier.clickable { onUpdateSelectedAppsCsv("") }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pinnedApps.forEach { app ->
                            Surface(
                                onClick = { togglePackage(app.packageName) },
                                shape = CircleShape,
                                color = roles.pc,
                                contentColor = roles.onPc
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PushPin,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = app.appName,
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Uncheck ${app.appName}",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Search bar + System apps filter chip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = roles.s4,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = null,
                                    tint = roles.onSv,
                                    modifier = Modifier.size(18.dp)
                                )
                                Box(modifier = Modifier.weight(1f)) {
                                    if (appSearchQuery.isEmpty()) {
                                        Text(
                                            text = "Search system & installed apps...",
                                            fontFamily = PlusJakartaSansFontFamily,
                                            fontSize = 12.5.sp,
                                            color = roles.onSv
                                        )
                                    }
                                    BasicTextField(
                                        value = appSearchQuery,
                                        onValueChange = { appSearchQuery = it },
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = roles.onS
                                        ),
                                        cursorBrush = SolidColor(roles.primary),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                if (appSearchQuery.isNotEmpty()) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Clear app search",
                                        tint = roles.onSv,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { appSearchQuery = "" }
                                    )
                                }
                            }
                        }

                        Surface(
                            onClick = { includeSystemApps = !includeSystemApps },
                            shape = CircleShape,
                            color = if (includeSystemApps) roles.primary else roles.s4,
                            contentColor = if (includeSystemApps) roles.onPrimary else roles.onSv
                        ) {
                            Text(
                                text = if (includeSystemApps) "All + System" else "User only",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp)
                            )
                        }
                    }

                    // Scrollable list of apps with checked apps pinned at the top
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(roles.s3),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        if (filteredAndPinnedApps.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (installedApps.isEmpty()) "Loading installed system apps..." else "No matching apps found",
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontSize = 13.sp,
                                        color = roles.onSv
                                    )
                                }
                            }
                        } else {
                            items(
                                items = filteredAndPinnedApps,
                                key = { it.packageName }
                            ) { app ->
                                val isChecked = app.packageName in selectedPackages
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { togglePackage(app.packageName) }
                                        .background(
                                            if (isChecked) roles.pc.copy(alpha = 0.28f) else Color.Transparent
                                        )
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    if (app.iconBitmap != null) {
                                        Image(
                                            bitmap = app.iconBitmap.asImageBitmap(),
                                            contentDescription = app.appName,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                        )
                                    } else {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = roles.s4,
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Filled.DeveloperBoard,
                                                    contentDescription = null,
                                                    tint = roles.onSv,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = app.appName,
                                                fontFamily = SpaceGroteskFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.5.sp,
                                                color = roles.onS,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            if (app.isSystemApp) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = roles.s4,
                                                    contentColor = roles.onSv
                                                ) {
                                                    Text(
                                                        text = "SYSTEM",
                                                        fontFamily = JetBrainsMonoFontFamily,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 8.5.sp,
                                                        maxLines = 1,
                                                        softWrap = false,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = app.packageName,
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 10.5.sp,
                                            color = roles.onSv,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { togglePackage(app.packageName) },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = roles.primary,
                                            checkmarkColor = roles.onPrimary,
                                            uncheckedColor = roles.outline
                                        )
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
private fun ExpressiveRefreshRateAndIoSection(
    refreshRateHz: Int,
    ioItems: List<TweakItemSpec>,
    config: TweakConfigEntity,
    onSelectRefreshRate: (Int) -> Unit,
    onUpdateTweak: (String, Boolean) -> Unit
) {
    val roles = LocalMonetRoles.current
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 30.dp,
                topEnd = 30.dp,
                bottomStart = if (ioItems.isEmpty()) 30.dp else 10.dp,
                bottomEnd = if (ioItems.isEmpty()) 30.dp else 10.dp
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
                    val rates = listOf(0 to "Auto", 60 to "60Hz", 90 to "90Hz", 120 to "120Hz")
                    rates.forEach { (hz, label) ->
                        val selected = refreshRateHz == hz
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

        ioItems.forEachIndexed { index, spec ->
            val isLast = index == ioItems.lastIndex
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
                if (spec.subtitle.isNotBlank()) {
                    Text(
                        text = spec.subtitle,
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 12.sp,
                        color = roles.onSv,
                        modifier = Modifier.padding(top = 2.dp)
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
                        Text(
                            text = "No tweaks applied yet.",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = roles.onSv,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        logs.forEach { entry ->
                            TerminalStreamLine(
                                status = "APPLIED",
                                time = entry.timestamp,
                                message = entry.displayName
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
    time: String,
    message: String
) {
    val roles = LocalMonetRoles.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = status,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp,
                color = roles.primary
            )
            Text(
                text = message,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = roles.onS
            )
        }
        Text(
            text = time,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 11.sp,
            color = roles.onSv,
            modifier = Modifier.padding(start = 8.dp)
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

private fun countSelectedTweaks(config: TweakConfigEntity): Int {
    return when (config.activeProfile) {
        "PERFORMANCE" -> listOf(
            config.perfCpuGovernorLockEnabled,
            config.perfAllCoresOnlineEnabled,
            config.perfSchedtuneBoostEnabled,
            config.perfTouchBoostEnabled,
            config.perfGpuAdrenoBoostEnabled,
            config.perfGpuNoNapEnabled,
            config.perfDdrBusBoostEnabled,
            config.perfStorageQueue512Enabled,
            config.perfLmkTuningEnabled
        ).count { it }

        "BALANCE" -> listOf(
            config.balanceCpuTweaksEnabled,
            config.balanceCfsSchedulerEnabled,
            config.balanceRamScalingEnabled,
            config.balanceGpuOptimizationEnabled,
            config.balanceStorageIoEnabled,
            config.balanceNetworkBbrEnabled,
            config.deepSleepScreenOffEnabled,
            config.developerProcessLimitEnabled,
            config.refreshRateHz > 0
        ).count { it }

        else -> {
            if (config.hyperOsPowerSaverEnabled) {
                listOf(
                    config.systemBatterySaverEnabled,
                    config.hyperOsAurogonFreezerEnabled,
                    config.hyperOsSuperPowerCleanEnabled,
                    config.hyperOsAodDisabled,
                    config.hyperOsTouchBoostDisabled,
                    config.touchDozeModeEnabled,
                    config.hyperOsFiveGPowerOptEnabled,
                    config.backgroundScanningDisabled,
                    config.hyperOsLock60HzEnabled,
                    config.systemBatterySaverWithoutDarkEnabled,
                    config.systemDarkModeEnabled,
                    config.hyperOsUltraSuperSaveUiEnabled
                ).count { it }
            } else {
                listOf(
                    config.cpuFreqCapEnabled,
                    config.schedutilGovernorEnabled,
                    config.schedutilRateLimitsEnabled,
                    config.twoCoresOfflineBelow20Enabled,
                    config.inputTouchBoostDisabled,
                    config.gpuPowerLimitEnabled,
                    config.adrenoIdlerEnabled,
                    config.gmsDozeEnabled,
                    config.lpmSleepEnabled,
                    config.powerEfficientWorkqueueEnabled,
                    config.schedtuneTopAppEnabled,
                    config.backgroundScanningDisabled,
                    config.storageIoQueueEnabled,
                    config.dynamicFsyncEnabled,
                    config.vmDirtyWritebackEnabled,
                    config.refreshRateHz > 0
                ).count { it }
            }
        }
    }
}

@Composable
private fun ExpressiveSwipeToApplyDock(
    config: TweakConfigEntity,
    hasUnappliedChanges: Boolean,
    onSwipeApply: () -> Unit,
    onRevertStock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val roles = LocalMonetRoles.current
    val density = LocalDensity.current
    val selectedCount = remember(config) { countSelectedTweaks(config) }

    var trackWidthPx by remember { mutableIntStateOf(0) }
    val thumbSizeDp = 48.dp
    val trackPaddingDp = 5.dp
    val thumbSizePx = with(density) { thumbSizeDp.toPx() }
    val trackPaddingPx = with(density) { (trackPaddingDp * 2).toPx() }
    val maxDragPx = (trackWidthPx - thumbSizePx - trackPaddingPx).coerceAtLeast(1f)

    var rawDragPx by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    val animatedOffsetPx by animateFloatAsState(
        targetValue = if (isDragging) rawDragPx else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "SwipeThumbOffset"
    )

    val progress = (animatedOffsetPx / maxDragPx).coerceIn(0f, 1f)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Swipe to Apply Interactive Track
        Surface(
            shape = RoundedCornerShape(29.dp),
            color = roles.s3.copy(alpha = 0.96f),
            border = BorderStroke(
                width = 1.5.dp,
                color = if (hasUnappliedChanges) roles.primary else roles.outline.copy(alpha = 0.45f)
            ),
            shadowElevation = 12.dp,
            modifier = Modifier
                .weight(1f)
                .height(58.dp)
                .onSizeChanged { trackWidthPx = it.width }
                .testTag("swipe_to_apply_button")
                .pointerInput(maxDragPx) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            isDragging = true
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            rawDragPx = (rawDragPx + dragAmount).coerceIn(0f, maxDragPx)
                        },
                        onDragEnd = {
                            val reachedThreshold = rawDragPx >= maxDragPx * 0.68f
                            isDragging = false
                            rawDragPx = 0f
                            if (reachedThreshold) {
                                onSwipeApply()
                            }
                        },
                        onDragCancel = {
                            isDragging = false
                            rawDragPx = 0f
                        }
                    )
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(trackPaddingDp),
                contentAlignment = Alignment.CenterStart
            ) {
                // Progressive glow fill as user drags thumb
                Box(
                    modifier = Modifier
                        .height(thumbSizeDp)
                        .width(
                            with(density) {
                                (thumbSizePx + animatedOffsetPx).toDp()
                            }
                        )
                        .clip(RoundedCornerShape(24.dp))
                        .background(roles.primary.copy(alpha = 0.22f + 0.45f * progress))
                )

                // Center label
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 52.dp, end = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val labelText = when {
                        progress >= 0.68f -> "Release to apply!"
                        hasUnappliedChanges -> "Swipe to apply · $selectedCount selected"
                        config.isMasterApplied -> "Swipe to re-apply ($selectedCount active)"
                        else -> "Swipe to apply ($selectedCount selected)"
                    }
                    Text(
                        text = labelText,
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = roles.onS.copy(alpha = (1f - progress * 0.5f).coerceIn(0.35f, 1f)),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (hasUnappliedChanges) {
                            "UNAPPLIED ${config.activeProfile} TWEAKS"
                        } else {
                            "${config.activeProfile} MODE READY"
                        },
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 9.sp,
                        letterSpacing = 0.7.sp,
                        color = if (hasUnappliedChanges) roles.primary else roles.onSv
                    )
                }

                // Draggable Pill Thumb
                Surface(
                    shape = CircleShape,
                    color = roles.primary,
                    contentColor = roles.onPrimary,
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .offset { IntOffset(animatedOffsetPx.roundToInt(), 0) }
                        .size(thumbSizeDp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (progress >= 0.68f) {
                                Icons.Filled.Check
                            } else {
                                Icons.AutoMirrored.Filled.ArrowForward
                            },
                            contentDescription = "Swipe to apply tweaks",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // Compact Revert Stock Button alongside Swipe to Apply
        Surface(
            onClick = onRevertStock,
            shape = RoundedCornerShape(22.dp),
            color = roles.tc,
            contentColor = roles.onTc,
            shadowElevation = 10.dp,
            modifier = Modifier
                .height(58.dp)
                .testTag("revert_stock_button")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Revert stock",
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Revert",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

