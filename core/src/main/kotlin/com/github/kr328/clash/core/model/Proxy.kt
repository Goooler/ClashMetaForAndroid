package com.github.kr328.clash.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Proxy(
  val name: String,
  val title: String,
  val subtitle: String,
  val type: String,
  val delay: Int,
  val isGroup: Boolean,
) : Parcelable {
}
