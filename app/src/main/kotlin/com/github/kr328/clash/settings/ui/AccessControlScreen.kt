package com.github.kr328.clash.settings.ui

import android.widget.ImageView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.kr328.clash.R
import com.github.kr328.clash.model.AppInfo
import com.github.kr328.clash.model.AppInfoSort
import com.github.kr328.clash.settings.vm.AccessControlViewModel
import com.github.kr328.clash.ui.component.MihomoScaffold
import com.github.kr328.clash.ui.icon.BaselineMoreVert
import com.github.kr328.clash.ui.icon.BaselineSearch
import com.github.kr328.clash.ui.icon.MihomoIcons
import com.github.kr328.clash.ui.theme.MihomoThemeWrapper
import com.github.kr328.clash.ui.theme.PreviewMihomo
import com.github.kr328.clash.ui.theme.mihomoDimens
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext

@Composable
fun AccessControlScreen(
  modifier: Modifier = Modifier,
  viewModel: AccessControlViewModel = viewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  DisposableEffect(viewModel) { onDispose { viewModel.persistSelection() } }

  AccessControlContent(
    apps = uiState.apps,
    selected = uiState.selected,
    sort = uiState.sort,
    reverse = uiState.reverse,
    showSystemApps = uiState.showSystemApps,
    actions = viewModel,
    modifier = modifier,
  )
}

