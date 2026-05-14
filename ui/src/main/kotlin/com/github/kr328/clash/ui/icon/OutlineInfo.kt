package com.github.kr328.clash.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val TabbyIcons.OutlineInfo: ImageVector by
  lazy(NONE) {
    ImageVector.Builder(
        name = "OutlineInfo",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
      )
      .apply {
        path(fill = SolidColor(Color.White)) {
          moveTo(11f, 7f)
          horizontalLineToRelative(2f)
          verticalLineToRelative(2f)
          horizontalLineToRelative(-2f)
          close()
          moveTo(11f, 11f)
          horizontalLineToRelative(2f)
          verticalLineToRelative(6f)
          horizontalLineToRelative(-2f)
          close()
          moveTo(12f, 2f)
          curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
          reflectiveCurveToRelative(4.48f, 10f, 10f, 10f)
          reflectiveCurveToRelative(10f, -4.48f, 10f, -10f)
          reflectiveCurveTo(17.52f, 2f, 12f, 2f)
          close()
          moveTo(12f, 20f)
          curveToRelative(-4.41f, 0f, -8f, -3.59f, -8f, -8f)
          reflectiveCurveToRelative(3.59f, -8f, 8f, -8f)
          reflectiveCurveToRelative(8f, 3.59f, 8f, 8f)
          reflectiveCurveToRelative(-3.59f, 8f, -8f, 8f)
          close()
        }
      }
      .build()
  }
