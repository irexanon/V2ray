package com.hollow.vpn.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hollow.vpn.R
import com.hollow.vpn.data.ServerEntity
import com.hollow.vpn.data.ServerFilter
import com.hollow.vpn.ui.ConnectionTelemetryState
import com.hollow.vpn.ui.components.CircularCountryFlag
import com.hollow.vpn.ui.components.FetchingSpinnerRing
import com.hollow.vpn.ui.components.HollowAtmosphereBackground
import com.hollow.vpn.ui.components.HollowVpnLogoCard
import com.hollow.vpn.ui.components.SignalStrengthBars
import com.hollow.vpn.ui.components.TunnelPowerReactor
import com.hollow.vpn.ui.theme.GroupedItemShapes
import com.hollow.vpn.ui.theme.SignalMediumDark
import com.hollow.vpn.ui.theme.SignalMediumLight
import com.hollow.vpn.ui.theme.SignalStrongDark
import com.hollow.vpn.ui.theme.SignalStrongLight
import com.hollow.vpn.ui.theme.SignalWeakDark
import com.hollow.vpn.ui.theme.SignalWeakLight
import com.hollow.vpn.ui.theme.TabularMonoBody
import com.hollow.vpn.ui.theme.TabularMonoLarge

/**
 * Formats internal protocol identifiers cleanly for consumer UI without technical clutter.
 */
internal fun formatConsumerProtocol(raw: String, transport: String? = null): String {
    val cleaned = raw
        .replace("(V2Ray)", "", ignoreCase = true)
        .replace("(Xray)", "", ignoreCase = true)
        .replace("V2Ray · ", "", ignoreCase = true)
        .replace("Xray ", "", ignoreCase = true)
        .trim()
        .ifBlank { "Auto" }

    val cleanTransport = transport?.trim().orEmpty()
    return if (cleanTransport.isNotBlank() && !cleaned.contains(cleanTransport, ignoreCase = true)) {
        "$cleaned · $cleanTransport"
    } else {
        cleaned
    }
}

/**
 * SCREEN 1: Material 3 Expressive Startup Discovery & Auto-Benchmark Splash Screen
 */
