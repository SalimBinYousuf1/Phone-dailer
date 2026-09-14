package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.VoicemailEntity
import com.example.data.repository.VoicemailRepository
import com.example.telecom.TelecomHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class VoicemailUiState(
    val voicemails: List<VoicemailEntity> = emptyList(),
    val activePlayingId: Long? = null,
    val isPlaying: Boolean = false,
    val playbackProgress: Float = 0f,
    val currentPositionSeconds: Long = 0
)

class VoicemailViewModel(
    private val voicemailRepository: VoicemailRepository
) : ViewModel() {

    private val _activePlayingId = MutableStateFlow<Long?>(null)
    private val _isPlaying = MutableStateFlow(false)
    private val _playbackProgress = MutableStateFlow(0f)
    private val _currentSeconds = MutableStateFlow(0L)

    private var playbackJob: Job? = null

    val uiState: StateFlow<VoicemailUiState> = combine(
        voicemailRepository.allVoicemails,
        _activePlayingId,
        _isPlaying,
        _playbackProgress,
        _currentSeconds
    ) { list, activeId, isPlaying, progress, seconds ->
        VoicemailUiState(
            voicemails = list,
            activePlayingId = activeId,
            isPlaying = isPlaying,
            playbackProgress = progress,
            currentPositionSeconds = seconds
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        VoicemailUiState()
    )

    fun togglePlay(voicemail: VoicemailEntity) {
        if (_activePlayingId.value == voicemail.id && _isPlaying.value) {
            pause()
        } else {
            play(voicemail)
        }
    }

    private fun play(voicemail: VoicemailEntity) {
        playbackJob?.cancel()
        _activePlayingId.value = voicemail.id
        _isPlaying.value = true

        // Mark as read
        if (!voicemail.isRead) {
            viewModelScope.launch {
                voicemailRepository.markAsRead(voicemail.id)
            }
        }

        playbackJob = viewModelScope.launch {
            val totalSeconds = voicemail.durationSeconds.coerceAtLeast(1)
            var current = _currentSeconds.value
            if (current >= totalSeconds) current = 0L

            while (current < totalSeconds && _isPlaying.value) {
                delay(1000)
                current++
                _currentSeconds.value = current
                _playbackProgress.value = current.toFloat() / totalSeconds.toFloat()
            }
            _isPlaying.value = false
            _currentSeconds.value = 0L
            _playbackProgress.value = 0f
        }
    }

    fun pause() {
        _isPlaying.value = false
        playbackJob?.cancel()
    }

    fun delete(id: Long) {
        if (_activePlayingId.value == id) {
            pause()
            _activePlayingId.value = null
        }
        viewModelScope.launch {
            voicemailRepository.deleteVoicemail(id)
        }
    }

    fun callVoicemailBox(context: Context) {
        TelecomHelper.placeCall(context, "*86", "Carrier Voicemail")
    }

    fun callBack(context: Context, number: String, name: String? = null) {
        TelecomHelper.placeCall(context, number, name)
    }
}
