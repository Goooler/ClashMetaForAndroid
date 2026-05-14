package com.github.kr328.clash.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val TabbyIcons.BaselineClearAll: ImageVector by
  lazy(NONE) {
    ImageVector.Builder(
        name = "BaselineClearAll",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
      )
      .apply {
        path(fill = SolidColor(Color.White)) {
          moveTo(5f, 13f)
          horizontalLineToRelative(14f)
          verticalLineToRelative(-2f)
          lineTo(5f, 11f)
          verticalLineToRelative(2f)
          close()
          moveTo(3f, 17f)
          horizontalLineToRelative(14f)
          verticalLineToRelative(-2f)
          lineTo(3f, 15f)
          verticalLineToRelative(2f)
          close()
          moveTo(7f, 7f)
          verticalLineToRelative(2f)
          horizontalLineToRelative(14f)
          lineTo(21f, 7f)
          lineTo(7f, 7f)
          close()
        }
      }
      .build()
  }
