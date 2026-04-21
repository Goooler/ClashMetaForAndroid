package com.github.kr328.clash.design

import android.content.Context
import android.view.View
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import com.github.kr328.clash.core.model.ConfigurationOverride
import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.core.model.TunnelState
import com.github.kr328.clash.design.component.MihomoScaffold
import com.github.kr328.clash.design.ui.theme.MihomoTheme
import com.github.kr328.clash.design.ui.theme.PreviewMihomo
import me.zhanghai.compose.preference.ListPreference
import me.zhanghai.compose.preference.Preference
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.preferenceCategory

class OverrideSettingsDesign(context: Context, configuration: ConfigurationOverride) :
  Design<OverrideSettingsDesign.Request>(context) {
  enum class Request {
    ResetOverride
  }

  override val root: View by composeView {
    MihomoTheme {
      OverrideSettingsScreen(
        configuration = configuration,
        onResetConfirmed = { requests.trySend(Request.ResetOverride) },
      )
    }
  }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun OverrideSettingsScreen(
  configuration: ConfigurationOverride,
  onResetConfirmed: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var showResetConfirmDialog by remember { mutableStateOf(false) }

  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
  MihomoScaffold(
    title = stringResource(R.string.override),
    modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    scrollBehavior = scrollBehavior,
    actions = {
      IconButton(onClick = { showResetConfirmDialog = true }) {
        Icon(
          painter = painterResource(R.drawable.ic_baseline_replay),
          contentDescription = stringResource(R.string.reset),
        )
      }
    },
  ) { innerPadding ->
    ProvidePreferenceLocals {
      val dnsEnableState =
        rememberWriteThroughState(configuration.dns.enable) { configuration.dns.enable = it }
      val dnsEnabled by dnsEnableState

      LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = innerPadding) {
        generalPreferenceItems(configuration)
        dnsPreferenceItems(configuration, dnsEnableState, dnsEnabled)
      }

      if (showResetConfirmDialog) {
        AlertDialog(
          onDismissRequest = { showResetConfirmDialog = false },
          title = { Text(stringResource(R.string.reset_override_settings)) },
          text = { Text(stringResource(R.string.reset_override_settings_message)) },
          confirmButton = {
            TextButton(
              onClick = {
                showResetConfirmDialog = false
                onResetConfirmed()
              }
            ) {
              Text(stringResource(R.string.ok))
            }
          },
          dismissButton = {
            TextButton(onClick = { showResetConfirmDialog = false }) {
              Text(stringResource(R.string.cancel))
            }
          },
        )
      }
    }
  }
}

