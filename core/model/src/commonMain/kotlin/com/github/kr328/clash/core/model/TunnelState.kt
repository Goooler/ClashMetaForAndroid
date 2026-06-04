package com.github.kr328.clash.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TunnelState(val mode: Mode) {
  @Serializable
  enum class Mode {
    @SerialName("direct") Direct,
    @SerialName("global") Global,
    @SerialName("rule") Rule,
  }
}
