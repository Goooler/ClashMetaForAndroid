package com.github.kr328.clash.core.model

@JvmInline
value class Traffic(val packed: Long) {
  val uploadScaled: Long
    get() = unpackTrafficScaled(packed ushr 32)

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
