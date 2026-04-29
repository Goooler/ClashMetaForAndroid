package com.github.kr328.clash.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
val MihomoIcons.BaselineReplay: ImageVector
  get() {
    if (_BaselineReplay != null) {
      return _BaselineReplay!!
    }
    _BaselineReplay =
      ImageVector.Builder(
          name = "BaselineReplay",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(fill = SolidColor(Color.White)) {
            moveTo(12f, 5f)
            verticalLineTo(1f)
            lineTo(7f, 6f)
            lineToRelative(5f, 5f)
            verticalLineTo(7f)
            curveToRelative(3.31f, 0f, 6f, 2.69f, 6f, 6f)
            reflectiveCurveToRelative(-2.69f, 6f, -6f, 6f)
            reflectiveCurveToRelative(-6f, -2.69f, -6f, -6f)
            horizontalLineTo(4f)
            curveToRelative(0f, 4.42f, 3.58f, 8f, 8f, 8f)
            reflectiveCurveToRelative(8f, -3.58f, 8f, -8f)
            reflectiveCurveToRelative(-3.58f, -8f, -8f, -8f)
            close()
          }
        }
        .build()

    return _BaselineReplay!!
  }

@Suppress("ObjectPropertyName") private var _BaselineReplay: ImageVector? = null
