package com.github.kr328.clash.profile.ui

import android.content.Context
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.kr328.clash.R
import com.github.kr328.clash.service.model.Profile
import com.github.kr328.clash.ui.Design
import com.github.kr328.clash.ui.component.MihomoScaffold
import com.github.kr328.clash.ui.theme.MihomoTheme
import com.github.kr328.clash.ui.theme.PreviewMihomo
import com.github.kr328.clash.ui.theme.mihomoDimens
import com.github.kr328.clash.util.elapsedIntervalString
import com.github.kr328.clash.util.toBytesString
import com.github.kr328.clash.util.toDateStr
import com.github.kr328.clash.util.toString
import java.util.UUID
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfilesDesign(context: Context) : Design<ProfilesDesign.Request>(context) {
  sealed interface Request {
    data object UpdateAll : Request

    data object Create : Request

    data class Active(val profile: Profile) : Request

    data class Update(val profile: Profile) : Request

    data class Edit(val profile: Profile) : Request

    data class Duplicate(val profile: Profile) : Request

    data class Delete(val profile: Profile) : Request
  }

  private var profiles by mutableStateOf<List<Profile>>(emptyList())
  private var allUpdating by mutableStateOf(false)
  private var hasUpdatableProfile by mutableStateOf(false)
  private var currentTime by mutableLongStateOf(System.currentTimeMillis())

  @Composable
  override fun Content() = MihomoTheme {
    ProfilesScreen(
      profiles = profiles,
      allUpdating = allUpdating,
      hasUpdatableProfile = hasUpdatableProfile,
      currentTime = currentTime,
      onUpdateAll = {
        allUpdating = true
        requests.trySend(Request.UpdateAll)
      },
      onCreate = { requests.trySend(Request.Create) },
      onActivate = { requests.trySend(Request.Active(it)) },
      onUpdate = { requests.trySend(Request.Update(it)) },
      onEdit = { requests.trySend(Request.Edit(it)) },
      onDuplicate = { requests.trySend(Request.Duplicate(it)) },
      onDelete = { requests.trySend(Request.Delete(it)) },
    )
  }

  suspend fun patchProfiles(profiles: List<Profile>) {
    val updatable =
      withContext(Dispatchers.Default) {
        profiles.any { it.imported && it.type != Profile.Type.File }
      }

    withContext(Dispatchers.Main) {
      this@ProfilesDesign.profiles = profiles
      hasUpdatableProfile = updatable
    }
  }

  suspend fun requestSave(profile: Profile) {
    snackbar(R.string.active_unsaved_tips) {
      setAction(R.string.edit) { requests.trySend(Request.Edit(profile)) }
    }
  }

  fun updateElapsed() {
    currentTime = System.currentTimeMillis()
  }

  fun finishUpdateAll() {
    allUpdating = false
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfilesScreen(
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
          icon = R.drawable.ic_baseline_update,
          text = stringResource(R.string.update),
          onClick = {
            menuProfile = null
            onUpdate(profile)
          },
        )
      }
      ProfilesMenuAction(
        icon = R.drawable.ic_baseline_edit,
        text = stringResource(R.string.edit),
        onClick = {
          menuProfile = null
          onEdit(profile)
        },
      )
      if (profile.imported) {
        ProfilesMenuAction(
          icon = R.drawable.ic_baseline_content_copy,
          text = stringResource(R.string.duplicate),
          onClick = {
            menuProfile = null
            onDuplicate(profile)
          },
        )
      }
      ProfilesMenuAction(
        icon = R.drawable.ic_outline_delete,
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
              painter = painterResource(R.drawable.ic_baseline_sync),
              contentDescription = stringResource(R.string.update_all),
            )
          }
        }
      }
      IconButton(onClick = onCreate) {
        Icon(
          painter = painterResource(R.drawable.ic_baseline_add),
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
      "${(profile.download + profile.upload).toBytesString()} / ${profile.total.toBytesString()}"
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
          painter = painterResource(R.drawable.ic_baseline_more_vert),
          contentDescription = stringResource(R.string.more),
        )
      }
    }
  }
}

@Composable
private fun ProfilesMenuAction(
  icon: Int,
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
      painter = painterResource(icon),
      contentDescription = null,
      tint = tint,
      modifier = Modifier.size(24.dp),
    )
    Spacer(modifier = Modifier.width(16.dp))
    Text(text = text, color = tint)
  }
}

@PreviewMihomo
@Composable
private fun ProfilesScreenPreview() = MihomoTheme {
  ProfilesScreen(
    profiles =
      listOf(
        Profile(
          uuid = UUID(0, 0),
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
          uuid = UUID(0, 1),
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
