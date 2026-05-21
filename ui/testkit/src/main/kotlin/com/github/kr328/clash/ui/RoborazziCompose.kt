@file:OptIn(ExperimentalRoborazziApi::class)

package com.github.kr328.clash.ui

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziComposeOptions
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.uiMode

fun captureRoboImages(content: @Composable () -> Unit) {
  captureRoboImage(
    roborazziComposeOptions = RoborazziComposeOptions { uiMode(UI_MODE_NIGHT_NO) },
    content = content,
  )
  captureRoboImage(
    roborazziComposeOptions = RoborazziComposeOptions { uiMode(UI_MODE_NIGHT_YES) },
    content = content,
  )
}
