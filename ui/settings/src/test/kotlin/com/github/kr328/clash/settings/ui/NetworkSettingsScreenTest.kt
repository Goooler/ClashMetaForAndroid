package com.github.kr328.clash.settings.ui

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
class NetworkSettingsScreenTest {
  @Test fun networkSettingsScreenPreview() = captureRoboImages { NetworkSettingsScreenPreview() }

  @Test
  fun networkSettingsScreenRunningPreview() = captureRoboImages {
    NetworkSettingsScreenRunningPreview()
  }
}
