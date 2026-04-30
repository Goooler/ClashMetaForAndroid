package com.github.kr328.clash.main.ui

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.kr328.clash.R
import com.github.kr328.clash.main.vm.MainViewModel
import com.github.kr328.clash.ui.component.MihomoScaffold
import com.github.kr328.clash.ui.icon.BaselineApps
import com.github.kr328.clash.ui.icon.BaselineAssignment
import com.github.kr328.clash.ui.icon.BaselineHelpCenter
import com.github.kr328.clash.ui.icon.BaselineInfo
import com.github.kr328.clash.ui.icon.BaselineSettings
import com.github.kr328.clash.ui.icon.BaselineSwapVerticalCircle
import com.github.kr328.clash.ui.icon.BaselineViewList
import com.github.kr328.clash.ui.icon.Clash
import com.github.kr328.clash.ui.icon.MihomoIcons
import com.github.kr328.clash.ui.icon.OutlineCheckCircle
import com.github.kr328.clash.ui.icon.OutlineNotInterested
import com.github.kr328.clash.ui.theme.MihomoDarkSurface
import com.github.kr328.clash.ui.theme.MihomoLightStopped
import com.github.kr328.clash.ui.theme.MihomoOnPrimary
import com.github.kr328.clash.ui.theme.MihomoThemeWrapper
import com.github.kr328.clash.ui.theme.PreviewMihomo
import com.github.kr328.clash.ui.theme.mihomoDimens

@Composable
fun MainScreen(
  modifier: Modifier = Modifier,
  viewModel: MainViewModel = viewModel(),
  onOpenProxy: () -> Unit,
  onOpenProfiles: () -> Unit,
  onOpenProviders: () -> Unit,
  onOpenLogs: () -> Unit,
  onOpenSettings: () -> Unit,
  onOpenHelp: () -> Unit,
) {
  val lifecycleOwner = LocalLifecycleOwner.current
  val clashRunning by viewModel.clashRunning.collectAsStateWithLifecycle()
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val eventState by viewModel.eventState.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }

  val noProfileText = stringResource(R.string.no_profile_selected)
  val profilesActionText = stringResource(R.string.profiles)

  DisposableEffect(lifecycleOwner, viewModel) {
    lifecycleOwner.lifecycle.addObserver(viewModel)
    onDispose { lifecycleOwner.lifecycle.removeObserver(viewModel) }
  }

  val vpnLauncher =
    rememberLauncherForActivityResult(StartActivityForResult()) { result ->
      if (result.resultCode == Activity.RESULT_OK) {
        viewModel.onVpnPermissionGranted()
      }
    }

  LaunchedEffect(eventState) {
    when (val event = eventState) {
      MainViewModel.EventState.Idle -> Unit
      is MainViewModel.EventState.RequestVpnPermission -> vpnLauncher.launch(event.intent)
      MainViewModel.EventState.ShowNoProfileMessage -> {
        val result =
          snackbarHostState.showSnackbar(
            message = noProfileText,
            actionLabel = profilesActionText,
            duration = SnackbarDuration.Long,
          )

        if (result == SnackbarResult.ActionPerformed) onOpenProfiles()
      }
      is MainViewModel.EventState.ShowMessage -> {
        snackbarHostState.showSnackbar(message = event.message)
      }
    }
    viewModel.consumeEvent()
  }

  MainContent(
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    clashRunning = clashRunning,
    forwarded = uiState.forwarded,
    mode = uiState.mode,
    profileName = uiState.profileName,
    hasProviders = uiState.hasProviders,
    aboutVersionName = uiState.aboutVersionName,
    onDismissAbout = viewModel::dismissAbout,
    onToggleStatus = viewModel::toggleStatus,
    onOpenProxy = onOpenProxy,
    onOpenProfiles = onOpenProfiles,
    onOpenProviders = onOpenProviders,
    onOpenLogs = onOpenLogs,
    onOpenSettings = onOpenSettings,
    onOpenHelp = onOpenHelp,
    onOpenAbout = viewModel::showAbout,
  )
}

