package com.github.kr328.clash.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val DefaultFontFamily = FontFamily.Default

private val Title1TextStyle =
  TextStyle(
    fontFamily = DefaultFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 20.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.15.sp,
  )

private val Headline1TextStyle =
  TextStyle(
    fontFamily = DefaultFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 24.sp,
    lineHeight = 32.sp,
    letterSpacing = 0.sp,
  )

private val Body1TextStyle =
  TextStyle(
    fontFamily = DefaultFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.5.sp,
  )

private val Body2TextStyle =
  TextStyle(
    fontFamily = DefaultFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.25.sp,
  )

private val CaptionTextStyle =
  TextStyle(
    fontFamily = DefaultFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.4.sp,
  )

private val TooltipTextStyle =
  TextStyle(
    fontFamily = DefaultFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 10.sp,
    lineHeight = 14.sp,
    letterSpacing = 0.1.sp,
  )

val Typography =
  Typography(
    headlineLarge = Headline1TextStyle,
    titleLarge = Title1TextStyle,
    bodyLarge = Body1TextStyle,
    bodyMedium = Body2TextStyle,
    labelLarge = Body2TextStyle.copy(fontWeight = FontWeight.Medium),
    bodySmall = CaptionTextStyle,
    labelSmall = TooltipTextStyle,
  )

@Immutable
data class TabbyTextStyles(
  val toolbarTitle: TextStyle,
  val title: TextStyle,
  val body: TextStyle,
  val bodySecondary: TextStyle,
  val caption: TextStyle,
  val tooltip: TextStyle,
  val proxy: TextStyle,
  val proxyGrid3: TextStyle,
)

internal val DefaultTabbyTextStyles =
  TabbyTextStyles(
    toolbarTitle = Title1TextStyle,
    title = Title1TextStyle,
    body = Body1TextStyle,
    bodySecondary = Body2TextStyle,
    caption = CaptionTextStyle,
    tooltip = TooltipTextStyle,
    proxy = CaptionTextStyle.copy(fontSize = 12.sp, lineHeight = 16.sp),
    proxyGrid3 = CaptionTextStyle.copy(fontSize = 11.sp, lineHeight = 14.sp),
  )
