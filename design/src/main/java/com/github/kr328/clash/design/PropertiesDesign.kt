package com.github.kr328.clash.design

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.Dp
import com.github.kr328.clash.core.model.FetchStatus
import com.github.kr328.clash.design.component.MihomoScaffold
import com.github.kr328.clash.design.component.SettingsTipsItem
import com.github.kr328.clash.design.dialog.ModelProgressBarConfigure
import com.github.kr328.clash.design.dialog.requestModelTextInput
import com.github.kr328.clash.design.dialog.withModelProgressBar
import com.github.kr328.clash.design.ui.theme.MihomoTheme
import com.github.kr328.clash.design.ui.theme.PreviewMihomo
import com.github.kr328.clash.design.util.ValidatorAutoUpdateInterval
import com.github.kr328.clash.design.util.ValidatorHttpUrl
import com.github.kr328.clash.design.util.ValidatorNotBlank
import com.github.kr328.clash.service.model.Profile
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PropertiesDesign(context: Context) : Design<PropertiesDesign.Request>(context) {
  sealed class Request {
    object Commit : Request()

    object BrowseFiles : Request()
  }

  private var profileState by mutableStateOf<Profile?>(null)
  private var originalProfileState by mutableStateOf<Profile?>(null)
  private var processingState by mutableStateOf(false)

  override val root: View by composeView {
    MihomoTheme {
      profileState?.let { profile ->
        PropertiesScreen(
          profile = profile,
          processing = processingState,
          hasUnsavedChanges =
            originalProfileState?.let { original -> hasUnsavedChanges(profile, original) } == true,
          onInputName = ::inputName,
          onInputUrl = ::inputUrl,
          onInputInterval = ::inputInterval,
          onBrowseFiles = { requests.trySend(Request.BrowseFiles) },
          onCommit = { requests.trySend(Request.Commit) },
          onRequestClose = { (context as? Activity)?.finish() },
        )
      }
    }
  }

  var profile: Profile
    get() = requireNotNull(profileState)
    set(value) {
      if (originalProfileState == null) {
        originalProfileState = value.copy()
      }
      profileState = value
    }

  suspend fun withProcessing(executeTask: suspend (suspend (FetchStatus) -> Unit) -> Unit) =
    withContext(Dispatchers.Main) {
      try {
        processingState = true
        context.withModelProgressBar {
          configure {
            isIndeterminate = true
            text = context.getString(R.string.initializing)
          }
          executeTask { configure { applyFrom(it) } }
        }
      } finally {
        processingState = false
      }
    }

  private fun inputName() = launch {
    val name =
      context.requestModelTextInput(
        initial = profile.name,
        title = context.getText(R.string.name),
        hint = context.getText(R.string.properties),
        error = context.getText(R.string.should_not_be_blank),
        validator = ValidatorNotBlank,
      )

    if (name != profile.name) {
      profile = profile.copy(name = name)
    }
  }

  private fun inputUrl() {
    if (profile.type == Profile.Type.External) return
    launch {
      val url =
        context.requestModelTextInput(
          initial = profile.source,
          title = context.getText(R.string.url),
          hint = context.getText(R.string.profile_url),
          error = context.getText(R.string.accept_http_content),
          validator = ValidatorHttpUrl,
        )

      if (url != profile.source) {
        profile = profile.copy(source = url)
      }
    }
  }

  private fun inputInterval() {
    launch {
      var minutes = profile.interval.milliseconds.inWholeMinutes

      minutes =
        context
          .requestModelTextInput(
            initial = if (minutes == 0L) "" else minutes.toString(),
            title = context.getText(R.string.auto_update),
            hint = context.getText(R.string.auto_update_minutes),
            error = context.getText(R.string.at_least_15_minutes),
            validator = ValidatorAutoUpdateInterval,
          )
          .toLongOrNull() ?: 0

      val interval = minutes.minutes.inWholeMilliseconds

      if (interval != profile.interval) {
        profile = profile.copy(interval = interval)
      }
    }
  }

  private fun hasUnsavedChanges(profile: Profile, original: Profile): Boolean {
    return profile.name != original.name ||
      profile.source != original.source ||
      profile.interval != original.interval
  }

  private fun ModelProgressBarConfigure.applyFrom(status: FetchStatus) {
    when (status.action) {
      FetchStatus.Action.FetchConfiguration -> {
        text = context.getString(R.string.format_fetching_configuration, status.args[0])
        isIndeterminate = true
      }
      FetchStatus.Action.FetchProviders -> {
        text = context.getString(R.string.format_fetching_provider, status.args[0])
        isIndeterminate = false
        max = status.max
        progress = status.progress
      }
      FetchStatus.Action.Verifying -> {
        text = context.getString(R.string.verifying)
        isIndeterminate = false
        max = status.max
        progress = status.progress
      }
    }
  }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun PropertiesScreen(
  profile: Profile,
  processing: Boolean,
  hasUnsavedChanges: Boolean,
  onInputName: () -> Unit,
  onInputUrl: () -> Unit,
  onInputInterval: () -> Unit,
  onBrowseFiles: () -> Unit,
  onCommit: () -> Unit,
  onRequestClose: () -> Unit,
) {
  val contentPaddingHorizontal = dimensionResource(R.dimen.item_tailing_margin)
  val itemPaddingVertical = dimensionResource(R.dimen.item_padding_vertical)
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

  MihomoScaffold(
    title = stringResource(R.string.properties),
    onBack = onBack,
    scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
    actions = {
      if (processing) {
        CircularProgressIndicator(
          modifier = Modifier.size(dimensionResource(R.dimen.item_tailing_component_size) / 2),
          strokeWidth = dimensionResource(R.dimen.toolbar_image_action_padding) / 2,
        )
      } else {
        IconButton(onClick = onCommit) {
          Icon(
            painter = painterResource(R.drawable.ic_baseline_save),
            contentDescription = stringResource(R.string.save),
          )
        }
      }
    },
  ) { innerPadding ->
    Column(
      modifier =
        Modifier.fillMaxSize()
          .padding(innerPadding)
          .verticalScroll(rememberScrollState())
          .padding(horizontal = contentPaddingHorizontal)
    ) {
      SettingsTipsItem(text = AnnotatedString.fromHtml(stringResource(R.string.tips_properties)))
      PropertiesActionItem(
        title = stringResource(R.string.name),
        text = profile.name,
        placeholder = stringResource(R.string.profile_name),
        iconRes = R.drawable.ic_outline_label,
        enabled = true,
        onClick = onInputName,
        itemPaddingVertical = itemPaddingVertical,
      )
      PropertiesActionItem(
        title = stringResource(R.string.url),
        text = profile.source,
        placeholder = stringResource(R.string.accept_http_content),
        iconRes = R.drawable.ic_outline_inbox,
        enabled = profile.type != Profile.Type.File && profile.type != Profile.Type.External,
        onClick = onInputUrl,
        itemPaddingVertical = itemPaddingVertical,
      )
      PropertiesActionItem(
        title = stringResource(R.string.auto_update),
        text =
          if (profile.interval == 0L) {
            stringResource(R.string.disabled)
          } else {
            stringResource(R.string.format_minutes, profile.interval.milliseconds.inWholeMinutes)
          },
        placeholder = stringResource(R.string.at_least_15_minutes),
        iconRes = R.drawable.ic_outline_update,
        enabled = profile.type != Profile.Type.File,
        onClick = onInputInterval,
        itemPaddingVertical = itemPaddingVertical,
      )
      PropertiesActionItem(
        title = stringResource(R.string.browse_files),
        text = stringResource(R.string.browse_configuration_providers),
        placeholder = stringResource(R.string.browse_configuration_providers),
        iconRes = R.drawable.ic_outline_folder,
        enabled = true,
        onClick = onBrowseFiles,
        itemPaddingVertical = itemPaddingVertical,
      )
    }
  }

  if (showExitWithoutSavingDialog) {
    ExitWithoutSavingDialog(
      onConfirm = onRequestClose,
      onDismiss = { showExitWithoutSavingDialog = false },
    )
  }
}

@Composable
private fun ExitWithoutSavingDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(text = stringResource(R.string.exit_without_save)) },
    text = { Text(text = stringResource(R.string.exit_without_save_warning)) },
    confirmButton = {
      TextButton(onClick = onConfirm) { Text(text = stringResource(R.string.ok)) }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(text = stringResource(R.string.cancel)) }
    },
  )
}

