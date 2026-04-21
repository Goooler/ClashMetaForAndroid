package com.github.kr328.clash.design

import android.content.Context
import android.view.View
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.kr328.clash.core.model.ConfigurationOverride
import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.core.model.TunnelState
import com.github.kr328.clash.design.component.MihomoScaffold
import com.github.kr328.clash.design.component.SettingsCategoryTitle
import com.github.kr328.clash.design.ui.theme.MihomoTheme
import com.github.kr328.clash.design.ui.theme.PreviewMihomo
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class OverrideSettingsDesign(context: Context, configuration: ConfigurationOverride) :
  Design<OverrideSettingsDesign.Request>(context) {
  enum class Request {
    ResetOverride
  }

  override val root: View by composeView {
    MihomoTheme {
      OverrideSettingsScreen(
        configuration = configuration,
        onReset = ::requestClear,
      )
    }
  }

  suspend fun requestResetConfirm(): Boolean {
    return suspendCancellableCoroutine { ctx ->
      val dialog =
        MaterialAlertDialogBuilder(context)
          .setTitle(R.string.reset_override_settings)
          .setMessage(R.string.reset_override_settings_message)
          .setPositiveButton(R.string.ok) { _, _ -> ctx.resume(true) }
          .setNegativeButton(R.string.cancel) { _, _ -> }
          .show()

      dialog.setOnDismissListener { if (!ctx.isCompleted) ctx.resume(false) }

      ctx.invokeOnCancellation { dialog.dismiss() }
    }
  }

  fun requestClear() {
    requests.trySend(Request.ResetOverride)
  }
}

private data class SelectOptionUi(@StringRes val labelRes: Int, val onSelect: () -> Unit)

private sealed interface OverrideDialogState {
  data class Select(
    @StringRes val titleRes: Int,
    val options: List<SelectOptionUi>,
    val selectedIndex: Int,
  ) : OverrideDialogState

  data class EditText(
    @StringRes val titleRes: Int,
    val initialValue: String,
    @StringRes val placeholderRes: Int,
    val onConfirm: (String?) -> Unit,
  ) : OverrideDialogState

  data class EditList(
    @StringRes val titleRes: Int,
    val initialValue: String,
    @StringRes val placeholderRes: Int,
    val onConfirm: (List<String>?) -> Unit,
  ) : OverrideDialogState

