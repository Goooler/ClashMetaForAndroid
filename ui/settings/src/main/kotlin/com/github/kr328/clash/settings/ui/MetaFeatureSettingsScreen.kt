package com.github.kr328.clash.settings.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.core.model.ConfigurationOverride
import com.github.kr328.clash.settings.R
import com.github.kr328.clash.settings.vm.MetaFeatureSettingsViewModel
import com.github.kr328.clash.settings.vm.MetaFeatureSettingsViewModel.ImportType
import com.github.kr328.clash.ui.component.TabbyScaffold
import com.github.kr328.clash.ui.icon.BaselineReplay
import com.github.kr328.clash.ui.icon.TabbyIcons
import com.github.kr328.clash.ui.lifecycle.viewModelWithLifecycle
import com.github.kr328.clash.ui.nav.TabbyNavDisplay
import com.github.kr328.clash.ui.nav.addIfNotLast
import com.github.kr328.clash.ui.nav.rememberNavBackStackBuilder
import com.github.kr328.clash.ui.theme.PreviewTabby
import com.github.kr328.clash.ui.theme.TabbyThemeWrapper
import kotlinx.serialization.Serializable
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.listPreference
import me.zhanghai.compose.preference.preference
import me.zhanghai.compose.preference.preferenceCategory

private sealed interface MetaFeatureSettingsRoute : NavKey {
  @Serializable data object Main : MetaFeatureSettingsRoute
}

