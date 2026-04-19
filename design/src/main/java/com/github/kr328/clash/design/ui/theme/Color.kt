package com.github.kr328.clash.design.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

val ClashLightPrimary = Color(0xFF1E4376)
val ClashDarkPrimary = Color(0xFF1976D2)
val ClashOnPrimary = Color(0xFFFFFFFF)

val ClashSystemUiOverlay = Color(0x50000000)

val ClashLightBackground = Color(0xFFFAFAFA)
val ClashDarkBackground = Color(0xFF121212)
val ClashDarkSurface = Color(0xFF202020)

val ClashLightControlNormal = Color(0xFF000000)
val ClashDarkControlNormal = Color(0xFFFFFFFF)
val ClashLightClashStopped = Color(0xFF808080)
val ClashLightControlDisabled = Color(0xFFD3D3D3)
val ClashDarkControlDisabled = Color(0xFF808080)

val ClashError = Color(0xFFB00020)

@Immutable
data class ClashColorTokens(
  val controlNormal: Color,
  val controlDisabled: Color,
  val clashStopped: Color,
  val logo: Color,
  val systemUiOverlay: Color,
)

internal val LightClashColorTokens =
  ClashColorTokens(
    controlNormal = ClashLightControlNormal,
    controlDisabled = ClashLightControlDisabled,
    clashStopped = ClashLightClashStopped,
    logo = ClashLightPrimary,
    systemUiOverlay = ClashSystemUiOverlay,
  )

internal val DarkClashColorTokens =
  ClashColorTokens(
    controlNormal = ClashDarkControlNormal,
    controlDisabled = ClashDarkControlDisabled,
    clashStopped = ClashDarkSurface,
    logo = ClashDarkControlNormal,
    systemUiOverlay = Color.Transparent,
  )
