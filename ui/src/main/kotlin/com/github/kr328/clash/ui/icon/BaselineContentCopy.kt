package com.github.kr328.clash.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
val TabbyIcons.BaselineContentCopy: ImageVector
  get() {
    if (_BaselineContentCopy != null) {
      return _BaselineContentCopy!!
    }
    _BaselineContentCopy =
      ImageVector.Builder(
          name = "BaselineContentCopy",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(fill = SolidColor(Color.White)) {
            moveTo(16f, 1f)
            lineTo(4f, 1f)
            curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
            verticalLineToRelative(14f)
            horizontalLineToRelative(2f)
            lineTo(4f, 3f)
            horizontalLineToRelative(12f)
            lineTo(16f, 1f)
            close()
            moveTo(19f, 5f)
            lineTo(8f, 5f)
            curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
            verticalLineToRelative(14f)
            curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
            horizontalLineToRelative(11f)
            curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
            lineTo(21f, 7f)
            curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
            close()
            moveTo(19f, 21f)
            lineTo(8f, 21f)
            lineTo(8f, 7f)
            horizontalLineToRelative(11f)
            verticalLineToRelative(14f)
            close()
          }
        }
        .build()

    return _BaselineContentCopy!!
  }

@Suppress("ObjectPropertyName") private var _BaselineContentCopy: ImageVector? = null
