package com.github.kr328.clash.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json

class CoreModelSerializationTest {
  private val json = Json { ignoreUnknownKeys = true }

  @Test
  fun proxyGroupSerializesProductStateWithoutAndroidTypes() {
    val group =
      ProxyGroup(
        type = Proxy.Type.Selector,
        proxies =
          listOf(
            Proxy(
              name = "proxy-a",
              title = "Proxy A",
              subtitle = "100 ms",
              type = Proxy.Type.Shadowsocks,
              delay = 100,
            )
          ),
        now = "proxy-a",
      )

    val encoded = json.encodeToString(group)
    val decoded = json.decodeFromString<ProxyGroup>(encoded)

    assertEquals(group, decoded)
  }

  @Test
  fun configurationOverrideSerializesMihomoYamlShape() {
    val override =
      ConfigurationOverride(
        httpPort = 7890,
        mode = TunnelState.Mode.Rule,
        dns = ConfigurationOverride.Dns(enable = true),
      )

    val encoded = json.encodeToString(override)

    assertEquals(
      ConfigurationOverride(
        httpPort = 7890,
        mode = TunnelState.Mode.Rule,
        dns = ConfigurationOverride.Dns(enable = true),
      ),
      json.decodeFromString<ConfigurationOverride>(encoded),
    )
  }

  @Test
  fun trafficUnpacksNativePackedValueInCommonCode() {
    val traffic = Traffic((1L shl 32) or 2L)

    assertEquals(1L, traffic.uploadScaled)
    assertEquals(2L, traffic.downloadScaled)
  }
}