private fun LazyListScope.generalPreferenceItems(configuration: ConfigurationOverride) {
  preferenceCategory(key = "cat_general", title = { Text(stringResource(R.string.general)) })
  item(key = "httpPort", contentType = "EditTextPreference") {
    OverrideEditTextPreferenceItem(
      title = stringResource(R.string.http_port),
      state =
        rememberWriteThroughState(portText(configuration.httpPort)) {
          configuration.httpPort = parsePort(it)
        },
      placeholder = stringResource(R.string.dont_modify),
      emptyLabel = stringResource(R.string.disabled),
    )
  }
  item(key = "socksPort", contentType = "EditTextPreference") {
    OverrideEditTextPreferenceItem(
      title = stringResource(R.string.socks_port),
      state =
        rememberWriteThroughState(portText(configuration.socksPort)) {
          configuration.socksPort = parsePort(it)
        },
      placeholder = stringResource(R.string.dont_modify),
      emptyLabel = stringResource(R.string.disabled),
    )
  }
  item(key = "redirectPort", contentType = "EditTextPreference") {
    OverrideEditTextPreferenceItem(
      title = stringResource(R.string.redirect_port),
      state =
        rememberWriteThroughState(portText(configuration.redirectPort)) {
          configuration.redirectPort = parsePort(it)
        },
      placeholder = stringResource(R.string.dont_modify),
      emptyLabel = stringResource(R.string.disabled),
    )
  }
  item(key = "tproxyPort", contentType = "EditTextPreference") {
    OverrideEditTextPreferenceItem(
      title = stringResource(R.string.tproxy_port),
      state =
        rememberWriteThroughState(portText(configuration.tproxyPort)) {
          configuration.tproxyPort = parsePort(it)
        },
      placeholder = stringResource(R.string.dont_modify),
      emptyLabel = stringResource(R.string.disabled),
    )
  }
  item(key = "mixedPort", contentType = "EditTextPreference") {
    OverrideEditTextPreferenceItem(
      title = stringResource(R.string.mixed_port),
      state =
        rememberWriteThroughState(portText(configuration.mixedPort)) {
          configuration.mixedPort = parsePort(it)
        },
      placeholder = stringResource(R.string.dont_modify),
      emptyLabel = stringResource(R.string.disabled),
    )
  }
  item(key = "authentication", contentType = "EditTextListPreference") {
    OverrideEditTextListPreferenceItem(
      title = stringResource(R.string.authentication),
      state =
        rememberWriteThroughState(configuration.authentication) {
          configuration.authentication = it
        },
      placeholder = stringResource(R.string.dont_modify),
    )
  }
  item(key = "allowLan", contentType = "ListPreference") {
    val state = rememberWriteThroughState(configuration.allowLan) { configuration.allowLan = it }
    val value by state
    ListPreference(
      state = state,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      title = { Text(stringResource(R.string.allow_lan)) },
      summary = { Text(booleanText(value)) },
      valueToText = { v: Boolean? -> AnnotatedString(booleanText(v)) },
    )
  }
  item(key = "ipv6", contentType = "ListPreference") {
    val state = rememberWriteThroughState(configuration.ipv6) { configuration.ipv6 = it }
    val value by state
    ListPreference(
      state = state,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      title = { Text(stringResource(R.string.ipv6)) },
      summary = { Text(booleanText(value)) },
      valueToText = { v: Boolean? -> AnnotatedString(booleanText(v)) },
    )
  }
  item(key = "bindAddress", contentType = "EditTextPreference") {
    OverrideEditTextPreferenceItem(
      title = stringResource(R.string.bind_address),
      state =
        rememberWriteThroughState(configuration.bindAddress) { configuration.bindAddress = it },
      placeholder = stringResource(R.string.dont_modify),
      emptyLabel = stringResource(R.string.default_),
    )
  }
  item(key = "externalController", contentType = "EditTextPreference") {
    OverrideEditTextPreferenceItem(
      title = stringResource(R.string.external_controller),
      state =
        rememberWriteThroughState(configuration.externalController) {
          configuration.externalController = it
        },
      placeholder = stringResource(R.string.dont_modify),
      emptyLabel = stringResource(R.string.default_),
    )
  }
  item(key = "externalControllerTls", contentType = "EditTextPreference") {
    OverrideEditTextPreferenceItem(
      title = stringResource(R.string.external_controller_tls),
      state =
        rememberWriteThroughState(configuration.externalControllerTLS) {
          configuration.externalControllerTLS = it
        },
      placeholder = stringResource(R.string.dont_modify),
      emptyLabel = stringResource(R.string.default_),
    )
  }
  item(key = "allowOrigins", contentType = "EditTextListPreference") {
    OverrideEditTextListPreferenceItem(
      title = stringResource(R.string.allow_origins),
      state =
        rememberWriteThroughState(configuration.externalControllerCors.allowOrigins) {
          configuration.externalControllerCors.allowOrigins = it
        },
      placeholder = stringResource(R.string.dont_modify),
    )
  }
  item(key = "allowPrivateNetwork", contentType = "ListPreference") {
    val state =
      rememberWriteThroughState(configuration.externalControllerCors.allowPrivateNetwork) {
        configuration.externalControllerCors.allowPrivateNetwork = it
      }
    val value by state
    ListPreference(
      state = state,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      title = { Text(stringResource(R.string.allow_private_network)) },
      summary = { Text(booleanText(value)) },
      valueToText = { v: Boolean? -> AnnotatedString(booleanText(v)) },
    )
  }
  item(key = "secret", contentType = "EditTextPreference") {
    OverrideEditTextPreferenceItem(
      title = stringResource(R.string.secret),
      state = rememberWriteThroughState(configuration.secret) { configuration.secret = it },
      placeholder = stringResource(R.string.dont_modify),
      emptyLabel = stringResource(R.string.default_),
    )
  }
  item(key = "mode", contentType = "ListPreference") {
    val state = rememberWriteThroughState(configuration.mode) { configuration.mode = it }
    val value by state
    ListPreference(
      state = state,
      values = TunnelState.Mode.entries,
      modifier = Modifier.fillMaxWidth(),
      title = { Text(stringResource(R.string.mode)) },
      summary = { Text(modeText(value)) },
      valueToText = { v: TunnelState.Mode? -> AnnotatedString(modeText(v)) },
    )
  }
  item(key = "logLevel", contentType = "ListPreference") {
    val state = rememberWriteThroughState(configuration.logLevel) { configuration.logLevel = it }
    val value by state
    ListPreference(
      state = state,
      values = LogMessage.Level.entries,
      modifier = Modifier.fillMaxWidth(),
      title = { Text(stringResource(R.string.log_level)) },
      summary = { Text(logLevelText(value)) },
      valueToText = { v: LogMessage.Level? -> AnnotatedString(logLevelText(v)) },
    )
  }
  item(key = "hosts", contentType = "EditTextMapPreference") {
    OverrideEditTextMapPreferenceItem(
      title = stringResource(R.string.hosts),
      state = rememberWriteThroughState(configuration.hosts) { configuration.hosts = it },
      placeholder = stringResource(R.string.dont_modify),
    )
  }
}

