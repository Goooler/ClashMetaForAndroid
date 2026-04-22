package com.github.kr328.clash.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Proxy(
  val name: String,
  val title: String,
  val subtitle: String,
  val type: Type,
  val delay: Int,
) : Parcelable {
  enum class Type(val group: Boolean) {
    Direct(false),
    Reject(false),
    RejectDrop(false),
    Compatible(false),
    Pass(false),
    Shadowsocks(false),
    ShadowsocksR(false),
    Snell(false),
    Socks5(false),
    Http(false),
    Vmess(false),
    Vless(false),
    Trojan(false),
    Hysteria(false),
    Hysteria2(false),
    Tuic(false),
    WireGuard(false),
    Dns(false),
    Ssh(false),
    Mieru(false),
    AnyTLS(false),
    Sudoku(false),
    Masque(false),
    TrustTunnel(false),
    Relay(true),
    Selector(true),
    Fallback(true),
    URLTest(true),
    LoadBalance(true),
    Unknown(false),
  }
}