  data class EditMap(
    @StringRes val titleRes: Int,
    val initialValue: String,
    @StringRes val placeholderRes: Int,
    val onConfirm: (Map<String, String>?) -> Unit,
  ) : OverrideDialogState
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun OverrideSettingsScreen(
  configuration: ConfigurationOverride,
  onReset: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var revision by remember { mutableIntStateOf(0) }
  var dialogState: OverrideDialogState? by remember { mutableStateOf(null) }

  fun refresh() {
    revision += 1
  }

  MihomoScaffold(
    title = stringResource(R.string.override),
    modifier = modifier,
    scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
    actions = {
      IconButton(onClick = onReset) {
        Icon(
          painter = painterResource(R.drawable.ic_baseline_replay),
          contentDescription = stringResource(R.string.reset),
        )
      }
    },
  ) { innerPadding ->
    key(revision) {
      Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        SettingsCategoryTitle(text = stringResource(R.string.general))

        OverrideTextPreference(
          titleRes = R.string.http_port,
          summary = summaryNullableInt(configuration.httpPort, R.string.dont_modify, R.string.disabled),
          onClick = {
            dialogState =
              OverrideDialogState.EditText(
                titleRes = R.string.http_port,
                initialValue = configuration.httpPort?.toString().orEmpty(),
                placeholderRes = R.string.dont_modify,
              ) { value ->
                configuration.httpPort = value?.toIntOrNull()
                refresh()
              }
          },
        )

        OverrideTextPreference(
          titleRes = R.string.socks_port,
          summary = summaryNullableInt(configuration.socksPort, R.string.dont_modify, R.string.disabled),
          onClick = {
            dialogState =
              OverrideDialogState.EditText(
                titleRes = R.string.socks_port,
                initialValue = configuration.socksPort?.toString().orEmpty(),
                placeholderRes = R.string.dont_modify,
              ) { value ->
                configuration.socksPort = value?.toIntOrNull()
                refresh()
              }
          },
        )

        OverrideTextPreference(
          titleRes = R.string.redirect_port,
          summary = summaryNullableInt(configuration.redirectPort, R.string.dont_modify, R.string.disabled),
          onClick = {
            dialogState =
              OverrideDialogState.EditText(
                titleRes = R.string.redirect_port,
                initialValue = configuration.redirectPort?.toString().orEmpty(),
                placeholderRes = R.string.dont_modify,
              ) { value ->
                configuration.redirectPort = value?.toIntOrNull()
                refresh()
              }
          },
        )

        OverrideTextPreference(
          titleRes = R.string.tproxy_port,
          summary = summaryNullableInt(configuration.tproxyPort, R.string.dont_modify, R.string.disabled),
          onClick = {
            dialogState =
              OverrideDialogState.EditText(
                titleRes = R.string.tproxy_port,
                initialValue = configuration.tproxyPort?.toString().orEmpty(),
                placeholderRes = R.string.dont_modify,
              ) { value ->
                configuration.tproxyPort = value?.toIntOrNull()
                refresh()
              }
          },
        )

        OverrideTextPreference(
          titleRes = R.string.mixed_port,
          summary = summaryNullableInt(configuration.mixedPort, R.string.dont_modify, R.string.disabled),
          onClick = {
            dialogState =
              OverrideDialogState.EditText(
                titleRes = R.string.mixed_port,
                initialValue = configuration.mixedPort?.toString().orEmpty(),
                placeholderRes = R.string.dont_modify,
              ) { value ->
                configuration.mixedPort = value?.toIntOrNull()
                refresh()
              }
          },
        )

        OverrideTextPreference(
          titleRes = R.string.authentication,
          summary = summaryList(configuration.authentication, R.string.dont_modify, R.string.disabled),
          onClick = {
            dialogState =
              OverrideDialogState.EditList(
                titleRes = R.string.authentication,
                initialValue = configuration.authentication.orEmpty().joinToString("\n"),
                placeholderRes = R.string.dont_modify,
              ) { value ->
                configuration.authentication = value
                refresh()
              }
          },
        )

        OverrideTextPreference(
          titleRes = R.string.allow_lan,
          summary = summaryBooleanTriState(configuration.allowLan),
          onClick = {
            dialogState =
              triStateBooleanDialog(
                titleRes = R.string.allow_lan,
                current = configuration.allowLan,
                onSelect = {
                  configuration.allowLan = it
                  refresh()
                },
              )
          },
        )

        OverrideTextPreference(
          titleRes = R.string.ipv6,
          summary = summaryBooleanTriState(configuration.ipv6),
          onClick = {
            dialogState =
              triStateBooleanDialog(
                titleRes = R.string.ipv6,
                current = configuration.ipv6,
                onSelect = {
                  configuration.ipv6 = it
                  refresh()
                },
              )
          },
        )

        OverrideTextPreference(
          titleRes = R.string.bind_address,
          summary = summaryNullableText(configuration.bindAddress, R.string.dont_modify, R.string.default_),
          onClick = {
            dialogState =
              OverrideDialogState.EditText(
                titleRes = R.string.bind_address,
                initialValue = configuration.bindAddress.orEmpty(),
                placeholderRes = R.string.dont_modify,
              ) { value ->
                configuration.bindAddress = value
                refresh()
              }
          },
        )

        OverrideTextPreference(
          titleRes = R.string.external_controller,
          summary =
            summaryNullableText(
              configuration.externalController,
              R.string.dont_modify,
              R.string.default_,
            ),
          onClick = {
            dialogState =
              OverrideDialogState.EditText(
                titleRes = R.string.external_controller,
                initialValue = configuration.externalController.orEmpty(),
                placeholderRes = R.string.dont_modify,
              ) { value ->
                configuration.externalController = value
                refresh()
              }
          },
        )

        OverrideTextPreference(
          titleRes = R.string.external_controller_tls,
          summary =
            summaryNullableText(
              configuration.externalControllerTLS,
              R.string.dont_modify,
              R.string.default_,
            ),
          onClick = {
            dialogState =
              OverrideDialogState.EditText(
                titleRes = R.string.external_controller_tls,
                initialValue = configuration.externalControllerTLS.orEmpty(),
                placeholderRes = R.string.dont_modify,
              ) { value ->
                configuration.externalControllerTLS = value
                refresh()
              }
          },
        )

        OverrideTextPreference(
          titleRes = R.string.allow_origins,
          summary =
            summaryList(
              configuration.externalControllerCors.allowOrigins,
              R.string.dont_modify,
              R.string.disabled,
            ),
          onClick = {
            dialogState =
              OverrideDialogState.EditList(
                titleRes = R.string.allow_origins,
                initialValue =
                  configuration.externalControllerCors.allowOrigins.orEmpty().joinToString("\n"),
                placeholderRes = R.string.dont_modify,
              ) { value ->
                configuration.externalControllerCors.allowOrigins = value
                refresh()
              }
          },
        )

        OverrideTextPreference(
          titleRes = R.string.allow_private_network,
          summary = summaryBooleanTriState(configuration.externalControllerCors.allowPrivateNetwork),
          onClick = {
            dialogState =
              triStateBooleanDialog(
                titleRes = R.string.allow_private_network,
                current = configuration.externalControllerCors.allowPrivateNetwork,
                onSelect = {
                  configuration.externalControllerCors.allowPrivateNetwork = it
                  refresh()
                },
              )
          },
        )

        OverrideTextPreference(
          titleRes = R.string.secret,
          summary = summaryNullableText(configuration.secret, R.string.dont_modify, R.string.default_),
          onClick = {
            dialogState =
              OverrideDialogState.EditText(
                titleRes = R.string.secret,
                initialValue = configuration.secret.orEmpty(),
                placeholderRes = R.string.dont_modify,
              ) { value ->
                configuration.secret = value
                refresh()
              }
          },
        )

        val modeValue = configuration.mode
        OverrideTextPreference(
          titleRes = R.string.mode,
          summary =
            when (modeValue) {
              null -> stringResource(R.string.dont_modify)
              TunnelState.Mode.Direct -> stringResource(R.string.direct_mode)
              TunnelState.Mode.Global -> stringResource(R.string.global_mode)
              TunnelState.Mode.Rule -> stringResource(R.string.rule_mode)
              else -> modeValue?.name.orEmpty()
            },
          onClick = {
            dialogState =
              OverrideDialogState.Select(
                titleRes = R.string.mode,
                selectedIndex =
                  when (modeValue) {
                    null -> 0
                    TunnelState.Mode.Direct -> 1
                    TunnelState.Mode.Global -> 2
                    TunnelState.Mode.Rule -> 3
                    else -> 0
                  },
                options =
                  listOf(
                    SelectOptionUi(R.string.dont_modify) {
                      configuration.mode = null
                      refresh()
                    },
                    SelectOptionUi(R.string.direct_mode) {
                      configuration.mode = TunnelState.Mode.Direct
                      refresh()
                    },
                    SelectOptionUi(R.string.global_mode) {
                      configuration.mode = TunnelState.Mode.Global
                      refresh()
                    },
                    SelectOptionUi(R.string.rule_mode) {
                      configuration.mode = TunnelState.Mode.Rule
                      refresh()
                    },
                  ),
              )
          },
        )

        val logLevelValue = configuration.logLevel
        OverrideTextPreference(
          titleRes = R.string.log_level,
          summary =
            when (logLevelValue) {
              null -> stringResource(R.string.dont_modify)
              LogMessage.Level.Info -> stringResource(R.string.info)
              LogMessage.Level.Warning -> stringResource(R.string.warning)
              LogMessage.Level.Error -> stringResource(R.string.error)
              LogMessage.Level.Debug -> stringResource(R.string.debug)
              LogMessage.Level.Silent -> stringResource(R.string.silent)
              else -> logLevelValue?.name.orEmpty()
            },
          onClick = {
            dialogState =
              OverrideDialogState.Select(
                titleRes = R.string.log_level,
                selectedIndex =
                  when (logLevelValue) {
                    null -> 0
                    LogMessage.Level.Info -> 1
                    LogMessage.Level.Warning -> 2
                    LogMessage.Level.Error -> 3
                    LogMessage.Level.Debug -> 4
                    LogMessage.Level.Silent -> 5
                    else -> 0
                  },
                options =
                  listOf(
                    SelectOptionUi(R.string.dont_modify) {
                      configuration.logLevel = null
                      refresh()
                    },
                    SelectOptionUi(R.string.info) {
                      configuration.logLevel = LogMessage.Level.Info
                      refresh()
                    },
                    SelectOptionUi(R.string.warning) {
                      configuration.logLevel = LogMessage.Level.Warning
                      refresh()
                    },
                    SelectOptionUi(R.string.error) {
                      configuration.logLevel = LogMessage.Level.Error
                      refresh()
                    },
                    SelectOptionUi(R.string.debug) {
                      configuration.logLevel = LogMessage.Level.Debug
                      refresh()
                    },
                    SelectOptionUi(R.string.silent) {
                      configuration.logLevel = LogMessage.Level.Silent
                      refresh()
                    },
                  ),
              )
          },
        )

        OverrideTextPreference(
          titleRes = R.string.hosts,
          summary = summaryMap(configuration.hosts, R.string.dont_modify, R.string.disabled),
          onClick = {
            dialogState =
              OverrideDialogState.EditMap(
                titleRes = R.string.hosts,
                initialValue = mapToEditableText(configuration.hosts),
                placeholderRes = R.string.dont_modify,
              ) { value ->
                configuration.hosts = value
                refresh()
              }
          },
        )

        SettingsCategoryTitle(text = stringResource(R.string.dns))

        OverrideTextPreference(
          titleRes = R.string.strategy,
          summary =
            when (configuration.dns.enable) {
              null -> stringResource(R.string.dont_modify)
              true -> stringResource(R.string.force_enable)
              false -> stringResource(R.string.use_built_in)
            },
          onClick = {
            dialogState =
              OverrideDialogState.Select(
                titleRes = R.string.strategy,
                selectedIndex =
                  when (configuration.dns.enable) {
                    null -> 0
                    true -> 1
                    false -> 2
                  },
                options =
                  listOf(
                    SelectOptionUi(R.string.dont_modify) {
                      configuration.dns.enable = null
                      refresh()
                    },
                    SelectOptionUi(R.string.force_enable) {
                      configuration.dns.enable = true
                      refresh()
                    },
                    SelectOptionUi(R.string.use_built_in) {
                      configuration.dns.enable = false
                      refresh()
                    },
                  ),
              )
          },
        )

        val dnsDependenciesEnabled = configuration.dns.enable != false

        OverrideTextPreference(
          titleRes = R.string.prefer_h3,
          summary = summaryBooleanTriState(configuration.dns.preferH3),
          enabled = dnsDependenciesEnabled,
          onClick = {
            dialogState =
              triStateBooleanDialog(
                titleRes = R.string.prefer_h3,
                current = configuration.dns.preferH3,
                onSelect = {
                  configuration.dns.preferH3 = it
                  refresh()
                },
              )
          },
        )

        OverrideTextPreference(
          titleRes = R.string.listen,
          summary = summaryNullableText(configuration.dns.listen, R.string.dont_modify, R.string.disabled),
          enabled = dnsDependenciesEnabled,
          onClick = {
            dialogState =
              OverrideDialogState.EditText(
                titleRes = R.string.listen,
                initialValue = configuration.dns.listen.orEmpty(),
                placeholderRes = R.string.dont_modify,
              ) { value ->
                configuration.dns.listen = value
                refresh()
              }
          },
        )

        OverrideTextPreference(
          titleRes = R.string.append_system_dns,
          summary = summaryBooleanTriState(configuration.app.appendSystemDns),
          enabled = dnsDependenciesEnabled,
          onClick = {
            dialogState =
              triStateBooleanDialog(
                titleRes = R.string.append_system_dns,
                current = configuration.app.appendSystemDns,
                onSelect = {
                  configuration.app.appendSystemDns = it
                  refresh()
                },
              )
          },
        )

        OverrideTextPreference(
          titleRes = R.string.ipv6,
          summary = summaryBooleanTriState(configuration.dns.ipv6),
          enabled = dnsDependenciesEnabled,
          onClick = {
            dialogState =
              triStateBooleanDialog(
                titleRes = R.string.ipv6,
                current = configuration.dns.ipv6,
                onSelect = {
                  configuration.dns.ipv6 = it
                  refresh()
                },
              )
          },
        )

        OverrideTextPreference(
          titleRes = R.string.use_hosts,
          summary = summaryBooleanTriState(configuration.dns.useHosts),
          enabled = dnsDependenciesEnabled,
          onClick = {
            dialogState =
              triStateBooleanDialog(
                titleRes = R.string.use_hosts,
                current = configuration.dns.useHosts,
                onSelect = {
                  configuration.dns.useHosts = it
                  refresh()
                },
              )
          },
        )

        OverrideTextPreference(
          titleRes = R.string.enhanced_mode,
          summary =
            when (configuration.dns.enhancedMode) {
              null -> stringResource(R.string.dont_modify)
              ConfigurationOverride.DnsEnhancedMode.None -> stringResource(R.string.disabled)
              ConfigurationOverride.DnsEnhancedMode.FakeIp -> stringResource(R.string.fakeip)
              ConfigurationOverride.DnsEnhancedMode.Mapping -> stringResource(R.string.mapping)
            },
          enabled = dnsDependenciesEnabled,
          onClick = {
            dialogState =
              OverrideDialogState.Select(
                titleRes = R.string.enhanced_mode,
                selectedIndex =
                  when (configuration.dns.enhancedMode) {
                    null -> 0
                    ConfigurationOverride.DnsEnhancedMode.None -> 1
                    ConfigurationOverride.DnsEnhancedMode.FakeIp -> 2
                    ConfigurationOverride.DnsEnhancedMode.Mapping -> 3
                  },
                options =
                  listOf(
                    SelectOptionUi(R.string.dont_modify) {
                      configuration.dns.enhancedMode = null
                      refresh()
                    },
                    SelectOptionUi(R.string.disabled) {
                      configuration.dns.enhancedMode = ConfigurationOverride.DnsEnhancedMode.None
                      refresh()
                    },
                    SelectOptionUi(R.string.fakeip) {
                      configuration.dns.enhancedMode = ConfigurationOverride.DnsEnhancedMode.FakeIp
                      refresh()
                    },
                    SelectOptionUi(R.string.mapping) {
                      configuration.dns.enhancedMode = ConfigurationOverride.DnsEnhancedMode.Mapping
                      refresh()
                    },
                  ),
              )
          },
        )

        OverrideTextPreference(
          titleRes = R.string.name_server,
          summary = summaryList(configuration.dns.nameServer, R.string.dont_modify, R.string.disabled),
          enabled = dnsDependenciesEnabled,
          onClick = {
            dialogState =
              OverrideDialogState.EditList(
                titleRes = R.string.name_server,
                initialValue = configuration.dns.nameServer.orEmpty().joinToString("\n"),
                placeholderRes = R.string.dont_modify,
              ) { value ->
                configuration.dns.nameServer = value
                refresh()
              }
          },
        )

        OverrideTextPreference(
          titleRes = R.string.fallback,
          summary = summaryList(configuration.dns.fallback, R.string.dont_modify, R.string.disabled),
          enabled = dnsDependenciesEnabled,
          onClick = {
            dialogState =
              OverrideDialogState.EditList(
                titleRes = R.string.fallback,
                initialValue = configuration.dns.fallback.orEmpty().joinToString("\n"),
                placeholderRes = R.string.dont_modify,
              ) { value ->
                configuration.dns.fallback = value
                refresh()
              }
          },
        )

        OverrideTextPreference(
          titleRes = R.string.default_name_server,
          summary =
            summaryList(configuration.dns.defaultServer, R.string.dont_modify, R.string.disabled),
          enabled = dnsDependenciesEnabled,
          onClick = {
            dialogState =
              OverrideDialogState.EditList(
                titleRes = R.string.default_name_server,
                initialValue = configuration.dns.defaultServer.orEmpty().joinToString("\n"),
                placeholderRes = R.string.dont_modify,
              ) { value ->
                configuration.dns.defaultServer = value
                refresh()
              }
          },
        )

        OverrideTextPreference(
          titleRes = R.string.fakeip_filter,
          summary =
            summaryList(configuration.dns.fakeIpFilter, R.string.dont_modify, R.string.disabled),
          enabled = dnsDependenciesEnabled,
          onClick = {
            dialogState =
              OverrideDialogState.EditList(
                titleRes = R.string.fakeip_filter,
                initialValue = configuration.dns.fakeIpFilter.orEmpty().joinToString("\n"),
                placeholderRes = R.string.dont_modify,
              ) { value ->
                configuration.dns.fakeIpFilter = value
                refresh()
              }
          },
        )

        OverrideTextPreference(
          titleRes = R.string.fakeip_filter_mode,
          summary =
            when (configuration.dns.fakeIPFilterMode) {
              null -> stringResource(R.string.dont_modify)
              ConfigurationOverride.FilterMode.BlackList -> stringResource(R.string.blacklist)
              ConfigurationOverride.FilterMode.WhiteList -> stringResource(R.string.whitelist)
            },
          enabled = dnsDependenciesEnabled,
          onClick = {
            dialogState =
              OverrideDialogState.Select(
                titleRes = R.string.fakeip_filter_mode,
                selectedIndex =
                  when (configuration.dns.fakeIPFilterMode) {
                    null -> 0
                    ConfigurationOverride.FilterMode.BlackList -> 1
                    ConfigurationOverride.FilterMode.WhiteList -> 2
                  },
                options =
                  listOf(
                    SelectOptionUi(R.string.dont_modify) {
                      configuration.dns.fakeIPFilterMode = null
                      refresh()
                    },
                    SelectOptionUi(R.string.blacklist) {
                      configuration.dns.fakeIPFilterMode = ConfigurationOverride.FilterMode.BlackList
                      refresh()
                    },
                    SelectOptionUi(R.string.whitelist) {
                      configuration.dns.fakeIPFilterMode = ConfigurationOverride.FilterMode.WhiteList
                      refresh()
                    },
                  ),
              )
          },
        )

        OverrideTextPreference(
          titleRes = R.string.geoip_fallback,
          summary = summaryBooleanTriState(configuration.dns.fallbackFilter.geoIp),
          enabled = dnsDependenciesEnabled,
          onClick = {
            dialogState =
              triStateBooleanDialog(
                titleRes = R.string.geoip_fallback,
                current = configuration.dns.fallbackFilter.geoIp,
                onSelect = {
                  configuration.dns.fallbackFilter.geoIp = it
                  refresh()
                },
              )
          },
        )

        OverrideTextPreference(
          titleRes = R.string.geoip_fallback_code,
          summary =
            summaryNullableText(
              configuration.dns.fallbackFilter.geoIpCode,
              R.string.dont_modify,
              R.string.raw_cn,
            ),
          enabled = dnsDependenciesEnabled,
          onClick = {
            dialogState =
              OverrideDialogState.EditText(
                titleRes = R.string.geoip_fallback_code,
                initialValue = configuration.dns.fallbackFilter.geoIpCode.orEmpty(),
                placeholderRes = R.string.dont_modify,
              ) { value ->
                configuration.dns.fallbackFilter.geoIpCode = value
                refresh()
              }
          },
        )

        OverrideTextPreference(
          titleRes = R.string.domain_fallback,
          summary =
            summaryList(
              configuration.dns.fallbackFilter.domain,
              R.string.dont_modify,
              R.string.disabled,
            ),
          enabled = dnsDependenciesEnabled,
          onClick = {
            dialogState =
              OverrideDialogState.EditList(
                titleRes = R.string.domain_fallback,
                initialValue = configuration.dns.fallbackFilter.domain.orEmpty().joinToString("\n"),
                placeholderRes = R.string.dont_modify,
              ) { value ->
                configuration.dns.fallbackFilter.domain = value
                refresh()
              }
          },
        )

        OverrideTextPreference(
          titleRes = R.string.ipcidr_fallback,
          summary =
            summaryList(
              configuration.dns.fallbackFilter.ipcidr,
              R.string.dont_modify,
              R.string.disabled,
            ),
          enabled = dnsDependenciesEnabled,
          onClick = {
            dialogState =
              OverrideDialogState.EditList(
                titleRes = R.string.ipcidr_fallback,
                initialValue = configuration.dns.fallbackFilter.ipcidr.orEmpty().joinToString("\n"),
                placeholderRes = R.string.dont_modify,
              ) { value ->
                configuration.dns.fallbackFilter.ipcidr = value
                refresh()
              }
          },
        )

        OverrideTextPreference(
          titleRes = R.string.name_server_policy,
          summary =
            summaryMap(configuration.dns.nameserverPolicy, R.string.dont_modify, R.string.disabled),
          enabled = dnsDependenciesEnabled,
          onClick = {
            dialogState =
              OverrideDialogState.EditMap(
                titleRes = R.string.name_server_policy,
                initialValue = mapToEditableText(configuration.dns.nameserverPolicy),
                placeholderRes = R.string.dont_modify,
              ) { value ->
                configuration.dns.nameserverPolicy = value
                refresh()
              }
          },
        )
      }
    }

    when (val state = dialogState) {
      null -> Unit
      is OverrideDialogState.Select ->
        SelectDialog(
          title = stringResource(state.titleRes),
          options = state.options,
          selectedIndex = state.selectedIndex,
          onDismiss = { dialogState = null },
          onSelect = { option ->
            option.onSelect()
            dialogState = null
          },
        )
      is OverrideDialogState.EditText ->
        EditTextDialog(
          title = stringResource(state.titleRes),
          initialValue = state.initialValue,
          placeholder = stringResource(state.placeholderRes),
          onDismiss = { dialogState = null },
          onConfirm = {
            state.onConfirm(it)
            dialogState = null
          },
        )
      is OverrideDialogState.EditList ->
        EditListDialog(
          title = stringResource(state.titleRes),
          initialValue = state.initialValue,
          placeholder = stringResource(state.placeholderRes),
          onDismiss = { dialogState = null },
          onConfirm = {
            state.onConfirm(it)
            dialogState = null
          },
        )
      is OverrideDialogState.EditMap ->
        EditMapDialog(
          title = stringResource(state.titleRes),
          initialValue = state.initialValue,
          placeholder = stringResource(state.placeholderRes),
          onDismiss = { dialogState = null },
          onConfirm = {
            state.onConfirm(it)
            dialogState = null
          },
        )
    }
  }
}

