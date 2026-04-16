package com.github.kr328.clash.service.remote

import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.core.model.ConfigurationOverride
import com.github.kr328.clash.core.model.Provider
import com.github.kr328.clash.core.model.ProviderList
import com.github.kr328.clash.core.model.ProxyGroup
import com.github.kr328.clash.core.model.ProxySort
import com.github.kr328.clash.core.model.TunnelState
import com.github.kr328.clash.core.model.UiConfiguration
import com.github.kr328.kaidl.BinderInterface

@BinderInterface
interface IClashManager {
    fun queryTunnelState(): TunnelState

    fun queryTrafficTotal(): Long

    fun queryProxyGroupNames(excludeNotSelectable: Boolean): List<String>

    fun queryProxyGroup(name: String, proxySort: ProxySort): ProxyGroup

    fun queryConfiguration(): UiConfiguration

    fun queryProviders(): ProviderList

    fun patchSelector(group: String, name: String): Boolean

    suspend fun healthCheck(group: String)

    suspend fun updateProvider(type: Provider.Type, name: String)

    fun queryOverride(slot: Clash.OverrideSlot): ConfigurationOverride

    fun patchOverride(slot: Clash.OverrideSlot, configuration: ConfigurationOverride)

    fun clearOverride(slot: Clash.OverrideSlot)

    fun setLogObserver(observer: ILogObserver?)
}
