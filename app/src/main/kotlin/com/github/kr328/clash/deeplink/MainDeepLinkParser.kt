package com.github.kr328.clash.deeplink

import android.content.Intent
import android.net.Uri
import androidx.navigation3.runtime.NavKey
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.common.util.uuid
import com.github.kr328.clash.crash.CrashRoute
import com.github.kr328.clash.log.LogRoute
import com.github.kr328.clash.profile.ProfilesRoute
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

sealed interface MainNavigationAction {
  data class InstallConfig(val deepLink: InstallConfigDeepLink) : MainNavigationAction

  data class Navigate(val key: NavKey, val resetBackStack: Boolean = false) : MainNavigationAction
}

@Serializable
data class InstallConfigDeepLink(
  val url: String,
  val name: String? = null,
  val type: String? = null,
) : NavKey

private data class DeepLinkPattern<T : NavKey>(
  val serializer: DeserializationStrategy<T>,
  val scheme: String,
  val host: String,
)

object MainDeepLinkParser {
  private val json = Json { ignoreUnknownKeys = true }

  private val patterns =
    listOf(
      DeepLinkPattern(
        InstallConfigDeepLink.serializer(),
        scheme = "clash",
        host = "install-config",
      ),
      DeepLinkPattern(
        InstallConfigDeepLink.serializer(),
        scheme = "clashmeta",
        host = "install-config",
      ),
    )

  fun parse(intent: Intent): MainNavigationAction? {
    return when (intent.action) {
      Intent.ACTION_VIEW -> {
        val uri = intent.data ?: return null
        patterns.firstNotNullOfOrNull { decode(it, uri) }?.let(MainNavigationAction::InstallConfig)
      }
      Intents.ACTION_PROPERTIES -> {
        intent.uuid?.let {
          MainNavigationAction.Navigate(ProfilesRoute.Profiles(openPropertyUuid = it))
        }
      }
      Intents.ACTION_LOGCAT -> MainNavigationAction.Navigate(LogRoute.Root)
      Intents.ACTION_APP_CRASHED ->
        MainNavigationAction.Navigate(CrashRoute.AppCrashed, resetBackStack = true)
      Intents.ACTION_APK_BROKEN ->
        MainNavigationAction.Navigate(CrashRoute.ApkBroken, resetBackStack = true)
      else -> null
    }
  }

  private fun <T : NavKey> decode(pattern: DeepLinkPattern<T>, uri: Uri): T? {
    if (uri.scheme != pattern.scheme || uri.host != pattern.host) return null

    val descriptor = pattern.serializer.descriptor
    val arguments = buildMap {
      repeat(descriptor.elementsCount) { index ->
        val name = descriptor.getElementName(index)
        val value = uri.getQueryParameter(name)

        if (value != null) {
          put(name, JsonPrimitive(value))
        }
      }
    }

    repeat(descriptor.elementsCount) { index ->
      if (descriptor.isElementOptional(index)) return@repeat

      val requiredName = descriptor.getElementName(index)
      if (!arguments.containsKey(requiredName)) return null
    }

    return runCatching { json.decodeFromJsonElement(pattern.serializer, JsonObject(arguments)) }
      .getOrNull()
  }
}
