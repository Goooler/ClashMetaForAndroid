package com.github.kr328.clash.proxy.ui

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
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.kr328.clash.R
import com.github.kr328.clash.core.model.Proxy
import com.github.kr328.clash.core.model.ProxySort
import com.github.kr328.clash.core.model.TunnelState
import com.github.kr328.clash.proxy.vm.ProxyViewModel
import com.github.kr328.clash.proxy.vm.ProxyViewModel.SelectedProxy
import com.github.kr328.clash.ui.component.MihomoScaffold
import com.github.kr328.clash.ui.theme.MihomoTheme
import com.github.kr328.clash.ui.theme.PreviewMihomo
import com.github.kr328.clash.ui.theme.mihomoDimens
import com.github.kr328.clash.util.toast
import kotlinx.coroutines.launch

@Composable
fun ProxyScreen(
  modifier: Modifier = Modifier,
  viewModel: ProxyViewModel = viewModel(),
  onReLaunch: () -> Unit,
) {
  val lifecycleOwner = LocalLifecycleOwner.current
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val selectedProxies by viewModel.selectedProxies.collectAsStateWithLifecycle()
  val eventState by viewModel.eventState.collectAsStateWithLifecycle()
  val context = LocalContext.current

  DisposableEffect(lifecycleOwner, viewModel) {
    lifecycleOwner.lifecycle.addObserver(viewModel)
    onDispose { lifecycleOwner.lifecycle.removeObserver(viewModel) }
  }

  LaunchedEffect(eventState) {
    when (eventState) {
      ProxyViewModel.EventState.Idle -> Unit
      ProxyViewModel.EventState.ReLaunch -> {
        onReLaunch()
      }
      ProxyViewModel.EventState.ShowModeSwitchTips -> {
        context.toast(R.string.mode_switch_tips)
      }
    }
    viewModel.consumeEvent()
  }

  ProxyContent(
    modifier = modifier,
    uiState = uiState,
    selectedProxies = selectedProxies,
    onPageChanged = viewModel::onPageChanged,
    onUrlTest = viewModel::onUrlTest,
    onExcludeNotSelectableChanged = viewModel::onExcludeNotSelectableChanged,
    onProxyLineChanged = viewModel::onProxyLineChanged,
    onProxySortChanged = viewModel::onProxySortChanged,
    onOverrideModeSelected = viewModel::onOverrideModeSelected,
    onProxySelected = viewModel::onProxySelected,
  )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ProxyContent(
  modifier: Modifier = Modifier,
  uiState: ProxyViewModel.UiState,
  selectedProxies: List<SelectedProxy>,
  onPageChanged: (Int) -> Unit,
  onUrlTest: (Int) -> Unit,
  onExcludeNotSelectableChanged: (Boolean) -> Unit,
  onProxyLineChanged: (Int) -> Unit,
  onProxySortChanged: (ProxySort) -> Unit,
  onOverrideModeSelected: (TunnelState.Mode?) -> Unit,
  onProxySelected: (Int, String) -> Unit,
) {
  var menuVisible by remember { mutableStateOf(false) }
  val currentGroup = uiState.groups.getOrNull(uiState.currentPage)
  val showUrlTestAction = uiState.groupNames.isNotEmpty()

  if (menuVisible) {
    ModalBottomSheet(onDismissRequest = { menuVisible = false }) {
      ProxyMenuSheetContent(
        overrideMode = uiState.overrideMode,
        excludeNotSelectable = uiState.excludeNotSelectable,
        proxyLine = uiState.proxyLine,
        proxySort = uiState.proxySort,
        onExcludeNotSelectableChanged = {
          menuVisible = false
          onExcludeNotSelectableChanged(it)
        },
        onProxyLineChanged = {
          menuVisible = false
          onProxyLineChanged(it)
        },
        onProxySortChanged = {
          menuVisible = false
          onProxySortChanged(it)
        },
        onOverrideModeSelected = {
          menuVisible = false
          onOverrideModeSelected(it)
        },
      )
    }
  }

  MihomoScaffold(
    modifier = modifier,
    title = stringResource(R.string.proxy),
    actions = {
      if (showUrlTestAction) {
        if (currentGroup?.urlTesting == true) {
          CircularProgressIndicator(
            modifier = Modifier.padding(horizontal = 12.dp).size(24.dp),
            strokeWidth = 2.dp,
          )
        } else {
          IconButton(onClick = { onUrlTest(uiState.currentPage) }) {
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
      if (uiState.groupNames.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text(
            text = stringResource(R.string.proxy_empty_tips),
            style = MaterialTheme.typography.titleMedium,
          )
        }
      } else {
        ProxyPagerContent(
          uiState = uiState,
          selectedProxies = selectedProxies,
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
  uiState: ProxyViewModel.UiState,
  selectedProxies: List<SelectedProxy>,
  onPageChanged: (Int) -> Unit,
  onProxySelected: (Int, String) -> Unit,
) {
  val groupNames = uiState.groupNames
  if (groupNames.isEmpty()) return

  val initialPage = uiState.initialPage.coerceIn(groupNames.indices)
  val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { groupNames.size })
  val scope = rememberCoroutineScope()

  LaunchedEffect(pagerState) { snapshotFlow { pagerState.currentPage }.collect(onPageChanged) }

  val currentPage = uiState.currentPage.coerceIn(groupNames.indices)
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
        proxyLine = uiState.proxyLine,
        group = uiState.groups.getOrNull(page) ?: ProxyViewModel.UiState.ProxyGroupUiState(),
        selectedProxies = selectedProxies,
        onProxySelected = onProxySelected,
      )
    }
  }
}

@Composable
private fun ProxyGroupPage(
  index: Int,
  proxyLine: Int,
  group: ProxyViewModel.UiState.ProxyGroupUiState,
  selectedProxies: List<SelectedProxy>,
  onProxySelected: (Int, String) -> Unit,
) {
  val dimens = mihomoDimens
  val sources = group.sources
  val refreshVersion = group.refreshVersion
  val selectedControl = MaterialTheme.colorScheme.onPrimary
  val selectedBackground = MaterialTheme.colorScheme.primary
  val unselectedControl = MaterialTheme.colorScheme.onSurface
  val unselectedBackground = MaterialTheme.colorScheme.surface

  LazyVerticalGrid(
    columns = GridCells.Fixed(columnsForProxyLine(proxyLine)),
    state = rememberLazyGridState(),
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(dimens.proxyContentPaddingGrid3),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    items(count = sources.size, key = { itemIndex -> sources[itemIndex].proxy.name }) { itemIndex ->
      val source = sources[itemIndex]
      val parentNow = selectedProxies.getOrNull(index)
      val linkNow = source.linkIndex.takeIf { it >= 0 }?.let { selectedProxies.getOrNull(it) }
      val item =
        remember(source, refreshVersion, proxyLine, parentNow, linkNow) {
          source.toUiState(
            parentNow = parentNow ?: SelectedProxy("?"),
            linkNow = linkNow,
            proxyLine = proxyLine,
            selectedControl = selectedControl,
            selectedBackground = selectedBackground,
            unselectedControl = unselectedControl,
            unselectedBackground = unselectedBackground,
          )
        }

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
  item: ProxyViewModel.UiState.ProxyItemUiState,
  proxyLine: Int,
  selectable: Boolean,
  onClick: () -> Unit,
) {
  val dimens = mihomoDimens
  val shape =
    RoundedCornerShape(if (proxyLine == 1) dimens.proxyCardOffset else dimens.proxyCardRadius)
  val modifier =
    Modifier.fillMaxWidth()
      .then(if (proxyLine == 1) Modifier else Modifier.shadow(elevation = 2.dp, shape = shape))
      .clip(shape)
      .background(item.background)
      .clickable(enabled = selectable, onClick = onClick)
      .padding(
        horizontal =
          if (proxyLine == 3) dimens.proxyContentPaddingGrid3 else dimens.proxyContentPadding,
        vertical = 14.dp,
      )

  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Text(
        text = item.title,
        color = item.controls,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = item.subtitle,
        color = item.controls,
        style = MaterialTheme.typography.bodySmall,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }

    if (item.delayText.isNotEmpty()) {
      Text(
        text = item.delayText,
        color = item.controls,
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
private fun ProxyContentPreview() = MihomoTheme {
  val groups = remember {
    listOf(
      ProxyViewModel.UiState.ProxyGroupUiState(
        selectable = true,
        sources =
          listOf(
            ProxyViewModel.UiState.ProxyItemSource(
              proxy =
                Proxy(
                  name = "auto",
                  title = "Auto",
                  subtitle = "",
                  type = Proxy.Type.URLTest,
                  delay = 48,
                ),
              linkIndex = 1,
            ),
            ProxyViewModel.UiState.ProxyItemSource(
              proxy =
                Proxy(
                  name = "hk-01",
                  title = "Hong Kong 01",
                  subtitle = "BGP | 1.2x",
                  type = Proxy.Type.Shadowsocks,
                  delay = 62,
                ),
              linkIndex = -1,
            ),
          ),
      )
    )
  }

  ProxyContent(
    selectedProxies = listOf(SelectedProxy("auto"), SelectedProxy("hk-01")),
    uiState =
      ProxyViewModel.UiState(
        groupNames = listOf("Auto"),
        groups = groups,
        currentPage = 0,
        proxyLine = 2,
        excludeNotSelectable = false,
        proxySort = ProxySort.Delay,
        overrideMode = TunnelState.Mode.Rule,
        initialPage = 0,
      ),
    onPageChanged = {},
    onUrlTest = {},
    onExcludeNotSelectableChanged = {},
    onProxyLineChanged = {},
    onProxySortChanged = {},
    onOverrideModeSelected = {},
    onProxySelected = { _, _ -> },
  )
}
