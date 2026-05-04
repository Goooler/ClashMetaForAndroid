package com.github.kr328.clash.core.model

import android.os.Parcel
import android.os.Parcelable
import com.github.kr328.clash.core.util.createListFromParcelSlice
import com.github.kr328.clash.core.util.writeToParcelSlice
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kotlinx.parcelize.parcelableCreator

@Parcelize
@TypeParceler<List<Provider>, ProviderListParceler>
class ProviderList(private val data: List<Provider>) : List<Provider> by data, Parcelable

private object ProviderListParceler : Parceler<List<Provider>> {
  override fun create(parcel: Parcel): List<Provider> {
    return parcelableCreator<Provider>().createListFromParcelSlice(parcel, 0, 20)
  }

  override fun List<Provider>.write(parcel: Parcel, flags: Int) {
    writeToParcelSlice(parcel, flags)
  }
}
