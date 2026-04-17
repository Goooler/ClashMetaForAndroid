package com.github.kr328.clash.core.model

import android.os.Parcelable
import kotlinx.serialization.Serializable
import kotlinx.parcelize.Parcelize

@Parcelize
@Serializable
data class FetchStatus(
  val action: Action,
  val args: List<String>,
  val progress: Int,
  val max: Int,
) : Parcelable {
  enum class Action {
    FetchConfiguration,
    FetchProviders,
    Verifying,
  }
}
