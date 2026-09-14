package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.DialerPreferencesRepository
import com.example.telecom.CallManager
import com.example.telecom.CallState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class InCallUiState(
    val callState: CallState = CallState.Idle,
    val quickResponses: List<String> = emptyList(),
    val showDtmfKeypad: Boolean = false,
    val showQuickSmsSheet: Boolean = false,
    val showNotesSheet: Boolean = false
)

class InCallViewModel(
    preferencesRepository: DialerPreferencesRepository
) : ViewModel() {

    val quickResponses: StateFlow<List<String>> = preferencesRepository.quickResponses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val callState: StateFlow<CallState> = CallManager.callState

    fun answerCall() {
        CallManager.answerCall()
    }

    fun rejectCall() {
        CallManager.rejectCall()
    }

    fun disconnectCall() {
        CallManager.disconnectCall()
    }

    fun toggleMute() {
        CallManager.toggleMute()
    }

    fun toggleSpeaker(context: Context) {
        CallManager.toggleSpeaker(context)
    }

    fun toggleHold() {
        CallManager.toggleHold()
    }

    fun playDtmf(digit: Char) {
        CallManager.playDtmf(digit)
    }

    fun updateNotes(notes: String) {
        CallManager.updateNotes(notes)
    }

    fun sendQuickSms(context: Context, message: String) {
        CallManager.sendQuickSms(context, message)
    }
}
