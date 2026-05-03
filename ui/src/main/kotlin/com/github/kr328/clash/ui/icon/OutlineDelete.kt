package com.github.kr328.clash.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
val MihomoIcons.OutlineDelete: ImageVector
  get() {
    if (_OutlineDelete != null) {
      return _OutlineDelete!!
    }
    _OutlineDelete =
      ImageVector.Builder(
          name = "OutlineDelete",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(fill = SolidColor(Color.White)) {
            moveTo(16f, 9f)
            verticalLineToRelative(10f)
            horizontalLineTo(8f)
            verticalLineTo(9f)
            horizontalLineToRelative(8f)
            moveToRelative(-1.5f, -6f)
            horizontalLineToRelative(-5f)
            lineToRelative(-1f, 1f)
            horizontalLineTo(5f)
            verticalLineToRelative(2f)
            horizontalLineToRelative(14f)
            verticalLineTo(4f)
            horizontalLineToRelative(-3.5f)
            lineToRelative(-1f, -1f)
            close()
            moveTo(18f, 7f)
            horizontalLineTo(6f)
            verticalLineToRelative(12f)
            curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
            horizontalLineToRelative(8f)
            curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
            verticalLineTo(7f)
            close()
          }
        }
        .build()

    return _OutlineDelete!!
  }

@Suppress("ObjectPropertyName") private var _OutlineDelete: ImageVector? = null
