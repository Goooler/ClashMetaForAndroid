package com.github.kr328.clash.settings.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
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
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.core.model.ConfigurationOverride
import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.core.model.TunnelState
import com.github.kr328.clash.settings.R
import com.github.kr328.clash.settings.vm.OverrideSettingsViewModel
import com.github.kr328.clash.ui.component.TabbyScaffold
import com.github.kr328.clash.ui.icon.BaselineReplay
import com.github.kr328.clash.ui.icon.TabbyIcons
import com.github.kr328.clash.ui.nav.TabbyNavDisplay
import com.github.kr328.clash.ui.nav.addIfNotLast
import com.github.kr328.clash.ui.nav.rememberNavBackStackBuilder
import com.github.kr328.clash.ui.theme.PreviewTabby
import com.github.kr328.clash.ui.theme.TabbyTheme
import kotlinx.serialization.Serializable
import me.zhanghai.compose.preference.Preference
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.listPreference
import me.zhanghai.compose.preference.preference
import me.zhanghai.compose.preference.preferenceCategory

private sealed interface OverrideSettingsRoute : NavKey {
  @Serializable data object Main : OverrideSettingsRoute
}

@Composable
internal fun OverrideSettingsScreen(
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

  TabbyNavDisplay(
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
  TabbyScaffold(
    title = stringResource(R.string.override),
    modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    scrollBehavior = scrollBehavior,
    actions = {
      IconButton(onClick = { onShowResetConfirmDialogChange(true) }) {
        Icon(
          imageVector = TabbyIcons.BaselineReplay,
          contentDescription = stringResource(CommonR.string.reset),
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
    }

    if (showResetConfirmDialog) {
      ResetOverrideSettingsDialog(
        onConfirm = {
          onShowResetConfirmDialogChange(false)
          onResetConfirmed()
        },
        onDismiss = { onShowResetConfirmDialogChange(false) },
      )
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
  overrideEditTextPreferenceItem(
    key = "httpPort",
    title = R.string.http_port,
    placeholder = R.string.dont_modify,
    emptyLabel = CommonR.string.disabled,
    value = portText(configuration.httpPort),
    onValueChange = { actions.updateHttpPort(parsePort(it)) },
    numericOnly = true,
  )
  overrideEditTextPreferenceItem(
    key = "socksPort",
    title = R.string.socks_port,
    placeholder = R.string.dont_modify,
    emptyLabel = CommonR.string.disabled,
    value = portText(configuration.socksPort),
    onValueChange = { actions.updateSocksPort(parsePort(it)) },
    numericOnly = true,
  )
  overrideEditTextPreferenceItem(
    key = "redirectPort",
    title = R.string.redirect_port,
    placeholder = R.string.dont_modify,
    emptyLabel = CommonR.string.disabled,
    value = portText(configuration.redirectPort),
    onValueChange = { actions.updateRedirectPort(parsePort(it)) },
    numericOnly = true,
  )
  overrideEditTextPreferenceItem(
    key = "tproxyPort",
    title = R.string.tproxy_port,
    placeholder = R.string.dont_modify,
    emptyLabel = CommonR.string.disabled,
    value = portText(configuration.tproxyPort),
    onValueChange = { actions.updateTproxyPort(parsePort(it)) },
    numericOnly = true,
  )
  overrideEditTextPreferenceItem(
    key = "mixedPort",
    title = R.string.mixed_port,
    placeholder = R.string.dont_modify,
    emptyLabel = CommonR.string.disabled,
    value = portText(configuration.mixedPort),
    onValueChange = { actions.updateMixedPort(parsePort(it)) },
    numericOnly = true,
  )
  preference(
    key = "authentication",
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
  listPreference(
    key = "allowLan",
    value = configuration.allowLan,
    onValueChange = actions::updateAllowLan,
    values = booleanOptions,
    title = { Text(stringResource(R.string.allow_lan)) },
    summary = { Text(stringResource(configuration.allowLan.textRes)) },
    valueToText = { AnnotatedString(stringResource(it.textRes)) },
  )
  listPreference(
    key = "ipv6",
    value = configuration.ipv6,
    onValueChange = actions::updateIpv6,
    values = booleanOptions,
    title = { Text(stringResource(R.string.ipv6)) },
    summary = { Text(stringResource(configuration.ipv6.textRes)) },
    valueToText = { AnnotatedString(stringResource(it.textRes)) },
  )
  overrideEditTextPreferenceItem(
    key = "bindAddress",
    title = R.string.bind_address,
    placeholder = R.string.dont_modify,
    emptyLabel = CommonR.string.default_,
    value = configuration.bindAddress,
    onValueChange = actions::updateBindAddress,
  )
  overrideEditTextPreferenceItem(
    key = "externalController",
    title = R.string.external_controller,
    placeholder = R.string.dont_modify,
    emptyLabel = CommonR.string.default_,
    value = configuration.externalController,
    onValueChange = actions::updateExternalController,
  )
  overrideEditTextPreferenceItem(
    key = "externalControllerTls",
    title = R.string.external_controller_tls,
    placeholder = R.string.dont_modify,
    emptyLabel = CommonR.string.default_,
    value = configuration.externalControllerTLS,
    onValueChange = actions::updateExternalControllerTls,
  )
  preference(
    key = "allowOrigins",
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
  listPreference(
    key = "allowPrivateNetwork",
    value = configuration.externalControllerCors.allowPrivateNetwork,
    onValueChange = actions::updateAllowPrivateNetwork,
    values = booleanOptions,
    title = { Text(stringResource(R.string.allow_private_network)) },
    summary = {
      Text(stringResource(configuration.externalControllerCors.allowPrivateNetwork.textRes))
    },
    valueToText = { AnnotatedString(stringResource(it.textRes)) },
  )
  overrideEditTextPreferenceItem(
    key = "secret",
    title = R.string.secret,
    placeholder = R.string.dont_modify,
    emptyLabel = CommonR.string.default_,
    value = configuration.secret,
    onValueChange = actions::updateSecret,
  )
  listPreference(
    key = "mode",
    value = configuration.mode,
    onValueChange = actions::updateMode,
    values = TunnelState.Mode.entries,
    title = { Text(stringResource(CommonR.string.mode)) },
    summary = { Text(stringResource(configuration.mode.textRes)) },
    valueToText = { AnnotatedString(stringResource(it.textRes)) },
  )
  listPreference(
    key = "logLevel",
    value = configuration.logLevel,
    onValueChange = actions::updateLogLevel,
    values = LogMessage.Level.entries,
    title = { Text(stringResource(R.string.log_level)) },
    summary = { Text(stringResource(configuration.logLevel.textRes)) },
    valueToText = { AnnotatedString(stringResource(it.textRes)) },
  )
  preference(
    key = "hosts",
    title = { Text(stringResource(R.string.hosts)) },
    summary = { Text(configuration.hosts.summary(R.string.dont_modify)) },
    onClick = { onOpenEditableTextMap(R.string.hosts, configuration.hosts, actions::updateHosts) },
  )
}

private fun LazyListScope.dnsPreferenceItems(
  configuration: ConfigurationOverride,
  actions: OverrideSettingsActions,
  dnsEnabled: Boolean?,
  onOpenEditableTextMap: (Int, Map<String, String>?, (Map<String, String>?) -> Unit) -> Unit,
  onOpenEditableTextList: (Int, List<String>?, (List<String>?) -> Unit) -> Unit,
) {
  preferenceCategory(key = "cat_dns", title = { Text(stringResource(R.string.dns)) })
  listPreference(
    key = "dnsStrategy",
    value = dnsEnabled,
    onValueChange = actions::updateDnsEnable,
    values = booleanOptions,
    title = { Text(stringResource(R.string.strategy)) },
    summary = { Text(stringResource(dnsEnabled.dnsStrategyTextRes)) },
    valueToText = { AnnotatedString(stringResource(it.dnsStrategyTextRes)) },
  )
  listPreference(
    key = "dnsPreferH3",
    value = configuration.dns.preferH3,
    onValueChange = actions::updateDnsPreferH3,
    values = booleanOptions,
    enabled = dnsEnabled != false,
    title = { Text(stringResource(R.string.prefer_h3)) },
    summary = { Text(stringResource(configuration.dns.preferH3.textRes)) },
    valueToText = { AnnotatedString(stringResource(it.textRes)) },
  )
  overrideEditTextPreferenceItem(
    key = "dnsListen",
    title = R.string.listen,
    placeholder = R.string.dont_modify,
    emptyLabel = CommonR.string.disabled,
    value = configuration.dns.listen,
    onValueChange = actions::updateDnsListen,
    enabled = dnsEnabled != false,
  )
  listPreference(
    key = "appendSystemDns",
    value = configuration.app.appendSystemDns,
    onValueChange = actions::updateAppendSystemDns,
    values = booleanOptions,
    enabled = dnsEnabled != false,
    title = { Text(stringResource(R.string.append_system_dns)) },
    summary = { Text(stringResource(configuration.app.appendSystemDns.textRes)) },
    valueToText = { AnnotatedString(stringResource(it.textRes)) },
  )
  listPreference(
    key = "dnsIpv6",
    value = configuration.dns.ipv6,
    onValueChange = actions::updateDnsIpv6,
    values = booleanOptions,
    enabled = dnsEnabled != false,
    title = { Text(stringResource(R.string.ipv6)) },
    summary = { Text(stringResource(configuration.dns.ipv6.textRes)) },
    valueToText = { AnnotatedString(stringResource(it.textRes)) },
  )
  listPreference(
    key = "dnsUseHosts",
    value = configuration.dns.useHosts,
    onValueChange = actions::updateDnsUseHosts,
    values = booleanOptions,
    enabled = dnsEnabled != false,
    title = { Text(stringResource(R.string.use_hosts)) },
    summary = { Text(stringResource(configuration.dns.useHosts.textRes)) },
    valueToText = { AnnotatedString(stringResource(it.textRes)) },
  )
  listPreference(
    key = "dnsEnhancedMode",
    value = configuration.dns.enhancedMode,
    onValueChange = actions::updateDnsEnhancedMode,
    values = ConfigurationOverride.DnsEnhancedMode.entries,
    enabled = dnsEnabled != false,
    title = { Text(stringResource(R.string.enhanced_mode)) },
    summary = { Text(stringResource(configuration.dns.enhancedMode.textRes)) },
    valueToText = { AnnotatedString(stringResource(it.textRes)) },
  )
  preference(
    key = "dnsNameServer",
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
  preference(
    key = "dnsFallback",
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
  preference(
    key = "dnsDefaultServer",
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
  preference(
    key = "dnsFakeIpFilter",
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
  listPreference(
    key = "dnsFakeIpFilterMode",
    value = configuration.dns.fakeIPFilterMode,
    onValueChange = actions::updateDnsFakeIpFilterMode,
    values = ConfigurationOverride.FilterMode.entries,
    enabled = dnsEnabled != false,
    title = { Text(stringResource(R.string.fakeip_filter_mode)) },
    summary = { Text(stringResource(configuration.dns.fakeIPFilterMode.textRes)) },
    valueToText = { AnnotatedString(stringResource(it.textRes)) },
  )
  listPreference(
    key = "dnsGeoIpFallback",
    value = configuration.dns.fallbackFilter.geoIp,
    onValueChange = actions::updateDnsGeoIpFallback,
    values = booleanOptions,
    enabled = dnsEnabled != false,
    title = { Text(stringResource(R.string.geoip_fallback)) },
    summary = { Text(stringResource(configuration.dns.fallbackFilter.geoIp.textRes)) },
    valueToText = { AnnotatedString(stringResource(it.textRes)) },
  )
  overrideEditTextPreferenceItem(
    key = "dnsGeoIpCode",
    title = R.string.geoip_fallback_code,
    placeholder = R.string.dont_modify,
    emptyLabel = R.string.raw_cn,
    value = configuration.dns.fallbackFilter.geoIpCode,
    onValueChange = actions::updateDnsGeoIpCode,
    enabled = dnsEnabled != false,
  )
  preference(
    key = "dnsDomainFallback",
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
  preference(
    key = "dnsIpcidrFallback",
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
  preference(
    key = "dnsNameserverPolicy",
    title = { Text(stringResource(R.string.name_server_policy)) },
    summary = { Text(configuration.dns.nameserverPolicy.summary(R.string.dont_modify)) },
    enabled = dnsEnabled != false,
    onClick = {
      onOpenEditableTextMap(
        R.string.name_server_policy,
        configuration.dns.nameserverPolicy,
        actions::updateDnsNameserverPolicy,
      )
    },
  )
}

private fun LazyListScope.overrideEditTextPreferenceItem(
  key: String,
  @StringRes title: Int,
  @StringRes placeholder: Int,
  @StringRes emptyLabel: Int,
  value: String?,
  onValueChange: (String?) -> Unit,
  enabled: Boolean = true,
  numericOnly: Boolean = false,
) {
  item(key = key, contentType = "EditTextPreference") {
    var showDialog by remember { mutableStateOf(false) }
    val summary =
      when {
        value == null -> stringResource(placeholder)
        value.isEmpty() -> stringResource(emptyLabel)
        else -> value
      }
    Preference(
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
            Text(stringResource(CommonR.string.ok))
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
              Text(stringResource(CommonR.string.reset))
            }
            TextButton(onClick = { showDialog = false }) {
              Text(stringResource(CommonR.string.cancel))
            }
          }
        },
      )
    }
  }
}

private fun TextFieldValue.filterDigits(): TextFieldValue {
  val filtered = text.filter(Char::isDigit)
  if (filtered == text) return this
  val start = text.take(selection.start).count(Char::isDigit)
  val end = text.take(selection.end).count(Char::isDigit)
  return copy(text = filtered, selection = TextRange(start, end))
}

@Composable
private fun Map<String, String>?.summary(@StringRes placeholder: Int) =
  when {
    this == null -> stringResource(placeholder)
    isEmpty() -> stringResource(R.string.empty)
    else -> stringResource(CommonR.string.format_elements, size)
  }

internal val Boolean?.textRes: Int
  @StringRes
  get() =
    when (this) {
      true -> R.string.enabled
      false -> CommonR.string.disabled
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
      Direct -> CommonR.string.direct_mode
      Global -> CommonR.string.global_mode
      Rule -> CommonR.string.rule_mode
      null -> R.string.dont_modify
    }

private val LogMessage.Level?.textRes: Int
  @StringRes
  get() =
    when (this) {
      Info -> R.string.info
      Warning -> R.string.warning
      LogMessage.Level.Error -> R.string.error
      Debug -> R.string.debug
      Silent -> R.string.silent
      Unknown -> CommonR.string.unknown
      null -> R.string.dont_modify
    }

private val ConfigurationOverride.DnsEnhancedMode?.textRes: Int
  @StringRes
  get() =
    when (this) {
      None -> CommonR.string.disabled
      FakeIp -> R.string.fakeip
      Mapping -> R.string.mapping
      null -> R.string.dont_modify
    }

private val ConfigurationOverride.FilterMode?.textRes: Int
  @StringRes
  get() =
    when (this) {
      BlackList -> R.string.blacklist
      WhiteList -> R.string.whitelist
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

@PreviewTabby
@Composable
internal fun OverrideSettingsContentPreview() = TabbyTheme {
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
