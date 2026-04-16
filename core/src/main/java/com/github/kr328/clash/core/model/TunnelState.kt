package com.github.kr328.clash.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class TunnelState(val mode: Mode) : Parcelable {
    @Serializable
    enum class Mode {
        @SerialName("direct") Direct,
        @SerialName("global") Global,
        @SerialName("rule") Rule,
    }
}
