package com.hollow.vpn.ui.screens

import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hollow.vpn.R
import com.hollow.vpn.data.AppSettingsState
import com.hollow.vpn.ui.AddServerTab
import com.hollow.vpn.ui.components.CameraQrScannerDialog
import com.hollow.vpn.ui.components.HollowAtmosphereBackground
import com.hollow.vpn.ui.components.decodeQrFromUri
import com.hollow.vpn.ui.theme.GroupedItemShapes
import com.hollow.vpn.ui.theme.JetBrainsMonoFamily

private val MANUAL_PROTOCOL_PRESETS = listOf("VLESS", "VMess", "Trojan", "Shadowsocks", "Hysteria2")
private val MANUAL_TRANSPORT_PRESETS = listOf("Reality", "WS", "gRPC", "TCP", "Hysteria2")
private val SUPPORTED_FORMAT_TAGS = listOf(
    "VLESS",
    "VMess",
    "Trojan",
    "Shadowsocks",
    "Hysteria2",
    "Reality",
    "gRPC / WS",
    "Subscription",
    "JSON Config",
    "QR Code"
)

/**
 * SCREEN 5: Pure Material 3 Expressive Add Server Screen (Subscription / Manual / QR Code)
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddServerScreen(
    activeTab: AddServerTab,
    settings: AppSettingsState,
    onSelectTab: (AddServerTab) -> Unit,
    onAddAndFetchSubscription: (String) -> Unit,
    onImportClipboardText: (String) -> Unit,
    onAddManualServer: (
        countryName: String,
        host: String,
        port: Int,
        protocol: String,
        transport: String,
        uuid: String,
        sni: String,
        rawJson: String,
        publicKey: String,
        shortId: String,
        wsPath: String,
        wsHost: String,
        grpcServiceName: String
    ) -> Unit,
    onUpdateSettings: ((AppSettingsState) -> AppSettingsState) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    var subscriptionUrl by remember(settings.activeSubscriptionUrl) {
        mutableStateOf(settings.activeSubscriptionUrl)
    }
    var showIntervalPicker by remember { mutableStateOf(false) }

    var manualCountry by remember { mutableStateOf("") }
    var manualHost by remember { mutableStateOf("") }
    var manualPort by remember { mutableStateOf("443") }
    var manualProtocol by remember { mutableStateOf("VLESS") }
    var manualTransport by remember { mutableStateOf("Reality") }
    var manualUuid by remember { mutableStateOf("") }
    var manualSni by remember { mutableStateOf("") }
    var manualPublicKey by remember { mutableStateOf("") }
    var manualShortId by remember { mutableStateOf("") }
    var manualWsPath by remember { mutableStateOf("") }
    var manualWsHost by remember { mutableStateOf("") }
    var manualGrpcServiceName by remember { mutableStateOf("") }
    var manualRawJson by remember { mutableStateOf("") }

    var qrUriPayload by remember { mutableStateOf("") }
    var showCameraQrScanner by remember { mutableStateOf(false) }
    var qrDecodeStatus by remember { mutableStateOf<String?>(null) }

    val qrGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val decoded = decodeQrFromUri(context, uri)
            if (!decoded.isNullOrBlank()) {
                qrUriPayload = decoded
                qrDecodeStatus = null
                onImportClipboardText(decoded)
            } else {
                qrDecodeStatus = "Could not detect a QR code in that image."
            }
        }
    }

    val readClipboardText: () -> String = {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString().orEmpty()
    }

    HollowAtmosphereBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                ScreenTopBarWithBack(
                    title = stringResource(R.string.add_server_title),
                    subtitle = "Import links, custom configs or QR codes",
                    onBack = onBack
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
                    Spacer(modifier = Modifier.height(6.dp))

                    // Native M3 SingleChoiceSegmentedButtonRow
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AddServerTab.entries.forEachIndexed { index, tab ->
                            val selected = tab == activeTab
                            SegmentedButton(
                                selected = selected,
                                onClick = { onSelectTab(tab) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = AddServerTab.entries.size
                                ),
                                modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                            ) {
                                Text(
                                    text = tab.title,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    when (activeTab) {
                        AddServerTab.SUBSCRIPTION -> {
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = scheme.surfaceContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.label_subscription_link),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = scheme.onSurface
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = subscriptionUrl,
                                        onValueChange = { subscriptionUrl = it },
                                        placeholder = { Text(stringResource(R.string.hint_subscription_url)) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Outlined.Link,
                                                contentDescription = null
                                            )
                                        },
                                        trailingIcon = {
                                            IconButton(
                                                onClick = {
                                                    val pasted = readClipboardText()
                                                    if (pasted.isNotBlank()) subscriptionUrl = pasted
                                                },
                                                modifier = Modifier.minimumInteractiveComponentSize()
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.ContentPaste,
                                                    contentDescription = "Paste URL",
                                                    tint = scheme.primary
                                                )
                                            }
                                        },
                                        textStyle = TextStyle(
                                            fontFamily = JetBrainsMonoFamily,
                                            fontSize = 13.sp,
                                            color = scheme.onSurface
                                        ),
                                        singleLine = true,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("subscription_url_input")
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Button(
                                        onClick = { onAddAndFetchSubscription(subscriptionUrl) },
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp)
                                            .testTag("btn_add_and_fetch")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Download,
                                            contentDescription = null,
                                            modifier = Modifier.size(19.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(R.string.action_add_and_fetch),
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Import from Clipboard Action Card
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .clip(RoundedCornerShape(24.dp))
                                    .clickable(role = Role.Button) {
                                        onImportClipboardText(readClipboardText())
                                    }
                                    .testTag("card_import_clipboard"),
                                shape = RoundedCornerShape(24.dp),
                                color = scheme.secondaryContainer
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 15.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(scheme.surfaceContainerLowest),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.ContentPaste,
                                            contentDescription = null,
                                            tint = scheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(R.string.action_import_clipboard),
                                            style = MaterialTheme.typography.titleSmall,
                                            color = scheme.onSecondaryContainer
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = stringResource(R.string.action_import_clipboard_sub),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = scheme.onSecondaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = scheme.onSecondaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            SettingsGroupCard(sectionTitle = stringResource(R.string.section_subscription_options)) {
                                SettingsToggleRow(
                                    icon = Icons.Outlined.Sync,
                                    title = stringResource(R.string.setting_auto_update),
                                    subtitle = null,
                                    checked = settings.subAutoUpdate,
                                    onCheckedChange = { enabled ->
                                        onUpdateSettings { it.copy(subAutoUpdate = enabled) }
                                    },
                                    shape = GroupedItemShapes.Top,
                                    testTag = "switch_sub_auto_update"
                                )
                                SettingsValueNavigationRow(
                                    icon = Icons.Outlined.Schedule,
                                    title = stringResource(R.string.setting_update_interval),
                                    subtitle = null,
                                    valueText = settings.subUpdateInterval,
                                    onClick = { showIntervalPicker = true },
                                    shape = GroupedItemShapes.Middle,
                                    testTag = "row_sub_update_interval"
                                )
                                SettingsToggleRow(
                                    icon = Icons.Outlined.SwapHoriz,
                                    title = stringResource(R.string.setting_replace_existing),
                                    subtitle = stringResource(R.string.setting_replace_existing_sub),
                                    checked = settings.subReplaceExisting,
                                    onCheckedChange = { enabled ->
                                        onUpdateSettings { it.copy(subReplaceExisting = enabled) }
                                    },
                                    shape = GroupedItemShapes.Bottom,
                                    testTag = "switch_sub_replace_existing"
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = scheme.surfaceContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.section_supported_formats),
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = scheme.onSurface
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        SUPPORTED_FORMAT_TAGS.forEach { tag ->
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = scheme.surfaceContainerHigh
                                            ) {
                                                Text(
                                                    text = tag,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = scheme.onSurfaceVariant,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        AddServerTab.MANUAL -> {
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = scheme.surfaceContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "Custom Server",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = scheme.onSurface
                                    )

                                    OutlinedTextField(
                                        value = manualCountry,
                                        onValueChange = { manualCountry = it },
                                        label = { Text("Server Country / Remark") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        OutlinedTextField(
                                            value = manualHost,
                                            onValueChange = { manualHost = it },
                                            label = { Text("Server Address / Host") },
                                            singleLine = true,
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier.weight(0.68f)
                                        )
                                        OutlinedTextField(
                                            value = manualPort,
                                            onValueChange = { manualPort = it },
                                            label = { Text("Port") },
                                            singleLine = true,
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier.weight(0.32f)
                                        )
                                    }

                                    // Protocol preset FilterChips
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        MANUAL_PROTOCOL_PRESETS.forEach { preset ->
                                            val selected = manualProtocol.equals(preset, ignoreCase = true)
                                            FilterChip(
                                                selected = selected,
                                                onClick = {
                                                    manualProtocol = preset
                                                    if (preset.equals("Hysteria2", ignoreCase = true)) {
                                                        manualTransport = "Hysteria2"
                                                    } else if (manualTransport.equals("Hysteria2", ignoreCase = true)) {
                                                        manualTransport = "Reality"
                                                    }
                                                },
                                                label = { Text(preset) },
                                                leadingIcon = if (selected) {
                                                    {
                                                        Icon(
                                                            imageVector = Icons.Rounded.Check,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                                                        )
                                                    }
                                                } else null,
                                                shape = RoundedCornerShape(50)
                                            )
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        OutlinedTextField(
                                            value = manualProtocol,
                                            onValueChange = { manualProtocol = it },
                                            label = { Text("Protocol") },
                                            singleLine = true,
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                            value = manualTransport,
                                            onValueChange = { manualTransport = it },
                                            label = { Text("Transport") },
                                            singleLine = true,
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    // Transport preset FilterChips
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        MANUAL_TRANSPORT_PRESETS.forEach { preset ->
                                            val selected = manualTransport.equals(preset, ignoreCase = true)
                                            FilterChip(
                                                selected = selected,
                                                onClick = { manualTransport = preset },
                                                label = { Text(preset) },
                                                leadingIcon = if (selected) {
                                                    {
                                                        Icon(
                                                            imageVector = Icons.Rounded.Check,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                                                        )
                                                    }
                                                } else null,
                                                shape = RoundedCornerShape(50)
                                            )
                                        }
                                    }

                                    OutlinedTextField(
                                        value = manualUuid,
                                        onValueChange = { manualUuid = it },
                                        label = {
                                            Text(
                                                if (manualProtocol.equals("Hysteria2", ignoreCase = true) ||
                                                    manualProtocol.equals("Trojan", ignoreCase = true)
                                                ) "Auth Password" else "UUID / Password"
                                            )
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = manualSni,
                                        onValueChange = { manualSni = it },
                                        label = { Text("SNI / Server Name") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    if (manualTransport.equals("Reality", ignoreCase = true) &&
                                        !manualProtocol.equals("Hysteria2", ignoreCase = true)
                                    ) {
                                        OutlinedTextField(
                                            value = manualPublicKey,
                                            onValueChange = { manualPublicKey = it },
                                            label = { Text("Reality Public Key (pbk)") },
                                            singleLine = true,
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("manual_public_key_input")
                                        )
                                        OutlinedTextField(
                                            value = manualShortId,
                                            onValueChange = { manualShortId = it },
                                            label = { Text("Reality Short ID (sid)") },
                                            singleLine = true,
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("manual_short_id_input")
                                        )
                                    }

                                    if (manualTransport.equals("WS", ignoreCase = true) ||
                                        manualTransport.equals("WebSocket", ignoreCase = true)
                                    ) {
                                        OutlinedTextField(
                                            value = manualWsPath,
                                            onValueChange = { manualWsPath = it },
                                            label = { Text("WebSocket Path (e.g. /ws)") },
                                            singleLine = true,
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("manual_ws_path_input")
                                        )
                                        OutlinedTextField(
                                            value = manualWsHost,
                                            onValueChange = { manualWsHost = it },
                                            label = { Text("WebSocket Host Header") },
                                            singleLine = true,
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("manual_ws_host_input")
                                        )
                                    }

                                    if (manualTransport.equals("gRPC", ignoreCase = true)) {
                                        OutlinedTextField(
                                            value = manualGrpcServiceName,
                                            onValueChange = { manualGrpcServiceName = it },
                                            label = { Text("gRPC Service Name") },
                                            singleLine = true,
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("manual_grpc_service_input")
                                        )
                                    }

                                    OutlinedTextField(
                                        value = manualRawJson,
                                        onValueChange = { manualRawJson = it },
                                        label = { Text("Optional Raw Config (JSON)") },
                                        textStyle = TextStyle(
                                            fontFamily = JetBrainsMonoFamily,
                                            fontSize = 12.sp,
                                            color = scheme.onSurface
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                        minLines = 4,
                                        maxLines = 8,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("raw_xray_json_input")
                                    )

                                    Button(
                                        onClick = {
                                            onAddManualServer(
                                                manualCountry,
                                                manualHost,
                                                manualPort.toIntOrNull() ?: 443,
                                                manualProtocol,
                                                manualTransport,
                                                manualUuid,
                                                manualSni,
                                                manualRawJson,
                                                manualPublicKey,
                                                manualShortId,
                                                manualWsPath,
                                                manualWsHost,
                                                manualGrpcServiceName
                                            )
                                        },
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp)
                                            .testTag("btn_save_manual_server")
                                    ) {
                                        Text(
                                            text = "Save Custom Server & Ping Test",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }
                        }

                        AddServerTab.QR_CODE -> {
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = scheme.surfaceContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(76.dp)
                                            .clip(RoundedCornerShape(22.dp))
                                            .background(scheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.QrCodeScanner,
                                            contentDescription = null,
                                            tint = scheme.onPrimaryContainer,
                                            modifier = Modifier.size(38.dp)
                                        )
                                    }

                                    Text(
                                        text = "Import Server QR Code",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = scheme.onSurface
                                    )

                                    Text(
                                        text = "Scan a QR code with your camera, pick a QR screenshot from gallery, or paste a decoded link below.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = scheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Button(
                                            onClick = { showCameraQrScanner = true },
                                            shape = RoundedCornerShape(50),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(50.dp)
                                                .testTag("btn_scan_camera_qr")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.QrCodeScanner,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Text("Scan Camera")
                                        }
                                        FilledTonalButton(
                                            onClick = {
                                                qrGalleryLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            },
                                            shape = RoundedCornerShape(50),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(50.dp)
                                                .testTag("btn_pick_qr_image")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.PhotoLibrary,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Text("From Gallery")
                                        }
                                    }

                                    if (!qrDecodeStatus.isNullOrBlank()) {
                                        Text(
                                            text = qrDecodeStatus.orEmpty(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = scheme.error
                                        )
                                    }

                                    OutlinedTextField(
                                        value = qrUriPayload,
                                        onValueChange = { qrUriPayload = it },
                                        label = { Text("Decoded Server Link") },
                                        textStyle = TextStyle(
                                            fontFamily = JetBrainsMonoFamily,
                                            fontSize = 12.sp,
                                            color = scheme.onSurface
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                        minLines = 3,
                                        maxLines = 5,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Button(
                                        onClick = { onImportClipboardText(qrUriPayload) },
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp)
                                            .testTag("btn_import_qr_uri")
                                    ) {
                                        Text(
                                            text = "Import QR Server & Ping Test",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                    Spacer(modifier = Modifier.navigationBarsPadding())
                }
            }
        }
    }

    if (showCameraQrScanner) {
        CameraQrScannerDialog(
            onQrDecoded = { decoded ->
                showCameraQrScanner = false
                qrUriPayload = decoded
                qrDecodeStatus = null
                onImportClipboardText(decoded)
            },
            onDismiss = { showCameraQrScanner = false }
        )
    }

    if (showIntervalPicker) {
        SettingOptionPickerDialog(
            title = "Select Update Interval",
            options = UPDATE_INTERVAL_OPTIONS,
            selectedValue = settings.subUpdateInterval,
            onSelect = { chosen ->
                onUpdateSettings {
                    it.copy(updateIntervalLabel = chosen, subUpdateInterval = chosen)
                }
                showIntervalPicker = false
            },
            onDismiss = { showIntervalPicker = false }
        )
    }
}
