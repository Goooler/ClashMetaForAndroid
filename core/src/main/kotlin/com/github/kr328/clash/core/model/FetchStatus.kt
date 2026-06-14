package com.github.kr328.clash.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class FetchStatus(
  val action: Action,
  val args: List<String>,
  val progress: Int,
  val max: Int,
  val subUpload: Long = 0L,
  val subDownload: Long = 0L,
  val subTotal: Long = 0L,
  val subExpire: Long = 0L,
  val subUpdateInterval: Long = 0L,
) : Parcelable {
  enum class Action {
    FetchConfiguration,
    FetchProviders,
    SubscriptionInfo,
    Verifying,
  }
}
