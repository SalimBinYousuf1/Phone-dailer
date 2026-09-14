package com.example.telecom

sealed class CallState {
    object Idle : CallState()
    data class Outgoing(val number: String, val name: String? = null) : CallState()
    data class Ringing(val number: String, val name: String? = null) : CallState() // Incoming
    data class Active(
        val number: String,
        val name: String? = null,
        val durationSeconds: Long = 0,
        val isMuted: Boolean = false,
        val isSpeakerOn: Boolean = false,
        val isOnHold: Boolean = false,
        val notes: String = ""
    ) : CallState()
    data class Disconnected(val reason: String = "Call ended") : CallState()
}

enum class AudioRoute {
    EARPIECE,
    SPEAKER,
    BLUETOOTH
}
