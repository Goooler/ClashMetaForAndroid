package com.github.kr328.clash.profile.model

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
import org.jetbrains.compose.resources.StringResource

internal sealed interface ProfileProvider {
  data object File : ProfileProvider {
    override val name: StringResource = CommonRes.string.file
    override val summary: StringResource = Res.string.import_from_file
    override val icon: ImageVector = TabbyIcons.BaselineAttachFile
  }

  data object Url : ProfileProvider {
    override val name: StringResource = CommonRes.string.url
    override val summary: StringResource = Res.string.import_from_url
    override val icon: ImageVector = TabbyIcons.BaselineCloudDownload
  }

  data object QR : ProfileProvider {
    override val name: StringResource = Res.string.qr
    override val summary: StringResource = Res.string.import_from_qr
    override val icon: ImageVector = TabbyIcons.BaselineQrCodeScanner
  }

  class External(
    override val name: String,
    override val summary: String,
    override val icon: Any?,
    val intent: Intent,
  ) : ProfileProvider

  val name: Any
  val summary: Any
  val icon: Any?
}