private fun LazyListScope.dnsPreferenceItems(
  configuration: ConfigurationOverride,
  dnsEnableState: MutableState<Boolean?>,
  dnsEnabled: Boolean?,
) {
  preferenceCategory(key = "cat_dns", title = { Text(stringResource(R.string.dns)) })
  item(key = "dnsStrategy", contentType = "ListPreference") {
    ListPreference(
      state = dnsEnableState,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      title = { Text(stringResource(R.string.strategy)) },
      summary = {
        Text(
          stringResource(
            when (dnsEnabled) {
              true -> R.string.force_enable
              false -> R.string.use_built_in
              else -> R.string.dont_modify
            }
          )
        )
      },
      valueToText = { v: Boolean? ->
        AnnotatedString(
          stringResource(
            when (v) {
              true -> R.string.force_enable
              false -> R.string.use_built_in
              else -> R.string.dont_modify
            }
          )
        )
      },
    )
  }
  item(key = "dnsPreferH3", contentType = "ListPreference") {
    val state =
      rememberWriteThroughState(configuration.dns.preferH3) { configuration.dns.preferH3 = it }
    val value by state
    ListPreference(
      state = state,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      enabled = dnsEnabled != false,
      title = { Text(stringResource(R.string.prefer_h3)) },
      summary = { Text(booleanText(value)) },
      valueToText = { v: Boolean? -> AnnotatedString(booleanText(v)) },
    )
  }
  item(key = "dnsListen", contentType = "EditTextPreference") {
    OverrideEditTextPreferenceItem(
      title = stringResource(R.string.listen),
      state = rememberWriteThroughState(configuration.dns.listen) { configuration.dns.listen = it },
      placeholder = stringResource(R.string.dont_modify),
      emptyLabel = stringResource(R.string.disabled),
      enabled = dnsEnabled != false,
    )
  }
  item(key = "appendSystemDns", contentType = "ListPreference") {
    val state =
      rememberWriteThroughState(configuration.app.appendSystemDns) {
        configuration.app.appendSystemDns = it
      }
    val value by state
    ListPreference(
      state = state,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      enabled = dnsEnabled != false,
      title = { Text(stringResource(R.string.append_system_dns)) },
      summary = { Text(booleanText(value)) },
      valueToText = { v: Boolean? -> AnnotatedString(booleanText(v)) },
    )
  }
  item(key = "dnsIpv6", contentType = "ListPreference") {
    val state = rememberWriteThroughState(configuration.dns.ipv6) { configuration.dns.ipv6 = it }
    val value by state
    ListPreference(
      state = state,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      enabled = dnsEnabled != false,
      title = { Text(stringResource(R.string.ipv6)) },
      summary = { Text(booleanText(value)) },
      valueToText = { v: Boolean? -> AnnotatedString(booleanText(v)) },
    )
  }
  item(key = "dnsUseHosts", contentType = "ListPreference") {
    val state =
      rememberWriteThroughState(configuration.dns.useHosts) { configuration.dns.useHosts = it }
    val value by state
    ListPreference(
      state = state,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      enabled = dnsEnabled != false,
      title = { Text(stringResource(R.string.use_hosts)) },
      summary = { Text(booleanText(value)) },
      valueToText = { v: Boolean? -> AnnotatedString(booleanText(v)) },
    )
  }
  item(key = "dnsEnhancedMode", contentType = "ListPreference") {
    val state =
      rememberWriteThroughState(configuration.dns.enhancedMode) {
        configuration.dns.enhancedMode = it
      }
    val value by state
    ListPreference(
      state = state,
      values = ConfigurationOverride.DnsEnhancedMode.entries,
      modifier = Modifier.fillMaxWidth(),
      enabled = dnsEnabled != false,
      title = { Text(stringResource(R.string.enhanced_mode)) },
      summary = { Text(enhancedModeText(value)) },
      valueToText = { v: ConfigurationOverride.DnsEnhancedMode? ->
        AnnotatedString(enhancedModeText(v))
      },
    )
  }
  item(key = "dnsNameServer", contentType = "EditTextListPreference") {
    OverrideEditTextListPreferenceItem(
      title = stringResource(R.string.name_server),
      state =
        rememberWriteThroughState(configuration.dns.nameServer) {
          configuration.dns.nameServer = it
        },
      placeholder = stringResource(R.string.dont_modify),
      enabled = dnsEnabled != false,
    )
  }
  item(key = "dnsFallback", contentType = "EditTextListPreference") {
    OverrideEditTextListPreferenceItem(
      title = stringResource(R.string.fallback),
      state =
        rememberWriteThroughState(configuration.dns.fallback) { configuration.dns.fallback = it },
      placeholder = stringResource(R.string.dont_modify),
      enabled = dnsEnabled != false,
    )
  }
  item(key = "dnsDefaultServer", contentType = "EditTextListPreference") {
    OverrideEditTextListPreferenceItem(
      title = stringResource(R.string.default_name_server),
      state =
        rememberWriteThroughState(configuration.dns.defaultServer) {
          configuration.dns.defaultServer = it
        },
      placeholder = stringResource(R.string.dont_modify),
      enabled = dnsEnabled != false,
    )
  }
  item(key = "dnsFakeIpFilter", contentType = "EditTextListPreference") {
    OverrideEditTextListPreferenceItem(
      title = stringResource(R.string.fakeip_filter),
      state =
        rememberWriteThroughState(configuration.dns.fakeIpFilter) {
          configuration.dns.fakeIpFilter = it
        },
      placeholder = stringResource(R.string.dont_modify),
      enabled = dnsEnabled != false,
    )
  }
  item(key = "dnsFakeIpFilterMode", contentType = "ListPreference") {
    val state =
      rememberWriteThroughState(configuration.dns.fakeIPFilterMode) {
        configuration.dns.fakeIPFilterMode = it
      }
    val value by state
    ListPreference(
      state = state,
      values = ConfigurationOverride.FilterMode.entries,
      modifier = Modifier.fillMaxWidth(),
      enabled = dnsEnabled != false,
      title = { Text(stringResource(R.string.fakeip_filter_mode)) },
      summary = { Text(filterModeText(value)) },
      valueToText = { v: ConfigurationOverride.FilterMode? -> AnnotatedString(filterModeText(v)) },
    )
  }
  item(key = "dnsGeoIpFallback", contentType = "ListPreference") {
    val state =
      rememberWriteThroughState(configuration.dns.fallbackFilter.geoIp) {
        configuration.dns.fallbackFilter.geoIp = it
      }
    val value by state
    ListPreference(
      state = state,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      enabled = dnsEnabled != false,
      title = { Text(stringResource(R.string.geoip_fallback)) },
      summary = { Text(booleanText(value)) },
      valueToText = { v: Boolean? -> AnnotatedString(booleanText(v)) },
    )
  }
  item(key = "dnsGeoIpCode", contentType = "EditTextPreference") {
    OverrideEditTextPreferenceItem(
      title = stringResource(R.string.geoip_fallback_code),
      state =
        rememberWriteThroughState(configuration.dns.fallbackFilter.geoIpCode) {
          configuration.dns.fallbackFilter.geoIpCode = it
        },
      placeholder = stringResource(R.string.dont_modify),
      emptyLabel = stringResource(R.string.raw_cn),
      enabled = dnsEnabled != false,
    )
  }
  item(key = "dnsDomainFallback", contentType = "EditTextListPreference") {
    OverrideEditTextListPreferenceItem(
      title = stringResource(R.string.domain_fallback),
      state =
        rememberWriteThroughState(configuration.dns.fallbackFilter.domain) {
          configuration.dns.fallbackFilter.domain = it
        },
      placeholder = stringResource(R.string.dont_modify),
      enabled = dnsEnabled != false,
    )
  }
  item(key = "dnsIpcidrFallback", contentType = "EditTextListPreference") {
    OverrideEditTextListPreferenceItem(
      title = stringResource(R.string.ipcidr_fallback),
      state =
        rememberWriteThroughState(configuration.dns.fallbackFilter.ipcidr) {
          configuration.dns.fallbackFilter.ipcidr = it
        },
      placeholder = stringResource(R.string.dont_modify),
      enabled = dnsEnabled != false,
    )
  }
  item(key = "dnsNameserverPolicy", contentType = "EditTextMapPreference") {
    OverrideEditTextMapPreferenceItem(
      title = stringResource(R.string.name_server_policy),
      state =
        rememberWriteThroughState(configuration.dns.nameserverPolicy) {
          configuration.dns.nameserverPolicy = it
        },
      placeholder = stringResource(R.string.dont_modify),
      enabled = dnsEnabled != false,
    )
  }
}

