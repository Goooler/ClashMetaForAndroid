package com.github.kr328.clash.files.ui

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.kr328.clash.R
import com.github.kr328.clash.common.util.grantPermissions
import com.github.kr328.clash.files.vm.FilesViewModel
import com.github.kr328.clash.model.File
import com.github.kr328.clash.ui.component.MihomoScaffold
import com.github.kr328.clash.ui.component.ModelTextInputDialog
import com.github.kr328.clash.ui.theme.MihomoTheme
import com.github.kr328.clash.ui.theme.PreviewMihomo
import com.github.kr328.clash.util.ValidatorFileName
import com.github.kr328.clash.util.elapsedIntervalString
import com.github.kr328.clash.util.toBytesString
import com.github.kr328.clash.util.toast
import java.util.UUID
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.delay

@Composable
fun FilesScreen(
  uuid: UUID,
  modifier: Modifier = Modifier,
  viewModel: FilesViewModel = viewModel(),
  onFinish: () -> Unit,
) {
  val lifecycleOwner = LocalLifecycleOwner.current
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val eventState by viewModel.eventState.collectAsStateWithLifecycle()
  val context = LocalContext.current

  var pendingImportTarget by remember { mutableStateOf<File?>(null) }
  var pendingExportSource by remember { mutableStateOf<File?>(null) }

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
      FilesViewModel.EventState.Idle -> Unit
      FilesViewModel.EventState.Finish -> {
        onFinish()
      }
      is FilesViewModel.EventState.OpenFile -> {
        openFileLauncher.launch(
          Intent(Intent.ACTION_VIEW).setDataAndType(event.uri, "text/plain").grantPermissions()
        )
      }
      is FilesViewModel.EventState.RequestImport -> {
        pendingImportTarget = event.targetFile
        importLauncher.launch("*/*")
      }
      is FilesViewModel.EventState.RequestExport -> {
        pendingExportSource = event.sourceFile
        exportLauncher.launch(event.sourceFile.name)
      }
      is FilesViewModel.EventState.ShowMessage -> {
        context.toast(event.message)
      }
    }
    viewModel.consumeEvent()
  }

  FilesContent(
    modifier = modifier,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilesContent(
  modifier: Modifier = Modifier,
  uiState: FilesViewModel.UiState,
  onBack: () -> Unit,
  onOpen: (File) -> Unit,
  onNew: () -> Unit,
  onImport: (File) -> Unit,
  onExport: (File) -> Unit,
  onRename: (File, String) -> Unit,
  onDelete: (File) -> Unit,
) {
  var menuFile by remember { mutableStateOf<File?>(null) }
  var renameFile by remember { mutableStateOf<File?>(null) }
  val sheetState = rememberModalBottomSheetState()
  val currentInBaseDir = uiState.currentInBaseDir
  val configurationEditable = uiState.configurationEditable
  val files = uiState.files

  if (menuFile != null) {
    ModalBottomSheet(onDismissRequest = { menuFile = null }, sheetState = sheetState) {
      val file = menuFile!!
      if (!file.isDirectory && (!currentInBaseDir || configurationEditable)) {
        FilesMenuAction(
          icon = R.drawable.ic_baseline_get_app,
          text = stringResource(R.string.import_),
          onClick = {
            menuFile = null
            onImport(file)
          },
        )
      }
      if (!file.isDirectory && file.size > 0) {
        FilesMenuAction(
          icon = R.drawable.ic_baseline_publish,
          text = stringResource(R.string.export),
          onClick = {
            menuFile = null
            onExport(file)
          },
        )
      }
      if (!currentInBaseDir) {
        FilesMenuAction(
          icon = R.drawable.ic_baseline_edit,
          text = stringResource(R.string.rename),
          onClick = {
            menuFile = null
            renameFile = file
          },
        )
        FilesMenuAction(
          icon = R.drawable.ic_outline_delete,
          text = stringResource(R.string.delete),
          tint = MaterialTheme.colorScheme.error,
          onClick = {
            menuFile = null
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
    title = stringResource(R.string.files),
    onBack = onBack,
    actions = {
      if (!currentInBaseDir) {
        IconButton(onClick = onNew) {
          Icon(
            painter = painterResource(R.drawable.ic_baseline_add),
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
      items(items = files, key = File::id) { file ->
        FileItem(
          file = file,
          currentTime = currentTime,
          context = context,
          onClick = { onOpen(file) },
          onMore = { menuFile = file },
        )
        HorizontalDivider()
      }
    }
  }

  if (renameFile != null) {
    ModelTextInputDialog(
      title = stringResource(R.string.file_name),
      initialValue = renameFile!!.name,
      hint = stringResource(R.string.file_name),
      error = stringResource(R.string.invalid_file_name),
      validator = ValidatorFileName,
      onDismiss = { renameFile = null },
      onConfirm = { newName ->
        onRename(renameFile!!, newName)
        renameFile = null
      },
    )
  }
}

@Composable
private fun FileItem(
  file: File,
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
        painter =
          painterResource(
            if (file.isDirectory) R.drawable.ic_outline_folder else R.drawable.ic_outline_article
          ),
        contentDescription = null,
        modifier = Modifier.size(28.dp),
      )
    }

    Column(modifier = Modifier.weight(1f).padding(vertical = 8.dp)) {
      Text(text = file.name)
      if (!file.isDirectory) {
        Spacer(modifier = Modifier.size(3.dp))
        Text(text = file.size.toBytesString(), style = MaterialTheme.typography.bodyMedium)
      }
    }

    if (!file.isDirectory) {
      Text(
        text = (currentTime - file.lastModified).elapsedIntervalString(context),
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.padding(horizontal = 8.dp),
      )
    }

    IconButton(onClick = onMore) {
      Icon(
        painter = painterResource(R.drawable.ic_baseline_more_vert),
        contentDescription = stringResource(R.string.more),
      )
    }
  }
}

@Composable
private fun FilesMenuAction(
  icon: Int,
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
      painter = painterResource(icon),
      contentDescription = null,
      tint = tint,
      modifier = Modifier.size(24.dp),
    )
    Spacer(modifier = Modifier.size(16.dp))
    Text(text = text, color = tint)
  }
}

@PreviewMihomo
@Composable
private fun FilesContentPreview() = MihomoTheme {
  FilesContent(
    uiState =
      FilesViewModel.UiState(
        files =
          listOf(
            File("1", "config.yaml", 1024, System.currentTimeMillis() - 60_000, false),
            File("2", "scripts", 0, System.currentTimeMillis() - 3_600_000, true),
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
