package com.github.kr328.clash.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val TabbyIcons.BaselineApps: ImageVector by
  lazy(NONE) {
    ImageVector.Builder(
        name = "BaselineApps",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
      )
      .apply {
        path(fill = SolidColor(Color.White)) {
          moveTo(4f, 8f)
          horizontalLineToRelative(4f)
          lineTo(8f, 4f)
          lineTo(4f, 4f)
          verticalLineToRelative(4f)
          close()
          moveTo(10f, 20f)
          horizontalLineToRelative(4f)
          verticalLineToRelative(-4f)
          horizontalLineToRelative(-4f)
          verticalLineToRelative(4f)
          close()
          moveTo(4f, 20f)
          horizontalLineToRelative(4f)
          verticalLineToRelative(-4f)
          lineTo(4f, 16f)
          verticalLineToRelative(4f)
          close()
          moveTo(4f, 14f)
          horizontalLineToRelative(4f)
          verticalLineToRelative(-4f)
          lineTo(4f, 10f)
          verticalLineToRelative(4f)
          close()
          moveTo(10f, 14f)
          horizontalLineToRelative(4f)
          verticalLineToRelative(-4f)
          horizontalLineToRelative(-4f)
          verticalLineToRelative(4f)
          close()
          moveTo(16f, 4f)
          verticalLineToRelative(4f)
          horizontalLineToRelative(4f)
          lineTo(20f, 4f)
          horizontalLineToRelative(-4f)
          close()
          moveTo(10f, 8f)
          horizontalLineToRelative(4f)
          lineTo(14f, 4f)
          horizontalLineToRelative(-4f)
          verticalLineToRelative(4f)
          close()
          moveTo(16f, 14f)
          horizontalLineToRelative(4f)
          verticalLineToRelative(-4f)
          horizontalLineToRelative(-4f)
          verticalLineToRelative(4f)
          close()
          moveTo(16f, 20f)
          horizontalLineToRelative(4f)
          verticalLineToRelative(-4f)
          horizontalLineToRelative(-4f)
          verticalLineToRelative(4f)
          close()
        }
      }
      .build()
  }
