package com.github.kr328.clash.core.model

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Parcelize
@Serializable
data class ConfigurationOverride(
  @SerialName("port") val httpPort: Int? = null,
  @SerialName("socks-port") val socksPort: Int? = null,
  @SerialName("redir-port") val redirectPort: Int? = null,
  @SerialName("tproxy-port") val tproxyPort: Int? = null,
  @SerialName("mixed-port") val mixedPort: Int? = null,
  val authentication: List<String>? = null,
  @SerialName("allow-lan") val allowLan: Boolean? = null,
  @SerialName("bind-address") val bindAddress: String? = null,
  val mode: TunnelState.Mode? = null,
  @SerialName("log-level") val logLevel: LogMessage.Level? = null,
  val ipv6: Boolean? = null,
  @SerialName("external-controller") val externalController: String? = null,
  @SerialName("external-controller-tls") val externalControllerTLS: String? = null,
  @SerialName("external-controller-cors")
  val externalControllerCors: ExternalControllerCors = ExternalControllerCors(),
  val secret: String? = null,
  val hosts: Map<String, String>? = null,
  @SerialName("unified-delay") val unifiedDelay: Boolean? = null,
  @SerialName("geodata-mode") val geodataMode: Boolean? = null,
  @SerialName("tcp-concurrent") val tcpConcurrent: Boolean? = null,
  @SerialName("find-process-mode") val findProcessMode: FindProcessMode? = null,
  val dns: Dns = Dns(),
  @SerialName("clash-for-android") val app: App = App(),
  val sniffer: Sniffer = Sniffer(),
  @SerialName("geox-url") val geoxurl: GeoXUrl = GeoXUrl(),
  @IgnoredOnParcel @Transient val revision: Int = 0,
) : Parcelable {
  @Serializable
  @Parcelize
  data class Dns(
    val enable: Boolean? = null,
    @SerialName("prefer-h3") val preferH3: Boolean? = null,
    val listen: String? = null,
    val ipv6: Boolean? = null,
    @SerialName("use-hosts") val useHosts: Boolean? = null,
    @SerialName("enhanced-mode") val enhancedMode: DnsEnhancedMode? = null,
    @SerialName("nameserver") val nameServer: List<String>? = null,
    val fallback: List<String>? = null,
    @SerialName("default-nameserver") val defaultServer: List<String>? = null,
    @SerialName("fake-ip-filter") val fakeIpFilter: List<String>? = null,
    @SerialName("fake-ip-filter-mode") val fakeIPFilterMode: FilterMode? = null,
    @SerialName("fallback-filter") val fallbackFilter: DnsFallbackFilter = DnsFallbackFilter(),
    @SerialName("nameserver-policy") val nameserverPolicy: Map<String, String>? = null,
  ) : Parcelable

  @Serializable
  @Parcelize
  data class DnsFallbackFilter(
    @SerialName("geoip") val geoIp: Boolean? = null,
    @SerialName("geoip-code") val geoIpCode: String? = null,
    val ipcidr: List<String>? = null,
    val domain: List<String>? = null,
  ) : Parcelable

  @Serializable
  @Parcelize
  data class App(@SerialName("append-system-dns") val appendSystemDns: Boolean? = null) : Parcelable

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
    val enable: Boolean? = null,
    val sniff: Sniff = Sniff(),
    @SerialName("force-dns-mapping") val forceDnsMapping: Boolean? = null,
    @SerialName("parse-pure-ip") val parsePureIp: Boolean? = null,
    @SerialName("override-destination") val overrideDestination: Boolean? = null,
    @SerialName("force-domain") val forceDomain: List<String>? = null,
    @SerialName("skip-domain") val skipDomain: List<String>? = null,
    @SerialName("skip-src-address") val skipSrcAddress: List<String>? = null,
    @SerialName("skip-dst-address") val skipDstAddress: List<String>? = null,
  ) : Parcelable

  @Serializable
  @Parcelize
  data class GeoXUrl(
    val geoip: String? = null,
    val mmdb: String? = null,
    val geosite: String? = null,
  ) : Parcelable

  @Serializable
  @Parcelize
  data class ExternalControllerCors(
    @SerialName("allow-origins") val allowOrigins: List<String>? = null,
    @SerialName("allow-private-network") val allowPrivateNetwork: Boolean? = null,
  ) : Parcelable

  @Serializable
  @Parcelize
  data class Sniff(
    @SerialName("HTTP") val http: ProtocolConfig = ProtocolConfig(),
    @SerialName("TLS") val tls: ProtocolConfig = ProtocolConfig(),
    @SerialName("QUIC") val quic: ProtocolConfig = ProtocolConfig(),
  ) : Parcelable

  @Serializable
  @Parcelize
  data class ProtocolConfig(
    val ports: List<String>? = null,
    @SerialName("override-destination") val overrideDestination: Boolean? = null,
  ) : Parcelable
}
