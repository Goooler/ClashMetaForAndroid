package com.github.kr328.clash.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
val MihomoIcons.BaselineAssignment: ImageVector
  get() {
    if (_BaselineAssignment != null) {
      return _BaselineAssignment!!
    }
    _BaselineAssignment =
      ImageVector.Builder(
          name = "BaselineAssignment",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(fill = SolidColor(Color.White)) {
            moveTo(19f, 3f)
            horizontalLineToRelative(-4.18f)
            curveTo(14.4f, 1.84f, 13.3f, 1f, 12f, 1f)
            curveToRelative(-1.3f, 0f, -2.4f, 0.84f, -2.82f, 2f)
            lineTo(5f, 3f)
            curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
            verticalLineToRelative(14f)
            curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
            horizontalLineToRelative(14f)
            curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
            lineTo(21f, 5f)
            curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
            close()
            moveTo(12f, 3f)
            curveToRelative(0.55f, 0f, 1f, 0.45f, 1f, 1f)
            reflectiveCurveToRelative(-0.45f, 1f, -1f, 1f)
            reflectiveCurveToRelative(-1f, -0.45f, -1f, -1f)
            reflectiveCurveToRelative(0.45f, -1f, 1f, -1f)
            close()
            moveTo(14f, 17f)
            lineTo(7f, 17f)
            verticalLineToRelative(-2f)
            horizontalLineToRelative(7f)
            verticalLineToRelative(2f)
            close()
            moveTo(17f, 13f)
            lineTo(7f, 13f)
            verticalLineToRelative(-2f)
            horizontalLineToRelative(10f)
            verticalLineToRelative(2f)
            close()
            moveTo(17f, 9f)
            lineTo(7f, 9f)
            lineTo(7f, 7f)
            horizontalLineToRelative(10f)
            verticalLineToRelative(2f)
            close()
          }
        }
        .build()

    return _BaselineAssignment!!
  }

@Suppress("ObjectPropertyName") private var _BaselineAssignment: ImageVector? = null
