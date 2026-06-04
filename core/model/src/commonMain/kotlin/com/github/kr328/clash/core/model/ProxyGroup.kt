package com.github.kr328.clash.core.model

import kotlinx.serialization.Serializable

@Serializable data class ProxyGroup(val type: Proxy.Type, val proxies: List<Proxy>, val now: String)
