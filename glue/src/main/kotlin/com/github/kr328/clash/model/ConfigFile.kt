package com.github.kr328.clash.model

data class ConfigFile(
  val id: String,
  val name: String,
  val size: Long,
  val lastModified: Long,
  val isDirectory: Boolean,
)
