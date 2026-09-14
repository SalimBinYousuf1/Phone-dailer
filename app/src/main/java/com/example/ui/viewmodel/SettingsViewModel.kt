package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.CallLogRepository
import com.example.data.repository.ContactsRepository
import com.example.data.repository.DialerPreferencesRepository
import com.example.telecom.TelecomHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isDefaultDialer: Boolean = false,
    val themeMode: String = "system",
    val hapticsEnabled: Boolean = true,
    val dialpadTonesEnabled: Boolean = true,
    val defaultSim: Int = 0,
    val silenceUnknown: Boolean = false,
    val autoFormat: Boolean = true,
    val quickResponses: List<String> = emptyList(),
    val exportSuccessMessage: String? = null
)

class SettingsViewModel(
    private val preferencesRepository: DialerPreferencesRepository,
    private val callLogRepository: CallLogRepository,
    private val contactsRepository: ContactsRepository
) : ViewModel() {

    private val _isDefaultDialer = MutableStateFlow(false)
    private val _exportMessage = MutableStateFlow<String?>(null)

    private data class BasePrefs(
        val theme: String,
        val haptics: Boolean,
        val tones: Boolean,
        val sim: Int,
        val silence: Boolean
    )

    private val basePrefsFlow = combine(
        preferencesRepository.themeMode,
        preferencesRepository.hapticsEnabled,
        preferencesRepository.dialpadTonesEnabled,
        preferencesRepository.defaultSim,
        preferencesRepository.silenceUnknown
    ) { theme, haptics, tones, sim, silence ->
        BasePrefs(theme, haptics, tones, sim, silence)
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        _isDefaultDialer,
        basePrefsFlow,
        preferencesRepository.autoFormat,
        preferencesRepository.quickResponses,
        _exportMessage
    ) { dialer, base, autoFmt, responses, msg ->
        SettingsUiState(
            isDefaultDialer = dialer,
            themeMode = base.theme,
            hapticsEnabled = base.haptics,
            dialpadTonesEnabled = base.tones,
            defaultSim = base.sim,
            silenceUnknown = base.silence,
            autoFormat = autoFmt,
            quickResponses = responses,
            exportSuccessMessage = msg
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        SettingsUiState()
    )

    fun checkDefaultDialer(context: Context) {
        _isDefaultDialer.value = TelecomHelper.isDefaultDialer(context)
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun toggleHaptics() {
        viewModelScope.launch {
            preferencesRepository.setHapticsEnabled(!uiState.value.hapticsEnabled)
        }
    }

    fun toggleDialpadTones() {
        viewModelScope.launch {
            preferencesRepository.setDialpadTonesEnabled(!uiState.value.dialpadTonesEnabled)
        }
    }

    fun setDefaultSim(sim: Int) {
        viewModelScope.launch {
            preferencesRepository.setDefaultSim(sim)
        }
    }

    fun toggleSilenceUnknown() {
        viewModelScope.launch {
            preferencesRepository.setSilenceUnknown(!uiState.value.silenceUnknown)
        }
    }

    fun toggleAutoFormat() {
        viewModelScope.launch {
            preferencesRepository.setAutoFormat(!uiState.value.autoFormat)
        }
    }

    fun updateQuickResponses(responses: List<String>) {
        viewModelScope.launch {
            preferencesRepository.updateQuickResponses(responses)
        }
    }

    fun exportCallLogCsv(onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            val logs = callLogRepository.allCallLogs.first()
            val csv = callLogRepository.exportCsv(logs)
            _exportMessage.value = "Exported ${logs.size} call records to CSV"
            onSuccess(csv)
        }
    }

    fun exportContactsVCard(onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            val contacts = contactsRepository.allContacts.first()
            val vCard = contactsRepository.exportVCard(contacts)
            _exportMessage.value = "Exported ${contacts.size} contacts to vCard"
            onSuccess(vCard)
        }
    }

    fun clearExportMessage() {
        _exportMessage.value = null
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            callLogRepository.clearAllCallLogs()
            _exportMessage.value = "Call history cleared"
        }
    }
}
