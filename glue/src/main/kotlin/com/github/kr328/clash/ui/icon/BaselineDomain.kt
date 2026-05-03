package com.github.kr328.clash.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
val MihomoIcons.BaselineDomain: ImageVector
  get() {
    if (_BaselineDomain != null) {
      return _BaselineDomain!!
    }
    _BaselineDomain =
      ImageVector.Builder(
          name = "BaselineDomain",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(fill = SolidColor(Color.White)) {
            moveTo(12f, 7f)
            verticalLineTo(3f)
            horizontalLineTo(2f)
            verticalLineToRelative(18f)
            horizontalLineToRelative(20f)
            verticalLineTo(7f)
            horizontalLineTo(12f)
            close()
            moveTo(6f, 19f)
            horizontalLineTo(4f)
            verticalLineToRelative(-2f)
            horizontalLineToRelative(2f)
            verticalLineTo(19f)
            close()
            moveTo(6f, 15f)
            horizontalLineTo(4f)
            verticalLineToRelative(-2f)
            horizontalLineToRelative(2f)
            verticalLineTo(15f)
            close()
            moveTo(6f, 11f)
            horizontalLineTo(4f)
            verticalLineTo(9f)
            horizontalLineToRelative(2f)
            verticalLineTo(11f)
            close()
            moveTo(6f, 7f)
            horizontalLineTo(4f)
            verticalLineTo(5f)
            horizontalLineToRelative(2f)
            verticalLineTo(7f)
            close()
            moveTo(10f, 19f)
            horizontalLineTo(8f)
            verticalLineToRelative(-2f)
            horizontalLineToRelative(2f)
            verticalLineTo(19f)
            close()
            moveTo(10f, 15f)
            horizontalLineTo(8f)
            verticalLineToRelative(-2f)
            horizontalLineToRelative(2f)
            verticalLineTo(15f)
            close()
            moveTo(10f, 11f)
            horizontalLineTo(8f)
            verticalLineTo(9f)
            horizontalLineToRelative(2f)
            verticalLineTo(11f)
            close()
            moveTo(10f, 7f)
            horizontalLineTo(8f)
            verticalLineTo(5f)
            horizontalLineToRelative(2f)
            verticalLineTo(7f)
            close()
            moveTo(20f, 19f)
            horizontalLineToRelative(-8f)
            verticalLineToRelative(-2f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(-2f)
            horizontalLineToRelative(-2f)
            verticalLineToRelative(-2f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(-2f)
            horizontalLineToRelative(-2f)
            verticalLineTo(9f)
            horizontalLineToRelative(8f)
            verticalLineTo(19f)
            close()
            moveTo(18f, 11f)
            horizontalLineToRelative(-2f)
            verticalLineToRelative(2f)
            horizontalLineToRelative(2f)
            verticalLineTo(11f)
            close()
            moveTo(18f, 15f)
            horizontalLineToRelative(-2f)
            verticalLineToRelative(2f)
            horizontalLineToRelative(2f)
            verticalLineTo(15f)
            close()
          }
        }
        .build()

    return _BaselineDomain!!
  }

@Suppress("ObjectPropertyName") private var _BaselineDomain: ImageVector? = null
