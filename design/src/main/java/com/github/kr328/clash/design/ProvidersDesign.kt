package com.github.kr328.clash.design

import android.content.Context
import android.view.View
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.kr328.clash.core.model.Provider
import com.github.kr328.clash.design.component.MihomoScaffold
import com.github.kr328.clash.design.ui.theme.MihomoTheme
import com.github.kr328.clash.design.ui.theme.PreviewMihomo
import com.github.kr328.clash.design.util.elapsedIntervalString
import com.github.kr328.clash.design.util.type
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class ProvidersDesign(context: Context, providers: List<Provider>) :
  Design<ProvidersDesign.Request>(context) {
  sealed interface Request {
    data class Update(val index: Int, val provider: Provider) : Request
  }

  private val states =
    mutableStateListOf<ProviderItemState>().apply {
      addAll(
        providers.map {
          ProviderItemState(provider = it, updatedAt = it.updatedAt, updating = false)
        }
      )
    }

  override val root: View by composeView {
    MihomoTheme {
      ProvidersScreen(
        states = states,
        onUpdateAll = ::requestUpdateAll,
        onUpdate = { index, provider ->
          states[index] = states[index].copy(updating = true)
          requests.trySend(Request.Update(index, provider))
        },
      )
    }
  }

  suspend fun notifyUpdated(index: Int) =
    withContext(Dispatchers.Main) { states[index] = states[index].copy(updating = false) }

  suspend fun notifyChanged(index: Int) =
    withContext(Dispatchers.Main) {
      states[index] = states[index].copy(updating = false, updatedAt = System.currentTimeMillis())
    }

  private fun requestUpdateAll() {
    states.forEachIndexed { index, state ->
      if (state.updating || state.provider.vehicleType == Provider.VehicleType.Inline) {
        return@forEachIndexed
      }

      states[index] = state.copy(updating = true)
      requests.trySend(Request.Update(index, state.provider))
    }
  }
}

private data class ProviderItemState(
  val provider: Provider,
  val updatedAt: Long,
  val updating: Boolean,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProvidersScreen(
  states: List<ProviderItemState>,
  onUpdateAll: () -> Unit,
  onUpdate: (Int, Provider) -> Unit,
) {
  var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

  LaunchedEffect(Unit) {
    while (true) {
      delay(1.minutes)
      currentTime = System.currentTimeMillis()
    }
  }

  MihomoScaffold(
    title = stringResource(R.string.providers),
    actions = {
      IconButton(onClick = onUpdateAll) {
        Icon(
          painter = painterResource(R.drawable.ic_baseline_sync),
          contentDescription = stringResource(R.string.update_all),
        )
      }
    },
  ) { innerPadding ->
    LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      itemsIndexed(
        items = states,
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
  val itemMinHeight = dimensionResource(R.dimen.item_min_height)
  val itemHeaderMargin = dimensionResource(R.dimen.item_header_margin)
  val itemTextMargin = dimensionResource(R.dimen.item_text_margin)
  val itemMiddleMargin = dimensionResource(R.dimen.item_midden_margin)

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
          Modifier.size(width = dimensionResource(R.dimen.divider_size), height = itemMinHeight)
            .background(MaterialTheme.colorScheme.outline)
      )
      IconButton(
        onClick = onUpdate,
        enabled = !state.updating,
        modifier = Modifier.padding(horizontal = 4.dp),
      ) {
        if (state.updating) {
          CircularProgressIndicator(
            modifier = Modifier.size(dimensionResource(R.dimen.item_tailing_component_size)),
            strokeWidth = 2.dp,
          )
        } else {
          Icon(
            painter = painterResource(R.drawable.ic_baseline_swap_vert),
            contentDescription = stringResource(R.string.update),
          )
        }
      }
    }
  }
}

@PreviewMihomo
@Composable
private fun ProvidersScreenPreview() = MihomoTheme {
  ProvidersScreen(
    states =
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
    onUpdateAll = {},
    onUpdate = { _, _ -> },
  )
}
