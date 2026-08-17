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
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(fill = SolidColor(Color.White)) {
            moveTo(12f, 19f)
            arcTo(7f, 7f, 0f, true, true, 12f, 5f)
            arcToRelative(7f, 7f, 0f, false, true, 0f, 14f)
            moveToRelative(0f, 3f)
            arcTo(10f, 10f, 0f, true, false, 12f, 2f)
            arcToRelative(10f, 10f, 0f, false, false, 0f, 20f)
          }
          path(fill = SolidColor(Color.White)) {
            moveTo(16f, 12f)
            arcToRelative(4f, 4f, 0f, true, true, -8f, 0f)
            arcToRelative(4f, 4f, 0f, false, true, 8f, 0f)
          }
        }
        .build()

    return _BaselineCircleCenter!!
  }

@Suppress("ObjectPropertyName") private var _BaselineCircleCenter: ImageVector? = null
