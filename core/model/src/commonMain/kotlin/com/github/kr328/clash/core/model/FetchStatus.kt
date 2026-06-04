package com.github.kr328.clash.core.model

import kotlinx.serialization.Serializable

@Serializable
data class FetchStatus(
  val action: Action,
  val args: List<String>,
  val progress: Int,
  val max: Int,
) {
  enum class Action {
    FetchConfiguration,
    FetchProviders,
    Verifying,
  }
}
