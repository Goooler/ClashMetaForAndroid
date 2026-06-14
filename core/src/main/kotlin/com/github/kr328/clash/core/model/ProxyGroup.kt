package com.github.kr328.clash.core.model

import android.os.Parcel
import android.os.Parcelable
import com.github.kr328.clash.core.util.createListFromParcelSlice
import com.github.kr328.clash.core.util.writeToParcelSlice
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kotlinx.parcelize.parcelableCreator
import kotlinx.serialization.Serializable

@Parcelize
@TypeParceler<List<Proxy>, ProxyGroupListParceler>
@Serializable
data class ProxyGroup(val type: Proxy.Type, val proxies: List<Proxy>, val now: String) : Parcelable

private object ProxyGroupListParceler : Parceler<List<Proxy>> {
  override fun create(parcel: Parcel): List<Proxy> {
    return parcelableCreator<Proxy>().createListFromParcelSlice(parcel, 0, 50)
  }

  override fun List<Proxy>.write(parcel: Parcel, flags: Int) {
    writeToParcelSlice(parcel, flags)
  }
}
