package com.github.kr328.clash.design

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.kr328.clash.core.model.Proxy
import com.github.kr328.clash.core.model.ProxySort
import com.github.kr328.clash.core.model.TunnelState
import com.github.kr328.clash.design.component.MihomoScaffold
import com.github.kr328.clash.design.component.ProxyViewConfig
import com.github.kr328.clash.design.component.ProxyViewState
import com.github.kr328.clash.design.model.ProxyState
import com.github.kr328.clash.design.store.UiStore
import com.github.kr328.clash.design.ui.theme.MihomoTheme
import com.github.kr328.clash.design.ui.theme.PreviewMihomo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProxyDesign(
  context: Context,
  private val overrideMode: TunnelState.Mode?,
  private val groupNames: List<String>,
  private val uiStore: UiStore,
) : Design<ProxyDesign.Request>(context) {
  sealed class Request {
    object ReloadAll : Request()

    object ReLaunch : Request()

    data class PatchMode(val mode: TunnelState.Mode?) : Request()

    data class Reload(val index: Int) : Request()

    data class Select(val index: Int, val name: String) : Request()

    data class UrlTest(val index: Int) : Request()
  }

  private val config = ProxyViewConfig(context, uiStore.proxyLine)
  private val groups = List(groupNames.size) { ProxyGroupUiState() }
  private val initialPage = groupNames.indexOf(uiStore.proxyLastGroup).coerceAtLeast(0)

  private var currentPage by
    mutableIntStateOf(initialPage.coerceAtMost((groupNames.size - 1).coerceAtLeast(0)))
  private var proxyLine by mutableIntStateOf(uiStore.proxyLine)
  private var excludeNotSelectable by mutableStateOf(uiStore.proxyExcludeNotSelectable)
  private var proxySort by mutableStateOf(uiStore.proxySort)
  private var selectedMode by mutableStateOf(overrideMode)

  override val root: View by composeView {
    MihomoTheme {
      ProxyScreen(
        groupNames = groupNames,
        groups = groups,
        currentPage = currentPage,
        proxyLine = proxyLine,
        excludeNotSelectable = excludeNotSelectable,
        proxySort = proxySort,
        overrideMode = selectedMode,
        initialPage = initialPage,
        onPageChanged = { index ->
          currentPage = index
          groupNames.getOrNull(index)?.let { uiStore.proxyLastGroup = it }
        },
        onUrlTest = ::requestUrlTesting,
        onExcludeNotSelectableChanged = { enabled ->
          excludeNotSelectable = enabled
          uiStore.proxyExcludeNotSelectable = enabled
          requests.trySend(Request.ReLaunch)
        },
        onProxyLineChanged = { line ->
          proxyLine = line
          uiStore.proxyLine = line
          config.proxyLine = line
          requests.trySend(Request.ReloadAll)
        },
        onProxySortChanged = { sort ->
          proxySort = sort
          uiStore.proxySort = sort
          requests.trySend(Request.ReloadAll)
        },
        onOverrideModeSelected = { mode ->
          selectedMode = mode
          requests.trySend(Request.PatchMode(mode))
        },
        onProxySelected = { index, name -> requests.trySend(Request.Select(index, name)) },
      )
    }
  }

  suspend fun updateGroup(
    position: Int,
    proxies: List<Proxy>,
    selectable: Boolean,
    parent: ProxyState,
    links: Map<String, ProxyState>,
  ) {
    val states =
      withContext(Dispatchers.Default) {
        proxies.map { proxy ->
          ProxyViewState(config, proxy, parent, if (proxy.type.group) links[proxy.name] else null)
        }
      }

    withContext(Dispatchers.Main) {
      groups[position].apply {
        rawStates = states
        this.selectable = selectable
        urlTesting = false
        refresh()
      }
    }
  }

  suspend fun requestRedrawVisible() =
    withContext(Dispatchers.Main) { groups.forEach(ProxyGroupUiState::refresh) }

  suspend fun showModeSwitchTips() =
    withContext(Dispatchers.Main) {
      Toast.makeText(context, R.string.mode_switch_tips, Toast.LENGTH_LONG).show()
    }

  fun requestUrlTesting() {
    if (groups.isEmpty()) return

    val page = currentPage.coerceIn(groups.indices)

    groups[page].urlTesting = true
    requests.trySend(Request.UrlTest(page))
  }
}

private data class ProxyItemUiState(
  val key: String,
  val title: String,
  val subtitle: String,
  val delayText: String,
  @get:ColorInt val background: Int,
  val controls: Int,
)

private class ProxyGroupUiState {
  var items by mutableStateOf<List<ProxyItemUiState>>(emptyList())
  var selectable by mutableStateOf(false)
  var urlTesting by mutableStateOf(false)
  var rawStates: List<ProxyViewState> = emptyList()

