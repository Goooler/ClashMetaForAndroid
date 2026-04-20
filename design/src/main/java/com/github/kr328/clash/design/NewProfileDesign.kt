package com.github.kr328.clash.design

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.core.graphics.drawable.toBitmap
import com.github.kr328.clash.common.compat.getDrawableCompat
import com.github.kr328.clash.design.component.MihomoScaffold
import com.github.kr328.clash.design.model.ProfileProvider
import com.github.kr328.clash.design.ui.theme.MihomoTheme
import com.github.kr328.clash.design.ui.theme.PreviewMihomo
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NewProfileDesign(context: Context) : Design<NewProfileDesign.Request>(context) {
  sealed class Request {
    data class Create(val provider: ProfileProvider) : Request()

    data class OpenDetail(val provider: ProfileProvider.External) : Request()

    data class LaunchScanner(val provider: ProfileProvider.QR) : Request()
  }

  private var providers by mutableStateOf<List<ProfileProvider>>(emptyList())

  override val root: View by composeView {
    MihomoTheme {
      NewProfileScreen(
        providers = providers,
        onCreate = ::requestCreate,
        onDetail = ::requestDetail,
      )
    }
  }

  suspend fun patchProviders(providers: List<ProfileProvider>) =
    withContext(Dispatchers.Main) { this@NewProfileDesign.providers = providers }

  private fun requestCreate(provider: ProfileProvider) {
    if (provider is ProfileProvider.QR) {
      requests.trySend(Request.LaunchScanner(provider))
    } else {
      requests.trySend(Request.Create(provider))
    }
  }

  private fun requestDetail(provider: ProfileProvider): Boolean {
    if (provider !is ProfileProvider.External) return false

    requests.trySend(Request.OpenDetail(provider))

    return true
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewProfileScreen(
  providers: List<ProfileProvider>,
  onCreate: (ProfileProvider) -> Unit,
  onDetail: (ProfileProvider) -> Boolean,
) {
  MihomoScaffold(title = stringResource(R.string.new_profile)) { innerPadding ->
    LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      items(items = providers) { provider ->
        ProfileProviderItem(
          provider = provider,
          onClick = { onCreate(provider) },
          onLongClick = { onDetail(provider) },
        )
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProfileProviderItem(
  provider: ProfileProvider,
  onClick: () -> Unit,
  onLongClick: () -> Unit,
) {
  val density = LocalDensity.current
  val itemPaddingVertical = dimensionResource(R.dimen.item_padding_vertical)
  val headerSize = dimensionResource(R.dimen.item_header_component_size)
  val headerMargin = dimensionResource(R.dimen.item_header_margin)
  val textMargin = dimensionResource(R.dimen.item_text_margin)
  val iconSizePx = with(density) { headerSize.toPx().roundToInt() }
  val iconPainter =
    remember(provider.icon, iconSizePx) {
      provider.icon
        ?.toBitmap(width = iconSizePx, height = iconSizePx)
        ?.asImageBitmap()
        ?.let(::BitmapPainter)
    }

  Row(
    modifier =
      Modifier.fillMaxWidth()
        .combinedClickable(onClick = onClick, onLongClick = onLongClick)
        .padding(vertical = itemPaddingVertical),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Spacer(modifier = Modifier.width(headerMargin))
    if (iconPainter != null) {
      Icon(
        painter = iconPainter,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.size(headerSize),
      )
    } else {
      Spacer(modifier = Modifier.size(headerSize))
    }
    Spacer(modifier = Modifier.width(headerMargin))
    Column {
      Text(text = provider.name, style = MaterialTheme.typography.bodyLarge)
      Text(
        text = provider.summary,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = textMargin),
      )
    }
  }
}

@PreviewMihomo
@Composable
private fun NewProfileScreenPreview() = MihomoTheme {
  val context = LocalContext.current
  val providers =
    listOf(
      ProfileProvider.File(context),
      ProfileProvider.Url(context),
      ProfileProvider.QR(context),
      ProfileProvider.External(
        name = "External Provider",
        summary = "Import from external app",
        icon = context.getDrawableCompat(R.drawable.ic_baseline_extension),
        intent = Intent(),
      ),
    )

  NewProfileScreen(providers = providers, onCreate = {}, onDetail = { false })
}
