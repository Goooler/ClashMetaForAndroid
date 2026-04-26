package com.github.kr328.clash.profile.ui

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.drawable.Drawable
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.kr328.clash.R
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.common.util.setUUID
import com.github.kr328.clash.model.ProfileProvider
import com.github.kr328.clash.profile.PropertiesActivity
import com.github.kr328.clash.profile.vm.NewProfileViewModel
import com.github.kr328.clash.ui.component.MihomoScaffold
import com.github.kr328.clash.ui.icon.BaselineExtension
import com.github.kr328.clash.ui.icon.MihomoIcons
import com.github.kr328.clash.ui.theme.MihomoTheme
import com.github.kr328.clash.ui.theme.PreviewMihomo
import com.github.kr328.clash.ui.theme.mihomoDimens
import io.github.g00fy2.quickie.ScanQRCode
import kotlin.math.roundToInt

@Composable
fun NewProfileScreen(
  modifier: Modifier = Modifier,
  viewModel: NewProfileViewModel = viewModel(),
  onFinish: () -> Unit,
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val eventState by viewModel.eventState.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }

  val qrLauncher =
    rememberLauncherForActivityResult(ScanQRCode()) { result -> viewModel.onQRResult(result) }

  val externalProviderLauncher =
    rememberLauncherForActivityResult(StartActivityForResult()) { result ->
      if (result.resultCode == RESULT_OK) {
        val uri = result.data?.data ?: return@rememberLauncherForActivityResult
        val name = result.data?.getStringExtra(Intents.EXTRA_NAME)
        viewModel.onExternalProviderResult(uri, name)
      }
    }

  val propertiesLauncher =
    rememberLauncherForActivityResult(StartActivityForResult()) { result ->
      viewModel.onPropertiesResult(result.resultCode == RESULT_OK)
    }

  LaunchedEffect(eventState) {
    when (val event = eventState) {
      NewProfileViewModel.EventState.Idle -> Unit
      NewProfileViewModel.EventState.LaunchQRScanner -> qrLauncher.launch(null)
      is NewProfileViewModel.EventState.LaunchExternalProvider ->
        externalProviderLauncher.launch(event.intent)
      is NewProfileViewModel.EventState.LaunchProperties ->
        propertiesLauncher.launch(PropertiesActivity::class.intent.setUUID(event.uuid))
      is NewProfileViewModel.EventState.OpenAppSettings ->
        context.startActivity(
          Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(event.uri)
        )
      is NewProfileViewModel.EventState.ShowMessage ->
        snackbarHostState.showSnackbar(message = event.message, duration = SnackbarDuration.Long)
      NewProfileViewModel.EventState.Finish -> onFinish()
    }
    viewModel.consumeEvent()
  }

  Box(modifier = modifier.fillMaxSize()) {
    NewProfileContent(
      providers = uiState.providers,
      onCreate = viewModel::onCreate,
      onDetail = viewModel::onDetail,
    )
    SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewProfileContent(
  providers: List<ProfileProvider>,
  onCreate: (ProfileProvider) -> Unit,
  onDetail: (ProfileProvider.External) -> Unit,
) {
  MihomoScaffold(title = stringResource(R.string.new_profile)) { innerPadding ->
    LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      items(items = providers) { provider ->
        ProfileProviderItem(
          provider = provider,
          onClick = { onCreate(provider) },
          onLongClick = { if (provider is ProfileProvider.External) onDetail(provider) },
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
  val dimens = mihomoDimens
  val itemPaddingVertical = dimens.itemPaddingVertical
  val headerSize = dimens.itemHeaderComponentSize
  val headerMargin = dimens.itemHeaderMargin
  val textMargin = dimens.itemTextMargin
  val iconSizePx = with(density) { headerSize.toPx().roundToInt() }
  val iconVector = provider.icon as? ImageVector
  val iconDrawable = provider.icon as? Drawable
  val iconPainter =
    if (iconVector != null) {
      rememberVectorPainter(iconVector)
    } else {
      remember(iconDrawable, iconSizePx) {
        iconDrawable
          ?.toBitmap(width = iconSizePx, height = iconSizePx)
          ?.asImageBitmap()
          ?.let(::BitmapPainter)
      }
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
      Icon(painter = iconPainter, contentDescription = null, modifier = Modifier.size(headerSize))
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
private fun NewProfileContentPreview() = MihomoTheme {
  val context = LocalContext.current
  val providers =
    listOf(
      ProfileProvider.File(context),
      ProfileProvider.Url(context),
      ProfileProvider.QR(context),
      ProfileProvider.External(
        name = "External Provider",
        summary = "Import from external app",
        icon = MihomoIcons.BaselineExtension,
        intent = Intent(),
      ),
    )

  NewProfileContent(providers = providers, onCreate = {}, onDetail = {})
}
