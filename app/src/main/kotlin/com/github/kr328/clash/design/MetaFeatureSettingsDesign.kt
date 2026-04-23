package com.github.kr328.clash.design

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.github.kr328.clash.R
import com.github.kr328.clash.core.model.ConfigurationOverride
import com.github.kr328.clash.design.component.MihomoScaffold
import com.github.kr328.clash.design.component.SettingsClickablePreferenceItem
import com.github.kr328.clash.design.component.SettingsEditTextListPreferenceItem
import com.github.kr328.clash.design.component.SettingsListPreferenceItem
import com.github.kr328.clash.design.component.rememberWriteThroughState
import com.github.kr328.clash.design.ui.theme.MihomoTheme
import com.github.kr328.clash.design.ui.theme.PreviewMihomo
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.preferenceCategory

class MetaFeatureSettingsDesign(
  context: Context,
  private val configuration: ConfigurationOverride,
) : Design<MetaFeatureSettingsDesign.Request>(context) {
  sealed interface Request {
    data object ResetOverride : Request

    data object ImportGeoIp : Request

    data object ImportGeoSite : Request

    data object ImportCountry : Request

    data object ImportASN : Request
  }

  @Composable
  override fun Content() = MihomoTheme {
    MetaFeatureSettingsScreen(
      configuration = configuration,
      onResetConfirmed = { requests.trySend(Request.ResetOverride) },
      onImportGeoIp = { requests.trySend(Request.ImportGeoIp) },
      onImportGeoSite = { requests.trySend(Request.ImportGeoSite) },
      onImportCountry = { requests.trySend(Request.ImportCountry) },
      onImportASN = { requests.trySend(Request.ImportASN) },
    )
  }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MetaFeatureSettingsScreen(
  configuration: ConfigurationOverride,
  onResetConfirmed: () -> Unit,
  onImportGeoIp: () -> Unit,
  onImportGeoSite: () -> Unit,
  onImportCountry: () -> Unit,
  onImportASN: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var showResetConfirmDialog by remember { mutableStateOf(false) }

  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
  MihomoScaffold(
    title = stringResource(R.string.meta_features),
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
      LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = innerPadding) {
        metaBasicPreferenceItems(configuration)
        metaSnifferPreferenceItems(configuration)
        metaGeoFileItems(onImportGeoIp, onImportGeoSite, onImportCountry, onImportASN)
      }
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

private fun LazyListScope.metaBasicPreferenceItems(configuration: ConfigurationOverride) {
  preferenceCategory(key = "cat_settings", title = { Text(stringResource(R.string.settings)) })

  item(key = "unifiedDelay", contentType = "ListPreference") {
    val state =
      rememberWriteThroughState(configuration.unifiedDelay) { configuration.unifiedDelay = it }
    val value by state
    SettingsListPreferenceItem(
      state = state,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      title = R.string.unified_delay,
      summary = value.textRes,
      valueToText = { it.textRes },
    )
  }
  item(key = "geodataMode", contentType = "ListPreference") {
    val state =
      rememberWriteThroughState(configuration.geodataMode) { configuration.geodataMode = it }
    val value by state
    SettingsListPreferenceItem(
      state = state,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      title = R.string.geodata_mode,
      summary = value.textRes,
      valueToText = { it.textRes },
    )
  }
  item(key = "tcpConcurrent", contentType = "ListPreference") {
    val state =
      rememberWriteThroughState(configuration.tcpConcurrent) { configuration.tcpConcurrent = it }
    val value by state
    SettingsListPreferenceItem(
      state = state,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      title = R.string.tcp_concurrent,
      summary = value.textRes,
      valueToText = { it.textRes },
    )
  }
  item(key = "findProcessMode", contentType = "ListPreference") {
    val state =
      rememberWriteThroughState(configuration.findProcessMode) {
        configuration.findProcessMode = it
      }
    val value by state
    SettingsListPreferenceItem(
      state = state,
      values = ConfigurationOverride.FindProcessMode.entries,
      modifier = Modifier.fillMaxWidth(),
      title = R.string.find_process_mode,
      summary = value.textRes,
      valueToText = { it.textRes },
    )
  }
}

private fun LazyListScope.metaSnifferPreferenceItems(configuration: ConfigurationOverride) {
  preferenceCategory(
    key = "cat_sniffer",
    title = { Text(stringResource(R.string.sniffer_setting)) },
  )

  item(key = "snifferEnable", contentType = "ListPreference") {
    val state =
      rememberWriteThroughState(configuration.sniffer.enable) { configuration.sniffer.enable = it }
    val value by state
    SettingsListPreferenceItem(
      state = state,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      title = R.string.strategy,
      summary = value.textRes,
      valueToText = { it.textRes },
    )
  }
  item(key = "sniffHttpPorts", contentType = "EditTextListPreference") {
    val enabled = configuration.sniffer.enable != false
    SettingsEditTextListPreferenceItem(
      title = R.string.sniff_http_ports,
      placeholder = R.string.dont_modify,
      state =
        rememberWriteThroughState(configuration.sniffer.sniff.http.ports) {
          configuration.sniffer.sniff.http.ports = it
        },
      enabled = enabled,
    )
  }
  item(key = "sniffHttpOverrideDestination", contentType = "ListPreference") {
    val enabled = configuration.sniffer.enable != false
    val state =
      rememberWriteThroughState(configuration.sniffer.sniff.http.overrideDestination) {
        configuration.sniffer.sniff.http.overrideDestination = it
      }
    val value by state
    SettingsListPreferenceItem(
      state = state,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      enabled = enabled,
      title = R.string.sniff_http_override_destination,
      summary = value.textRes,
      valueToText = { it.textRes },
    )
  }
  item(key = "sniffTlsPorts", contentType = "EditTextListPreference") {
    val enabled = configuration.sniffer.enable != false
    SettingsEditTextListPreferenceItem(
      title = R.string.sniff_tls_ports,
      placeholder = R.string.dont_modify,
      state =
        rememberWriteThroughState(configuration.sniffer.sniff.tls.ports) {
          configuration.sniffer.sniff.tls.ports = it
        },
      enabled = enabled,
    )
  }
  item(key = "sniffTlsOverrideDestination", contentType = "ListPreference") {
    val enabled = configuration.sniffer.enable != false
    val state =
      rememberWriteThroughState(configuration.sniffer.sniff.tls.overrideDestination) {
        configuration.sniffer.sniff.tls.overrideDestination = it
      }
    val value by state
    SettingsListPreferenceItem(
      state = state,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      enabled = enabled,
      title = R.string.sniff_tls_override_destination,
      summary = value.textRes,
      valueToText = { it.textRes },
    )
  }
  item(key = "sniffQuicPorts", contentType = "EditTextListPreference") {
    val enabled = configuration.sniffer.enable != false
    SettingsEditTextListPreferenceItem(
      title = R.string.sniff_quic_ports,
      placeholder = R.string.dont_modify,
      state =
        rememberWriteThroughState(configuration.sniffer.sniff.quic.ports) {
          configuration.sniffer.sniff.quic.ports = it
        },
      enabled = enabled,
    )
  }
  item(key = "sniffQuicOverrideDestination", contentType = "ListPreference") {
    val enabled = configuration.sniffer.enable != false
    val state =
      rememberWriteThroughState(configuration.sniffer.sniff.quic.overrideDestination) {
        configuration.sniffer.sniff.quic.overrideDestination = it
      }
    val value by state
    SettingsListPreferenceItem(
      state = state,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      enabled = enabled,
      title = R.string.sniff_quic_override_destination,
      summary = value.textRes,
      valueToText = { it.textRes },
    )
  }
  item(key = "forceDnsMapping", contentType = "ListPreference") {
    val enabled = configuration.sniffer.enable != false
    val state =
      rememberWriteThroughState(configuration.sniffer.forceDnsMapping) {
        configuration.sniffer.forceDnsMapping = it
      }
    val value by state
    SettingsListPreferenceItem(
      state = state,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      enabled = enabled,
      title = R.string.force_dns_mapping,
      summary = value.textRes,
      valueToText = { it.textRes },
    )
  }
  item(key = "parsePureIp", contentType = "ListPreference") {
    val enabled = configuration.sniffer.enable != false
    val state =
      rememberWriteThroughState(configuration.sniffer.parsePureIp) {
        configuration.sniffer.parsePureIp = it
      }
    val value by state
    SettingsListPreferenceItem(
      state = state,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      enabled = enabled,
      title = R.string.parse_pure_ip,
      summary = value.textRes,
      valueToText = { it.textRes },
    )
  }
  item(key = "overrideDestination", contentType = "ListPreference") {
    val enabled = configuration.sniffer.enable != false
    val state =
      rememberWriteThroughState(configuration.sniffer.overrideDestination) {
        configuration.sniffer.overrideDestination = it
      }
    val value by state
    SettingsListPreferenceItem(
      state = state,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      enabled = enabled,
      title = R.string.override_destination,
      summary = value.textRes,
      valueToText = { it.textRes },
    )
  }
  item(key = "forceDomain", contentType = "EditTextListPreference") {
    val enabled = configuration.sniffer.enable != false
    SettingsEditTextListPreferenceItem(
      title = R.string.force_domain,
      placeholder = R.string.dont_modify,
      state =
        rememberWriteThroughState(configuration.sniffer.forceDomain) {
          configuration.sniffer.forceDomain = it
        },
      enabled = enabled,
    )
  }
  item(key = "skipDomain", contentType = "EditTextListPreference") {
    val enabled = configuration.sniffer.enable != false
    SettingsEditTextListPreferenceItem(
      title = R.string.skip_domain,
      placeholder = R.string.dont_modify,
      state =
        rememberWriteThroughState(configuration.sniffer.skipDomain) {
          configuration.sniffer.skipDomain = it
        },
      enabled = enabled,
    )
  }
  item(key = "skipSrcAddress", contentType = "EditTextListPreference") {
    val enabled = configuration.sniffer.enable != false
    SettingsEditTextListPreferenceItem(
      title = R.string.skip_src_address,
      placeholder = R.string.dont_modify,
      state =
        rememberWriteThroughState(configuration.sniffer.skipSrcAddress) {
          configuration.sniffer.skipSrcAddress = it
        },
      enabled = enabled,
    )
  }
  item(key = "skipDstAddress", contentType = "EditTextListPreference") {
    val enabled = configuration.sniffer.enable != false
    SettingsEditTextListPreferenceItem(
      title = R.string.skip_dst_address,
      placeholder = R.string.dont_modify,
      state =
        rememberWriteThroughState(configuration.sniffer.skipDstAddress) {
          configuration.sniffer.skipDstAddress = it
        },
      enabled = enabled,
    )
  }
}

private fun LazyListScope.metaGeoFileItems(
  onImportGeoIp: () -> Unit,
  onImportGeoSite: () -> Unit,
  onImportCountry: () -> Unit,
  onImportASN: () -> Unit,
) {
  preferenceCategory(key = "cat_geox", title = { Text(stringResource(R.string.geox_files)) })

  item(key = "importGeoIp", contentType = "ClickablePreference") {
    SettingsClickablePreferenceItem(
      title = R.string.import_geoip_file,
      summary = R.string.press_to_import,
      onClick = onImportGeoIp,
    )
  }
  item(key = "importGeoSite", contentType = "ClickablePreference") {
    SettingsClickablePreferenceItem(
      title = R.string.import_geosite_file,
      summary = R.string.press_to_import,
      onClick = onImportGeoSite,
    )
  }
  item(key = "importCountry", contentType = "ClickablePreference") {
    SettingsClickablePreferenceItem(
      title = R.string.import_country_file,
      summary = R.string.press_to_import,
      onClick = onImportCountry,
    )
  }
  item(key = "importASN", contentType = "ClickablePreference") {
    SettingsClickablePreferenceItem(
      title = R.string.import_asn_file,
      summary = R.string.press_to_import,
      onClick = onImportASN,
    )
  }
}

private val ConfigurationOverride.FindProcessMode?.textRes: Int
  @StringRes
  get() =
    when (this) {
      ConfigurationOverride.FindProcessMode.Off -> R.string.off
      ConfigurationOverride.FindProcessMode.Strict -> R.string.strict
      ConfigurationOverride.FindProcessMode.Always -> R.string.always
      null -> R.string.dont_modify
    }

@PreviewMihomo
@Composable
private fun MetaFeatureSettingsScreenPreview() = MihomoTheme {
  MetaFeatureSettingsScreen(
    configuration = ConfigurationOverride(),
    onResetConfirmed = {},
    onImportGeoIp = {},
    onImportGeoSite = {},
    onImportCountry = {},
    onImportASN = {},
  )
}
