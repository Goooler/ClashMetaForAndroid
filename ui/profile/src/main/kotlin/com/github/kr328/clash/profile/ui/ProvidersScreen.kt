package com.github.kr328.clash.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.core.model.Provider
import com.github.kr328.clash.glue.util.elapsedIntervalString
import com.github.kr328.clash.glue.util.type
import com.github.kr328.clash.profile.R
import com.github.kr328.clash.profile.vm.ProvidersViewModel
import com.github.kr328.clash.profile.vm.ProvidersViewModel.UiState.ProviderItemState
import com.github.kr328.clash.ui.component.Spacer
import com.github.kr328.clash.ui.component.TabbyScaffold
import com.github.kr328.clash.ui.icon.BaselineSwapVert
import com.github.kr328.clash.ui.icon.BaselineSync
import com.github.kr328.clash.ui.icon.TabbyIcons
import com.github.kr328.clash.ui.lifecycle.withLifecycle
import com.github.kr328.clash.ui.theme.PreviewTabby
import com.github.kr328.clash.ui.theme.TabbyThemeWrapper
import com.github.kr328.clash.ui.theme.tabbyDimens
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ProvidersScreen(
  modifier: Modifier = Modifier,
  viewModel: ProvidersViewModel = koinViewModel<ProvidersViewModel>().withLifecycle(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val eventState by viewModel.eventState.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(eventState) {
    when (val event = eventState) {
      Idle -> Unit
      is ShowMessage -> {
        snackbarHostState.showSnackbar(message = event.message)
      }
    }
    viewModel.consumeEvent()
  }

  ProvidersContent(
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    providers = uiState.providers,
    currentTime = uiState.currentTime,
    onUpdateAll = viewModel::onUpdateAll,
    onUpdate = { _, provider -> viewModel.onUpdate(provider) },
  )
}

@Composable
private fun ProvidersContent(
  modifier: Modifier = Modifier,
  snackbarHostState: SnackbarHostState,
  providers: List<ProviderItemState>,
  currentTime: Long,
  onUpdateAll: () -> Unit,
  onUpdate: (Int, Provider) -> Unit,
) {
  TabbyScaffold(
    title = stringResource(CommonR.string.providers),
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    actions = {
      IconButton(onClick = onUpdateAll) {
        Icon(
          imageVector = TabbyIcons.BaselineSync,
          contentDescription = stringResource(R.string.update_all),
        )
      }
    },
  ) { innerPadding ->
    LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      itemsIndexed(
        items = providers,
        key = { _, state -> "${state.provider.type}-${state.provider.name}" },
      ) { index, state ->
        ProviderItem(
          state = state,
          currentTime = currentTime,
          onUpdate = { onUpdate(index, state.provider) },
        )
      }
    }
  }
}

@Composable
private fun ProviderItem(state: ProviderItemState, currentTime: Long, onUpdate: () -> Unit) {
  val context = LocalContext.current
  val dimens = tabbyDimens
  val itemMinHeight = dimens.itemMinHeight
  val itemHeaderMargin = dimens.itemHeaderMargin
  val itemTextMargin = dimens.itemTextMargin

  val canUpdate = state.provider.vehicleType != Inline

  Row(
    modifier =
      Modifier.fillMaxWidth().heightIn(min = itemMinHeight).padding(start = itemHeaderMargin),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(text = state.provider.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
      Spacer(itemTextMargin)
      Text(text = state.provider.type(context), style = MaterialTheme.typography.bodyMedium)
    }

    if (canUpdate) {
      Text(
        text = (currentTime - state.updatedAt).elapsedIntervalString(context),
        modifier = Modifier.padding(end = 10.dp),
      )
      Box(
        modifier =
          Modifier.size(width = 1.dp, height = itemMinHeight)
            .background(MaterialTheme.colorScheme.outline)
      )
      IconButton(
        onClick = onUpdate,
        enabled = !state.updating,
        modifier = Modifier.padding(horizontal = 4.dp),
      ) {
        if (state.updating) {
          CircularProgressIndicator(modifier = Modifier.size(30.dp), strokeWidth = 2.dp)
        } else {
          Icon(
            imageVector = TabbyIcons.BaselineSwapVert,
            contentDescription = stringResource(R.string.update),
          )
        }
      }
    }
  }
}

@PreviewWrapper(TabbyThemeWrapper::class)
@PreviewTabby
@Composable
private fun ProvidersContentPreview() {
  ProvidersContent(
    snackbarHostState = SnackbarHostState(),
    providers =
      listOf(
        ProviderItemState(
          provider =
            Provider(
              name = "Proxy Provider",
              type = Proxy,
              vehicleType = HTTP,
              updatedAt = System.currentTimeMillis() - 10 * 60 * 1000,
            ),
          updatedAt = System.currentTimeMillis() - 10 * 60 * 1000,
          updating = false,
        ),
        ProviderItemState(
          provider =
            Provider(
              name = "Inline Rules",
              type = Rule,
              vehicleType = Inline,
              updatedAt = System.currentTimeMillis() - 60 * 60 * 1000,
            ),
          updatedAt = System.currentTimeMillis() - 60 * 60 * 1000,
          updating = false,
        ),
      ),
    currentTime = System.currentTimeMillis(),
    onUpdateAll = {},
    onUpdate = { _, _ -> },
  )
}
