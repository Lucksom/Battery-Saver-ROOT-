package com.example.ui

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.TweakConfigEntity
import com.example.data.log.LogEntry
import com.example.data.telemetry.CpuCoreInfo
import com.example.data.telemetry.DeviceTelemetry
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import java.util.Locale
import kotlin.math.abs

private enum class MaterialYouTab(val label: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    OVERVIEW("Overview", Icons.Outlined.Dashboard, Icons.Filled.Speed),
    TWEAKS("Kernel Tweaks", Icons.Outlined.Tune, Icons.Filled.Tune),
    CONSOLE("Live Console", Icons.Outlined.Terminal, Icons.Filled.Terminal)
}

private enum class MonetSeedPreset(
    val id: String,
    val label: String,
    val previewColor: Color
) {
    WALLPAPER("WALLPAPER", "System Monet", Color(0xFF8AB4F8)),
    EMERALD("EMERALD", "Pixel Mint", Color(0xFF4ADE80)),
    IRIS("IRIS", "Android 15 Iris", Color(0xFFA78BFA)),
    OCEAN("OCEAN", "Cobalt Wave", Color(0xFF38BDF8)),
    AMBER("AMBER", "Solar Citrus", Color(0xFFFBBF24)),
    ROSE("ROSE", "Coral Pulse", Color(0xFFFB7185))
}

