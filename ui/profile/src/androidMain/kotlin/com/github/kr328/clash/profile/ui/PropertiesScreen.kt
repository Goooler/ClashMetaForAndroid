package com.github.kr328.clash.profile.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.kr328.clash.common.Res as CommonRes
import com.github.kr328.clash.common.cancel
import com.github.kr328.clash.common.disabled
import com.github.kr328.clash.common.name
import com.github.kr328.clash.common.ok
import com.github.kr328.clash.common.url
import com.github.kr328.clash.core.model.Profile
import com.github.kr328.clash.glue.util.ValidatorAgeSecretKey
import com.github.kr328.clash.glue.util.ValidatorAutoUpdateInterval
import com.github.kr328.clash.glue.util.ValidatorHttpUrl
import com.github.kr328.clash.glue.util.ValidatorNotBlank
import com.github.kr328.clash.profile.Res
import com.github.kr328.clash.profile.accept_http_content
import com.github.kr328.clash.profile.age_secret_key
import com.github.kr328.clash.profile.age_secret_key_hint
import com.github.kr328.clash.profile.auto_update
import com.github.kr328.clash.profile.browse_configuration_providers
import com.github.kr328.clash.profile.browse_files
import com.github.kr328.clash.profile.exit_without_save
import com.github.kr328.clash.profile.exit_without_save_warning
import com.github.kr328.clash.profile.format_minutes
import com.github.kr328.clash.profile.profile_name
import com.github.kr328.clash.profile.properties
import com.github.kr328.clash.profile.save
import com.github.kr328.clash.profile.tips_properties
import com.github.kr328.clash.profile.vm.PropertiesViewModel
import com.github.kr328.clash.ui.component.ModelProgressBarDialog
import com.github.kr328.clash.ui.component.TabbyScaffold
import com.github.kr328.clash.ui.icon.BaselineKey
import com.github.kr328.clash.ui.icon.BaselineSave
import com.github.kr328.clash.ui.icon.OutlineFolder
import com.github.kr328.clash.ui.icon.OutlineInbox
import com.github.kr328.clash.ui.icon.OutlineInfo
import com.github.kr328.clash.ui.icon.OutlineLabel
import com.github.kr328.clash.ui.icon.OutlineUpdate
import com.github.kr328.clash.ui.icon.TabbyIcons
import com.github.kr328.clash.ui.theme.PreviewTabby
import com.github.kr328.clash.ui.theme.TabbyThemeWrapper
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.preference
import me.zhanghai.compose.preference.textFieldPreference
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun PropertiesScreen(
  uuid: Uuid,
  modifier: Modifier = Modifier,
  viewModel: PropertiesViewModel = koinViewModel<PropertiesViewModel>(),
  onBrowseFiles: (Uuid) -> Unit,
  onFinish: (Boolean) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(uuid) { viewModel.init(uuid = uuid) }

  LaunchedEffect(viewModel) {
    viewModel.eventState.collect { event ->
      when (event) {
        is Finish -> onFinish(event.success)
        is BrowseFiles -> onBrowseFiles(event.uuid)
        is ShowMessage -> {
          snackbarHostState.showSnackbar(message = event.message)
        }
      }
    }
  }

  LifecycleStartEffect(viewModel) {
    onStopOrDispose {
      viewModel.persist()
    }
  }

  val profile = uiState.profile
  if (profile != null) {
    PropertiesContent(
      modifier = modifier,
      snackbarHostState = snackbarHostState,
      profile = profile,
      processing = uiState.processing,
      progressState = uiState.progress,
      hasUnsavedChanges = uiState.hasUnsavedChanges,
      onBrowseFiles = viewModel::onBrowseFiles,
      onCommit = viewModel::onCommit,
      onRequestClose = viewModel::onRequestClose,
      onNameChanged = viewModel::onNameChanged,
      onUrlChanged = viewModel::onUrlChanged,
      onIntervalChanged = viewModel::onIntervalChanged,
      onAgeSecretKeyChanged = viewModel::onAgeSecretKeyChanged,
    )
  }
}

