package com.github.kr328.clash.core

import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.core.bridge.Bridge
import com.github.kr328.clash.core.bridge.ClashException
import com.github.kr328.clash.core.bridge.FetchCallback
import com.github.kr328.clash.core.bridge.LogcatInterface
import com.github.kr328.clash.core.bridge.TunInterface
import com.github.kr328.clash.core.model.ConfigurationOverride
import com.github.kr328.clash.core.model.FetchStatus
import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.core.model.Provider
import com.github.kr328.clash.core.model.Proxy
import com.github.kr328.clash.core.model.ProxyGroup
import com.github.kr328.clash.core.model.ProxySort
import com.github.kr328.clash.core.model.Traffic
import com.github.kr328.clash.core.model.TunnelState
import com.github.kr328.clash.core.model.UiConfiguration
import com.github.kr328.clash.core.util.parseInetSocketAddress
import java.io.File
import java.net.InetSocketAddress
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonPrimitive

object Clash {
  enum class OverrideSlot {
    Persist,
    Session,
  }

  private val ConfigurationOverrideJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = false
  }

  fun reset() {
    Bridge.nativeReset()
  }

  fun forceGc() {
    Bridge.nativeForceGc()
  }

  fun suspendCore(suspended: Boolean) {
    Bridge.nativeSuspend(suspended)
  }

  fun queryTunnelState(): TunnelState {
    val json = Bridge.nativeQueryTunnelState()

    return Json.decodeFromString(TunnelState.serializer(), json)
  }

  fun queryTrafficNow(): Traffic {
    return Traffic(Bridge.nativeQueryTrafficNow())
  }

  fun queryTrafficTotal(): Traffic {
    return Traffic(Bridge.nativeQueryTrafficTotal())
  }

  fun notifyDnsChanged(dns: List<String>) {
    Bridge.nativeNotifyDnsChanged(dns.toSet().joinToString(separator = ","))
  }

  fun notifyTimeZoneChanged(name: String, offset: Int) {
    Bridge.nativeNotifyTimeZoneChanged(name, offset)
  }

  fun notifyInstalledAppsChanged(uids: List<Pair<Int, String>>) {
    val uidList = uids.joinToString(separator = ",") { "${it.first}:${it.second}" }

    Bridge.nativeNotifyInstalledAppChanged(uidList)
  }

  fun startTun(
    fd: Int,
    stack: String,
    gateway: String,
    portal: String,
    dns: String,
    markSocket: (Int) -> Boolean,
    querySocketUid: (protocol: Int, source: InetSocketAddress, target: InetSocketAddress) -> Int,
  ) {
    Bridge.nativeStartTun(
      fd,
      stack,
      gateway,
      portal,
      dns,
      object : TunInterface {
        override fun markSocket(fd: Int) {
          markSocket(fd)
        }

        override fun querySocketUid(protocol: Int, source: String, target: String): Int {
          return querySocketUid(
            protocol,
            parseInetSocketAddress(source),
            parseInetSocketAddress(target),
          )
        }
      },
    )
  }

  fun stopTun() {
    Bridge.nativeStopTun()
  }

  fun startHttp(listenAt: String): String? {
    return Bridge.nativeStartHttp(listenAt)
  }

  fun stopHttp() {
    Bridge.nativeStopHttp()
  }

  fun queryGroupNames(excludeNotSelectable: Boolean): List<String> {
    val names =
      Json.decodeFromString(
        JsonArray.serializer(),
        Bridge.nativeQueryGroupNames(excludeNotSelectable),
      )

    return names.map {
      require(it.jsonPrimitive.isString)

      it.jsonPrimitive.content
    }
  }

  fun queryGroup(name: String, sort: ProxySort): ProxyGroup {
    return Bridge.nativeQueryGroup(name, sort.name)?.let {
      Json.decodeFromString(ProxyGroup.serializer(), it)
    } ?: ProxyGroup(Proxy.Type.Unknown, emptyList(), "")
  }

  fun healthCheck(name: String): CompletableDeferred<Unit> {
    return CompletableDeferred<Unit>().apply { Bridge.nativeHealthCheck(this, name) }
  }

  fun healthCheckAll() {
    Bridge.nativeHealthCheckAll()
  }

  fun patchSelector(selector: String, name: String): Boolean {
    return Bridge.nativePatchSelector(selector, name)
  }

  fun fetchAndValid(
    path: File,
    url: String,
    force: Boolean,
    reportStatus: (FetchStatus) -> Unit,
  ): CompletableDeferred<Unit> {
    return CompletableDeferred<Unit>().apply {
      Bridge.nativeFetchAndValid(
        object : FetchCallback {
          override fun report(statusJson: String) {
            reportStatus(Json.decodeFromString(FetchStatus.serializer(), statusJson))
          }

          override fun complete(error: String?) {
            if (error != null) completeExceptionally(ClashException(error)) else complete(Unit)
          }
        },
        path.absolutePath,
        url,
        force,
      )
    }
  }

  fun load(path: File): CompletableDeferred<Unit> {
    return CompletableDeferred<Unit>().apply { Bridge.nativeLoad(this, path.absolutePath) }
  }

  fun queryProviders(): List<Provider> {
    val providers = Json.decodeFromString(JsonArray.serializer(), Bridge.nativeQueryProviders())

    return List(providers.size) { Json.decodeFromJsonElement(Provider.serializer(), providers[it]) }
  }

  fun updateProvider(type: Provider.Type, name: String): CompletableDeferred<Unit> {
    return CompletableDeferred<Unit>().apply {
      Bridge.nativeUpdateProvider(this, type.toString(), name)
    }
  }

  fun queryOverride(slot: OverrideSlot): ConfigurationOverride {
    return try {
      ConfigurationOverrideJson.decodeFromString(
        ConfigurationOverride.serializer(),
        Bridge.nativeReadOverride(slot.ordinal),
      )
    } catch (e: Exception) {
      Log.e("Read override failed: ${e.message}", e)
      ConfigurationOverride()
    }
  }

  fun patchOverride(slot: OverrideSlot, configuration: ConfigurationOverride) {
    Bridge.nativeWriteOverride(
      slot.ordinal,
      ConfigurationOverrideJson.encodeToString(ConfigurationOverride.serializer(), configuration),
    )
  }

  fun clearOverride(slot: OverrideSlot) {
    Bridge.nativeClearOverride(slot.ordinal)
  }

  fun queryConfiguration(): UiConfiguration {
    return Json.decodeFromString(UiConfiguration.serializer(), Bridge.nativeQueryConfiguration())
  }

  fun subscribeLogcat(): ReceiveChannel<LogMessage> {
    return Channel<LogMessage>(32).apply {
      Bridge.nativeSubscribeLogcat(
        object : LogcatInterface {
          override fun received(jsonPayload: String) {
            trySend(Json.decodeFromString(LogMessage.serializer(), jsonPayload))
          }
        }
      )
    }
  }
}
