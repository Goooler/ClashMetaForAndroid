package com.github.kr328.clash.core.model

import android.os.Parcelable
import kotlin.jvm.JvmInline
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@JvmInline @Serializable @Parcelize value class ProxyType(val name: String) : Parcelable

@Parcelize
@Serializable
data class Proxy(
  val name: String,
  val title: String,
  val subtitle: String,
  val type: ProxyType,
  val delay: Int,
  val isGroup: Boolean,
) : Parcelable {}
