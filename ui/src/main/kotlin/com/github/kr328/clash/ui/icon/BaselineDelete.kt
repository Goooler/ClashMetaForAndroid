package com.github.kr328.clash.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val TabbyIcons.BaselineDelete: ImageVector by
  lazy(NONE) {
    ImageVector.Builder(
        name = "BaselineDelete",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
      )
      .apply {
        path(fill = SolidColor(Color.White)) {
          moveTo(6f, 19f)
          curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
          horizontalLineToRelative(8f)
          curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
          verticalLineTo(7f)
          horizontalLineTo(6f)
          verticalLineToRelative(12f)
          close()
          moveTo(19f, 4f)
          horizontalLineToRelative(-3.5f)
          lineToRelative(-1f, -1f)
          horizontalLineToRelative(-5f)
          lineToRelative(-1f, 1f)
          horizontalLineTo(5f)
          verticalLineToRelative(2f)
          horizontalLineToRelative(14f)
          verticalLineTo(4f)
          close()
        }
      }
      .build()
  }
