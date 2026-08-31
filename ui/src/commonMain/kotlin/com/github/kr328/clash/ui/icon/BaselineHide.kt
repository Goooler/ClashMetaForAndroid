package com.github.kr328.clash.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
val TabbyIcons.BaselineHide: ImageVector
  get() {
    if (_BaselineHide != null) {
      return _BaselineHide!!
    }
    _BaselineHide =
      ImageVector.Builder(
          name = "BaselineHide",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 1024f,
          viewportHeight = 1024f,
        )
        .apply {
          path(fill = SolidColor(Color.White)) {
            moveTo(825.9f, 134.2f)
            lineToRelative(51.7f, 51.7f)
            lineToRelative(-655.1f, 655.1f)
            lineToRelative(-51.7f, -51.7f)
            lineToRelative(655.1f, -655.1f)
            close()
            moveTo(804.4f, 325.8f)
            curveToRelative(41.3f, 39.5f, 81.3f, 87.9f, 120f, 145.3f)
            arcToRelative(
              73.1f,
              73.1f,
              0f,
              isMoreThanHalf = false,
              isPositiveArc = true,
              2.8f,
              77.4f,
            )
            lineToRelative(-2.8f, 4.4f)
            lineToRelative(-6.9f, 10.1f)
            curveTo(795.2f, 740.3f, 660f, 829f, 512f, 829f)
            curveToRelative(-58.4f, 0f, -114.9f, -13.8f, -169.3f, -41.4f)
            lineToRelative(55.1f, -55.1f)
            curveToRelative(37.4f, 15.7f, 75.5f, 23.4f, 114.2f, 23.4f)
            curveToRelative(120.9f, 0f, 235.5f, -75.1f, 345.1f, -234f)
            lineToRelative(6.7f, -9.8f)
            lineToRelative(-6.7f, -9.8f)
            curveToRelative(-34.3f, -49.7f, -69f, -91.2f, -104.4f, -124.7f)
            lineToRelative(51.7f, -51.7f)
            close()
            moveTo(512f, 195f)
            curveToRelative(51.4f, 0f, 101.3f, 10.7f, 149.7f, 32.1f)
            lineToRelative(-56.5f, 56.5f)
            arcTo(289.4f, 289.4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 512f, 268.2f)
            curveToRelative(-120.9f, 0f, -235.5f, 75.1f, -345.1f, 234f)
            lineTo(160.2f, 512f)
            lineToRelative(6.7f, 9.8f)
            curveToRelative(29.5f, 42.8f, 59.4f, 79.5f, 89.7f, 110.3f)
            lineToRelative(-51.7f, 51.7f)
            curveToRelative(-36.1f, -36.7f, -71.3f, -80.3f, -105.4f, -130.9f)
            arcToRelative(
              73.1f,
              73.1f,
              0f,
              isMoreThanHalf = false,
              isPositiveArc = true,
              -2.8f,
              -77.4f,
            )
            lineToRelative(2.8f, -4.4f)
            lineToRelative(6.9f, -10.1f)
            curveTo(228.8f, 283.7f, 364f, 195f, 512f, 195f)
            close()
            moveTo(664.8f, 465.4f)
            arcToRelative(
              161.7f,
              161.7f,
              0f,
              isMoreThanHalf = false,
              isPositiveArc = true,
              -205.8f,
              205.8f,
            )
            lineToRelative(65.1f, -65.1f)
            arcToRelative(
              88.6f,
              88.6f,
              0f,
              isMoreThanHalf = false,
              isPositiveArc = false,
              75.6f,
              -75.6f,
            )
            lineToRelative(65.1f, -65.1f)
            close()
            moveTo(512f, 356.7f)
            curveToRelative(6.4f, 0f, 12.8f, 0.4f, 19f, 1.1f)
            lineToRelative(-179.5f, 179.6f)
            arcTo(161.7f, 161.7f, 0f, isMoreThanHalf = false, isPositiveArc = true, 512f, 356.7f)
            close()
          }
        }
        .build()

    return _BaselineHide!!
  }

@Suppress("ObjectPropertyName") private var _BaselineHide: ImageVector? = null
