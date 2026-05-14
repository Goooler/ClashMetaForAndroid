package com.github.kr328.clash.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val TabbyIcons.OutlineInbox: ImageVector by
  lazy(NONE) {
    ImageVector.Builder(
        name = "OutlineInbox",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
      )
      .apply {
        path(fill = SolidColor(Color.White)) {
          moveTo(19f, 3f)
          lineTo(5f, 3f)
          curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
          verticalLineToRelative(14f)
          curveToRelative(0f, 1.1f, 0.89f, 2f, 2f, 2f)
          horizontalLineToRelative(14f)
          curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
          lineTo(21f, 5f)
          curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
          close()
          moveTo(19f, 19f)
          lineTo(5f, 19f)
          verticalLineToRelative(-3f)
          horizontalLineToRelative(3.56f)
          curveToRelative(0.69f, 1.19f, 1.97f, 2f, 3.45f, 2f)
          reflectiveCurveToRelative(2.75f, -0.81f, 3.45f, -2f)
          lineTo(19f, 16f)
          verticalLineToRelative(3f)
          close()
          moveTo(19f, 14f)
          horizontalLineToRelative(-4.99f)
          curveToRelative(0f, 1.1f, -0.9f, 2f, -2f, 2f)
          reflectiveCurveToRelative(-2f, -0.9f, -2f, -2f)
          lineTo(5f, 14f)
          lineTo(5f, 5f)
          horizontalLineToRelative(14f)
          verticalLineToRelative(9f)
          close()
        }
      }
      .build()
  }
