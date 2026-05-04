package com.github.kr328.clash.profile.ui

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.kr328.clash.common.util.grantPermissions
import com.github.kr328.clash.glue.R
import com.github.kr328.clash.model.ConfigFile
import com.github.kr328.clash.profile.vm.FilesViewModel
import com.github.kr328.clash.ui.component.MihomoScaffold
import com.github.kr328.clash.ui.icon.BaselineAdd
import com.github.kr328.clash.ui.icon.BaselineEdit
import com.github.kr328.clash.ui.icon.BaselineGetApp
import com.github.kr328.clash.ui.icon.BaselineMoreVert
import com.github.kr328.clash.ui.icon.BaselineSave
import com.github.kr328.clash.ui.icon.MihomoIcons
import com.github.kr328.clash.ui.icon.OutlineArticle
import com.github.kr328.clash.ui.icon.OutlineDelete
import com.github.kr328.clash.ui.icon.OutlineFolder
import com.github.kr328.clash.ui.theme.MihomoThemeWrapper
import com.github.kr328.clash.ui.theme.PreviewMihomo
import com.github.kr328.clash.util.Validator
import com.github.kr328.clash.util.ValidatorFileName
import com.github.kr328.clash.util.elapsedIntervalString
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid
import kotlinx.coroutines.delay
import me.saket.bytesize.binaryBytes

@Composable
internal fun FilesScreen(
  uuid: Uuid,
  modifier: Modifier = Modifier,
  viewModel: FilesViewModel = viewModel(),
  onFinish: () -> Unit,
) {
  val lifecycleOwner = LocalLifecycleOwner.current
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val eventState by viewModel.eventState.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }

  var pendingImportTarget by remember { mutableStateOf<ConfigFile?>(null) }
  var pendingExportSource by remember { mutableStateOf<ConfigFile?>(null) }

  val openFileLauncher = rememberLauncherForActivityResult(StartActivityForResult()) {}

  val importLauncher =
    rememberLauncherForActivityResult(GetContent()) { uri ->
      viewModel.onImportResult(uri, pendingImportTarget)
      pendingImportTarget = null
    }

  val exportLauncher =
    rememberLauncherForActivityResult(CreateDocument("text/plain")) { uri ->
      viewModel.onExportResult(uri, pendingExportSource)
      pendingExportSource = null
    }

  LaunchedEffect(uuid) { viewModel.init(uuid = uuid) }

  DisposableEffect(lifecycleOwner, viewModel) {
    lifecycleOwner.lifecycle.addObserver(viewModel)
    onDispose { lifecycleOwner.lifecycle.removeObserver(viewModel) }
  }

  LaunchedEffect(eventState) {
    when (val event = eventState) {
      Idle -> Unit
      Finish -> {
        onFinish()
      }
      is OpenFile -> {
        openFileLauncher.launch(
          Intent(Intent.ACTION_VIEW).setDataAndType(event.uri, "text/plain").grantPermissions()
        )
      }
      is RequestImport -> {
        pendingImportTarget = event.targetConfigFile
        importLauncher.launch("*/*")
      }
      is RequestExport -> {
        pendingExportSource = event.sourceConfigFile
        exportLauncher.launch(event.sourceConfigFile.name)
      }
      is ShowMessage -> {
        snackbarHostState.showSnackbar(message = event.message)
      }
    }
    viewModel.consumeEvent()
  }

  FilesContent(
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    uiState = uiState,
    onBack = viewModel::onBack,
    onOpen = viewModel::onOpen,
    onNew = { viewModel.onRequestImport(null) },
    onImport = { viewModel.onRequestImport(it) },
    onExport = { viewModel.onRequestExport(it) },
    onRename = viewModel::onRename,
    onDelete = viewModel::onDelete,
  )
}

