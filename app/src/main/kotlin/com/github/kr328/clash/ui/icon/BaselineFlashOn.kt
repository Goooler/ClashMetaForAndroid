package com.github.kr328.clash.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val MihomoIcons.BaselineFlashOn: ImageVector
  get() {
    if (_BaselineFlashOn != null) {
      return _BaselineFlashOn!!
    }
    _BaselineFlashOn =
      ImageVector.Builder(
          name = "BaselineFlashOn",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(fill = SolidColor(Color.White)) {
            moveTo(7f, 2f)
            verticalLineToRelative(11f)
            horizontalLineToRelative(3f)
            verticalLineToRelative(9f)
            lineToRelative(7f, -12f)
            horizontalLineToRelative(-4f)
            lineToRelative(4f, -8f)
            close()
          }
        }
        .build()

    return _BaselineFlashOn!!
  }

@Suppress("ObjectPropertyName") private var _BaselineFlashOn: ImageVector? = null
