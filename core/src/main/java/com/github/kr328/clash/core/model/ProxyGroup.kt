package com.github.kr328.clash.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class ProxyGroup(val type: Proxy.Type, val proxies: List<Proxy>, val now: String) :
    Parcelable {
    @Parcelize class SliceProxyList(val data: List<Proxy>) : List<Proxy> by data, Parcelable
}
