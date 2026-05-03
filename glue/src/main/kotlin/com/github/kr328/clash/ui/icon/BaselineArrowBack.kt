package com.github.kr328.clash.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
val MihomoIcons.BaselineArrowBack: ImageVector
  get() {
    if (_BaselineArrowBack != null) {
      return _BaselineArrowBack!!
    }
    _BaselineArrowBack =
      ImageVector.Builder(
          name = "BaselineArrowBack",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(fill = SolidColor(Color.White)) {
            moveTo(20f, 11f)
            horizontalLineTo(7.83f)
            lineToRelative(5.59f, -5.59f)
            lineTo(12f, 4f)
            lineToRelative(-8f, 8f)
            lineToRelative(8f, 8f)
            lineToRelative(1.41f, -1.41f)
            lineTo(7.83f, 13f)
            horizontalLineTo(20f)
            verticalLineToRelative(-2f)
            close()
          }
        }
        .build()

    return _BaselineArrowBack!!
  }

@Suppress("ObjectPropertyName") private var _BaselineArrowBack: ImageVector? = null
