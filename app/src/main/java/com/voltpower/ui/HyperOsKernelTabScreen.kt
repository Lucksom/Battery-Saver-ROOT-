package com.voltpower.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voltpower.data.apps.InstalledAppItem
import com.voltpower.data.root.KernelRamLabRepository
import com.voltpower.data.root.KernelRamLiveSnapshot
import com.voltpower.data.root.KernelSection
import com.voltpower.data.root.KernelTweakSpec
import com.voltpower.data.root.XiaomiDaemonState
import com.voltpower.ui.theme.JetBrainsMonoFontFamily
import com.voltpower.ui.theme.PlusJakartaSansFontFamily
import com.voltpower.ui.theme.SpaceGroteskFontFamily
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun HyperOsKernelTabScreen(
    snapshot: KernelRamLiveSnapshot,
    stagedTweaks: Map<String, String>,
    lastActionBanner: String?,
    installedApps: List<InstalledAppItem>,
    onStageTweakValue: (String, String?) -> Unit,
    onRunSingleLineNow: (KernelTweakSpec, String) -> Unit,
    onUndoSingleLineToWas: (KernelTweakSpec) -> Unit,
    onSwipeApplyStaged: () -> Unit,
    onRestoreAllWasValues: () -> Unit,
    onRefreshSnapshot: () -> Unit,
    onRunOneShotVmAction: (Boolean) -> Unit,
    onToggleDaemon: (String, Boolean) -> Unit,
    onExecuteProcessAction: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val roles = LocalMonetRoles.current
    var selectedSection by rememberSaveable { mutableStateOf(KernelSection.ALL) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var targetPackageInput by rememberSaveable { mutableStateOf("") }
    var showAppPickerSheet by rememberSaveable { mutableStateOf(false) }

    val filteredSpecs = remember(selectedSection, searchQuery) {
        val base = if (selectedSection == KernelSection.ALL) {
            KernelRamLabRepository.tweakSpecs
        } else {
            KernelRamLabRepository.tweakSpecs.filter { it.section == selectedSection }
        }
        val q = searchQuery.trim().lowercase(Locale.US)
        if (q.isEmpty()) {
            base
        } else {
            base.filter { spec ->
                spec.title.lowercase(Locale.US).contains(q) ||
                    spec.functionDesc.lowercase(Locale.US).contains(q) ||
                    spec.upDownEffect.lowercase(Locale.US).contains(q) ||
                    spec.section.title.lowercase(Locale.US).contains(q)
            }
        }
    }

    val groupedBySection = remember(filteredSpecs) {
        KernelSection.entries
            .filter { it != KernelSection.ALL }
            .mapNotNull { sec ->
                val list = filteredSpecs.filter { it.section == sec }
                if (list.isNotEmpty()) sec to list else null
            }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("hyperos_kernel_tab_list"),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 220.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. EXPRESSIVE HERO / LIVE TEMPERATURE WATCH CARD
            item(key = "temp_watch_header") {
                ExpressiveKernelHeroCard(
                    snapshot = snapshot,
                    stagedCount = stagedTweaks.size,
                    lastActionBanner = lastActionBanner,
                    onRefresh = onRefreshSnapshot,
                    onRestoreAllWas = onRestoreAllWasValues
                )
            }

            // 2. EXPRESSIVE SEARCH BAR (Matches Tweaks tab search bar)
            item(key = "hyperos_search_bar") {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = roles.s2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
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
                            tint = roles.onSv,
                            modifier = Modifier.size(20.dp)
                        )
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search processor, memory, thermal, battery...",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 13.5.sp,
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
                                    .size(18.dp)
                                    .clickable { searchQuery = "" }
                            )
                        }
                    }
                }
            }

            // 3. EXPRESSIVE SECTION PILL STRIP
            item(key = "section_filter_chips") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KernelSection.entries.forEach { sec ->
                        val isSelected = selectedSection == sec
                        Surface(
                            onClick = { selectedSection = sec },
                            shape = CircleShape,
                            color = if (isSelected) roles.primary else roles.s2,
                            contentColor = if (isSelected) roles.onPrimary else roles.onSv
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = sectionIcon(sec),
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = sec.title,
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                            }
                        }
                    }
                }
            }

            // 4. HYPEROS PERFORMANCE & THERMAL SERVICES (Connected Expressive Stack)
            if ((selectedSection == KernelSection.ALL ||
                    selectedSection == KernelSection.DAEMONS ||
                    selectedSection == KernelSection.THERMAL) &&
                searchQuery.isBlank()
            ) {
                item(key = "xiaomi_daemons_group") {
                    ExpressiveDaemonGroupSection(
                        daemons = snapshot.daemons,
                        onToggleDaemon = onToggleDaemon
                    )
                }
            }

            // 5. QUICK MEMORY MAINTENANCE (Connected Expressive Tiles)
            if ((selectedSection == KernelSection.ALL || selectedSection == KernelSection.RAM) &&
                searchQuery.isBlank()
            ) {
                item(key = "ram_oneshot_actions") {
                    ExpressiveOneShotRamSection(onRunOneShotVmAction = onRunOneShotVmAction)
                }
            }

            // 6. PER-APP BACKGROUND FREEZER & CORE LIMITER
            if ((selectedSection == KernelSection.ALL || selectedSection == KernelSection.FREEZE) &&
                searchQuery.isBlank()
            ) {
                item(key = "suspend_and_app_freezer") {
                    ExpressiveAppFreezerSection(
                        snapshot = snapshot,
                        targetPackage = targetPackageInput,
                        installedApps = installedApps,
                        showAppPicker = showAppPickerSheet,
                        onTargetPackageChange = { targetPackageInput = it },
                        onToggleAppPicker = { showAppPickerSheet = !showAppPickerSheet },
                        onExecuteProcessAction = onExecuteProcessAction
                    )
                }
            }

            // 7. GROUPED STACKED TWEAK CARDS BY SECTION (Matches Tweaks tab 30.dp/10.dp stacks)
            groupedBySection.forEach { (section, specsInSection) ->
                item(key = "section_stack_${section.id}") {
                    ExpressiveKernelGroupHeader(
                        title = section.title,
                        countLabel = "${specsInSection.size} controls"
                    )
                    ExpressiveGroupedKernelTweakStack(
                        specs = specsInSection,
                        snapshot = snapshot,
                        stagedTweaks = stagedTweaks,
                        onStageTweakValue = onStageTweakValue,
                        onRunSingleLineNow = onRunSingleLineNow,
                        onUndoSingleLineToWas = onUndoSingleLineToWas
                    )
                }
            }
        }

        // Floating Swipe-to-Apply + Revert Dock at bottom of HyperOS tab
        HyperOsKernelSwipeDock(
            stagedCount = stagedTweaks.size,
            onSwipeApply = onSwipeApplyStaged,
            onRestoreAllWas = onRestoreAllWasValues,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 104.dp)
        )
    }
}

