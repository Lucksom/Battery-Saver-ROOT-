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
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import com.voltpower.data.db.TweakConfigEntity
import com.voltpower.data.root.HyperOsProfileTemplate
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

private data class ShiftedHyperOsTweakSpec(
    val key: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val isChecked: (TweakConfigEntity) -> Boolean
)

@Composable
fun HyperOsKernelTabScreen(
    config: TweakConfigEntity,
    hasUnappliedSaverChanges: Boolean,
    snapshot: KernelRamLiveSnapshot,
    stagedTweaks: Map<String, String>,
    selectedTemplate: HyperOsProfileTemplate?,
    isBalanceScreenOffSaverActive: Boolean,
    isPocoF7Enabled: Boolean,
    lastActionBanner: String?,
    installedApps: List<InstalledAppItem>,
    freezeCheckedAppsRaw: String,
    backgroundAllowedAppsCsv: String,
    onToggleHyperOsPowerSaver: (Boolean) -> Unit,
    onUpdateHyperOsTweak: (String, Boolean) -> Unit,
    onSelectRefreshRate: (Int) -> Unit,
    onSetAllHyperOsBatterySaverTweaks: (Boolean) -> Unit,
    onSelectTemplate: (HyperOsProfileTemplate?) -> Unit,
    onTogglePocoF7Mode: (Boolean) -> Unit,
    onStageTweakValue: (String, String?) -> Unit,
    onRunSingleLineNow: (KernelTweakSpec, String) -> Unit,
    onUndoSingleLineToWas: (KernelTweakSpec) -> Unit,
    onSwipeApplyStaged: () -> Unit,
    onRestoreAllWasValues: () -> Unit,
    onRefreshSnapshot: () -> Unit,
    onRunOneShotVmAction: (Boolean) -> Unit,
    onToggleDaemon: (String, Boolean) -> Unit,
    onUpdateFreezeCheckedApps: (Set<String>, Boolean) -> Unit,
    onUpdateBackgroundAllowedApps: (String) -> Unit,
    onExecuteProcessAction: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val roles = LocalMonetRoles.current
    var selectedSection by rememberSaveable { mutableStateOf(KernelSection.ALL) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showAppPickerSheet by rememberSaveable { mutableStateOf(true) }

    val shiftedHyperOsTweaks = remember {
        listOf(
            ShiftedHyperOsTweakSpec(
                key = "systemBatterySaver",
                title = "HyperOS PowerKeeper SuperSave",
                subtitle = "Extreme standby mode, Audio HAL & Wi-Fi SISO antenna save",
                icon = Icons.Filled.Bolt,
                isChecked = { it.systemBatterySaverEnabled }
            ),
            ShiftedHyperOsTweakSpec(
                key = "hyperOsAurogonFreezer",
                title = "Aurogon process freezer",
                subtitle = "Freeze background cached processes in standby",
                icon = Icons.Filled.AcUnit,
                isChecked = { it.hyperOsAurogonFreezerEnabled }
            ),
            ShiftedHyperOsTweakSpec(
                key = "hyperOsSuperPowerClean",
                title = "SuperPowerClean RAM purge",
                subtitle = "Stop unwhitelisted 3rd-party background tasks",
                icon = Icons.Filled.Memory,
                isChecked = { it.hyperOsSuperPowerCleanEnabled }
            ),
            ShiftedHyperOsTweakSpec(
                key = "hyperOsAodDisabled",
                title = "Disable Always-On Display (AOD)",
                subtitle = "Turn off ambient display during power saving",
                icon = Icons.Filled.DarkMode,
                isChecked = { it.hyperOsAodDisabled }
            ),
            ShiftedHyperOsTweakSpec(
                key = "hyperOsTouchBoostDisabled",
                title = "Clamp Xiaomi touch & launch boost",
                subtitle = "Eliminate app-launch & scroll frequency spikes",
                icon = Icons.Filled.Speed,
                isChecked = { it.hyperOsTouchBoostDisabled }
            ),
            ShiftedHyperOsTweakSpec(
                key = "touchDozeMode",
                title = "Xiaomi THP touch doze mode",
                subtitle = "Low-power touch panel scanning when idle",
                icon = Icons.Filled.Eco,
                isChecked = { it.touchDozeModeEnabled }
            ),
            ShiftedHyperOsTweakSpec(
                key = "hyperOsFiveGPowerOpt",
                title = "5G smart modem power saving",
                subtitle = "Optimize 5G cellular radio power consumption",
                icon = Icons.Filled.Wifi,
                isChecked = { it.hyperOsFiveGPowerOptEnabled }
            ),
            ShiftedHyperOsTweakSpec(
                key = "backgroundScanning",
                title = "Disable Wi-Fi & BLE background scan",
                subtitle = "Stop hidden beacon scans when idle",
                icon = Icons.Filled.Wifi,
                isChecked = { it.backgroundScanningDisabled }
            ),
            ShiftedHyperOsTweakSpec(
                key = "hyperOsLock60Hz",
                title = "Lock 60Hz MIUI refresh rate",
                subtitle = "Clamp display refresh rate to 60Hz",
                icon = Icons.Filled.Tune,
                isChecked = { it.hyperOsLock60HzEnabled }
            ),
            ShiftedHyperOsTweakSpec(
                key = "controlCenterBlur",
                title = "Remove notification blur",
                subtitle = "Disable real-time GPU window blur on notification shade to save battery",
                icon = Icons.Filled.Contrast,
                isChecked = { it.controlCenterBlurEnabled }
            ),
            ShiftedHyperOsTweakSpec(
                key = "batterySaverNoDark",
                title = "Enable system battery saver",
                subtitle = "Activate Android & HyperOS low power mode",
                icon = Icons.Filled.Eco,
                isChecked = { it.systemBatterySaverWithoutDarkEnabled }
            ),
            ShiftedHyperOsTweakSpec(
                key = "systemDarkMode",
                title = "Force AMOLED dark mode",
                subtitle = "Enable system-wide dark theme for OLED power saving",
                icon = Icons.Filled.DarkMode,
                isChecked = { it.systemDarkModeEnabled }
            ),
            ShiftedHyperOsTweakSpec(
                key = "hyperOsUltraSuperSaveUi",
                title = "Ultra SuperSave dark wallpaper & panel off",
                subtitle = "Dark simple wallpaper+ turn off notification panel & few control center options.",
                icon = Icons.Filled.Visibility,
                isChecked = { it.hyperOsUltraSuperSaveUiEnabled }
            )
        )
    }

    val filteredShiftedTweaks = remember(searchQuery, shiftedHyperOsTweaks) {
        val q = searchQuery.trim().lowercase(Locale.US)
        if (q.isEmpty()) {
            shiftedHyperOsTweaks
        } else {
            shiftedHyperOsTweaks.filter {
                it.title.lowercase(Locale.US).contains(q) ||
                    it.subtitle.lowercase(Locale.US).contains(q)
            }
        }
    }

    val selectedSaverTweaksCount = remember(config) {
        if (!config.hyperOsPowerSaverEnabled) {
            0
        } else {
            shiftedHyperOsTweaks.count { it.isChecked(config) }
        }
    }
    val totalPendingOrSelectedCount = stagedTweaks.size +
        (if (config.hyperOsPowerSaverEnabled || hasUnappliedSaverChanges) selectedSaverTweaksCount else 0)

    val effectiveSelectedSection = if (!isPocoF7Enabled && selectedSection == KernelSection.MEM_BUS) {
        KernelSection.ALL
    } else {
        selectedSection
    }

    val filteredSpecs = remember(effectiveSelectedSection, searchQuery, selectedTemplate, isPocoF7Enabled) {
        val visibleSpecs = KernelRamLabRepository.tweakSpecs.filter { spec ->
            if (!isPocoF7Enabled && spec.isPocoF7Only) {
                false
            } else if (isPocoF7Enabled &&
                selectedTemplate == HyperOsProfileTemplate.POWERSAVE &&
                (spec.id == "walt_hispeed_load" || spec.id == "walt_hispeed_freq")
            ) {
                // Dropped in Poco F7 Powersave because policy5 uses 'powersave' governor
                false
            } else {
                true
            }
        }
        val base = if (effectiveSelectedSection == KernelSection.ALL) {
            visibleSpecs
        } else {
            visibleSpecs.filter { it.section == effectiveSelectedSection }
        }
        val sortedBase = KernelRamLabRepository.sortSpecsForTemplate(base, selectedTemplate)
        val q = searchQuery.trim().lowercase(Locale.US)
        if (q.isEmpty()) {
            sortedBase
        } else {
            sortedBase.filter { spec ->
                spec.title.lowercase(Locale.US).contains(q) ||
                    spec.functionDesc.lowercase(Locale.US).contains(q) ||
                    spec.upDownEffect.lowercase(Locale.US).contains(q) ||
                    spec.section.title.lowercase(Locale.US).contains(q)
            }
        }
    }

    val groupedBySection = remember(filteredSpecs, selectedTemplate, isPocoF7Enabled) {
        val orderedSections = KernelRamLabRepository.getSectionOrderForTemplate(
            template = selectedTemplate,
            pocoF7Enabled = isPocoF7Enabled
        )
        orderedSections.mapNotNull { sec ->
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
            // 0. SHIFTED AT TOP: HYPEROS BATTERY SAVER TOGGLE & ALL SHIFTED HYPEROS POWERSAVE TWEAKS
            item(key = "shifted_hyperos_battery_saver_top") {
                ShiftedHyperOsBatterySaverTopSection(
                    config = config,
                    shiftedTweaks = filteredShiftedTweaks,
                    onToggleHyperOsPowerSaver = onToggleHyperOsPowerSaver,
                    onUpdateHyperOsTweak = onUpdateHyperOsTweak,
                    onSelectRefreshRate = onSelectRefreshRate,
                    onSetAllHyperOsBatterySaverTweaks = onSetAllHyperOsBatterySaverTweaks
                )
            }

            // 1. EXPRESSIVE HERO CARD (Processor temperature removed per user request)
            item(key = "temp_watch_header") {
                ExpressiveKernelHeroCard(
                    snapshot = snapshot,
                    stagedCount = totalPendingOrSelectedCount,
                    selectedTemplate = selectedTemplate,
                    isBalanceScreenOffSaverActive = isBalanceScreenOffSaverActive,
                    lastActionBanner = lastActionBanner,
                    onRefresh = onRefreshSnapshot
                )
            }

            // 1B. HYPEROS PROFILE TEMPLATES CARD (Powersave / Balance Screen-Off / Performance + Poco F7 Toggle)
            item(key = "hyperos_profile_templates") {
                ExpressiveHyperOsTemplateCard(
                    selectedTemplate = selectedTemplate,
                    isBalanceScreenOffSaverActive = isBalanceScreenOffSaverActive,
                    isPocoF7Enabled = isPocoF7Enabled,
                    onSelectTemplate = onSelectTemplate,
                    onTogglePocoF7Mode = onTogglePocoF7Mode
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
                val visibleSections = remember(isPocoF7Enabled) {
                    KernelSection.entries.filter { isPocoF7Enabled || it != KernelSection.MEM_BUS }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    visibleSections.forEach { sec ->
                        val isSelected = effectiveSelectedSection == sec
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
            if ((effectiveSelectedSection == KernelSection.ALL ||
                    effectiveSelectedSection == KernelSection.DAEMONS ||
                    effectiveSelectedSection == KernelSection.THERMAL) &&
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
            if ((effectiveSelectedSection == KernelSection.ALL || effectiveSelectedSection == KernelSection.RAM) &&
                searchQuery.isBlank()
            ) {
                item(key = "ram_oneshot_actions") {
                    ExpressiveOneShotRamSection(onRunOneShotVmAction = onRunOneShotVmAction)
                }
            }

            // 6. PER-APP BACKGROUND FREEZER & CORE LIMITER
            if ((effectiveSelectedSection == KernelSection.ALL || effectiveSelectedSection == KernelSection.FREEZE) &&
                searchQuery.isBlank()
            ) {
                item(key = "suspend_and_app_freezer") {
                    ExpressiveAppFreezerSection(
                        installedApps = installedApps,
                        freezeCheckedAppsRaw = freezeCheckedAppsRaw,
                        backgroundAllowedAppsCsv = backgroundAllowedAppsCsv,
                        showAppPicker = showAppPickerSheet,
                        onToggleAppPicker = { showAppPickerSheet = !showAppPickerSheet },
                        onUpdateFreezeCheckedApps = onUpdateFreezeCheckedApps,
                        onUpdateBackgroundAllowedApps = onUpdateBackgroundAllowedApps,
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
                        isPocoF7Enabled = isPocoF7Enabled,
                        onStageTweakValue = onStageTweakValue,
                        onRunSingleLineNow = onRunSingleLineNow,
                        onUndoSingleLineToWas = onUndoSingleLineToWas
                    )
                }
            }
        }

        // Floating Swipe-to-Apply + Revert Dock at bottom of HyperOS tab
        HyperOsKernelSwipeDock(
            stagedCount = totalPendingOrSelectedCount,
            onSwipeApply = onSwipeApplyStaged,
            onRestoreAllWas = onRestoreAllWasValues,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 104.dp)
        )
    }
}

@Composable
private fun ShiftedHyperOsBatterySaverTopSection(
    config: TweakConfigEntity,
    shiftedTweaks: List<ShiftedHyperOsTweakSpec>,
    onToggleHyperOsPowerSaver: (Boolean) -> Unit,
    onUpdateHyperOsTweak: (String, Boolean) -> Unit,
    onSelectRefreshRate: (Int) -> Unit,
    onSetAllHyperOsBatterySaverTweaks: (Boolean) -> Unit
) {
    val roles = LocalMonetRoles.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ExpressiveKernelGroupHeader(
            title = "HyperOS battery saver",
            countLabel = if (config.hyperOsPowerSaverEnabled) "enabled" else "disabled"
        )

        Surface(
            onClick = { onToggleHyperOsPowerSaver(!config.hyperOsPowerSaverEnabled) },
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
                        text = "Xiaomi / HyperOS power saving tweaks & toggles (shifted at top)",
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

                KernelExpressiveSwitch(
                    checked = config.hyperOsPowerSaverEnabled,
                    onCheckedChange = { onToggleHyperOsPowerSaver(it) }
                )
            }
        }

        AnimatedVisibility(
            visible = config.hyperOsPowerSaverEnabled,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (shiftedTweaks.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "HyperOS powersave tweaks (${shiftedTweaks.size})",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = roles.onS
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                onClick = { onSetAllHyperOsBatterySaverTweaks(true) },
                                shape = CircleShape,
                                color = roles.s4,
                                contentColor = roles.onS
                            ) {
                                Text(
                                    text = "Select all",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                            Surface(
                                onClick = { onSetAllHyperOsBatterySaverTweaks(false) },
                                shape = CircleShape,
                                color = roles.s4,
                                contentColor = roles.onSv
                            ) {
                                Text(
                                    text = "Uncheck all",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        val total = shiftedTweaks.size
                        shiftedTweaks.forEachIndexed { index, spec ->
                            val isOn = spec.isChecked(config)
                            val topRadius = if (index == 0) 30.dp else 10.dp
                            val bottomRadius = if (index == total - 1) 30.dp else 10.dp
                            val shape = RoundedCornerShape(
                                topStart = topRadius,
                                topEnd = topRadius,
                                bottomStart = bottomRadius,
                                bottomEnd = bottomRadius
                            )
                            val rowBg by animateColorAsState(
                                targetValue = if (isOn) roles.pc else roles.s2,
                                label = "ShiftedTweakBg_${spec.key}"
                            )
                            val rowFg by animateColorAsState(
                                targetValue = if (isOn) roles.onPc else roles.onS,
                                label = "ShiftedTweakFg_${spec.key}"
                            )
                            Surface(
                                onClick = { onUpdateHyperOsTweak(spec.key, !isOn) },
                                shape = shape,
                                color = rowBg,
                                contentColor = rowFg,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("tweak_row_${spec.key}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(if (isOn) 23.dp else 16.dp),
                                        color = if (isOn) roles.primary else roles.sc,
                                        contentColor = if (isOn) roles.onPrimary else roles.onSc,
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = spec.icon,
                                                contentDescription = null,
                                                modifier = Modifier.size(21.dp)
                                            )
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = spec.title,
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.5.sp,
                                            color = rowFg
                                        )
                                        Text(
                                            text = spec.subtitle,
                                            fontFamily = PlusJakartaSansFontFamily,
                                            fontSize = 11.5.sp,
                                            color = if (isOn) rowFg.copy(alpha = 0.82f) else roles.onSv,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }

                                    KernelExpressiveSwitch(
                                        checked = isOn,
                                        onCheckedChange = { onUpdateHyperOsTweak(spec.key, it) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Shifted HyperOS Display Refresh Rate selector card
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
                            Column {
                                Text(
                                    text = "HyperOS display refresh rate",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.5.sp,
                                    color = roles.onS
                                )
                                Text(
                                    text = "Clamp MIUI/HyperOS peak & min refresh rate",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 11.5.sp,
                                    color = roles.onSv,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = roles.pc,
                                contentColor = roles.onPc
                            ) {
                                Text(
                                    text = if (config.refreshRateHz == 0) "DYNAMIC" else "${config.refreshRateHz} HZ",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .clip(CircleShape)
                                .background(roles.s4)
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val rates = listOf(0 to "Auto", 60 to "60 Hz", 90 to "90 Hz", 120 to "120 Hz")
                            rates.forEach { (hz, label) ->
                                val selected = config.refreshRateHz == hz
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(CircleShape)
                                        .background(if (selected) roles.primary else Color.Transparent)
                                        .clickable { onSelectRefreshRate(hz) }
                                        .padding(vertical = 8.dp),
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
            }
        }
    }
}

private fun sectionIcon(section: KernelSection): ImageVector {
    return when (section) {
        KernelSection.ALL -> Icons.Filled.Tune
        KernelSection.CPU -> Icons.Filled.Speed
        KernelSection.SCHED -> Icons.Filled.DeveloperBoard
        KernelSection.GPU -> Icons.Filled.RocketLaunch
        KernelSection.MEM_BUS -> Icons.Filled.Storage
        KernelSection.RAM -> Icons.Filled.Memory
        KernelSection.IRQ -> Icons.Filled.GraphicEq
        KernelSection.FREEZE -> Icons.Filled.AcUnit
        KernelSection.THERMAL -> Icons.Filled.Thermostat
        KernelSection.DAEMONS -> Icons.Filled.Security
    }
}

// ============================================================================
// 1. EXPRESSIVE HERO CARD (Processor temperature removed per user request)
// ============================================================================

@Composable
private fun ExpressiveKernelHeroCard(
    snapshot: KernelRamLiveSnapshot,
    stagedCount: Int,
    selectedTemplate: HyperOsProfileTemplate?,
    isBalanceScreenOffSaverActive: Boolean,
    lastActionBanner: String?,
    onRefresh: () -> Unit
) {
    val roles = LocalMonetRoles.current

    Surface(
        shape = RoundedCornerShape(34.dp),
        color = roles.s2,
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
                        color = roles.primary,
                        contentColor = roles.onPrimary,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Tune,
                                contentDescription = "HyperOS Hardware Tuning",
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
                            text = "Works alongside HyperOS Power Saver · Flushes on standard Power Save, Balance, or Performance",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 11.5.sp,
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

            // 2 Compact Telemetry Pills (Battery Temp / Selected Count — Processor Temp removed)
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
                            text = if (stagedCount > 0) "$stagedCount staged" else (selectedTemplate?.title ?: "Custom"),
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = if (stagedCount > 0) roles.onPc else roles.onS,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            if (isBalanceScreenOffSaverActive) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = roles.sc,
                    contentColor = roles.onSc,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Immediate Battery Saver on Screen Off is armed & active",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
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
// 1B. EXPRESSIVE HYPEROS PROFILE TEMPLATES CARD
// (Powersave / Balance with Immediate Battery Saver when Screen Off / Performance)
// ============================================================================

@Composable
private fun ExpressiveHyperOsTemplateCard(
    selectedTemplate: HyperOsProfileTemplate?,
    isBalanceScreenOffSaverActive: Boolean,
    isPocoF7Enabled: Boolean,
    onSelectTemplate: (HyperOsProfileTemplate?) -> Unit,
    onTogglePocoF7Mode: (Boolean) -> Unit
) {
    val roles = LocalMonetRoles.current
    var showFunctionsGuide by rememberSaveable { mutableStateOf(false) }

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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "HyperOS Profile Templates",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = roles.onS
                        )
                    }
                    Text(
                        text = "Select Powersave, Balance, or Performance · Swipe below to apply in RAM",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 11.5.sp,
                        color = roles.onSv,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                if (selectedTemplate != null) {
                    AlignedKernelPill(
                        text = "Reset sort",
                        backgroundColor = roles.s4,
                        contentColor = roles.onSv,
                        onClick = { onSelectTemplate(null) }
                    )
                }
            }

            // 3-Way Segmented Profile Template Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(roles.s4)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val templates = listOf(
                    HyperOsProfileTemplate.POWERSAVE to "Powersave",
                    HyperOsProfileTemplate.BALANCE_SCREEN_OFF to "Balance",
                    HyperOsProfileTemplate.PERFORMANCE to "Performance"
                )
                templates.forEach { (tpl, shortLabel) ->
                    val isSelected = selectedTemplate == tpl
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) roles.primary else Color.Transparent)
                            .clickable { onSelectTemplate(tpl) }
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = shortLabel,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = if (isSelected) roles.onPrimary else roles.onSv,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Toggle for Poco F7 (onyx, Snapdragon 8s Gen 4, HyperOS 3)
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = if (isPocoF7Enabled) roles.pc else roles.s3,
                border = if (isPocoF7Enabled) BorderStroke(1.5.dp, roles.primary) else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .clickable { onTogglePocoF7Mode(!isPocoF7Enabled) }
                    .testTag("poco_f7_onyx_toggle_row")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isPocoF7Enabled) roles.primary else roles.s4,
                        contentColor = if (isPocoF7Enabled) roles.onPrimary else roles.onSv,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.DeveloperBoard,
                                contentDescription = "Poco F7 Mode",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Poco F7 (onyx, Snapdragon 8s Gen 4, HyperOS 3)",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = if (isPocoF7Enabled) roles.onPc else roles.onS
                        )
                        Text(
                            text = if (isPocoF7Enabled) {
                                "Enabled · Revealing & sorting Poco F7 tweaks in the same Powersave, Balance, and Performance list"
                            } else {
                                "Enable to reveal tweaks assigned & sorted for Poco F7 in Powersave, Balance, and Performance"
                            },
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 11.5.sp,
                            color = if (isPocoF7Enabled) roles.onPc.copy(alpha = 0.85f) else roles.onSv,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    KernelExpressiveSwitch(
                        checked = isPocoF7Enabled,
                        onCheckedChange = { onTogglePocoF7Mode(it) },
                        modifier = Modifier.testTag("poco_f7_onyx_switch")
                    )
                }
            }

            // Profile summary & functions panel
            val activeDesc = if (isPocoF7Enabled) {
                when (selectedTemplate) {
                    HyperOsProfileTemplate.POWERSAVE ->
                        "Poco F7 (onyx, Snapdragon 8s Gen 4, HyperOS 3) · Powersave: Low idle floors (365/480 MHz), policy5 'powersave' governor, CPU caps (1.52 / 1.73 / 1.06 / 1.48 GHz), Core 6 parked (0), big cluster min_cpus=1, no power-key boost, DDR capped at 3.19 GHz, LLCC 806 MHz, L3 1.34 GHz, GPU 600 MHz (pwrlevel 4, idle 50ms), swappiness 60, vfs 100, read_ahead 64 KB, iostats/add_random off (0), freeze 5000ms, pm_async 0, blur off."
                    HyperOsProfileTemplate.BALANCE_SCREEN_OFF ->
                        if (isBalanceScreenOffSaverActive) {
                            "Poco F7 (onyx, Snapdragon 8s Gen 4, HyperOS 3) · Balance (Screen-Off Saver Active): Low idle floors (365/480 MHz), 'walt' governor (hispeed 90% / 1.19 GHz), CPU caps (1.80 / 2.21 / 1.82 / 2.05 GHz), DDR 3.69 GHz, LLCC 933 MHz, L3 1.80 GHz, GPU 873 MHz (pwrlevel 2, idle 50ms), swappiness 100, vfs 100, read_ahead 128 KB + Immediate Screen-Off Saver."
                        } else {
                            "Poco F7 (onyx, Snapdragon 8s Gen 4, HyperOS 3) · Balance: Smooth daily use & trims waste — low idle floors (365/480 MHz), 'walt' governor (hispeed 90% / 1.19 GHz), CPU caps (1.80 / 2.21 / 1.82 / 2.05 GHz), DDR 3.69 GHz, LLCC 933 MHz, L3 1.80 GHz stock, GPU 873 MHz (pwrlevel 2, idle 50ms), swappiness 100, vfs 100, read_ahead 128 KB, iostats/add_random off (0)."
                        }
                    HyperOsProfileTemplate.PERFORMANCE ->
                        "Poco F7 (onyx, Snapdragon 8s Gen 4, HyperOS 3) · Performance: Faster response & caps top speeds below absolute max to limit heat — 'walt' governor (hispeed 85% / 1.40 GHz), CPU caps (2.02 / 2.52 / 2.21 / 2.05 GHz), faster big-core migration (up 85 / down 75), uncapped stock DDR (4.76 GHz) / LLCC (1.21 GHz) / L3 (1.80 GHz), GPU 873 MHz (idle 80ms), swappiness 100, vfs 73, read_ahead 256 KB."
                    null ->
                        "Poco F7 (onyx, Snapdragon 8s Gen 4, HyperOS 3) tweaks are revealed in the list below. Tap Powersave, Balance, or Performance above to stage & sort them."
                }
            } else {
                when (selectedTemplate) {
                    HyperOsProfileTemplate.POWERSAVE ->
                        "Powersave selected: Sorts & pre-selects low idle speeds, background core limits, GPU 600 MHz cap, notification blur removal, and fast deep sleep. Turn on the Poco F7 toggle above to also reveal Poco F7 (onyx, 8s Gen 4) bus/core_ctl/walt tweaks."
                    HyperOsProfileTemplate.BALANCE_SCREEN_OFF ->
                        "Balance selected: Balanced daily responsiveness + Immediate Battery Saver when screen turns off. Turn on the Poco F7 toggle above to also reveal Poco F7 (onyx, 8s Gen 4) tweaks."
                    HyperOsProfileTemplate.PERFORMANCE ->
                        "Performance selected: Sorts & pre-selects higher CPU/GPU ceilings and faster scheduler ramp-up. Turn on the Poco F7 toggle above to also reveal Poco F7 (onyx, 8s Gen 4) tweaks."
                    null ->
                        "Tap Powersave, Balance, or Performance above to sort & stage HyperOS tweaks, or enable the Poco F7 (onyx, Snapdragon 8s Gen 4, HyperOS 3) toggle."
                }
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (selectedTemplate != null || isPocoF7Enabled) roles.pc.copy(alpha = 0.45f) else roles.s3,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isPocoF7Enabled) {
                                when (selectedTemplate) {
                                    HyperOsProfileTemplate.POWERSAVE -> "POWERSAVE · POCO F7 (ONYX · 8S GEN 4 · HYPEROS 3)"
                                    HyperOsProfileTemplate.BALANCE_SCREEN_OFF -> "BALANCE · POCO F7 (ONYX · 8S GEN 4 · HYPEROS 3)"
                                    HyperOsProfileTemplate.PERFORMANCE -> "PERFORMANCE · POCO F7 (ONYX · 8S GEN 4 · HYPEROS 3)"
                                    null -> "POCO F7 (ONYX · SNAPDRAGON 8S GEN 4 · HYPEROS 3)"
                                }
                            } else {
                                when (selectedTemplate) {
                                    HyperOsProfileTemplate.POWERSAVE -> "HYPEROS POWERSAVE PROFILE"
                                    HyperOsProfileTemplate.BALANCE_SCREEN_OFF -> "HYPEROS BALANCE PROFILE"
                                    HyperOsProfileTemplate.PERFORMANCE -> "HYPEROS PERFORMANCE PROFILE"
                                    null -> "STANDARD HYPEROS TWEAKS"
                                }
                            },
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.6.sp,
                            color = roles.primary
                        )

                        if (isPocoF7Enabled) {
                            AlignedKernelPill(
                                text = if (showFunctionsGuide) "Hide Functions" else "What Each Tweak Does",
                                backgroundColor = roles.s4,
                                contentColor = roles.onS,
                                onClick = { showFunctionsGuide = !showFunctionsGuide }
                            )
                        }
                    }

                    Text(
                        text = activeDesc,
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 12.sp,
                        color = roles.onS
                    )

                    AnimatedVisibility(
                        visible = isPocoF7Enabled && showFunctionsGuide,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        PocoF7FunctionsGuidePanel(selectedTemplate = selectedTemplate)
                    }
                }
            }
        }
    }
}

@Composable
private fun PocoF7FunctionsGuidePanel(selectedTemplate: HyperOsProfileTemplate?) {
    val roles = LocalMonetRoles.current
    val groups = remember(selectedTemplate) {
        listOf(
            "CPU" to listOf(
                "scaling_min_freq" to "The lowest speed a core cluster idles at (364.8 MHz on policy0, 480 MHz on policy2/5/7). A low floor saves power at idle.",
                "scaling_max_freq" to "The speed ceiling. A lower cap means less heat and power, but slower peaks (Powersave: 1.52/1.73/1.06/1.48 GHz · Balanced: 1.80/2.21/1.82/2.05 GHz · Performance: 2.02/2.52/2.21/2.05 GHz).",
                "scaling_governor" to "The rule for choosing speed. 'walt' follows load (Balanced/Perf). 'powersave' pins the cluster to its minimum (Powersave).",
                "hispeed_load / hispeed_freq" to "With 'walt', the load % at which the CPU jumps to a fixed speed, and what that speed is (Balanced: 90% / 1.19 GHz · Performance: 85% / 1.40 GHz).",
                "cpu6 online" to "0 parks Core 6 entirely (Powersave), 1 brings it back (Balanced/Performance).",
                "core_ctl min_cpus" to "Minimum cores kept on in big cluster cpu2 (1 in Powersave lets cores park; 3 in Balanced/Performance).",
                "sched_pelt_multiplier" to "How fast the scheduler reacts to load. 1 is slow and smooth (Powersave), 2 is balanced (Balanced), 4 is fast and uses more power (Performance).",
                "sched_upmigrate / downmigrate" to "Load % at which a task moves to a bigger core and back down to a smaller one (95/85 in Powersave & Balanced · 85/75 in Performance).",
                "powerkey_input_boost_freq" to "Short speed boost when you press the power key (Off in Powersave · On in Balanced & Performance)."
            ),
            "Task groups" to listOf(
                "cpuset ... cpus" to "Which cores a group of tasks may use. background (0-1 Powersave, 0-3 Balanced, 0-7 Perf) and system-background (0-3 Powersave, 0-7 Balanced/Perf) are limited to small cores; misf is the frame service group (0-3 Powersave, 4-7 Balanced/Perf).",
                "cpuctl uclamp.max / uclamp.min" to "Ceiling or floor on the speed a group can request, as a %. background/system-background: 15%/25% (Powersave), 25%/30% (Balanced), 30%/40% (Perf). nnapi-hal (AI/ML) floor: 0.00%.",
                "sched_rt_runtime_us" to "How much of each period real-time tasks (audio, UI) may use; the rest stays for normal tasks (950000 Powersave · 1250000 Balanced/Perf)."
            ),
            "GPU" to listOf(
                "idle_timer" to "How long the GPU waits idle before sleeping (50 ms in Powersave & Balanced · 80 ms in Performance).",
                "max_pwrlevel" to "Highest GPU level allowed. 0 is fastest, higher number is slower (4 in Powersave · 2 in Balanced & Performance).",
                "GPU max_freq" to "The GPU clock cap (600 MHz in Powersave · 873 MHz in Balanced & Performance).",
                "kgsl-busmon max_freq" to "Cap on the GPU's memory bus speed (600 MHz in Powersave · 873 MHz in Balanced & Performance)."
            ),
            "Memory bus (bus_dcvs)" to listOf(
                "DDR cap" to "RAM speed ceiling across all voters. Saves the most power, though games/camera feel it first (3.19 GHz Powersave · 3.69 GHz Balanced · 4.76 GHz Stock in Performance).",
                "LLCC cap" to "Clock of the shared Last-Level Cache (806 MHz Powersave · 933 MHz Balanced · 1.21 GHz / 933 MHz Stock in Performance).",
                "L3 cap" to "Clock of the CPU's L3 cache; affects CPU speed directly (1.34 GHz Powersave · 1.80 GHz Stock in Balanced & Performance)."
            ),
            "RAM and storage" to listOf(
                "swappiness" to "How readily memory moves to ZRAM; higher swaps more (60 Powersave · 100 Balanced & Performance · 150 Stock).",
                "vfs_cache_pressure" to "How quickly the kernel drops cached file and folder info; lower keeps more (100 Powersave & Balanced · 73 Performance & Stock).",
                "read_ahead_kb" to "How much extra data is read ahead on sequential reads (64 KB Powersave · 128 KB Balanced · 256 KB Performance).",
                "iostats / add_random" to "Per-read storage bookkeeping, turned off (0) across all profiles to save CPU overhead."
            ),
            "Suspend & Display" to listOf(
                "pm_freeze_timeout" to "How long the phone waits for tasks to freeze before giving up on sleeping (5000 ms Powersave & Balanced · 20000 ms Performance).",
                "pm_async" to "Whether devices suspend in parallel (1 in Balanced & Performance) or one by one (0 in Powersave).",
                "disable_window_blurs" to "Turns off blur effects, which cost some GPU work (1 in Powersave · 0 in Balanced & Performance)."
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        groups.forEach { (groupTitle, items) ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = roles.s2,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = groupTitle.uppercase(Locale.US),
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        color = roles.primary
                    )
                    items.forEach { (name, desc) ->
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "• $name",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = roles.onS
                            )
                            Text(
                                text = desc,
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 11.sp,
                                color = roles.onSv,
                                modifier = Modifier.padding(start = 10.dp)
                            )
                        }
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
    isPocoF7Enabled: Boolean,
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
                isPocoF7Enabled = isPocoF7Enabled,
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
    isPocoF7Enabled: Boolean,
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
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Left Morphing Squircle/Circle Icon Box (aligned to top of title)
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
                        .padding(top = 2.dp)
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

                // Center Title + Plain-English Explanation + Uniform-Plane Status Pills
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

                    // Uniform-plane status & micro-actions row (identical 26.dp height & Y-axis center for all pills)
                    FlowRow(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isPocoF7Enabled && spec.isPocoF7Only) {
                            AlignedKernelPill(
                                text = "Poco F7 · onyx · 8s Gen 4",
                                backgroundColor = roles.sc,
                                contentColor = roles.onSc
                            )
                        }

                        if (spec.isRisky) {
                            AlignedKernelPill(
                                text = "Caution",
                                backgroundColor = roles.err.copy(alpha = 0.16f),
                                contentColor = roles.err
                            )
                        }

                        AlignedKernelPill(
                            text = if (isStaged) {
                                "Staged: ${spec.formatValue(stagedValue ?: "")}"
                            } else {
                                "Current: ${spec.formatValue(liveValue)}"
                            },
                            backgroundColor = if (isStaged) roles.pc else roles.s4,
                            contentColor = if (isStaged) roles.onPc else roles.onSv
                        )

                        if (isStaged) {
                            AlignedKernelPill(
                                text = "Apply now",
                                backgroundColor = roles.primary,
                                contentColor = roles.onPrimary,
                                onClick = { onRunSingleLineNow(stagedValue ?: spec.targetValue) }
                            )
                        }

                        if (isModifiedFromWas || isStaged) {
                            AlignedKernelPill(
                                text = if (isStaged) "Clear" else "Default",
                                backgroundColor = roles.tc,
                                contentColor = roles.onTc,
                                onClick = {
                                    if (isStaged) {
                                        onStageValue(null)
                                    } else {
                                        onUndoToWas()
                                    }
                                }
                            )
                        }

                        AlignedKernelPill(
                            text = if (showMoreInfo) "Hide tip" else "Tip",
                            backgroundColor = if (showMoreInfo) roles.sc else roles.s3,
                            contentColor = roles.onSv,
                            leadingIcon = Icons.Filled.Info,
                            onClick = { showMoreInfo = !showMoreInfo }
                        )
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
                        },
                        modifier = Modifier.padding(top = 6.dp)
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
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        spec.options.forEach { optionVal ->
                            val isSelected = effectiveVal == optionVal
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) roles.primary else Color.Transparent)
                                    .clickable {
                                        if (stagedValue == optionVal) {
                                            onStageValue(null)
                                        } else {
                                            onStageValue(optionVal)
                                        }
                                    }
                                    .padding(horizontal = 8.dp),
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
                            AlignedKernelPill(
                                text = spec.formatValue(optionVal),
                                backgroundColor = if (isSelected) roles.primary else roles.s4,
                                contentColor = if (isSelected) roles.onPrimary else roles.onSv,
                                heightDp = 32,
                                horizontalPaddingDp = 12,
                                fontSizeSp = 11.5f,
                                onClick = {
                                    if (stagedValue == optionVal) {
                                        onStageValue(null)
                                    } else {
                                        onStageValue(optionVal)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlignedKernelPill(
    text: String,
    backgroundColor: Color,
    contentColor: Color,
    leadingIcon: ImageVector? = null,
    heightDp: Int = 26,
    horizontalPaddingDp: Int = 10,
    fontSizeSp: Float = 10.5f,
    onClick: (() -> Unit)? = null
) {
    val baseModifier = Modifier
        .height(heightDp.dp)
        .clip(CircleShape)
        .background(backgroundColor)
        .let { mod ->
            if (onClick != null) {
                mod.clickable(onClick = onClick)
            } else {
                mod
            }
        }
        .padding(horizontal = horizontalPaddingDp.dp)

    Box(
        modifier = baseModifier,
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(12.dp)
                )
            }
            Text(
                text = text,
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = fontSizeSp.sp,
                color = contentColor,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
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
                    verticalAlignment = Alignment.Top,
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
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(46.dp)
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
                            modifier = Modifier.padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AlignedKernelPill(
                                text = if (d.isRunning) "Status: Running" else "Status: Paused",
                                backgroundColor = if (d.isRunning) roles.pc else roles.tc,
                                contentColor = if (d.isRunning) roles.onPc else roles.onTc
                            )
                            if (d.isThermalDaemon) {
                                AlignedKernelPill(
                                    text = "Thermal Protection",
                                    backgroundColor = roles.err.copy(alpha = 0.16f),
                                    contentColor = roles.err
                                )
                            }
                        }
                    }

                    KernelExpressiveSwitch(
                        checked = d.isRunning,
                        onCheckedChange = { shouldRun -> onToggleDaemon(d.name, shouldRun) },
                        modifier = Modifier.padding(top = 6.dp)
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
// ============================================================================
// 5. EXPRESSIVE PER-APP BACKGROUND FREEZER (Multi-Select Checkboxes + Pinned Top + Synced Exceptions)
// ============================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExpressiveAppFreezerSection(
    installedApps: List<InstalledAppItem>,
    freezeCheckedAppsRaw: String,
    backgroundAllowedAppsCsv: String,
    showAppPicker: Boolean,
    onToggleAppPicker: () -> Unit,
    onUpdateFreezeCheckedApps: (Set<String>, Boolean) -> Unit,
    onUpdateBackgroundAllowedApps: (String) -> Unit,
    onExecuteProcessAction: (String, String) -> Unit
) {
    val roles = LocalMonetRoles.current
    var appFilterQuery by rememberSaveable { mutableStateOf("") }

    // Only user-installed enabled apps (excludes disabled apps & system apps)
    val userEnabledApps = remember(installedApps) {
        installedApps.filter { it.isUserEnabledApp }
    }
    val allUserEnabledPkgSet = remember(userEnabledApps) {
        userEnabledApps.map { it.packageName }.toSet()
    }

    // Exceptions list synced from HyperOS Battery Saver in Tweaks
    val exceptionPackages = remember(backgroundAllowedAppsCsv) {
        backgroundAllowedAppsCsv
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    // Checked user-enabled apps to freeze
    val checkedFreezePackages = remember(freezeCheckedAppsRaw, allUserEnabledPkgSet) {
        when (freezeCheckedAppsRaw) {
            "__ALL_USER_ENABLED__" -> allUserEnabledPkgSet
            "__NONE__" -> emptySet()
            else -> {
                val explicit = freezeCheckedAppsRaw
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .toSet()
                if (allUserEnabledPkgSet.isEmpty()) explicit else (explicit intersect allUserEnabledPkgSet)
            }
        }
    }

    val isAllUserEnabledChecked = remember(freezeCheckedAppsRaw, checkedFreezePackages, allUserEnabledPkgSet) {
        freezeCheckedAppsRaw == "__ALL_USER_ENABLED__" ||
            (allUserEnabledPkgSet.isNotEmpty() && checkedFreezePackages.size == allUserEnabledPkgSet.size)
    }

    // Checked apps are ALWAYS pinned at the top of the list
    val filteredAndPinnedApps = remember(userEnabledApps, checkedFreezePackages, appFilterQuery) {
        val q = appFilterQuery.trim().lowercase(Locale.US)
        val filtered = if (q.isEmpty()) {
            userEnabledApps
        } else {
            userEnabledApps.filter {
                it.appName.lowercase(Locale.US).contains(q) ||
                    it.packageName.lowercase(Locale.US).contains(q)
            }
        }
        filtered.sortedWith(
            compareByDescending<InstalledAppItem> { it.packageName in checkedFreezePackages }
                .thenBy { it.appName.lowercase(Locale.US) }
        )
    }

    fun toggleSelectAllUserEnabled(selectAll: Boolean) {
        if (selectAll) {
            onUpdateFreezeCheckedApps(allUserEnabledPkgSet, true)
        } else {
            onUpdateFreezeCheckedApps(emptySet(), false)
        }
    }

    fun toggleSingleApp(packageName: String) {
        val updated = checkedFreezePackages.toMutableSet()
        if (packageName in updated) {
            updated.remove(packageName)
        } else {
            updated.add(packageName)
        }
        val nowAllChecked = allUserEnabledPkgSet.isNotEmpty() && updated.size == allUserEnabledPkgSet.size
        onUpdateFreezeCheckedApps(updated, nowAllChecked)
    }

    fun toggleExceptionApp(packageName: String) {
        val updatedExceptions = exceptionPackages.toMutableSet()
        if (packageName in updatedExceptions) {
            updatedExceptions.remove(packageName)
        } else {
            updatedExceptions.add(packageName)
        }
        onUpdateBackgroundAllowedApps(updatedExceptions.joinToString(","))
    }

    ExpressiveKernelGroupHeader(
        title = "Select an App to Freeze",
        countLabel = "${checkedFreezePackages.size}/${userEnabledApps.size} checked"
    )

    Surface(
        shape = RoundedCornerShape(30.dp),
        color = roles.s2,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hyperos_app_freezer_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card Header Row
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
                        color = if (checkedFreezePackages.isNotEmpty()) roles.pc else roles.s4,
                        contentColor = if (checkedFreezePackages.isNotEmpty()) roles.onPc else roles.onSv,
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
                            text = when {
                                isAllUserEnabledChecked ->
                                    "All user enabled apps (${userEnabledApps.size} checked)"
                                checkedFreezePackages.isNotEmpty() ->
                                    "${checkedFreezePackages.size} user apps checked (pinned at top)"
                                else ->
                                    "No apps checked to freeze"
                            },
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = roles.onS
                        )
                        Text(
                            text = "Checked apps are pinned at top · Exceptions synced with HyperOS Battery Saver",
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
                        text = if (showAppPicker) "Hide apps" else "Select apps (${checkedFreezePackages.size})",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }

            // Prominent "Select all user enabled apps" Row with Checkbox
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isAllUserEnabledChecked) roles.pc.copy(alpha = 0.38f) else roles.s3,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { toggleSelectAllUserEnabled(!isAllUserEnabledChecked) }
                    .testTag("select_all_user_enabled_apps_row")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isAllUserEnabledChecked) roles.primary else roles.s4,
                        contentColor = if (isAllUserEnabledChecked) roles.onPrimary else roles.onSv,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.AcUnit,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Select all user enabled apps (${userEnabledApps.size})",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = roles.onS,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Checks all enabled user apps · Excludes disabled & system apps",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 11.sp,
                            color = roles.onSv
                        )
                    }
                    Checkbox(
                        checked = isAllUserEnabledChecked,
                        onCheckedChange = { wantAll -> toggleSelectAllUserEnabled(wantAll) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = roles.primary,
                            checkmarkColor = roles.onPrimary,
                            uncheckedColor = roles.outline
                        ),
                        modifier = Modifier.testTag("select_all_user_enabled_apps_checkbox")
                    )
                }
            }

            // Synced Exceptions List Banner & Chips (Synced from HyperOS Battery Saver in Tweaks)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = roles.s3,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "EXCEPTIONS LIST (SYNCED WITH HYPEROS BATTERY SAVER)",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.6.sp,
                                color = roles.primary
                            )
                            Text(
                                text = if (exceptionPackages.isEmpty()) {
                                    "No exceptions active · Uncheck any app below to add it to exceptions"
                                } else {
                                    "${exceptionPackages.size} apps exempt from freezing · Tap chip to remove exception"
                                },
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 11.sp,
                                color = roles.onSv,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        if (exceptionPackages.isNotEmpty()) {
                            AlignedKernelPill(
                                text = "Sync exceptions",
                                backgroundColor = roles.s4,
                                contentColor = roles.onS,
                                heightDp = 28,
                                horizontalPaddingDp = 10,
                                fontSizeSp = 11f,
                                onClick = {
                                    onUpdateBackgroundAllowedApps(exceptionPackages.joinToString(","))
                                }
                            )
                        }
                    }

                    if (exceptionPackages.isNotEmpty()) {
                        val exceptionAppItems = remember(installedApps, exceptionPackages) {
                            val mapByPkg = installedApps.associateBy { it.packageName }
                            exceptionPackages.map { pkg ->
                                mapByPkg[pkg] ?: InstalledAppItem(
                                    packageName = pkg,
                                    appName = pkg.substringAfterLast('.').replaceFirstChar { it.uppercase() },
                                    isSystemApp = false,
                                    isPermanentlyProtected = false,
                                    iconBitmap = null
                                )
                            }.sortedBy { it.appName.lowercase(Locale.US) }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            exceptionAppItems.forEach { app ->
                                Surface(
                                    onClick = { toggleExceptionApp(app.packageName) },
                                    shape = CircleShape,
                                    color = roles.s4,
                                    contentColor = roles.onS
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Security,
                                            contentDescription = null,
                                            tint = roles.primary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = app.appName,
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp,
                                            maxLines = 1
                                        )
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Remove exception ${app.appName}",
                                            tint = roles.onSv,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Expandable User-Enabled Apps List with Checkboxes & Pinned-at-Top Sorting
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
                                        text = "Search user enabled apps...",
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
                            .heightIn(max = 300.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(roles.s3),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        if (filteredAndPinnedApps.isEmpty()) {
                            item(key = "__no_user_apps__") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (installedApps.isEmpty()) {
                                            "Loading user enabled apps..."
                                        } else {
                                            "No matching user enabled apps found"
                                        },
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontSize = 12.5.sp,
                                        color = roles.onSv
                                    )
                                }
                            }
                        } else {
                            items(
                                items = filteredAndPinnedApps,
                                key = { it.packageName }
                            ) { app ->
                                val isChecked = app.packageName in checkedFreezePackages
                                val isException = app.packageName in exceptionPackages
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { toggleSingleApp(app.packageName) }
                                        .background(
                                            if (isChecked) roles.pc.copy(alpha = 0.28f) else Color.Transparent
                                        )
                                        .padding(horizontal = 14.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    if (app.iconBitmap != null) {
                                        Image(
                                            bitmap = app.iconBitmap.asImageBitmap(),
                                            contentDescription = app.appName,
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(9.dp))
                                        )
                                    } else {
                                        Surface(
                                            shape = RoundedCornerShape(9.dp),
                                            color = roles.s4,
                                            modifier = Modifier.size(34.dp)
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
                                            if (!isChecked && isException) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = roles.s4,
                                                    contentColor = roles.onSv
                                                ) {
                                                    Text(
                                                        text = "EXCEPTION",
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
                                        onCheckedChange = { toggleSingleApp(app.packageName) },
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

            // Clean Action Pills (Uniform X & Y plane via AlignedKernelPill)
            val targetPayload = remember(isAllUserEnabledChecked, checkedFreezePackages) {
                when {
                    isAllUserEnabledChecked -> KernelRamLabRepository.ALL_USER_ENABLED_APPS_TOKEN
                    checkedFreezePackages.isNotEmpty() -> checkedFreezePackages.joinToString(",")
                    else -> ""
                }
            }

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
                    AlignedKernelPill(
                        text = label,
                        backgroundColor = if (isPrimaryTone) roles.pc else roles.s4,
                        contentColor = if (isPrimaryTone) roles.onPc else roles.onS,
                        heightDp = 32,
                        horizontalPaddingDp = 14,
                        fontSizeSp = 12f,
                        onClick = { onExecuteProcessAction(targetPayload, code) }
                    )
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
