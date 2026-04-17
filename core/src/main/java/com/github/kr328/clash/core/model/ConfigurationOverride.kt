package com.github.kr328.clash.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class ConfigurationOverride(
  @SerialName("port") var httpPort: Int? = null,
  @SerialName("socks-port") var socksPort: Int? = null,
  @SerialName("redir-port") var redirectPort: Int? = null,
  @SerialName("tproxy-port") var tproxyPort: Int? = null,
  @SerialName("mixed-port") var mixedPort: Int? = null,
  @SerialName("authentication") var authentication: List<String>? = null,
  @SerialName("allow-lan") var allowLan: Boolean? = null,
  @SerialName("bind-address") var bindAddress: String? = null,
  @SerialName("mode") var mode: TunnelState.Mode? = null,
  @SerialName("log-level") var logLevel: LogMessage.Level? = null,
  @SerialName("ipv6") var ipv6: Boolean? = null,
  @SerialName("external-controller") var externalController: String? = null,
  @SerialName("external-controller-tls") var externalControllerTLS: String? = null,
  @SerialName("external-controller-cors")
  var externalControllerCors: ExternalControllerCors = ExternalControllerCors(),
  @SerialName("secret") var secret: String? = null,
  @SerialName("hosts") var hosts: Map<String, String>? = null,
  @SerialName("unified-delay") var unifiedDelay: Boolean? = null,
  @SerialName("geodata-mode") var geodataMode: Boolean? = null,
  @SerialName("tcp-concurrent") var tcpConcurrent: Boolean? = null,
  @SerialName("find-process-mode") var findProcessMode: FindProcessMode? = null,
  @SerialName("dns") val dns: Dns = Dns(),
  @SerialName("clash-for-android") val app: App = App(),
  @SerialName("sniffer") val sniffer: Sniffer = Sniffer(),
  @SerialName("geox-url") val geoxurl: GeoXUrl = GeoXUrl(),
) : Parcelable {
  @Serializable
  @Parcelize
  data class Dns(
    @SerialName("enable") var enable: Boolean? = null,
    @SerialName("prefer-h3") var preferH3: Boolean? = null,
    @SerialName("listen") var listen: String? = null,
    @SerialName("ipv6") var ipv6: Boolean? = null,
    @SerialName("use-hosts") var useHosts: Boolean? = null,
    @SerialName("enhanced-mode") var enhancedMode: DnsEnhancedMode? = null,
    @SerialName("nameserver") var nameServer: List<String>? = null,
    @SerialName("fallback") var fallback: List<String>? = null,
    @SerialName("default-nameserver") var defaultServer: List<String>? = null,
    @SerialName("fake-ip-filter") var fakeIpFilter: List<String>? = null,
    @SerialName("fake-ip-filter-mode") var fakeIPFilterMode: FilterMode? = null,
    @SerialName("fallback-filter") val fallbackFilter: DnsFallbackFilter = DnsFallbackFilter(),
    @SerialName("nameserver-policy") var nameserverPolicy: Map<String, String>? = null,
  ) : Parcelable

  @Serializable
  @Parcelize
  data class DnsFallbackFilter(
    @SerialName("geoip") var geoIp: Boolean? = null,
    @SerialName("geoip-code") var geoIpCode: String? = null,
    @SerialName("ipcidr") var ipcidr: List<String>? = null,
    @SerialName("domain") var domain: List<String>? = null,
  ) : Parcelable

  @Serializable
  @Parcelize
  data class App(@SerialName("append-system-dns") var appendSystemDns: Boolean? = null) : Parcelable

  @Serializable
  enum class FindProcessMode {
    @SerialName("off") Off,
    @SerialName("strict") Strict,
    @SerialName("always") Always,
  }

  @Serializable
  enum class DnsEnhancedMode {
    @SerialName("normal") None,
    @SerialName("redir-host") Mapping,
    @SerialName("fake-ip") FakeIp,
  }

  @Serializable
  enum class FilterMode {
    @SerialName("blacklist") BlackList,
    @SerialName("whitelist") WhiteList,
  }

  @Serializable
  @Parcelize
  data class Sniffer(
    @SerialName("enable") var enable: Boolean? = null,
    @SerialName("sniff") var sniff: Sniff = Sniff(),
    @SerialName("force-dns-mapping") var forceDnsMapping: Boolean? = null,
    @SerialName("parse-pure-ip") var parsePureIp: Boolean? = null,
    @SerialName("override-destination") var overrideDestination: Boolean? = null,
    @SerialName("force-domain") var forceDomain: List<String>? = null,
    @SerialName("skip-domain") var skipDomain: List<String>? = null,
    @SerialName("skip-src-address") var skipSrcAddress: List<String>? = null,
    @SerialName("skip-dst-address") var skipDstAddress: List<String>? = null,
  ) : Parcelable

  @Serializable
  @Parcelize
  data class GeoXUrl(
    @SerialName("geoip") var geoip: String? = null,
    @SerialName("mmdb") var mmdb: String? = null,
    @SerialName("geosite") var geosite: String? = null,
  ) : Parcelable

  @Serializable
  @Parcelize
  data class ExternalControllerCors(
    @SerialName("allow-origins") var allowOrigins: List<String>? = null,
    @SerialName("allow-private-network") var allowPrivateNetwork: Boolean? = null,
  ) : Parcelable

  @Serializable
  @Parcelize
  data class Sniff(
    @SerialName("HTTP") var http: ProtocolConfig = ProtocolConfig(),
    @SerialName("TLS") var tls: ProtocolConfig = ProtocolConfig(),
    @SerialName("QUIC") var quic: ProtocolConfig = ProtocolConfig(),
  ) : Parcelable

  @Serializable
  @Parcelize
  data class ProtocolConfig(
    @SerialName("ports") var ports: List<String>? = null,
    @SerialName("override-destination") var overrideDestination: Boolean? = null,
  ) : Parcelable
}
