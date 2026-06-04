package com.github.kr328.clash.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LogMessage(val level: Level, val message: String, val timeMillis: Long) {
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
