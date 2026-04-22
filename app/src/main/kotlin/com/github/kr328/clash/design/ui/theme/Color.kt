package com.github.kr328.clash.design.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

val MihomoLightPrimary = Color(0xFF1E4376)
val MihomoDarkPrimary = Color(0xFF1976D2)
val MihomoOnPrimary = Color(0xFFFFFFFF)

val MihomoSystemUiOverlay = Color(0x50000000)

val MihomoLightBackground = Color(0xFFFAFAFA)
val MihomoDarkBackground = Color(0xFF121212)
val MihomoDarkSurface = Color(0xFF202020)

val MihomoLightControlNormal = Color(0xFF000000)
val MihomoDarkControlNormal = Color(0xFFFFFFFF)
val MihomoLightStopped = Color(0xFF808080)
val MihomoLightControlDisabled = Color(0xFFD3D3D3)
val MihomoDarkControlDisabled = Color(0xFF808080)

val MihomoError = Color(0xFFB00020)

@Immutable
data class MihomoColorTokens(
  val controlNormal: Color,
  val controlDisabled: Color,
  val stopped: Color,
  val logo: Color,
  val systemUiOverlay: Color,
)

internal val LightMihomoColorTokens =
  MihomoColorTokens(
    controlNormal = MihomoLightControlNormal,
    controlDisabled = MihomoLightControlDisabled,
    stopped = MihomoLightStopped,
    logo = MihomoLightPrimary,
    systemUiOverlay = MihomoSystemUiOverlay,
  )

internal val DarkMihomoColorTokens =
  MihomoColorTokens(
    controlNormal = MihomoDarkControlNormal,
    controlDisabled = MihomoDarkControlDisabled,
    stopped = MihomoDarkSurface,
    logo = MihomoDarkControlNormal,
    systemUiOverlay = Color.Transparent,
  )
