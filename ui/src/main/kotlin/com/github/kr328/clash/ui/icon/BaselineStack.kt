package com.github.kr328.clash.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
val MihomoIcons.BaselineStack: ImageVector
  get() {
    if (_BaselineStack != null) {
      return _BaselineStack!!
    }
    _BaselineStack =
      ImageVector.Builder(
          name = "BaselineStack",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 1024f,
          viewportHeight = 1024f,
        )
        .apply {
          path(fill = SolidColor(Color.White)) {
            moveTo(1024f, 320f)
            lineTo(512f, 64f)
            lineTo(0f, 320f)
            lineToRelative(512f, 256f)
            lineTo(1024f, 320f)
            close()
            moveTo(512f, 149f)
            lineTo(854f, 320f)
            lineTo(512f, 491f)
            lineTo(170f, 320f)
            lineTo(512f, 149f)
            close()
            moveTo(921.4f, 460.7f)
            lineTo(1024f, 512f)
            lineTo(512f, 768f)
            lineTo(0f, 512f)
            lineTo(102.6f, 460.7f)
            lineTo(512f, 665.4f)
            close()
            moveTo(921.4f, 652.7f)
            lineTo(1024f, 704f)
            lineTo(512f, 960f)
            lineTo(0f, 704f)
            lineTo(102.6f, 652.7f)
            lineTo(512f, 857.4f)
            close()
          }
        }
        .build()

    return _BaselineStack!!
  }

@Suppress("ObjectPropertyName") private var _BaselineStack: ImageVector? = null
