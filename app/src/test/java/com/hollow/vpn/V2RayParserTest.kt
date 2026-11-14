package com.hollow.vpn

import android.util.Base64
import com.hollow.vpn.data.AppSettingsState
import com.hollow.vpn.vpn.V2RayParserAndFletcher
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class V2RayParserTest {

    private val parser = V2RayParserAndFletcher()

    @Test
    fun `parses VLESS Reality URI with public key, sni, shortId, and flow`() {
        val uri = "vless://11111111-2222-3333-4444-555555555555@185.216.34.12:443?security=reality&encryption=none&pbk=TEST_PUBLIC_KEY&headerType=none&fp=chrome&type=tcp&flow=xtls-rprx-vision&sni=www.cloudflare.com&sid=ab12cd34#%F0%9F%87%A9%F0%9F%87%AA%20Germany%20Reality"
        val servers = parser.parseImportPayload(uri)
        assertEquals(1, servers.size)
        val server = servers.first()
        assertEquals("DE", server.countryCode)
        assertEquals("Germany", server.countryName)
        assertEquals("Reality", server.transport)
        assertEquals("185.216.34.12", server.host)
        assertEquals(443, server.port)

        val json = JSONObject(server.rawConfigJson)
        val proxy = json.getJSONArray("outbounds").getJSONObject(0)
        assertEquals("vless", proxy.getString("protocol"))
        val user = proxy.getJSONObject("settings").getJSONArray("vnext").getJSONObject(0).getJSONArray("users").getJSONObject(0)
        assertEquals("none", user.getString("encryption"))
        assertEquals("xtls-rprx-vision", user.getString("flow"))
        val stream = proxy.getJSONObject("streamSettings")
        assertEquals("reality", stream.getString("security"))
        assertEquals("TEST_PUBLIC_KEY", stream.getJSONObject("realitySettings").getString("publicKey"))
        assertEquals("ab12cd34", stream.getJSONObject("realitySettings").getString("shortId"))
    }

    @Test
    fun `parses Trojan TLS WebSocket URI into servers array with wsSettings`() {
        val uri = "trojan://secretpass@nl.example.org:8443?security=tls&type=ws&host=cdn.example.org&path=%2Fws-tunnel&sni=nl.example.org#Netherlands%20Trojan"
        val servers = parser.parseImportPayload(uri)
        assertEquals(1, servers.size)
        val server = servers.first()
        assertEquals("NL", server.countryCode)

        val json = JSONObject(server.rawConfigJson)
        val proxy = json.getJSONArray("outbounds").getJSONObject(0)
        assertEquals("trojan", proxy.getString("protocol"))
        val serversArr = proxy.getJSONObject("settings").getJSONArray("servers")
        assertEquals("nl.example.org", serversArr.getJSONObject(0).getString("address"))
        assertEquals("secretpass", serversArr.getJSONObject(0).getString("password"))
        val stream = proxy.getJSONObject("streamSettings")
        assertEquals("ws", stream.getString("network"))
        assertEquals("tls", stream.getString("security"))
        assertEquals("/ws-tunnel", stream.getJSONObject("wsSettings").getString("path"))
        assertEquals("cdn.example.org", stream.getJSONObject("wsSettings").getJSONObject("headers").getString("Host"))
    }

    @Test
    fun `parses Shadowsocks URI into servers array with method and password`() {
        val creds = Base64.encodeToString("aes-256-gcm:my-ss-pass".toByteArray(), Base64.NO_WRAP)
        val uri = "ss://$creds@sg.example.net:8388#Singapore%20SS"
        val servers = parser.parseImportPayload(uri)
        assertEquals(1, servers.size)
        val json = JSONObject(servers.first().rawConfigJson)
        val proxy = json.getJSONArray("outbounds").getJSONObject(0)
        assertEquals("shadowsocks", proxy.getString("protocol"))
        val ssServer = proxy.getJSONObject("settings").getJSONArray("servers").getJSONObject(0)
        assertEquals("aes-256-gcm", ssServer.getString("method"))
        assertEquals("my-ss-pass", ssServer.getString("password"))
        assertEquals(8388, ssServer.getInt("port"))
    }

    @Test
    fun `parses VMess Base64 URI with vnext users alterId and gRPC serviceName`() {
        val vmessPayload = JSONObject()
            .put("v", "2")
            .put("ps", "Japan gRPC")
            .put("add", "jp.example.com")
            .put("port", "443")
            .put("id", "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")
            .put("net", "grpc")
            .put("path", "my-grpc-svc")
            .put("tls", "tls")
            .put("sni", "jp.example.com")
            .toString()
        val uri = "vmess://" + Base64.encodeToString(vmessPayload.toByteArray(), Base64.NO_WRAP)
        val servers = parser.parseImportPayload(uri)
        assertEquals(1, servers.size)
        val json = JSONObject(servers.first().rawConfigJson)
        val proxy = json.getJSONArray("outbounds").getJSONObject(0)
        assertEquals("vmess", proxy.getString("protocol"))
        val user = proxy.getJSONObject("settings").getJSONArray("vnext").getJSONObject(0).getJSONArray("users").getJSONObject(0)
        assertEquals(0, user.getInt("alterId"))
        assertEquals("auto", user.getString("security"))
        assertEquals("my-grpc-svc", proxy.getJSONObject("streamSettings").getJSONObject("grpcSettings").getString("serviceName"))
    }

    @Test
    fun `applyRuntimeSettingsToXrayJson injects DNS, Bypass LAN CIDR routing, and proxy-only inbounds`() {
        val uri = "vless://11111111-2222-3333-4444-555555555555@de.example.com:443?security=tls&type=tcp#Germany"
        val baseJson = parser.parseImportPayload(uri).first().rawConfigJson

        val settings = AppSettingsState(
            dnsMode = "Quad9 9.9.9.9",
            routingMode = "Bypass LAN",
            tunMode = false,
            ipv6Enabled = true
        )
        val runtime = JSONObject(parser.applyRuntimeSettingsToXrayJson(baseJson, settings))

        val dnsServers = runtime.getJSONObject("dns").getJSONArray("servers")
        assertEquals("9.9.9.9", dnsServers.getString(0))
        assertEquals("UseIP", runtime.getJSONObject("dns").getString("queryStrategy"))

        val rules = runtime.getJSONObject("routing").getJSONArray("rules")
        assertTrue(rules.length() >= 1)
        assertEquals("direct", rules.getJSONObject(0).getString("outboundTag"))
        val ipArr = rules.getJSONObject(0).getJSONArray("ip")
        val ips = (0 until ipArr.length()).map { ipArr.getString(it) }
        assertTrue(ips.contains("192.168.0.0/16"))

        val inbounds = runtime.getJSONArray("inbounds")
        val protocols = (0 until inbounds.length()).map { inbounds.getJSONObject(it).getString("protocol") }
        assertFalse(protocols.contains("tun"))
        assertTrue(protocols.contains("socks"))
        assertTrue(protocols.contains("http"))
        assertNotNull(runtime.optJSONObject("stats"))
    }
}