@Composable
private fun OverrideTextPreference(
  @StringRes titleRes: Int,
  summary: String,
  enabled: Boolean = true,
  onClick: () -> Unit,
) {
  Row(
    modifier =
      Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick).padding(vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(modifier = Modifier.padding(start = 65.dp, end = 20.dp)) {
      Text(
        text = stringResource(titleRes),
        style = MaterialTheme.typography.bodyLarge,
        color =
          if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
      )
      Text(
        text = summary,
        style = MaterialTheme.typography.bodyMedium,
        color =
          if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
          else MaterialTheme.colorScheme.outline,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(top = 4.dp),
      )
    }
  }
}

@Composable
private fun SelectDialog(
  title: String,
  options: List<SelectOptionUi>,
  selectedIndex: Int,
  onDismiss: () -> Unit,
  onSelect: (SelectOptionUi) -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(text = title) },
    text = {
      Column {
        options.forEachIndexed { index, option ->
          Row(
            modifier = Modifier.fillMaxWidth().clickable { onSelect(option) },
            verticalAlignment = Alignment.CenterVertically,
          ) {
            RadioButton(selected = index == selectedIndex, onClick = { onSelect(option) })
            Text(text = stringResource(option.labelRes), modifier = Modifier.padding(start = 8.dp))
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) { Text(text = stringResource(R.string.cancel)) }
    },
  )
}

