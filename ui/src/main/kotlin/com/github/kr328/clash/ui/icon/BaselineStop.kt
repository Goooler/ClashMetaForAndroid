package com.github.kr328.clash.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val TabbyIcons.BaselineStop: ImageVector by
  lazy(NONE) {
    ImageVector.Builder(
        name = "BaselineStop",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
      )
      .apply {
        path(fill = SolidColor(Color.White)) {
          moveTo(6f, 6f)
          horizontalLineToRelative(12f)
          verticalLineToRelative(12f)
          horizontalLineTo(6f)
          close()
        }
      }
      .build()
  }