@Composable
private fun OverrideEditTextPreferenceItem(
  title: String,
  state: MutableState<String?>,
  placeholder: String,
  emptyLabel: String,
  enabled: Boolean = true,
) {
  var text by state
  var showDialog by remember { mutableStateOf(false) }
  val summary = text?.let { it.ifEmpty { emptyLabel } } ?: placeholder
  Preference(
    modifier = Modifier.fillMaxWidth(),
    title = { Text(title) },
    summary = { Text(summary) },
    enabled = enabled,
    onClick = { showDialog = true },
  )
  if (showDialog) {
    var inputText by remember { mutableStateOf(text ?: "") }
    AlertDialog(
      onDismissRequest = { showDialog = false },
      title = { Text(title) },
      text = {
        OutlinedTextField(
          value = inputText,
          onValueChange = { inputText = it },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            text = inputText
            showDialog = false
          }
        ) {
          Text(stringResource(R.string.ok))
        }
      },
      dismissButton = {
        Row {
          TextButton(
            onClick = {
              text = null
              showDialog = false
            }
          ) {
            Text(stringResource(R.string.reset))
          }
          TextButton(onClick = { showDialog = false }) { Text(stringResource(R.string.cancel)) }
        }
      },
    )
  }
}

@Composable
private fun OverrideEditTextListPreferenceItem(
  title: String,
  state: MutableState<List<String>?>,
  placeholder: String,
  enabled: Boolean = true,
) {
  var values by state
  var showDialog by remember { mutableStateOf(false) }
  val summary = values?.firstOrNull() ?: placeholder
  Preference(
    modifier = Modifier.fillMaxWidth(),
    title = { Text(title) },
    summary = { Text(summary) },
    enabled = enabled,
    onClick = { showDialog = true },
  )
  if (showDialog) {
    var inputText by remember { mutableStateOf(values?.joinToString("\n") ?: "") }
    AlertDialog(
      onDismissRequest = { showDialog = false },
      title = { Text(title) },
      text = {
        OutlinedTextField(
          value = inputText,
          onValueChange = { inputText = it },
          minLines = 4,
          modifier = Modifier.fillMaxWidth(),
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            val items = inputText.lines().filter { it.isNotBlank() }
            values = items.takeIf { it.isNotEmpty() }
            showDialog = false
          }
        ) {
          Text(stringResource(R.string.ok))
        }
      },
      dismissButton = {
        Row {
          TextButton(
            onClick = {
              values = null
              showDialog = false
            }
          ) {
            Text(stringResource(R.string.reset))
          }
          TextButton(onClick = { showDialog = false }) { Text(stringResource(R.string.cancel)) }
        }
      },
    )
  }
}

