@file:UseSerializers(UUIDSerializer::class)

package com.github.kr328.clash.service.model

import android.os.Parcelable
import com.github.kr328.clash.service.util.UUIDSerializer
import java.util.UUID
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Parcelize
@Serializable
data class Profile(
  val uuid: UUID,
  val name: String,
  val type: Type,
  val source: String,
  val active: Boolean,
  val interval: Long,
  val upload: Long,
  var download: Long,
  val total: Long,
  val expire: Long,
  val updatedAt: Long,
  val imported: Boolean,
  val pending: Boolean,
) : Parcelable {
  enum class Type {
    File,
    Url,
    External,
  }
}