@Composable
private fun PropertiesContent(
  modifier: Modifier = Modifier,
  snackbarHostState: SnackbarHostState,
  profile: Profile,
  processing: Boolean,
  progressState: PropertiesViewModel.ProgressState,
  hasUnsavedChanges: Boolean,
  onBrowseFiles: () -> Unit,
  onCommit: () -> Unit,
  onRequestClose: () -> Unit,
  onNameChanged: (String) -> Unit,
  onUrlChanged: (String) -> Unit,
  onIntervalChanged: (Long) -> Unit,
  onAgeSecretKeyChanged: (String?) -> Unit,
) {
  var showExitWithoutSavingDialog by rememberSaveable { mutableStateOf(false) }

  val onBack = {
    when {
      processing -> Unit
      showExitWithoutSavingDialog -> showExitWithoutSavingDialog = false
      hasUnsavedChanges -> showExitWithoutSavingDialog = true
      else -> onRequestClose()
    }
  }

  BackHandler(onBack = onBack)

  TabbyScaffold(
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    title = stringResource(Res.string.properties),
    onBack = onBack,
    actions = {
      if (processing) {
        CircularProgressIndicator(modifier = Modifier.size(15.dp), strokeWidth = 2.5.dp)
      } else {
        IconButton(onClick = onCommit) {
          Icon(
            imageVector = TabbyIcons.BaselineSave,
            contentDescription = stringResource(Res.string.save),
          )
        }
      }
    },
  ) { innerPadding ->
    ProvidePreferenceLocals {
      LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = innerPadding) {
        preference(
          key = "tips",
          title = { Text(stringResource(Res.string.properties)) },
          summary = { Text(AnnotatedString.fromHtml(stringResource(Res.string.tips_properties))) },
          icon = { Icon(imageVector = TabbyIcons.OutlineInfo, contentDescription = null) },
        )
        textFieldPreference(
          key = "name",
          value = profile.name,
          onValueChange = { newName ->
            if (newName != profile.name) {
              onNameChanged(newName)
            }
          },
          title = { Text(stringResource(CommonRes.string.name)) },
          textToValue = { input -> if (ValidatorNotBlank(input)) input else null },
          icon = { Icon(imageVector = TabbyIcons.OutlineLabel, contentDescription = null) },
          summary = { Text(profile.name.ifBlank { stringResource(Res.string.profile_name) }) },
        )
        textFieldPreference(
          key = "source",
          value = profile.source,
          onValueChange = { newUrl ->
            if (newUrl != profile.source) {
              onUrlChanged(newUrl)
            }
          },
          title = { Text(stringResource(CommonRes.string.url)) },
          textToValue = { input -> if (ValidatorHttpUrl(input)) input else null },
          enabled = profile.type != File && profile.type != External,
          icon = { Icon(imageVector = TabbyIcons.OutlineInbox, contentDescription = null) },
          summary = {
            Text(profile.source.ifBlank { stringResource(Res.string.accept_http_content) })
          },
        )
        textFieldPreference(
          key = "interval",
          value = profile.interval,
          onValueChange = { interval ->
            if (interval != profile.interval) {
              onIntervalChanged(interval)
            }
          },
          title = { Text(stringResource(Res.string.auto_update)) },
          textToValue = { input ->
            if (!ValidatorAutoUpdateInterval(input)) {
              null
            } else {
              val minutes = input.toLongOrNull() ?: 0
              minutes.minutes.inWholeMilliseconds
            }
          },
          enabled = profile.type != File,
          icon = { Icon(imageVector = TabbyIcons.OutlineUpdate, contentDescription = null) },
          summary = {
            val intervalSummary =
              if (profile.interval == 0L) {
                stringResource(CommonRes.string.disabled)
              } else {
                stringResource(
                  Res.string.format_minutes,
                  profile.interval.milliseconds.inWholeMinutes,
                )
              }
            Text(intervalSummary)
          },
          valueToText = { interval ->
            if (interval == 0L) {
              ""
            } else {
              interval.milliseconds.inWholeMinutes.toString()
            }
          },
        )
        textFieldPreference(
          key = "age_secret_key",
          value = profile.ageSecretKey ?: "",
          onValueChange = { newKey ->
            val key = newKey.ifBlank { null }
            if (key != profile.ageSecretKey) {
              onAgeSecretKeyChanged(key)
            }
          },
          title = { Text(stringResource(Res.string.age_secret_key)) },
          textToValue = { input -> if (ValidatorAgeSecretKey(input)) input else null },
          icon = { Icon(imageVector = TabbyIcons.BaselineKey, contentDescription = null) },
          summary = {
            Text(profile.ageSecretKey ?: stringResource(Res.string.age_secret_key_hint))
          },
        )
        preference(
          key = "browse_files",
          title = { Text(stringResource(Res.string.browse_files)) },
          summary = { Text(stringResource(Res.string.browse_configuration_providers)) },
          icon = { Icon(imageVector = TabbyIcons.OutlineFolder, contentDescription = null) },
          onClick = onBrowseFiles,
        )
      }
    }
  }

  if (showExitWithoutSavingDialog) {
    ExitWithoutSavingDialog(
      onConfirm = onRequestClose,
      onDismiss = { showExitWithoutSavingDialog = false },
    )
  }

  ModelProgressBarDialog(
    visible = progressState.visible,
    isIndeterminate = progressState.isIndeterminate,
    text = progressState.text,
    progress = progressState.progress,
    max = progressState.max,
  )
}

@Composable
private fun ExitWithoutSavingDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(text = stringResource(Res.string.exit_without_save)) },
    text = { Text(text = stringResource(Res.string.exit_without_save_warning)) },
    confirmButton = {
      TextButton(onClick = onConfirm) { Text(text = stringResource(CommonRes.string.ok)) }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(text = stringResource(CommonRes.string.cancel)) }
    },
  )
}

@PreviewWrapper(TabbyThemeWrapper::class)
@PreviewTabby
@Composable
private fun PropertiesContentPreview() {
  PropertiesContent(
    snackbarHostState = SnackbarHostState(),
    profile =
      Profile(
        uuid = Uuid.fromLongs(0, 0),
        name = "Meta Profile",
        type = Url,
        source = "https://example.com/config.yaml",
        active = false,
        interval = 60.minutes.inWholeMilliseconds,
        upload = 0,
        download = 0,
        total = 0,
        expire = 0,
        updatedAt = 0,
        imported = false,
        pending = false,
      ),
    processing = false,
    progressState = PropertiesViewModel.ProgressState(),
    hasUnsavedChanges = false,
    onBrowseFiles = {},
    onCommit = {},
    onRequestClose = {},
    onNameChanged = {},
    onUrlChanged = {},
    onIntervalChanged = {},
    onAgeSecretKeyChanged = {},
  )
}