@Composable
private fun OverrideEditTextMapPreferenceItem(
  title: String,
  state: MutableState<Map<String, String>?>,
  placeholder: String,
  enabled: Boolean = true,
) {
  var values by state
  var showDialog by remember { mutableStateOf(false) }
  val summary = values?.entries?.firstOrNull()?.let { "${it.key}=${it.value}" } ?: placeholder
  Preference(
    modifier = Modifier.fillMaxWidth(),
    title = { Text(title) },
    summary = { Text(summary) },
    enabled = enabled,
    onClick = { showDialog = true },
  )
  if (showDialog) {
    var inputText by remember {
      mutableStateOf(values?.entries?.joinToString("\n") { "${it.key}=${it.value}" } ?: "")
    }
    AlertDialog(
      onDismissRequest = { showDialog = false },
      title = { Text(title) },
      text = {
        OutlinedTextField(
          value = inputText,
          onValueChange = { inputText = it },
          minLines = 4,
          modifier = Modifier.fillMaxWidth(),
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            val map =
              inputText
                .lines()
                .filter { it.isNotBlank() }
                .mapNotNull { line ->
                  val idx = line.indexOf('=')
                  if (idx > 0) line.substring(0, idx) to line.substring(idx + 1) else null
                }
                .toMap()
            values = map.takeIf { it.isNotEmpty() }
            showDialog = false
          }
        ) {
          Text(stringResource(R.string.ok))
        }
      },
      dismissButton = {
        Row {
          TextButton(
            onClick = {
              values = null
              showDialog = false
            }
          ) {
            Text(stringResource(R.string.reset))
          }
          TextButton(onClick = { showDialog = false }) { Text(stringResource(R.string.cancel)) }
        }
      },
    )
  }
}

