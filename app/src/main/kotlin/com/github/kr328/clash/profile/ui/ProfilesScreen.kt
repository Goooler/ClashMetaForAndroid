package com.github.kr328.clash.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.kr328.clash.R
import com.github.kr328.clash.profile.vm.ProfilesViewModel
import com.github.kr328.clash.service.model.Profile
import com.github.kr328.clash.ui.component.MihomoScaffold
import com.github.kr328.clash.ui.icon.BaselineAdd
import com.github.kr328.clash.ui.icon.BaselineContentCopy
import com.github.kr328.clash.ui.icon.BaselineEdit
import com.github.kr328.clash.ui.icon.BaselineMoreVert
import com.github.kr328.clash.ui.icon.BaselineSync
import com.github.kr328.clash.ui.icon.BaselineUpdate
import com.github.kr328.clash.ui.icon.MihomoIcons
import com.github.kr328.clash.ui.icon.OutlineDelete
import com.github.kr328.clash.ui.theme.MihomoThemeWrapper
import com.github.kr328.clash.ui.theme.PreviewMihomo
import com.github.kr328.clash.ui.theme.mihomoDimens
import com.github.kr328.clash.util.elapsedIntervalString
import com.github.kr328.clash.util.toDateStr
import com.github.kr328.clash.util.toString
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid
import me.saket.bytesize.binaryBytes

@Composable
fun ProfilesScreen(
  modifier: Modifier = Modifier,
  viewModel: ProfilesViewModel = viewModel(),
  onOpenCreate: () -> Unit,
  onOpenEdit: (Uuid) -> Unit,
) {
  val lifecycleOwner = LocalLifecycleOwner.current
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val eventState by viewModel.eventState.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }
  val editText = stringResource(R.string.edit)

  DisposableEffect(lifecycleOwner, viewModel) {
    lifecycleOwner.lifecycle.addObserver(viewModel)
    onDispose { lifecycleOwner.lifecycle.removeObserver(viewModel) }
  }

  LaunchedEffect(eventState) {
    when (val event = eventState) {
      ProfilesViewModel.EventState.Idle -> Unit
      ProfilesViewModel.EventState.OpenCreate -> onOpenCreate()
      is ProfilesViewModel.EventState.OpenEdit -> onOpenEdit(event.uuid)
      is ProfilesViewModel.EventState.ShowMessage -> {
        snackbarHostState.showSnackbar(message = event.message)
      }
      is ProfilesViewModel.EventState.ShowEditableMessage -> {
        val result =
          snackbarHostState.showSnackbar(
            message = event.message,
            actionLabel = editText,
            duration = SnackbarDuration.Long,
          )

        if (result == SnackbarResult.ActionPerformed) {
          onOpenEdit(event.uuid)
        }
      }
    }
    viewModel.consumeEvent()
  }

  ProfilesContent(
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    profiles = uiState.profiles,
    allUpdating = uiState.allUpdating,
    hasUpdatableProfile = uiState.hasUpdatableProfile,
    currentTime = uiState.currentTime,
    onUpdateAll = viewModel::onUpdateAll,
    onCreate = viewModel::onOpenCreate,
    onActivate = viewModel::onActivate,
    onUpdate = viewModel::onUpdate,
    onEdit = viewModel::onEdit,
    onDuplicate = viewModel::onDuplicate,
    onDelete = viewModel::onDelete,
  )
}

@Composable
private fun ProfilesContent(
  modifier: Modifier = Modifier,
  snackbarHostState: SnackbarHostState,
  profiles: List<Profile>,
  allUpdating: Boolean,
  hasUpdatableProfile: Boolean,
  currentTime: Long,
  onUpdateAll: () -> Unit,
  onCreate: () -> Unit,
  onActivate: (Profile) -> Unit,
  onUpdate: (Profile) -> Unit,
  onEdit: (Profile) -> Unit,
  onDuplicate: (Profile) -> Unit,
  onDelete: (Profile) -> Unit,
) {
  var menuProfile by remember { mutableStateOf<Profile?>(null) }
  val sheetState = rememberModalBottomSheetState()

  menuProfile?.let { profile ->
    ModalBottomSheet(onDismissRequest = { menuProfile = null }, sheetState = sheetState) {
      if (profile.imported && profile.type != Profile.Type.File) {
        ProfilesMenuAction(
          icon = MihomoIcons.BaselineUpdate,
          text = stringResource(R.string.update),
          onClick = {
            menuProfile = null
            onUpdate(profile)
          },
        )
      }
      ProfilesMenuAction(
        icon = MihomoIcons.BaselineEdit,
        text = stringResource(R.string.edit),
        onClick = {
          menuProfile = null
          onEdit(profile)
        },
      )
      if (profile.imported) {
        ProfilesMenuAction(
          icon = MihomoIcons.BaselineContentCopy,
          text = stringResource(R.string.duplicate),
          onClick = {
            menuProfile = null
            onDuplicate(profile)
          },
        )
      }
      ProfilesMenuAction(
        icon = MihomoIcons.OutlineDelete,
        text = stringResource(R.string.delete),
        tint = MaterialTheme.colorScheme.error,
        onClick = {
          menuProfile = null
          onDelete(profile)
        },
      )
      Spacer(modifier = Modifier.height(16.dp))
    }
  }

  MihomoScaffold(
    title = stringResource(R.string.profiles),
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    actions = {
      if (hasUpdatableProfile) {
        IconButton(onClick = onUpdateAll, enabled = !allUpdating) {
          if (allUpdating) {
            val dimens = mihomoDimens
            CircularProgressIndicator(
              modifier = Modifier.size(dimens.itemTrailingComponentSize / 2),
              strokeWidth = dimens.toolbarImageActionPadding / 2,
            )
          } else {
            Icon(
              imageVector = MihomoIcons.BaselineSync,
              contentDescription = stringResource(R.string.update_all),
            )
          }
        }
      }
      IconButton(onClick = onCreate) {
        Icon(
          imageVector = MihomoIcons.BaselineAdd,
          contentDescription = stringResource(R.string.new_profile),
        )
      }
    },
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier.fillMaxSize().padding(innerPadding),
      contentPadding = PaddingValues(vertical = 5.dp),
      verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
      items(items = profiles, key = Profile::uuid) { profile ->
        ProfileItem(
          profile = profile,
          currentTime = currentTime,
          onClick = { onActivate(profile) },
          onMenuClick = { menuProfile = profile },
        )
      }
    }
  }
}

