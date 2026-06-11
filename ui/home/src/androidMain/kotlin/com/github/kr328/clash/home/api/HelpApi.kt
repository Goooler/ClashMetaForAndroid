package com.github.kr328.clash.home.api

import com.github.kr328.clash.glue.util.TABBY_REPO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

class HelpApi(
  private val client: OkHttpClient = OkHttpClient(),
  private val json: Json = Json { ignoreUnknownKeys = true },
) {
  suspend fun getLatestRelease(): String? =
    withContext(Dispatchers.IO) {
      val request =
        Request.Builder()
          .url("https://api.github.com/repos/$TABBY_REPO/releases/latest")
          .header("Accept", "application/json")
          .build()
      client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) return@withContext null
        json.decodeFromString<GithubRelease>(response.body.string()).tagName
      }
    }
}

@Serializable data class GithubRelease(@SerialName("tag_name") val tagName: String)