@Composable
private fun EditTextDialog(
  title: String,
  initialValue: String,
  placeholder: String,
  onDismiss: () -> Unit,
  onConfirm: (String?) -> Unit,
) {
  var value by remember(initialValue) { mutableStateOf(initialValue) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(text = title) },
    text = {
      OutlinedTextField(
        value = value,
        onValueChange = { value = it },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(text = placeholder) },
        singleLine = true,
      )
    },
    confirmButton = {
      TextButton(onClick = { onConfirm(value.trim().takeIf { it.isNotEmpty() }) }) {
        Text(text = stringResource(R.string.ok))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(text = stringResource(R.string.cancel)) }
    },
  )
}

@Composable
private fun EditListDialog(
  title: String,
  initialValue: String,
  placeholder: String,
  onDismiss: () -> Unit,
  onConfirm: (List<String>?) -> Unit,
) {
  var value by remember(initialValue) { mutableStateOf(initialValue) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(text = title) },
    text = {
      OutlinedTextField(
        value = value,
        onValueChange = { value = it },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(text = placeholder) },
        maxLines = 8,
        minLines = 5,
      )
    },
    confirmButton = {
      TextButton(onClick = { onConfirm(parseList(value)) }) { Text(text = stringResource(R.string.ok)) }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(text = stringResource(R.string.cancel)) }
    },
  )
}

