package com.LDadb.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.LDadb.AppServices
import com.LDadb.R
import com.LDadb.data.AppSettingsStore
import com.LDadb.data.RecentDeviceStore
import com.LDadb.data.ThemeMode
import com.LDadb.discovery.NetworkInfoProvider
import com.LDadb.discovery.ScanRangeParser
import com.LDadb.i18n.AppLanguage
import com.LDadb.i18n.appString
import com.LDadb.scrcpy.MirrorQualityPreset
import com.LDadb.validation.NetworkInputValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val defaultPort: String = "5555",
    val connectionTimeoutSeconds: String = "10",
    val commandTimeoutSeconds: String = "30",
    val scanRanges: String = "",
    val themeMode: ThemeMode = ThemeMode.System,
    val mirrorQualityPreset: MirrorQualityPreset = MirrorQualityPreset.Balanced,
    val language: AppLanguage = AppLanguage.FollowSystem,
    val defaultPortError: String? = null,
    val connectionTimeoutError: String? = null,
    val commandTimeoutError: String? = null,
    val scanRangesError: String? = null,
)

class SettingsViewModel(
    private val settingsStore: AppSettingsStore = AppServices.settingsStore,
    private val recentDeviceStore: RecentDeviceStore = AppServices.recentDeviceStore,
    private val networkInfoProvider: NetworkInfoProvider = AppServices.networkInfoProvider,
) : ViewModel() {
    private val state = MutableStateFlow(SettingsUiState(language = AppLanguage.current()))
    val uiState: StateFlow<SettingsUiState> = state.asStateFlow()
    private var defaultScanRangeSaved = false

    init {
        viewModelScope.launch {
            settingsStore.settings.collect { settings ->
                val scanRanges = settings.scanRanges.ifBlank { currentDefaultScanRange() }
                state.value = state.value.copy(
                    defaultPort = settings.defaultPort.toString(),
                    connectionTimeoutSeconds = settings.connectionTimeoutSeconds.toString(),
                    commandTimeoutSeconds = settings.commandTimeoutSeconds.toString(),
                    scanRanges = scanRanges,
                    themeMode = settings.themeMode,
                    mirrorQualityPreset = settings.mirrorQualityPreset,
                    defaultPortError = null,
                    connectionTimeoutError = null,
                    commandTimeoutError = null,
                    scanRangesError = null,
                )
                if (!defaultScanRangeSaved && settings.scanRanges.isBlank() && scanRanges.isNotBlank()) {
                    defaultScanRangeSaved = true
                    settingsStore.updateScanRanges(scanRanges)
                }
            }
        }
    }

    fun onDefaultPortChanged(value: String) {
        val filtered = value.filter { it.isDigit() }.take(5)
        val error = NetworkInputValidator.portError(filtered)?.resolve(AppServices.context)
        state.value = state.value.copy(defaultPort = filtered, defaultPortError = error)
        val port = filtered.toIntOrNull()
        if (port != null && error == null) {
            viewModelScope.launch {
                settingsStore.updateDefaultPort(port)
            }
        }
    }

    fun onConnectionTimeoutChanged(value: String) {
        updateTimeout(
            value = value,
            onState = { text, error ->
                state.value = state.value.copy(
                    connectionTimeoutSeconds = text,
                    connectionTimeoutError = error,
                )
            },
            onSave = settingsStore::updateConnectionTimeoutSeconds,
        )
    }

    fun onCommandTimeoutChanged(value: String) {
        updateTimeout(
            value = value,
            onState = { text, error ->
                state.value = state.value.copy(
                    commandTimeoutSeconds = text,
                    commandTimeoutError = error,
                )
            },
            onSave = settingsStore::updateCommandTimeoutSeconds,
        )
    }

    fun onScanRangesChanged(value: String) {
        val normalized = value
            .lineSequence()
            .joinToString("\n") { line -> line.trim().take(32) }
        val error = ScanRangeParser.validationError(normalized)?.resolve(AppServices.context)
        state.value = state.value.copy(scanRanges = normalized, scanRangesError = error)
        if (error == null) {
            viewModelScope.launch {
                settingsStore.updateScanRanges(normalized)
            }
        }
    }

    fun onThemeModeSelected(themeMode: ThemeMode) {
        state.value = state.value.copy(themeMode = themeMode)
        viewModelScope.launch {
            settingsStore.updateThemeMode(themeMode)
        }
    }

    fun onMirrorQualityPresetSelected(preset: MirrorQualityPreset) {
        state.value = state.value.copy(mirrorQualityPreset = preset)
        viewModelScope.launch {
            settingsStore.updateMirrorQualityPreset(preset)
        }
    }

    fun onLanguageSelected(language: AppLanguage) {
        state.value = state.value.copy(language = language)
        AppLanguage.apply(language)
    }

    fun onClearRecentDevicesClicked() {
        viewModelScope.launch {
            recentDeviceStore.clear()
        }
    }

    private fun updateTimeout(
        value: String,
        onState: (String, String?) -> Unit,
        onSave: suspend (Int) -> Unit,
    ) {
        val filtered = value.filter { it.isDigit() }.take(3)
        val seconds = filtered.toIntOrNull()
        val error = when {
            filtered.isBlank() -> appString(R.string.settings_timeout_required)
            seconds == null || seconds !in 1..300 -> appString(R.string.settings_timeout_range)
            else -> null
        }
        onState(filtered, error)
        if (seconds != null && error == null) {
            viewModelScope.launch {
                onSave(seconds)
            }
        }
    }

    private fun currentDefaultScanRange(): String {
        return networkInfoProvider.currentLocalNetworks()
            .firstOrNull()
            ?.subnetLabel
            .orEmpty()
    }
}
