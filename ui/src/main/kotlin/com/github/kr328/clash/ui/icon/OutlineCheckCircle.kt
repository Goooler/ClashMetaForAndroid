package com.github.kr328.clash.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val TabbyIcons.OutlineCheckCircle: ImageVector by
  lazy(NONE) {
    ImageVector.Builder(
        name = "OutlineCheckCircle",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
      )
      .apply {
        path(fill = SolidColor(Color.White)) {
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
          moveTo(16.59f, 7.58f)
          lineTo(10f, 14.17f)
          lineToRelative(-2.59f, -2.58f)
          lineTo(6f, 13f)
          lineToRelative(4f, 4f)
          lineToRelative(8f, -8f)
          close()
        }
      }
      .build()
  }