@Composable
private fun PropertiesActionItem(
  title: String,
  text: String,
  placeholder: String,
  iconRes: Int,
  enabled: Boolean,
  onClick: () -> Unit,
  itemPaddingVertical: Dp,
) {
  val itemHeaderComponentSize = dimensionResource(R.dimen.item_header_component_size)
  val itemHeaderMargin = dimensionResource(R.dimen.item_header_margin)
  val itemTextMargin = dimensionResource(R.dimen.item_text_margin)
  val contentAlpha = if (enabled) 1f else 0.5f

  Row(
    modifier =
      Modifier.fillMaxWidth()
        .clickable(enabled = enabled, onClick = onClick)
        .padding(vertical = itemPaddingVertical)
        .alpha(contentAlpha),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Spacer(modifier = Modifier.width(itemHeaderMargin))
    Icon(
      painter = painterResource(iconRes),
      contentDescription = null,
      modifier = Modifier.size(itemHeaderComponentSize),
    )
    Spacer(modifier = Modifier.width(itemHeaderMargin))
    Column {
      Text(text = title, style = MaterialTheme.typography.bodyLarge)
      Text(
        text = text.ifBlank { placeholder },
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = itemTextMargin),
      )
    }
  }
  Spacer(modifier = Modifier.height(dimensionResource(R.dimen.properties_element_margin_vertical)))
}

@PreviewMihomo
@Composable
private fun PropertiesScreenPreview() = MihomoTheme {
  PropertiesScreen(
    profile =
      Profile(
        uuid = UUID(0, 0),
        name = "Meta Profile",
        type = Profile.Type.Url,
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
    hasUnsavedChanges = false,
    onInputName = {},
    onInputUrl = {},
    onInputInterval = {},
    onBrowseFiles = {},
    onCommit = {},
    onRequestClose = {},
  )
}
