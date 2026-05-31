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
import com.github.kr328.clash.glue.model.DarkMode

private val DarkColorScheme =
  darkColorScheme(
    primary = TabbyDarkPrimary,
    onPrimary = TabbyOnPrimary,
    primaryContainer = TabbyDarkPrimary,
    onPrimaryContainer = TabbyOnPrimary,
    secondary = TabbyDarkPrimary,
    onSecondary = TabbyOnPrimary,
    background = TabbyDarkBackground,
    onBackground = TabbyDarkControlNormal,
    surface = TabbyDarkSurface,
    onSurface = TabbyDarkControlNormal,
    surfaceVariant = TabbyDarkSurface,
    onSurfaceVariant = TabbyDarkControlNormal,
    surfaceContainerLowest = TabbyDarkBackground,
    surfaceContainerLow = TabbyDarkSurface,
    surfaceContainer = TabbyDarkSurface,
    surfaceContainerHigh = TabbyDarkSurface,
    surfaceContainerHighest = TabbyDarkSurface,
    outline = TabbyDarkControlDisabled,
    inverseSurface = TabbyLightBackground,
    inverseOnSurface = TabbyLightControlNormal,
    inversePrimary = TabbyDarkPrimary,
    error = TabbyError,
    onError = TabbyOnPrimary,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = TabbyLightPrimary,
    onPrimary = TabbyOnPrimary,
    primaryContainer = TabbyLightPrimary,
    onPrimaryContainer = TabbyOnPrimary,
    secondary = TabbyLightPrimary,
    onSecondary = TabbyOnPrimary,
    background = TabbyLightBackground,
    onBackground = TabbyLightControlNormal,
    surface = TabbyLightBackground,
    onSurface = TabbyLightControlNormal,
    surfaceVariant = TabbyLightBackground,
    onSurfaceVariant = TabbyLightControlNormal,
    surfaceContainerLowest = TabbyLightBackground,
    surfaceContainerLow = TabbyLightBackground,
    surfaceContainer = TabbyLightBackground,
    surfaceContainerHigh = TabbyLightBackground,
    surfaceContainerHighest = TabbyLightBackground,
    outline = TabbyLightControlDisabled,
    inverseSurface = TabbyDarkSurface,
    inverseOnSurface = TabbyDarkControlNormal,
    inversePrimary = TabbyDarkPrimary,
    error = TabbyError,
    onError = TabbyOnPrimary,
  )

@Immutable
data class TabbyDimens(
  val dialogPadding: Dp,
  val itemMinHeight: Dp,
  val itemHeaderComponentSize: Dp,
  val itemHeaderMargin: Dp,
  val itemPaddingVertical: Dp,
  val itemTextMargin: Dp,
  val settingsItemEndPadding: Dp,
  val dialogContentSpacing: Dp,
)

private val DefaultTabbyDimens =
  TabbyDimens(
    dialogPadding = 20.dp,
    itemMinHeight = 75.dp,
    itemHeaderComponentSize = 30.dp,
    itemHeaderMargin = 17.5.dp,
    itemPaddingVertical = 16.dp,
    itemTextMargin = 5.dp,
    settingsItemEndPadding = 20.dp,
    dialogContentSpacing = 12.dp,
  )

private val LocalTabbyColors = staticCompositionLocalOf { LightTabbyColorTokens }
private val LocalTabbyDimens = staticCompositionLocalOf { DefaultTabbyDimens }
private val LocalTabbyTypography = staticCompositionLocalOf { DefaultTabbyTextStyles }

val tabbyDimens: TabbyDimens
  @Composable @ReadOnlyComposable get() = LocalTabbyDimens.current

@Composable
fun TabbyTheme(
  darkMode: DarkMode = DarkMode.Auto,
  darkTheme: Boolean =
    when (darkMode) {
      ForceDark -> true
      ForceLight -> false
      Auto -> isSystemInDarkTheme()
    },
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  val tabbyColors = if (darkTheme) DarkTabbyColorTokens else LightTabbyColorTokens

  CompositionLocalProvider(
    LocalTabbyColors provides tabbyColors,
    LocalTabbyDimens provides DefaultTabbyDimens,
    LocalTabbyTypography provides DefaultTabbyTextStyles,
  ) {
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
  }
}

class TabbyThemeWrapper : PreviewWrapperProvider {
  @Composable
  override fun Wrap(content: @Composable () -> Unit) {
    TabbyTheme(content = content)
  }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = UI_MODE_NIGHT_YES)
annotation class PreviewTabby
