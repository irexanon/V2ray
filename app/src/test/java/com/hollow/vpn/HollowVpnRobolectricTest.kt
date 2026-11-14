package com.hollow.vpn

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.hollow.vpn.data.AppSettingsState
import com.hollow.vpn.data.VpnPreferencesStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HollowVpnRobolectricTest {

    @Test
    fun `app name matches Hollow VPN`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Hollow VPN", appName)
    }

    @Test
    fun `preferences store persists Phase 2 connection and per-app proxy settings`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = VpnPreferencesStore(context)
        val custom = AppSettingsState(
            protocolPreference = "Trojan (TLS)",
            routingMode = "Bypass LAN",
            dnsMode = "Quad9 9.9.9.9",
            tunMode = false,
            ipv6Enabled = true,
            perAppProxyEnabled = true,
            bypassSelectedApps = true,
            selectedAppPackages = setOf("com.android.chrome", "org.mozilla.firefox")
        )
        store.saveSettings(custom)
        val loaded = store.loadSettings()
        assertEquals("Trojan (TLS)", loaded.protocolPreference)
        assertEquals("Bypass LAN", loaded.routingMode)
        assertEquals("Quad9 9.9.9.9", loaded.dnsMode)
        assertEquals(false, loaded.tunMode)
        assertEquals(true, loaded.ipv6Enabled)
        assertEquals(true, loaded.perAppProxyEnabled)
        assertEquals(true, loaded.bypassSelectedApps)
        assertTrue(loaded.selectedAppPackages.contains("com.android.chrome"))
    }
}
