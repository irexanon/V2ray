package com.hollow.vpn.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hollow.vpn.data.ServerEntity
import com.hollow.vpn.data.ServerFilter
import com.hollow.vpn.ui.components.CircularCountryFlag
import com.hollow.vpn.ui.components.SageOrganicBackground
import com.hollow.vpn.ui.theme.EmeraldAccent
import com.hollow.vpn.ui.theme.ForestEmerald
import com.hollow.vpn.ui.theme.PureWhiteCard
import com.hollow.vpn.ui.theme.SelectedMintCard
import com.hollow.vpn.ui.theme.SubtleCardBorder
import com.hollow.vpn.ui.theme.TextPrimaryDark
import com.hollow.vpn.ui.theme.TextSecondaryMuted

@Composable
fun CountryServerListScreen(
    servers: List<ServerEntity>,
    selectedFilter: ServerFilter = ServerFilter.ALL,
    activeProtocolPreference: String = "Auto (V2Ray)",
    onSelectFilter: (ServerFilter) -> Unit = {},
    onResetProtocolFilter: () -> Unit = {},
    onSelectServer: (ServerEntity) -> Unit,
    onRefresh: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAddServer: () -> Unit,
    onPingServer: (ServerEntity) -> Unit,
    onInspectJson: (ServerEntity) -> Unit,
    onDeleteServer: (ServerEntity) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val groups = servers
        .groupBy { it.countryName.ifBlank { "Unknown" } }
        .toSortedMap(String.CASE_INSENSITIVE_ORDER)
    val fastest = servers
        .filter { it.pingMs > 0 }
        .sortedBy { it.pingMs }
        .take(5)
    val expanded = remember { mutableStateMapOf<String, Boolean>() }

    SageOrganicBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp)
        ) {
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Servers", style = MaterialTheme.typography.headlineMedium, color = TextPrimaryDark)
                    Text("Fastest locations and all countries", style = MaterialTheme.typography.bodyMedium, color = TextSecondaryMuted)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularHeaderIconButton(Icons.Rounded.Add, "Add server", "country_add", onOpenAddServer)
                    CircularHeaderIconButton(Icons.Rounded.Refresh, "Refresh servers", "country_refresh", onRefresh)
                    CircularHeaderIconButton(Icons.Rounded.Settings, "Settings", "country_settings", onOpenSettings)
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ServerFilter.entries.forEach { filter ->
                    val isSelected = filter == selectedFilter
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onSelectFilter(filter) }
                            .testTag("country_filter_${filter.name.lowercase()}"),
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) ForestEmerald else PureWhiteCard.copy(alpha = 0.85f),
                        border = if (isSelected) null else BorderStroke(1.dp, SubtleCardBorder)
                    ) {
                        Text(
                            text = filter.label,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 12.5.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                            ),
                            color = if (isSelected) Color.White else TextSecondaryMuted,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
                if (activeProtocolPreference != "Auto (V2Ray)") {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onResetProtocolFilter() }
                            .testTag("chip_reset_protocol_filter"),
                        shape = RoundedCornerShape(50),
                        color = SelectedMintCard,
                        border = BorderStroke(1.dp, ForestEmerald.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = activeProtocolPreference,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimaryDark
                            )
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Reset protocol filter to Auto",
                                tint = TextPrimaryDark,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (fastest.isNotEmpty()) {
                    item {
                        Text("Top 5 fastest locations", style = MaterialTheme.typography.titleMedium, color = TextPrimaryDark)
                    }
                    items(fastest, key = { "fastest_${it.id}" }) { server ->
                        FastestLocationCard(server = server, onClick = { onSelectServer(server) })
                    }
                    item {
                        Spacer(Modifier.height(6.dp))
                        Text("All countries", style = MaterialTheme.typography.titleMedium, color = TextPrimaryDark)
                    }
                }
                if (groups.isEmpty()) {
                    item {
                        Surface(shape = RoundedCornerShape(18.dp), color = PureWhiteCard) {
                            val emptyMsg = if (activeProtocolPreference != "Auto (V2Ray)") {
                                "No servers match protocol \"$activeProtocolPreference\". Tap the filter chip above to reset to Auto, or refresh servers."
                            } else {
                                "No servers available. Tap refresh to fetch servers."
                            }
                            Text(emptyMsg, modifier = Modifier.padding(22.dp), color = TextSecondaryMuted)
                        }
                    }
                }
                groups.forEach { (country, countryServers) ->
                    val orderedServers = countryServers.sortedBy { it.pingMs }
                    val isExpanded = expanded[country] ?: (groups.size == 1)
                    item(key = "country_$country") {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .clickable { expanded[country] = !isExpanded },
                            color = PureWhiteCard,
                            border = BorderStroke(1.dp, SubtleCardBorder),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularCountryFlag(orderedServers.first().countryCode, 30.dp)
                                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                                    Text(country, style = MaterialTheme.typography.titleMedium, color = TextPrimaryDark)
                                    Text("${orderedServers.size} server${if (orderedServers.size == 1) "" else "s"}", style = MaterialTheme.typography.bodySmall, color = TextSecondaryMuted)
                                }
                                Icon(
                                    if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                    contentDescription = if (isExpanded) "Collapse $country" else "Expand $country",
                                    tint = ForestEmerald,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                    if (isExpanded) {
                        items(orderedServers, key = { "server_${it.id}" }) { server ->
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(start = 10.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = if (server.isSelected) SelectedMintCard else PureWhiteCard
                            ) {
                                ServerItemRowContent(
                                    server = server,
                                    showLightningBadge = server.isSelected,
                                    onSelect = { onSelectServer(server) },
                                    onPing = { onPingServer(server) },
                                    onInspectJson = { onInspectJson(server) },
                                    onDelete = { onDeleteServer(server) }
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
private fun FastestLocationCard(server: ServerEntity, onClick: () -> Unit) {
    val address = server.resolvedIp.ifBlank { server.host }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        color = PureWhiteCard,
        border = BorderStroke(1.dp, EmeraldAccent.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularCountryFlag(server.countryCode, 34.dp)
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(server.countryName, style = MaterialTheme.typography.titleSmall, color = TextPrimaryDark)
                Text(address, style = MaterialTheme.typography.bodyMedium, color = TextSecondaryMuted)
                Text("${server.protocolDisplay} · ${server.port}", style = MaterialTheme.typography.labelSmall, color = TextSecondaryMuted)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${server.pingMs} ms", style = MaterialTheme.typography.titleSmall, color = EmeraldAccent)
                Text("FASTEST", style = MaterialTheme.typography.labelSmall, color = ForestEmerald)
            }
        }
    }
}
