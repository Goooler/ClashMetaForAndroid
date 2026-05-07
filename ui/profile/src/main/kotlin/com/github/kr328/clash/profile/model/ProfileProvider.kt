package com.github.kr328.clash.profile.model

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.vector.ImageVector
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.profile.R
import com.github.kr328.clash.ui.icon.BaselineAttachFile
import com.github.kr328.clash.ui.icon.BaselineCloudDownload
import com.github.kr328.clash.ui.icon.BaselineQrCodeScanner
import com.github.kr328.clash.ui.icon.MihomoIcons

internal sealed class ProfileProvider {
  class File(private val context: Context) : ProfileProvider() {
    override val name: String
      get() = context.getString(CommonR.string.file)

    override val summary: String
      get() = context.getString(R.string.import_from_file)

    override val icon: ImageVector = MihomoIcons.BaselineAttachFile
  }

  class Url(private val context: Context) : ProfileProvider() {
    override val name: String
      get() = context.getString(CommonR.string.url)

    override val summary: String
      get() = context.getString(R.string.import_from_url)

    override val icon: ImageVector = MihomoIcons.BaselineCloudDownload
  }

  class QR(private val context: Context) : ProfileProvider() {
    override val name: String
      get() = context.getString(R.string.qr)

    override val summary: String
      get() = context.getString(R.string.import_from_qr)

    override val icon: ImageVector = MihomoIcons.BaselineQrCodeScanner
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
