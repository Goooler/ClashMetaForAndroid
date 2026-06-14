package com.github.kr328.clash.core.model

import android.os.Parcelable
import kotlin.jvm.JvmInline
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Proxy(
  val name: String,
  val title: String,
  val subtitle: String,
  val type: Type,
  val delay: Int,
  val isGroup: Boolean,
) : Parcelable {
  @JvmInline
  @Serializable
  @Parcelize
  value class Type(val name: String) : Parcelable {
    companion object {
      val Selector: Type = Type("Selector")
      val URLTest: Type = Type("URLTest")
      val Unknown: Type = Type("Unknown")
    }
  }
}
