package com.github.kr328.clash.settings.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.kr328.clash.R
import com.github.kr328.clash.core.model.ConfigurationOverride
import com.github.kr328.clash.settings.vm.MetaFeatureSettingsViewModel
import com.github.kr328.clash.settings.vm.MetaFeatureSettingsViewModel.ImportResult
import com.github.kr328.clash.settings.vm.MetaFeatureSettingsViewModel.ImportType
import com.github.kr328.clash.ui.component.MihomoScaffold
import com.github.kr328.clash.ui.component.SettingsClickablePreferenceItem
import com.github.kr328.clash.ui.component.SettingsEditTextListPreferenceItem
import com.github.kr328.clash.ui.component.SettingsListPreferenceItem
import com.github.kr328.clash.ui.theme.MihomoTheme
import com.github.kr328.clash.ui.theme.PreviewMihomo
import com.github.kr328.clash.util.toast
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.preferenceCategory

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MetaFeatureSettingsScreen(
  modifier: Modifier = Modifier,
  viewModel: MetaFeatureSettingsViewModel = viewModel(),
  onResetCompleted: () -> Unit,
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val importResult by viewModel.importResult.collectAsStateWithLifecycle()
  val importedText = stringResource(R.string.geofile_imported)
  var pendingImportType by remember { mutableStateOf<ImportType?>(null) }
  var showUnsupportedFormatDialog by remember { mutableStateOf(false) }
  var validExtensionsSummary by remember { mutableStateOf("") }
  var showResetConfirmDialog by remember { mutableStateOf(false) }

  DisposableEffect(viewModel) { onDispose { viewModel.persistOverride() } }

  LaunchedEffect(importResult) {
    when (val result = importResult) {
      ImportResult.NotStart,
      ImportResult.InProgress -> Unit

      is ImportResult.Success -> {
        context.toast(importedText.format(result.displayName))
      }

      is ImportResult.UnsupportedFormat -> {
        validExtensionsSummary = result.summary
        showUnsupportedFormatDialog = true
      }

      ImportResult.Failed -> context.toast(R.string.geofile_import_failed)
    }
  }

  val importLauncher =
    rememberLauncherForActivityResult(GetContent()) { uri ->
      val type = pendingImportType ?: return@rememberLauncherForActivityResult
      pendingImportType = null
      viewModel.importGeoFile(uri, type)
    }

  MetaFeatureSettingsContent(
    configuration = uiState,
    actions = viewModel,
    modifier = modifier,
    showResetConfirmDialog = showResetConfirmDialog,
    onShowResetConfirmDialogChange = { showResetConfirmDialog = it },
    onResetConfirmed = {
      viewModel.resetOverride()
      onResetCompleted()
    },
    onImportGeoIp = {
      pendingImportType = ImportType.GeoIp
      importLauncher.launch("*/*")
    },
    onImportGeoSite = {
      pendingImportType = ImportType.GeoSite
      importLauncher.launch("*/*")
    },
    onImportCountry = {
      pendingImportType = ImportType.Country
      importLauncher.launch("*/*")
    },
    onImportASN = {
      pendingImportType = ImportType.ASN
      importLauncher.launch("*/*")
    },
  )

  if (showUnsupportedFormatDialog) {
    AlertDialog(
      onDismissRequest = { showUnsupportedFormatDialog = false },
      title = { Text(stringResource(R.string.geofile_unknown_db_format)) },
      text = {
        Text(stringResource(R.string.geofile_unknown_db_format_message, validExtensionsSummary))
      },
      confirmButton = {
        TextButton(onClick = { showUnsupportedFormatDialog = false }) {
          Text(text = stringResource(R.string.ok))
        }
      },
    )
  }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MetaFeatureSettingsContent(
  configuration: ConfigurationOverride,
  actions: MetaFeatureSettingsActions,
  modifier: Modifier = Modifier,
  showResetConfirmDialog: Boolean,
  onShowResetConfirmDialogChange: (Boolean) -> Unit,
  onResetConfirmed: () -> Unit,
  onImportGeoIp: () -> Unit,
  onImportGeoSite: () -> Unit,
  onImportCountry: () -> Unit,
  onImportASN: () -> Unit,
) {
  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
  MihomoScaffold(
    title = stringResource(R.string.meta_features),
    modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    scrollBehavior = scrollBehavior,
    actions = {
      IconButton(onClick = { onShowResetConfirmDialogChange(true) }) {
        Icon(
          painter = painterResource(R.drawable.ic_baseline_replay),
          contentDescription = stringResource(R.string.reset),
        )
      }
    },
  ) { innerPadding ->
    ProvidePreferenceLocals {
      LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = innerPadding) {
        metaBasicPreferenceItems(configuration, actions)
        metaSnifferPreferenceItems(configuration, actions)
        metaGeoFileItems(
          onImportGeoIp = onImportGeoIp,
          onImportGeoSite = onImportGeoSite,
          onImportCountry = onImportCountry,
          onImportASN = onImportASN,
        )
      }
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

private fun LazyListScope.metaBasicPreferenceItems(
  configuration: ConfigurationOverride,
  actions: MetaFeatureSettingsActions,
) {
  preferenceCategory(key = "cat_settings", title = { Text(stringResource(R.string.settings)) })

  item(key = "unifiedDelay", contentType = "ListPreference") {
    SettingsListPreferenceItem(
      value = configuration.unifiedDelay,
      onValueChange = actions::updateUnifiedDelay,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      title = R.string.unified_delay,
      summary = configuration.unifiedDelay.textRes,
      valueToText = { it.textRes },
    )
  }
  item(key = "geodataMode", contentType = "ListPreference") {
    SettingsListPreferenceItem(
      value = configuration.geodataMode,
      onValueChange = actions::updateGeodataMode,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      title = R.string.geodata_mode,
      summary = configuration.geodataMode.textRes,
      valueToText = { it.textRes },
    )
  }
  item(key = "tcpConcurrent", contentType = "ListPreference") {
    SettingsListPreferenceItem(
      value = configuration.tcpConcurrent,
      onValueChange = actions::updateTcpConcurrent,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      title = R.string.tcp_concurrent,
      summary = configuration.tcpConcurrent.textRes,
      valueToText = { it.textRes },
    )
  }
  item(key = "findProcessMode", contentType = "ListPreference") {
    SettingsListPreferenceItem(
      value = configuration.findProcessMode,
      onValueChange = actions::updateFindProcessMode,
      values = ConfigurationOverride.FindProcessMode.entries,
      modifier = Modifier.fillMaxWidth(),
      title = R.string.find_process_mode,
      summary = configuration.findProcessMode.textRes,
      valueToText = { it.textRes },
    )
  }
}

private fun LazyListScope.metaSnifferPreferenceItems(
  configuration: ConfigurationOverride,
  actions: MetaFeatureSettingsActions,
) {
  preferenceCategory(
    key = "cat_sniffer",
    title = { Text(stringResource(R.string.sniffer_setting)) },
  )

  item(key = "snifferEnable", contentType = "ListPreference") {
    SettingsListPreferenceItem(
      value = configuration.sniffer.enable,
      onValueChange = actions::updateSnifferEnable,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      title = R.string.strategy,
      summary = configuration.sniffer.enable.textRes,
      valueToText = { it.textRes },
    )
  }
  item(key = "sniffHttpPorts", contentType = "EditTextListPreference") {
    val enabled = configuration.sniffer.enable != false
    SettingsEditTextListPreferenceItem(
      title = R.string.sniff_http_ports,
      placeholder = R.string.dont_modify,
      value = configuration.sniffer.sniff.http.ports,
      onValueChange = actions::updateSniffHttpPorts,
      enabled = enabled,
    )
  }
  item(key = "sniffHttpOverrideDestination", contentType = "ListPreference") {
    val enabled = configuration.sniffer.enable != false
    SettingsListPreferenceItem(
      value = configuration.sniffer.sniff.http.overrideDestination,
      onValueChange = actions::updateSniffHttpOverrideDestination,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      enabled = enabled,
      title = R.string.sniff_http_override_destination,
      summary = configuration.sniffer.sniff.http.overrideDestination.textRes,
      valueToText = { it.textRes },
    )
  }
  item(key = "sniffTlsPorts", contentType = "EditTextListPreference") {
    val enabled = configuration.sniffer.enable != false
    SettingsEditTextListPreferenceItem(
      title = R.string.sniff_tls_ports,
      placeholder = R.string.dont_modify,
      value = configuration.sniffer.sniff.tls.ports,
      onValueChange = actions::updateSniffTlsPorts,
      enabled = enabled,
    )
  }
  item(key = "sniffTlsOverrideDestination", contentType = "ListPreference") {
    val enabled = configuration.sniffer.enable != false
    SettingsListPreferenceItem(
      value = configuration.sniffer.sniff.tls.overrideDestination,
      onValueChange = actions::updateSniffTlsOverrideDestination,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      enabled = enabled,
      title = R.string.sniff_tls_override_destination,
      summary = configuration.sniffer.sniff.tls.overrideDestination.textRes,
      valueToText = { it.textRes },
    )
  }
  item(key = "sniffQuicPorts", contentType = "EditTextListPreference") {
    val enabled = configuration.sniffer.enable != false
    SettingsEditTextListPreferenceItem(
      title = R.string.sniff_quic_ports,
      placeholder = R.string.dont_modify,
      value = configuration.sniffer.sniff.quic.ports,
      onValueChange = actions::updateSniffQuicPorts,
      enabled = enabled,
    )
  }
  item(key = "sniffQuicOverrideDestination", contentType = "ListPreference") {
    val enabled = configuration.sniffer.enable != false
    SettingsListPreferenceItem(
      value = configuration.sniffer.sniff.quic.overrideDestination,
      onValueChange = actions::updateSniffQuicOverrideDestination,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      enabled = enabled,
      title = R.string.sniff_quic_override_destination,
      summary = configuration.sniffer.sniff.quic.overrideDestination.textRes,
      valueToText = { it.textRes },
    )
  }
  item(key = "forceDnsMapping", contentType = "ListPreference") {
    val enabled = configuration.sniffer.enable != false
    SettingsListPreferenceItem(
      value = configuration.sniffer.forceDnsMapping,
      onValueChange = actions::updateForceDnsMapping,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      enabled = enabled,
      title = R.string.force_dns_mapping,
      summary = configuration.sniffer.forceDnsMapping.textRes,
      valueToText = { it.textRes },
    )
  }
  item(key = "parsePureIp", contentType = "ListPreference") {
    val enabled = configuration.sniffer.enable != false
    SettingsListPreferenceItem(
      value = configuration.sniffer.parsePureIp,
      onValueChange = actions::updateParsePureIp,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      enabled = enabled,
      title = R.string.parse_pure_ip,
      summary = configuration.sniffer.parsePureIp.textRes,
      valueToText = { it.textRes },
    )
  }
  item(key = "overrideDestination", contentType = "ListPreference") {
    val enabled = configuration.sniffer.enable != false
    SettingsListPreferenceItem(
      value = configuration.sniffer.overrideDestination,
      onValueChange = actions::updateOverrideDestination,
      values = booleanOptions,
      modifier = Modifier.fillMaxWidth(),
      enabled = enabled,
      title = R.string.override_destination,
      summary = configuration.sniffer.overrideDestination.textRes,
      valueToText = { it.textRes },
    )
  }
  item(key = "forceDomain", contentType = "EditTextListPreference") {
    val enabled = configuration.sniffer.enable != false
    SettingsEditTextListPreferenceItem(
      title = R.string.force_domain,
      placeholder = R.string.dont_modify,
      value = configuration.sniffer.forceDomain,
      onValueChange = actions::updateForceDomain,
      enabled = enabled,
    )
  }
  item(key = "skipDomain", contentType = "EditTextListPreference") {
    val enabled = configuration.sniffer.enable != false
    SettingsEditTextListPreferenceItem(
      title = R.string.skip_domain,
      placeholder = R.string.dont_modify,
      value = configuration.sniffer.skipDomain,
      onValueChange = actions::updateSkipDomain,
      enabled = enabled,
    )
  }
  item(key = "skipSrcAddress", contentType = "EditTextListPreference") {
    val enabled = configuration.sniffer.enable != false
    SettingsEditTextListPreferenceItem(
      title = R.string.skip_src_address,
      placeholder = R.string.dont_modify,
      value = configuration.sniffer.skipSrcAddress,
      onValueChange = actions::updateSkipSrcAddress,
      enabled = enabled,
    )
  }
  item(key = "skipDstAddress", contentType = "EditTextListPreference") {
    val enabled = configuration.sniffer.enable != false
    SettingsEditTextListPreferenceItem(
      title = R.string.skip_dst_address,
      placeholder = R.string.dont_modify,
      value = configuration.sniffer.skipDstAddress,
      onValueChange = actions::updateSkipDstAddress,
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

interface MetaFeatureSettingsActions {
  fun updateUnifiedDelay(value: Boolean?) = Unit

  fun updateGeodataMode(value: Boolean?) = Unit

  fun updateTcpConcurrent(value: Boolean?) = Unit

  fun updateFindProcessMode(value: ConfigurationOverride.FindProcessMode?) = Unit

  fun updateSnifferEnable(value: Boolean?) = Unit

  fun updateSniffHttpPorts(value: List<String>?) = Unit

  fun updateSniffHttpOverrideDestination(value: Boolean?) = Unit

  fun updateSniffTlsPorts(value: List<String>?) = Unit

  fun updateSniffTlsOverrideDestination(value: Boolean?) = Unit

  fun updateSniffQuicPorts(value: List<String>?) = Unit

  fun updateSniffQuicOverrideDestination(value: Boolean?) = Unit

  fun updateForceDnsMapping(value: Boolean?) = Unit

  fun updateParsePureIp(value: Boolean?) = Unit

  fun updateOverrideDestination(value: Boolean?) = Unit

  fun updateForceDomain(value: List<String>?) = Unit

  fun updateSkipDomain(value: List<String>?) = Unit

  fun updateSkipSrcAddress(value: List<String>?) = Unit

  fun updateSkipDstAddress(value: List<String>?) = Unit
}

@PreviewMihomo
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MetaFeatureSettingsContentPreview() = MihomoTheme {
  MetaFeatureSettingsContent(
    configuration = ConfigurationOverride(),
    actions = object : MetaFeatureSettingsActions {},
    showResetConfirmDialog = false,
    onShowResetConfirmDialogChange = {},
    onResetConfirmed = {},
    onImportGeoIp = {},
    onImportGeoSite = {},
    onImportCountry = {},
    onImportASN = {},
  )
}
