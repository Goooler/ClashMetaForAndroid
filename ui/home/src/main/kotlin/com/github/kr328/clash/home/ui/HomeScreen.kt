package com.github.kr328.clash.home.ui

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.home.R
import com.github.kr328.clash.home.vm.HomeViewModel
import com.github.kr328.clash.ui.component.Spacer
import com.github.kr328.clash.ui.component.TabbyScaffold
import com.github.kr328.clash.ui.icon.BaselineApps
import com.github.kr328.clash.ui.icon.BaselineAssignment
import com.github.kr328.clash.ui.icon.BaselineHelpCenter
import com.github.kr328.clash.ui.icon.BaselineInfo
import com.github.kr328.clash.ui.icon.BaselineSettings
import com.github.kr328.clash.ui.icon.BaselineSwapVerticalCircle
import com.github.kr328.clash.ui.icon.BaselineViewList
import com.github.kr328.clash.ui.icon.OutlineCheckCircle
import com.github.kr328.clash.ui.icon.OutlineNotInterested
import com.github.kr328.clash.ui.icon.TabbyIcons
import com.github.kr328.clash.ui.lifecycle.viewModelWithLifecycle
import com.github.kr328.clash.ui.theme.PreviewTabby
import com.github.kr328.clash.ui.theme.TabbyDarkSurface
import com.github.kr328.clash.ui.theme.TabbyLightStopped
import com.github.kr328.clash.ui.theme.TabbyOnPrimary
import com.github.kr328.clash.ui.theme.TabbyTheme