@Composable
private fun MainContent(
  modifier: Modifier = Modifier,
  snackbarHostState: SnackbarHostState,
  clashRunning: Boolean,
  forwarded: String?,
  mode: String?,
  profileName: String?,
  hasProviders: Boolean,
  aboutVersionName: String?,
  onDismissAbout: () -> Unit,
  onToggleStatus: () -> Unit,
  onOpenProxy: () -> Unit,
  onOpenProfiles: () -> Unit,
  onOpenProviders: () -> Unit,
  onOpenLogs: () -> Unit,
  onOpenSettings: () -> Unit,
  onOpenHelp: () -> Unit,
  onOpenAbout: () -> Unit,
) {
  val darkTheme = isSystemInDarkTheme()
  val dimens = mihomoDimens
  val stoppedColor = if (darkTheme) MihomoDarkSurface else MihomoLightStopped

  MihomoScaffold(
    title = "",
    modifier = modifier,
    topBar = {},
    snackbarHostState = snackbarHostState,
  ) { innerPadding ->
    Column(
      modifier =
        Modifier.fillMaxSize()
          .padding(innerPadding)
          .padding(horizontal = dimens.mainPaddingHorizontal)
          .verticalScroll(rememberScrollState())
    ) {
      Row(
        modifier = Modifier.fillMaxWidth().height(dimens.mainTopBannerHeight),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Image(
          imageVector = MihomoIcons.Clash,
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
        icon =
          if (clashRunning) MihomoIcons.OutlineCheckCircle else MihomoIcons.OutlineNotInterested,
        text = stringResource(if (clashRunning) R.string.running else R.string.stopped),
        subtext =
          if (clashRunning && forwarded != null)
            stringResource(R.string.format_traffic_forwarded, forwarded)
          else stringResource(R.string.tap_to_start),
        backgroundColor = if (clashRunning) MaterialTheme.colorScheme.primary else stoppedColor,
        contentColor = MihomoOnPrimary,
        onClick = onToggleStatus,
      )

      AnimatedVisibility(visible = clashRunning) {
        MainActionCard(
          modifier = Modifier.padding(vertical = dimens.mainCardMarginVertical),
          icon = MihomoIcons.BaselineApps,
          text = stringResource(R.string.proxy),
          subtext = mode,
          backgroundColor = MaterialTheme.colorScheme.surface,
          contentColor = MaterialTheme.colorScheme.onSurface,
          onClick = onOpenProxy,
        )
      }

      MainActionCard(
        modifier = Modifier.padding(vertical = dimens.mainCardMarginVertical),
        icon = MihomoIcons.BaselineViewList,
        text = stringResource(R.string.profile),
        subtext =
          if (profileName != null) stringResource(R.string.format_profile_activated, profileName)
          else stringResource(R.string.not_selected),
        backgroundColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        onClick = onOpenProfiles,
      )

      AnimatedVisibility(visible = clashRunning && hasProviders) {
        MainActionLabel(
          modifier = Modifier.padding(vertical = dimens.mainLabelMarginVertical),
          icon = MihomoIcons.BaselineSwapVerticalCircle,
          text = stringResource(R.string.providers),
          onClick = onOpenProviders,
        )
      }

      MainActionLabel(
        modifier = Modifier.padding(vertical = dimens.mainLabelMarginVertical),
        icon = MihomoIcons.BaselineAssignment,
        text = stringResource(R.string.logs),
        onClick = onOpenLogs,
      )
      MainActionLabel(
        modifier = Modifier.padding(vertical = dimens.mainLabelMarginVertical),
        icon = MihomoIcons.BaselineSettings,
        text = stringResource(R.string.settings),
        onClick = onOpenSettings,
      )
      MainActionLabel(
        modifier = Modifier.padding(vertical = dimens.mainLabelMarginVertical),
        icon = MihomoIcons.BaselineHelpCenter,
        text = stringResource(R.string.help),
        onClick = onOpenHelp,
      )
      MainActionLabel(
        modifier = Modifier.padding(vertical = dimens.mainLabelMarginVertical),
        icon = MihomoIcons.BaselineInfo,
        text = stringResource(R.string.about),
        onClick = onOpenAbout,
      )
    }

    aboutVersionName?.let { AboutDialog(versionName = it, onDismiss = onDismissAbout) }
  }
}

@Composable
private fun MainActionCard(
  icon: ImageVector,
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
        imageVector = icon,
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
  icon: ImageVector,
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
      imageVector = icon,
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
        imageVector = MihomoIcons.Clash,
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

@PreviewWrapper(MihomoThemeWrapper::class)
@PreviewMihomo
@Composable
private fun MainContentRunningPreview() {
  MainContent(
    snackbarHostState = SnackbarHostState(),
    clashRunning = true,
    forwarded = "1.23 GB",
    mode = "Rule",
    profileName = "My Profile",
    hasProviders = true,
    aboutVersionName = null,
    onDismissAbout = {},
    onToggleStatus = {},
    onOpenProxy = {},
    onOpenProfiles = {},
    onOpenProviders = {},
    onOpenLogs = {},
    onOpenSettings = {},
    onOpenHelp = {},
    onOpenAbout = {},
  )
}

@PreviewWrapper(MihomoThemeWrapper::class)
@PreviewMihomo
@Composable
private fun MainContentStoppedPreview() {
  MainContent(
    snackbarHostState = SnackbarHostState(),
    clashRunning = false,
    forwarded = null,
    mode = null,
    profileName = null,
    hasProviders = false,
    aboutVersionName = null,
    onDismissAbout = {},
    onToggleStatus = {},
    onOpenProxy = {},
    onOpenProfiles = {},
    onOpenProviders = {},
    onOpenLogs = {},
    onOpenSettings = {},
    onOpenHelp = {},
    onOpenAbout = {},
  )
}