@Composable
fun SplashFetchingScreen(
    statusSubtitle: String,
    onCancelOrSkip: () -> Unit
) {
    BackHandler { onCancelOrSkip() }
    val scheme = MaterialTheme.colorScheme
    val displaySubtitle = statusSubtitle
        .replace("V2Ray / Xray ", "", ignoreCase = true)
        .replace("V2Ray/Xray ", "", ignoreCase = true)

    HollowAtmosphereBackground(
        isConnecting = true,
        showWorldMap = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .widthIn(max = 520.dp)
                .align(Alignment.Center)
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            HollowVpnLogoCard(cardSize = 92.dp)

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displaySmall,
                color = scheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.tagline_fast_private),
                style = MaterialTheme.typography.bodyLarge,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = scheme.surfaceContainerHigh
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FetchingSpinnerRing(size = 64.dp)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.fetching_servers_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = scheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = displaySubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            FilledTonalButton(
                onClick = onCancelOrSkip,
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Text("Browse Servers")
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

/**
 * Legacy flat ServerListScreen updated with pure Material 3 Expressive components.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerListScreen(
    servers: List<ServerEntity>,
    selectedFilter: ServerFilter,
    onSelectFilter: (ServerFilter) -> Unit,
    onSelectServer: (ServerEntity) -> Unit,
    onRefreshFletcher: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAddServer: () -> Unit,
    onPingServer: (ServerEntity) -> Unit,
    onInspectJson: (ServerEntity) -> Unit,
    onDeleteServer: (ServerEntity) -> Unit,
    onShareQr: (ServerEntity) -> Unit = {},
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val scheme = MaterialTheme.colorScheme

    HollowAtmosphereBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = scheme.surfaceContainer
                    ),
                    title = {
                        Column {
                            Text(
                                text = stringResource(R.string.vpn_header_title),
                                style = MaterialTheme.typography.headlineMedium,
                                color = scheme.onBackground
                            )
                            Text(
                                text = stringResource(R.string.vpn_header_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        FilledTonalIconButton(
                            onClick = onOpenAddServer,
                            modifier = Modifier.testTag("btn_open_add_server")
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "Add Custom Server")
                        }
                        Spacer(Modifier.width(4.dp))
                        FilledTonalIconButton(
                            onClick = onRefreshFletcher,
                            modifier = Modifier.testTag("btn_refresh_fletcher")
                        ) {
                            Icon(Icons.Rounded.Refresh, contentDescription = "Refresh Servers")
                        }
                        Spacer(Modifier.width(4.dp))
                        FilledTonalIconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier.testTag("btn_open_settings")
                        ) {
                            Icon(Icons.Rounded.Settings, contentDescription = "Open Settings")
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .widthIn(max = 620.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ServerFilter.entries.forEach { filter ->
                        val isSelected = filter == selectedFilter
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectFilter(filter) },
                            label = { Text(filter.label) },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            modifier = Modifier.testTag("filter_chip_${filter.name.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (servers.isEmpty()) {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = scheme.surfaceContainer)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No servers match this filter",
                                style = MaterialTheme.typography.titleMedium,
                                color = scheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tap Refresh to find the fastest live locations.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = scheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    val topHighlighted = servers.first()
                    val restServers = servers.drop(1)

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("server_lazy_column"),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item(key = "top_${topHighlighted.id}") {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .clickable { onSelectServer(topHighlighted) }
                                    .testTag("server_card_featured"),
                                shape = RoundedCornerShape(24.dp),
                                color = scheme.primaryContainer
                            ) {
                                ServerItemRowContent(
                                    server = topHighlighted,
                                    showLightningBadge = true,
                                    onSelect = { onSelectServer(topHighlighted) },
                                    onPing = { onPingServer(topHighlighted) },
                                    onInspectJson = { onInspectJson(topHighlighted) },
                                    onDelete = { onDeleteServer(topHighlighted) },
                                    onShareQr = { onShareQr(topHighlighted) }
                                )
                            }
                        }

                        if (restServers.isNotEmpty()) {
                            item(key = "grouped_server_card") {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    restServers.forEachIndexed { index, server ->
                                        val shape = GroupedItemShapes.forIndex(index, restServers.size)
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(shape)
                                                .clickable { onSelectServer(server) }
                                                .testTag("server_row_${server.countryCode.lowercase()}_$index"),
                                            shape = shape,
                                            color = if (server.isSelected) scheme.secondaryContainer else scheme.surfaceContainer
                                        ) {
                                            ServerItemRowContent(
                                                server = server,
                                                showLightningBadge = false,
                                                onSelect = { onSelectServer(server) },
                                                onPing = { onPingServer(server) },
                                                onInspectJson = { onInspectJson(server) },
                                                onDelete = { onDeleteServer(server) },
                                                onShareQr = { onShareQr(server) }
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
    }
}

@Composable
fun ServerItemRowContent(
    server: ServerEntity,
    showLightningBadge: Boolean,
    onSelect: () -> Unit,
    onPing: () -> Unit,
    onInspectJson: () -> Unit,
    onDelete: () -> Unit,
    onCopyFeedback: (String) -> Unit = {},
    onShareQr: () -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme
    val isDark = scheme.background.luminance() < 0.5f
    var menuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val pingColor = when {
        server.pingMs in 1..89 -> if (isDark) SignalStrongDark else SignalStrongLight
        server.pingMs in 90..179 -> if (isDark) SignalMediumDark else SignalMediumLight
        else -> if (isDark) SignalWeakDark else SignalWeakLight
    }

    val cleanProtocol = remember(server.protocolCore, server.transport) {
        formatConsumerProtocol(server.protocolCore, server.transport)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 6.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularCountryFlag(
            countryCode = server.countryCode,
            size = 36.dp
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = server.countryName,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.5.sp),
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (server.isSelected) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = scheme.tertiaryContainer
                    ) {
                        Text(
                            text = "ACTIVE",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                            color = scheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = cleanProtocol,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${server.resolvedIp.ifBlank { server.host }}:${server.port}",
                style = TabularMonoBody.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Normal),
                color = scheme.onSurfaceVariant.copy(alpha = 0.78f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (showLightningBadge) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(scheme.tertiaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Bolt,
                    contentDescription = "Fastest Recommended Server",
                    tint = scheme.onTertiaryContainer,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SignalStrengthBars(pingMs = server.pingMs)
            Text(
                text = if (server.pingMs > 0) "${server.pingMs} ms" else "Timeout",
                style = TabularMonoBody.copy(fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold),
                color = pingColor
            )
        }

        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "Server Options for ${server.countryName}",
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Connect Now") },
                    onClick = {
                        menuExpanded = false
                        onSelect()
                    }
                )
                DropdownMenuItem(
                    text = {
                        val pingSub = if (server.pingMs > 0) "${server.pingMs} ms" else "Timeout"
                        Text("Ping Test ($pingSub)")
                    },
                    onClick = {
                        menuExpanded = false
                        onPing()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Inspect Raw Config") },
                    onClick = {
                        menuExpanded = false
                        onInspectJson()
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(if (server.shareUri.isNotBlank()) "Copy Share Link" else "Copy Raw Config")
                    },
                    onClick = {
                        menuExpanded = false
                        val payload = server.shareUri.ifBlank { server.rawConfigJson }
                        if (payload.isNotBlank()) {
                            val cm = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                            cm?.setPrimaryClip(android.content.ClipData.newPlainText("Hollow VPN Server", payload))
                            onCopyFeedback("Copied server config for ${server.countryName}")
                        }
                    }
                )
                DropdownMenuItem(
                    text = { Text("Show QR Code") },
                    onClick = {
                        menuExpanded = false
                        onShareQr()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete Server", color = scheme.error) },
                    onClick = {
                        menuExpanded = false
                        onDelete()
                    }
                )
            }
        }
    }
}

/**
 * SCREEN 3: Pure Material 3 Expressive Connected / Home Security Dashboard Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectedHomeScreen(
    selectedServer: ServerEntity?,
    telemetry: ConnectionTelemetryState,
    tunModeEnabled: Boolean = true,
    bottomContentPadding: Dp = 0.dp,
    onToggleConnect: () -> Unit,
    onOpenServerList: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAddServer: () -> Unit,
    onRefreshRealIp: () -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme
    val activeCountryName = selectedServer?.countryName ?: "Add Server"
    val activeCountryCode = selectedServer?.countryCode.orEmpty()
    val activeProtocol = selectedServer?.let {
        formatConsumerProtocol(it.protocolCore, it.transport)
    } ?: "Not configured"
    val activePingLabel = selectedServer?.let {
        if (it.pingMs > 0) "${it.pingMs} ms" else "Timeout"
    } ?: "—"
    val cleanServerIp = selectedServer?.resolvedIp?.takeIf { it.isNotBlank() }
        ?: selectedServer?.host?.takeIf { it.isNotBlank() }
        ?: "—"

    val statusDotColor by animateColorAsState(
        targetValue = when {
            telemetry.isConnected -> scheme.tertiary
            telemetry.isConnecting -> scheme.primary
            else -> scheme.outline
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "m3_status_dot_color"
    )

    val statusPulse = rememberInfiniteTransition(label = "m3_status_pulse")
    val dotScale by statusPulse.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "m3_status_dot_scale"
    )

    HollowAtmosphereBackground(
        isConnected = telemetry.isConnected,
        isConnecting = telemetry.isConnecting,
        showWorldMap = true,
        activeCountryCode = activeCountryCode
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    navigationIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (telemetry.isConnected) scheme.tertiaryContainer else scheme.primaryContainer
                            ) {
                                Box(
                                    modifier = Modifier.size(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Shield,
                                        contentDescription = null,
                                        tint = if (telemetry.isConnected) scheme.onTertiaryContainer else scheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    },
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = scheme.onBackground
                            )
                            Text(
                                text = when {
                                    telemetry.isConnected -> if (tunModeEnabled) "Protected · Full Tunnel" else "Protected · Proxy Mode"
                                    telemetry.isConnecting -> "Establishing secure route"
                                    else -> if (tunModeEnabled) "Full Device Protection" else "Local Proxy Mode"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (telemetry.isConnected) scheme.tertiary else scheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        FilledTonalIconButton(
                            onClick = onOpenAddServer,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = scheme.surfaceContainerHigh
                            ),
                            modifier = Modifier.testTag("home_add_server_button")
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "Add Server")
                        }
                        FilledTonalIconButton(
                            onClick = onOpenSettings,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = scheme.surfaceContainerHigh
                            ),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("home_settings_button")
                        ) {
                            Icon(Icons.Rounded.Settings, contentDescription = "Settings")
                        }
                    }
                )
            }
        ) { innerPadding ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding(), bottom = bottomContentPadding)
                    .widthIn(max = 600.dp)
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp)
            ) {
                val isCompactHeight = maxHeight < 580.dp
                val reactorSize = if (isCompactHeight) 152.dp else 188.dp
                val ctaHeight = if (isCompactHeight) 48.dp else 54.dp
                val statusKey = when {
                    telemetry.isConnecting -> 1
                    telemetry.isConnected -> 2
                    else -> 0
                }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Scrollable Dashboard Body
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(if (isCompactHeight) 2.dp else 6.dp))

                        // M3 Expressive Morphing Scalloped Hero Power Control over animated World Dot Map
                        TunnelPowerReactor(
                            isConnected = telemetry.isConnected,
                            isConnecting = telemetry.isConnecting,
                            onToggleClick = onToggleConnect,
                            reactorSize = reactorSize
                        )

                        Spacer(modifier = Modifier.height(if (isCompactHeight) 2.dp else 6.dp))

                        // Animated Status Indicator + Headline + Monospace Timer
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .scale(if (telemetry.isConnected || telemetry.isConnecting) dotScale else 1f)
                                    .clip(CircleShape)
                                    .background(statusDotColor)
                            )
                            AnimatedContent(
                                targetState = statusKey,
                                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
                                label = "m3_status_headline_anim"
                            ) { state ->
                                Text(
                                    text = when (state) {
                                        1 -> stringResource(R.string.status_connecting)
                                        2 -> stringResource(R.string.status_connected)
                                        else -> stringResource(R.string.status_disconnected)
                                    },
                                    style = if (isCompactHeight) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                                    color = scheme.onBackground,
                                    modifier = Modifier.testTag("connection_status_text")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = telemetry.formattedTimer,
                            style = TabularMonoLarge.copy(fontSize = if (isCompactHeight) 15.sp else 17.sp),
                            color = scheme.onSurfaceVariant,
                            modifier = Modifier.testTag("connection_timer_text")
                        )

                        Spacer(modifier = Modifier.height(if (isCompactHeight) 8.dp else 12.dp))

                        // M3 Expressive Active Server Pill
                        Surface(
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(50))
                                .clickable(role = Role.Button, onClick = onOpenServerList)
                                .testTag("active_server_pill"),
                            shape = RoundedCornerShape(50),
                            color = if (telemetry.isConnected) scheme.tertiaryContainer else scheme.surfaceContainerHigh,
                            tonalElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (activeCountryCode.isNotBlank()) {
                                    CircularCountryFlag(
                                        countryCode = activeCountryCode,
                                        size = 22.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Outlined.Public,
                                        contentDescription = null,
                                        tint = scheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Text(
                                    text = activeCountryName,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (telemetry.isConnected) scheme.onTertiaryContainer else scheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (selectedServer != null) {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = scheme.surfaceContainerLowest
                                    ) {
                                        Text(
                                            text = activePingLabel,
                                            style = TabularMonoBody.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                                            color = scheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = "Choose Server",
                                    tint = scheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(if (isCompactHeight) 10.dp else 14.dp))

                        // M3 Bento Row: Live Upload & Download Throughput
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TrafficStatTile(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Rounded.ArrowUpward,
                                label = stringResource(R.string.label_upload),
                                value = telemetry.formattedUpload,
                                speedLabel = if (telemetry.isConnected) telemetry.formattedUploadSpeed else null,
                                iconContainer = scheme.secondaryContainer,
                                iconTint = scheme.onSecondaryContainer,
                                compact = isCompactHeight
                            )
                            TrafficStatTile(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Rounded.ArrowDownward,
                                label = stringResource(R.string.label_download),
                                value = telemetry.formattedDownload,
                                speedLabel = if (telemetry.isConnected) telemetry.formattedDownloadSpeed else null,
                                iconContainer = scheme.tertiaryContainer,
                                iconTint = scheme.onTertiaryContainer,
                                compact = isCompactHeight
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // M3 Expressive Grouped Connection Summary Stack (24.dp outer, 6.dp inner joints)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = GroupedItemShapes.Top,
                                color = scheme.surfaceContainer
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = if (isCompactHeight) 10.dp else 13.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(R.string.label_protocol).uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = scheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = activeProtocol,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = scheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    VerticalDivider(
                                        modifier = Modifier
                                            .height(28.dp)
                                            .padding(horizontal = 12.dp),
                                        color = scheme.outlineVariant
                                    )
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = stringResource(R.string.label_latency).uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = scheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            if (selectedServer != null) {
                                                SignalStrengthBars(pingMs = selectedServer.pingMs)
                                            }
                                            Text(
                                                text = activePingLabel,
                                                style = TabularMonoBody.copy(fontWeight = FontWeight.SemiBold),
                                                color = scheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = GroupedItemShapes.Middle,
                                color = scheme.surfaceContainer
                            ) {
                                TelemetryDetailRow(
                                    icon = Icons.Outlined.Dns,
                                    label = stringResource(R.string.label_server_ip),
                                    value = cleanServerIp,
                                    compact = isCompactHeight
                                )
                            }

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = GroupedItemShapes.Bottom,
                                color = scheme.surfaceContainer
                            ) {
                                TelemetryDetailRow(
                                    icon = Icons.Outlined.Public,
                                    label = "Real IP",
                                    value = telemetry.realPublicIpLabel,
                                    flagCode = telemetry.realPublicCountryCode,
                                    trailingActionIcon = Icons.Rounded.Refresh,
                                    onClick = onRefreshRealIp,
                                    compact = isCompactHeight,
                                    modifier = Modifier.testTag("row_real_ip")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Pinned Native Material 3 Expressive Morphing Primary CTA — guaranteed visible on all screen heights
                    val ctaCorner by animateDpAsState(
                        targetValue = if (telemetry.isConnected) 18.dp else 28.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "m3_cta_shape_morph"
                    )
                    val ctaContainer by animateColorAsState(
                        targetValue = when {
                            telemetry.isConnected -> scheme.errorContainer
                            telemetry.isConnecting -> scheme.secondaryContainer
                            else -> scheme.primary
                        },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "m3_cta_container"
                    )
                    val ctaContent by animateColorAsState(
                        targetValue = when {
                            telemetry.isConnected -> scheme.onErrorContainer
                            telemetry.isConnecting -> scheme.onSecondaryContainer
                            else -> scheme.onPrimary
                        },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "m3_cta_content"
                    )

                    Button(
                        onClick = onToggleConnect,
                        shape = RoundedCornerShape(ctaCorner),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ctaContainer,
                            contentColor = ctaContent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 12.dp)
                            .height(ctaHeight)
                            .testTag("bottom_disconnect_connect_button")
                    ) {
                        AnimatedContent(
                            targetState = statusKey,
                            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(150)) },
                            label = "m3_cta_content_anim"
                        ) { state ->
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                when (state) {
                                    2 -> {
                                        Icon(
                                            imageVector = Icons.Rounded.Stop,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(R.string.action_disconnect),
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                    1 -> {
                                        Text(
                                            text = stringResource(R.string.status_connecting),
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                    else -> {
                                        Icon(
                                            imageVector = Icons.Rounded.PowerSettingsNew,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(R.string.action_connect),
                                            style = MaterialTheme.typography.titleMedium
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
}

@Composable
private fun TrafficStatTile(
    icon: ImageVector,
    label: String,
    value: String,
    speedLabel: String? = null,
    iconContainer: Color,
    iconTint: Color,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = scheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = if (compact) 10.dp else 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(if (compact) 32.dp else 36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = value,
                    style = TabularMonoBody.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!speedLabel.isNullOrBlank()) {
                    Text(
                        text = speedLabel,
                        style = TabularMonoBody.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                        color = scheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun TelemetryDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    flagCode: String? = null,
    trailingActionIcon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val vPad = if (compact) 10.dp else 13.dp
    val clickableModifier = if (onClick != null) {
        modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = vPad)
    } else {
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = vPad)
    }

    Row(
        modifier = clickableModifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Row(
            modifier = Modifier.weight(1f, fill = false),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            if (!flagCode.isNullOrBlank()) {
                CircularCountryFlag(
                    countryCode = flagCode,
                    size = 18.dp
                )
                Spacer(modifier = Modifier.width(7.dp))
            }
            Text(
                text = value,
                style = TabularMonoBody.copy(fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold),
                color = scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (trailingActionIcon != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = trailingActionIcon,
                    contentDescription = "Refresh Real IP",
                    tint = scheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
