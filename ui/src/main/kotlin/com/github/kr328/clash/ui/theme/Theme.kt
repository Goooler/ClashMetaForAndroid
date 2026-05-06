package com.github.kr328.clash.ui.theme

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapperProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.kr328.clash.model.DarkMode

private val DarkColorScheme =
  darkColorScheme(
    primary = MihomoDarkPrimary,
    onPrimary = MihomoOnPrimary,
    secondary = MihomoDarkPrimary,
    onSecondary = MihomoOnPrimary,
    background = MihomoDarkBackground,
    onBackground = MihomoDarkControlNormal,
    surface = MihomoDarkSurface,
    onSurface = MihomoDarkControlNormal,
    surfaceVariant = MihomoDarkSurface,
    onSurfaceVariant = MihomoDarkControlNormal,
    surfaceContainerLowest = MihomoDarkBackground,
    surfaceContainerLow = MihomoDarkSurface,
    surfaceContainer = MihomoDarkSurface,
    surfaceContainerHigh = MihomoDarkSurface,
    surfaceContainerHighest = MihomoDarkSurface,
    outline = MihomoDarkControlDisabled,
    inverseSurface = MihomoLightBackground,
    inverseOnSurface = MihomoLightControlNormal,
    inversePrimary = MihomoDarkPrimary,
    error = MihomoError,
    onError = MihomoOnPrimary,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = MihomoLightPrimary,
    onPrimary = MihomoOnPrimary,
    secondary = MihomoLightPrimary,
    onSecondary = MihomoOnPrimary,
    background = MihomoLightBackground,
    onBackground = MihomoLightControlNormal,
    surface = MihomoLightBackground,
    onSurface = MihomoLightControlNormal,
    surfaceVariant = MihomoLightBackground,
    onSurfaceVariant = MihomoLightControlNormal,
    surfaceContainerLowest = MihomoLightBackground,
    surfaceContainerLow = MihomoLightBackground,
    surfaceContainer = MihomoLightBackground,
    surfaceContainerHigh = MihomoLightBackground,
    surfaceContainerHighest = MihomoLightBackground,
    outline = MihomoLightControlDisabled,
    inverseSurface = MihomoDarkSurface,
    inverseOnSurface = MihomoDarkControlNormal,
    inversePrimary = MihomoDarkPrimary,
    error = MihomoError,
    onError = MihomoOnPrimary,
  )

@Immutable
data class MihomoDimens(
  val dialogPadding: Dp,
  val itemMinHeight: Dp,
  val itemHeaderComponentSize: Dp,
  val itemHeaderMargin: Dp,
  val itemPaddingVertical: Dp,
  val itemTextMargin: Dp,
  val settingsItemEndPadding: Dp,
  val dialogContentSpacing: Dp,
)

private val DefaultMihomoDimens =
  MihomoDimens(
    dialogPadding = 20.dp,
    itemMinHeight = 75.dp,
    itemHeaderComponentSize = 30.dp,
    itemHeaderMargin = 17.5.dp,
    itemPaddingVertical = 16.dp,
    itemTextMargin = 5.dp,
    settingsItemEndPadding = 20.dp,
    dialogContentSpacing = 12.dp,
  )

private val LocalMihomoColors = staticCompositionLocalOf { LightMihomoColorTokens }
private val LocalMihomoDimens = staticCompositionLocalOf { DefaultMihomoDimens }
private val LocalMihomoTypography = staticCompositionLocalOf { DefaultMihomoTextStyles }

val mihomoDimens: MihomoDimens
  @Composable @ReadOnlyComposable get() = LocalMihomoDimens.current

@Composable
fun MihomoTheme(
  darkModeInSettings: DarkMode = DarkMode.Auto,
  darkTheme: Boolean =
    when (darkModeInSettings) {
      ForceDark -> true
      ForceLight -> false
      Auto -> isSystemInDarkTheme()
    },
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  val mihomoColors = if (darkTheme) DarkMihomoColorTokens else LightMihomoColorTokens

  CompositionLocalProvider(
    LocalMihomoColors provides mihomoColors,
    LocalMihomoDimens provides DefaultMihomoDimens,
    LocalMihomoTypography provides DefaultMihomoTextStyles,
  ) {
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
  }
}

class MihomoThemeWrapper : PreviewWrapperProvider {
  @Composable
  override fun Wrap(content: @Composable () -> Unit) {
    MihomoTheme(content = content)
  }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = UI_MODE_NIGHT_YES)
annotation class PreviewMihomo
