package com.hollow.vpn.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hollow.vpn.R
import com.hollow.vpn.data.AppSettingsState
import com.hollow.vpn.data.ServerEntity
import com.hollow.vpn.ui.components.HollowAtmosphereBackground
import com.hollow.vpn.ui.theme.GroupedItemShapes
import com.hollow.vpn.ui.theme.JetBrainsMonoFamily

data class SettingChoiceOption(
    val value: String,
    val subtitle: String,
    val label: String = value
)

private enum class ActiveSettingDialog {
    UPDATE_INTERVAL,
    PROTOCOL,
    ROUTING_MODE,
    DNS_PROVIDER,
    XRAY_LOGS,
    CLEAR_CACHE_CONFIRM
}

internal val UPDATE_INTERVAL_OPTIONS = listOf(
    SettingChoiceOption("15 min", "High-frequency auto-refresh"),
    SettingChoiceOption("30 min", "Balanced refresh interval"),
    SettingChoiceOption("1 hour", "Recommended standard interval"),
    SettingChoiceOption("6 hours", "Battery-saving interval"),
    SettingChoiceOption("12 hours", "Twice-daily subscription refresh"),
    SettingChoiceOption("24 hours", "Daily subscription refresh")
)

private val PROTOCOL_OPTIONS = listOf(
    SettingChoiceOption("Auto (V2Ray)", "Automatically select the fastest available route", "Auto (Recommended)"),
    SettingChoiceOption("VLESS (Reality)", "Reality TLS stealth camouflage with ultra-low latency", "VLESS · Reality"),
    SettingChoiceOption("VLESS (WS / TCP)", "Lightweight stateless stream transport", "VLESS · WS / TCP"),
    SettingChoiceOption("VMess (WebSocket)", "Encrypted WebSocket tunnel", "VMess · WebSocket"),
    SettingChoiceOption("Trojan (TLS)", "HTTPS-disguised TLS tunnel", "Trojan · TLS"),
    SettingChoiceOption("Hysteria2", "QUIC-based high-throughput stealth tunnel", "Hysteria2"),
    SettingChoiceOption("gRPC", "Multiplexed HTTP/2 stream transport", "gRPC"),
    SettingChoiceOption("Shadowsocks", "Fast stream cipher proxy", "Shadowsocks")
)

private val ROUTING_MODE_OPTIONS = listOf(
    SettingChoiceOption("Global", "Route all device traffic through the VPN tunnel"),
    SettingChoiceOption("Bypass LAN", "Tunnel internet traffic while keeping local LAN (192.168.x.x) direct"),
    SettingChoiceOption("Bypass LAN & Domestic", "Keep local & domestic subnets direct; tunnel international traffic"),
    SettingChoiceOption("Direct Only", "Diagnostic mode with direct connection")
)

private val DNS_PROVIDER_OPTIONS = listOf(
    SettingChoiceOption("Auto (DoH)", "Encrypted DNS-over-HTTPS (1.1.1.1 / 1.0.0.1)"),
    SettingChoiceOption("Cloudflare 1.1.1.1", "Fast privacy-first DNS resolver (1.1.1.1)"),
    SettingChoiceOption("Google 8.8.8.8", "Reliable global DNS resolver (8.8.8.8)"),
    SettingChoiceOption("Quad9 9.9.9.9", "Security & malware-blocking DNS (9.9.9.9)"),
    SettingChoiceOption("AdGuard 94.140.14.14", "Ad-blocking and tracker-filtering DNS (94.140.14.14)")
)