@Composable
private fun EditMapDialog(
  title: String,
  initialValue: String,
  placeholder: String,
  onDismiss: () -> Unit,
  onConfirm: (Map<String, String>?) -> Unit,
) {
  var value by remember(initialValue) { mutableStateOf(initialValue) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(text = title) },
    text = {
      OutlinedTextField(
        value = value,
        onValueChange = { value = it },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(text = placeholder) },
        maxLines = 8,
        minLines = 5,
      )
    },
    confirmButton = {
      TextButton(onClick = { onConfirm(parseMap(value)) }) { Text(text = stringResource(R.string.ok)) }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(text = stringResource(R.string.cancel)) }
    },
  )
}

private fun triStateBooleanDialog(
  @StringRes titleRes: Int,
  current: Boolean?,
  onSelect: (Boolean?) -> Unit,
): OverrideDialogState.Select {
  val options =
    listOf(
      SelectOptionUi(R.string.dont_modify) { onSelect(null) },
      SelectOptionUi(R.string.enabled) { onSelect(true) },
      SelectOptionUi(R.string.disabled) { onSelect(false) },
    )

  val selectedIndex =
    when (current) {
      null -> 0
      true -> 1
      false -> 2
    }

  return OverrideDialogState.Select(titleRes = titleRes, options = options, selectedIndex = selectedIndex)
}

