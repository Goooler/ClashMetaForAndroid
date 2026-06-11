package com.github.kr328.clash.profile.util

import androidx.compose.runtime.Composable
import com.github.kr328.clash.common.Res as CommonRes
import com.github.kr328.clash.common.compatible
import com.github.kr328.clash.common.file
import com.github.kr328.clash.common.format_provider_type
import com.github.kr328.clash.common.http
import com.github.kr328.clash.common.inline
import com.github.kr328.clash.common.proxy
import com.github.kr328.clash.common.rule
import com.github.kr328.clash.core.model.Provider
import com.github.kr328.clash.core.model.Provider.Type.Proxy
import com.github.kr328.clash.core.model.Provider.Type.Rule
import com.github.kr328.clash.core.model.Provider.VehicleType.Compatible
import com.github.kr328.clash.core.model.Provider.VehicleType.File
import com.github.kr328.clash.core.model.Provider.VehicleType.HTTP
import com.github.kr328.clash.core.model.Provider.VehicleType.Inline
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun Provider.type(): String {
  val type =
    when (type) {
      Proxy -> stringResource(CommonRes.string.proxy)
      Rule -> stringResource(CommonRes.string.rule)
    }

  val vehicle =
    when (vehicleType) {
      HTTP -> stringResource(CommonRes.string.http)
      File -> stringResource(CommonRes.string.file)
      Inline -> stringResource(CommonRes.string.inline)
      Compatible -> stringResource(CommonRes.string.compatible)
    }

  return stringResource(CommonRes.string.format_provider_type, type, vehicle)
}