@Composable
internal fun MetaFeatureSettingsScreen(
  modifier: Modifier = Modifier,
  viewModel: MetaFeatureSettingsViewModel = viewModelWithLifecycle(),
  onResetCompleted: () -> Unit,
) {
  val backStack = rememberNavBackStackBuilder { add(MetaFeatureSettingsRoute.Main) }
  var currentEditableTextListOnApply by remember {
    mutableStateOf<((List<String>?) -> Unit)?>(null)
  }

  TabbyNavDisplay(
    backStack = backStack,
    entryProvider =
      entryProvider {
        entry<MetaFeatureSettingsRoute.Main> {
          val configuration by viewModel.configuration.collectAsStateWithLifecycle()
          val importResult by viewModel.importResult.collectAsStateWithLifecycle()
          val snackbarHostState = remember { SnackbarHostState() }
          val importedText = stringResource(R.string.geofile_imported)
          val importFailedText = stringResource(R.string.geofile_import_failed)
          var pendingImportType by remember { mutableStateOf<ImportType?>(null) }
          var showUnsupportedFormatDialog by remember { mutableStateOf(false) }
          var validExtensionsSummary by remember { mutableStateOf("") }
          var showResetConfirmDialog by remember { mutableStateOf(false) }

          LaunchedEffect(importResult) {
            when (val result = importResult) {
              Idle,
              InProgress -> Unit

              is Success -> {
                snackbarHostState.showSnackbar(message = importedText.format(result.displayName))
              }

              is UnsupportedFormat -> {
                validExtensionsSummary = result.summary
                showUnsupportedFormatDialog = true
              }

              Failed -> snackbarHostState.showSnackbar(message = importFailedText)
            }
          }

          val importLauncher =
            rememberLauncherForActivityResult(GetContent()) { uri ->
              val type = pendingImportType ?: return@rememberLauncherForActivityResult
              pendingImportType = null
              viewModel.importGeoFile(uri, type)
            }

          MetaFeatureSettingsContent(
            configuration = configuration,
            actions = viewModel,
            snackbarHostState = snackbarHostState,
            modifier = modifier,
            showResetConfirmDialog = showResetConfirmDialog,
            onShowResetConfirmDialogChange = { showResetConfirmDialog = it },
            onResetConfirmed = {
              viewModel.resetOverride()
              onResetCompleted()
            },
            onImportGeoIp = {
              pendingImportType = GeoIp
              importLauncher.launch("*/*")
            },
            onImportGeoSite = {
              pendingImportType = GeoSite
              importLauncher.launch("*/*")
            },
            onImportCountry = {
              pendingImportType = Country
              importLauncher.launch("*/*")
            },
            onImportASN = {
              pendingImportType = ASN
              importLauncher.launch("*/*")
            },
            onOpenEditableTextList = { title, initialValues, onApply ->
              currentEditableTextListOnApply = onApply
              backStack.addIfNotLast(EditableTextList(title, initialValues))
            },
          )

          if (showUnsupportedFormatDialog) {
            AlertDialog(
              onDismissRequest = { showUnsupportedFormatDialog = false },
              title = { Text(stringResource(R.string.geofile_unknown_db_format)) },
              text = {
                Text(
                  stringResource(R.string.geofile_unknown_db_format_message, validExtensionsSummary)
                )
              },
              confirmButton = {
                TextButton(onClick = { showUnsupportedFormatDialog = false }) {
                  Text(text = stringResource(CommonR.string.ok))
                }
              },
            )
          }
        }
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
private fun MetaFeatureSettingsContent(
  configuration: ConfigurationOverride,
  actions: MetaFeatureSettingsActions,
  snackbarHostState: SnackbarHostState,
  modifier: Modifier = Modifier,
  showResetConfirmDialog: Boolean,
  onShowResetConfirmDialogChange: (Boolean) -> Unit,
  onResetConfirmed: () -> Unit,
  onImportGeoIp: () -> Unit,
  onImportGeoSite: () -> Unit,
  onImportCountry: () -> Unit,
  onImportASN: () -> Unit,
  onOpenEditableTextList: (Int, List<String>?, (List<String>?) -> Unit) -> Unit,
) {
  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
  TabbyScaffold(
    title = stringResource(R.string.meta_features),
    modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    snackbarHostState = snackbarHostState,
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
        metaBasicPreferenceItems(configuration, actions)
        metaSnifferPreferenceItems(configuration, actions, onOpenEditableTextList)
        metaGeoFileItems(
          onImportGeoIp = onImportGeoIp,
          onImportGeoSite = onImportGeoSite,
          onImportCountry = onImportCountry,
          onImportASN = onImportASN,
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

private fun LazyListScope.metaBasicPreferenceItems(
  configuration: ConfigurationOverride,
  actions: MetaFeatureSettingsActions,
) {
  preferenceCategory(key = "cat_general", title = { Text(stringResource(R.string.general)) })
  listPreference(
    key = "unifiedDelay",
    value = configuration.unifiedDelay,
    onValueChange = actions::updateUnifiedDelay,
    values = booleanOptions,
    title = { Text(stringResource(R.string.unified_delay)) },
    summary = { Text(stringResource(configuration.unifiedDelay.textRes)) },
    valueToText = { AnnotatedString(stringResource(it.textRes)) },
  )
  listPreference(
    key = "geodataMode",
    value = configuration.geodataMode,
    onValueChange = actions::updateGeodataMode,
    values = booleanOptions,
    title = { Text(stringResource(R.string.geodata_mode)) },
    summary = { Text(stringResource(configuration.geodataMode.textRes)) },
    valueToText = { AnnotatedString(stringResource(it.textRes)) },
  )
  listPreference(
    key = "tcpConcurrent",
    value = configuration.tcpConcurrent,
    onValueChange = actions::updateTcpConcurrent,
    values = booleanOptions,
    title = { Text(stringResource(R.string.tcp_concurrent)) },
    summary = { Text(stringResource(configuration.tcpConcurrent.textRes)) },
    valueToText = { AnnotatedString(stringResource(it.textRes)) },
  )
  listPreference(
    key = "findProcessMode",
    value = configuration.findProcessMode,
    onValueChange = actions::updateFindProcessMode,
    values = ConfigurationOverride.FindProcessMode.entries,
    title = { Text(stringResource(R.string.find_process_mode)) },
    summary = { Text(stringResource(configuration.findProcessMode.textRes)) },
    valueToText = { AnnotatedString(stringResource(it.textRes)) },
  )
}

private fun LazyListScope.metaSnifferPreferenceItems(
  configuration: ConfigurationOverride,
  actions: MetaFeatureSettingsActions,
  onOpenEditableTextList: (Int, List<String>?, (List<String>?) -> Unit) -> Unit,
) {
  val enabled = configuration.sniffer.enable != false
  preferenceCategory(
    key = "cat_sniffer",
    title = { Text(stringResource(R.string.sniffer_setting)) },
  )
  listPreference(
    key = "snifferEnable",
    value = configuration.sniffer.enable,
    onValueChange = actions::updateSnifferEnable,
    values = booleanOptions,
    title = { Text(stringResource(R.string.strategy)) },
    summary = { Text(stringResource(configuration.sniffer.enable.textRes)) },
    valueToText = { AnnotatedString(stringResource(it.textRes)) },
  )
  preference(
    key = "sniffHttpPorts",
    title = { Text(stringResource(R.string.sniff_http_ports)) },
    summary = { Text(configuration.sniffer.sniff.http.ports.listSummary(R.string.dont_modify)) },
    enabled = enabled,
    onClick = {
      onOpenEditableTextList(
        R.string.sniff_http_ports,
        configuration.sniffer.sniff.http.ports,
        actions::updateSniffHttpPorts,
      )
    },
  )
  listPreference(
    key = "sniffHttpOverrideDestination",
    value = configuration.sniffer.sniff.http.overrideDestination,
    onValueChange = actions::updateSniffHttpOverrideDestination,
    values = booleanOptions,
    enabled = enabled,
    title = { Text(stringResource(R.string.sniff_http_override_destination)) },
    summary = {
      Text(stringResource(configuration.sniffer.sniff.http.overrideDestination.textRes))
    },
    valueToText = { AnnotatedString(stringResource(it.textRes)) },
  )
  preference(
    key = "sniffTlsPorts",
    title = { Text(stringResource(R.string.sniff_tls_ports)) },
    summary = { Text(configuration.sniffer.sniff.tls.ports.listSummary(R.string.dont_modify)) },
    enabled = enabled,
    onClick = {
      onOpenEditableTextList(
        R.string.sniff_tls_ports,
        configuration.sniffer.sniff.tls.ports,
        actions::updateSniffTlsPorts,
      )
    },
  )
  listPreference(
    key = "sniffTlsOverrideDestination",
    value = configuration.sniffer.sniff.tls.overrideDestination,
    onValueChange = actions::updateSniffTlsOverrideDestination,
    values = booleanOptions,
    enabled = enabled,
    title = { Text(stringResource(R.string.sniff_tls_override_destination)) },
    summary = { Text(stringResource(configuration.sniffer.sniff.tls.overrideDestination.textRes)) },
    valueToText = { AnnotatedString(stringResource(it.textRes)) },
  )
  preference(
    key = "sniffQuicPorts",
    title = { Text(stringResource(R.string.sniff_quic_ports)) },
    summary = { Text(configuration.sniffer.sniff.quic.ports.listSummary(R.string.dont_modify)) },
    enabled = enabled,
    onClick = {
      onOpenEditableTextList(
        R.string.sniff_quic_ports,
        configuration.sniffer.sniff.quic.ports,
        actions::updateSniffQuicPorts,
      )
    },
  )
  listPreference(
    key = "sniffQuicOverrideDestination",
    value = configuration.sniffer.sniff.quic.overrideDestination,
    onValueChange = actions::updateSniffQuicOverrideDestination,
    values = booleanOptions,
    enabled = enabled,
    title = { Text(stringResource(R.string.sniff_quic_override_destination)) },
    summary = {
      Text(stringResource(configuration.sniffer.sniff.quic.overrideDestination.textRes))
    },
    valueToText = { AnnotatedString(stringResource(it.textRes)) },
  )
  listPreference(
    key = "forceDnsMapping",
    value = configuration.sniffer.forceDnsMapping,
    onValueChange = actions::updateForceDnsMapping,
    values = booleanOptions,
    enabled = enabled,
    title = { Text(stringResource(R.string.force_dns_mapping)) },
    summary = { Text(stringResource(configuration.sniffer.forceDnsMapping.textRes)) },
    valueToText = { AnnotatedString(stringResource(it.textRes)) },
  )
  listPreference(
    key = "parsePureIp",
    value = configuration.sniffer.parsePureIp,
    onValueChange = actions::updateParsePureIp,
    values = booleanOptions,
    enabled = enabled,
    title = { Text(stringResource(R.string.parse_pure_ip)) },
    summary = { Text(stringResource(configuration.sniffer.parsePureIp.textRes)) },
    valueToText = { AnnotatedString(stringResource(it.textRes)) },
  )
  listPreference(
    key = "overrideDestination",
    value = configuration.sniffer.overrideDestination,
    onValueChange = actions::updateOverrideDestination,
    values = booleanOptions,
    enabled = enabled,
    title = { Text(stringResource(R.string.override_destination)) },
    summary = { Text(stringResource(configuration.sniffer.overrideDestination.textRes)) },
    valueToText = { AnnotatedString(stringResource(it.textRes)) },
  )
  preference(
    key = "forceDomain",
    title = { Text(stringResource(R.string.force_domain)) },
    summary = { Text(configuration.sniffer.forceDomain.listSummary(R.string.dont_modify)) },
    enabled = enabled,
    onClick = {
      onOpenEditableTextList(
        R.string.force_domain,
        configuration.sniffer.forceDomain,
        actions::updateForceDomain,
      )
    },
  )
  preference(
    key = "skipDomain",
    title = { Text(stringResource(R.string.skip_domain)) },
    summary = { Text(configuration.sniffer.skipDomain.listSummary(R.string.dont_modify)) },
    enabled = enabled,
    onClick = {
      onOpenEditableTextList(
        R.string.skip_domain,
        configuration.sniffer.skipDomain,
        actions::updateSkipDomain,
      )
    },
  )
  preference(
    key = "skipSrcAddress",
    title = { Text(stringResource(R.string.skip_src_address)) },
    summary = { Text(configuration.sniffer.skipSrcAddress.listSummary(R.string.dont_modify)) },
    enabled = enabled,
    onClick = {
      onOpenEditableTextList(
        R.string.skip_src_address,
        configuration.sniffer.skipSrcAddress,
        actions::updateSkipSrcAddress,
      )
    },
  )
  preference(
    key = "skipDstAddress",
    title = { Text(stringResource(R.string.skip_dst_address)) },
    summary = { Text(configuration.sniffer.skipDstAddress.listSummary(R.string.dont_modify)) },
    enabled = enabled,
    onClick = {
      onOpenEditableTextList(
        R.string.skip_dst_address,
        configuration.sniffer.skipDstAddress,
        actions::updateSkipDstAddress,
      )
    },
  )
}

private fun LazyListScope.metaGeoFileItems(
  onImportGeoIp: () -> Unit,
  onImportGeoSite: () -> Unit,
  onImportCountry: () -> Unit,
  onImportASN: () -> Unit,
) {
  preferenceCategory(key = "cat_geox", title = { Text(stringResource(R.string.geox_files)) })
  preference(
    key = "importGeoIp",
    title = { Text(stringResource(R.string.import_geoip_file)) },
    summary = { Text(stringResource(R.string.press_to_import)) },
    onClick = onImportGeoIp,
  )
  preference(
    key = "importGeoSite",
    title = { Text(stringResource(R.string.import_geosite_file)) },
    summary = { Text(stringResource(R.string.press_to_import)) },
    onClick = onImportGeoSite,
  )
  preference(
    key = "importCountry",
    title = { Text(stringResource(R.string.import_country_file)) },
    summary = { Text(stringResource(R.string.press_to_import)) },
    onClick = onImportCountry,
  )
  preference(
    key = "importASN",
    title = { Text(stringResource(R.string.import_asn_file)) },
    summary = { Text(stringResource(R.string.press_to_import)) },
    onClick = onImportASN,
  )
}

private val ConfigurationOverride.FindProcessMode?.textRes: Int
  @StringRes
  get() =
    when (this) {
      Off -> R.string.off
      Strict -> R.string.strict
      Always -> R.string.always
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

@PreviewWrapper(TabbyThemeWrapper::class)
@PreviewTabby
@Composable
private fun MetaFeatureSettingsContentPreview() {
  MetaFeatureSettingsContent(
    configuration = ConfigurationOverride(),
    actions = object : MetaFeatureSettingsActions {},
    snackbarHostState = SnackbarHostState(),
    showResetConfirmDialog = false,
    onShowResetConfirmDialogChange = {},
    onResetConfirmed = {},
    onImportGeoIp = {},
    onImportGeoSite = {},
    onImportCountry = {},
    onImportASN = {},
    onOpenEditableTextList = { _, _, _ -> },
  )
}