@Composable
private fun AccessControlContent(
  apps: List<AppInfo>,
  selected: Set<String>,
  sort: AppInfoSort,
  reverse: Boolean,
  showSystemApps: Boolean,
  actions: AccessControlActions,
  modifier: Modifier = Modifier,
) {
  var showSearch by remember { mutableStateOf(false) }
  var showMenu by remember { mutableStateOf(false) }

  if (showMenu) {
    ModalBottomSheet(
      onDismissRequest = { showMenu = false },
      sheetState = rememberModalBottomSheetState(),
    ) {
      AccessControlMenuContent(
        sort = sort,
        reverse = reverse,
        showSystemApps = showSystemApps,
        onSelectAll = {
          showMenu = false
          actions.selectAll()
        },
        onSelectNone = {
          showMenu = false
          actions.selectNone()
        },
        onSelectInvert = {
          showMenu = false
          actions.selectInvert()
        },
        onImport = {
          showMenu = false
          actions.importFromClipboard()
        },
        onExport = {
          showMenu = false
          actions.exportToClipboard()
        },
        onUpdateSort = {
          showMenu = false
          actions.updateSort(it)
        },
        onUpdateReverse = {
          showMenu = false
          actions.updateReverse(it)
        },
        onUpdateShowSystemApps = {
          showMenu = false
          actions.updateShowSystemApps(it)
        },
      )
      Spacer(modifier = Modifier.height(16.dp))
    }
  }

  if (showSearch) {
    ModalBottomSheet(
      onDismissRequest = { showSearch = false },
      sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
      AccessControlSearchContent(apps = apps, selected = selected, onToggleApp = actions::toggleApp)
    }
  }

  MihomoScaffold(
    title = stringResource(R.string.access_control_packages),
    modifier = modifier,
    actions = {
      IconButton(onClick = { showSearch = true }) {
        Icon(
          imageVector = MihomoIcons.BaselineSearch,
          contentDescription = stringResource(R.string.search),
        )
      }
      IconButton(onClick = { showMenu = true }) {
        Icon(
          imageVector = MihomoIcons.BaselineMoreVert,
          contentDescription = stringResource(R.string.more),
        )
      }
    },
  ) { innerPadding ->
    LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      items(items = apps, key = AppInfo::packageName) { app ->
        AccessControlAppItem(
          app = app,
          selected = app.packageName in selected,
          onClick = { actions.toggleApp(app.packageName) },
        )
        HorizontalDivider()
      }
    }
  }
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@Composable
private fun ColumnScope.AccessControlSearchContent(
  apps: List<AppInfo>,
  selected: Set<String>,
  onToggleApp: (String) -> Unit,
) {
  var keyword by rememberSaveable { mutableStateOf("") }
  var filtered by remember(apps) { mutableStateOf(emptyList<AppInfo>()) }

  LaunchedEffect(apps) {
    snapshotFlow { keyword }
      .debounce(200.milliseconds)
      .distinctUntilChanged()
      .mapLatest { currentKeyword ->
        if (currentKeyword.isBlank()) {
          emptyList()
        } else {
          withContext(Dispatchers.Default) {
            apps.filter {
              it.label.contains(currentKeyword, ignoreCase = true) ||
                it.packageName.contains(currentKeyword, ignoreCase = true)
            }
          }
        }
      }
      .collect { filtered = it }
  }

  TextField(
    value = keyword,
    onValueChange = { keyword = it },
    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
    placeholder = { Text(text = stringResource(R.string.keyword)) },
    singleLine = true,
    colors = TextFieldDefaults.colors(),
  )

  Spacer(modifier = Modifier.height(8.dp))

  LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 520.dp)) {
    items(items = filtered, key = AppInfo::packageName) { app ->
      AccessControlAppItem(
        app = app,
        selected = app.packageName in selected,
        onClick = { onToggleApp(app.packageName) },
      )
      HorizontalDivider()
    }
  }

  Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun ColumnScope.AccessControlMenuContent(
  sort: AppInfoSort,
  reverse: Boolean,
  showSystemApps: Boolean,
  onSelectAll: () -> Unit,
  onSelectNone: () -> Unit,
  onSelectInvert: () -> Unit,
  onImport: () -> Unit,
  onExport: () -> Unit,
  onUpdateSort: (AppInfoSort) -> Unit,
  onUpdateReverse: (Boolean) -> Unit,
  onUpdateShowSystemApps: (Boolean) -> Unit,
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    AccessControlMenuAction(text = stringResource(R.string.select_all), onClick = onSelectAll)
    AccessControlMenuAction(text = stringResource(R.string.select_none), onClick = onSelectNone)
    AccessControlMenuAction(text = stringResource(R.string.select_invert), onClick = onSelectInvert)

    AccessControlMenuSectionTitle(text = stringResource(R.string.filter))
    AccessControlMenuCheckAction(
      text = stringResource(R.string.system_apps),
      checked = showSystemApps,
      onCheckedChange = onUpdateShowSystemApps,
    )

    AccessControlMenuSectionTitle(text = stringResource(R.string.sort))
    AccessControlMenuSortAction(
      text = stringResource(R.string.name),
      checked = sort == AppInfoSort.Label,
      onClick = { onUpdateSort(AppInfoSort.Label) },
    )
    AccessControlMenuSortAction(
      text = stringResource(R.string.package_name),
      checked = sort == AppInfoSort.PackageName,
      onClick = { onUpdateSort(AppInfoSort.PackageName) },
    )
    AccessControlMenuSortAction(
      text = stringResource(R.string.install_time),
      checked = sort == AppInfoSort.InstallTime,
      onClick = { onUpdateSort(AppInfoSort.InstallTime) },
    )
    AccessControlMenuSortAction(
      text = stringResource(R.string.update_time),
      checked = sort == AppInfoSort.UpdateTime,
      onClick = { onUpdateSort(AppInfoSort.UpdateTime) },
    )
    AccessControlMenuCheckAction(
      text = stringResource(R.string.reverse),
      checked = reverse,
      onCheckedChange = onUpdateReverse,
    )

    AccessControlMenuSectionTitle(text = stringResource(R.string.external))
    AccessControlMenuAction(
      text = stringResource(R.string.import_from_clipboard),
      onClick = onImport,
    )
    AccessControlMenuAction(text = stringResource(R.string.export_to_clipboard), onClick = onExport)
  }
}

@Composable
private fun AccessControlMenuSectionTitle(text: String) {
  Text(
    text = text,
    style = MaterialTheme.typography.titleSmall,
    color = MaterialTheme.colorScheme.primary,
    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
  )
}

@Composable
private fun AccessControlMenuAction(text: String, onClick: () -> Unit) {
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(horizontal = 20.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(text = text)
  }
}

@Composable
private fun AccessControlMenuSortAction(text: String, checked: Boolean, onClick: () -> Unit) {
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .selectable(selected = checked, onClick = onClick, role = Role.RadioButton)
        .padding(horizontal = 8.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    RadioButton(selected = checked, onClick = null)
    Text(text = text, modifier = Modifier.padding(start = 8.dp))
  }
}

