package com.hollow.vpn.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hollow.vpn.ui.components.HollowShieldMark
import com.hollow.vpn.ui.theme.HollowCream
import com.hollow.vpn.ui.theme.HollowCreamDivider
import com.hollow.vpn.ui.theme.HollowCreamSurface
import com.hollow.vpn.ui.theme.HollowInk
import com.hollow.vpn.ui.theme.HollowInkMuted
import com.hollow.vpn.ui.theme.HollowInkSecondary
import com.hollow.vpn.ui.theme.HollowOrange
import com.hollow.vpn.ui.theme.JetBrainsMonoFamily
import com.hollow.vpn.ui.theme.SpaceGroteskFamily

/**
 * Getting started: 3 pages (welcome, ways to add a server, permissions).
 * [onFinish] is used by Skip; [onRequestPermissions] runs the notification + VPN consent
 * prompts and finishes onboarding afterwards.
 */
@Composable
fun HollowOnboardingScreen(
    onFinish: () -> Unit,
    onRequestPermissions: () -> Unit
) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    BackHandler(enabled = page > 0) { page -= 1 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HollowCream)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(3) { i ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (i <= page) HollowInk else HollowCreamDivider)
                    )
                }
            }
            Spacer(modifier = Modifier.size(20.dp))
            Text(
                text = "Skip",
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = HollowInkSecondary,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(role = Role.Button, onClick = onFinish)
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            )
        }

        AnimatedContent(
            targetState = page,
            transitionSpec = { fadeIn(tween(260)) togetherWith fadeOut(tween(160)) },
            modifier = Modifier.weight(1f).widthIn(max = 600.dp),
            label = "onboarding_page"
        ) { p ->
            when (p) {
                0 -> WelcomePage()
                1 -> ServerOptionsPage()
                else -> PermissionsPage()
            }
        }

        Box(
            modifier = Modifier
                .padding(top = 12.dp, bottom = 24.dp)
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(HollowInk)
                .clickable(role = Role.Button) {
                    if (page < 2) page += 1 else onRequestPermissions()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when (page) { 0 -> "Get started"; 1 -> "Continue"; else -> "Allow & continue" },
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                color = HollowCream
            )
        }
    }
}

@Composable
private fun PageHeading(title: String, body: String) {
    Text(
        text = title,
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 38.sp,
        lineHeight = 42.sp,
        letterSpacing = (-1).sp,
        color = HollowInk
    )
    Text(
        text = body,
        fontFamily = SpaceGroteskFamily,
        fontSize = 16.sp,
        lineHeight = 23.sp,
        color = HollowInkSecondary,
        modifier = Modifier.padding(top = 12.dp)
    )
}

@Composable
private fun Enter(delayMs: Int, content: @Composable () -> Unit) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    AnimatedVisibility(
        visible = shown,
        enter = fadeIn(tween(450, delayMillis = delayMs)) +
            slideInVertically(tween(450, delayMillis = delayMs)) { it / 4 }
    ) { content() }
}

@Composable
private fun WelcomePage() {
    Column(modifier = Modifier.fillMaxSize()) {
        val t = rememberInfiniteTransition(label = "welcome_ping")
        val ping by t.animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(2200, easing = LinearOutSlowInEasing), RepeatMode.Restart),
            label = "ping"
        )
        Box(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val c = Offset(size.width / 2f, size.height / 2f)
                val base = minOf(size.width, size.height) / 2f
                drawCircle(HollowCreamDivider, base * 0.92f, c, style = Stroke(1.dp.toPx()))
                drawCircle(HollowCreamDivider, base * 0.68f, c, style = Stroke(1.dp.toPx()))
                drawCircle(
                    HollowOrange.copy(alpha = (1f - ping) * 0.7f),
                    base * 0.42f + base * 0.4f * ping, c, style = Stroke(2.dp.toPx())
                )
                drawCircle(HollowInk, 6.dp.toPx(), Offset(c.x - base * 0.62f, c.y - base * 0.5f))
                drawCircle(HollowOrange, 6.dp.toPx(), Offset(c.x + base * 0.66f, c.y - base * 0.38f))
                drawCircle(HollowInk, 6.dp.toPx(), Offset(c.x + base * 0.5f, c.y + base * 0.6f))
            }
            HollowShieldMark(size = 112.dp, background = HollowOrange, foreground = HollowCream)
        }
        Column(modifier = Modifier.padding(bottom = 8.dp)) {
            PageHeading(
                "Private by default.",
                "Hollow sends your whole device through servers you control. No accounts, no tracking."
            )
        }
    }
}

@Composable
private fun ServerOptionsPage() {
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(24.dp))
        PageHeading(
            "Bring your own servers.",
            "Add a server any of these ways. You can change it later."
        )
        Spacer(modifier = Modifier.height(20.dp))
        Enter(100) { OptionRow(Icons.Outlined.Link, "Subscription link", "Paste a link, syncs hourly") }
        Spacer(modifier = Modifier.height(12.dp))
        Enter(220) { OptionRow(Icons.Outlined.ContentPaste, "Paste a config", "vless://, vmess://, ss://") }
        Spacer(modifier = Modifier.height(12.dp))
        Enter(340) { OptionRow(Icons.Outlined.QrCodeScanner, "Scan a QR code", "Point your camera at a code") }
    }
}

@Composable
private fun OptionRow(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HollowCreamSurface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(HollowInk),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = HollowCream, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
            Text(title, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = HollowInk)
            Text(subtitle, fontFamily = SpaceGroteskFamily, fontSize = 14.sp, color = HollowInkSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun PermissionsPage() {
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(24.dp))
        PageHeading(
            "One last step.",
            "Android will ask to set up a VPN. Allow it so Hollow can protect your device."
        )
        Spacer(modifier = Modifier.height(20.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(HollowCreamDivider))
        Enter(100) { PermissionRow("VPN connection", "Required to route traffic", "REQUIRED", true) }
        Enter(220) { PermissionRow("Notifications", "Show connection status", "OPTIONAL", false) }
        Enter(340) { PermissionRow("Unrestricted battery", "Keep the tunnel alive", "ADVISED", false) }
    }
}

@Composable
private fun PermissionRow(title: String, subtitle: String, tag: String, required: Boolean) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .then(
                        if (required) Modifier.background(HollowOrange)
                        else Modifier.border(2.dp, HollowCreamDivider, CircleShape)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (required) {
                    Icon(Icons.Rounded.Check, contentDescription = null, tint = HollowCream, modifier = Modifier.size(18.dp))
                }
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 14.dp)) {
                Text(title, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = HollowInk)
                Text(subtitle, fontFamily = SpaceGroteskFamily, fontSize = 14.sp, color = HollowInkSecondary)
            }
            Text(
                text = tag,
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 10.5.sp,
                letterSpacing = 1.1.sp,
                color = if (required) HollowOrange else HollowInkMuted
            )
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(HollowCreamDivider))
    }
}
