package com.github.kr328.clash.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
val MihomoIcons.BaselinePublish: ImageVector
  get() {
    if (_BaselinePublish != null) {
      return _BaselinePublish!!
    }
    _BaselinePublish =
      ImageVector.Builder(
          name = "BaselinePublish",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(fill = SolidColor(Color.White)) {
            moveTo(5f, 4f)
            verticalLineToRelative(2f)
            horizontalLineToRelative(14f)
            lineTo(19f, 4f)
            lineTo(5f, 4f)
            close()
            moveTo(5f, 14f)
            horizontalLineToRelative(4f)
            verticalLineToRelative(6f)
            horizontalLineToRelative(6f)
            verticalLineToRelative(-6f)
            horizontalLineToRelative(4f)
            lineToRelative(-7f, -7f)
            lineToRelative(-7f, 7f)
            close()
          }
        }
        .build()

    return _BaselinePublish!!
  }

@Suppress("ObjectPropertyName") private var _BaselinePublish: ImageVector? = null