  fun refresh() {
    items = rawStates.map { state ->
      state.update(true)

      ProxyItemUiState(
        key = state.proxy.name,
        title = state.title,
        subtitle = state.subtitle,
        delayText = state.delayText,
        background = state.background,
        controls = state.controls,
      )
    }
  }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ProxyScreen(
  groupNames: List<String>,
  groups: List<ProxyGroupUiState>,
  currentPage: Int,
  proxyLine: Int,
  excludeNotSelectable: Boolean,
  proxySort: ProxySort,
  overrideMode: TunnelState.Mode?,
  initialPage: Int,
  onPageChanged: (Int) -> Unit,
  onUrlTest: () -> Unit,
  onExcludeNotSelectableChanged: (Boolean) -> Unit,
  onProxyLineChanged: (Int) -> Unit,
  onProxySortChanged: (ProxySort) -> Unit,
  onOverrideModeSelected: (TunnelState.Mode?) -> Unit,
  onProxySelected: (Int, String) -> Unit,
) {
  var menuVisible by remember { mutableStateOf(false) }
  val currentGroup = groups.getOrNull(currentPage)
  val showUrlTestAction = groupNames.isNotEmpty()

  if (menuVisible) {
    ModalBottomSheet(onDismissRequest = { menuVisible = false }) {
      ProxyMenuSheetContent(
        overrideMode = overrideMode,
        excludeNotSelectable = excludeNotSelectable,
        proxyLine = proxyLine,
        proxySort = proxySort,
        onExcludeNotSelectableChanged = onExcludeNotSelectableChanged,
        onProxyLineChanged = onProxyLineChanged,
        onProxySortChanged = onProxySortChanged,
        onOverrideModeSelected = onOverrideModeSelected,
      )
    }
  }

  MihomoScaffold(
    title = stringResource(R.string.proxy),
    actions = {
      if (showUrlTestAction) {
        if (currentGroup?.urlTesting == true) {
          CircularProgressIndicator(
            modifier = Modifier.padding(horizontal = 12.dp).size(24.dp),
            strokeWidth = 2.dp,
          )
        } else {
          IconButton(onClick = onUrlTest) {
            Icon(
              painter = painterResource(R.drawable.ic_baseline_flash_on),
              contentDescription = stringResource(R.string.delay_test),
            )
          }
        }
      }

      IconButton(onClick = { menuVisible = true }) {
        Icon(
          painter = painterResource(R.drawable.ic_baseline_more_vert),
          contentDescription = stringResource(R.string.more),
        )
      }
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      if (groupNames.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text(
            text = stringResource(R.string.proxy_empty_tips),
            style = MaterialTheme.typography.titleMedium,
          )
        }
      } else {
        ProxyPagerContent(
          groupNames = groupNames,
          groups = groups,
          proxyLine = proxyLine,
          initialPage = initialPage,
          currentPage = currentPage,
          onPageChanged = onPageChanged,
          onProxySelected = onProxySelected,
        )
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProxyPagerContent(
  groupNames: List<String>,
  groups: List<ProxyGroupUiState>,
  proxyLine: Int,
  initialPage: Int,
  currentPage: Int,
  onPageChanged: (Int) -> Unit,
  onProxySelected: (Int, String) -> Unit,
) {
  val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { groupNames.size })
  val scope = rememberCoroutineScope()

  LaunchedEffect(pagerState) { snapshotFlow { pagerState.currentPage }.collect(onPageChanged) }

  LaunchedEffect(currentPage) {
    if (currentPage != pagerState.currentPage) pagerState.scrollToPage(currentPage)
  }

  Column(modifier = Modifier.fillMaxSize()) {
    PrimaryScrollableTabRow(selectedTabIndex = pagerState.currentPage, edgePadding = 0.dp) {
      groupNames.forEachIndexed { index, name ->
        Tab(
          selected = pagerState.currentPage == index,
          onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
          text = { Text(text = name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        )
      }
    }

    HorizontalDivider()

    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
      ProxyGroupPage(
        index = page,
        proxyLine = proxyLine,
        group = groups[page],
        onProxySelected = onProxySelected,
      )
    }
  }
}

@Composable
private fun ProxyGroupPage(
  index: Int,
  proxyLine: Int,
  group: ProxyGroupUiState,
  onProxySelected: (Int, String) -> Unit,
) {
  LazyVerticalGrid(
    columns = GridCells.Fixed(columnsForProxyLine(proxyLine)),
    state = rememberLazyGridState(),
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(12.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    items(count = group.items.size, key = { group.items[it].key }) { itemIndex ->
      val item = group.items[itemIndex]

      ProxyItemCard(
        item = item,
        proxyLine = proxyLine,
        selectable = group.selectable,
        onClick = { onProxySelected(index, item.key) },
      )
    }
  }
}

@Composable
private fun ProxyItemCard(
  item: ProxyItemUiState,
  proxyLine: Int,
  selectable: Boolean,
  onClick: () -> Unit,
) {
  val shape = RoundedCornerShape(if (proxyLine == 1) 0.dp else 5.dp)
  val modifier =
    Modifier.fillMaxWidth()
      .then(if (proxyLine == 1) Modifier else Modifier.shadow(elevation = 2.dp, shape = shape))
      .clip(shape)
      .background(Color(item.background))
      .clickable(enabled = selectable, onClick = onClick)
      .padding(horizontal = if (proxyLine == 3) 12.dp else 15.dp, vertical = 14.dp)

  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Text(
        text = item.title,
        color = Color(item.controls),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = item.subtitle,
        color = Color(item.controls),
        style = MaterialTheme.typography.bodySmall,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }

    if (item.delayText.isNotEmpty()) {
      Text(
        text = item.delayText,
        color = Color(item.controls),
        style = MaterialTheme.typography.bodyMedium,
        maxLines = 1,
      )
    }
  }
}

@Composable
private fun ColumnScope.ProxyMenuSheetContent(
  overrideMode: TunnelState.Mode?,
  excludeNotSelectable: Boolean,
  proxyLine: Int,
  proxySort: ProxySort,
  onExcludeNotSelectableChanged: (Boolean) -> Unit,
  onProxyLineChanged: (Int) -> Unit,
  onProxySortChanged: (ProxySort) -> Unit,
  onOverrideModeSelected: (TunnelState.Mode?) -> Unit,
) {
  ProxyMenuSection(title = stringResource(R.string.filter)) {
    ProxyMenuCheckboxRow(
      title = stringResource(R.string.not_selectable),
      checked = excludeNotSelectable,
      onClick = { onExcludeNotSelectableChanged(!excludeNotSelectable) },
    )
  }

  ProxyMenuSection(title = stringResource(R.string.mode)) {
    ProxyMenuRadioRow(
      title = stringResource(R.string.dont_modify),
      selected = overrideMode == null,
      onClick = { onOverrideModeSelected(null) },
    )
    ProxyMenuRadioRow(
      title = stringResource(R.string.direct_mode),
      selected = overrideMode == TunnelState.Mode.Direct,
      onClick = { onOverrideModeSelected(TunnelState.Mode.Direct) },
    )
    ProxyMenuRadioRow(
      title = stringResource(R.string.global_mode),
      selected = overrideMode == TunnelState.Mode.Global,
      onClick = { onOverrideModeSelected(TunnelState.Mode.Global) },
    )
    ProxyMenuRadioRow(
      title = stringResource(R.string.rule_mode),
      selected = overrideMode == TunnelState.Mode.Rule,
      onClick = { onOverrideModeSelected(TunnelState.Mode.Rule) },
    )
  }

  ProxyMenuSection(title = stringResource(R.string.layout)) {
    ProxyMenuRadioRow(
      title = stringResource(R.string.single),
      selected = proxyLine == 1,
      onClick = { onProxyLineChanged(1) },
    )
    ProxyMenuRadioRow(
      title = stringResource(R.string.doubles),
      selected = proxyLine == 2,
      onClick = { onProxyLineChanged(2) },
    )
    ProxyMenuRadioRow(
      title = stringResource(R.string.multiple),
      selected = proxyLine == 3,
      onClick = { onProxyLineChanged(3) },
    )
  }

  ProxyMenuSection(title = stringResource(R.string.sort)) {
    ProxyMenuRadioRow(
      title = stringResource(R.string.default_),
      selected = proxySort == ProxySort.Default,
      onClick = { onProxySortChanged(ProxySort.Default) },
    )
    ProxyMenuRadioRow(
      title = stringResource(R.string.name),
      selected = proxySort == ProxySort.Title,
      onClick = { onProxySortChanged(ProxySort.Title) },
    )
    ProxyMenuRadioRow(
      title = stringResource(R.string.delay),
      selected = proxySort == ProxySort.Delay,
      onClick = { onProxySortChanged(ProxySort.Delay) },
    )
  }

  Spacer(modifier = Modifier.height(24.dp))
}

@Composable
private fun ProxyMenuSection(title: String, content: @Composable () -> Unit) {
  Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleSmall,
      color = MaterialTheme.colorScheme.primary,
      modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
    )
    content()
    Spacer(modifier = Modifier.height(8.dp))
  }
}

@Composable
private fun ProxyMenuCheckboxRow(title: String, checked: Boolean, onClick: () -> Unit) {
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .toggleable(value = checked, onValueChange = { onClick() }, role = Role.Checkbox)
        .padding(vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Checkbox(checked = checked, onCheckedChange = null)
    Spacer(modifier = Modifier.width(12.dp))
    Text(text = title, style = MaterialTheme.typography.bodyLarge)
  }
}

@Composable
private fun ProxyMenuRadioRow(title: String, selected: Boolean, onClick: () -> Unit) {
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
        .padding(vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    RadioButton(selected = selected, onClick = null)
    Spacer(modifier = Modifier.width(12.dp))
    Text(text = title, style = MaterialTheme.typography.bodyLarge)
  }
}

private fun columnsForProxyLine(proxyLine: Int): Int =
  when (proxyLine) {
    1 -> 1
    2 -> 2
    else -> 3
  }

@PreviewMihomo
@Composable
private fun ProxyScreenPreview() = MihomoTheme {
  val selectedBackground = MaterialTheme.colorScheme.primary.toArgb()
  val selectedControls = MaterialTheme.colorScheme.onPrimary.toArgb()
  val unselectedBackground = MaterialTheme.colorScheme.surface.toArgb()
  val unselectedControls = MaterialTheme.colorScheme.onSurface.toArgb()
  val groups =
    remember(selectedBackground, selectedControls, unselectedBackground, unselectedControls) {
      listOf(
        ProxyGroupUiState().apply {
          selectable = true
          items =
            listOf(
              ProxyItemUiState(
                key = "auto",
                title = "Auto",
                subtitle = "URLTest(HK-01)",
                delayText = "48",
                background = selectedBackground,
                controls = selectedControls,
              ),
              ProxyItemUiState(
                key = "hk-01",
                title = "Hong Kong 01",
                subtitle = "BGP | 1.2x",
                delayText = "62",
                background = unselectedBackground,
                controls = unselectedControls,
              ),
              ProxyItemUiState(
                key = "jp-01",
                title = "Japan 01",
                subtitle = "Tokyo | IPLC",
                delayText = "89",
                background = unselectedBackground,
                controls = unselectedControls,
              ),
              ProxyItemUiState(
                key = "sg-01",
                title = "Singapore 01",
                subtitle = "Premium",
                delayText = "74",
                background = unselectedBackground,
                controls = unselectedControls,
              ),
            )
        },
        ProxyGroupUiState().apply {
          selectable = true
          urlTesting = true
          items =
            listOf(
              ProxyItemUiState(
                key = "fallback-a",
                title = "Fallback A",
                subtitle = "Selector(Node-2)",
                delayText = "128",
                background = unselectedBackground,
                controls = unselectedControls,
              ),
              ProxyItemUiState(
                key = "fallback-b",
                title = "Fallback B",
                subtitle = "Selector(Node-4)",
                delayText = "156",
                background = unselectedBackground,
                controls = unselectedControls,
              ),
            )
        },
      )
    }

  ProxyScreen(
    groupNames = listOf("Auto", "Fallback"),
    groups = groups,
    currentPage = 0,
    proxyLine = 2,
    excludeNotSelectable = false,
    proxySort = ProxySort.Delay,
    overrideMode = TunnelState.Mode.Rule,
    initialPage = 0,
    onPageChanged = {},
    onUrlTest = {},
    onExcludeNotSelectableChanged = {},
    onProxyLineChanged = {},
    onProxySortChanged = {},
    onOverrideModeSelected = {},
    onProxySelected = { _, _ -> },
  )
}

@PreviewMihomo
@Composable
private fun ProxyScreenEmptyPreview() = MihomoTheme {
  ProxyScreen(
    groupNames = emptyList(),
    groups = emptyList(),
    currentPage = 0,
    proxyLine = 2,
    excludeNotSelectable = false,
    proxySort = ProxySort.Default,
    overrideMode = null,
    initialPage = 0,
    onPageChanged = {},
    onUrlTest = {},
    onExcludeNotSelectableChanged = {},
    onProxyLineChanged = {},
    onProxySortChanged = {},
    onOverrideModeSelected = {},
    onProxySelected = { _, _ -> },
  )
}

@PreviewMihomo
@Composable
private fun ProxyMenuSheetContentPreview() = MihomoTheme {
  Surface {
    Column {
      ProxyMenuSheetContent(
        overrideMode = TunnelState.Mode.Rule,
        excludeNotSelectable = false,
        proxyLine = 2,
        proxySort = ProxySort.Delay,
        onExcludeNotSelectableChanged = {},
        onProxyLineChanged = {},
        onProxySortChanged = {},
        onOverrideModeSelected = {},
      )
    }
  }
}
