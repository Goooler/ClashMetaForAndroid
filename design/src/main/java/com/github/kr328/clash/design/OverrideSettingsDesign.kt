package com.github.kr328.clash.design

import android.content.Context
import android.view.View
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
      title = R.string.http_port,
      placeholder = R.string.dont_modify,
      emptyLabel = R.string.disabled,
      state =
        rememberWriteThroughState(portText(configuration.httpPort)) {
          configuration.httpPort = parsePort(it)
        },
    )
  }
  item(key = "socksPort", contentType = "EditTextPreference") {
    OverrideEditTextPreferenceItem(
      title = R.string.socks_port,
      placeholder = R.string.dont_modify,
      emptyLabel = R.string.disabled,
      state =
        rememberWriteThroughState(portText(configuration.socksPort)) {
          configuration.socksPort = parsePort(it)
        },
    )
  }
  item(key = "redirectPort", contentType = "EditTextPreference") {
    OverrideEditTextPreferenceItem(
      title = R.string.redirect_port,
      placeholder = R.string.dont_modify,
      emptyLabel = R.string.disabled,
      state =
        rememberWriteThroughState(portText(configuration.redirectPort)) {
          configuration.redirectPort = parsePort(it)
        },
    )
  }
  item(key = "tproxyPort", contentType = "EditTextPreference") {
    OverrideEditTextPreferenceItem(
      title = R.string.tproxy_port,
      placeholder = R.string.dont_modify,
      emptyLabel = R.string.disabled,
      state =
        rememberWriteThroughState(portText(configuration.tproxyPort)) {
          configuration.tproxyPort = parsePort(it)
        },
    )
  }
  item(key = "mixedPort", contentType = "EditTextPreference") {
    OverrideEditTextPreferenceItem(
      title = R.string.mixed_port,
      placeholder = R.string.dont_modify,
      emptyLabel = R.string.disabled,
      state =
        rememberWriteThroughState(portText(configuration.mixedPort)) {
          configuration.mixedPort = parsePort(it)
        },
    )
  }
  item(key = "authentication", contentType = "EditTextListPreference") {
    OverrideEditTextListPreferenceItem(
      title = R.string.authentication,
      placeholder = R.string.dont_modify,
      state =
        rememberWriteThroughState(configuration.authentication) {
          configuration.authentication = it
        },
    )
  }
  item(key = "allowLan", contentType = "ListPreference") {
    val state = rememberWriteThroughState(configuration.allowLan) { configuration.allowLan = it }
    val value by state
    OverrideListPreferenceItem(
      state = state,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      title = R.string.allow_lan,
      summary = value.textRes,
      valueToText = { it.textRes },
    )
  }
  item(key = "ipv6", contentType = "ListPreference") {
    val state = rememberWriteThroughState(configuration.ipv6) { configuration.ipv6 = it }
    val value by state
    OverrideListPreferenceItem(
      state = state,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      title = R.string.ipv6,
      summary = value.textRes,
      valueToText = { it.textRes },
    )
  }
  item(key = "bindAddress", contentType = "EditTextPreference") {
    OverrideEditTextPreferenceItem(
      title = R.string.bind_address,
      placeholder = R.string.dont_modify,
      emptyLabel = R.string.default_,
      state =
        rememberWriteThroughState(configuration.bindAddress) { configuration.bindAddress = it },
    )
  }
  item(key = "externalController", contentType = "EditTextPreference") {
    OverrideEditTextPreferenceItem(
      title = R.string.external_controller,
      placeholder = R.string.dont_modify,
      emptyLabel = R.string.default_,
      state =
        rememberWriteThroughState(configuration.externalController) {
          configuration.externalController = it
        },
    )
  }
  item(key = "externalControllerTls", contentType = "EditTextPreference") {
    OverrideEditTextPreferenceItem(
      title = R.string.external_controller_tls,
      placeholder = R.string.dont_modify,
      emptyLabel = R.string.default_,
      state =
        rememberWriteThroughState(configuration.externalControllerTLS) {
          configuration.externalControllerTLS = it
        },
    )
  }
  item(key = "allowOrigins", contentType = "EditTextListPreference") {
    OverrideEditTextListPreferenceItem(
      title = R.string.allow_origins,
      placeholder = R.string.dont_modify,
      state =
        rememberWriteThroughState(configuration.externalControllerCors.allowOrigins) {
          configuration.externalControllerCors.allowOrigins = it
        },
    )
  }
  item(key = "allowPrivateNetwork", contentType = "ListPreference") {
    val state =
      rememberWriteThroughState(configuration.externalControllerCors.allowPrivateNetwork) {
        configuration.externalControllerCors.allowPrivateNetwork = it
      }
    val value by state
    OverrideListPreferenceItem(
      state = state,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      title = R.string.allow_private_network,
      summary = value.textRes,
      valueToText = { it.textRes },
    )
  }
  item(key = "secret", contentType = "EditTextPreference") {
    OverrideEditTextPreferenceItem(
      title = R.string.secret,
      placeholder = R.string.dont_modify,
      emptyLabel = R.string.default_,
      state = rememberWriteThroughState(configuration.secret) { configuration.secret = it },
    )
  }
  item(key = "mode", contentType = "ListPreference") {
    val state = rememberWriteThroughState(configuration.mode) { configuration.mode = it }
    val value by state
    OverrideListPreferenceItem(
      state = state,
      values = TunnelState.Mode.entries,
      modifier = Modifier.fillMaxWidth(),
      title = R.string.mode,
      summary = value.textRes,
      valueToText = { it.textRes },
    )
  }
  item(key = "logLevel", contentType = "ListPreference") {
    val state = rememberWriteThroughState(configuration.logLevel) { configuration.logLevel = it }
    val value by state
    OverrideListPreferenceItem(
      state = state,
      values = LogMessage.Level.entries,
      modifier = Modifier.fillMaxWidth(),
      title = R.string.log_level,
      summary = value.textRes,
      valueToText = { it.textRes },
    )
  }
  item(key = "hosts", contentType = "EditTextMapPreference") {
    OverrideEditTextMapPreferenceItem(
      title = R.string.hosts,
      placeholder = R.string.dont_modify,
      state = rememberWriteThroughState(configuration.hosts) { configuration.hosts = it },
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
    OverrideListPreferenceItem(
      state = dnsEnableState,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      title = R.string.strategy,
      summary = dnsEnabled.dnsStrategyTextRes,
      valueToText = { it.dnsStrategyTextRes },
    )
  }
  item(key = "dnsPreferH3", contentType = "ListPreference") {
    val state =
      rememberWriteThroughState(configuration.dns.preferH3) { configuration.dns.preferH3 = it }
    val value by state
    OverrideListPreferenceItem(
      state = state,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      enabled = dnsEnabled != false,
      title = R.string.prefer_h3,
      summary = value.textRes,
      valueToText = { it.textRes },
    )
  }
  item(key = "dnsListen", contentType = "EditTextPreference") {
    OverrideEditTextPreferenceItem(
      title = R.string.listen,
      placeholder = R.string.dont_modify,
      emptyLabel = R.string.disabled,
      state = rememberWriteThroughState(configuration.dns.listen) { configuration.dns.listen = it },
      enabled = dnsEnabled != false,
    )
  }
  item(key = "appendSystemDns", contentType = "ListPreference") {
    val state =
      rememberWriteThroughState(configuration.app.appendSystemDns) {
        configuration.app.appendSystemDns = it
      }
    val value by state
    OverrideListPreferenceItem(
      state = state,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      enabled = dnsEnabled != false,
      title = R.string.append_system_dns,
      summary = value.textRes,
      valueToText = { it.textRes },
    )
  }
  item(key = "dnsIpv6", contentType = "ListPreference") {
    val state = rememberWriteThroughState(configuration.dns.ipv6) { configuration.dns.ipv6 = it }
    val value by state
    OverrideListPreferenceItem(
      state = state,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      enabled = dnsEnabled != false,
      title = R.string.ipv6,
      summary = value.textRes,
      valueToText = { it.textRes },
    )
  }
  item(key = "dnsUseHosts", contentType = "ListPreference") {
    val state =
      rememberWriteThroughState(configuration.dns.useHosts) { configuration.dns.useHosts = it }
    val value by state
    OverrideListPreferenceItem(
      state = state,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      enabled = dnsEnabled != false,
      title = R.string.use_hosts,
      summary = value.textRes,
      valueToText = { it.textRes },
    )
  }
  item(key = "dnsEnhancedMode", contentType = "ListPreference") {
    val state =
      rememberWriteThroughState(configuration.dns.enhancedMode) {
        configuration.dns.enhancedMode = it
      }
    val value by state
    OverrideListPreferenceItem(
      state = state,
      values = ConfigurationOverride.DnsEnhancedMode.entries,
      modifier = Modifier.fillMaxWidth(),
      enabled = dnsEnabled != false,
      title = R.string.enhanced_mode,
      summary = value.textRes,
      valueToText = { it.textRes },
    )
  }
  item(key = "dnsNameServer", contentType = "EditTextListPreference") {
    OverrideEditTextListPreferenceItem(
      title = R.string.name_server,
      placeholder = R.string.dont_modify,
      state =
        rememberWriteThroughState(configuration.dns.nameServer) {
          configuration.dns.nameServer = it
        },
      enabled = dnsEnabled != false,
    )
  }
  item(key = "dnsFallback", contentType = "EditTextListPreference") {
    OverrideEditTextListPreferenceItem(
      title = R.string.fallback,
      placeholder = R.string.dont_modify,
      state =
        rememberWriteThroughState(configuration.dns.fallback) { configuration.dns.fallback = it },
      enabled = dnsEnabled != false,
    )
  }
  item(key = "dnsDefaultServer", contentType = "EditTextListPreference") {
    OverrideEditTextListPreferenceItem(
      title = R.string.default_name_server,
      placeholder = R.string.dont_modify,
      state =
        rememberWriteThroughState(configuration.dns.defaultServer) {
          configuration.dns.defaultServer = it
        },
      enabled = dnsEnabled != false,
    )
  }
  item(key = "dnsFakeIpFilter", contentType = "EditTextListPreference") {
    OverrideEditTextListPreferenceItem(
      title = R.string.fakeip_filter,
      placeholder = R.string.dont_modify,
      state =
        rememberWriteThroughState(configuration.dns.fakeIpFilter) {
          configuration.dns.fakeIpFilter = it
        },
      enabled = dnsEnabled != false,
    )
  }
  item(key = "dnsFakeIpFilterMode", contentType = "ListPreference") {
    val state =
      rememberWriteThroughState(configuration.dns.fakeIPFilterMode) {
        configuration.dns.fakeIPFilterMode = it
      }
    val value by state
    OverrideListPreferenceItem(
      state = state,
      values = ConfigurationOverride.FilterMode.entries,
      modifier = Modifier.fillMaxWidth(),
      enabled = dnsEnabled != false,
      title = R.string.fakeip_filter_mode,
      summary = value.textRes,
      valueToText = { it.textRes },
    )
  }
  item(key = "dnsGeoIpFallback", contentType = "ListPreference") {
    val state =
      rememberWriteThroughState(configuration.dns.fallbackFilter.geoIp) {
        configuration.dns.fallbackFilter.geoIp = it
      }
    val value by state
    OverrideListPreferenceItem(
      state = state,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      enabled = dnsEnabled != false,
      title = R.string.geoip_fallback,
      summary = value.textRes,
      valueToText = { it.textRes },
    )
  }
  item(key = "dnsGeoIpCode", contentType = "EditTextPreference") {
    OverrideEditTextPreferenceItem(
      title = R.string.geoip_fallback_code,
      placeholder = R.string.dont_modify,
      emptyLabel = R.string.raw_cn,
      state =
        rememberWriteThroughState(configuration.dns.fallbackFilter.geoIpCode) {
          configuration.dns.fallbackFilter.geoIpCode = it
        },
      enabled = dnsEnabled != false,
    )
  }
  item(key = "dnsDomainFallback", contentType = "EditTextListPreference") {
    OverrideEditTextListPreferenceItem(
      title = R.string.domain_fallback,
      placeholder = R.string.dont_modify,
      state =
        rememberWriteThroughState(configuration.dns.fallbackFilter.domain) {
          configuration.dns.fallbackFilter.domain = it
        },
      enabled = dnsEnabled != false,
    )
  }
  item(key = "dnsIpcidrFallback", contentType = "EditTextListPreference") {
    OverrideEditTextListPreferenceItem(
      title = R.string.ipcidr_fallback,
      placeholder = R.string.dont_modify,
      state =
        rememberWriteThroughState(configuration.dns.fallbackFilter.ipcidr) {
          configuration.dns.fallbackFilter.ipcidr = it
        },
      enabled = dnsEnabled != false,
    )
  }
  item(key = "dnsNameserverPolicy", contentType = "EditTextMapPreference") {
    OverrideEditTextMapPreferenceItem(
      title = R.string.name_server_policy,
      placeholder = R.string.dont_modify,
      state =
        rememberWriteThroughState(configuration.dns.nameserverPolicy) {
          configuration.dns.nameserverPolicy = it
        },
      enabled = dnsEnabled != false,
    )
  }
}

