package com.github.kr328.clash.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
val MihomoIcons.OutlineUpdate: ImageVector
  get() {
    if (_OutlineUpdate != null) {
      return _OutlineUpdate!!
    }
    _OutlineUpdate =
      ImageVector.Builder(
          name = "OutlineUpdate",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(fill = SolidColor(Color.White)) {
            moveTo(11f, 8f)
            verticalLineToRelative(5f)
            lineToRelative(4.25f, 2.52f)
            lineToRelative(0.77f, -1.28f)
            lineToRelative(-3.52f, -2.09f)
            verticalLineTo(8f)
            horizontalLineTo(11f)
            close()
            moveTo(21f, 10f)
            verticalLineTo(3f)
            lineToRelative(-2.64f, 2.64f)
            curveTo(16.74f, 4.01f, 14.49f, 3f, 12f, 3f)
            curveToRelative(-4.97f, 0f, -9f, 4.03f, -9f, 9f)
            reflectiveCurveToRelative(4.03f, 9f, 9f, 9f)
            reflectiveCurveToRelative(9f, -4.03f, 9f, -9f)
            horizontalLineToRelative(-2f)
            curveToRelative(0f, 3.86f, -3.14f, 7f, -7f, 7f)
            reflectiveCurveToRelative(-7f, -3.14f, -7f, -7f)
            reflectiveCurveToRelative(3.14f, -7f, 7f, -7f)
            curveToRelative(1.93f, 0f, 3.68f, 0.79f, 4.95f, 2.05f)
            lineTo(14f, 10f)
            horizontalLineTo(21f)
            close()
          }
        }
        .build()

    return _OutlineUpdate!!
  }

@Suppress("ObjectPropertyName") private var _OutlineUpdate: ImageVector? = null
