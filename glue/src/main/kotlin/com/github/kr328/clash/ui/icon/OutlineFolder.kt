package com.github.kr328.clash.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
val MihomoIcons.OutlineFolder: ImageVector
  get() {
    if (_OutlineFolder != null) {
      return _OutlineFolder!!
    }
    _OutlineFolder =
      ImageVector.Builder(
          name = "OutlineFolder",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(fill = SolidColor(Color.White)) {
            moveTo(9.17f, 6f)
            lineToRelative(2f, 2f)
            horizontalLineTo(20f)
            verticalLineToRelative(10f)
            horizontalLineTo(4f)
            verticalLineTo(6f)
            horizontalLineToRelative(5.17f)
            moveTo(10f, 4f)
            horizontalLineTo(4f)
            curveToRelative(-1.1f, 0f, -1.99f, 0.9f, -1.99f, 2f)
            lineTo(2f, 18f)
            curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
            horizontalLineToRelative(16f)
            curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
            verticalLineTo(8f)
            curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
            horizontalLineToRelative(-8f)
            lineToRelative(-2f, -2f)
            close()
          }
        }
        .build()

    return _OutlineFolder!!
  }

@Suppress("ObjectPropertyName") private var _OutlineFolder: ImageVector? = null