@Composable
private fun OverrideEditTextPreferenceItem(
  @StringRes title: Int,
  @StringRes placeholder: Int,
  @StringRes emptyLabel: Int,
  state: MutableState<String?>,
  enabled: Boolean = true,
) {
  var text by state
  var showDialog by remember { mutableStateOf(false) }
  val summary =
    when {
      text == null -> stringResource(placeholder)
      text.isNullOrEmpty() -> stringResource(emptyLabel)
      else -> text.orEmpty()
    }
  Preference(
    modifier = Modifier.fillMaxWidth(),
    title = { Text(stringResource(title)) },
    summary = { Text(summary) },
    enabled = enabled,
    onClick = { showDialog = true },
  )
  if (showDialog) {
    var inputText by remember {
      mutableStateOf(
        TextFieldValue(text = text.orEmpty(), selection = TextRange(text.orEmpty().length))
      )
    }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
      focusRequester.requestFocus()
      keyboardController?.show()
    }
    AlertDialog(
      onDismissRequest = { showDialog = false },
      title = { Text(stringResource(title)) },
      text = {
        OutlinedTextField(
          value = inputText,
          onValueChange = { inputText = it },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            text = inputText.text
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
private fun <T> OverrideListPreferenceItem(
  state: MutableState<T>,
  values: List<T>,
  @StringRes title: Int,
  @StringRes summary: Int,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  valueToText: @Composable (T) -> Int,
) {
  ListPreference(
    state = state,
    values = values,
    modifier = modifier,
    enabled = enabled,
    title = { Text(stringResource(title)) },
    summary = { Text(stringResource(summary)) },
    valueToText = { AnnotatedString(stringResource(valueToText(it))) },
  )
}

@Composable
private fun OverrideEditTextListPreferenceItem(
  @StringRes title: Int,
  @StringRes placeholder: Int,
  state: MutableState<List<String>?>,
  enabled: Boolean = true,
) {
  var values by state
  var showDialog by remember { mutableStateOf(false) }
  Preference(
    modifier = Modifier.fillMaxWidth(),
    title = { Text(stringResource(title)) },
    summary = { Text(values.summary(placeholder)) },
    enabled = enabled,
    onClick = { showDialog = true },
  )
  if (showDialog) {
    EditableTextListDialog(
      title = title,
      initialValues = values,
      onDismiss = { showDialog = false },
      onApply = {
        values = it
        showDialog = false
      },
    )
  }
}

@Composable
private fun OverrideEditTextMapPreferenceItem(
  @StringRes title: Int,
  @StringRes placeholder: Int,
  state: MutableState<Map<String, String>?>,
  enabled: Boolean = true,
) {
  var values by state
  var showDialog by remember { mutableStateOf(false) }
  Preference(
    modifier = Modifier.fillMaxWidth(),
    title = { Text(stringResource(title)) },
    summary = { Text(values.summary(placeholder)) },
    enabled = enabled,
    onClick = { showDialog = true },
  )
  if (showDialog) {
    EditableTextMapDialog(
      title = title,
      initialValues = values,
      onDismiss = { showDialog = false },
      onApply = {
        values = it
        showDialog = false
      },
    )
  }
}

@Composable
private fun EditableTextListDialog(
  @StringRes title: Int,
  initialValues: List<String>?,
  onDismiss: () -> Unit,
  onApply: (List<String>?) -> Unit,
) {
  var values by remember(initialValues) { mutableStateOf(initialValues.orEmpty()) }
  var showAddDialog by remember { mutableStateOf(false) }

  FullScreenPreferenceDialog(
    title = title,
    onDismiss = onDismiss,
    onAdd = { showAddDialog = true },
    onReset = { onApply(null) },
    onConfirm = { onApply(values) },
  ) { modifier ->
    if (values.isEmpty()) {
      EmptyEditorContent(modifier)
    } else {
      LazyColumn(modifier = modifier) {
        itemsIndexed(values) { index, value ->
          ListItem(
            headlineContent = { Text(value) },
            trailingContent = {
              IconButton(onClick = { values = values.toMutableList().apply { removeAt(index) } }) {
                Icon(
                  painter = painterResource(R.drawable.ic_outline_delete),
                  contentDescription = stringResource(R.string.delete),
                )
              }
            },
          )
          HorizontalDivider()
        }
      }
    }
  }

  if (showAddDialog) {
    SingleTextInputDialog(
      title = title,
      initialValue = "",
      onDismiss = { showAddDialog = false },
      onConfirm = { newValue ->
        if (newValue.isNotBlank()) {
          values = values + newValue
        }
        showAddDialog = false
      },
    )
  }
}

@Composable
private fun EditableTextMapDialog(
  @StringRes title: Int,
  initialValues: Map<String, String>?,
  onDismiss: () -> Unit,
  onApply: (Map<String, String>?) -> Unit,
) {
  var values by
    remember(initialValues) {
      mutableStateOf(initialValues?.entries?.map { it.toPair() }.orEmpty())
    }
  var showAddDialog by remember { mutableStateOf(false) }

  FullScreenPreferenceDialog(
    title = title,
    onDismiss = onDismiss,
    onAdd = { showAddDialog = true },
    onReset = { onApply(null) },
    onConfirm = { onApply(values.toMap()) },
  ) { modifier ->
    if (values.isEmpty()) {
      EmptyEditorContent(modifier)
    } else {
      LazyColumn(modifier = modifier) {
        itemsIndexed(values) { index, entry ->
          ListItem(
            headlineContent = { Text(entry.first) },
            supportingContent = { Text(entry.second) },
            trailingContent = {
              IconButton(onClick = { values = values.toMutableList().apply { removeAt(index) } }) {
                Icon(
                  painter = painterResource(R.drawable.ic_outline_delete),
                  contentDescription = stringResource(R.string.delete),
                )
              }
            },
          )
          HorizontalDivider()
        }
      }
    }
  }

  if (showAddDialog) {
    MapEntryInputDialog(
      title = title,
      onDismiss = { showAddDialog = false },
      onConfirm = { key, value ->
        values = values + (key to value)
        showAddDialog = false
      },
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FullScreenPreferenceDialog(
  @StringRes title: Int,
  onDismiss: () -> Unit,
  onAdd: () -> Unit,
  onReset: () -> Unit,
  onConfirm: () -> Unit,
  content: @Composable (Modifier) -> Unit,
) {
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    MihomoScaffold(
      modifier = Modifier.fillMaxSize(),
      title = stringResource(title),
      onBack = onDismiss,
      actions = {
        IconButton(onClick = onAdd) {
          Icon(
            painter = painterResource(R.drawable.ic_baseline_add),
            contentDescription = stringResource(R.string._new),
          )
        }
      },
    ) { innerPadding ->
      Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        content(Modifier.weight(1f))
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.End,
        ) {
          TextButton(onClick = onReset) { Text(stringResource(R.string.reset)) }
          TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
          TextButton(onClick = onConfirm) { Text(stringResource(R.string.ok)) }
        }
      }
    }
  }
}

@Composable
private fun EmptyEditorContent(modifier: Modifier = Modifier) {
  Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
    Text(stringResource(R.string.empty))
  }
}

@Composable
private fun SingleTextInputDialog(
  @StringRes title: Int,
  initialValue: String,
  onDismiss: () -> Unit,
  onConfirm: (String) -> Unit,
) {
  var inputText by remember { mutableStateOf(initialTextFieldValue(initialValue)) }
  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  LaunchedEffect(Unit) {
    focusRequester.requestFocus()
    keyboardController?.show()
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(title)) },
    text = {
      OutlinedTextField(
        value = inputText,
        onValueChange = { inputText = it },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
      )
    },
    confirmButton = {
      TextButton(onClick = { onConfirm(inputText.text) }) { Text(stringResource(R.string.ok)) }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
  )
}

@Composable
private fun MapEntryInputDialog(
  @StringRes title: Int,
  onDismiss: () -> Unit,
  onConfirm: (String, String) -> Unit,
) {
  var keyText by remember { mutableStateOf(initialTextFieldValue("")) }
  var valueText by remember { mutableStateOf(initialTextFieldValue("")) }
  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current
  val confirmEnabled = keyText.text.isNotBlank() && valueText.text.isNotBlank()

  LaunchedEffect(Unit) {
    focusRequester.requestFocus()
    keyboardController?.show()
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(title)) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
          value = keyText,
          onValueChange = { keyText = it },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
        )
        OutlinedTextField(
          value = valueText,
          onValueChange = { valueText = it },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    },
    confirmButton = {
      TextButton(
        onClick = { onConfirm(keyText.text.trim(), valueText.text.trim()) },
        enabled = confirmEnabled,
      ) {
        Text(stringResource(R.string.ok))
      }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
  )
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

private fun initialTextFieldValue(text: String) =
  TextFieldValue(text = text, selection = TextRange(text.length))

@Composable
private fun List<String>?.summary(@StringRes placeholder: Int) =
  when {
    this == null -> stringResource(placeholder)
    isEmpty() -> stringResource(R.string.empty)
    else -> stringResource(R.string.format_elements, size)
  }

@Composable
private fun Map<String, String>?.summary(@StringRes placeholder: Int) =
  when {
    this == null -> stringResource(placeholder)
    isEmpty() -> stringResource(R.string.empty)
    else -> stringResource(R.string.format_elements, size)
  }

private val Boolean?.textRes: Int
  @StringRes
  get() =
    when (this) {
      true -> R.string.enabled
      false -> R.string.disabled
      null -> R.string.dont_modify
    }

private val Boolean?.dnsStrategyTextRes: Int
  @StringRes
  get() =
    when (this) {
      true -> R.string.force_enable
      false -> R.string.use_built_in
      null -> R.string.dont_modify
    }

private val TunnelState.Mode?.textRes: Int
  @StringRes
  get() =
    when (this) {
      TunnelState.Mode.Direct -> R.string.direct_mode
      TunnelState.Mode.Global -> R.string.global_mode
      TunnelState.Mode.Rule -> R.string.rule_mode
      null -> R.string.dont_modify
    }

private val LogMessage.Level?.textRes: Int
  @StringRes
  get() =
    when (this) {
      LogMessage.Level.Info -> R.string.info
      LogMessage.Level.Warning -> R.string.warning
      LogMessage.Level.Error -> R.string.error
      LogMessage.Level.Debug -> R.string.debug
      LogMessage.Level.Silent -> R.string.silent
      LogMessage.Level.Unknown -> R.string.unknown
      null -> R.string.dont_modify
    }

private val ConfigurationOverride.DnsEnhancedMode?.textRes: Int
  @StringRes
  get() =
    when (this) {
      ConfigurationOverride.DnsEnhancedMode.None -> R.string.disabled
      ConfigurationOverride.DnsEnhancedMode.FakeIp -> R.string.fakeip
      ConfigurationOverride.DnsEnhancedMode.Mapping -> R.string.mapping
      null -> R.string.dont_modify
    }

private val ConfigurationOverride.FilterMode?.textRes: Int
  @StringRes
  get() =
    when (this) {
      ConfigurationOverride.FilterMode.BlackList -> R.string.blacklist
      ConfigurationOverride.FilterMode.WhiteList -> R.string.whitelist
      null -> R.string.dont_modify
    }

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
