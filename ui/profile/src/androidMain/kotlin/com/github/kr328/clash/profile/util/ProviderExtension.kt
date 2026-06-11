package com.github.kr328.clash.profile.util

import android.content.Context
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
import com.github.kr328.clash.ui.util.getString

internal fun Provider.type(context: Context): String {
  val type =
    when (type) {
      Proxy -> context.getString(CommonRes.string.proxy)
      Rule -> context.getString(CommonRes.string.rule)
    }

  val vehicle =
    when (vehicleType) {
      HTTP -> context.getString(CommonRes.string.http)
      File -> context.getString(CommonRes.string.file)
      Inline -> context.getString(CommonRes.string.inline)
      Compatible -> context.getString(CommonRes.string.compatible)
    }

  return context.getString(CommonRes.string.format_provider_type, type, vehicle)
}
