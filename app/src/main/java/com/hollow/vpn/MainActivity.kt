package com.hollow.vpn

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hollow.vpn.ui.VpnScreen
import com.hollow.vpn.ui.VpnViewModel
import com.hollow.vpn.ui.components.ServerQrShareDialog
import com.hollow.vpn.ui.screens.AddServerScreen
import com.hollow.vpn.ui.screens.ConnectedHomeScreen
import com.hollow.vpn.ui.screens.CountryServerListScreen
import com.hollow.vpn.ui.screens.RawXrayJsonEditorDialog
import com.hollow.vpn.ui.screens.SettingsScreen
import com.hollow.vpn.ui.screens.SplashFetchingScreen
import com.hollow.vpn.ui.theme.HollowVpnTheme
import com.hollow.vpn.vpn.XrayVpnService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HollowVpnTheme {
                HollowVpnApp()
            }
        }
    }
}

@Composable
fun HollowVpnApp(
    vpnViewModel: VpnViewModel = viewModel()
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val currentScreen by vpnViewModel.currentScreen.collectAsStateWithLifecycle()
    val allServers by vpnViewModel.allServers.collectAsStateWithLifecycle()
    val filteredServers by vpnViewModel.filteredServers.collectAsStateWithLifecycle()
    val selectedFilter by vpnViewModel.selectedFilter.collectAsStateWithLifecycle()
    val selectedServer by vpnViewModel.selectedServer.collectAsStateWithLifecycle()
    val telemetry by vpnViewModel.telemetry.collectAsStateWithLifecycle()
    val settings by vpnViewModel.settings.collectAsStateWithLifecycle()
    val fetchingStatus by vpnViewModel.fetchingStatusMessage.collectAsStateWithLifecycle()
    val snackbarMessage by vpnViewModel.snackbarMessage.collectAsStateWithLifecycle()
    val activeAddTab by vpnViewModel.activeAddTab.collectAsStateWithLifecycle()
    val inspectingJsonServer by vpnViewModel.inspectingJsonServer.collectAsStateWithLifecycle()
    val sharingQrServer by vpnViewModel.sharingQrServer.collectAsStateWithLifecycle()
    val xrayLogs by XrayVpnService.logs.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(settings.hideFromRecentApps) {
        try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            am?.appTasks?.firstOrNull()?.setExcludeFromRecents(settings.hideFromRecentApps)
        } catch (_: Exception) {
        }
    }

    LaunchedEffect(snackbarMessage) {
        val msg = snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        vpnViewModel.clearSnackbar()
    }

    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            vpnViewModel.toggleConnection()
        } else {
            vpnViewModel.showMessage("VPN permission was denied")
        }
    }

    var pendingNotificationAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var notificationPromptedThisSession by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        pendingNotificationAction?.invoke()
        pendingNotificationAction = null
    }

    val runWithNotificationPermission: (() -> Unit) -> Unit = { action ->
        val shouldRequest = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !notificationPromptedThisSession &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        if (shouldRequest) {
            notificationPromptedThisSession = true
            pendingNotificationAction = action
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            action()
        }
    }

    val startOrToggleVpnConsent = {
        val prepareIntent = try {
            if (settings.tunMode && !telemetry.isConnected && !telemetry.isConnecting) {
                VpnService.prepare(context)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
        if (prepareIntent != null && context is Activity) {
            vpnPermissionLauncher.launch(prepareIntent)
        } else {
            vpnViewModel.toggleConnection()
        }
    }

    val requestOrToggleVpn = {
        if (telemetry.isConnected || telemetry.isConnecting) {
            vpnViewModel.toggleConnection()
        } else {
            runWithNotificationPermission(startOrToggleVpnConsent)
        }
    }

    val showTopLevelNav = currentScreen == VpnScreen.CONNECTED_HOME || currentScreen == VpnScreen.SERVER_LIST

    val navItems = remember {
        listOf(
            Triple(VpnScreen.CONNECTED_HOME, Icons.Rounded.Dashboard, Icons.Outlined.Dashboard to "Connect"),
            Triple(VpnScreen.SERVER_LIST, Icons.Rounded.Dns, Icons.Outlined.Dns to "Servers")
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = showTopLevelNav && maxWidth >= 600.dp

        Row(modifier = Modifier.fillMaxSize()) {
            if (useNavigationRail) {
                NavigationRail(
                    containerColor = scheme.surfaceContainer,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Spacer(Modifier.weight(1f))
                    navItems.forEach { (screen, activeIcon, inactiveAndLabel) ->
                        val selected = currentScreen == screen
                        val (inactiveIcon, label) = inactiveAndLabel
                        NavigationRailItem(
                            selected = selected,
                            onClick = { vpnViewModel.navigateTo(screen) },
                            icon = {
                                Icon(
                                    imageVector = if (selected) activeIcon else inactiveIcon,
                                    contentDescription = label
                                )
                            },
                            label = { Text(label) }
                        )
                    }
                    Spacer(Modifier.weight(1f))
                }
            }

            Scaffold(
                modifier = Modifier.weight(1f),
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState) { data ->
                        Snackbar(
                            snackbarData = data,
                            shape = RoundedCornerShape(18.dp),
                            containerColor = scheme.inverseSurface,
                            contentColor = scheme.inverseOnSurface
                        )
                    }
                },
                bottomBar = {
                    if (showTopLevelNav && !useNavigationRail) {
                        NavigationBar(
                            containerColor = scheme.surfaceContainer
                        ) {
                            navItems.forEach { (screen, activeIcon, inactiveAndLabel) ->
                                val selected = currentScreen == screen
                                val (inactiveIcon, label) = inactiveAndLabel
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { vpnViewModel.navigateTo(screen) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) activeIcon else inactiveIcon,
                                            contentDescription = label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.fillMaxSize()) {
                    Crossfade(
                        targetState = currentScreen,
                        animationSpec = tween(durationMillis = 220),
                        label = "vpn_screen_crossfade"
                    ) { screen ->
                        when (screen) {
                            VpnScreen.SPLASH_FETCHING -> {
                                SplashFetchingScreen(
                                    statusSubtitle = fetchingStatus,
                                    onCancelOrSkip = {
                                        vpnViewModel.navigateTo(VpnScreen.SERVER_LIST)
                                    }
                                )
                            }

                            VpnScreen.SERVER_LIST -> {
                                CountryServerListScreen(
                                    servers = filteredServers,
                                    selectedFilter = selectedFilter,
                                    activeProtocolPreference = settings.protocolPreference,
                                    bottomContentPadding = innerPadding.calculateBottomPadding(),
                                    onSelectFilter = vpnViewModel::setFilter,
                                    onResetProtocolFilter = {
                                        vpnViewModel.updateSettings { it.copy(protocolPreference = "Auto (V2Ray)") }
                                    },
                                    onSelectServer = { server ->
                                        runWithNotificationPermission {
                                            val prepareIntent = try {
                                                if (settings.tunMode) VpnService.prepare(context) else null
                                            } catch (_: Exception) {
                                                null
                                            }
                                            if (prepareIntent != null && context is Activity) {
                                                vpnViewModel.selectServer(server, openConnectedScreen = true)
                                                vpnPermissionLauncher.launch(prepareIntent)
                                            } else {
                                                vpnViewModel.selectAndConnectServer(
                                                    server = server,
                                                    openConnectedScreen = true
                                                )
                                            }
                                        }
                                    },
                                    onRefresh = {
                                        vpnViewModel.runFletcherAutoFetchAndPingSweep(
                                            returnScreen = VpnScreen.SERVER_LIST
                                        )
                                    },
                                    onOpenSettings = {
                                        vpnViewModel.navigateTo(VpnScreen.SETTINGS)
                                    },
                                    onOpenAddServer = {
                                        vpnViewModel.navigateTo(VpnScreen.ADD_SERVER)
                                    },
                                    onPingServer = vpnViewModel::pingSingleServer,
                                    onRepingAll = vpnViewModel::repingAllSavedServers,
                                    onShowMessage = vpnViewModel::showMessage,
                                    onShareQr = vpnViewModel::openQrShare,
                                    onInspectJson = vpnViewModel::openJsonInspector,
                                    onDeleteServer = vpnViewModel::deleteServer,
                                    onBack = {
                                        if (!vpnViewModel.navigateBack()) {
                                            vpnViewModel.navigateTo(VpnScreen.CONNECTED_HOME)
                                        }
                                    }
                                )
                            }

                            VpnScreen.CONNECTED_HOME -> {
                                ConnectedHomeScreen(
                                    selectedServer = selectedServer,
                                    telemetry = telemetry,
                                    tunModeEnabled = settings.tunMode,
                                    bottomContentPadding = innerPadding.calculateBottomPadding(),
                                    onToggleConnect = requestOrToggleVpn,
                                    onOpenServerList = {
                                        vpnViewModel.navigateTo(VpnScreen.SERVER_LIST)
                                    },
                                    onOpenSettings = {
                                        vpnViewModel.navigateTo(VpnScreen.SETTINGS)
                                    },
                                    onOpenAddServer = {
                                        vpnViewModel.navigateTo(VpnScreen.ADD_SERVER)
                                    },
                                    onRefreshRealIp = vpnViewModel::refreshRealPublicIp
                                )
                            }

                            VpnScreen.SETTINGS -> {
                                SettingsScreen(
                                    settings = settings,
                                    xrayLogs = xrayLogs,
                                    serverCount = allServers.size,
                                    onUpdateSettings = vpnViewModel::updateSettings,
                                    onOpenAddSubscription = {
                                        vpnViewModel.navigateTo(VpnScreen.ADD_SERVER)
                                    },
                                    onClearXrayLogs = vpnViewModel::clearXrayLogs,
                                    onExportServers = {
                                        val servers = allServers
                                        val payload = vpnViewModel.buildExportBackupPayload(servers)
                                        if (payload.isBlank()) {
                                            vpnViewModel.showMessage("No saved servers to export")
                                        } else {
                                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                            cm?.setPrimaryClip(android.content.ClipData.newPlainText("Hollow VPN Servers", payload))
                                            vpnViewModel.showMessage("Exported ${servers.size} server(s) to clipboard")
                                        }
                                    },
                                    onShowMessage = vpnViewModel::showMessage,
                                    onClearCache = vpnViewModel::clearServerCacheAndReset,
                                    onBack = {
                                        if (!vpnViewModel.navigateBack()) {
                                            vpnViewModel.navigateTo(VpnScreen.CONNECTED_HOME)
                                        }
                                    }
                                )
                            }

                            VpnScreen.ADD_SERVER -> {
                                AddServerScreen(
                                    activeTab = activeAddTab,
                                    settings = settings,
                                    onSelectTab = vpnViewModel::setAddServerTab,
                                    onAddAndFetchSubscription = vpnViewModel::addAndFetchSubscriptionUrl,
                                    onImportClipboardText = vpnViewModel::importFromTextOrClipboard,
                                    onAddManualServer = vpnViewModel::addManualCustomServer,
                                    onUpdateSettings = vpnViewModel::updateSettings,
                                    onBack = {
                                        if (!vpnViewModel.navigateBack()) {
                                            vpnViewModel.navigateTo(VpnScreen.SERVER_LIST)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    inspectingJsonServer?.let { server ->
                        RawXrayJsonEditorDialog(
                            server = server,
                            onDismiss = vpnViewModel::closeJsonInspector,
                            onSaveJson = { updatedJson ->
                                vpnViewModel.saveEditedServerJson(server, updatedJson)
                            }
                        )
                    }

                    sharingQrServer?.let { server ->
                        ServerQrShareDialog(
                            server = server,
                            onCopyPayload = { payload ->
                                if (payload.isNotBlank()) {
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                    cm?.setPrimaryClip(android.content.ClipData.newPlainText("Hollow VPN Server", payload))
                                    vpnViewModel.showMessage("Copied server config for ${server.countryName}")
                                    vpnViewModel.closeQrShare()
                                }
                            },
                            onDismiss = vpnViewModel::closeQrShare
                        )
                    }
                }
            }
        }
    }
}
