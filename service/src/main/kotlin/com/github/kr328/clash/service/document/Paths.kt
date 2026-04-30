package com.github.kr328.clash.service.document

import kotlin.uuid.Uuid

object Paths {
  const val CONFIGURATION_ID = "config.yaml"
  const val PROVIDERS_ID = "providers"

  fun resolve(path: String): Path {
    val segments = path.split("/").filter { it.isNotBlank() && it != "." && it != ".." }

    return when (segments.size) {
      0 -> Path(uuid = null, scope = null, relative = null)
      1 -> Path(uuid = Uuid.parse(segments[0]), scope = null, relative = null)
      2 ->
        Path(
          uuid = Uuid.parse(segments[0]),
          scope =
            when (segments[1]) {
              CONFIGURATION_ID -> Path.Scope.Configuration
              PROVIDERS_ID -> Path.Scope.Providers
              else -> throw IllegalArgumentException("unknown scope ${segments[1]}")
            },
          relative = null,
        )
      else ->
        Path(
          uuid = Uuid.parse(segments[0]),
          scope =
            when (segments[1]) {
              CONFIGURATION_ID -> Path.Scope.Configuration
              PROVIDERS_ID -> Path.Scope.Providers
              else -> throw IllegalArgumentException("unknown scope ${segments[1]}")
            },
          relative = segments.drop(2),
        )
    }
  }
}
