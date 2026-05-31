package com.github.kr328.clash.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
val TabbyIcons.BaselineCircleCenter: ImageVector
  get() {
    if (_BaselineCircleCenter != null) {
      return _BaselineCircleCenter!!
    }
    _BaselineCircleCenter =
      ImageVector.Builder(
          name = "record-circle",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 16f,
          viewportHeight = 16f,
        )
        .apply {
          path(fill = SolidColor(Color.White)) {
            moveTo(8f, 14f)
            arcTo(6f, 6f, 0f, true, true, 8f, 2f)
            arcToRelative(6f, 6f, 0f, false, true, 0f, 12f)
            moveToRelative(0f, 2f)
            arcTo(8f, 8f, 0f, true, false, 8f, 0f)
            arcToRelative(8f, 8f, 0f, false, false, 0f, 16f)
          }
          path(fill = SolidColor(Color.White)) {
            moveTo(11f, 8f)
            arcToRelative(3f, 3f, 0f, true, true, -6f, 0f)
            arcToRelative(3f, 3f, 0f, false, true, 6f, 0f)
          }
        }
        .build()

    return _BaselineCircleCenter!!
  }

@Suppress("ObjectPropertyName") private var _BaselineCircleCenter: ImageVector? = null
