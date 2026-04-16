@file:UseSerializers(DateSerializer::class)

package com.github.kr328.clash.core.model

import android.os.Parcelable
import com.github.kr328.clash.core.util.DateSerializer
import java.util.Date
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Parcelize
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
