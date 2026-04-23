package com.github.kr328.clash.main.ui

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.kr328.clash.R
import com.github.kr328.clash.core.model.TunnelState
import com.github.kr328.clash.core.util.trafficTotal
import com.github.kr328.clash.ui.Design
import com.github.kr328.clash.ui.theme.MihomoDarkSurface
import com.github.kr328.clash.ui.theme.MihomoLightStopped
import com.github.kr328.clash.ui.theme.MihomoOnPrimary
import com.github.kr328.clash.ui.theme.MihomoTheme
import com.github.kr328.clash.ui.theme.PreviewMihomo
import com.github.kr328.clash.ui.theme.mihomoDimens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainDesign(context: Context) : Design<MainDesign.Request>(context) {
  sealed interface Request {
    data object ToggleStatus : Request

    data object OpenProxy : Request

    data object OpenProfiles : Request

    data object OpenProviders : Request

    data object OpenLogs : Request

    data object OpenSettings : Request

    data object OpenHelp : Request

    data object OpenAbout : Request
  }

  private var clashRunning by mutableStateOf(false)
  private var forwarded by mutableStateOf<String?>(null)
  private var mode by mutableStateOf<String?>(null)
  private var profileName by mutableStateOf<String?>(null)
  private var hasProviders by mutableStateOf(false)
  private var aboutVersionName by mutableStateOf<String?>(null)

  @Composable
  override fun Content() = MihomoTheme {
    MainScreen(
      clashRunning = clashRunning,
      forwarded = forwarded,
      mode = mode,
      profileName = profileName,
      hasProviders = hasProviders,
      aboutVersionName = aboutVersionName,
      onDismissAbout = { aboutVersionName = null },
      onRequest = { requests.trySend(it) },
    )
  }

  suspend fun setProfileName(name: String?) = withContext(Dispatchers.Main) { profileName = name }

  suspend fun setClashRunning(running: Boolean) =
    withContext(Dispatchers.Main) { clashRunning = running }

  suspend fun setForwarded(value: Long) =
    withContext(Dispatchers.Main) { forwarded = value.trafficTotal() }

  suspend fun setMode(mode: TunnelState.Mode) =
    withContext(Dispatchers.Main) {
      this@MainDesign.mode =
        when (mode) {
          TunnelState.Mode.Direct -> context.getString(R.string.direct_mode)
          TunnelState.Mode.Global -> context.getString(R.string.global_mode)
          TunnelState.Mode.Rule -> context.getString(R.string.rule_mode)
        }
    }

  suspend fun setHasProviders(has: Boolean) = withContext(Dispatchers.Main) { hasProviders = has }

  suspend fun showAbout(versionName: String) =
    withContext(Dispatchers.Main) { aboutVersionName = versionName }
}

@Composable
private fun MainScreen(
  clashRunning: Boolean,
  forwarded: String?,
  mode: String?,
  profileName: String?,
  hasProviders: Boolean,
  aboutVersionName: String?,
  onDismissAbout: () -> Unit,
  onRequest: (MainDesign.Request) -> Unit,
) {
  Surface(modifier = Modifier.fillMaxSize()) {
    val darkTheme = isSystemInDarkTheme()
    val dimens = mihomoDimens
    val stoppedColor = if (darkTheme) MihomoDarkSurface else MihomoLightStopped

    Column(
      modifier =
        Modifier.fillMaxSize()
          .windowInsetsPadding(WindowInsets.safeDrawing)
          .padding(horizontal = dimens.mainPaddingHorizontal)
          .verticalScroll(rememberScrollState())
    ) {
      Row(
        modifier = Modifier.fillMaxWidth().height(dimens.mainTopBannerHeight),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Image(
          painter = painterResource(R.drawable.ic_clash),
          contentDescription = null,
          modifier = Modifier.size(dimens.mainLogoSize),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = stringResource(R.string.launch_name_meta),
          style = MaterialTheme.typography.titleLarge,
        )
      }

      MainActionCard(
        modifier = Modifier.padding(vertical = dimens.mainCardMarginVertical),
        iconRes =
          if (clashRunning) R.drawable.ic_outline_check_circle
          else R.drawable.ic_outline_not_interested,
        text = stringResource(if (clashRunning) R.string.running else R.string.stopped),
        subtext =
          if (clashRunning && forwarded != null)
            stringResource(R.string.format_traffic_forwarded, forwarded)
          else stringResource(R.string.tap_to_start),
        backgroundColor = if (clashRunning) MaterialTheme.colorScheme.primary else stoppedColor,
        contentColor = MihomoOnPrimary,
        onClick = { onRequest(MainDesign.Request.ToggleStatus) },
      )

      AnimatedVisibility(visible = clashRunning) {
        MainActionCard(
          modifier = Modifier.padding(vertical = dimens.mainCardMarginVertical),
          iconRes = R.drawable.ic_baseline_apps,
          text = stringResource(R.string.proxy),
          subtext = mode,
          backgroundColor = MaterialTheme.colorScheme.surface,
          contentColor = MaterialTheme.colorScheme.onSurface,
          onClick = { onRequest(MainDesign.Request.OpenProxy) },
        )
      }

      MainActionCard(
        modifier = Modifier.padding(vertical = dimens.mainCardMarginVertical),
        iconRes = R.drawable.ic_baseline_view_list,
        text = stringResource(R.string.profile),
        subtext =
          if (profileName != null) stringResource(R.string.format_profile_activated, profileName)
          else stringResource(R.string.not_selected),
        backgroundColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        onClick = { onRequest(MainDesign.Request.OpenProfiles) },
      )

      AnimatedVisibility(visible = clashRunning && hasProviders) {
        MainActionLabel(
          modifier = Modifier.padding(vertical = dimens.mainLabelMarginVertical),
          iconRes = R.drawable.ic_baseline_swap_vertical_circle,
          text = stringResource(R.string.providers),
          onClick = { onRequest(MainDesign.Request.OpenProviders) },
        )
      }

      MainActionLabel(
        modifier = Modifier.padding(vertical = dimens.mainLabelMarginVertical),
        iconRes = R.drawable.ic_baseline_assignment,
        text = stringResource(R.string.logs),
        onClick = { onRequest(MainDesign.Request.OpenLogs) },
      )
      MainActionLabel(
        modifier = Modifier.padding(vertical = dimens.mainLabelMarginVertical),
        iconRes = R.drawable.ic_baseline_settings,
        text = stringResource(R.string.settings),
        onClick = { onRequest(MainDesign.Request.OpenSettings) },
      )
      MainActionLabel(
        modifier = Modifier.padding(vertical = dimens.mainLabelMarginVertical),
        iconRes = R.drawable.ic_baseline_help_center,
        text = stringResource(R.string.help),
        onClick = { onRequest(MainDesign.Request.OpenHelp) },
      )
      MainActionLabel(
        modifier = Modifier.padding(vertical = dimens.mainLabelMarginVertical),
        iconRes = R.drawable.ic_baseline_info,
        text = stringResource(R.string.about),
        onClick = { onRequest(MainDesign.Request.OpenAbout) },
      )
    }

    aboutVersionName?.let { AboutDialog(versionName = it, onDismiss = onDismissAbout) }
  }
}

