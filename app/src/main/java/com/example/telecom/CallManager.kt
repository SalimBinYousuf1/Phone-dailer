package com.example.telecom

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.telecom.Call
import android.telephony.SmsManager
import com.example.data.local.entity.CallLogEntity
import com.example.data.repository.CallLogRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object CallManager {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var timerJob: Job? = null
    private var toneGenerator: ToneGenerator? = null

    private var activeTelecomCall: Call? = null
    private var callLogRepository: CallLogRepository? = null

    private val _callState = MutableStateFlow<CallState>(CallState.Idle)
    val callState: StateFlow<CallState> = _callState.asStateFlow()

    private var callStartTime: Long = 0L

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_VOICE_CALL, 80)
        } catch (_: Exception) {
            // Audio manager not available in this environment
        }
    }

    fun init(repo: CallLogRepository) {
        callLogRepository = repo
    }

    fun registerTelecomCall(call: Call) {
        activeTelecomCall = call
        val number = call.details.handle?.schemeSpecificPart ?: "Unknown"

        call.registerCallback(object : Call.Callback() {
            override fun onStateChanged(call: Call, state: Int) {
                when (state) {
                    Call.STATE_RINGING -> {
                        _callState.value = CallState.Ringing(number = number)
                    }
                    Call.STATE_DIALING, Call.STATE_CONNECTING -> {
                        _callState.value = CallState.Outgoing(number = number)
                    }
                    Call.STATE_ACTIVE -> {
                        startActiveCallTimer(number)
                    }
                    Call.STATE_HOLDING -> {
                        val current = _callState.value
                        if (current is CallState.Active) {
                            _callState.value = current.copy(isOnHold = true)
                        }
                    }
                    Call.STATE_DISCONNECTED -> {
                        endCallInternal("Call ended")
                    }
                }
            }
        })

        if (call.state == Call.STATE_RINGING) {
            _callState.value = CallState.Ringing(number = number)
        } else if (call.state == Call.STATE_DIALING) {
            _callState.value = CallState.Outgoing(number = number)
        } else if (call.state == Call.STATE_ACTIVE) {
            startActiveCallTimer(number)
        }
    }

    fun startOutgoingCall(number: String, contactName: String? = null) {
        _callState.value = CallState.Outgoing(number = number, name = contactName)
        // If telecom call binds, it will take over; otherwise simulate connection after 2 seconds
        scope.launch {
            delay(2000)
            if (_callState.value is CallState.Outgoing) {
                startActiveCallTimer(number, contactName)
            }
        }
    }

    fun simulateIncomingCall(number: String, contactName: String? = null) {
        _callState.value = CallState.Ringing(number = number, name = contactName)
    }

    fun answerCall() {
        activeTelecomCall?.answer(0)
        val current = _callState.value
        if (current is CallState.Ringing) {
            startActiveCallTimer(current.number, current.name)
        }
    }

    fun rejectCall() {
        activeTelecomCall?.reject(false, null)
        val current = _callState.value
        if (current is CallState.Ringing) {
            logCall(current.number, current.name, CallLogEntity.TYPE_REJECTED, 0)
        }
        endCallInternal("Call declined")
    }

    fun disconnectCall() {
        activeTelecomCall?.disconnect()
        endCallInternal("Call ended")
    }

    private fun startActiveCallTimer(number: String, name: String? = null) {
        callStartTime = System.currentTimeMillis()
        timerJob?.cancel()
        _callState.value = CallState.Active(
            number = number,
            name = name,
            durationSeconds = 0,
            isMuted = false,
            isSpeakerOn = false,
            isOnHold = false
        )

        timerJob = scope.launch {
            var seconds = 0L
            while (true) {
                delay(1000)
                seconds++
                val curr = _callState.value
                if (curr is CallState.Active) {
                    _callState.value = curr.copy(durationSeconds = seconds)
                } else {
                    break
                }
            }
        }
    }

    private fun endCallInternal(reason: String) {
        timerJob?.cancel()
        val current = _callState.value
        if (current is CallState.Active) {
            logCall(current.number, current.name, CallLogEntity.TYPE_OUTGOING, current.durationSeconds)
        }
        _callState.value = CallState.Disconnected(reason)
        scope.launch {
            delay(1500)
            _callState.value = CallState.Idle
            activeTelecomCall = null
        }
    }

    private fun logCall(number: String, name: String?, type: Int, duration: Long) {
        scope.launch(Dispatchers.IO) {
            callLogRepository?.insertCallLog(
                CallLogEntity(
                    number = number,
                    name = name,
                    callType = type,
                    timestamp = System.currentTimeMillis(),
                    durationSeconds = duration,
                    simSlot = 0
                )
            )
        }
    }

    fun toggleMute() {
        val current = _callState.value
        if (current is CallState.Active) {
            _callState.value = current.copy(isMuted = !current.isMuted)
        }
    }

    fun toggleSpeaker(context: Context) {
        val current = _callState.value
        if (current is CallState.Active) {
            val nextSpeaker = !current.isSpeakerOn
            try {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                audioManager.isSpeakerphoneOn = nextSpeaker
            } catch (_: Exception) {}
            _callState.value = current.copy(isSpeakerOn = nextSpeaker)
        }
    }

    fun toggleHold() {
        val current = _callState.value
        if (current is CallState.Active) {
            val nextHold = !current.isOnHold
            if (nextHold) {
                activeTelecomCall?.hold()
            } else {
                activeTelecomCall?.unhold()
            }
            _callState.value = current.copy(isOnHold = nextHold)
        }
    }

    fun updateNotes(notes: String) {
        val current = _callState.value
        if (current is CallState.Active) {
            _callState.value = current.copy(notes = notes)
        }
    }

    fun playDtmf(digit: Char) {
        activeTelecomCall?.playDtmfTone(digit)
        val tone = when (digit) {
            '0' -> ToneGenerator.TONE_DTMF_0
            '1' -> ToneGenerator.TONE_DTMF_1
            '2' -> ToneGenerator.TONE_DTMF_2
            '3' -> ToneGenerator.TONE_DTMF_3
            '4' -> ToneGenerator.TONE_DTMF_4
            '5' -> ToneGenerator.TONE_DTMF_5
            '6' -> ToneGenerator.TONE_DTMF_6
            '7' -> ToneGenerator.TONE_DTMF_7
            '8' -> ToneGenerator.TONE_DTMF_8
            '9' -> ToneGenerator.TONE_DTMF_9
            '*' -> ToneGenerator.TONE_DTMF_S
            '#' -> ToneGenerator.TONE_DTMF_P
            else -> null
        }
        tone?.let { toneGenerator?.startTone(it, 150) }
    }

    fun stopDtmf() {
        activeTelecomCall?.stopDtmfTone()
    }

    fun sendQuickSms(context: Context, text: String) {
        val current = _callState.value
        val number = when (current) {
            is CallState.Active -> current.number
            is CallState.Ringing -> current.number
            else -> return
        }
        try {
            val smsManager = context.getSystemService(SmsManager::class.java)
            smsManager.sendTextMessage(number, null, text, null, null)
        } catch (_: Exception) {}

        if (current is CallState.Ringing) {
            rejectCall()
        }
    }
}
