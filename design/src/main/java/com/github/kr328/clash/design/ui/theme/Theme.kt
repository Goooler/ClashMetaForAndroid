package com.github.kr328.clash.design.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkColorScheme =
  darkColorScheme(
    primary = ClashDarkPrimary,
    onPrimary = ClashOnPrimary,
    secondary = ClashDarkPrimary,
    onSecondary = ClashOnPrimary,
    background = ClashDarkBackground,
    onBackground = ClashDarkControlNormal,
    surface = ClashDarkSurface,
    onSurface = ClashDarkControlNormal,
    surfaceVariant = ClashDarkSurface,
    onSurfaceVariant = ClashDarkControlNormal,
    outline = ClashDarkControlDisabled,
    error = ClashError,
    onError = ClashOnPrimary,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ClashLightPrimary,
    onPrimary = ClashOnPrimary,
    secondary = ClashLightPrimary,
    onSecondary = ClashOnPrimary,
    background = ClashLightBackground,
    onBackground = ClashLightControlNormal,
    surface = ClashLightBackground,
    onSurface = ClashLightControlNormal,
    surfaceVariant = ClashLightBackground,
    onSurfaceVariant = ClashLightControlNormal,
    outline = ClashLightControlDisabled,
    error = ClashError,
    onError = ClashOnPrimary,
  )

@Immutable
data class ClashDimens(
  val dividerSize: Dp,
  val dialogPadding: Dp,
  val dialogButtonMargin: Dp,
  val dialogMenuMinWidth: Dp,
  val surfaceLandscapeMinWidth: Dp,
  val toolbarHeight: Dp,
  val toolbarElevation: Dp,
  val toolbarImageActionPadding: Dp,
  val tabLayoutHeight: Dp,
  val tabLayoutElevation: Dp,
  val bottomSheetBackgroundPaddingTop: Dp,
  val bottomSheetHeaderHeight: Dp,
  val bottomSheetMenuItemsPadding: Dp,
  val largeItemPaddingVertical: Dp,
  val largeItemHeaderComponentSize: Dp,
  val largeItemHeaderMarginHorizontal: Dp,
  val largeItemHeaderLayoutSize: Dp,
  val largeItemTailingMarginHorizontal: Dp,
  val largeItemTextMargin: Dp,
  val itemMinHeight: Dp,
  val itemHeaderComponentSize: Dp,
  val itemHeaderMargin: Dp,
  val itemTailingComponentSize: Dp,
  val itemTailingMargin: Dp,
  val itemPaddingVertical: Dp,
  val itemTextMargin: Dp,
  val itemMiddenMargin: Dp,
  val largeActionCardRadius: Dp,
  val largeActionCardElevation: Dp,
  val largeActionCardMinHeight: Dp,
  val tipsIconSize: Dp,
  val tipsIconMargin: Dp,
  val preferenceFullscreenButtonPadding: Dp,
  val mainCardMarginVertical: Dp,
  val mainLabelMarginVertical: Dp,
  val mainPaddingHorizontal: Dp,
  val mainLogoSize: Dp,
  val mainTopBannerHeight: Dp,
  val proxyLayoutPadding: Dp,
  val proxyContentPadding: Dp,
  val proxyContentPaddingGrid3: Dp,
  val proxyTextMargin: Dp,
  val proxyTextMarginGrid3: Dp,
  val proxyTextSize: TextUnit,
  val proxyTextSizeGrid3: TextUnit,
  val proxyCardRadius: Dp,
  val proxyCardOffset: Dp,
  val propertiesElementMarginVertical: Dp,
  val aboutIconSize: Dp,
  val aboutTextMargin: Dp,
  val logcatPaddingVertical: Dp,
  val logcatPaddingHorizontal: Dp,
)

private val DefaultClashDimens =
  ClashDimens(
    dividerSize = 1.dp,
    dialogPadding = 20.dp,
    dialogButtonMargin = 5.dp,
    dialogMenuMinWidth = 150.dp,
    surfaceLandscapeMinWidth = 500.dp,
    toolbarHeight = 56.dp,
    toolbarElevation = 5.dp,
    toolbarImageActionPadding = 5.dp,
    tabLayoutHeight = 48.dp,
    tabLayoutElevation = 5.dp,
    bottomSheetBackgroundPaddingTop = 10.dp,
    bottomSheetHeaderHeight = 5.dp,
    bottomSheetMenuItemsPadding = 5.dp,
    largeItemPaddingVertical = 15.dp,
    largeItemHeaderComponentSize = 30.dp,
    largeItemHeaderMarginHorizontal = 20.dp,
    largeItemHeaderLayoutSize = 70.dp,
    largeItemTailingMarginHorizontal = 20.dp,
    largeItemTextMargin = 5.dp,
    itemMinHeight = 75.dp,
    itemHeaderComponentSize = 30.dp,
    itemHeaderMargin = 17.5.dp,
    itemTailingComponentSize = 30.dp,
    itemTailingMargin = 17.5.dp,
    itemPaddingVertical = 16.dp,
    itemTextMargin = 5.dp,
    itemMiddenMargin = 10.dp,
    largeActionCardRadius = 8.dp,
    largeActionCardElevation = 5.dp,
    largeActionCardMinHeight = 85.dp,
    tipsIconSize = 25.dp,
    tipsIconMargin = 20.dp,
    preferenceFullscreenButtonPadding = 20.dp,
    mainCardMarginVertical = 5.dp,
    mainLabelMarginVertical = 2.dp,
    mainPaddingHorizontal = 30.dp,
    mainLogoSize = 50.dp,
    mainTopBannerHeight = 90.dp,
    proxyLayoutPadding = 3.dp,
    proxyContentPadding = 15.dp,
    proxyContentPaddingGrid3 = 12.dp,
    proxyTextMargin = 10.dp,
    proxyTextMarginGrid3 = 5.dp,
    proxyTextSize = 12.sp,
    proxyTextSizeGrid3 = 11.sp,
    proxyCardRadius = 5.dp,
    proxyCardOffset = 0.dp,
    propertiesElementMarginVertical = 2.5.dp,
    aboutIconSize = 50.dp,
    aboutTextMargin = 15.dp,
    logcatPaddingVertical = 12.dp,
    logcatPaddingHorizontal = 12.dp,
  )

private val LocalClashColors = staticCompositionLocalOf { LightClashColorTokens }
private val LocalClashDimens = staticCompositionLocalOf { DefaultClashDimens }
private val LocalClashTypography = staticCompositionLocalOf { DefaultClashTextStyles }

@Composable
fun ClashDesignTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  val clashColors = if (darkTheme) DarkClashColorTokens else LightClashColorTokens

  CompositionLocalProvider(
    LocalClashColors provides clashColors,
    LocalClashDimens provides DefaultClashDimens,
    LocalClashTypography provides DefaultClashTextStyles,
  ) {
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
  }
}
