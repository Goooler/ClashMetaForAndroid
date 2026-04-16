package com.github.kr328.clash.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class ConfigurationOverride(
    @SerialName("port") val httpPort: Int? = null,
    @SerialName("socks-port") val socksPort: Int? = null,
    @SerialName("redir-port") val redirectPort: Int? = null,
    @SerialName("tproxy-port") val tproxyPort: Int? = null,
    @SerialName("mixed-port") val mixedPort: Int? = null,
    @SerialName("authentication") val authentication: List<String>? = null,
    @SerialName("allow-lan") val allowLan: Boolean? = null,
    @SerialName("bind-address") val bindAddress: String? = null,
    @SerialName("mode") val mode: TunnelState.Mode? = null,
    @SerialName("log-level") val logLevel: LogMessage.Level? = null,
    @SerialName("ipv6") val ipv6: Boolean? = null,
    @SerialName("external-controller") val externalController: String? = null,
    @SerialName("external-controller-tls") val externalControllerTLS: String? = null,
    @SerialName("external-controller-cors")
    val externalControllerCors: ExternalControllerCors = ExternalControllerCors(),
    @SerialName("secret") val secret: String? = null,
    @SerialName("hosts") val hosts: Map<String, String>? = null,
    @SerialName("unified-delay") val unifiedDelay: Boolean? = null,
    @SerialName("geodata-mode") val geodataMode: Boolean? = null,
    @SerialName("tcp-concurrent") val tcpConcurrent: Boolean? = null,
    @SerialName("find-process-mode") val findProcessMode: FindProcessMode? = null,
    @SerialName("dns") val dns: Dns = Dns(),
    @SerialName("clash-for-android") val app: App = App(),
    @SerialName("sniffer") val sniffer: Sniffer = Sniffer(),
    @SerialName("geox-url") val geoxurl: GeoXUrl = GeoXUrl(),
) : Parcelable {
    @Parcelize
    @Serializable
    data class Dns(
        @SerialName("enable") val enable: Boolean? = null,
        @SerialName("prefer-h3") val preferH3: Boolean? = null,
        @SerialName("listen") val listen: String? = null,
        @SerialName("ipv6") val ipv6: Boolean? = null,
        @SerialName("use-hosts") val useHosts: Boolean? = null,
        @SerialName("enhanced-mode") val enhancedMode: DnsEnhancedMode? = null,
        @SerialName("nameserver") val nameServer: List<String>? = null,
        @SerialName("fallback") val fallback: List<String>? = null,
        @SerialName("default-nameserver") val defaultServer: List<String>? = null,
        @SerialName("fake-ip-filter") val fakeIpFilter: List<String>? = null,
        @SerialName("fake-ip-filter-mode") val fakeIPFilterMode: FilterMode? = null,
        @SerialName("fallback-filter") val fallbackFilter: DnsFallbackFilter = DnsFallbackFilter(),
        @SerialName("nameserver-policy") val nameserverPolicy: Map<String, String>? = null,
    ) : Parcelable

    @Parcelize
    @Serializable
    data class DnsFallbackFilter(
        @SerialName("geoip") val geoIp: Boolean? = null,
        @SerialName("geoip-code") val geoIpCode: String? = null,
        @SerialName("ipcidr") val ipcidr: List<String>? = null,
        @SerialName("domain") val domain: List<String>? = null,
    ) : Parcelable

    @Parcelize
    @Serializable
    data class App(@SerialName("append-system-dns") val appendSystemDns: Boolean? = null) :
        Parcelable

    @Parcelize
    @Serializable
    enum class FindProcessMode : Parcelable {
        @SerialName("off") Off,
        @SerialName("strict") Strict,
        @SerialName("always") Always,
    }

    @Parcelize
    @Serializable
    enum class DnsEnhancedMode : Parcelable {
        @SerialName("normal") None,
        @SerialName("redir-host") Mapping,
        @SerialName("fake-ip") FakeIp,
    }

    @Parcelize
    @Serializable
    enum class FilterMode : Parcelable {
        @SerialName("blacklist") BlackList,
        @SerialName("whitelist") WhiteList,
    }

    @Parcelize
    @Serializable
    data class Sniffer(
        @SerialName("enable") val enable: Boolean? = null,
        @SerialName("sniff") val sniff: Sniff = Sniff(),
        @SerialName("force-dns-mapping") val forceDnsMapping: Boolean? = null,
        @SerialName("parse-pure-ip") val parsePureIp: Boolean? = null,
        @SerialName("override-destination") val overrideDestination: Boolean? = null,
        @SerialName("force-domain") val forceDomain: List<String>? = null,
        @SerialName("skip-domain") val skipDomain: List<String>? = null,
        @SerialName("skip-src-address") val skipSrcAddress: List<String>? = null,
        @SerialName("skip-dst-address") val skipDstAddress: List<String>? = null,
    ) : Parcelable

    @Parcelize
    @Serializable
    data class GeoXUrl(
        @SerialName("geoip") val geoip: String? = null,
        @SerialName("mmdb") val mmdb: String? = null,
        @SerialName("geosite") val geosite: String? = null,
    ) : Parcelable

    @Parcelize
    @Serializable
    data class ExternalControllerCors(
        @SerialName("allow-origins") val allowOrigins: List<String>? = null,
        @SerialName("allow-private-network") val allowPrivateNetwork: Boolean? = null,
    ) : Parcelable

    @Parcelize
    @Serializable
    data class Sniff(
        @SerialName("HTTP") val http: ProtocolConig = ProtocolConig(),
        @SerialName("TLS") val tls: ProtocolConig = ProtocolConig(),
        @SerialName("QUIC") val quic: ProtocolConig = ProtocolConig(),
    ) : Parcelable

    @Parcelize
    @Serializable
    data class ProtocolConig(
        @SerialName("ports") val ports: List<String>? = null,
        @SerialName("override-destination") val overrideDestination: Boolean? = null,
    ) : Parcelable
}
