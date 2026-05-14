package com.github.kr328.clash.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
val TabbyIcons.BaselineArrowUp: ImageVector
  get() {
    if (_BaselineArrowUp != null) {
      return _BaselineArrowUp!!
    }
    _BaselineArrowUp =
      ImageVector.Builder(
          name = "arrow-up",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 16f,
          viewportHeight = 16f,
        )
        .apply {
          path(fill = SolidColor(Color.White)) {
            moveTo(8f, 15f)
            arcToRelative(0.5f, 0.5f, 0f, false, false, 0.5f, -0.5f)
            verticalLineTo(2.707f)
            lineToRelative(3.146f, 3.147f)
            arcToRelative(0.5f, 0.5f, 0f, false, false, 0.708f, -0.708f)
            lineToRelative(-4f, -4f)
            arcToRelative(0.5f, 0.5f, 0f, false, false, -0.708f, 0f)
            lineToRelative(-4f, 4f)
            arcToRelative(0.5f, 0.5f, 0f, true, false, 0.708f, 0.708f)
            lineTo(7.5f, 2.707f)
            verticalLineTo(14.5f)
            arcToRelative(0.5f, 0.5f, 0f, false, false, 0.5f, 0.5f)
          }
        }
        .build()
    return _BaselineArrowUp!!
  }

@Suppress("ObjectPropertyName") private var _BaselineArrowUp: ImageVector? = null
