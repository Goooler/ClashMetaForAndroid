package com.github.kr328.clash.main.ui

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.kr328.clash.R
import com.github.kr328.clash.main.vm.MainViewModel
import com.github.kr328.clash.ui.theme.MihomoDarkSurface
import com.github.kr328.clash.ui.theme.MihomoLightStopped
import com.github.kr328.clash.ui.theme.MihomoOnPrimary
import com.github.kr328.clash.ui.theme.MihomoTheme
import com.github.kr328.clash.ui.theme.PreviewMihomo
import com.github.kr328.clash.ui.theme.mihomoDimens
import kotlinx.coroutines.launch

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
  val scope = rememberCoroutineScope()

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
        scope.launch {
          val result =
            snackbarHostState.showSnackbar(
              message = noProfileText,
              actionLabel = profilesActionText,
              duration = SnackbarDuration.Long,
            )
          if (result == SnackbarResult.ActionPerformed) onOpenProfiles()
        }
      }
      is MainViewModel.EventState.ShowMessage -> {
        scope.launch {
          snackbarHostState.showSnackbar(message = event.message, duration = SnackbarDuration.Long)
        }
      }
    }
    viewModel.consumeEvent()
  }

  Box(modifier = modifier.fillMaxSize()) {
    MainContent(
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
    SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
  }
}

@Composable
private fun MainContent(
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
        onClick = onToggleStatus,
      )

      AnimatedVisibility(visible = clashRunning) {
        MainActionCard(
          modifier = Modifier.padding(vertical = dimens.mainCardMarginVertical),
          iconRes = R.drawable.ic_baseline_apps,
          text = stringResource(R.string.proxy),
          subtext = mode,
          backgroundColor = MaterialTheme.colorScheme.surface,
          contentColor = MaterialTheme.colorScheme.onSurface,
          onClick = onOpenProxy,
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
        onClick = onOpenProfiles,
      )

      AnimatedVisibility(visible = clashRunning && hasProviders) {
        MainActionLabel(
          modifier = Modifier.padding(vertical = dimens.mainLabelMarginVertical),
          iconRes = R.drawable.ic_baseline_swap_vertical_circle,
          text = stringResource(R.string.providers),
          onClick = onOpenProviders,
        )
      }

      MainActionLabel(
        modifier = Modifier.padding(vertical = dimens.mainLabelMarginVertical),
        iconRes = R.drawable.ic_baseline_assignment,
        text = stringResource(R.string.logs),
        onClick = onOpenLogs,
      )
      MainActionLabel(
        modifier = Modifier.padding(vertical = dimens.mainLabelMarginVertical),
        iconRes = R.drawable.ic_baseline_settings,
        text = stringResource(R.string.settings),
        onClick = onOpenSettings,
      )
      MainActionLabel(
        modifier = Modifier.padding(vertical = dimens.mainLabelMarginVertical),
        iconRes = R.drawable.ic_baseline_help_center,
        text = stringResource(R.string.help),
        onClick = onOpenHelp,
      )
      MainActionLabel(
        modifier = Modifier.padding(vertical = dimens.mainLabelMarginVertical),
        iconRes = R.drawable.ic_baseline_info,
        text = stringResource(R.string.about),
        onClick = onOpenAbout,
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
private fun MainContentRunningPreview() = MihomoTheme {
  MainContent(
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

@PreviewMihomo
@Composable
private fun MainContentStoppedPreview() = MihomoTheme {
  MainContent(
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
