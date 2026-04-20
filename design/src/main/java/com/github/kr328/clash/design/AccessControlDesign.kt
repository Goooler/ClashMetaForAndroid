package com.github.kr328.clash.design

import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toDrawable
import com.github.kr328.clash.design.component.MihomoScaffold
import com.github.kr328.clash.design.model.AppInfo
import com.github.kr328.clash.design.model.AppInfoSort
import com.github.kr328.clash.design.store.UiStore
import com.github.kr328.clash.design.ui.theme.MihomoTheme
import com.github.kr328.clash.design.ui.theme.PreviewMihomo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AccessControlDesign(
  context: Context,
  uiStore: UiStore,
  private val selected: MutableSet<String>,
) : Design<AccessControlDesign.Request>(context) {
  enum class Request {
    ReloadApps,
    SelectAll,
    SelectNone,
    SelectInvert,
    Import,
    Export,
  }

  private var appsState by mutableStateOf<List<AppInfo>>(emptyList())
  private val selectedState = mutableStateSetOf<String>().apply { addAll(selected) }

  val apps: List<AppInfo>
    get() = appsState

  override val root: View by composeView {
    MihomoTheme {
      AccessControlScreen(
        apps = appsState,
        selected = selectedState,
        initialSort = uiStore.accessControlSort,
        initialReverse = uiStore.accessControlReverse,
        initialHideSystemApps = !uiStore.accessControlSystemApp,
        onToggleApp = ::toggleApp,
        onSelectAll = { requests.trySend(Request.SelectAll) },
        onSelectNone = { requests.trySend(Request.SelectNone) },
        onSelectInvert = { requests.trySend(Request.SelectInvert) },
        onImport = { requests.trySend(Request.Import) },
        onExport = { requests.trySend(Request.Export) },
        onUpdateSort = {
          uiStore.accessControlSort = it
          requests.trySend(Request.ReloadApps)
        },
        onUpdateReverse = {
          uiStore.accessControlReverse = it
          requests.trySend(Request.ReloadApps)
        },
        onUpdateHideSystemApps = {
          uiStore.accessControlSystemApp = !it
          requests.trySend(Request.ReloadApps)
        },
      )
    }
  }

  suspend fun patchApps(apps: List<AppInfo>) = withContext(Dispatchers.Main) { appsState = apps }

  suspend fun rebindAll() =
    withContext(Dispatchers.Main) {
      selectedState.clear()
      selectedState.addAll(selected)
    }

  private fun toggleApp(packageName: String) {
    if (packageName in selectedState) {
      selectedState.remove(packageName)
      selected.remove(packageName)
    } else {
      selectedState.add(packageName)
      selected.add(packageName)
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccessControlScreen(
  apps: List<AppInfo>,
  selected: Set<String>,
  initialSort: AppInfoSort,
  initialReverse: Boolean,
  initialHideSystemApps: Boolean,
  onToggleApp: (String) -> Unit,
  onSelectAll: () -> Unit,
  onSelectNone: () -> Unit,
  onSelectInvert: () -> Unit,
  onImport: () -> Unit,
  onExport: () -> Unit,
  onUpdateSort: (AppInfoSort) -> Unit,
  onUpdateReverse: (Boolean) -> Unit,
  onUpdateHideSystemApps: (Boolean) -> Unit,
) {
  var sort by rememberSaveable { mutableStateOf(initialSort) }
  var reverse by rememberSaveable { mutableStateOf(initialReverse) }
  var hideSystemApps by rememberSaveable { mutableStateOf(initialHideSystemApps) }
  var showSearch by rememberSaveable { mutableStateOf(false) }
  var showMenu by rememberSaveable { mutableStateOf(false) }

  if (showMenu) {
    AccessControlMenuSheet(
      sort = sort,
      reverse = reverse,
      hideSystemApps = hideSystemApps,
      onDismiss = { showMenu = false },
      onSelectAll = {
        showMenu = false
        onSelectAll()
      },
      onSelectNone = {
        showMenu = false
        onSelectNone()
      },
      onSelectInvert = {
        showMenu = false
        onSelectInvert()
      },
      onImport = {
        showMenu = false
        onImport()
      },
      onExport = {
        showMenu = false
        onExport()
      },
      onUpdateSort = {
        sort = it
        onUpdateSort(it)
      },
      onUpdateReverse = {
        reverse = it
        onUpdateReverse(it)
      },
      onUpdateHideSystemApps = {
        hideSystemApps = it
        onUpdateHideSystemApps(it)
      },
    )
  }

  if (showSearch) {
    AccessControlSearchSheet(
      apps = apps,
      selected = selected,
      onToggleApp = onToggleApp,
      onDismiss = { showSearch = false },
    )
  }

  MihomoScaffold(
    title = stringResource(R.string.access_control_packages),
    actions = {
      IconButton(onClick = { showSearch = true }) {
        Icon(
          painter = painterResource(R.drawable.ic_baseline_search),
          contentDescription = stringResource(R.string.search),
        )
      }
      IconButton(onClick = { showMenu = true }) {
        Icon(
          painter = painterResource(R.drawable.ic_baseline_more_vert),
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
          onClick = { onToggleApp(app.packageName) },
        )
        HorizontalDivider()
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccessControlMenuSheet(
  sort: AppInfoSort,
  reverse: Boolean,
  hideSystemApps: Boolean,
  modifier: Modifier = Modifier,
  onDismiss: () -> Unit,
  onSelectAll: () -> Unit,
  onSelectNone: () -> Unit,
  onSelectInvert: () -> Unit,
  onImport: () -> Unit,
  onExport: () -> Unit,
  onUpdateSort: (AppInfoSort) -> Unit,
  onUpdateReverse: (Boolean) -> Unit,
  onUpdateHideSystemApps: (Boolean) -> Unit,
) {
  ModalBottomSheet(
    modifier = modifier,
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState(),
  ) {
    AccessControlMenuContent(
      sort = sort,
      reverse = reverse,
      hideSystemApps = hideSystemApps,
      onSelectAll = onSelectAll,
      onSelectNone = onSelectNone,
      onSelectInvert = onSelectInvert,
      onImport = onImport,
      onExport = onExport,
      onUpdateSort = onUpdateSort,
      onUpdateReverse = onUpdateReverse,
      onUpdateHideSystemApps = onUpdateHideSystemApps,
    )
    Spacer(modifier = Modifier.height(16.dp))
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccessControlSearchSheet(
  apps: List<AppInfo>,
  selected: Set<String>,
  onToggleApp: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
  ) {
    AccessControlSearchContent(apps = apps, selected = selected, onToggleApp = onToggleApp)
  }
}

@Composable
private fun AccessControlSearchContent(
  apps: List<AppInfo>,
  selected: Set<String>,
  onToggleApp: (String) -> Unit,
) {
  var keyword by rememberSaveable { mutableStateOf("") }
  val filtered =
    remember(apps, keyword) {
      if (keyword.isBlank()) {
        emptyList()
      } else {
        apps.filter {
          it.label.contains(keyword, ignoreCase = true) ||
            it.packageName.contains(keyword, ignoreCase = true)
        }
      }
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
private fun AccessControlMenuContent(
  sort: AppInfoSort,
  reverse: Boolean,
  hideSystemApps: Boolean,
  onSelectAll: () -> Unit,
  onSelectNone: () -> Unit,
  onSelectInvert: () -> Unit,
  onImport: () -> Unit,
  onExport: () -> Unit,
  onUpdateSort: (AppInfoSort) -> Unit,
  onUpdateReverse: (Boolean) -> Unit,
  onUpdateHideSystemApps: (Boolean) -> Unit,
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    AccessControlMenuAction(text = stringResource(R.string.select_all), onClick = onSelectAll)
    AccessControlMenuAction(text = stringResource(R.string.select_none), onClick = onSelectNone)
    AccessControlMenuAction(text = stringResource(R.string.select_invert), onClick = onSelectInvert)

    AccessControlMenuSectionTitle(text = stringResource(R.string.filter))
    AccessControlMenuCheckAction(
      text = stringResource(R.string.system_apps),
      checked = hideSystemApps,
      onCheckedChange = onUpdateHideSystemApps,
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
        .clickable(onClick = onClick)
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
        .clickable(onClick = { onCheckedChange(!checked) })
        .padding(horizontal = 8.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Checkbox(checked = checked, onCheckedChange = null)
    Text(text = text, modifier = Modifier.padding(start = 8.dp))
  }
}

@Composable
private fun AccessControlAppItem(app: AppInfo, selected: Boolean, onClick: () -> Unit) {
  val itemMinHeight = dimensionResource(R.dimen.item_min_height)
  val itemTextMargin = dimensionResource(R.dimen.item_text_margin)

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
        modifier = Modifier.size(dimensionResource(R.dimen.item_header_component_size)),
      )
    }

    Column(
      modifier =
        Modifier.weight(1f).padding(vertical = dimensionResource(R.dimen.item_padding_vertical)),
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

@PreviewMihomo
@Composable
private fun AccessControlScreenPreview() = MihomoTheme {
  AccessControlScreen(
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
    initialSort = AppInfoSort.Label,
    initialReverse = false,
    initialHideSystemApps = false,
    onToggleApp = {},
    onSelectAll = {},
    onSelectNone = {},
    onSelectInvert = {},
    onImport = {},
    onExport = {},
    onUpdateSort = {},
    onUpdateReverse = {},
    onUpdateHideSystemApps = {},
  )
}

@PreviewMihomo
@Composable
private fun AccessControlMenuSheetPreview() = MihomoTheme {
  AccessControlMenuContent(
    sort = AppInfoSort.Label,
    reverse = false,
    hideSystemApps = false,
    onSelectAll = {},
    onSelectNone = {},
    onSelectInvert = {},
    onImport = {},
    onExport = {},
    onUpdateSort = {},
    onUpdateReverse = {},
    onUpdateHideSystemApps = {},
  )
}

@PreviewMihomo
@Composable
private fun AccessControlSearchSheetPreview() = MihomoTheme {
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
