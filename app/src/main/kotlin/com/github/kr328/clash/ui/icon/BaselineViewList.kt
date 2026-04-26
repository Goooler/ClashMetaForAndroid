package com.github.kr328.clash.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val MihomoIcons.BaselineViewList: ImageVector
  get() {
    if (_BaselineViewList != null) {
      return _BaselineViewList!!
    }
    _BaselineViewList =
      ImageVector.Builder(
          name = "BaselineViewList",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(fill = SolidColor(Color.White)) {
            moveTo(4f, 14f)
            horizontalLineToRelative(4f)
            verticalLineToRelative(-4f)
            lineTo(4f, 10f)
            verticalLineToRelative(4f)
            close()
            moveTo(4f, 19f)
            horizontalLineToRelative(4f)
            verticalLineToRelative(-4f)
            lineTo(4f, 15f)
            verticalLineToRelative(4f)
            close()
            moveTo(4f, 9f)
            horizontalLineToRelative(4f)
            lineTo(8f, 5f)
            lineTo(4f, 5f)
            verticalLineToRelative(4f)
            close()
            moveTo(9f, 14f)
            horizontalLineToRelative(12f)
            verticalLineToRelative(-4f)
            lineTo(9f, 10f)
            verticalLineToRelative(4f)
            close()
            moveTo(9f, 19f)
            horizontalLineToRelative(12f)
            verticalLineToRelative(-4f)
            lineTo(9f, 15f)
            verticalLineToRelative(4f)
            close()
            moveTo(9f, 5f)
            verticalLineToRelative(4f)
            horizontalLineToRelative(12f)
            lineTo(21f, 5f)
            lineTo(9f, 5f)
            close()
          }
        }
        .build()

    return _BaselineViewList!!
  }

@Suppress("ObjectPropertyName") private var _BaselineViewList: ImageVector? = null
