package com.github.kr328.clash.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
val TabbyIcons.BaselineSwapVerticalCircle: ImageVector
  get() {
    if (_BaselineSwapVerticalCircle != null) {
      return _BaselineSwapVerticalCircle!!
    }
    _BaselineSwapVerticalCircle =
      ImageVector.Builder(
          name = "BaselineSwapVerticalCircle",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(fill = SolidColor(Color.White)) {
            moveTo(12f, 2f)
            curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
            reflectiveCurveToRelative(4.48f, 10f, 10f, 10f)
            reflectiveCurveToRelative(10f, -4.48f, 10f, -10f)
            reflectiveCurveTo(17.52f, 2f, 12f, 2f)
            close()
            moveTo(6.5f, 9f)
            lineTo(10f, 5.5f)
            lineTo(13.5f, 9f)
            lineTo(11f, 9f)
            verticalLineToRelative(4f)
            lineTo(9f, 13f)
            lineTo(9f, 9f)
            lineTo(6.5f, 9f)
            close()
            moveTo(17.5f, 15f)
            lineTo(14f, 18.5f)
            lineTo(10.5f, 15f)
            lineTo(13f, 15f)
            verticalLineToRelative(-4f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(4f)
            horizontalLineToRelative(2.5f)
            close()
          }
        }
        .build()

    return _BaselineSwapVerticalCircle!!
  }

@Suppress("ObjectPropertyName") private var _BaselineSwapVerticalCircle: ImageVector? = null
