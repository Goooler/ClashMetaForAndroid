package com.github.kr328.clash.design

import android.content.Context
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.kr328.clash.R
import com.github.kr328.clash.design.component.MihomoScaffold
import com.github.kr328.clash.design.component.ModelTextInputDialog
import com.github.kr328.clash.design.model.File
import com.github.kr328.clash.design.ui.theme.MihomoTheme
import com.github.kr328.clash.design.ui.theme.PreviewMihomo
import com.github.kr328.clash.design.util.ValidatorFileName
import com.github.kr328.clash.design.util.elapsedIntervalString
import com.github.kr328.clash.design.util.toBytesString
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class FilesDesign(context: Context) : Design<FilesDesign.Request>(context) {
  sealed interface Request {
    data class OpenFile(val file: File) : Request

    data class OpenDirectory(val file: File) : Request

    data class RenameFile(val file: File, val newName: String) : Request

    data class DeleteFile(val file: File) : Request

    data class ImportFile(val file: File?) : Request

    data class ExportFile(val file: File) : Request

    data object PopStack : Request
  }

  private var files by mutableStateOf<List<File>>(emptyList())
  private var currentInBaseDir by mutableStateOf(false)
  private var configurationEditable by mutableStateOf(false)

  @Composable
  override fun Content() = MihomoTheme {
    FilesScreen(
      files = files,
      currentInBaseDir = currentInBaseDir,
      configurationEditable = configurationEditable,
      onBack = { requests.trySend(Request.PopStack) },
      onOpen = { file ->
        if (file.isDirectory) {
          requests.trySend(Request.OpenDirectory(file))
        } else {
          requests.trySend(Request.OpenFile(file))
        }
      },
      onNew = { requests.trySend(Request.ImportFile(null)) },
      onImport = { requests.trySend(Request.ImportFile(it)) },
      onExport = { requests.trySend(Request.ExportFile(it)) },
      onRename = { file, newName -> requests.trySend(Request.RenameFile(file, newName)) },
      onDelete = { requests.trySend(Request.DeleteFile(it)) },
    )
  }

  suspend fun swapFiles(files: List<File>, currentInBaseDir: Boolean) =
    withContext(Dispatchers.Main) {
      this@FilesDesign.files = files
      this@FilesDesign.currentInBaseDir = currentInBaseDir
    }

  fun updateConfigurationEditable(editable: Boolean) {
    configurationEditable = editable
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilesScreen(
  files: List<File>,
  currentInBaseDir: Boolean,
  configurationEditable: Boolean,
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

  MihomoScaffold(
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
  tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
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
private fun FilesScreenPreview() = MihomoTheme {
  FilesScreen(
    files =
      listOf(
        File("1", "config.yaml", 1024, System.currentTimeMillis() - 60_000, false),
        File("2", "scripts", 0, System.currentTimeMillis() - 3_600_000, true),
      ),
    currentInBaseDir = true,
    configurationEditable = false,
    onBack = {},
    onOpen = {},
    onNew = {},
    onImport = {},
    onExport = {},
    onRename = { _, _ -> },
    onDelete = {},
  )
}
