package com.github.kr328.clash.profile

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.github.kr328.clash.profile.ui.FilesScreen
import com.github.kr328.clash.profile.ui.NewProfileScreen
import com.github.kr328.clash.profile.ui.ProfilesScreen
import com.github.kr328.clash.profile.ui.PropertiesScreen
import com.github.kr328.clash.profile.ui.ProvidersScreen
import com.github.kr328.clash.ui.nav.MihomoNavDisplay
import com.github.kr328.clash.ui.nav.addIfNotLast
import com.github.kr328.clash.ui.nav.rememberNavBackStackBuilder
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

sealed interface ProfilesRoute : NavKey {
  @Serializable data class Profiles(val openPropertyUuid: Uuid? = null) : ProfilesRoute

  @Serializable data object Providers : ProfilesRoute
}

@Serializable private data object NewProfiles : ProfilesRoute

@Serializable private data class Files(val uuid: Uuid) : ProfilesRoute

@Serializable private data class Properties(val uuid: Uuid) : ProfilesRoute

fun EntryProviderScope<NavKey>.profilesEntries() {
  entry<ProfilesRoute.Profiles> { key ->
    val backStack = rememberNavBackStackBuilder {
      add(ProfilesRoute.Profiles())
      when {
        key.openPropertyUuid != null -> add(Properties(key.openPropertyUuid))
      }
    }

    MihomoNavDisplay(
      backStack = backStack,
      entryProvider =
        entryProvider {
          entry<ProfilesRoute.Profiles> {
            ProfilesScreen(
              onOpenCreate = { backStack.addIfNotLast(NewProfiles) },
              onOpenEdit = { uuid -> backStack.addIfNotLast(Properties(uuid)) },
            )
          }
          entry<NewProfiles> {
            NewProfileScreen(
              onProperties = { uuid -> backStack.addIfNotLast(Properties(uuid)) },
              onFinish = { backStack.removeLastOrNull() },
            )
          }
          entry<Properties> { key ->
            PropertiesScreen(
              uuid = key.uuid,
              onBrowseFiles = { uuid -> backStack.addIfNotLast(Files(uuid)) },
              onFinish = { success ->
                backStack.removeLastOrNull()
                if (success && backStack.lastOrNull() is NewProfiles) {
                  backStack.removeLastOrNull()
                }
              },
            )
          }
          entry<Files> { key ->
            FilesScreen(uuid = key.uuid, onFinish = { backStack.removeLastOrNull() })
          }
        },
    )
  }

  entry<ProfilesRoute.Providers> { ProvidersScreen() }
}