@Composable
internal fun HomeScreen(
  modifier: Modifier = Modifier,
  viewModel: HomeViewModel = viewModelWithLifecycle(),
  onOpenProxy: () -> Unit,
  onOpenProfiles: () -> Unit,
  onOpenProviders: () -> Unit,
  onOpenLogs: () -> Unit,
  onOpenSettings: () -> Unit,
  onOpenHelp: () -> Unit,
) {
  val clashRunning by viewModel.clashRunning.collectAsStateWithLifecycle()
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val eventState by viewModel.eventState.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }

  val noProfileText = stringResource(R.string.no_profile_selected)
  val profilesActionText = stringResource(CommonR.string.profiles)

  val vpnLauncher =
    rememberLauncherForActivityResult(StartActivityForResult()) { result ->
      if (result.resultCode == Activity.RESULT_OK) {
        viewModel.onVpnPermissionGranted()
      }
    }

  LaunchedEffect(eventState) {
    when (val event = eventState) {
      Idle -> Unit
      is RequestVpnPermission -> vpnLauncher.launch(event.intent)
      ShowNoProfileMessage -> {
        val result =
          snackbarHostState.showSnackbar(
            message = noProfileText,
            actionLabel = profilesActionText,
            duration = SnackbarDuration.Long,
          )

        if (result == SnackbarResult.ActionPerformed) onOpenProfiles()
      }
      is ShowMessage -> {
        snackbarHostState.showSnackbar(message = event.message)
      }
    }
    viewModel.consumeEvent()
  }

  HomeContent(
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
private fun HomeContent(
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
  val stoppedColor = if (darkTheme) TabbyDarkSurface else TabbyLightStopped

  TabbyScaffold(
    title = "",
    modifier = modifier,
    topBar = {},
    snackbarHostState = snackbarHostState,
  ) { innerPadding ->
    Column(
      modifier =
        Modifier.fillMaxSize()
          .padding(innerPadding)
          .padding(horizontal = 30.dp)
          .verticalScroll(rememberScrollState())
    ) {
      Row(
        modifier = Modifier.fillMaxWidth().height(90.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Spacer(10.dp)
        Image(
          painter = painterResource(CommonR.drawable.ic_tabby_foreground),
          contentDescription = null,
          modifier = Modifier.size(logoSize),
        )
        Spacer(10.dp)
        Text(
          text = stringResource(CommonR.string.tabby),
          style = MaterialTheme.typography.titleLarge,
        )
      }

      HomeActionCard(
        modifier = Modifier.padding(vertical = cardMarginVertical),
        icon = if (clashRunning) TabbyIcons.OutlineCheckCircle else TabbyIcons.OutlineNotInterested,
        text = stringResource(if (clashRunning) CommonR.string.running else R.string.stopped),
        subtext =
          if (clashRunning && forwarded != null)
            stringResource(R.string.format_traffic_forwarded, forwarded)
          else stringResource(CommonR.string.tap_to_start),
        backgroundColor = if (clashRunning) MaterialTheme.colorScheme.primary else stoppedColor,
        contentColor = TabbyOnPrimary,
        onClick = onToggleStatus,
      )

      AnimatedVisibility(visible = clashRunning) {
        HomeActionCard(
          modifier = Modifier.padding(vertical = cardMarginVertical),
          icon = TabbyIcons.BaselineApps,
          text = stringResource(CommonR.string.proxy),
          subtext = mode,
          backgroundColor = MaterialTheme.colorScheme.surface,
          contentColor = MaterialTheme.colorScheme.onSurface,
          onClick = onOpenProxy,
        )
      }

      HomeActionCard(
        modifier = Modifier.padding(vertical = cardMarginVertical),
        icon = TabbyIcons.BaselineViewList,
        text = stringResource(R.string.profile),
        subtext =
          if (profileName != null) stringResource(R.string.format_profile_activated, profileName)
          else stringResource(CommonR.string.not_selected),
        backgroundColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        onClick = onOpenProfiles,
      )

      AnimatedVisibility(visible = clashRunning && hasProviders) {
        HomeActionLabel(
          modifier = Modifier.padding(vertical = labelMarginVertical),
          icon = TabbyIcons.BaselineSwapVerticalCircle,
          text = stringResource(CommonR.string.providers),
          onClick = onOpenProviders,
        )
      }

      HomeActionLabel(
        modifier = Modifier.padding(vertical = labelMarginVertical),
        icon = TabbyIcons.BaselineAssignment,
        text = stringResource(CommonR.string.logs),
        onClick = onOpenLogs,
      )
      HomeActionLabel(
        modifier = Modifier.padding(vertical = labelMarginVertical),
        icon = TabbyIcons.BaselineSettings,
        text = stringResource(CommonR.string.settings),
        onClick = onOpenSettings,
      )
      HomeActionLabel(
        modifier = Modifier.padding(vertical = labelMarginVertical),
        icon = TabbyIcons.BaselineHelpCenter,
        text = stringResource(R.string.help),
        onClick = onOpenHelp,
      )
      HomeActionLabel(
        modifier = Modifier.padding(vertical = labelMarginVertical),
        icon = TabbyIcons.BaselineInfo,
        text = stringResource(R.string.about),
        onClick = onOpenAbout,
      )
    }

    aboutVersionName?.let { AboutDialog(versionName = it, onDismiss = onDismissAbout) }
  }
}

@Composable
private fun HomeActionCard(
  icon: ImageVector,
  text: String,
  subtext: String?,
  backgroundColor: Color,
  contentColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier.fillMaxWidth().heightIn(min = 85.dp),
    onClick = onClick,
    colors = CardDefaults.cardColors(containerColor = backgroundColor, contentColor = contentColor),
    elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
  ) {
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .padding(horizontal = actionItemPaddingHorizontal, vertical = actionItemPaddingVertical),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(actionIconSize),
        tint = contentColor,
      )
      Spacer(actionItemPaddingHorizontal)
      Column {
        Text(text = text, style = MaterialTheme.typography.bodyLarge, color = contentColor)
        if (subtext != null) {
          Spacer(5.dp)
          Text(text = subtext, style = MaterialTheme.typography.bodyMedium, color = contentColor)
        }
      }
    }
  }
}

@Composable
private fun HomeActionLabel(
  icon: ImageVector,
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .heightIn(min = 60.dp)
        .clickable(onClick = onClick)
        .padding(vertical = actionItemPaddingVertical),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Spacer(actionItemPaddingHorizontal)
    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(actionIconSize))
    Spacer(actionItemPaddingHorizontal)
    Text(text = text, style = MaterialTheme.typography.bodyLarge)
  }
}

@Composable
private fun AboutDialog(versionName: String, onDismiss: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(onClick = onDismiss) { Text(text = stringResource(CommonR.string.ok)) }
    },
    icon = {
      Image(
        painter = painterResource(CommonR.drawable.ic_tabby_foreground),
        contentDescription = null,
        modifier = Modifier.size(logoSize),
      )
    },
    title = {
      Text(
        text = stringResource(CommonR.string.tabby),
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
      )
    },
    text = {
      Text(text = versionName, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    },
  )
}

private val logoSize = 55.dp
private val cardMarginVertical = 5.dp
private val labelMarginVertical = 2.dp
private val actionItemPaddingHorizontal = 20.dp
private val actionItemPaddingVertical = 15.dp
private val actionIconSize = 30.dp

@PreviewTabby
@Composable
private fun HomeContentRunningPreview() = TabbyTheme {
  HomeContent(
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

@PreviewTabby
@Composable
private fun HomeContentStoppedPreview() = TabbyTheme {
  HomeContent(
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