/**
 * SCREEN 4: Pure Material 3 Expressive Settings Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettingsState,
    xrayLogs: List<String> = emptyList(),
    serverCount: Int = 0,
    onUpdateSettings: ((AppSettingsState) -> AppSettingsState) -> Unit,
    onOpenAddSubscription: () -> Unit,
    onClearXrayLogs: () -> Unit = {},
    onExportServers: () -> Unit = {},
    onShowMessage: (String) -> Unit = {},
    onClearCache: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var activeDialog by remember { mutableStateOf<ActiveSettingDialog?>(null) }
    var showPerAppPicker by remember { mutableStateOf(false) }

    HollowAtmosphereBackground {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = Color.Transparent,
            topBar = {
                LargeTopAppBar(
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = scheme.surfaceContainer
                    ),
                    navigationIcon = {
                        FilledTonalIconButton(
                            onClick = onBack,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = scheme.surfaceContainerHigh
                            ),
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .testTag("btn_navigate_back")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.settings_title),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 600.dp)
                        .align(Alignment.TopCenter)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Tunnel, routing & privacy",
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 6.dp, bottom = 14.dp)
                    )

                    // Section 1: Configuration (3 expressive grouped cards)
                    SettingsGroupCard(sectionTitle = stringResource(R.string.section_configuration)) {
                        SettingsToggleRow(
                            icon = Icons.Outlined.CloudQueue,
                            title = stringResource(R.string.setting_auto_configure),
                            subtitle = stringResource(R.string.setting_auto_configure_sub),
                            checked = settings.autoConfigure,
                            onCheckedChange = { enabled ->
                                onUpdateSettings { it.copy(autoConfigure = enabled) }
                            },
                            shape = GroupedItemShapes.Top,
                            testTag = "switch_auto_configure"
                        )
                        SettingsValueNavigationRow(
                            icon = Icons.Outlined.Sync,
                            title = stringResource(R.string.setting_update_interval),
                            subtitle = stringResource(R.string.setting_update_interval_sub),
                            valueText = settings.updateIntervalLabel,
                            onClick = { activeDialog = ActiveSettingDialog.UPDATE_INTERVAL },
                            shape = GroupedItemShapes.Middle,
                            testTag = "row_update_interval"
                        )
                        SettingsValueNavigationRow(
                            icon = Icons.Outlined.Link,
                            title = stringResource(R.string.setting_custom_subscription),
                            subtitle = stringResource(R.string.setting_custom_subscription_sub),
                            valueText = null,
                            onClick = onOpenAddSubscription,
                            shape = GroupedItemShapes.Bottom,
                            testTag = "row_custom_subscription"
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Section 2: Connection (dynamic item count so shapes adapt smoothly)
                    val connectionRowCount = if (settings.perAppProxyEnabled) 9 else 7
                    var connectionIndex = 0

                    SettingsGroupCard(sectionTitle = stringResource(R.string.section_connection)) {
                        SettingsValueNavigationRow(
                            icon = Icons.Rounded.AccountTree,
                            title = stringResource(R.string.setting_protocol),
                            subtitle = null,
                            valueText = formatConsumerProtocol(settings.protocolPreference),
                            onClick = { activeDialog = ActiveSettingDialog.PROTOCOL },
                            shape = GroupedItemShapes.forIndex(connectionIndex++, connectionRowCount),
                            testTag = "row_protocol"
                        )
                        SettingsValueNavigationRow(
                            icon = Icons.Rounded.Route,
                            title = stringResource(R.string.setting_routing_mode),
                            subtitle = null,
                            valueText = settings.routingMode,
                            onClick = { activeDialog = ActiveSettingDialog.ROUTING_MODE },
                            shape = GroupedItemShapes.forIndex(connectionIndex++, connectionRowCount),
                            testTag = "row_routing_mode"
                        )
                        SettingsToggleRow(
                            icon = Icons.Outlined.Shield,
                            title = "Per-app proxy",
                            subtitle = "Proxy only selected apps, or bypass them",
                            checked = settings.perAppProxyEnabled,
                            onCheckedChange = { enabled ->
                                onUpdateSettings { it.copy(perAppProxyEnabled = enabled) }
                            },
                            shape = GroupedItemShapes.forIndex(connectionIndex++, connectionRowCount),
                            testTag = "switch_per_app_proxy"
                        )
                        if (settings.perAppProxyEnabled) {
                            SettingsValueNavigationRow(
                                icon = Icons.Outlined.Apps,
                                title = "Choose apps",
                                subtitle = "Select packages affected by the VPN",
                                valueText = "${settings.selectedAppPackages.size} selected",
                                onClick = { showPerAppPicker = true },
                                shape = GroupedItemShapes.forIndex(connectionIndex++, connectionRowCount),
                                testTag = "row_per_app_selection"
                            )
                            SettingsToggleRow(
                                icon = Icons.Outlined.SwapHoriz,
                                title = "Bypass selected apps",
                                subtitle = "Selected apps stay on the normal connection",
                                checked = settings.bypassSelectedApps,
                                onCheckedChange = { enabled ->
                                    onUpdateSettings { it.copy(bypassSelectedApps = enabled) }
                                },
                                shape = GroupedItemShapes.forIndex(connectionIndex++, connectionRowCount),
                                testTag = "switch_bypass_selected_apps"
                            )
                        }
                        SettingsValueNavigationRow(
                            icon = Icons.Outlined.Dns,
                            title = stringResource(R.string.setting_dns),
                            subtitle = null,
                            valueText = settings.dnsMode,
                            onClick = { activeDialog = ActiveSettingDialog.DNS_PROVIDER },
                            shape = GroupedItemShapes.forIndex(connectionIndex++, connectionRowCount),
                            testTag = "row_dns"
                        )
                        SettingsToggleRow(
                            icon = Icons.Outlined.Shield,
                            title = stringResource(R.string.setting_tun_mode),
                            subtitle = stringResource(R.string.setting_tun_mode_sub),
                            checked = settings.tunMode,
                            onCheckedChange = { enabled ->
                                onUpdateSettings { it.copy(tunMode = enabled) }
                            },
                            shape = GroupedItemShapes.forIndex(connectionIndex++, connectionRowCount),
                            testTag = "switch_tun_mode"
                        )
                        SettingsToggleRow(
                            icon = Icons.Outlined.Language,
                            title = stringResource(R.string.setting_ipv6),
                            subtitle = stringResource(R.string.setting_ipv6_sub),
                            checked = settings.ipv6Enabled,
                            onCheckedChange = { enabled ->
                                onUpdateSettings { it.copy(ipv6Enabled = enabled) }
                            },
                            shape = GroupedItemShapes.forIndex(connectionIndex++, connectionRowCount),
                            testTag = "switch_ipv6"
                        )
                        SettingsValueNavigationRow(
                            icon = Icons.Outlined.Lock,
                            title = "Always-On & Kill Switch",
                            subtitle = "Enable Always-On VPN & Block connections without VPN",
                            valueText = "Android",
                            onClick = {
                                try {
                                    context.startActivity(
                                        Intent(Settings.ACTION_VPN_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    )
                                } catch (_: Exception) {
                                }
                            },
                            shape = GroupedItemShapes.forIndex(connectionIndex, connectionRowCount),
                            testTag = "row_system_vpn_settings"
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Section 3: App (expressive grouped cards)
                    val appRowCount = 5
                    var appRowIndex = 0
                    SettingsGroupCard(sectionTitle = stringResource(R.string.section_app)) {
                        SettingsToggleRow(
                            icon = Icons.Outlined.PlayArrow,
                            title = stringResource(R.string.setting_start_on_open),
                            subtitle = stringResource(R.string.setting_start_on_open_sub),
                            checked = settings.startOnAppOpen,
                            onCheckedChange = { enabled ->
                                onUpdateSettings { it.copy(startOnAppOpen = enabled) }
                            },
                            shape = GroupedItemShapes.forIndex(appRowIndex++, appRowCount),
                            testTag = "switch_start_on_open"
                        )
                        SettingsToggleRow(
                            icon = Icons.Outlined.VisibilityOff,
                            title = stringResource(R.string.setting_hide_recents),
                            subtitle = stringResource(R.string.setting_hide_recents_sub),
                            checked = settings.hideFromRecentApps,
                            onCheckedChange = { enabled ->
                                onUpdateSettings { it.copy(hideFromRecentApps = enabled) }
                            },
                            shape = GroupedItemShapes.forIndex(appRowIndex++, appRowCount),
                            testTag = "switch_hide_recents"
                        )
                        SettingsValueNavigationRow(
                            icon = Icons.Outlined.Terminal,
                            title = "Diagnostic Logs",
                            subtitle = "View live Xray core status & connection events",
                            valueText = if (xrayLogs.isEmpty()) "Empty" else "${xrayLogs.size}",
                            onClick = { activeDialog = ActiveSettingDialog.XRAY_LOGS },
                            shape = GroupedItemShapes.forIndex(appRowIndex++, appRowCount),
                            testTag = "row_xray_logs"
                        )
                        SettingsValueNavigationRow(
                            icon = Icons.Outlined.UploadFile,
                            title = "Export Servers",
                            subtitle = "Copy all saved server configs to clipboard",
                            valueText = "$serverCount",
                            onClick = onExportServers,
                            shape = GroupedItemShapes.forIndex(appRowIndex++, appRowCount),
                            testTag = "row_export_servers"
                        )
                        SettingsValueNavigationRow(
                            icon = Icons.Outlined.DeleteOutline,
                            title = stringResource(R.string.setting_clear_cache),
                            subtitle = stringResource(R.string.setting_clear_cache_sub),
                            valueText = null,
                            isDestructive = true,
                            onClick = { activeDialog = ActiveSettingDialog.CLEAR_CACHE_CONFIRM },
                            shape = GroupedItemShapes.forIndex(appRowIndex, appRowCount),
                            testTag = "row_clear_cache"
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                    Spacer(modifier = Modifier.navigationBarsPadding())
                }
            }
        }
    }

    when (activeDialog) {
        ActiveSettingDialog.UPDATE_INTERVAL -> {
            SettingOptionPickerDialog(
                title = "Select Update Interval",
                options = UPDATE_INTERVAL_OPTIONS,
                selectedValue = settings.updateIntervalLabel,
                onSelect = { chosen ->
                    onUpdateSettings {
                        it.copy(updateIntervalLabel = chosen, subUpdateInterval = chosen)
                    }
                    activeDialog = null
                },
                onDismiss = { activeDialog = null }
            )
        }

        ActiveSettingDialog.PROTOCOL -> {
            SettingOptionPickerDialog(
                title = "Connection Protocol",
                options = PROTOCOL_OPTIONS,
                selectedValue = settings.protocolPreference,
                onSelect = { chosen ->
                    onUpdateSettings { it.copy(protocolPreference = chosen) }
                    activeDialog = null
                },
                onDismiss = { activeDialog = null }
            )
        }

        ActiveSettingDialog.ROUTING_MODE -> {
            SettingOptionPickerDialog(
                title = "Select Routing Mode",
                options = ROUTING_MODE_OPTIONS,
                selectedValue = settings.routingMode,
                onSelect = { chosen ->
                    onUpdateSettings { it.copy(routingMode = chosen) }
                    activeDialog = null
                },
                onDismiss = { activeDialog = null }
            )
        }

        ActiveSettingDialog.DNS_PROVIDER -> {
            SettingOptionPickerDialog(
                title = "Select DNS Provider",
                options = DNS_PROVIDER_OPTIONS,
                selectedValue = settings.dnsMode,
                onSelect = { chosen ->
                    onUpdateSettings { it.copy(dnsMode = chosen) }
                    activeDialog = null
                },
                onDismiss = { activeDialog = null }
            )
        }

        ActiveSettingDialog.XRAY_LOGS -> {
            XrayDiagnosticLogsDialog(
                logs = xrayLogs,
                onCopyLogs = {
                    val joined = xrayLogs.joinToString("\n")
                    if (joined.isNotBlank()) {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        cm?.setPrimaryClip(ClipData.newPlainText("Hollow VPN Xray Logs", joined))
                        onShowMessage("Copied ${xrayLogs.size} log line(s)")
                    }
                },
                onClearLogs = onClearXrayLogs,
                onDismiss = { activeDialog = null }
            )
        }

        ActiveSettingDialog.CLEAR_CACHE_CONFIRM -> {
            AlertDialog(
                onDismissRequest = { activeDialog = null },
                containerColor = scheme.surfaceContainerHigh,
                shape = RoundedCornerShape(28.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = null,
                        tint = scheme.error
                    )
                },
                title = {
                    Text(
                        text = "Clear Cached Servers?",
                        style = MaterialTheme.typography.titleLarge,
                        color = scheme.onSurface
                    )
                },
                text = {
                    Text(
                        text = "This removes all currently cached servers from your device. You can re-fetch fresh nodes anytime from your subscription link.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            activeDialog = null
                            onClearCache()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = scheme.error,
                            contentColor = scheme.onError
                        )
                    ) {
                        Text("Clear Cache")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        null -> Unit
    }

    if (showPerAppPicker) {
        PerAppProxyPickerDialog(
            selectedPackages = settings.selectedAppPackages,
            onSave = { packages ->
                onUpdateSettings { it.copy(selectedAppPackages = packages) }
                showPerAppPicker = false
            },
            onDismiss = { showPerAppPicker = false }
        )
    }
}

@Composable
private fun XrayDiagnosticLogsDialog(
    logs: List<String>,
    onCopyLogs: () -> Unit,
    onClearLogs: () -> Unit,
    onDismiss: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = scheme.surfaceContainerHigh,
        shape = RoundedCornerShape(28.dp),
        icon = {
            Icon(
                imageVector = Icons.Outlined.Terminal,
                contentDescription = null,
                tint = scheme.primary
            )
        },
        title = {
            Text(
                text = "Xray Diagnostic Logs",
                style = MaterialTheme.typography.titleLarge,
                color = scheme.onSurface
            )
        },
        text = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp),
                shape = RoundedCornerShape(18.dp),
                color = scheme.surfaceContainerLowest
            ) {
                if (logs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No diagnostic events captured yet.\nConnect to a server to inspect live Xray core status and tunnel verification logs.",
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant
                        )
                    }
                } else {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        logs.forEach { line ->
                            Text(
                                text = line,
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    color = if (line.contains("ERROR:", ignoreCase = true)) {
                                        scheme.error
                                    } else {
                                        scheme.onSurface
                                    }
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (logs.isNotEmpty()) {
                    TextButton(
                        onClick = onClearLogs,
                        modifier = Modifier.testTag("btn_clear_xray_logs")
                    ) {
                        Text("Clear", color = scheme.error)
                    }
                    Button(
                        onClick = onCopyLogs,
                        modifier = Modifier.testTag("btn_copy_xray_logs")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Copy")
                    }
                } else {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        },
        dismissButton = {
            if (logs.isNotEmpty()) {
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}

private data class ProxyAppChoice(val packageName: String, val label: String)

@Composable
private fun PerAppProxyPickerDialog(
    selectedPackages: Set<String>,
    onSave: (Set<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val apps = remember(context) {
        val pm = context.packageManager
        pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .asSequence()
            .filter { it.packageName != context.packageName && pm.getLaunchIntentForPackage(it.packageName) != null }
            .map { ProxyAppChoice(it.packageName, pm.getApplicationLabel(it).toString()) }
            .sortedBy { it.label.lowercase() }
            .toList()
    }
    var searchQuery by remember { mutableStateOf("") }
    var workingSelection by remember(selectedPackages) { mutableStateOf(selectedPackages) }
    val filteredApps = remember(apps, searchQuery) {
        val q = searchQuery.trim()
        if (q.isEmpty()) apps
        else apps.filter {
            it.label.contains(q, ignoreCase = true) || it.packageName.contains(q, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = scheme.surfaceContainerHigh,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                text = "Choose apps",
                style = MaterialTheme.typography.titleLarge,
                color = scheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(430.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search installed apps") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${workingSelection.size} selected",
                        color = scheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (workingSelection.isNotEmpty()) {
                        TextButton(onClick = { workingSelection = emptySet() }) {
                            Text("Clear", color = scheme.error)
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    filteredApps.forEachIndexed { idx, app ->
                        val checked = app.packageName in workingSelection
                        val itemShape = GroupedItemShapes.forIndex(idx, filteredApps.size)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(itemShape)
                                .clickable {
                                    workingSelection = if (checked) workingSelection - app.packageName
                                    else workingSelection + app.packageName
                                },
                            shape = itemShape,
                            color = if (checked) scheme.secondaryContainer else scheme.surfaceContainer
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = { value ->
                                        workingSelection = if (value) workingSelection + app.packageName
                                        else workingSelection - app.packageName
                                    }
                                )
                                Column(modifier = Modifier.padding(start = 6.dp)) {
                                    Text(
                                        text = app.label,
                                        color = scheme.onSurface,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = app.packageName,
                                        color = scheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(workingSelection) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingOptionPickerDialog(
    title: String,
    options: List<SettingChoiceOption>,
    selectedValue: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = scheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = scheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                options.forEachIndexed { idx, option ->
                    val isSelected = option.value == selectedValue
                    val itemShape = GroupedItemShapes.forIndex(idx, options.size)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .clip(itemShape)
                            .clickable(role = Role.RadioButton) { onSelect(option.value) },
                        shape = itemShape,
                        color = if (isSelected) scheme.primaryContainer else scheme.surfaceContainer
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onSelect(option.value) }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = option.label,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (isSelected) scheme.onPrimaryContainer else scheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = option.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) {
                                        scheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    } else {
                                        scheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            FilledTonalButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Close")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScreenTopBarWithBack(
    title: String,
    subtitle: String? = null,
    onBack: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = scheme.surfaceContainer
        ),
        navigationIcon = {
            FilledTonalIconButton(
                onClick = onBack,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = scheme.surfaceContainerHigh
                ),
                modifier = Modifier
                    .padding(start = 8.dp)
                    .testTag("btn_navigate_back")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = scheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    )
}

@Composable
internal fun SettingsGroupCard(
    sectionTitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = sectionTitle.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.8.sp),
            color = scheme.primary,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(3.dp),
            content = content
        )
    }
}

@Composable
internal fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    shape: Shape = GroupedItemShapes.Single,
    testTag: String
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clip(shape)
            .clickable(role = Role.Switch) { onCheckedChange(!checked) }
            .testTag(testTag),
        shape = shape,
        color = scheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (checked) scheme.primaryContainer else scheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (checked) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = scheme.onSurface
                )
                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                thumbContent = {
                    Icon(
                        imageVector = if (checked) Icons.Rounded.Check else Icons.Rounded.Close,
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize)
                    )
                }
            )
        }
    }
}

@Composable
internal fun SettingsValueNavigationRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    valueText: String?,
    isDestructive: Boolean = false,
    shape: Shape = GroupedItemShapes.Single,
    onClick: () -> Unit,
    testTag: String
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clip(shape)
            .clickable(role = Role.Button, onClick = onClick)
            .testTag(testTag),
        shape = shape,
        color = scheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDestructive) scheme.errorContainer else scheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDestructive) scheme.onErrorContainer else scheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isDestructive) scheme.error else scheme.onSurface
                )
                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
            }
            if (!valueText.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = scheme.secondaryContainer
                ) {
                    Text(
                        text = valueText,
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.onSecondaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Pure Material 3 Dialog for viewing and editing Raw Server JSON configurations.
 */
@Composable
fun RawXrayJsonEditorDialog(
    server: ServerEntity,
    onDismiss: () -> Unit,
    onSaveJson: (String) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    var jsonText by remember(server) { mutableStateOf(server.rawConfigJson) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = scheme.surfaceContainerHigh,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                text = "${server.countryName} · Raw Config",
                style = MaterialTheme.typography.titleLarge,
                color = scheme.onSurface
            )
        },
        text = {
            OutlinedTextField(
                value = jsonText,
                onValueChange = { jsonText = it },
                textStyle = TextStyle(
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 12.sp,
                    color = scheme.onSurface
                ),
                shape = RoundedCornerShape(16.dp),
                minLines = 10,
                maxLines = 15,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onSaveJson(jsonText) }) {
                Text("Save Config")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
