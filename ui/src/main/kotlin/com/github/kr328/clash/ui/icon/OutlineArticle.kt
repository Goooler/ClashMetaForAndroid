package com.github.kr328.clash.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
val MihomoIcons.OutlineArticle: ImageVector
  get() {
    if (_OutlineArticle != null) {
      return _OutlineArticle!!
    }
    _OutlineArticle =
      ImageVector.Builder(
          name = "OutlineArticle",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(fill = SolidColor(Color.White)) {
            moveTo(19f, 5f)
            verticalLineToRelative(14f)
            horizontalLineTo(5f)
            verticalLineTo(5f)
            horizontalLineTo(19f)
            moveTo(19f, 3f)
            horizontalLineTo(5f)
            curveTo(3.9f, 3f, 3f, 3.9f, 3f, 5f)
            verticalLineToRelative(14f)
            curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
            horizontalLineToRelative(14f)
            curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
            verticalLineTo(5f)
            curveTo(21f, 3.9f, 20.1f, 3f, 19f, 3f)
            lineTo(19f, 3f)
            close()
          }
          path(fill = SolidColor(Color.White)) {
            moveTo(14f, 17f)
            horizontalLineTo(7f)
            verticalLineToRelative(-2f)
            horizontalLineToRelative(7f)
            verticalLineTo(17f)
            close()
            moveTo(17f, 13f)
            horizontalLineTo(7f)
            verticalLineToRelative(-2f)
            horizontalLineToRelative(10f)
            verticalLineTo(13f)
            close()
            moveTo(17f, 9f)
            horizontalLineTo(7f)
            verticalLineTo(7f)
            horizontalLineToRelative(10f)
            verticalLineTo(9f)
            close()
          }
        }
        .build()

    return _OutlineArticle!!
  }

@Suppress("ObjectPropertyName") private var _OutlineArticle: ImageVector? = null
