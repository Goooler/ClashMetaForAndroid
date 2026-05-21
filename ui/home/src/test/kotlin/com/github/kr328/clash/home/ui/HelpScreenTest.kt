package com.github.kr328.clash.home.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.kr328.clash.ui.captureRoboImages
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36], qualifiers = RobolectricDeviceQualifiers.Pixel6)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HelpScreenTest {
  @Test fun helpScreenPreview() = captureRoboImages { HelpScreenPreview() }
}
