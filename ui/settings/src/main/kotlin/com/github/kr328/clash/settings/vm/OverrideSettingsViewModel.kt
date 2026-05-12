package com.github.kr328.clash.settings.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.core.model.ConfigurationOverride
import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.core.model.TunnelState
import com.github.kr328.clash.glue.util.withClash
import com.github.kr328.clash.settings.ui.OverrideSettingsActions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class OverrideSettingsViewModel(app: Application) :
  AndroidViewModel(app), OverrideSettingsActions, DefaultLifecycleObserver {
  private var initialized = false
  @Volatile private var skipPersist = false

  val configuration: StateFlow<ConfigurationOverride>
    field = MutableStateFlow(ConfigurationOverride())

  fun initialize() {
    if (initialized) return
    initialized = true

    viewModelScope.launch {
      configuration.value = withClash { queryOverride(Clash.OverrideSlot.Persist) }
    }
  }

  override fun onStop(owner: LifecycleOwner) {
    if (!initialized) return

    // Intended to use non-viewModel scope as we need the action to be called on disposed.
    Global.launch {
      withClash {
        if (skipPersist) clearOverride(Clash.OverrideSlot.Persist)
        else patchOverride(Clash.OverrideSlot.Persist, configuration.value)
      }
    }
  }

  fun resetOverride() {
    skipPersist = true
  }

  override fun updateHttpPort(value: Int?) = configuration.update { it.copy(httpPort = value) }

  override fun updateSocksPort(value: Int?) = configuration.update { it.copy(socksPort = value) }

  override fun updateRedirectPort(value: Int?) = configuration.update {
    it.copy(redirectPort = value)
  }

  override fun updateTproxyPort(value: Int?) = configuration.update { it.copy(tproxyPort = value) }

  override fun updateMixedPort(value: Int?) = configuration.update { it.copy(mixedPort = value) }

  override fun updateAuthentication(value: List<String>?) = configuration.update {
    it.copy(authentication = value)
  }

  override fun updateAllowLan(value: Boolean?) = configuration.update { it.copy(allowLan = value) }

  override fun updateIpv6(value: Boolean?) = configuration.update { it.copy(ipv6 = value) }

  override fun updateBindAddress(value: String?) = configuration.update {
    it.copy(bindAddress = value)
  }

  override fun updateExternalController(value: String?) = configuration.update {
    it.copy(externalController = value)
  }

  override fun updateExternalControllerTls(value: String?) = configuration.update {
    it.copy(externalControllerTLS = value)
  }

  override fun updateAllowOrigins(value: List<String>?) = configuration.update {
    it.copy(externalControllerCors = it.externalControllerCors.copy(allowOrigins = value))
  }

  override fun updateAllowPrivateNetwork(value: Boolean?) = configuration.update {
    it.copy(externalControllerCors = it.externalControllerCors.copy(allowPrivateNetwork = value))
  }

  override fun updateSecret(value: String?) = configuration.update { it.copy(secret = value) }

  override fun updateMode(value: TunnelState.Mode?) = configuration.update { it.copy(mode = value) }

  override fun updateLogLevel(value: LogMessage.Level?) = configuration.update {
    it.copy(logLevel = value)
  }

  override fun updateHosts(value: Map<String, String>?) = configuration.update {
    it.copy(hosts = value)
  }

  override fun updateDnsEnable(value: Boolean?) = configuration.update {
    it.copy(dns = it.dns.copy(enable = value))
  }

  override fun updateDnsPreferH3(value: Boolean?) = configuration.update {
    it.copy(dns = it.dns.copy(preferH3 = value))
  }

  override fun updateDnsListen(value: String?) = configuration.update {
    it.copy(dns = it.dns.copy(listen = value))
  }

  override fun updateAppendSystemDns(value: Boolean?) = configuration.update {
    it.copy(app = it.app.copy(appendSystemDns = value))
  }

  override fun updateDnsIpv6(value: Boolean?) = configuration.update {
    it.copy(dns = it.dns.copy(ipv6 = value))
  }

  override fun updateDnsUseHosts(value: Boolean?) = configuration.update {
    it.copy(dns = it.dns.copy(useHosts = value))
  }

  override fun updateDnsEnhancedMode(value: ConfigurationOverride.DnsEnhancedMode?) =
    configuration.update {
      it.copy(dns = it.dns.copy(enhancedMode = value))
    }

  override fun updateDnsNameServer(value: List<String>?) = configuration.update {
    it.copy(dns = it.dns.copy(nameServer = value))
  }

  override fun updateDnsFallback(value: List<String>?) = configuration.update {
    it.copy(dns = it.dns.copy(fallback = value))
  }

  override fun updateDnsDefaultServer(value: List<String>?) = configuration.update {
    it.copy(dns = it.dns.copy(defaultServer = value))
  }

  override fun updateDnsFakeIpFilter(value: List<String>?) = configuration.update {
    it.copy(dns = it.dns.copy(fakeIpFilter = value))
  }

  override fun updateDnsFakeIpFilterMode(value: ConfigurationOverride.FilterMode?) =
    configuration.update {
      it.copy(dns = it.dns.copy(fakeIPFilterMode = value))
    }

  override fun updateDnsGeoIpFallback(value: Boolean?) = configuration.update {
    it.copy(dns = it.dns.copy(fallbackFilter = it.dns.fallbackFilter.copy(geoIp = value)))
  }

  override fun updateDnsGeoIpCode(value: String?) = configuration.update {
    it.copy(dns = it.dns.copy(fallbackFilter = it.dns.fallbackFilter.copy(geoIpCode = value)))
  }

  override fun updateDnsDomainFallback(value: List<String>?) = configuration.update {
    it.copy(dns = it.dns.copy(fallbackFilter = it.dns.fallbackFilter.copy(domain = value)))
  }

  override fun updateDnsIpcidrFallback(value: List<String>?) = configuration.update {
    it.copy(dns = it.dns.copy(fallbackFilter = it.dns.fallbackFilter.copy(ipcidr = value)))
  }

  override fun updateDnsNameserverPolicy(value: Map<String, String>?) = configuration.update {
    it.copy(dns = it.dns.copy(nameserverPolicy = value))
  }
}