@Composable
private fun MonetMaterialThemeOverlay(
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

    val colorScheme = remember(isDark, seedPreset) {
        if (seedPreset == MonetSeedPreset.WALLPAPER && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
            buildMonetColorScheme(seedPreset, isDark)
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}

private fun buildMonetColorScheme(preset: MonetSeedPreset, isDark: Boolean) = when (preset) {
    MonetSeedPreset.EMERALD, MonetSeedPreset.WALLPAPER -> if (isDark) {
        darkColorScheme(
            primary = Color(0xFF7ADC9B),
            onPrimary = Color(0xFF00391B),
            primaryContainer = Color(0xFF005229),
            onPrimaryContainer = Color(0xFF96F9B5),
            secondary = Color(0xFFB7CCB9),
            onSecondary = Color(0xFF233427),
            secondaryContainer = Color(0xFF394B3D),
            onSecondaryContainer = Color(0xFFD3E8D4),
            tertiary = Color(0xFFA2CED9),
            onTertiary = Color(0xFF01363F),
            tertiaryContainer = Color(0xFF204D56),
            onTertiaryContainer = Color(0xFFBEEAF6),
            background = Color(0xFF0F1511),
            onBackground = Color(0xFFDFE4DD),
            surface = Color(0xFF0F1511),
            onSurface = Color(0xFFDFE4DD),
            surfaceVariant = Color(0xFF414942),
            onSurfaceVariant = Color(0xFFC0C9C0),
            surfaceContainerLowest = Color(0xFF0A0F0C),
            surfaceContainerLow = Color(0xFF171D19),
            surfaceContainer = Color(0xFF1B211D),
            surfaceContainerHigh = Color(0xFF252B27),
            surfaceContainerHighest = Color(0xFF303631),
            outline = Color(0xFF8B938B),
            outlineVariant = Color(0xFF414942)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF006D39),
            onPrimary = Color.White,
            primaryContainer = Color(0xFF96F9B5),
            onPrimaryContainer = Color(0xFF00210D),
            secondary = Color(0xFF506353),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFD3E8D4),
            onSecondaryContainer = Color(0xFF0E1F13),
            tertiary = Color(0xFF3A656F),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFBEEAF6),
            onTertiaryContainer = Color(0xFF001F25),
            background = Color(0xFFF6FBF4),
            onBackground = Color(0xFF171D19),
            surface = Color(0xFFF6FBF4),
            onSurface = Color(0xFF171D19),
            surfaceVariant = Color(0xFFDCE5DB),
            onSurfaceVariant = Color(0xFF414942),
            surfaceContainerLowest = Color.White,
            surfaceContainerLow = Color(0xFFF0F5EE),
            surfaceContainer = Color(0xFFEAEFE9),
            surfaceContainerHigh = Color(0xFFE4EAE3),
            surfaceContainerHighest = Color(0xFFDFE4DD),
            outline = Color(0xFF717971),
            outlineVariant = Color(0xFFC0C9C0)
        )
    }

    MonetSeedPreset.IRIS -> if (isDark) {
        darkColorScheme(
            primary = Color(0xFFCFBCFF),
            onPrimary = Color(0xFF381E72),
            primaryContainer = Color(0xFF4F378B),
            onPrimaryContainer = Color(0xFFEADDFF),
            secondary = Color(0xFFCCC2DC),
            onSecondary = Color(0xFF332D41),
            secondaryContainer = Color(0xFF4A4458),
            onSecondaryContainer = Color(0xFFE8DEF8),
            tertiary = Color(0xFFEFB8C8),
            onTertiary = Color(0xFF492532),
            tertiaryContainer = Color(0xFF633B48),
            onTertiaryContainer = Color(0xFFFFD8E4),
            background = Color(0xFF141218),
            onBackground = Color(0xFFE6E0E9),
            surface = Color(0xFF141218),
            onSurface = Color(0xFFE6E0E9),
            surfaceVariant = Color(0xFF49454F),
            onSurfaceVariant = Color(0xFFCAC4D0),
            surfaceContainerLowest = Color(0xFF0F0D13),
            surfaceContainerLow = Color(0xFF1D1B20),
            surfaceContainer = Color(0xFF211F26),
            surfaceContainerHigh = Color(0xFF2B2930),
            surfaceContainerHighest = Color(0xFF36343B),
            outline = Color(0xFF938F99),
            outlineVariant = Color(0xFF49454F)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF6750A4),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFEADDFF),
            onPrimaryContainer = Color(0xFF21005D),
            secondary = Color(0xFF625B71),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFE8DEF8),
            onSecondaryContainer = Color(0xFF1D192B),
            tertiary = Color(0xFF7D5260),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFFFD8E4),
            onTertiaryContainer = Color(0xFF31111D),
            background = Color(0xFFFEF7FF),
            onBackground = Color(0xFF1D1B20),
            surface = Color(0xFFFEF7FF),
            onSurface = Color(0xFF1D1B20),
            surfaceVariant = Color(0xFFE7E0EC),
            onSurfaceVariant = Color(0xFF49454F),
            surfaceContainerLowest = Color.White,
            surfaceContainerLow = Color(0xFFF7F2FA),
            surfaceContainer = Color(0xFFF3EDF7),
            surfaceContainerHigh = Color(0xFFECE6F0),
            surfaceContainerHighest = Color(0xFFE6E0E9),
            outline = Color(0xFF79747E),
            outlineVariant = Color(0xFFCAC4D0)
        )
    }

    MonetSeedPreset.OCEAN -> if (isDark) {
        darkColorScheme(
            primary = Color(0xFF93CCFF),
            onPrimary = Color(0xFF003354),
            primaryContainer = Color(0xFF004A76),
            onPrimaryContainer = Color(0xFFCCE5FF),
            secondary = Color(0xFFB9C8DA),
            onSecondary = Color(0xFF233240),
            secondaryContainer = Color(0xFF3A4857),
            onSecondaryContainer = Color(0xFFD5E4F7),
            tertiary = Color(0xFFD3BFE6),
            onTertiary = Color(0xFF382A49),
            tertiaryContainer = Color(0xFF4F4061),
            onTertiaryContainer = Color(0xFFEFDBFF),
            background = Color(0xFF101418),
            onBackground = Color(0xFFE0E2E8),
            surface = Color(0xFF101418),
            onSurface = Color(0xFFE0E2E8),
            surfaceVariant = Color(0xFF42474E),
            onSurfaceVariant = Color(0xFFC2C7CF),
            surfaceContainerLowest = Color(0xFF0B0F12),
            surfaceContainerLow = Color(0xFF181C20),
            surfaceContainer = Color(0xFF1C2024),
            surfaceContainerHigh = Color(0xFF272A2F),
            surfaceContainerHighest = Color(0xFF31353A),
            outline = Color(0xFF8C9198),
            outlineVariant = Color(0xFF42474E)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF00639A),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFCCE5FF),
            onPrimaryContainer = Color(0xFF001D32),
            secondary = Color(0xFF51606F),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFD5E4F7),
            onSecondaryContainer = Color(0xFF0E1D2A),
            tertiary = Color(0xFF68587A),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFEFDBFF),
            onTertiaryContainer = Color(0xFF231533),
            background = Color(0xFFF7F9FF),
            onBackground = Color(0xFF181C20),
            surface = Color(0xFFF7F9FF),
            onSurface = Color(0xFF181C20),
            surfaceVariant = Color(0xFFDEE3EB),
            onSurfaceVariant = Color(0xFF42474E),
            surfaceContainerLowest = Color.White,
            surfaceContainerLow = Color(0xFFF1F4F9),
            surfaceContainer = Color(0xFFEBEEF4),
            surfaceContainerHigh = Color(0xFFE6E8EE),
            surfaceContainerHighest = Color(0xFFE0E2E8),
            outline = Color(0xFF72777F),
            outlineVariant = Color(0xFFC2C7CF)
        )
    }

    MonetSeedPreset.AMBER -> if (isDark) {
        darkColorScheme(
            primary = Color(0xFFFABD48),
            onPrimary = Color(0xFF422C00),
            primaryContainer = Color(0xFF5E4100),
            onPrimaryContainer = Color(0xFFFFDEA8),
            secondary = Color(0xFFDBC3A1),
            onSecondary = Color(0xFF3D2E16),
            secondaryContainer = Color(0xFF55442A),
            onSecondaryContainer = Color(0xFFF8DFBB),
            tertiary = Color(0xFFB5CEA8),
            onTertiary = Color(0xFF21361B),
            tertiaryContainer = Color(0xFF374D2F),
            onTertiaryContainer = Color(0xFFD1EBC3),
            background = Color(0xFF17130B),
            onBackground = Color(0xFFEBE1D4),
            surface = Color(0xFF17130B),
            onSurface = Color(0xFFEBE1D4),
            surfaceVariant = Color(0xFF4E4539),
            onSurfaceVariant = Color(0xFFD1C5B4),
            surfaceContainerLowest = Color(0xFF110E07),
            surfaceContainerLow = Color(0xFF201B13),
            surfaceContainer = Color(0xFF241F17),
            surfaceContainerHigh = Color(0xFF2F2921),
            surfaceContainerHighest = Color(0xFF3A342B),
            outline = Color(0xFF9A8F80),
            outlineVariant = Color(0xFF4E4539)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF7B5800),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFFFDEA8),
            onPrimaryContainer = Color(0xFF271900),
            secondary = Color(0xFF6E5C40),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFF8DFBB),
            onSecondaryContainer = Color(0xFF261904),
            tertiary = Color(0xFF4E6545),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFD1EBC3),
            onTertiaryContainer = Color(0xFF0C2008),
            background = Color(0xFFFFF8F2),
            onBackground = Color(0xFF201B13),
            surface = Color(0xFFFFF8F2),
            onSurface = Color(0xFF201B13),
            surfaceVariant = Color(0xFFEEE1CF),
            onSurfaceVariant = Color(0xFF4E4539),
            surfaceContainerLowest = Color.White,
            surfaceContainerLow = Color(0xFFFBF2E7),
            surfaceContainer = Color(0xFFF5ECE1),
            surfaceContainerHigh = Color(0xFFEFE6DC),
            surfaceContainerHighest = Color(0xFFEBE1D4),
            outline = Color(0xFF807567),
            outlineVariant = Color(0xFFD1C5B4)
        )
    }

    MonetSeedPreset.ROSE -> if (isDark) {
        darkColorScheme(
            primary = Color(0xFFFFB2BC),
            onPrimary = Color(0xFF561D28),
            primaryContainer = Color(0xFF72333E),
            onPrimaryContainer = Color(0xFFFFD9DD),
            secondary = Color(0xFFE5BDC1),
            onSecondary = Color(0xFF43292D),
            secondaryContainer = Color(0xFF5C3F43),
            onSecondaryContainer = Color(0xFFFFD9DD),
            tertiary = Color(0xFFE8C08E),
            onTertiary = Color(0xFF442B06),
            tertiaryContainer = Color(0xFF5E411B),
            onTertiaryContainer = Color(0xFFFFDDB4),
            background = Color(0xFF191112),
            onBackground = Color(0xFFEFDFE0),
            surface = Color(0xFF191112),
            onSurface = Color(0xFFEFDFE0),
            surfaceVariant = Color(0xFF524345),
            onSurfaceVariant = Color(0xFFD6C2C4),
            surfaceContainerLowest = Color(0xFF130C0D),
            surfaceContainerLow = Color(0xFF22191A),
            surfaceContainer = Color(0xFF261D1E),
            surfaceContainerHigh = Color(0xFF312829),
            surfaceContainerHighest = Color(0xFF3C3233),
            outline = Color(0xFF9F8C8E),
            outlineVariant = Color(0xFF524345)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF8F4955),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFFFD9DD),
            onPrimaryContainer = Color(0xFF3B0714),
            secondary = Color(0xFF76565A),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFFFD9DD),
            onSecondaryContainer = Color(0xFF2C1518),
            tertiary = Color(0xFF775930),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFFFDDB4),
            onTertiaryContainer = Color(0xFF291800),
            background = Color(0xFFFFF8F8),
            onBackground = Color(0xFF22191A),
            surface = Color(0xFFFFF8F8),
            onSurface = Color(0xFF22191A),
            surfaceVariant = Color(0xFFF4DDDF),
            onSurfaceVariant = Color(0xFF524345),
            surfaceContainerLowest = Color.White,
            surfaceContainerLow = Color(0xFFFCF0F1),
            surfaceContainer = Color(0xFFF6EAEB),
            surfaceContainerHigh = Color(0xFFF1E5E6),
            surfaceContainerHighest = Color(0xFFEFDFE0),
            outline = Color(0xFF847375),
            outlineVariant = Color(0xFFD6C2C4)
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

    var currentTab by rememberSaveable { mutableStateOf(MaterialYouTab.OVERVIEW) }
    var selectedSeed by rememberSaveable { mutableStateOf(MonetSeedPreset.WALLPAPER) }
    var showMonetPicker by rememberSaveable { mutableStateOf(false) }

    if (currentTab != MaterialYouTab.OVERVIEW) {
        BackHandler {
            currentTab = MaterialYouTab.OVERVIEW
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

    MonetMaterialThemeOverlay(
        themeMode = config.themeMode,
        seedPreset = selectedSeed
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                MaterialYouExpressiveTopBar(
                    isRootGranted = telemetry.isRootGranted,
                    activeProfile = config.activeProfile,
                    isMasterApplied = config.isMasterApplied,
                    themeMode = config.themeMode,
                    selectedSeed = selectedSeed,
                    showMonetPicker = showMonetPicker,
                    onToggleMonetPicker = { showMonetPicker = !showMonetPicker },
                    onSelectSeed = { selectedSeed = it },
                    onCycleTheme = {
                        val next = when (config.themeMode) {
                            "SYSTEM" -> "DARK"
                            "DARK" -> "LIGHT"
                            else -> "SYSTEM"
                        }
                        viewModel.onToggleTheme(next)
                    },
                    onRevertStock = { viewModel.onRevertToStock() }
                )
            },
            bottomBar = {
                MaterialYouBottomNavigation(
                    currentTab = currentTab,
                    logCount = logs.size,
                    onTabSelected = { currentTab = it }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = { fadeIn(spring()) togetherWith fadeOut(spring()) },
                    label = "MaterialYouPageTransition"
                ) { targetTab ->
                    when (targetTab) {
                        MaterialYouTab.OVERVIEW -> {
                            MaterialYouOverviewScreen(
                                telemetry = telemetry,
                                config = config,
                                recentLogs = logs,
                                onSelectProfile = { viewModel.onSelectProfile(it) },
                                onToggleMaster = { viewModel.onToggleMaster(it) },
                                onSelectPerformanceSubMode = { viewModel.onSelectPerformanceSubMode(it) },
                                onOpenTweaks = { currentTab = MaterialYouTab.TWEAKS },
                                onOpenConsole = { currentTab = MaterialYouTab.CONSOLE }
                            )
                        }

                        MaterialYouTab.TWEAKS -> {
                            MaterialYouTweaksScreen(
                                config = config,
                                backupExists = telemetry.backupFileExists || config.backupCreated,
                                onUpdateTweak = { key, enabled -> viewModel.onUpdateTweak(key, enabled) },
                                onUpdateCoreTopology = { cores, threshold ->
                                    viewModel.onUpdateCoreTopology(cores, threshold)
                                },
                                onSelectPerformanceSubMode = { viewModel.onSelectPerformanceSubMode(it) },
                                onToggleHyperOs = { viewModel.onToggleHyperOsPowerSaver(it) },
                                onSelectRefreshRate = { viewModel.onSelectRefreshRate(it) },
                                onSelectProfile = { viewModel.onSelectProfile(it) },
                                onRevertStock = { viewModel.onRevertToStock() }
                            )
                        }

                        MaterialYouTab.CONSOLE -> {
                            MaterialYouConsoleScreen(
                                logs = logs,
                                backupFilePath = config.backupFilePath,
                                onClearLogs = { viewModel.onClearLogs() },
                                onRevertStock = { viewModel.onRevertToStock() }
                            )
                        }
                    }
                }

                if (isBusy) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(28.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            tonalElevation = 8.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    strokeWidth = 3.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(
                                        text = "Applying Kernel Parameters",
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Writing to volatile sysfs nodes…",
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
// 1. MATERIAL YOU EXPRESSIVE TOP BAR + MONET SEED DRAWER
// ============================================================================

@Composable
private fun MaterialYouExpressiveTopBar(
    isRootGranted: Boolean,
    activeProfile: String,
    isMasterApplied: Boolean,
    themeMode: String,
    selectedSeed: MonetSeedPreset,
    showMonetPicker: Boolean,
    onToggleMonetPicker: () -> Unit,
    onSelectSeed: (MonetSeedPreset) -> Unit,
    onCycleTheme: () -> Unit,
    onRevertStock: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Brand Title + Dynamic Status Pill
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "VoltPower",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Surface(
                            shape = CircleShape,
                            color = if (isMasterApplied) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHighest
                            }
                        ) {
                            Text(
                                text = if (isMasterApplied) activeProfile else "STANDBY",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isMasterApplied) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isRootGranted) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.error
                                )
                        )
                        Text(
                            text = if (isRootGranted) "Root Shell Active • Volatile RAM" else "Non-Root Preview • Volatile RAM",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Action Icon Buttons (Monet Palette, Theme Mode, Revert Stock)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalIconButton(
                        onClick = onToggleMonetPicker,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = if (showMonetPicker) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            },
                            contentColor = if (showMonetPicker) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Palette,
                            contentDescription = "Monet Dynamic Palette"
                        )
                    }

                    FilledTonalIconButton(
                        onClick = onCycleTheme,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = when (themeMode) {
                                "DARK" -> Icons.Filled.DarkMode
                                "LIGHT" -> Icons.Filled.LightMode
                                else -> Icons.Filled.DisplaySettings
                            },
                            contentDescription = "Cycle Theme Mode"
                        )
                    }

                    FilledTonalIconButton(
                        onClick = onRevertStock,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Restore,
                            contentDescription = "Restore Stock Kernel"
                        )
                    }
                }
            }

            // Expandable Monet Dynamic Color Seed Bar
            AnimatedVisibility(
                visible = showMonetPicker,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Material You (Monet) Color Engine",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Theme: $themeMode",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MonetSeedPreset.entries.forEach { preset ->
                                val isSelected = preset == selectedSeed
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSelectSeed(preset) },
                                    label = {
                                        Text(
                                            text = preset.label,
                                            fontFamily = PlusJakartaSansFontFamily,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(preset.previewColor)
                                        )
                                    },
                                    shape = CircleShape
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
// 2. MATERIAL 3 NAVIGATION BAR
// ============================================================================

@Composable
private fun MaterialYouBottomNavigation(
    currentTab: MaterialYouTab,
    logCount: Int,
    onTabSelected: (MaterialYouTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp
    ) {
        MaterialYouTab.entries.forEach { tab ->
            val selected = currentTab == tab
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = {
                    if (tab == MaterialYouTab.CONSOLE && logCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ) {
                                    Text(
                                        text = if (logCount > 99) "99+" else logCount.toString(),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (selected) tab.selectedIcon else tab.icon,
                                contentDescription = tab.label
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (selected) tab.selectedIcon else tab.icon,
                            contentDescription = tab.label
                        )
                    }
                },
                label = {
                    Text(
                        text = tab.label,
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

// ============================================================================
// 3. TAB 1: MATERIAL YOU EXPRESSIVE OVERVIEW SCREEN
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaterialYouOverviewScreen(
    telemetry: DeviceTelemetry,
    config: TweakConfigEntity,
    recentLogs: List<LogEntry>,
    onSelectProfile: (String) -> Unit,
    onToggleMaster: (Boolean) -> Unit,
    onSelectPerformanceSubMode: (String) -> Unit,
    onOpenTweaks: () -> Unit,
    onOpenConsole: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. MASTER VOLATILE RAM ENGINE HERO BANNER
        item {
            MaterialYouMasterHeroCard(
                isMasterApplied = config.isMasterApplied,
                activeProfile = config.activeProfile,
                onToggleMaster = onToggleMaster
            )
        }

        // 2. MATERIAL YOU EXPRESSIVE SEGMENTED PROFILE SWITCHER
        item {
            MaterialYouProfileSelectorCard(
                activeProfile = config.activeProfile,
                performanceSubMode = config.performanceSubMode,
                onSelectProfile = onSelectProfile,
                onSelectPerformanceSubMode = onSelectPerformanceSubMode
            )
        }

        // 3. ANDROID 15 STYLE EXPRESSIVE BATTERY PILL CARD
        // (Strictly NO Battery Health and NO Cycle Count)
        item {
            MaterialYouBatteryCard(telemetry = telemetry)
        }

        // 4. OCTA-CORE CPU TELEMETRY BENTO MATRIX
        // (Strictly NO Average CPU Frequency Graph; shows Cores 0-7 + Governor)
        item {
            MaterialYouCpuBentoCard(telemetry = telemetry)
        }

        // 5. LIVE EXECUTION CONSOLE PREVIEW
        item {
            MaterialYouMiniConsoleCard(
                recentLogs = recentLogs.take(4),
                onOpenTweaks = onOpenTweaks,
                onOpenConsole = onOpenConsole
            )
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun MaterialYouMasterHeroCard(
    isMasterApplied: Boolean,
    activeProfile: String,
    onToggleMaster: (Boolean) -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (isMasterApplied) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        label = "MasterContainerColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isMasterApplied) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        label = "MasterContentColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .clickable { onToggleMaster(!isMasterApplied) },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (isMasterApplied) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    },
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = null,
                            tint = if (isMasterApplied) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = if (isMasterApplied) "RAM Engine Active" else "RAM Engine Standby",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = contentColor
                    )
                    Text(
                        text = if (isMasterApplied) {
                            "Profile $activeProfile mounted in volatile memory"
                        } else {
                            "Tap to apply non-persistent kernel tweaks"
                        },
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 13.sp,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                }
            }

            Switch(
                checked = isMasterApplied,
                onCheckedChange = onToggleMaster,
                modifier = Modifier.testTag("master_engine_switch"),
                thumbContent = {
                    if (isMasterApplied) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize)
                        )
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaterialYouProfileSelectorCard(
    activeProfile: String,
    performanceSubMode: String,
    onSelectProfile: (String) -> Unit,
    onSelectPerformanceSubMode: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Power Profile",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Monet Expressive",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MaterialYouProfilePill(
                    title = "Powersave",
                    subtitle = "Max Efficiency",
                    icon = Icons.Filled.Eco,
                    selected = activeProfile == "POWERSAVE",
                    testTag = "profile_chip_powersave",
                    onClick = { onSelectProfile("POWERSAVE") },
                    modifier = Modifier.weight(1f)
                )
                MaterialYouProfilePill(
                    title = "Balanced",
                    subtitle = "Daily Adaptive",
                    icon = Icons.Filled.Speed,
                    selected = activeProfile == "BALANCE",
                    testTag = "profile_chip_balance",
                    onClick = { onSelectProfile("BALANCE") },
                    modifier = Modifier.weight(1f)
                )
                MaterialYouProfilePill(
                    title = "Performance",
                    subtitle = "Peak Boost",
                    icon = Icons.Filled.RocketLaunch,
                    selected = activeProfile == "PERFORMANCE",
                    testTag = "profile_chip_performance",
                    onClick = { onSelectProfile("PERFORMANCE") },
                    modifier = Modifier.weight(1f)
                )
            }

            AnimatedVisibility(
                visible = activeProfile == "PERFORMANCE",
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Performance Governor Tier",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val modes = listOf(
                        "LITE" to "Lite • Smooth",
                        "HEAVY" to "Heavy • Gaming",
                        "ULTRA" to "Ultra • Max Lock"
                    )
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        modes.forEachIndexed { index, (modeKey, modeLabel) ->
                            SegmentedButton(
                                selected = performanceSubMode == modeKey,
                                onClick = { onSelectPerformanceSubMode(modeKey) },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size)
                            ) {
                                Text(
                                    text = modeLabel,
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
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
private fun MaterialYouProfilePill(
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        label = "ProfilePillBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "ProfilePillContent"
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(22.dp),
        color = bgColor,
        border = if (selected) {
            BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        } else {
            null
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (selected) MaterialTheme.colorScheme.primary else contentColor,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = title,
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 10.sp,
                color = contentColor.copy(alpha = 0.75f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ============================================================================
// ANDROID 15 BATTERY TELEMETRY CARD (NO HEALTH, NO CYCLE COUNT)
// ============================================================================

@Composable
private fun MaterialYouBatteryCard(telemetry: DeviceTelemetry) {
    val voltageVolts = telemetry.batteryVoltageMv / 1000f
    val watts = if (telemetry.chargingWatts > 0f) {
        telemetry.chargingWatts
    } else {
        abs(telemetry.batteryCurrentMa * voltageVolts) / 1000f
    }

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Top Row: Title + Live Charging/Discharging Wattage Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (telemetry.isCharging) {
                                    Icons.Filled.BatteryChargingFull
                                } else {
                                    Icons.Filled.BatteryStd
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Battery Telemetry",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (telemetry.isCharging) "Fast Charging Active" else "Discharging on Battery",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Text(
                        text = String.format(Locale.US, "%s%.1f W", if (telemetry.isCharging) "+" else "-", watts),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            // Big Expressive Battery Percentage + Thick Material You Pill Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${telemetry.batteryLevel}",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 52.sp,
                        lineHeight = 52.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "%",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        modifier = Modifier.padding(bottom = 7.dp, start = 4.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = if (telemetry.batteryTempC >= 40f) "Warm • ${telemetry.batteryTempC}°C" else "Optimal • ${telemetry.batteryTempC}°C",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Expressive Thick Segmented M3 Battery Pill
            val animatedProgress by animateFloatAsState(
                targetValue = (telemetry.batteryLevel.coerceIn(0, 100)) / 100f,
                animationSpec = spring(stiffness = Spring.StiffnessLow),
                label = "BatteryBarProgress"
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }

            // 2x2 Tonal Bento Grid (Voltage, Current, Temperature, Capacity)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MaterialYouMetricTile(
                    label = "VOLTAGE",
                    value = String.format(Locale.US, "%.2f V", voltageVolts),
                    subtext = "${telemetry.batteryVoltageMv} mV",
                    modifier = Modifier.weight(1f)
                )
                MaterialYouMetricTile(
                    label = "CURRENT",
                    value = "${telemetry.batteryCurrentMa} mA",
                    subtext = if (telemetry.isCharging) "Charge flow" else "Drain rate",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MaterialYouMetricTile(
                    label = "TEMPERATURE",
                    value = String.format(Locale.US, "%.1f °C", telemetry.batteryTempC),
                    subtext = if (telemetry.batteryTempC < 38f) "Cool thermal state" else "Elevated thermal",
                    modifier = Modifier.weight(1f)
                )
                MaterialYouMetricTile(
                    label = "CAPACITY",
                    value = "${telemetry.batteryCapacityMah} mAh",
                    subtext = "Design capacity",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MaterialYouMetricTile(
    label: String,
    value: String,
    subtext: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtext,
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

// ============================================================================
// OCTA-CORE CPU TELEMETRY BENTO MATRIX (NO AVERAGE FREQUENCY GRAPH)
// ============================================================================

@Composable
private fun MaterialYouCpuBentoCard(telemetry: DeviceTelemetry) {
    val littleCores = telemetry.cpuCores.filter { it.coreIndex in 0..3 }
    val bigCores = telemetry.cpuCores.filter { it.coreIndex >= 4 }

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.DeveloperBoard,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Octa-Core CPU Matrix",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Per-Core Live Clocks • 8 Cores",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = telemetry.activeGovernor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Cluster 0: LITTLE Efficiency (Cores 0-3)
            Text(
                text = "CLUSTER 0 • EFFICIENCY CORES (0–3)",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            CpuClusterGrid(cores = littleCores)

            // Cluster 1: Big / Prime Performance (Cores 4-7)
            Text(
                text = "CLUSTER 1 • PERFORMANCE / PRIME CORES (4–7)",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )
            CpuClusterGrid(cores = bigCores)

            // GPU & ZRAM Tonal Footnote Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Memory,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "GPU CLOCK",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = telemetry.gpuFreqInfo,
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Storage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "ZRAM SWAP",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = telemetry.zramInfo,
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CpuClusterGrid(cores: List<CpuCoreInfo>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        cores.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pair.forEach { core ->
                    val ratio = if (!core.isOnline || core.maxFreqMhz <= 0) {
                        0f
                    } else {
                        (core.currentFreqMhz.toFloat() / core.maxFreqMhz.toFloat()).coerceIn(0f, 1f)
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CPU ${core.coreIndex}",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (core.isOnline) "${core.currentFreqMhz} MHz" else "OFFLINE",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (core.isOnline) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        MaterialTheme.colorScheme.error
                                    }
                                )
                            }
                            LinearProgressIndicator(
                                progress = { ratio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = if (core.coreIndex >= 4) {
                                    MaterialTheme.colorScheme.tertiary
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                strokeCap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MaterialYouMiniConsoleCard(
    recentLogs: List<LogEntry>,
    onOpenTweaks: () -> Unit,
    onOpenConsole: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Recent Kernel Activity",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Live sysfs & prop stream",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = onOpenTweaks,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Tweaks",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    FilledTonalButton(
                        onClick = onOpenConsole,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "All Logs",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (recentLogs.isEmpty()) {
                Text(
                    text = "No kernel commands executed yet in this session.",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                recentLogs.forEach { log ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = log.displayName,
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = log.displayValue,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = log.timestamp,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 4. TAB 2: MATERIAL 3 EXPRESSIVE KERNEL & HYPEROS TWEAKS SCREEN
// ============================================================================

@Composable
private fun MaterialYouTweaksScreen(
    config: TweakConfigEntity,
    backupExists: Boolean,
    onUpdateTweak: (String, Boolean) -> Unit,
    onUpdateCoreTopology: (Int, Int) -> Unit,
    onSelectPerformanceSubMode: (String) -> Unit,
    onToggleHyperOs: (Boolean) -> Unit,
    onSelectRefreshRate: (Int) -> Unit,
    onSelectProfile: (String) -> Unit,
    onRevertStock: () -> Unit
) {
    var selectedCategory by rememberSaveable { mutableStateOf("ALL") }
    val categories = listOf(
        "ALL" to "All Tweaks",
        "CPU" to "CPU & GPU",
        "BATTERY" to "Battery & Doze",
        "HYPEROS" to "HyperOS Engine",
        "DISPLAY" to "Display & I/O"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Backup & Volatile RAM Banner
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Column {
                            Text(
                                text = if (backupExists) "Stock Kernel Snapshot Ready" else "100% Volatile RAM Safety",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "All sysfs tweaks reset automatically on reboot",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    FilledTonalButton(onClick = onRevertStock) {
                        Text(
                            text = "Revert",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Quick-Tile Control Center Grid (2x2 Android 15 Quick Settings Style)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Quick Kernel Tiles",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MaterialYouQuickTile(
                        title = "CPU Cap",
                        subtitle = if (config.cpuFreqCapEnabled) "Active" else "Off",
                        checked = config.cpuFreqCapEnabled,
                        icon = Icons.Filled.Speed,
                        onToggle = { onUpdateTweak("cpuFreqCap", !config.cpuFreqCapEnabled) },
                        modifier = Modifier.weight(1f)
                    )
                    MaterialYouQuickTile(
                        title = "HyperOS Saver",
                        subtitle = if (config.hyperOsPowerSaverEnabled) "Engaged" else "Off",
                        checked = config.hyperOsPowerSaverEnabled,
                        icon = Icons.Filled.Bolt,
                        onToggle = { onToggleHyperOs(!config.hyperOsPowerSaverEnabled) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MaterialYouQuickTile(
                        title = "GMS Doze",
                        subtitle = if (config.gmsDozeEnabled) "Restricted" else "Standard",
                        checked = config.gmsDozeEnabled,
                        icon = Icons.Filled.Eco,
                        onToggle = { onUpdateTweak("gmsDoze", !config.gmsDozeEnabled) },
                        modifier = Modifier.weight(1f)
                    )
                    MaterialYouQuickTile(
                        title = "TCP BBR",
                        subtitle = if (config.tcpBbrCongestionEnabled) "Enabled" else "Cubic",
                        checked = config.tcpBbrCongestionEnabled,
                        icon = Icons.Filled.RocketLaunch,
                        onToggle = { onUpdateTweak("tcpBbrCongestion", !config.tcpBbrCongestionEnabled) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Category Filter Chip Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { (key, label) ->
                    val selected = selectedCategory == key
                    FilterChip(
                        selected = selected,
                        onClick = { selectedCategory = key },
                        label = {
                            Text(
                                text = label,
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        shape = CircleShape,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }

        // CPU & GPU Expressive Group
        if (selectedCategory == "ALL" || selectedCategory == "CPU") {
            item {
                MaterialYouGroupSectionTitle("CPU, Scheduler & Adreno GPU")
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    MaterialYouGroupedSwitchItem(
                        title = "Limit Peak CPU Frequencies",
                        subtitle = "Cap big & prime cluster frequencies to prevent thermal spikes",
                        command = "/sys/devices/system/cpu/cpu*/cpufreq/scaling_max_freq",
                        checked = config.cpuFreqCapEnabled,
                        position = GroupItemPosition.TOP,
                        onCheckedChange = { onUpdateTweak("cpuFreqCap", it) }
                    )
                    MaterialYouGroupedSwitchItem(
                        title = "Schedutil Energy Governor",
                        subtitle = "Tune EAS schedutil rate limits for smoother frequency ramps",
                        command = "schedutil/up_rate_limit_us=4000",
                        checked = config.schedutilGovernorEnabled,
                        position = GroupItemPosition.MIDDLE,
                        onCheckedChange = { onUpdateTweak("schedutilGovernor", it) }
                    )
                    MaterialYouGroupedSwitchItem(
                        title = "Disable Input Touch Boost",
                        subtitle = "Prevent sudden max-clock spikes on every screen swipe",
                        command = "msm_performance/touchboost=0",
                        checked = config.inputTouchBoostDisabled,
                        position = GroupItemPosition.MIDDLE,
                        onCheckedChange = { onUpdateTweak("inputTouchBoost", it) }
                    )
                    MaterialYouGroupedSwitchItem(
                        title = "Adreno GPU Power Limiter",
                        subtitle = "Throttle default GPU pwrlevel during non-gaming workloads",
                        command = "kgsl-3d0/default_pwrlevel=6",
                        checked = config.gpuPowerLimitEnabled,
                        position = GroupItemPosition.MIDDLE,
                        onCheckedChange = { onUpdateTweak("gpuPowerLimit", it) }
                    )
                    MaterialYouGroupedSwitchItem(
                        title = "Adreno Idler Downclock",
                        subtitle = "Rapidly drop GPU to lowest clock when frame queue is idle",
                        command = "adreno_idler/adreno_idler_active=Y",
                        checked = config.adrenoIdlerEnabled,
                        position = GroupItemPosition.BOTTOM,
                        onCheckedChange = { onUpdateTweak("adrenoIdler", it) }
                    )
                }
            }

            // Dynamic Core Offline Slider Card
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Auto-Offline Big Cores on Low Battery",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Park ${config.offlineCoreCount} performance cores below ${config.offlineBatteryThreshold}% battery",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = config.twoCoresOfflineBelow20Enabled,
                                onCheckedChange = { onUpdateTweak("twoCoresOfflineBelow20", it) }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(2, 3, 4).forEach { count ->
                                FilterChip(
                                    selected = config.offlineCoreCount == count,
                                    onClick = { onUpdateCoreTopology(count, config.offlineBatteryThreshold) },
                                    label = { Text("Park $count Cores") },
                                    shape = CircleShape
                                )
                            }
                        }

                        Slider(
                            value = config.offlineBatteryThreshold.toFloat(),
                            onValueChange = {
                                onUpdateCoreTopology(config.offlineCoreCount, it.toInt())
                            },
                            valueRange = 5f..60f,
                            steps = 10
                        )
                    }
                }
            }
        }

        // Battery, Doze & Deep Sleep Group
        if (selectedCategory == "ALL" || selectedCategory == "BATTERY") {
            item {
                MaterialYouGroupSectionTitle("Deep Doze & Power Saving")
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    MaterialYouGroupedSwitchItem(
                        title = "Aggressive GMS Doze",
                        subtitle = "Restrict Google Play Services background wake locks",
                        command = "cmd appops set com.google.android.gms WAKE_LOCK ignore",
                        checked = config.gmsDozeEnabled,
                        position = GroupItemPosition.TOP,
                        onCheckedChange = { onUpdateTweak("gmsDoze", it) }
                    )
                    MaterialYouGroupedSwitchItem(
                        title = "Kernel LPM Deep Sleep",
                        subtitle = "Enable low-power cluster sleep levels during screen off",
                        command = "lpm_levels/sleep_disabled=0",
                        checked = config.lpmSleepEnabled,
                        position = GroupItemPosition.MIDDLE,
                        onCheckedChange = { onUpdateTweak("lpmSleep", it) }
                    )
                    MaterialYouGroupedSwitchItem(
                        title = "Power-Efficient Workqueues",
                        subtitle = "Route unbound kernel background jobs to LITTLE cluster",
                        command = "workqueue/power_efficient=Y",
                        checked = config.powerEfficientWorkqueueEnabled,
                        position = GroupItemPosition.MIDDLE,
                        onCheckedChange = { onUpdateTweak("powerEfficientWorkqueue", it) }
                    )
                    MaterialYouGroupedSwitchItem(
                        title = "Disable Wi-Fi & BLE Background Scanning",
                        subtitle = "Stop hidden location beacon scans when radio is idle",
                        command = "settings put global wifi_scan_always_enabled 0",
                        checked = config.backgroundScanningDisabled,
                        position = GroupItemPosition.BOTTOM,
                        onCheckedChange = { onUpdateTweak("backgroundScanning", it) }
                    )
                }
            }
        }

        // HyperOS / MIUI Super Power Engine Group
        if (selectedCategory == "ALL" || selectedCategory == "HYPEROS") {
            item {
                MaterialYouGroupSectionTitle("HyperOS / MIUI Power Engine")
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    MaterialYouGroupedSwitchItem(
                        title = "HyperOS Extreme Standby Engine",
                        subtitle = "Engage Xiaomi Joyose & PowerKeeper hibernation subsystems",
                        command = "settings put System power_supersave_mode_open 1",
                        checked = config.hyperOsPowerSaverEnabled,
                        position = GroupItemPosition.TOP,
                        onCheckedChange = { onToggleHyperOs(it) }
                    )
                    MaterialYouGroupedSwitchItem(
                        title = "Aurogon Freezer App Hibernation",
                        subtitle = "Freeze cached background processes immediately on screen lock",
                        command = "persist.sys.miui.freezer=true",
                        checked = config.hyperOsAurogonFreezerEnabled,
                        position = GroupItemPosition.MIDDLE,
                        onCheckedChange = { onUpdateTweak("hyperOsAurogonFreezer", it) }
                    )
                    MaterialYouGroupedSwitchItem(
                        title = "5G Smart SA/NSA Power Optimization",
                        subtitle = "Dynamically fall back to LTE when throughput demand is low",
                        command = "settings put global fiveg_power_saving_enable 1",
                        checked = config.hyperOsFiveGPowerOptEnabled,
                        position = GroupItemPosition.MIDDLE,
                        onCheckedChange = { onUpdateTweak("hyperOsFiveGPowerOpt", it) }
                    )
                    MaterialYouGroupedSwitchItem(
                        title = "Remove Control Center Window Blur",
                        subtitle = "Disable real-time Gaussian blur shader to reduce GPU load",
                        command = "settings put global disable_window_blurs 1",
                        checked = config.controlCenterBlurEnabled,
                        position = GroupItemPosition.BOTTOM,
                        onCheckedChange = { onUpdateTweak("controlCenterBlur", it) }
                    )
                }
            }
        }

        // Display Refresh Rate & Storage/Network I/O Group
        if (selectedCategory == "ALL" || selectedCategory == "DISPLAY") {
            item {
                MaterialYouGroupSectionTitle("Display Refresh Rate & I/O")
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Peak Display Refresh Rate",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val rates = listOf(0 to "Auto LTPO", 60 to "60 Hz", 90 to "90 Hz", 120 to "120 Hz")
                            rates.forEach { (hz, label) ->
                                FilterChip(
                                    selected = config.refreshRateHz == hz,
                                    onClick = { onSelectRefreshRate(hz) },
                                    label = { Text(label, fontSize = 12.sp) },
                                    shape = CircleShape
                                )
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    MaterialYouGroupedSwitchItem(
                        title = "Optimize UFS Storage I/O Queue",
                        subtitle = "Reduce iostats overhead and tune read_ahead_kb to 128KB",
                        command = "block/sda/queue/iostats=0",
                        checked = config.storageIoQueueEnabled,
                        position = GroupItemPosition.TOP,
                        onCheckedChange = { onUpdateTweak("storageIoQueue", it) }
                    )
                    MaterialYouGroupedSwitchItem(
                        title = "Defer VM Dirty Writeback",
                        subtitle = "Batch RAM disk flushes to reduce UFS flash wakeups",
                        command = "vm.dirty_writeback_centisecs=3000",
                        checked = config.vmDirtyWritebackEnabled,
                        position = GroupItemPosition.MIDDLE,
                        onCheckedChange = { onUpdateTweak("vmDirtyWriteback", it) }
                    )
                    MaterialYouGroupedSwitchItem(
                        title = "TCP BBR Congestion Control",
                        subtitle = "Google BBR congestion algorithm for lower socket latency",
                        command = "net.ipv4.tcp_congestion_control=bbr",
                        checked = config.tcpBbrCongestionEnabled,
                        position = GroupItemPosition.BOTTOM,
                        onCheckedChange = { onUpdateTweak("tcpBbrCongestion", it) }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

private enum class GroupItemPosition {
    TOP, MIDDLE, BOTTOM, SINGLE
}

@Composable
private fun MaterialYouGroupSectionTitle(title: String) {
    Text(
        text = title,
        fontFamily = SpaceGroteskFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
    )
}

@Composable
private fun MaterialYouQuickTile(
    title: String,
    subtitle: String,
    checked: Boolean,
    icon: ImageVector,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (checked) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        label = "QuickTileContainer"
    )
    val contentColor by animateColorAsState(
        targetValue = if (checked) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        label = "QuickTileContent"
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(24.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            Column {
                Text(
                    text = title,
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 11.sp,
                    color = contentColor.copy(alpha = 0.75f),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun MaterialYouGroupedSwitchItem(
    title: String,
    subtitle: String,
    command: String,
    checked: Boolean,
    position: GroupItemPosition,
    onCheckedChange: (Boolean) -> Unit
) {
    val shape = when (position) {
        GroupItemPosition.TOP -> RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 6.dp, bottomEnd = 6.dp)
        GroupItemPosition.MIDDLE -> RoundedCornerShape(6.dp)
        GroupItemPosition.BOTTOM -> RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
        GroupItemPosition.SINGLE -> RoundedCornerShape(24.dp)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable { onCheckedChange(!checked) },
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = command,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                thumbContent = if (checked) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize)
                        )
                    }
                } else null
            )
        }
    }
}

// ============================================================================
// 5. TAB 3: MATERIAL YOU LIVE KERNEL CONSOLE SCREEN
// ============================================================================

@Composable
private fun MaterialYouConsoleScreen(
    logs: List<LogEntry>,
    backupFilePath: String,
    onClearLogs: () -> Unit,
    onRevertStock: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
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
                            text = "Sysfs Execution Stream",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Snapshot: $backupFilePath",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    FilledIconButton(
                        onClick = onClearLogs,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteSweep,
                            contentDescription = "Clear Logs"
                        )
                    }
                }
            }
        }

        if (logs.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Terminal,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Console Stream Cleared",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Toggle any profile or kernel tweak to inspect live sysfs writes.",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(logs.size) { index ->
                val entry = logs[index]
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (entry.isSuccess) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.errorContainer
                                    }
                                ) {
                                    Text(
                                        text = if (entry.isSuccess) "APPLIED" else "SKIPPED",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (entry.isSuccess) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onErrorContainer
                                        }
                                    )
                                }
                                Text(
                                    text = entry.displayName,
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = entry.timestamp,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "Value: ${entry.displayValue}",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        entry.command?.let { cmd ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLowest
                            ) {
                                Text(
                                    text = "$ $cmd",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
