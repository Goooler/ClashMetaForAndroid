@file:UseSerializers(DateSerializer::class)

package com.github.kr328.clash.core.model

import android.os.Parcel
import android.os.Parcelable
import com.github.kr328.clash.core.util.DateSerializer
import java.util.Date
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Parcelize
@TypeParceler<Date, DateParceler>
@Serializable
data class LogMessage(val level: Level, val message: String, val time: Date) : Parcelable {
  @Serializable
  enum class Level {
    @SerialName("debug") Debug,
    @SerialName("info") Info,
    @SerialName("warning") Warning,
    @SerialName("error") Error,
    @SerialName("silent") Silent,
    @SerialName("unknown") Unknown,
  }
}

private object DateParceler : Parceler<Date> {
  override fun create(parcel: Parcel): Date {
    return Date(parcel.readLong())
  }

  override fun Date.write(parcel: Parcel, flags: Int) {
    parcel.writeLong(time)
  }
}
