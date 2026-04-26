package com.github.kr328.clash.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val MihomoIcons.BaselineSwapVert: ImageVector
  get() {
    if (_BaselineSwapVert != null) {
      return _BaselineSwapVert!!
    }
    _BaselineSwapVert =
      ImageVector.Builder(
          name = "BaselineSwapVert",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(fill = SolidColor(Color.White)) {
            moveTo(16f, 17.01f)
            verticalLineTo(10f)
            horizontalLineToRelative(-2f)
            verticalLineToRelative(7.01f)
            horizontalLineToRelative(-3f)
            lineTo(15f, 21f)
            lineToRelative(4f, -3.99f)
            horizontalLineToRelative(-3f)
            close()
            moveTo(9f, 3f)
            lineTo(5f, 6.99f)
            horizontalLineToRelative(3f)
            verticalLineTo(14f)
            horizontalLineToRelative(2f)
            verticalLineTo(6.99f)
            horizontalLineToRelative(3f)
            lineTo(9f, 3f)
            close()
          }
        }
        .build()

    return _BaselineSwapVert!!
  }

@Suppress("ObjectPropertyName") private var _BaselineSwapVert: ImageVector? = null
