package com.github.kr328.clash.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Provider(
  val name: String,
  val type: Type,
  val vehicleType: VehicleType,
  val updatedAt: Long,
) : Comparable<Provider> {
  enum class Type {
    Proxy,
    Rule,
  }

  enum class VehicleType {
    HTTP,
    File,
    Inline,
    Compatible,
  }

  override fun compareTo(other: Provider): Int {
    return compareValuesBy(this, other, Provider::type, Provider::name)
  }
}