@Composable
private fun <T> rememberWriteThroughState(initial: T, sync: (T) -> Unit): MutableState<T> =
  remember {
    object : MutableState<T> {
      private val inner = mutableStateOf(initial)

      override var value: T
        get() = inner.value
        set(v) {
          inner.value = v
          sync(v)
        }

      override fun component1(): T = value

      override fun component2(): (T) -> Unit = { value = it }
    }
  }

@Composable
private fun booleanText(v: Boolean?) =
  stringResource(
    when (v) {
      null -> R.string.dont_modify
      true -> R.string.enabled
      false -> R.string.disabled
    }
  )

@Composable
private fun modeText(v: TunnelState.Mode?) =
  stringResource(
    when (v) {
      TunnelState.Mode.Direct -> R.string.direct_mode
      TunnelState.Mode.Global -> R.string.global_mode
      TunnelState.Mode.Rule -> R.string.rule_mode
      else -> R.string.dont_modify
    }
  )

@Composable
private fun logLevelText(v: LogMessage.Level?) =
  stringResource(
    when (v) {
      LogMessage.Level.Info -> R.string.info
      LogMessage.Level.Warning -> R.string.warning
      LogMessage.Level.Error -> R.string.error
      LogMessage.Level.Debug -> R.string.debug
      LogMessage.Level.Silent -> R.string.silent
      else -> R.string.dont_modify
    }
  )

@Composable
private fun enhancedModeText(v: ConfigurationOverride.DnsEnhancedMode?) =
  stringResource(
    when (v) {
      ConfigurationOverride.DnsEnhancedMode.None -> R.string.disabled
      ConfigurationOverride.DnsEnhancedMode.FakeIp -> R.string.fakeip
      ConfigurationOverride.DnsEnhancedMode.Mapping -> R.string.mapping
      else -> R.string.dont_modify
    }
  )

@Composable
private fun filterModeText(v: ConfigurationOverride.FilterMode?) =
  stringResource(
    when (v) {
      ConfigurationOverride.FilterMode.BlackList -> R.string.blacklist
      ConfigurationOverride.FilterMode.WhiteList -> R.string.whitelist
      else -> R.string.dont_modify
    }
  )

private val booleanOptions: List<Boolean?> = listOf(null, true, false)

private fun portText(port: Int?): String? =
  when {
    port == null -> null
    port <= 0 -> ""
    else -> port.toString()
  }

private fun parsePort(text: String?): Int? =
  when {
    text == null -> null
    else -> text.toIntOrNull() ?: 0
  }

@PreviewMihomo
@Composable
private fun OverrideSettingsScreenPreview() = MihomoTheme {
  OverrideSettingsScreen(configuration = ConfigurationOverride(), onResetConfirmed = {})
}
