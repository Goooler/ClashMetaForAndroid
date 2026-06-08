package com.github.kr328.clash.proxy.vm

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.core.model.Proxy
import com.github.kr328.clash.core.model.ProxySort
import com.github.kr328.clash.core.model.TunnelState
import com.github.kr328.clash.glue.remote.Remote
import com.github.kr328.clash.glue.store.UiStore
import com.github.kr328.clash.glue.util.withClash
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

internal class ProxyViewModel(private val uiStore: UiStore) :
  ViewModel(), DefaultLifecycleObserver {
  private var broadcastEventsJob: Job? = null
  private var fetchInitialStateJob: Job? = null
  @Volatile private var initialized = false
  // Allow up to 10 concurrent group queries to avoid overwhelming the service
  private val reloadLock = Semaphore(10)

  val uiState: StateFlow<UiState>
    field = MutableStateFlow(UiState())

  val eventState: StateFlow<EventState>
    field = MutableStateFlow<EventState>(EventState.Idle)

  val selectedProxies: StateFlow<List<SelectedProxy>>
    field = MutableStateFlow(emptyList())

  init {
    uiState.update {
      it.copy(
        proxyLine = uiStore.proxyLine,
        excludeNotSelectable = uiStore.proxyExcludeNotSelectable,
        proxySort = uiStore.proxySort,
      )
    }
  }

  override fun onStart(owner: LifecycleOwner) {
    broadcastEventsJob?.cancel()
    broadcastEventsJob = viewModelScope.launch {
      Remote.broadcasts.event.collect { event ->
        when (event) {
          ProfileLoaded -> {
            if (!initialized) return@collect
            val newNames = withClash { queryProxyGroupNames(uiStore.proxyExcludeNotSelectable) }
            if (newNames != uiState.value.groupNames) {
              eventState.value = EventState.ReLaunch
            }
          }
          else -> Unit
        }
      }
    }

    fetchInitialStateJob?.cancel()
    fetchInitialStateJob = viewModelScope.launch { fetchInitialState() }
  }

  override fun onStop(owner: LifecycleOwner) {
    broadcastEventsJob?.cancel()
    broadcastEventsJob = null
    fetchInitialStateJob?.cancel()
    fetchInitialStateJob = null
  }

  fun consumeEvent() {
    eventState.value = EventState.Idle
  }

  private suspend fun fetchInitialState() {
    val mode = withClash { queryOverride(Clash.OverrideSlot.Session).mode }
    val names = withClash { queryProxyGroupNames(uiStore.proxyExcludeNotSelectable) }
    val preservedGroups =
      with(uiState.value) { groups.takeIf { groupNames == names && groups.size == names.size } }

    selectedProxies.value = List(names.size) { SelectedProxy("?") }

    val initialPage = names.indexOf(uiStore.proxyLastGroup).coerceAtLeast(0)
    val currentPage = initialPage.coerceAtMost((names.size - 1).coerceAtLeast(0))

    uiState.update {
      it.copy(
        overrideMode = mode,
        groupNames = names,
        groups = preservedGroups ?: List(names.size) { UiState.ProxyGroupUiState() },
        initialPage = initialPage,
        currentPage = currentPage,
      )
    }

    initialized = true
    reloadAll()
  }

  fun onPageChanged(index: Int) {
    val names = uiState.value.groupNames
    uiState.update { it.copy(currentPage = index) }
    names.getOrNull(index)?.let { uiStore.proxyLastGroup = it }
  }

  fun onExcludeNotSelectableChanged(enabled: Boolean) {
    uiStore.proxyExcludeNotSelectable = enabled
    uiState.update { it.copy(excludeNotSelectable = enabled) }
    eventState.value = EventState.ReLaunch
  }

  fun onProxyLineChanged(line: Int) {
    uiStore.proxyLine = line
    uiState.update { it.copy(proxyLine = line) }
    // Increment refresh version on groups
    uiState.update { current ->
      current.copy(groups = current.groups.map { it.copy(refreshVersion = it.refreshVersion + 1) })
    }
    reloadAll()
  }

  fun onProxySortChanged(sort: ProxySort) {
    uiStore.proxySort = sort
    uiState.update { it.copy(proxySort = sort) }
    reloadAll()
  }

  fun onOverrideModeSelected(mode: TunnelState.Mode?) {
    uiState.update { it.copy(overrideMode = mode) }
    eventState.value = EventState.ShowModeSwitchTips
    viewModelScope.launch {
      withClash {
        val o = queryOverride(Clash.OverrideSlot.Session)
        patchOverride(Clash.OverrideSlot.Session, o.copy(mode = mode))
      }
    }
  }

  fun onUrlTest(index: Int) {
    val names = uiState.value.groupNames
    if (names.isEmpty() || index !in names.indices) return

    updateGroupState(index) { it.copy(urlTesting = true) }

    viewModelScope.launch {
      withClash { healthCheck(names[index]) }
      reload(index)
    }
  }

  fun onProxySelected(index: Int, name: String) {
    val names = uiState.value.groupNames
    if (index !in names.indices) return

    viewModelScope.launch {
      withClash { patchSelector(names[index], name) }
      selectedProxies.update { list ->
        list.toMutableList().apply { set(index, SelectedProxy(name)) }
      }
      // trigger refresh version to redraw
      updateGroupState(index) { it.copy(refreshVersion = it.refreshVersion + 1) }
    }
  }

  fun onProxyDelayTest(index: Int, name: String) {
    val names = uiState.value.groupNames
    if (index !in names.indices) return

    updateGroupState(index) {
      it.copy(delayTestingKeys = it.delayTestingKeys + name, refreshVersion = it.refreshVersion + 1)
    }

    viewModelScope.launch {
      try {
        withClash { healthCheckProxy(names[index], name) }
        reload(index)
      } finally {
        updateGroupState(index) {
          it.copy(
            delayTestingKeys = it.delayTestingKeys - name,
            refreshVersion = it.refreshVersion + 1,
          )
        }
      }
    }
  }

  fun reloadAll() {
    val names = uiState.value.groupNames
    names.indices.forEach { idx -> reload(idx) }
  }

  private fun reload(index: Int) {
    viewModelScope.launch {
      val names = uiState.value.groupNames
      if (index !in names.indices) return@launch

      val sort = uiStore.proxySort

      val group = reloadLock.withPermit { withClash { queryProxyGroup(names[index], sort) } }

      selectedProxies.update { list ->
        list.toMutableList().apply { set(index, SelectedProxy(group.now)) }
      }

      val sources =
        withContext(Dispatchers.Default) {
          val nameIndexMap = names.withIndex().associate { (index, name) -> name to index }
          group.proxies.map { proxy ->
            UiState.ProxyItemSource(
              proxy = proxy,
              linkIndex = if (proxy.type.group) nameIndexMap[proxy.name] ?: -1 else -1,
            )
          }
        }

      updateGroupState(index) {
        it.copy(
          selectable = group.type == Proxy.Type.Selector,
          urlTesting = false,
          sources = sources,
          delayTestingKeys =
            it.delayTestingKeys.intersect(sources.mapTo(mutableSetOf()) { s -> s.proxy.name }),
          refreshVersion = it.refreshVersion + 1,
        )
      }
    }
  }

  private fun updateGroupState(
    index: Int,
    transform: (UiState.ProxyGroupUiState) -> UiState.ProxyGroupUiState,
  ) {
    uiState.update { current ->
      if (index !in current.groups.indices) return@update current
      val newGroups = current.groups.toMutableList()
      newGroups[index] = transform(newGroups[index])
      current.copy(groups = newGroups)
    }
  }

  data class UiState(
    val groupNames: List<String> = emptyList(),
    val groups: List<ProxyGroupUiState> = emptyList(),
    val currentPage: Int = 0,
    val proxyLine: Int = 0,
    val excludeNotSelectable: Boolean = false,
    val proxySort: ProxySort = ProxySort.Default,
    val overrideMode: TunnelState.Mode? = null,
    val initialPage: Int = 0,
  ) {
    data class ProxyGroupUiState(
      val selectable: Boolean = false,
      val urlTesting: Boolean = false,
      val sources: List<ProxyItemSource> = emptyList(),
      val delayTestingKeys: Set<String> = emptySet(),
      val refreshVersion: Int = 0,
    )

    data class ProxyItemSource(val proxy: Proxy, val linkIndex: Int) {
      fun toUiState(
        parentNow: SelectedProxy?,
        linkNow: SelectedProxy?,
        proxyLine: Int,
        selectedControl: Color,
        selectedBackground: Color,
        unselectedControl: Color,
        unselectedBackground: Color,
        delayTesting: Boolean,
      ): ProxyItemUiState {
        val selected = proxy.name == parentNow?.name
        val background =
          if (selected) {
            selectedBackground
          } else if (proxyLine == 1) {
            Color.Transparent
          } else {
            unselectedBackground
          }
        val controls = if (selected) selectedControl else unselectedControl
        val title = if (proxy.type.group) proxy.name else proxy.title
        val subtitle =
          if (proxy.type.group) {
            if (linkNow == null) {
              proxy.type.name
            } else {
              "%s(%s)".format(proxy.type.name, linkNow.name.ifEmpty { "*" })
            }
          } else {
            proxy.subtitle
          }
        val delayText =
          when {
            delayTesting -> "···"
            proxy.delay in 0..Short.MAX_VALUE -> proxy.delay.toString()
            else -> "--"
          }
        return ProxyItemUiState(
          key = proxy.name,
          title = title,
          subtitle = subtitle,
          delayText = delayText,
          delayTesting = delayTesting,
          selected = selected,
          background = background,
          controls = controls,
        )
      }
    }

    data class ProxyItemUiState(
      val key: String,
      val title: String,
      val subtitle: String,
      val delayText: String,
      val delayTesting: Boolean,
      val selected: Boolean,
      val background: Color,
      val controls: Color,
    )
  }

  @JvmInline value class SelectedProxy(val name: String)

  sealed interface EventState {
    data object Idle : EventState

    data object ReLaunch : EventState

    data object ShowModeSwitchTips : EventState
  }
}
