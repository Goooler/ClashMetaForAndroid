package com.github.kr328.clash.profile.model

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.vector.ImageVector
import com.github.kr328.clash.common.Res as CommonRes
import com.github.kr328.clash.common.file
import com.github.kr328.clash.common.url
import com.github.kr328.clash.profile.Res
import com.github.kr328.clash.profile.import_from_file
import com.github.kr328.clash.profile.import_from_qr
import com.github.kr328.clash.profile.import_from_url
import com.github.kr328.clash.profile.qr
import com.github.kr328.clash.ui.icon.BaselineAttachFile
import com.github.kr328.clash.ui.icon.BaselineCloudDownload
import com.github.kr328.clash.ui.icon.BaselineQrCodeScanner
import com.github.kr328.clash.ui.icon.TabbyIcons
import com.github.kr328.clash.ui.util.getString

internal sealed class ProfileProvider {
  class File(private val context: Context) : ProfileProvider() {
    override val name: String
      get() = context.getString(CommonRes.string.file)

    override val summary: String
      get() = context.getString(Res.string.import_from_file)

    override val icon: ImageVector = TabbyIcons.BaselineAttachFile
  }

  class Url(private val context: Context) : ProfileProvider() {
    override val name: String
      get() = context.getString(CommonRes.string.url)

    override val summary: String
      get() = context.getString(Res.string.import_from_url)

    override val icon: ImageVector = TabbyIcons.BaselineCloudDownload
  }

  class QR(private val context: Context) : ProfileProvider() {
    override val name: String
      get() = context.getString(Res.string.qr)

    override val summary: String
      get() = context.getString(Res.string.import_from_qr)

    override val icon: ImageVector = TabbyIcons.BaselineQrCodeScanner
  }

  class External(
    override val name: String,
    override val summary: String,
    override val icon: Any?,
    val intent: Intent,
  ) : ProfileProvider()

  abstract val name: String
  abstract val summary: String
  abstract val icon: Any?
}