@Composable
private fun summaryBooleanTriState(value: Boolean?): String {
  return when (value) {
    null -> stringResource(R.string.dont_modify)
    true -> stringResource(R.string.enabled)
    false -> stringResource(R.string.disabled)
  }
}

@Composable
private fun summaryNullableInt(value: Int?, @StringRes nullRes: Int, @StringRes emptyRes: Int): String {
  return when {
    value == null -> stringResource(nullRes)
    value == 0 -> stringResource(emptyRes)
    else -> value.toString()
  }
}

@Composable
private fun summaryNullableText(
  value: String?,
  @StringRes nullRes: Int,
  @StringRes emptyRes: Int,
): String {
  return when {
    value == null -> stringResource(nullRes)
    value.isEmpty() -> stringResource(emptyRes)
    else -> value
  }
}

@Composable
private fun summaryList(
  value: List<String>?,
  @StringRes nullRes: Int,
  @StringRes emptyRes: Int,
): String {
  return when {
    value == null -> stringResource(nullRes)
    value.isEmpty() -> stringResource(emptyRes)
    else -> value.joinToString(", ")
  }
}

@Composable
private fun summaryMap(
  value: Map<String, String>?,
  @StringRes nullRes: Int,
  @StringRes emptyRes: Int,
): String {
  return when {
    value == null -> stringResource(nullRes)
    value.isEmpty() -> stringResource(emptyRes)
    else -> value.entries.joinToString(", ") { "${it.key}=${it.value}" }
  }
}

private fun parseList(text: String): List<String>? {
  val result = text.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()

  return if (result.isEmpty()) null else result
}

private fun parseMap(text: String): Map<String, String>? {
  val result =
    text
      .lineSequence()
      .map { it.trim() }
      .filter { it.isNotEmpty() }
      .mapNotNull { line ->
        val separator = line.indexOf('=')
        if (separator <= 0 || separator >= line.lastIndex) {
          return@mapNotNull null
        }

        val key = line.substring(0, separator).trim()
        val value = line.substring(separator + 1).trim()

        if (key.isEmpty()) {
          null
        } else {
          key to value
        }
      }
      .toMap()

  return if (result.isEmpty()) null else result
}

private fun mapToEditableText(value: Map<String, String>?): String {
  return value.orEmpty().entries.joinToString("\n") { "${it.key}=${it.value}" }
}

@PreviewMihomo
@Composable
private fun OverrideSettingsScreenPreview() = MihomoTheme {
  OverrideSettingsScreen(configuration = ConfigurationOverride(), onReset = {})
}
