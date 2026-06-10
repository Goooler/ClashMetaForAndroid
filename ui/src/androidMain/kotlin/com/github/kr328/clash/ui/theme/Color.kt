package com.github.kr328.clash.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

val TabbyLightPrimary = Color(0xFF1E4376)
val TabbyDarkPrimary = Color(0xFF1976D2)
val TabbyOnPrimary = Color(0xFFFFFFFF)

val TabbySystemUiOverlay = Color(0x50000000)

val TabbyLightBackground = Color(0xFFFAFAFA)
val TabbyDarkBackground = Color(0xFF121212)
val TabbyDarkSurface = Color(0xFF202020)

val TabbyLightControlNormal = Color(0xFF000000)
val TabbyDarkControlNormal = Color(0xFFFFFFFF)
val TabbyLightStopped = Color(0xFF808080)
val TabbyLightControlDisabled = Color(0xFFD3D3D3)
val TabbyDarkControlDisabled = Color(0xFF808080)

val TabbyError = Color(0xFFB00020)

@Immutable
data class TabbyColorTokens(
  val controlNormal: Color,
  val controlDisabled: Color,
  val stopped: Color,
  val logo: Color,
  val systemUiOverlay: Color,
)

internal val LightTabbyColorTokens =
  TabbyColorTokens(
    controlNormal = TabbyLightControlNormal,
    controlDisabled = TabbyLightControlDisabled,
    stopped = TabbyLightStopped,
    logo = TabbyLightPrimary,
    systemUiOverlay = TabbySystemUiOverlay,
  )

internal val DarkTabbyColorTokens =
  TabbyColorTokens(
    controlNormal = TabbyDarkControlNormal,
    controlDisabled = TabbyDarkControlDisabled,
    stopped = TabbyDarkSurface,
    logo = TabbyDarkControlNormal,
    systemUiOverlay = Color.Transparent,
  )
