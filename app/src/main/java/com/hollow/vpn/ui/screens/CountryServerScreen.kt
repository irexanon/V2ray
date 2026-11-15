package com.hollow.vpn.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PublicOff
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hollow.vpn.data.ServerEntity
import com.hollow.vpn.data.ServerFilter
import com.hollow.vpn.ui.components.CircularCountryFlag
import com.hollow.vpn.ui.components.HollowAtmosphereBackground
import com.hollow.vpn.ui.components.SignalStrengthBars
import com.hollow.vpn.ui.theme.GroupedItemShapes
import com.hollow.vpn.ui.theme.SignalMediumDark
import com.hollow.vpn.ui.theme.SignalMediumLight
import com.hollow.vpn.ui.theme.SignalStrongDark
import com.hollow.vpn.ui.theme.SignalStrongLight
import com.hollow.vpn.ui.theme.SignalWeakDark
import com.hollow.vpn.ui.theme.SignalWeakLight
import com.hollow.vpn.ui.theme.TabularMonoBody

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryServerListScreen(
    servers: List<ServerEntity>,
    selectedFilter: ServerFilter = ServerFilter.ALL,
    activeProtocolPreference: String = "Auto (V2Ray)",
    bottomContentPadding: Dp = 0.dp,
    onSelectFilter: (ServerFilter) -> Unit = {},
    onResetProtocolFilter: () -> Unit = {},
    onSelectServer: (ServerEntity) -> Unit,
    onRefresh: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAddServer: () -> Unit,
    onPingServer: (ServerEntity) -> Unit,
    onRepingAll: () -> Unit = {},
    onShowMessage: (String) -> Unit = {},
    onShareQr: (ServerEntity) -> Unit = {},
    onInspectJson: (ServerEntity) -> Unit,
    onDeleteServer: (ServerEntity) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val scheme = MaterialTheme.colorScheme
    val isDark = scheme.background.luminance() < 0.5f

    val groups = servers
        .groupBy { it.countryName.ifBlank { "Unknown" } }
        .toSortedMap(String.CASE_INSENSITIVE_ORDER)
    val fastest = servers
        .filter { it.pingMs > 0 }
        .sortedBy { it.pingMs }
        .take(5)
    val expanded = remember { mutableStateMapOf<String, Boolean>() }

    HollowAtmosphereBackground {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = scheme.surfaceContainer
                    ),
                    title = {
                        Column {
                            Text(
                                text = "Servers",
                                style = MaterialTheme.typography.headlineMedium,
                                color = scheme.onBackground
                            )
                            Text(
                                text = "${servers.size} nodes · ${groups.size} countries",
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        FilledTonalIconButton(
                            onClick = onOpenAddServer,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = scheme.surfaceContainerHigh
                            ),
                            modifier = Modifier.testTag("country_add")
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "Add server")
                        }
                        Spacer(Modifier.width(4.dp))
                        FilledTonalIconButton(
                            onClick = onRepingAll,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = scheme.surfaceContainerHigh
                            ),
                            modifier = Modifier.testTag("country_reping_all")
                        ) {
                            Icon(Icons.Rounded.Speed, contentDescription = "Test all saved servers")
                        }
                        Spacer(Modifier.width(4.dp))
                        FilledTonalIconButton(
                            onClick = onRefresh,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = scheme.surfaceContainerHigh
                            ),
                            modifier = Modifier.testTag("country_refresh")
                        ) {
                            Icon(Icons.Rounded.Refresh, contentDescription = "Refresh servers")
                        }
                        Spacer(Modifier.width(4.dp))
                        FilledTonalIconButton(
                            onClick = onOpenSettings,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = scheme.surfaceContainerHigh
                            ),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("country_settings")
                        ) {
                            Icon(Icons.Rounded.Settings, contentDescription = "Settings")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding(), bottom = bottomContentPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 620.dp)
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 16.dp)
                ) {
                    // Material 3 FilterChips + Optional Protocol InputChip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
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
                                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                                        )
                                    }
                                } else null,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.testTag("country_filter_${filter.name.lowercase()}")
                            )
                        }

                        if (activeProtocolPreference != "Auto (V2Ray)") {
                            InputChip(
                                selected = true,
                                onClick = onResetProtocolFilter,
                                label = { Text(formatConsumerProtocol(activeProtocolPreference)) },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Reset protocol filter to Auto",
                                        modifier = Modifier.size(InputChipDefaults.IconSize)
                                    )
                                },
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.testTag("chip_reset_protocol_filter")
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (fastest.isNotEmpty()) {
                            item(key = "header_fastest") {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Bolt,
                                        contentDescription = null,
                                        tint = scheme.tertiary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Fastest Nodes",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = scheme.onBackground
                                    )
                                }
                            }

                            item(key = "fastest_carousel") {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    contentPadding = PaddingValues(end = 4.dp)
                                ) {
                                    items(fastest, key = { "fastest_${it.id}" }) { server ->
                                        FastestLocationCard(
                                            server = server,
                                            isDark = isDark,
                                            onClick = { onSelectServer(server) }
                                        )
                                    }
                                }
                            }

                            item(key = "header_countries") {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "All Countries",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = scheme.onBackground
                                )
                            }
                        }

                        if (groups.isEmpty()) {
                            item(key = "empty_state") {
                                ElevatedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = scheme.surfaceContainer
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(28.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(RoundedCornerShape(18.dp))
                                                .background(scheme.surfaceContainerHigh),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.PublicOff,
                                                contentDescription = null,
                                                tint = scheme.onSurfaceVariant,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                        Text(
                                            text = "No matching servers",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = scheme.onSurface
                                        )
                                        val emptyMsg = if (activeProtocolPreference != "Auto (V2Ray)") {
                                            "No servers match \"${formatConsumerProtocol(activeProtocolPreference)}\". Reset to Auto or add a server."
                                        } else {
                                            "No servers added yet. Import your subscription URL, server link, or QR code."
                                        }
                                        Text(
                                            text = emptyMsg,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = scheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            if (activeProtocolPreference != "Auto (V2Ray)") {
                                                TextButton(
                                                    onClick = onResetProtocolFilter,
                                                    modifier = Modifier.minimumInteractiveComponentSize()
                                                ) {
                                                    Text("Reset Protocol")
                                                }
                                            }
                                            FilledTonalButton(
                                                onClick = onOpenAddServer,
                                                modifier = Modifier
                                                    .minimumInteractiveComponentSize()
                                                    .testTag("empty_add_server_button")
                                            ) {
                                                Text("Add Server")
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        groups.forEach { (country, countryServers) ->
                            val orderedServers = countryServers.sortedBy {
                                if (it.pingMs > 0) it.pingMs else Int.MAX_VALUE
                            }
                            val bestPing = orderedServers.firstOrNull { it.pingMs > 0 }?.pingMs ?: 0
                            val hasSelected = orderedServers.any { it.isSelected }
                            val isExpanded = expanded[country] ?: (groups.size == 1 || hasSelected)

                            item(key = "country_$country") {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = if (hasSelected) scheme.secondaryContainer.copy(alpha = 0.55f) else scheme.surfaceContainer,
                                    tonalElevation = if (hasSelected) 2.dp else 0.dp,
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .minimumInteractiveComponentSize()
                                                .clickable(role = Role.Button) {
                                                    expanded[country] = !isExpanded
                                                }
                                                .padding(horizontal = 16.dp, vertical = 14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CircularCountryFlag(orderedServers.first().countryCode, 34.dp)
                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(start = 12.dp)
                                            ) {
                                                Text(
                                                    text = country,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = scheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${orderedServers.size} node${if (orderedServers.size == 1) "" else "s"}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = scheme.onSurfaceVariant
                                                )
                                            }
                                            if (bestPing > 0) {
                                                val pingTint = when {
                                                    bestPing < 90 -> if (isDark) SignalStrongDark else SignalStrongLight
                                                    bestPing < 180 -> if (isDark) SignalMediumDark else SignalMediumLight
                                                    else -> if (isDark) SignalWeakDark else SignalWeakLight
                                                }
                                                Surface(
                                                    shape = RoundedCornerShape(50),
                                                    color = pingTint.copy(alpha = 0.14f)
                                                ) {
                                                    Text(
                                                        text = "$bestPing ms",
                                                        style = TabularMonoBody.copy(fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold),
                                                        color = pingTint,
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                    )
                                                }
                                                Spacer(Modifier.width(8.dp))
                                            }
                                            Icon(
                                                imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                                contentDescription = if (isExpanded) "Collapse $country" else "Expand $country",
                                                tint = scheme.onSurfaceVariant,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }

                                        AnimatedVisibility(
                                            visible = isExpanded,
                                            enter = expandVertically() + fadeIn(),
                                            exit = shrinkVertically() + fadeOut()
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
                                                verticalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                HorizontalDivider(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    color = scheme.outlineVariant.copy(alpha = 0.6f)
                                                )
                                                orderedServers.forEachIndexed { idx, server ->
                                                    val itemShape = GroupedItemShapes.forIndex(idx, orderedServers.size)
                                                    Surface(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clip(itemShape)
                                                            .clickable { onSelectServer(server) }
                                                            .testTag("country_server_${server.id}"),
                                                        shape = itemShape,
                                                        color = if (server.isSelected) scheme.primaryContainer else scheme.surfaceContainerLow
                                                    ) {
                                                        ServerItemRowContent(
                                                            server = server,
                                                            showLightningBadge = server.isSelected,
                                                            onSelect = { onSelectServer(server) },
                                                            onPing = { onPingServer(server) },
                                                            onInspectJson = { onInspectJson(server) },
                                                            onDelete = { onDeleteServer(server) },
                                                            onCopyFeedback = onShowMessage,
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
        }
    }
}

@Composable
private fun FastestLocationCard(
    server: ServerEntity,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val address = server.resolvedIp.ifBlank { server.host }
    val pingTint = when {
        server.pingMs in 1..89 -> if (isDark) SignalStrongDark else SignalStrongLight
        server.pingMs in 90..179 -> if (isDark) SignalMediumDark else SignalMediumLight
        else -> if (isDark) SignalWeakDark else SignalWeakLight
    }

    val containerColor = if (server.isSelected) scheme.primaryContainer else scheme.surfaceContainerHigh
    val titleColor = if (server.isSelected) scheme.onPrimaryContainer else scheme.onSurface
    val subColor = if (server.isSelected) scheme.onPrimaryContainer.copy(alpha = 0.78f) else scheme.onSurfaceVariant

    ElevatedCard(
        modifier = Modifier
            .width(214.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularCountryFlag(server.countryCode, 30.dp)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    SignalStrengthBars(pingMs = server.pingMs)
                    Text(
                        text = if (server.pingMs > 0) "${server.pingMs} ms" else "Timeout",
                        style = TabularMonoBody.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                        color = pingTint
                    )
                }
            }

            Column {
                Text(
                    text = server.countryName,
                    style = MaterialTheme.typography.titleSmall,
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "$address:${server.port}",
                    style = TabularMonoBody.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Normal),
                    color = subColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = scheme.surfaceContainerLowest
                ) {
                    Text(
                        text = server.transport.ifBlank { formatConsumerProtocol(server.protocolCore) },
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (server.isSelected) scheme.tertiaryContainer else scheme.secondaryContainer
                ) {
                    Text(
                        text = if (server.isSelected) "ACTIVE" else "FAST",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = if (server.isSelected) scheme.onTertiaryContainer else scheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}
