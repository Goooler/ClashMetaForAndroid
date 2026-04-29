package com.github.kr328.clash.settings.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.github.kr328.clash.R
import com.github.kr328.clash.core.model.ConfigurationOverride
import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.core.model.TunnelState
import com.github.kr328.clash.nav.MihomoNavDisplay
import com.github.kr328.clash.nav.addIfNotLast
import com.github.kr328.clash.nav.rememberNavBackStackBuilder
import com.github.kr328.clash.settings.vm.OverrideSettingsViewModel
import com.github.kr328.clash.ui.component.MihomoScaffold
import com.github.kr328.clash.ui.icon.BaselineReplay
import com.github.kr328.clash.ui.icon.MihomoIcons
import com.github.kr328.clash.ui.theme.MihomoTheme
import com.github.kr328.clash.ui.theme.PreviewMihomo
import kotlinx.serialization.Serializable
import me.zhanghai.compose.preference.ListPreference
import me.zhanghai.compose.preference.Preference
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.preferenceCategory

private sealed interface OverrideSettingsRoute : NavKey {
  @Serializable data object Main : OverrideSettingsRoute
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun OverrideSettingsScreen(
  modifier: Modifier = Modifier,
  viewModel: OverrideSettingsViewModel = viewModel(),
  onResetCompleted: () -> Unit,
) {
  val backStack = rememberNavBackStackBuilder { add(OverrideSettingsRoute.Main) }
  var currentEditableTextMapOnApply by remember {
    mutableStateOf<((Map<String, String>?) -> Unit)?>(null)
  }
  var currentEditableTextListOnApply by remember {
    mutableStateOf<((List<String>?) -> Unit)?>(null)
  }

  DisposableEffect(viewModel) { onDispose { viewModel.persistOverride() } }

  MihomoNavDisplay(
    backStack = backStack,
    entryProvider =
      entryProvider {
        entry<OverrideSettingsRoute.Main> {
          val configuration by viewModel.configuration.collectAsStateWithLifecycle()
          var showResetConfirmDialog by remember { mutableStateOf(false) }

          OverrideSettingsContent(
            configuration = configuration,
            actions = viewModel,
            modifier = modifier,
            showResetConfirmDialog = showResetConfirmDialog,
            onShowResetConfirmDialogChange = { showResetConfirmDialog = it },
            onResetConfirmed = {
              viewModel.resetOverride()
              onResetCompleted()
            },
            onOpenEditableTextMap = { title, initialValues, onApply ->
              currentEditableTextMapOnApply = onApply
              backStack.addIfNotLast(EditableTextMap(title, initialValues))
            },
            onOpenEditableTextList = { title, initialValues, onApply ->
              currentEditableTextListOnApply = onApply
              backStack.addIfNotLast(EditableTextList(title, initialValues))
            },
          )
        }
        editableTextMapScreenEntry(
          onDismiss = {
            currentEditableTextMapOnApply = null
            backStack.removeLastOrNull()
          },
          onApply = { newValues ->
            currentEditableTextMapOnApply?.invoke(newValues)
            currentEditableTextMapOnApply = null
            backStack.removeLastOrNull()
          },
        )
        editableTextListScreenEntry(
          onDismiss = {
            currentEditableTextListOnApply = null
            backStack.removeLastOrNull()
          },
          onApply = { newValues ->
            currentEditableTextListOnApply?.invoke(newValues)
            currentEditableTextListOnApply = null
            backStack.removeLastOrNull()
          },
        )
      },
  )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun OverrideSettingsContent(
  configuration: ConfigurationOverride,
  actions: OverrideSettingsActions,
  modifier: Modifier = Modifier,
  showResetConfirmDialog: Boolean,
  onShowResetConfirmDialogChange: (Boolean) -> Unit,
  onResetConfirmed: () -> Unit,
  onOpenEditableTextMap: (Int, Map<String, String>?, (Map<String, String>?) -> Unit) -> Unit,
  onOpenEditableTextList: (Int, List<String>?, (List<String>?) -> Unit) -> Unit,
) {
  val dnsEnabled = configuration.dns.enable

  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
  MihomoScaffold(
    title = stringResource(R.string.override),
    modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    scrollBehavior = scrollBehavior,
    actions = {
      IconButton(onClick = { onShowResetConfirmDialogChange(true) }) {
        Icon(
          imageVector = MihomoIcons.BaselineReplay,
          contentDescription = stringResource(R.string.reset),
        )
      }
    },
  ) { innerPadding ->
    ProvidePreferenceLocals {
      LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = innerPadding) {
        generalPreferenceItems(
          configuration,
          actions,
          onOpenEditableTextMap,
          onOpenEditableTextList,
        )
        dnsPreferenceItems(
          configuration,
          actions,
          dnsEnabled,
          onOpenEditableTextMap,
          onOpenEditableTextList,
        )
      }

      if (showResetConfirmDialog) {
        AlertDialog(
          onDismissRequest = { onShowResetConfirmDialogChange(false) },
          title = { Text(stringResource(R.string.reset_override_settings)) },
          text = { Text(stringResource(R.string.reset_override_settings_message)) },
          confirmButton = {
            TextButton(
              onClick = {
                onShowResetConfirmDialogChange(false)
                onResetConfirmed()
              }
            ) {
              Text(stringResource(R.string.ok))
            }
          },
          dismissButton = {
            TextButton(onClick = { onShowResetConfirmDialogChange(false) }) {
              Text(stringResource(R.string.cancel))
            }
          },
        )
      }
    }
  }
}

private fun LazyListScope.generalPreferenceItems(
  configuration: ConfigurationOverride,
  actions: OverrideSettingsActions,
  onOpenEditableTextMap: (Int, Map<String, String>?, (Map<String, String>?) -> Unit) -> Unit,
  onOpenEditableTextList: (Int, List<String>?, (List<String>?) -> Unit) -> Unit,
) {
  preferenceCategory(key = "cat_general", title = { Text(stringResource(R.string.general)) })
  item(key = "httpPort", contentType = "EditTextPreference") {
    OverrideEditTextPreferenceItem(
      title = R.string.http_port,
      placeholder = R.string.dont_modify,
      emptyLabel = R.string.disabled,
      value = portText(configuration.httpPort),
      onValueChange = { actions.updateHttpPort(parsePort(it)) },
      numericOnly = true,
    )
  }
  item(key = "socksPort", contentType = "EditTextPreference") {
    OverrideEditTextPreferenceItem(
      title = R.string.socks_port,
      placeholder = R.string.dont_modify,
      emptyLabel = R.string.disabled,
      value = portText(configuration.socksPort),
      onValueChange = { actions.updateSocksPort(parsePort(it)) },
      numericOnly = true,
    )
  }
  item(key = "redirectPort", contentType = "EditTextPreference") {
    OverrideEditTextPreferenceItem(
      title = R.string.redirect_port,
      placeholder = R.string.dont_modify,
      emptyLabel = R.string.disabled,
      value = portText(configuration.redirectPort),
      onValueChange = { actions.updateRedirectPort(parsePort(it)) },
      numericOnly = true,
    )
  }
  item(key = "tproxyPort", contentType = "EditTextPreference") {
    OverrideEditTextPreferenceItem(
      title = R.string.tproxy_port,
      placeholder = R.string.dont_modify,
      emptyLabel = R.string.disabled,
      value = portText(configuration.tproxyPort),
      onValueChange = { actions.updateTproxyPort(parsePort(it)) },
      numericOnly = true,
    )
  }
  item(key = "mixedPort", contentType = "EditTextPreference") {
    OverrideEditTextPreferenceItem(
      title = R.string.mixed_port,
      placeholder = R.string.dont_modify,
      emptyLabel = R.string.disabled,
      value = portText(configuration.mixedPort),
      onValueChange = { actions.updateMixedPort(parsePort(it)) },
      numericOnly = true,
    )
  }
  item(key = "authentication", contentType = "EditTextListPreference") {
    Preference(
      modifier = Modifier.fillMaxWidth(),
      title = { Text(stringResource(R.string.authentication)) },
      summary = { Text(configuration.authentication.listSummary(R.string.dont_modify)) },
      onClick = {
        onOpenEditableTextList(
          R.string.authentication,
          configuration.authentication,
          actions::updateAuthentication,
        )
      },
    )
  }
  item(key = "allowLan", contentType = "ListPreference") {
    ListPreference(
      value = configuration.allowLan,
      onValueChange = actions::updateAllowLan,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      title = { Text(stringResource(R.string.allow_lan)) },
      summary = { Text(stringResource(configuration.allowLan.textRes)) },
      valueToText = { AnnotatedString(stringResource(it.textRes)) },
    )
  }
  item(key = "ipv6", contentType = "ListPreference") {
    ListPreference(
      value = configuration.ipv6,
      onValueChange = actions::updateIpv6,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      title = { Text(stringResource(R.string.ipv6)) },
      summary = { Text(stringResource(configuration.ipv6.textRes)) },
      valueToText = { AnnotatedString(stringResource(it.textRes)) },
    )
  }
  item(key = "bindAddress", contentType = "EditTextPreference") {
    OverrideEditTextPreferenceItem(
      title = R.string.bind_address,
      placeholder = R.string.dont_modify,
      emptyLabel = R.string.default_,
      value = configuration.bindAddress,
      onValueChange = actions::updateBindAddress,
    )
  }
  item(key = "externalController", contentType = "EditTextPreference") {
    OverrideEditTextPreferenceItem(
      title = R.string.external_controller,
      placeholder = R.string.dont_modify,
      emptyLabel = R.string.default_,
      value = configuration.externalController,
      onValueChange = actions::updateExternalController,
    )
  }
  item(key = "externalControllerTls", contentType = "EditTextPreference") {
    OverrideEditTextPreferenceItem(
      title = R.string.external_controller_tls,
      placeholder = R.string.dont_modify,
      emptyLabel = R.string.default_,
      value = configuration.externalControllerTLS,
      onValueChange = actions::updateExternalControllerTls,
    )
  }
  item(key = "allowOrigins", contentType = "EditTextListPreference") {
    Preference(
      modifier = Modifier.fillMaxWidth(),
      title = { Text(stringResource(R.string.allow_origins)) },
      summary = {
        Text(configuration.externalControllerCors.allowOrigins.listSummary(R.string.dont_modify))
      },
      onClick = {
        onOpenEditableTextList(
          R.string.allow_origins,
          configuration.externalControllerCors.allowOrigins,
          actions::updateAllowOrigins,
        )
      },
    )
  }
  item(key = "allowPrivateNetwork", contentType = "ListPreference") {
    ListPreference(
      value = configuration.externalControllerCors.allowPrivateNetwork,
      onValueChange = actions::updateAllowPrivateNetwork,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      title = { Text(stringResource(R.string.allow_private_network)) },
      summary = {
        Text(stringResource(configuration.externalControllerCors.allowPrivateNetwork.textRes))
      },
      valueToText = { AnnotatedString(stringResource(it.textRes)) },
    )
  }
  item(key = "secret", contentType = "EditTextPreference") {
    OverrideEditTextPreferenceItem(
      title = R.string.secret,
      placeholder = R.string.dont_modify,
      emptyLabel = R.string.default_,
      value = configuration.secret,
      onValueChange = actions::updateSecret,
    )
  }
  item(key = "mode", contentType = "ListPreference") {
    ListPreference(
      value = configuration.mode,
      onValueChange = actions::updateMode,
      values = TunnelState.Mode.entries,
      modifier = Modifier.fillMaxWidth(),
      title = { Text(stringResource(R.string.mode)) },
      summary = { Text(stringResource(configuration.mode.textRes)) },
      valueToText = { AnnotatedString(stringResource(it.textRes)) },
    )
  }
  item(key = "logLevel", contentType = "ListPreference") {
    ListPreference(
      value = configuration.logLevel,
      onValueChange = actions::updateLogLevel,
      values = LogMessage.Level.entries,
      modifier = Modifier.fillMaxWidth(),
      title = { Text(stringResource(R.string.log_level)) },
      summary = { Text(stringResource(configuration.logLevel.textRes)) },
      valueToText = { AnnotatedString(stringResource(it.textRes)) },
    )
  }
  item(key = "hosts", contentType = "EditTextMapPreference") {
    OverrideEditTextMapPreferenceItem(
      title = R.string.hosts,
      placeholder = R.string.dont_modify,
      values = configuration.hosts,
      onClick = { onOpenEditableTextMap(R.string.hosts, configuration.hosts, actions::updateHosts) },
    )
  }
}

private fun LazyListScope.dnsPreferenceItems(
  configuration: ConfigurationOverride,
  actions: OverrideSettingsActions,
  dnsEnabled: Boolean?,
  onOpenEditableTextMap: (Int, Map<String, String>?, (Map<String, String>?) -> Unit) -> Unit,
  onOpenEditableTextList: (Int, List<String>?, (List<String>?) -> Unit) -> Unit,
) {
  preferenceCategory(key = "cat_dns", title = { Text(stringResource(R.string.dns)) })
  item(key = "dnsStrategy", contentType = "ListPreference") {
    ListPreference(
      value = dnsEnabled,
      onValueChange = actions::updateDnsEnable,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      title = { Text(stringResource(R.string.strategy)) },
      summary = { Text(stringResource(dnsEnabled.dnsStrategyTextRes)) },
      valueToText = { AnnotatedString(stringResource(it.dnsStrategyTextRes)) },
    )
  }
  item(key = "dnsPreferH3", contentType = "ListPreference") {
    ListPreference(
      value = configuration.dns.preferH3,
      onValueChange = actions::updateDnsPreferH3,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      enabled = dnsEnabled != false,
      title = { Text(stringResource(R.string.prefer_h3)) },
      summary = { Text(stringResource(configuration.dns.preferH3.textRes)) },
      valueToText = { AnnotatedString(stringResource(it.textRes)) },
    )
  }
  item(key = "dnsListen", contentType = "EditTextPreference") {
    OverrideEditTextPreferenceItem(
      title = R.string.listen,
      placeholder = R.string.dont_modify,
      emptyLabel = R.string.disabled,
      value = configuration.dns.listen,
      onValueChange = actions::updateDnsListen,
      enabled = dnsEnabled != false,
    )
  }
  item(key = "appendSystemDns", contentType = "ListPreference") {
    ListPreference(
      value = configuration.app.appendSystemDns,
      onValueChange = actions::updateAppendSystemDns,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      enabled = dnsEnabled != false,
      title = { Text(stringResource(R.string.append_system_dns)) },
      summary = { Text(stringResource(configuration.app.appendSystemDns.textRes)) },
      valueToText = { AnnotatedString(stringResource(it.textRes)) },
    )
  }
  item(key = "dnsIpv6", contentType = "ListPreference") {
    ListPreference(
      value = configuration.dns.ipv6,
      onValueChange = actions::updateDnsIpv6,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      enabled = dnsEnabled != false,
      title = { Text(stringResource(R.string.ipv6)) },
      summary = { Text(stringResource(configuration.dns.ipv6.textRes)) },
      valueToText = { AnnotatedString(stringResource(it.textRes)) },
    )
  }
  item(key = "dnsUseHosts", contentType = "ListPreference") {
    ListPreference(
      value = configuration.dns.useHosts,
      onValueChange = actions::updateDnsUseHosts,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      enabled = dnsEnabled != false,
      title = { Text(stringResource(R.string.use_hosts)) },
      summary = { Text(stringResource(configuration.dns.useHosts.textRes)) },
      valueToText = { AnnotatedString(stringResource(it.textRes)) },
    )
  }
  item(key = "dnsEnhancedMode", contentType = "ListPreference") {
    ListPreference(
      value = configuration.dns.enhancedMode,
      onValueChange = actions::updateDnsEnhancedMode,
      values = ConfigurationOverride.DnsEnhancedMode.entries,
      modifier = Modifier.fillMaxWidth(),
      enabled = dnsEnabled != false,
      title = { Text(stringResource(R.string.enhanced_mode)) },
      summary = { Text(stringResource(configuration.dns.enhancedMode.textRes)) },
      valueToText = { AnnotatedString(stringResource(it.textRes)) },
    )
  }
  item(key = "dnsNameServer", contentType = "EditTextListPreference") {
    Preference(
      modifier = Modifier.fillMaxWidth(),
      title = { Text(stringResource(R.string.name_server)) },
      summary = { Text(configuration.dns.nameServer.listSummary(R.string.dont_modify)) },
      enabled = dnsEnabled != false,
      onClick = {
        onOpenEditableTextList(
          R.string.name_server,
          configuration.dns.nameServer,
          actions::updateDnsNameServer,
        )
      },
    )
  }
  item(key = "dnsFallback", contentType = "EditTextListPreference") {
    Preference(
      modifier = Modifier.fillMaxWidth(),
      title = { Text(stringResource(R.string.fallback)) },
      summary = { Text(configuration.dns.fallback.listSummary(R.string.dont_modify)) },
      enabled = dnsEnabled != false,
      onClick = {
        onOpenEditableTextList(
          R.string.fallback,
          configuration.dns.fallback,
          actions::updateDnsFallback,
        )
      },
    )
  }
  item(key = "dnsDefaultServer", contentType = "EditTextListPreference") {
    Preference(
      modifier = Modifier.fillMaxWidth(),
      title = { Text(stringResource(R.string.default_name_server)) },
      summary = { Text(configuration.dns.defaultServer.listSummary(R.string.dont_modify)) },
      enabled = dnsEnabled != false,
      onClick = {
        onOpenEditableTextList(
          R.string.default_name_server,
          configuration.dns.defaultServer,
          actions::updateDnsDefaultServer,
        )
      },
    )
  }
  item(key = "dnsFakeIpFilter", contentType = "EditTextListPreference") {
    Preference(
      modifier = Modifier.fillMaxWidth(),
      title = { Text(stringResource(R.string.fakeip_filter)) },
      summary = { Text(configuration.dns.fakeIpFilter.listSummary(R.string.dont_modify)) },
      enabled = dnsEnabled != false,
      onClick = {
        onOpenEditableTextList(
          R.string.fakeip_filter,
          configuration.dns.fakeIpFilter,
          actions::updateDnsFakeIpFilter,
        )
      },
    )
  }
  item(key = "dnsFakeIpFilterMode", contentType = "ListPreference") {
    ListPreference(
      value = configuration.dns.fakeIPFilterMode,
      onValueChange = actions::updateDnsFakeIpFilterMode,
      values = ConfigurationOverride.FilterMode.entries,
      modifier = Modifier.fillMaxWidth(),
      enabled = dnsEnabled != false,
      title = { Text(stringResource(R.string.fakeip_filter_mode)) },
      summary = { Text(stringResource(configuration.dns.fakeIPFilterMode.textRes)) },
      valueToText = { AnnotatedString(stringResource(it.textRes)) },
    )
  }
  item(key = "dnsGeoIpFallback", contentType = "ListPreference") {
    ListPreference(
      value = configuration.dns.fallbackFilter.geoIp,
      onValueChange = actions::updateDnsGeoIpFallback,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      enabled = dnsEnabled != false,
      title = { Text(stringResource(R.string.geoip_fallback)) },
      summary = { Text(stringResource(configuration.dns.fallbackFilter.geoIp.textRes)) },
      valueToText = { AnnotatedString(stringResource(it.textRes)) },
    )
  }
  item(key = "dnsGeoIpCode", contentType = "EditTextPreference") {
    OverrideEditTextPreferenceItem(
      title = R.string.geoip_fallback_code,
      placeholder = R.string.dont_modify,
      emptyLabel = R.string.raw_cn,
      value = configuration.dns.fallbackFilter.geoIpCode,
      onValueChange = actions::updateDnsGeoIpCode,
      enabled = dnsEnabled != false,
    )
  }
  item(key = "dnsDomainFallback", contentType = "EditTextListPreference") {
    Preference(
      modifier = Modifier.fillMaxWidth(),
      title = { Text(stringResource(R.string.domain_fallback)) },
      summary = { Text(configuration.dns.fallbackFilter.domain.listSummary(R.string.dont_modify)) },
      enabled = dnsEnabled != false,
      onClick = {
        onOpenEditableTextList(
          R.string.domain_fallback,
          configuration.dns.fallbackFilter.domain,
          actions::updateDnsDomainFallback,
        )
      },
    )
  }
  item(key = "dnsIpcidrFallback", contentType = "EditTextListPreference") {
    Preference(
      modifier = Modifier.fillMaxWidth(),
      title = { Text(stringResource(R.string.ipcidr_fallback)) },
      summary = { Text(configuration.dns.fallbackFilter.ipcidr.listSummary(R.string.dont_modify)) },
      enabled = dnsEnabled != false,
      onClick = {
        onOpenEditableTextList(
          R.string.ipcidr_fallback,
          configuration.dns.fallbackFilter.ipcidr,
          actions::updateDnsIpcidrFallback,
        )
      },
    )
  }
  item(key = "dnsNameserverPolicy", contentType = "EditTextMapPreference") {
    OverrideEditTextMapPreferenceItem(
      title = R.string.name_server_policy,
      placeholder = R.string.dont_modify,
      values = configuration.dns.nameserverPolicy,
      onClick = {
        onOpenEditableTextMap(
          R.string.name_server_policy,
          configuration.dns.nameserverPolicy,
          actions::updateDnsNameserverPolicy,
        )
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
  value: String?,
  onValueChange: (String?) -> Unit,
  enabled: Boolean = true,
  numericOnly: Boolean = false,
) {
  var showDialog by remember { mutableStateOf(false) }
  val summary =
    when {
      value == null -> stringResource(placeholder)
      value.isEmpty() -> stringResource(emptyLabel)
      else -> value
    }
  Preference(
    modifier = Modifier.fillMaxWidth(),
    title = { Text(stringResource(title)) },
    summary = { Text(summary) },
    enabled = enabled,
    onClick = { showDialog = true },
  )
  if (showDialog) {
    var inputText by
      remember(value) {
        mutableStateOf(
          TextFieldValue(text = value.orEmpty(), selection = TextRange(value.orEmpty().length))
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
          onValueChange = { inputText = if (numericOnly) it.filterDigits() else it },
          keyboardOptions =
            if (numericOnly) {
              KeyboardOptions(keyboardType = KeyboardType.Number)
            } else {
              KeyboardOptions.Default
            },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            onValueChange(
              if (numericOnly) {
                portText(parsePort(inputText.text))
              } else {
                inputText.text
              }
            )
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
              onValueChange(null)
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
  @StringRes title: Int,
  @StringRes placeholder: Int,
  values: Map<String, String>?,
  onClick: () -> Unit,
  enabled: Boolean = true,
) {
  Preference(
    modifier = Modifier.fillMaxWidth(),
    title = { Text(stringResource(title)) },
    summary = { Text(values.summary(placeholder)) },
    enabled = enabled,
    onClick = onClick,
  )
}

private fun TextFieldValue.filterDigits(): TextFieldValue {
  val filtered = text.filter(Char::isDigit)
  if (filtered == text) return this
  val start = text.take(selection.start).count(Char::isDigit)
  val end = text.take(selection.end).count(Char::isDigit)
  return copy(text = filtered, selection = TextRange(start, end))
}

@Composable
private fun List<String>?.listSummary(@StringRes placeholder: Int) =
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

internal val Boolean?.textRes: Int
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

internal val booleanOptions: List<Boolean?> = listOf(null, true, false)

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

interface OverrideSettingsActions {
  fun updateHttpPort(value: Int?) = Unit

  fun updateSocksPort(value: Int?) = Unit

  fun updateRedirectPort(value: Int?) = Unit

  fun updateTproxyPort(value: Int?) = Unit

  fun updateMixedPort(value: Int?) = Unit

  fun updateAuthentication(value: List<String>?) = Unit

  fun updateAllowLan(value: Boolean?) = Unit

  fun updateIpv6(value: Boolean?) = Unit

  fun updateBindAddress(value: String?) = Unit

  fun updateExternalController(value: String?) = Unit

  fun updateExternalControllerTls(value: String?) = Unit

  fun updateAllowOrigins(value: List<String>?) = Unit

  fun updateAllowPrivateNetwork(value: Boolean?) = Unit

  fun updateSecret(value: String?) = Unit

  fun updateMode(value: TunnelState.Mode?) = Unit

  fun updateLogLevel(value: LogMessage.Level?) = Unit

  fun updateHosts(value: Map<String, String>?) = Unit

  fun updateDnsEnable(value: Boolean?) = Unit

  fun updateDnsPreferH3(value: Boolean?) = Unit

  fun updateDnsListen(value: String?) = Unit

  fun updateAppendSystemDns(value: Boolean?) = Unit

  fun updateDnsIpv6(value: Boolean?) = Unit

  fun updateDnsUseHosts(value: Boolean?) = Unit

  fun updateDnsEnhancedMode(value: ConfigurationOverride.DnsEnhancedMode?) = Unit

  fun updateDnsNameServer(value: List<String>?) = Unit

  fun updateDnsFallback(value: List<String>?) = Unit

  fun updateDnsDefaultServer(value: List<String>?) = Unit

  fun updateDnsFakeIpFilter(value: List<String>?) = Unit

  fun updateDnsFakeIpFilterMode(value: ConfigurationOverride.FilterMode?) = Unit

  fun updateDnsGeoIpFallback(value: Boolean?) = Unit

  fun updateDnsGeoIpCode(value: String?) = Unit

  fun updateDnsDomainFallback(value: List<String>?) = Unit

  fun updateDnsIpcidrFallback(value: List<String>?) = Unit

  fun updateDnsNameserverPolicy(value: Map<String, String>?) = Unit
}

@PreviewMihomo
@Composable
private fun OverrideSettingsContentPreview() = MihomoTheme {
  OverrideSettingsContent(
    configuration = ConfigurationOverride(),
    actions = object : OverrideSettingsActions {},
    showResetConfirmDialog = false,
    onShowResetConfirmDialogChange = {},
    onResetConfirmed = {},
    onOpenEditableTextMap = { _, _, _ -> },
    onOpenEditableTextList = { _, _, _ -> },
  )
}