@Composable
private fun AccessControlMenuCheckAction(
  text: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
) {
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .toggleable(value = checked, onValueChange = onCheckedChange, role = Role.Checkbox)
        .padding(horizontal = 8.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Checkbox(checked = checked, onCheckedChange = null)
    Text(text = text, modifier = Modifier.padding(start = 8.dp))
  }
}

@Composable
private fun AccessControlAppItem(app: AppInfo, selected: Boolean, onClick: () -> Unit) {
  val dimens = mihomoDimens
  val itemMinHeight = dimens.itemMinHeight
  val itemTextMargin = dimens.itemTextMargin

  Row(
    modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(end = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier = Modifier.size(width = 65.dp, height = itemMinHeight),
      contentAlignment = Alignment.Center,
    ) {
      AndroidView(
        factory = { viewContext ->
          ImageView(viewContext).apply { scaleType = ImageView.ScaleType.CENTER_CROP }
        },
        update = { it.setImageDrawable(app.icon) },
        modifier = Modifier.size(dimens.itemHeaderComponentSize),
      )
    }

    Column(
      modifier = Modifier.weight(1f).padding(vertical = dimens.itemPaddingVertical),
      verticalArrangement = Arrangement.Center,
    ) {
      Text(text = app.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
      Spacer(modifier = Modifier.height(itemTextMargin))
      Text(
        text = app.packageName,
        style = MaterialTheme.typography.bodyMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }

    Checkbox(checked = selected, onCheckedChange = { onClick() })
  }
}

interface AccessControlActions {
  fun toggleApp(packageName: String) = Unit

  fun selectAll() = Unit

  fun selectNone() = Unit

  fun selectInvert() = Unit

  fun importFromClipboard() = Unit

  fun exportToClipboard() = Unit

  fun updateSort(sort: AppInfoSort) = Unit

  fun updateReverse(reverse: Boolean) = Unit

  fun updateShowSystemApps(show: Boolean) = Unit
}

@PreviewWrapper(MihomoThemeWrapper::class)
@PreviewMihomo
@Composable
private fun AccessControlContentPreview() {
  AccessControlContent(
    apps =
      listOf(
        AppInfo(
          packageName = "com.example.alpha",
          label = "Alpha",
          icon = Color.Gray.toArgb().toDrawable(),
          installTime = 1_700_000_000_000,
          updateDate = 1_710_000_000_000,
        ),
        AppInfo(
          packageName = "com.example.beta",
          label = "Beta",
          icon = Color.DarkGray.toArgb().toDrawable(),
          installTime = 1_690_000_000_000,
          updateDate = 1_715_000_000_000,
        ),
      ),
    selected = setOf("com.example.alpha"),
    sort = AppInfoSort.Label,
    reverse = false,
    showSystemApps = true,
    actions = object : AccessControlActions {},
  )
}

@PreviewWrapper(MihomoThemeWrapper::class)
@PreviewMihomo
@Composable
private fun AccessControlMenuSheetPreview() {
  Surface {
    Column {
      AccessControlMenuContent(
        sort = AppInfoSort.Label,
        reverse = false,
        showSystemApps = true,
        onSelectAll = {},
        onSelectNone = {},
        onSelectInvert = {},
        onImport = {},
        onExport = {},
        onUpdateSort = {},
        onUpdateReverse = {},
        onUpdateShowSystemApps = {},
      )
    }
  }
}

@PreviewWrapper(MihomoThemeWrapper::class)
@PreviewMihomo
@Composable
private fun AccessControlSearchSheetPreview() {
  Surface {
    Column {
      AccessControlSearchContent(
        apps =
          listOf(
            AppInfo(
              packageName = "com.example.alpha",
              label = "Alpha",
              icon = Color.Gray.toArgb().toDrawable(),
              installTime = 1_700_000_000_000,
              updateDate = 1_710_000_000_000,
            ),
            AppInfo(
              packageName = "com.example.beta",
              label = "Beta",
              icon = Color.DarkGray.toArgb().toDrawable(),
              installTime = 1_690_000_000_000,
              updateDate = 1_715_000_000_000,
            ),
          ),
        selected = setOf("com.example.alpha"),
        onToggleApp = {},
      )
    }
  }
}
