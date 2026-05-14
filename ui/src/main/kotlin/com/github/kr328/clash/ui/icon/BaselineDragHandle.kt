package com.github.kr328.clash.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val TabbyIcons.BaselineDragHandle: ImageVector by
  lazy(NONE) {
    ImageVector.Builder(
        name = "BaselineDragHandle",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
      )
      .apply {
        path(fill = SolidColor(Color.White)) {
          moveTo(20f, 9f)
          horizontalLineTo(4f)
          verticalLineToRelative(2f)
          horizontalLineToRelative(16f)
          verticalLineTo(9f)
          close()
          moveTo(4f, 15f)
          horizontalLineToRelative(16f)
          verticalLineToRelative(-2f)
          horizontalLineTo(4f)
          verticalLineToRelative(2f)
          close()
        }
      }
      .build()
  }
