package com.github.kr328.clash.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
val MihomoIcons.BaselineCloudDownload: ImageVector
  get() {
    if (_BaselineCloudDownload != null) {
      return _BaselineCloudDownload!!
    }
    _BaselineCloudDownload =
      ImageVector.Builder(
          name = "BaselineCloudDownload",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(fill = SolidColor(Color.White)) {
            moveTo(19.35f, 10.04f)
            curveTo(18.67f, 6.59f, 15.64f, 4f, 12f, 4f)
            curveTo(9.11f, 4f, 6.6f, 5.64f, 5.35f, 8.04f)
            curveTo(2.34f, 8.36f, 0f, 10.91f, 0f, 14f)
            curveToRelative(0f, 3.31f, 2.69f, 6f, 6f, 6f)
            horizontalLineToRelative(13f)
            curveToRelative(2.76f, 0f, 5f, -2.24f, 5f, -5f)
            curveToRelative(0f, -2.64f, -2.05f, -4.78f, -4.65f, -4.96f)
            close()
            moveTo(17f, 13f)
            lineToRelative(-5f, 5f)
            lineToRelative(-5f, -5f)
            horizontalLineToRelative(3f)
            verticalLineTo(9f)
            horizontalLineToRelative(4f)
            verticalLineToRelative(4f)
            horizontalLineToRelative(3f)
            close()
          }
        }
        .build()

    return _BaselineCloudDownload!!
  }

@Suppress("ObjectPropertyName") private var _BaselineCloudDownload: ImageVector? = null
