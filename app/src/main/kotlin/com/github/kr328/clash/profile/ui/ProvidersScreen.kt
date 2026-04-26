package com.github.kr328.clash.profile.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.kr328.clash.R
import com.github.kr328.clash.core.model.Provider
import com.github.kr328.clash.profile.vm.ProvidersViewModel
import com.github.kr328.clash.profile.vm.ProvidersViewModel.UiState.ProviderItemState
import com.github.kr328.clash.ui.component.MihomoScaffold
import com.github.kr328.clash.ui.icon.BaselineSwapVert
import com.github.kr328.clash.ui.icon.BaselineSync
import com.github.kr328.clash.ui.icon.MihomoIcons
import com.github.kr328.clash.ui.theme.MihomoTheme
import com.github.kr328.clash.ui.theme.PreviewMihomo
import com.github.kr328.clash.ui.theme.mihomoDimens
import com.github.kr328.clash.util.elapsedIntervalString
import com.github.kr328.clash.util.type

@Composable
fun ProvidersScreen(modifier: Modifier = Modifier, viewModel: ProvidersViewModel = viewModel()) {
  val lifecycleOwner = LocalLifecycleOwner.current
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val eventState by viewModel.eventState.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }

  DisposableEffect(lifecycleOwner, viewModel) {
    lifecycleOwner.lifecycle.addObserver(viewModel)
    onDispose { lifecycleOwner.lifecycle.removeObserver(viewModel) }
  }

  LaunchedEffect(eventState) {
    when (val event = eventState) {
      ProvidersViewModel.EventState.Idle -> Unit
      is ProvidersViewModel.EventState.ShowMessage -> {
        snackbarHostState.showSnackbar(message = event.message, duration = SnackbarDuration.Long)
      }
    }
    viewModel.consumeEvent()
  }

  Box(modifier = modifier.fillMaxSize()) {
    ProvidersContent(
      providers = uiState.providers,
      currentTime = uiState.currentTime,
      onUpdateAll = viewModel::onUpdateAll,
      onUpdate = { _, provider -> viewModel.onUpdate(provider) },
    )

    SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProvidersContent(
  providers: List<ProviderItemState>,
  currentTime: Long,
  onUpdateAll: () -> Unit,
  onUpdate: (Int, Provider) -> Unit,
) {
  MihomoScaffold(
    title = stringResource(R.string.providers),
    actions = {
      IconButton(onClick = onUpdateAll) {
        Icon(
          imageVector = MihomoIcons.BaselineSync,
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
  val dimens = mihomoDimens
  val itemMinHeight = dimens.itemMinHeight
  val itemHeaderMargin = dimens.itemHeaderMargin
  val itemTextMargin = dimens.itemTextMargin
  val itemMiddleMargin = dimens.itemMiddleMargin

  val canUpdate = state.provider.vehicleType != Provider.VehicleType.Inline

  Row(
    modifier =
      Modifier.fillMaxWidth().heightIn(min = itemMinHeight).padding(start = itemHeaderMargin),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(text = state.provider.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
      Spacer(modifier = Modifier.height(itemTextMargin))
      Text(text = state.provider.type(context), style = MaterialTheme.typography.bodyMedium)
    }

    if (canUpdate) {
      Text(
        text = (currentTime - state.updatedAt).elapsedIntervalString(context),
        modifier = Modifier.padding(end = itemMiddleMargin),
      )
      Box(
        modifier =
          Modifier.size(width = dimens.dividerSize, height = itemMinHeight)
            .background(MaterialTheme.colorScheme.outline)
      )
      IconButton(
        onClick = onUpdate,
        enabled = !state.updating,
        modifier = Modifier.padding(horizontal = 4.dp),
      ) {
        if (state.updating) {
          CircularProgressIndicator(
            modifier = Modifier.size(dimens.itemTrailingComponentSize),
            strokeWidth = 2.dp,
          )
        } else {
          Icon(
            imageVector = MihomoIcons.BaselineSwapVert,
            contentDescription = stringResource(R.string.update),
          )
        }
      }
    }
  }
}

@PreviewMihomo
@Composable
private fun ProvidersContentPreview() = MihomoTheme {
  ProvidersContent(
    providers =
      listOf(
        ProviderItemState(
          provider =
            Provider(
              name = "Proxy Provider",
              type = Provider.Type.Proxy,
              vehicleType = Provider.VehicleType.HTTP,
              updatedAt = System.currentTimeMillis() - 10 * 60 * 1000,
            ),
          updatedAt = System.currentTimeMillis() - 10 * 60 * 1000,
          updating = false,
        ),
        ProviderItemState(
          provider =
            Provider(
              name = "Inline Rules",
              type = Provider.Type.Rule,
              vehicleType = Provider.VehicleType.Inline,
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
