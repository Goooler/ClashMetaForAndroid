package com.github.kr328.clash.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@JvmInline
value class Traffic(val packed: Long) : Parcelable {
  /**
   * Upload value in internal scaled units used by native traffic formatting.
   *
   * This is not always raw bytes: for encoded type 0 it is bytes, while for encoded types 1..3 it
   * is an approximate centi-byte value reconstructed from native packed data.
   *
   * Native packing uses integer division (`value * 100 / 1024^n`), so unpacking restores a
   * truncated value (typically <= exact `bytes * 100`), not an exact one.
   */
  val uploadScaled: Long
    get() = unpackTrafficScaled(packed ushr 32)

  /**
   * Download value in internal scaled units used by native traffic formatting.
   *
   * This is not always raw bytes: for encoded type 0 it is bytes, while for encoded types 1..3 it
   * is an approximate centi-byte value reconstructed from native packed data.
   *
   * Native packing uses integer division (`value * 100 / 1024^n`), so unpacking restores a
   * truncated value (typically <= exact `bytes * 100`), not an exact one.
   */
  val downloadScaled: Long
    get() = unpackTrafficScaled(packed and 0xFFFFFFFF)
}

private fun unpackTrafficScaled(value: Long): Long {
  val type = (value ushr 30) and 0x3
  val data = value and 0x3FFFFFFF

  return when (type) {
    0L -> data
    1L -> data * 1024
    2L -> data * 1024 * 1024
    3L -> data * 1024 * 1024 * 1024
    else -> throw IllegalArgumentException("invalid value type")
  }
}
