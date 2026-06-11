package com.github.kr328.clash.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
val TabbyIcons.OutlineLabel: ImageVector
  get() {
    if (_OutlineLabel != null) {
      return _OutlineLabel!!
    }
    _OutlineLabel =
      ImageVector.Builder(
          name = "OutlineLabel",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(fill = SolidColor(Color.White)) {
            moveTo(17.63f, 5.84f)
            curveTo(17.27f, 5.33f, 16.67f, 5f, 16f, 5f)
            lineTo(5f, 5.01f)
            curveTo(3.9f, 5.01f, 3f, 5.9f, 3f, 7f)
            verticalLineToRelative(10f)
            curveToRelative(0f, 1.1f, 0.9f, 1.99f, 2f, 1.99f)
            lineTo(16f, 19f)
            curveToRelative(0.67f, 0f, 1.27f, -0.33f, 1.63f, -0.84f)
            lineTo(22f, 12f)
            lineToRelative(-4.37f, -6.16f)
            close()
            moveTo(16f, 17f)
            horizontalLineTo(5f)
            verticalLineTo(7f)
            horizontalLineToRelative(11f)
            lineToRelative(3.55f, 5f)
            lineTo(16f, 17f)
            close()
          }
        }
        .build()

    return _OutlineLabel!!
  }

@Suppress("ObjectPropertyName") private var _OutlineLabel: ImageVector? = null