private fun sectionIcon(section: KernelSection): ImageVector {
    return when (section) {
        KernelSection.ALL -> Icons.Filled.Tune
        KernelSection.CPU -> Icons.Filled.Speed
        KernelSection.SCHED -> Icons.Filled.DeveloperBoard
        KernelSection.GPU -> Icons.Filled.RocketLaunch
        KernelSection.RAM -> Icons.Filled.Memory
        KernelSection.IRQ -> Icons.Filled.GraphicEq
        KernelSection.FREEZE -> Icons.Filled.AcUnit
        KernelSection.THERMAL -> Icons.Filled.Thermostat
        KernelSection.CHARGING -> Icons.Filled.BatteryChargingFull
        KernelSection.DAEMONS -> Icons.Filled.Security
    }
}

// ============================================================================
// 1. EXPRESSIVE HERO / TEMPERATURE MONITOR CARD
// ============================================================================

@Composable
private fun ExpressiveKernelHeroCard(
    snapshot: KernelRamLiveSnapshot,
    stagedCount: Int,
    lastActionBanner: String?,
    onRefresh: () -> Unit,
    onRestoreAllWas: () -> Unit
) {
    val roles = LocalMonetRoles.current
    val isHot = snapshot.isOverheatWarning

    Surface(
        shape = RoundedCornerShape(34.dp),
        color = if (isHot) roles.err.copy(alpha = 0.18f) else roles.s2,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (isHot) roles.err else roles.primary,
                        contentColor = if (isHot) Color.White else roles.onPrimary,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isHot) Icons.Filled.Warning else Icons.Filled.Thermostat,
                                contentDescription = "Temperature Monitor",
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "HyperOS Hardware Tuning",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = roles.onS
                        )
                        Text(
                            text = "Safe in-memory tuning · Restarting phone restores factory defaults",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 12.sp,
                            color = roles.onSv,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Surface(
                    onClick = onRefresh,
                    shape = CircleShape,
                    color = roles.s4,
                    contentColor = roles.onS
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh status",
                            tint = roles.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Refresh",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // 3 Compact Telemetry Pills (Processor Temp / Battery Temp / Selected Count)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = roles.s3,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                        Text(
                            text = "PROCESSOR",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            letterSpacing = 0.8.sp,
                            color = roles.onSv
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f°C", snapshot.maxZoneTempC),
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (isHot) roles.err else roles.onS,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = roles.s3,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                        Text(
                            text = "BATTERY",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            letterSpacing = 0.8.sp,
                            color = roles.onSv
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f°C", snapshot.batteryTempC),
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = roles.onS,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (stagedCount > 0) roles.pc else roles.s3,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                        Text(
                            text = "SELECTED",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            letterSpacing = 0.8.sp,
                            color = if (stagedCount > 0) roles.onPc.copy(alpha = 0.75f) else roles.onSv
                        )
                        Text(
                            text = if (stagedCount > 0) "$stagedCount ready" else "Ready",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (stagedCount > 0) roles.onPc else roles.onS,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            if (isHot) {
                Surface(
                    onClick = onRestoreAllWas,
                    shape = RoundedCornerShape(18.dp),
                    color = roles.err,
                    contentColor = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Phone is warm (45°C+) · Tap to restore default thermal protection",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Restore defaults",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (!lastActionBanner.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = roles.pc,
                    contentColor = roles.onPc,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = lastActionBanner,
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// 2. EXPRESSIVE GROUP HEADER & CONNECTED STACK OF TWEAK CARDS
// ============================================================================

@Composable
private fun ExpressiveKernelGroupHeader(title: String, countLabel: String) {
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
private fun ExpressiveGroupedKernelTweakStack(
    specs: List<KernelTweakSpec>,
    snapshot: KernelRamLiveSnapshot,
    stagedTweaks: Map<String, String>,
    onStageTweakValue: (String, String?) -> Unit,
    onRunSingleLineNow: (KernelTweakSpec, String) -> Unit,
    onUndoSingleLineToWas: (KernelTweakSpec) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        specs.forEachIndexed { index, spec ->
            val topRadius = if (index == 0) 30.dp else 10.dp
            val bottomRadius = if (index == specs.lastIndex) 30.dp else 10.dp
            val liveVal = snapshot.nodeValues[spec.id] ?: spec.wasValue
            val stagedVal = stagedTweaks[spec.id]

            ExpressiveKernelTweakItem(
                spec = spec,
                liveValue = liveVal,
                stagedValue = stagedVal,
                shape = RoundedCornerShape(
                    topStart = topRadius,
                    topEnd = topRadius,
                    bottomStart = bottomRadius,
                    bottomEnd = bottomRadius
                ),
                onStageValue = { newVal -> onStageTweakValue(spec.id, newVal) },
                onRunSingleLineNow = { valToWrite -> onRunSingleLineNow(spec, valToWrite) },
                onUndoToWas = { onUndoSingleLineToWas(spec) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExpressiveKernelTweakItem(
    spec: KernelTweakSpec,
    liveValue: String,
    stagedValue: String?,
    shape: RoundedCornerShape,
    onStageValue: (String?) -> Unit,
    onRunSingleLineNow: (String) -> Unit,
    onUndoToWas: () -> Unit
) {
    val roles = LocalMonetRoles.current
    var showMoreInfo by rememberSaveable(spec.id) { mutableStateOf(false) }

    val effectiveVal = stagedValue ?: liveValue
    val isStaged = stagedValue != null
    val isModifiedFromWas = liveValue != spec.wasValue
    val isHighlighted = isStaged || isModifiedFromWas

    val avatarCorner by animateDpAsState(
        targetValue = if (isHighlighted) 23.dp else 16.dp,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "KernelAvatarCorner"
    )
    val avatarRotation by animateFloatAsState(
        targetValue = if (isStaged) 360f else 0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "KernelAvatarRotation"
    )

    val isBinaryToggle = spec.options.size == 2 && spec.wasValue in spec.options
    val nonDefaultOption = if (isBinaryToggle) {
        spec.options.firstOrNull { it != spec.wasValue } ?: spec.targetValue
    } else {
        spec.targetValue
    }
    val isToggleChecked = effectiveVal != spec.wasValue

    Surface(
        shape = shape,
        color = roles.s2,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Left Morphing Squircle/Circle Icon Box (matches ExpressiveTweakRowItem)
                Surface(
                    shape = RoundedCornerShape(avatarCorner),
                    color = when {
                        spec.isRisky && isHighlighted -> roles.err
                        isHighlighted -> roles.primary
                        else -> roles.sc
                    },
                    contentColor = when {
                        spec.isRisky && isHighlighted -> Color.White
                        isHighlighted -> roles.onPrimary
                        else -> roles.onSc
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .rotate(avatarRotation)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = sectionIcon(spec.section),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Center Title + Plain-English Explanation + Status Pill
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = spec.title,
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = roles.onS
                    )

                    Text(
                        text = spec.functionDesc,
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 12.sp,
                        color = roles.onSv,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    // Compact status & micro-actions FlowRow (prevents horizontal squeeze / vertical letter wrapping)
                    FlowRow(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (spec.isRisky) {
                            Surface(
                                shape = CircleShape,
                                color = roles.err.copy(alpha = 0.16f),
                                contentColor = roles.err
                            ) {
                                Text(
                                    text = "Caution",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = if (isStaged) roles.pc else roles.s4,
                            contentColor = if (isStaged) roles.onPc else roles.onSv
                        ) {
                            Text(
                                text = if (isStaged) {
                                    "Staged: ${spec.formatValue(stagedValue ?: "")}"
                                } else {
                                    "Current: ${spec.formatValue(liveValue)}"
                                },
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                            )
                        }

                        if (isStaged) {
                            Surface(
                                onClick = { onRunSingleLineNow(stagedValue ?: spec.targetValue) },
                                shape = CircleShape,
                                color = roles.primary,
                                contentColor = roles.onPrimary
                            ) {
                                Text(
                                    text = "Apply now",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                )
                            }
                        }

                        if (isModifiedFromWas || isStaged) {
                            Surface(
                                onClick = {
                                    if (isStaged) {
                                        onStageValue(null)
                                    } else {
                                        onUndoToWas()
                                    }
                                },
                                shape = CircleShape,
                                color = roles.tc,
                                contentColor = roles.onTc
                            ) {
                                Text(
                                    text = if (isStaged) "Clear" else "Default",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Surface(
                            onClick = { showMoreInfo = !showMoreInfo },
                            shape = CircleShape,
                            color = if (showMoreInfo) roles.sc else roles.s3,
                            contentColor = roles.onSv
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = "Effect details",
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = if (showMoreInfo) "Hide tip" else "Tip",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.5.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }

                // Right-side Expressive Switch for 2-option binary tweaks
                if (isBinaryToggle) {
                    KernelExpressiveSwitch(
                        checked = isToggleChecked,
                        onCheckedChange = { wantChecked ->
                            val desiredVal = if (wantChecked) nonDefaultOption else spec.wasValue
                            if (desiredVal == liveValue && stagedValue != null) {
                                onStageValue(null)
                            } else {
                                onStageValue(desiredVal)
                            }
                        }
                    )
                }
            }

            // Expandable plain-English tip (keeps cards compact while preserving full explanation)
            AnimatedVisibility(
                visible = showMoreInfo,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = roles.s3,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = spec.upDownEffect,
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 11.5.sp,
                            color = roles.onS
                        )
                        Text(
                            text = "Factory default: ${spec.formatValue(spec.wasValue)}",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = roles.primary
                        )
                    }
                }
            }

            // Segmented Pill Bar for multi-option tweaks (3+ presets)
            if (!isBinaryToggle) {
                val maxLabelLen = remember(spec.id) {
                    spec.options.maxOfOrNull { spec.formatValue(it).length } ?: 0
                }
                if (spec.options.size <= 3 && maxLabelLen <= 18) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CircleShape)
                            .background(roles.s4)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        spec.options.forEach { optionVal ->
                            val isSelected = effectiveVal == optionVal
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .background(if (isSelected) roles.primary else Color.Transparent)
                                    .clickable {
                                        if (stagedValue == optionVal) {
                                            onStageValue(null)
                                        } else {
                                            onStageValue(optionVal)
                                        }
                                    }
                                    .padding(vertical = 9.dp, horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = spec.formatValue(optionVal),
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    color = if (isSelected) roles.onPrimary else roles.onSv,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                } else {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(roles.s3)
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        spec.options.forEach { optionVal ->
                            val isSelected = effectiveVal == optionVal
                            Surface(
                                onClick = {
                                    if (stagedValue == optionVal) {
                                        onStageValue(null)
                                    } else {
                                        onStageValue(optionVal)
                                    }
                                },
                                shape = CircleShape,
                                color = if (isSelected) roles.primary else roles.s4,
                                contentColor = if (isSelected) roles.onPrimary else roles.onSv
                            ) {
                                Text(
                                    text = spec.formatValue(optionVal),
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
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
// 3. EXPRESSIVE HYPEROS SERVICES STACK
// ============================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExpressiveDaemonGroupSection(
    daemons: List<XiaomiDaemonState>,
    onToggleDaemon: (String, Boolean) -> Unit
) {
    val roles = LocalMonetRoles.current
    ExpressiveKernelGroupHeader(
        title = "HyperOS System Services",
        countLabel = "${daemons.count { it.isRunning }} active"
    )

    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        daemons.forEachIndexed { index, d ->
            val topRadius = if (index == 0) 30.dp else 10.dp
            val bottomRadius = if (index == daemons.lastIndex) 30.dp else 10.dp
            val isPaused = !d.isRunning

            Surface(
                onClick = { onToggleDaemon(d.name, !d.isRunning) },
                shape = RoundedCornerShape(
                    topStart = topRadius,
                    topEnd = topRadius,
                    bottomStart = bottomRadius,
                    bottomEnd = bottomRadius
                ),
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
                    Surface(
                        shape = RoundedCornerShape(if (d.isRunning) 23.dp else 16.dp),
                        color = when {
                            d.isThermalDaemon && isPaused -> roles.err
                            d.isRunning -> roles.primary
                            else -> roles.sc
                        },
                        contentColor = when {
                            d.isThermalDaemon && isPaused -> Color.White
                            d.isRunning -> roles.onPrimary
                            else -> roles.onSc
                        },
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (d.isThermalDaemon) Icons.Filled.Thermostat else Icons.Filled.Security,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = d.displayTitle,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp,
                            color = roles.onS
                        )
                        Text(
                            text = d.description,
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 12.sp,
                            color = roles.onSv,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        FlowRow(
                            modifier = Modifier.padding(top = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (d.isRunning) roles.pc else roles.tc,
                                contentColor = if (d.isRunning) roles.onPc else roles.onTc
                            ) {
                                Text(
                                    text = if (d.isRunning) "Status: Running" else "Status: Paused",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                )
                            }
                            if (d.isThermalDaemon) {
                                Surface(
                                    shape = CircleShape,
                                    color = roles.err.copy(alpha = 0.16f),
                                    contentColor = roles.err
                                ) {
                                    Text(
                                        text = "Thermal Protection",
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.5.sp,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    KernelExpressiveSwitch(
                        checked = d.isRunning,
                        onCheckedChange = { shouldRun -> onToggleDaemon(d.name, shouldRun) }
                    )
                }
            }
        }
    }
}

// ============================================================================
// 4. EXPRESSIVE QUICK MEMORY MAINTENANCE TILES
// ============================================================================

@Composable
private fun ExpressiveOneShotRamSection(
    onRunOneShotVmAction: (Boolean) -> Unit
) {
    val roles = LocalMonetRoles.current
    ExpressiveKernelGroupHeader(
        title = "Instant Memory Maintenance",
        countLabel = "2 actions"
    )

    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        // Tile 1: Clear Cached RAM
        Surface(
            onClick = { onRunOneShotVmAction(true) },
            shape = RoundedCornerShape(
                topStart = 30.dp,
                topEnd = 30.dp,
                bottomStart = 10.dp,
                bottomEnd = 10.dp
            ),
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
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = roles.pc,
                    contentColor = roles.onPc,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Memory,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Clear Cached Memory",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = roles.onS
                    )
                    Text(
                        text = "Safely flushes temporary file caches to free up available RAM immediately.",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 12.sp,
                        color = roles.onSv,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Surface(
                    onClick = { onRunOneShotVmAction(true) },
                    shape = CircleShape,
                    color = roles.primary,
                    contentColor = roles.onPrimary
                ) {
                    Text(
                        text = "Clean now",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Tile 2: Defragment RAM
        Surface(
            onClick = { onRunOneShotVmAction(false) },
            shape = RoundedCornerShape(
                topStart = 10.dp,
                topEnd = 10.dp,
                bottomStart = 30.dp,
                bottomEnd = 30.dp
            ),
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
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = roles.sc,
                    contentColor = roles.onSc,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Storage,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Defragment Memory Pages",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = roles.onS
                    )
                    Text(
                        text = "Compacts scattered memory blocks into contiguous space so heavy apps launch smoothly.",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 12.sp,
                        color = roles.onSv,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Surface(
                    onClick = { onRunOneShotVmAction(false) },
                    shape = CircleShape,
                    color = roles.s4,
                    contentColor = roles.onS
                ) {
                    Text(
                        text = "Compact",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

// ============================================================================
// 5. EXPRESSIVE PER-APP BACKGROUND FREEZER (Matches Background Exception Card)
// ============================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExpressiveAppFreezerSection(
    snapshot: KernelRamLiveSnapshot,
    targetPackage: String,
    installedApps: List<InstalledAppItem>,
    showAppPicker: Boolean,
    onTargetPackageChange: (String) -> Unit,
    onToggleAppPicker: () -> Unit,
    onExecuteProcessAction: (String, String) -> Unit
) {
    val roles = LocalMonetRoles.current
    var appFilterQuery by rememberSaveable { mutableStateOf("") }

    val selectedApp = remember(targetPackage, installedApps) {
        installedApps.find { it.packageName == targetPackage }
    }

    val filteredApps = remember(installedApps, appFilterQuery) {
        val q = appFilterQuery.trim().lowercase(Locale.US)
        if (q.isEmpty()) {
            installedApps
        } else {
            installedApps.filter {
                it.appName.lowercase(Locale.US).contains(q) ||
                    it.packageName.lowercase(Locale.US).contains(q)
            }
        }
    }

    ExpressiveKernelGroupHeader(
        title = "App Freezer & Core Limiter",
        countLabel = "${snapshot.suspendFailedFreeze} sleep blocks"
    )

    Surface(
        shape = RoundedCornerShape(30.dp),
        color = roles.s2,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleAppPicker() },
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
                        color = if (targetPackage.isNotBlank()) roles.pc else roles.s4,
                        contentColor = if (targetPackage.isNotBlank()) roles.onPc else roles.onSv,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.AcUnit,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedApp?.appName ?: "Select an App to Freeze or Limit",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = roles.onS
                        )
                        Text(
                            text = if (targetPackage.isNotBlank()) {
                                "Selected: ${selectedApp?.appName ?: targetPackage}"
                            } else {
                                "Pause a heavy app in RAM so it uses zero battery without closing your work"
                            },
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 12.sp,
                            color = roles.onSv,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Surface(
                    onClick = onToggleAppPicker,
                    shape = CircleShape,
                    color = if (showAppPicker) roles.primary else roles.s4,
                    contentColor = if (showAppPicker) roles.onPrimary else roles.onS,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = if (showAppPicker) "Hide apps" else "Choose app",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = showAppPicker,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = roles.s4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
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
                                modifier = Modifier.size(17.dp)
                            )
                            Box(modifier = Modifier.weight(1f)) {
                                if (appFilterQuery.isEmpty()) {
                                    Text(
                                        text = "Search installed apps...",
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontSize = 12.5.sp,
                                        color = roles.onSv
                                    )
                                }
                                BasicTextField(
                                    value = appFilterQuery,
                                    onValueChange = { appFilterQuery = it },
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
                            if (appFilterQuery.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Clear",
                                    tint = roles.onSv,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { appFilterQuery = "" }
                                )
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(roles.s3),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        items(filteredApps.take(50), key = { it.packageName }) { app ->
                            val isSelected = app.packageName == targetPackage
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onTargetPackageChange(app.packageName)
                                        onToggleAppPicker()
                                    }
                                    .background(if (isSelected) roles.pc.copy(alpha = 0.35f) else Color.Transparent)
                                    .padding(horizontal = 14.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (app.iconBitmap != null) {
                                    Image(
                                        bitmap = app.iconBitmap.asImageBitmap(),
                                        contentDescription = app.appName,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                } else {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = roles.s4,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Filled.DeveloperBoard,
                                                contentDescription = null,
                                                tint = roles.onSv,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = app.appName,
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = roles.onS,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Selected",
                                        tint = roles.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Clean Action Pills
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val actions = listOf(
                    Triple("FREEZE_SIGSTOP", "Pause App", true),
                    Triple("RESUME_SIGCONT", "Resume App", false),
                    Triple("CGROUP_FREEZE", "Deep Freeze", true),
                    Triple("CGROUP_UNFREEZE", "Unfreeze", false),
                    Triple("RENICE_10", "Lower Priority", false),
                    Triple("TASKSET_0F", "Small Cores Only", true),
                    Triple("TASKSET_FF", "Allow All Cores", false)
                )
                actions.forEach { (code, label, isPrimaryTone) ->
                    Surface(
                        onClick = { onExecuteProcessAction(targetPackage, code) },
                        shape = CircleShape,
                        color = if (isPrimaryTone) roles.pc else roles.s4,
                        contentColor = if (isPrimaryTone) roles.onPc else roles.onS
                    ) {
                        Text(
                            text = label,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// 6. CUSTOM M3 EXPRESSIVE SWITCH & FLOATING SWIPE DOCK
// ============================================================================

@Composable
private fun KernelExpressiveSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val roles = LocalMonetRoles.current
    val thumbSize by animateDpAsState(
        targetValue = if (checked) 24.dp else 16.dp,
        animationSpec = spring(dampingRatio = 0.58f, stiffness = Spring.StiffnessMedium),
        label = "KernelSwThumbSize"
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 22.dp else 6.dp,
        animationSpec = spring(dampingRatio = 0.58f, stiffness = Spring.StiffnessMedium),
        label = "KernelSwThumbOffset"
    )
    val trackColor by animateColorAsState(
        targetValue = if (checked) roles.primary else roles.s4,
        label = "KernelSwTrackColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (checked) roles.primary else roles.outline,
        label = "KernelSwBorderColor"
    )
    val thumbColor by animateColorAsState(
        targetValue = if (checked) roles.onPrimary else roles.outline,
        label = "KernelSwThumbColor"
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
private fun HyperOsKernelSwipeDock(
    stagedCount: Int,
    onSwipeApply: () -> Unit,
    onRestoreAllWas: () -> Unit,
    modifier: Modifier = Modifier
) {
    val roles = LocalMonetRoles.current
    val density = LocalDensity.current

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
        label = "HyperOsKernelSwipeOffset"
    )

    val progress = (animatedOffsetPx / maxDragPx).coerceIn(0f, 1f)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(29.dp),
            color = roles.s3.copy(alpha = 0.96f),
            border = BorderStroke(
                width = 1.5.dp,
                color = if (stagedCount > 0) roles.primary else roles.outline.copy(alpha = 0.45f)
            ),
            shadowElevation = 12.dp,
            modifier = Modifier
                .weight(1f)
                .height(58.dp)
                .onSizeChanged { trackWidthPx = it.width }
                .testTag("hyperos_kernel_swipe_apply")
                .pointerInput(maxDragPx) {
                    detectHorizontalDragGestures(
                        onDragStart = { isDragging = true },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            rawDragPx = (rawDragPx + dragAmount).coerceIn(0f, maxDragPx)
                        },
                        onDragEnd = {
                            val reached = rawDragPx >= maxDragPx * 0.68f
                            isDragging = false
                            rawDragPx = 0f
                            if (reached) {
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
                Box(
                    modifier = Modifier
                        .height(thumbSizeDp)
                        .width(with(density) { (thumbSizePx + animatedOffsetPx).toDp() })
                        .clip(RoundedCornerShape(24.dp))
                        .background(roles.primary.copy(alpha = 0.22f + 0.45f * progress))
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 52.dp, end = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = when {
                            progress >= 0.68f -> "Release to apply!"
                            stagedCount > 0 -> "Swipe to apply · $stagedCount selected"
                            else -> "Select options above to stage"
                        },
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = roles.onS.copy(alpha = (1f - progress * 0.5f).coerceIn(0.35f, 1f)),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (stagedCount > 0) "UNAPPLIED HYPEROS CHANGES" else "IN-MEMORY TUNING READY",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 9.sp,
                        letterSpacing = 0.7.sp,
                        color = if (stagedCount > 0) roles.primary else roles.onSv
                    )
                }

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
                            imageVector = if (progress >= 0.68f) Icons.Filled.Check else Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Swipe to apply tweaks",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        Surface(
            onClick = onRestoreAllWas,
            shape = RoundedCornerShape(22.dp),
            color = roles.tc,
            contentColor = roles.onTc,
            shadowElevation = 10.dp,
            modifier = Modifier
                .height(58.dp)
                .testTag("restore_all_was_button")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Revert all",
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
