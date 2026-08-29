package com.github.kr328.clash.service.clash.module

import android.app.Service
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.core.content.getSystemService
import co.touchlab.kermit.Logger
import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.service.util.asSocketAddressText
import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext

class NetworkObserveModule(service: Service) : Module<Network>(service) {
  private val connectivity = service.getSystemService<ConnectivityManager>()!!
  private val networks: Channel<Network> = Channel(Channel.UNLIMITED)
  private val request =
    NetworkRequest.Builder()
      .apply {
        addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
        addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        addCapability(NetworkCapabilities.NET_CAPABILITY_FOREGROUND)
        addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
      }
      .build()

  private data class NetworkInfo(
    val losingMs: Long = 0,
    val dnsList: List<InetAddress> = emptyList(),
  ) {
    fun isAvailable(): Boolean = losingMs < System.currentTimeMillis()
  }

  private val networkInfos = ConcurrentHashMap<Network, NetworkInfo>()

  @Volatile private var curDnsList = emptyList<String>()

  private val callback =
    object : ConnectivityManager.NetworkCallback() {
      override fun onAvailable(network: Network) {
        Logger.i("NetworkObserve onAvailable network=$network")
        networkInfos[network] = NetworkInfo()
      }

      override fun onLosing(network: Network, maxMsToLive: Int) {
        Logger.i("NetworkObserve onLosing network=$network")
        networkInfos.computeIfPresent(network) { _, info ->
          info.copy(losingMs = System.currentTimeMillis() + maxMsToLive)
        }
        notifyDnsChange()

        networks.trySend(network)
      }

      override fun onLost(network: Network) {
        Logger.i("NetworkObserve onLost network=$network")
        networkInfos.remove(network)
        notifyDnsChange()

        networks.trySend(network)
      }

      override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
        Logger.i("NetworkObserve onLinkPropertiesChanged network=$network $linkProperties")
        networkInfos.computeIfPresent(network) { _, info ->
          info.copy(dnsList = linkProperties.dnsServers)
        }
        notifyDnsChange()

        networks.trySend(network)
      }

      override fun onUnavailable() {
        Logger.i("NetworkObserve onUnavailable")
      }
    }

  private fun register(): Boolean {
    Logger.i("NetworkObserve start register")
    return try {
      connectivity.registerNetworkCallback(request, callback)

      true
    } catch (e: Exception) {
      Logger.w("NetworkObserve register failed", e)

      false
    }
  }

  private fun unregister(): Boolean {
    Logger.i("NetworkObserve start unregister")
    try {
      connectivity.unregisterNetworkCallback(callback)
    } catch (e: Exception) {
      Logger.w("NetworkObserve unregister failed", e)
    }

    return false
  }

  private fun networkToInt(entry: Map.Entry<Network, NetworkInfo>): Int {
    val capabilities = connectivity.getNetworkCapabilities(entry.key)
    // calculate priority based on transport type, available state
    // lower value means higher priority
    // wifi > ethernet > usb tethering > bluetooth tethering > cellular > satellite > other
    return when {
      capabilities == null -> 100
      capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> 90
      capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> 0
      capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> 1
      Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_USB) -> 2
      capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> 3
      capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> 4
      Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM &&
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_SATELLITE) -> 5
      // TRANSPORT_LOWPAN / TRANSPORT_THREAD / TRANSPORT_WIFI_AWARE are not for general
      // internet access, which will not set as default route.
      else -> 20
    } + (if (entry.value.isAvailable()) 0 else 10)
  }

  private fun notifyDnsChange() {
    val dnsList =
      networkInfos
        .asSequence()
        .minByOrNull { networkToInt(it) }
        ?.value
        ?.dnsList
        .orEmpty()
        .map { x -> x.asSocketAddressText(53) }
    val prevDnsList = curDnsList
    if (dnsList.isNotEmpty() && prevDnsList != dnsList) {
      Logger.i("notifyDnsChange $prevDnsList -> $dnsList")
      curDnsList = dnsList
      Clash.notifyDnsChanged(dnsList)
    }
  }

  override suspend fun run() {
    register()

    try {
      while (true) {
        val quit = select {
          networks.onReceive {
            enqueueEvent(it)

            false
          }
        }
        if (quit) {
          return
        }
      }
    } finally {
      withContext(NonCancellable) {
        unregister()

        Logger.i("NetworkObserve dns = []")
        Clash.notifyDnsChanged(emptyList())
      }
    }
  }
}