@Composable
private fun ProfileItem(
  profile: Profile,
  currentTime: Long,
  onClick: () -> Unit,
  onMenuClick: () -> Unit,
) {
  val context = LocalContext.current
  val dimens = mihomoDimens
  val itemMinHeight = dimens.itemMinHeight
  val itemHeaderMargin = dimens.itemHeaderMargin
  val itemTextMargin = dimens.itemTextMargin
  val itemMiddleMargin = dimens.itemMiddleMargin

  val profileTypeText =
    if (profile.pending) {
      stringResource(R.string.format_type_unsaved, profile.type.toString(context))
    } else {
      profile.type.toString(context)
    }
  val showTraffic = profile.download >= 2 && profile.total > 1
  val usageText =
    if (showTraffic) {
      "${(profile.download + profile.upload).binaryBytes} / ${profile.total.binaryBytes}"
    } else {
      null
    }
  val progress =
    if (showTraffic) {
      ((profile.download + profile.upload).toDouble() / profile.total.toDouble() * 1000)
        .toInt()
        .coerceIn(0, 1000)
    } else {
      0
    }

  ElevatedCard(
    modifier = Modifier.fillMaxWidth().padding(horizontal = itemHeaderMargin, vertical = 5.dp),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 10.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(start = 0.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier = Modifier.size(width = 65.dp, height = itemMinHeight),
        contentAlignment = Alignment.Center,
      ) {
        RadioButton(selected = profile.active, onClick = null)
      }

      Column(modifier = Modifier.weight(1f).padding(vertical = dimens.itemPaddingVertical)) {
        Text(text = profile.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(modifier = Modifier.height(itemTextMargin))
        Text(text = profileTypeText, style = MaterialTheme.typography.bodyMedium)
        usageText?.let {
          Spacer(modifier = Modifier.height(4.dp))
          Text(text = it, style = MaterialTheme.typography.labelMedium)
        }
        if (profile.expire != 0L) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(text = profile.expire.toDateStr(), style = MaterialTheme.typography.labelMedium)
        }
        if (showTraffic) {
          Spacer(modifier = Modifier.height(6.dp))
          LinearProgressIndicator(
            progress = { progress / 1000f },
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }

      Text(
        text = (currentTime - profile.updatedAt).elapsedIntervalString(context),
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.padding(horizontal = itemMiddleMargin),
      )

      Box(
        modifier =
          Modifier.width(1.dp).height(itemMinHeight).background(MaterialTheme.colorScheme.outline)
      )

      IconButton(onClick = onMenuClick, modifier = Modifier.padding(horizontal = 4.dp)) {
        Icon(
          imageVector = MihomoIcons.BaselineMoreVert,
          contentDescription = stringResource(R.string.more),
        )
      }
    }
  }
}

@Composable
private fun ProfilesMenuAction(
  icon: ImageVector,
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  tint: Color = MaterialTheme.colorScheme.onSurface,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(horizontal = 20.dp, vertical = 16.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = tint,
      modifier = Modifier.size(24.dp),
    )
    Spacer(modifier = Modifier.width(16.dp))
    Text(text = text, color = tint)
  }
}

@PreviewWrapper(MihomoThemeWrapper::class)
@PreviewMihomo
@Composable
private fun ProfilesContentPreview() {
  ProfilesContent(
    snackbarHostState = SnackbarHostState(),
    profiles =
      listOf(
        Profile(
          uuid = Uuid.fromLongs(0, 0),
          name = "Main Profile",
          type = Profile.Type.Url,
          source = "https://example.com/config.yaml",
          active = true,
          interval = 30.minutes.inWholeMilliseconds,
          upload = 512L * 1024 * 1024,
          download = 1024L * 1024 * 1024,
          total = 5L * 1024 * 1024 * 1024,
          expire = System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000,
          updatedAt = System.currentTimeMillis() - 30.minutes.inWholeMilliseconds,
          imported = true,
          pending = false,
        ),
        Profile(
          uuid = Uuid.fromLongs(0, 1),
          name = "Draft Profile",
          type = Profile.Type.File,
          source = "",
          active = false,
          interval = 0,
          upload = 0,
          download = 0,
          total = 0,
          expire = 0,
          updatedAt = System.currentTimeMillis() - 5.minutes.inWholeMilliseconds,
          imported = false,
          pending = true,
        ),
      ),
    allUpdating = false,
    hasUpdatableProfile = true,
    currentTime = System.currentTimeMillis(),
    onUpdateAll = {},
    onCreate = {},
    onActivate = {},
    onUpdate = {},
    onEdit = {},
    onDuplicate = {},
    onDelete = {},
  )
}