@Composable
private fun MainActionCard(
  iconRes: Int,
  text: String,
  subtext: String?,
  backgroundColor: Color,
  contentColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val dimens = mihomoDimens
  Card(
    modifier = modifier.fillMaxWidth().heightIn(min = dimens.largeActionCardMinHeight),
    onClick = onClick,
    colors = CardDefaults.cardColors(containerColor = backgroundColor, contentColor = contentColor),
    elevation = CardDefaults.cardElevation(defaultElevation = dimens.largeActionCardElevation),
  ) {
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .padding(
            horizontal = dimens.largeItemTrailingMarginHorizontal,
            vertical = dimens.largeItemPaddingVertical,
          ),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        painter = painterResource(iconRes),
        contentDescription = null,
        modifier = Modifier.size(dimens.largeItemHeaderComponentSize),
        tint = contentColor,
      )
      Spacer(modifier = Modifier.width(dimens.largeItemTrailingMarginHorizontal))
      Column {
        Text(text = text, style = MaterialTheme.typography.bodyLarge, color = contentColor)
        if (subtext != null) {
          Spacer(modifier = Modifier.height(dimens.largeItemTextMargin))
          Text(text = subtext, style = MaterialTheme.typography.bodyMedium, color = contentColor)
        }
      }
    }
  }
}

@Composable
private fun MainActionLabel(
  iconRes: Int,
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val dimens = mihomoDimens
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .heightIn(min = 60.dp)
        .clickable(onClick = onClick)
        .padding(vertical = dimens.largeItemPaddingVertical),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Spacer(modifier = Modifier.width(dimens.largeItemTrailingMarginHorizontal))
    Icon(
      painter = painterResource(iconRes),
      contentDescription = null,
      modifier = Modifier.size(dimens.largeItemHeaderComponentSize),
    )
    Spacer(modifier = Modifier.width(dimens.largeItemTrailingMarginHorizontal))
    Text(text = text, style = MaterialTheme.typography.bodyLarge)
  }
}

@Composable
private fun AboutDialog(versionName: String, onDismiss: () -> Unit) {
  val dimens = mihomoDimens
  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(onClick = onDismiss) { Text(text = stringResource(android.R.string.ok)) }
    },
    icon = {
      Image(
        painter = painterResource(R.drawable.ic_clash),
        contentDescription = null,
        modifier = Modifier.size(dimens.aboutIconSize),
      )
    },
    title = {
      Text(
        text = stringResource(R.string.launch_name_meta),
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
      )
    },
    text = {
      Text(text = versionName, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    },
  )
}

@PreviewMihomo
@Composable
private fun MainScreenRunningPreview() = MihomoTheme {
  MainScreen(
    clashRunning = true,
    forwarded = "1.23 GB",
    mode = "Rule",
    profileName = "My Profile",
    hasProviders = true,
    aboutVersionName = null,
    onDismissAbout = {},
    onRequest = {},
  )
}

@PreviewMihomo
@Composable
private fun MainScreenStoppedPreview() = MihomoTheme {
  MainScreen(
    clashRunning = false,
    forwarded = null,
    mode = null,
    profileName = null,
    hasProviders = false,
    aboutVersionName = null,
    onDismissAbout = {},
    onRequest = {},
  )
}