@Composable
private fun FilesContent(
  modifier: Modifier = Modifier,
  snackbarHostState: SnackbarHostState,
  uiState: FilesViewModel.UiState,
  onBack: () -> Unit,
  onOpen: (ConfigFile) -> Unit,
  onNew: () -> Unit,
  onImport: (ConfigFile) -> Unit,
  onExport: (ConfigFile) -> Unit,
  onRename: (ConfigFile, String) -> Unit,
  onDelete: (ConfigFile) -> Unit,
) {
  var menuConfigFile by remember { mutableStateOf<ConfigFile?>(null) }
  var renameConfigFile by remember { mutableStateOf<ConfigFile?>(null) }
  val sheetState = rememberModalBottomSheetState()
  val currentInBaseDir = uiState.currentInBaseDir
  val configurationEditable = uiState.configurationEditable
  val files = uiState.configFiles

  if (menuConfigFile != null) {
    ModalBottomSheet(onDismissRequest = { menuConfigFile = null }, sheetState = sheetState) {
      val file = menuConfigFile!!
      if (!file.isDirectory && (!currentInBaseDir || configurationEditable)) {
        FilesMenuAction(
          icon = MihomoIcons.BaselineGetApp,
          text = stringResource(R.string.import_),
          onClick = {
            menuConfigFile = null
            onImport(file)
          },
        )
      }
      if (!file.isDirectory && file.size > 0) {
        FilesMenuAction(
          icon = MihomoIcons.BaselineSave,
          text = stringResource(R.string.export),
          onClick = {
            menuConfigFile = null
            onExport(file)
          },
        )
      }
      if (!currentInBaseDir) {
        FilesMenuAction(
          icon = MihomoIcons.BaselineEdit,
          text = stringResource(R.string.rename),
          onClick = {
            menuConfigFile = null
            renameConfigFile = file
          },
        )
        FilesMenuAction(
          icon = MihomoIcons.OutlineDelete,
          text = stringResource(R.string.delete),
          tint = MaterialTheme.colorScheme.error,
          onClick = {
            menuConfigFile = null
            onDelete(file)
          },
        )
      }
      Spacer(modifier = Modifier.size(16.dp))
    }
  }

  BackHandler(onBack = onBack)

  MihomoScaffold(
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    title = stringResource(R.string.files),
    onBack = onBack,
    actions = {
      if (!currentInBaseDir) {
        IconButton(onClick = onNew) {
          Icon(
            imageVector = MihomoIcons.BaselineAdd,
            contentDescription = stringResource(R.string._new),
          )
        }
      }
    },
  ) { innerPadding ->
    val context = LocalContext.current
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
      while (true) {
        delay(1.minutes)
        currentTime = System.currentTimeMillis()
      }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      items(items = files, key = ConfigFile::id) { file ->
        FileItem(
          configFile = file,
          currentTime = currentTime,
          context = context,
          onClick = { onOpen(file) },
          onMore = { menuConfigFile = file },
        )
        HorizontalDivider()
      }
    }
  }

  if (renameConfigFile != null) {
    TextInputDialog(
      title = stringResource(R.string.file_name),
      initialValue = renameConfigFile!!.name,
      hint = stringResource(R.string.file_name),
      error = stringResource(R.string.invalid_file_name),
      validator = ValidatorFileName,
      onDismiss = { renameConfigFile = null },
      onConfirm = { newName ->
        onRename(renameConfigFile!!, newName)
        renameConfigFile = null
      },
    )
  }
}

@Composable
private fun TextInputDialog(
  title: String,
  initialValue: String? = null,
  hint: String? = null,
  error: String? = null,
  validator: Validator = { true },
  onDismiss: () -> Unit,
  onConfirm: (String) -> Unit,
) {
  val initialText = initialValue.orEmpty()

  var inputText by remember {
    mutableStateOf(
      TextFieldValue(text = initialText, selection = TextRange(initialValue?.length ?: 0))
    )
  }
  var inputError by remember { mutableStateOf(if (!validator(initialText)) error else null) }
  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  LaunchedEffect(Unit) {
    focusRequester.requestFocus()
    keyboardController?.show()
  }

  val isValidInput = validator(inputText.text)

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title) },
    text = {
      OutlinedTextField(
        value = inputText,
        onValueChange = { newValue ->
          inputText = newValue
          inputError =
            if (!validator(newValue.text)) {
              error
            } else {
              null
            }
        },
        label = hint?.let { { Text(it) } },
        isError = inputError != null,
        supportingText = inputError?.let { { Text(it) } },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
      )
    },
    confirmButton = {
      TextButton(onClick = { onConfirm(inputText.text) }, enabled = isValidInput) {
        Text(stringResource(R.string.ok))
      }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
  )
}

@Composable
private fun FileItem(
  configFile: ConfigFile,
  currentTime: Long,
  context: Context,
  onClick: () -> Unit,
  onMore: () -> Unit,
) {
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .heightIn(min = 56.dp)
        .clickable(onClick = onClick)
        .padding(end = 0.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier = Modifier.size(width = 65.dp, height = 56.dp),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector =
          if (configFile.isDirectory) MihomoIcons.OutlineFolder else MihomoIcons.OutlineArticle,
        contentDescription = null,
        modifier = Modifier.size(28.dp),
      )
    }

    Column(modifier = Modifier.weight(1f).padding(vertical = 8.dp)) {
      Text(text = configFile.name)
      if (!configFile.isDirectory) {
        Spacer(modifier = Modifier.size(3.dp))
        Text(
          text = configFile.size.binaryBytes.toString(),
          style = MaterialTheme.typography.bodyMedium,
        )
      }
    }

    if (!configFile.isDirectory) {
      Text(
        text = (currentTime - configFile.lastModified).elapsedIntervalString(context),
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.padding(horizontal = 8.dp),
      )
    }

    IconButton(onClick = onMore) {
      Icon(
        imageVector = MihomoIcons.BaselineMoreVert,
        contentDescription = stringResource(R.string.more),
      )
    }
  }
}

@Composable
private fun FilesMenuAction(
  icon: ImageVector,
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  tint: Color = MaterialTheme.colorScheme.onSurface,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(horizontal = 20.dp, vertical = 16.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = tint,
      modifier = Modifier.size(24.dp),
    )
    Spacer(modifier = Modifier.size(16.dp))
    Text(text = text, color = tint)
  }
}

@PreviewWrapper(MihomoThemeWrapper::class)
@PreviewMihomo
@Composable
private fun FilesContentPreview() {
  FilesContent(
    snackbarHostState = SnackbarHostState(),
    uiState =
      FilesViewModel.UiState(
        configFiles =
          listOf(
            ConfigFile("1", "config.yaml", 1024, System.currentTimeMillis() - 60_000, false),
            ConfigFile("2", "scripts", 0, System.currentTimeMillis() - 3_600_000, true),
          ),
        currentInBaseDir = true,
        configurationEditable = false,
      ),
    onBack = {},
    onOpen = {},
    onNew = {},
    onImport = {},
    onExport = {},
    onRename = { _, _ -> },
    onDelete = {},
  )
}
