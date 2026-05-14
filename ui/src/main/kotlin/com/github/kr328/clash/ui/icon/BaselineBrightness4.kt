package com.github.kr328.clash.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val TabbyIcons.BaselineBrightness4: ImageVector by
  lazy(NONE) {
    ImageVector.Builder(
        name = "BaselineBrightness4",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
      )
      .apply {
        path(fill = SolidColor(Color.White)) {
          moveTo(20f, 8.69f)
          verticalLineTo(4f)
          horizontalLineToRelative(-4.69f)
          lineTo(12f, 0.69f)
          lineTo(8.69f, 4f)
          horizontalLineTo(4f)
          verticalLineToRelative(4.69f)
          lineTo(0.69f, 12f)
          lineTo(4f, 15.31f)
          verticalLineTo(20f)
          horizontalLineToRelative(4.69f)
          lineTo(12f, 23.31f)
          lineTo(15.31f, 20f)
          horizontalLineTo(20f)
          verticalLineToRelative(-4.69f)
          lineTo(23.31f, 12f)
          lineTo(20f, 8.69f)
          close()
          moveTo(12f, 18f)
          curveToRelative(-0.89f, 0f, -1.74f, -0.2f, -2.5f, -0.55f)
          curveTo(11.56f, 16.5f, 13f, 14.42f, 13f, 12f)
          reflectiveCurveToRelative(-1.44f, -4.5f, -3.5f, -5.45f)
          curveTo(10.26f, 6.2f, 11.11f, 6f, 12f, 6f)
          curveToRelative(3.31f, 0f, 6f, 2.69f, 6f, 6f)
          reflectiveCurveToRelative(-2.69f, 6f, -6f, 6f)
          close()
        }
      }
      .build()
  }
