package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.telecom.CallState
import com.example.ui.components.VercelAvatar
import com.example.ui.components.VercelBadge
import com.example.ui.theme.LocalVercelColors
import com.example.ui.theme.VercelGreen
import com.example.ui.theme.VercelMono
import com.example.ui.theme.VercelRed
import com.example.ui.theme.VercelSpacing
import com.example.ui.viewmodel.InCallViewModel

@Composable
fun InCallScreen(
    viewModel: InCallViewModel,
    modifier: Modifier = Modifier
) {
    val callState by viewModel.callState.collectAsState()
    val quickResponses by viewModel.quickResponses.collectAsState()
    val context = LocalContext.current
    val vercelColors = LocalVercelColors.current

    var showDtmfDialog by remember { mutableStateOf(false) }
    var showSmsDialog by remember { mutableStateOf(false) }
    var showNotesDialog by remember { mutableStateOf(false) }
    var inCallNotes by remember { mutableStateOf("") }

    if (callState is CallState.Idle) {
        return
    }

    // DTMF In-call Keypad Dialog
    if (showDtmfDialog) {
        AlertDialog(
            onDismissRequest = { showDtmfDialog = false },
            title = {
                Text(
                    text = "KEYPAD // DTMF",
                    style = VercelMono.Badge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(VercelSpacing.Space8),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val rows = listOf(
                        listOf('1', '2', '3'),
                        listOf('4', '5', '6'),
                        listOf('7', '8', '9'),
                        listOf('*', '0', '#')
                    )
                    for (row in rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (digit in row) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(vercelColors.surfaceSubtle)
                                        .border(1.dp, vercelColors.border, RoundedCornerShape(8.dp))
                                        .clickable { viewModel.playDtmf(digit) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = digit.toString(),
                                        style = VercelMono.DialpadNumber.copy(fontSize = 22.sp),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDtmfDialog = false }) {
                    Text("DONE", style = VercelMono.Badge)
                }
            },
            shape = RoundedCornerShape(8.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.border(1.dp, vercelColors.border, RoundedCornerShape(8.dp))
        )
    }

    // Quick SMS Reject Dialog
    if (showSmsDialog) {
        AlertDialog(
            onDismissRequest = { showSmsDialog = false },
            title = {
                Text(
                    text = "REPLY WITH MESSAGE",
                    style = VercelMono.Badge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(VercelSpacing.Space8)) {
                    for (response in quickResponses) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(vercelColors.surfaceSubtle)
                                .border(1.dp, vercelColors.border, RoundedCornerShape(6.dp))
                                .clickable {
                                    viewModel.sendQuickSms(context, response)
                                    showSmsDialog = false
                                }
                                .padding(VercelSpacing.Space12)
                        ) {
                            Text(
                                text = response,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSmsDialog = false }) {
                    Text("CANCEL", style = VercelMono.Badge)
                }
            },
            shape = RoundedCornerShape(8.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.border(1.dp, vercelColors.border, RoundedCornerShape(8.dp))
        )
    }

    // In-Call Scratchpad / Notes Dialog
    if (showNotesDialog) {
        AlertDialog(
            onDismissRequest = { showNotesDialog = false },
            title = {
                Text(
                    text = "IN-CALL SCRATCHPAD",
                    style = VercelMono.Badge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                OutlinedTextField(
                    value = inCallNotes,
                    onValueChange = {
                        inCallNotes = it
                        viewModel.updateNotes(it)
                    },
                    placeholder = { Text("Jot quick notes during call...", color = vercelColors.textMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = vercelColors.border
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = { showNotesDialog = false }) {
                    Text("SAVE", style = VercelMono.Badge)
                }
            },
            shape = RoundedCornerShape(8.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.border(1.dp, vercelColors.border, RoundedCornerShape(8.dp))
        )
    }

    val callerName: String?
    val callerNumber: String
    val statusText: String
    val isIncoming: Boolean
    val isActive: Boolean

    when (val state = callState) {
        is CallState.Outgoing -> {
            callerName = state.name
            callerNumber = state.number
            statusText = "CALLING..."
            isIncoming = false
            isActive = false
        }
        is CallState.Ringing -> {
            callerName = state.name
            callerNumber = state.number
            statusText = "INCOMING CALL..."
            isIncoming = true
            isActive = false
        }
        is CallState.Active -> {
            callerName = state.name
            callerNumber = state.number
            val mins = state.durationSeconds / 60
            val secs = state.durationSeconds % 60
            statusText = if (state.isOnHold) "ON HOLD" else String.format("%02d:%02d", mins, secs)
            isIncoming = false
            isActive = true
        }
        is CallState.Disconnected -> {
            callerName = null
            callerNumber = ""
            statusText = state.reason.uppercase()
            isIncoming = false
            isActive = false
        }
        else -> return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(VercelSpacing.Space24)
            .testTag("in_call_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header / Status
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                VercelBadge(
                    text = "NATIVE TELECOM SESSION",
                    borderColor = if (isActive) VercelGreen else null
                )
                Spacer(modifier = Modifier.height(VercelSpacing.Space16))
                Text(
                    text = statusText,
                    style = VercelMono.Duration,
                    color = if (statusText == "ON HOLD") vercelColors.textMuted else MaterialTheme.colorScheme.onSurface
                )
            }

            // Caller Info & Minimalist Avatar
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                VercelAvatar(
                    name = callerName ?: callerNumber,
                    size = 96.dp
                )

                Spacer(modifier = Modifier.height(VercelSpacing.Space20))

                Text(
                    text = callerName ?: callerNumber,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (callerName != null) {
                    Spacer(modifier = Modifier.height(VercelSpacing.Space4))
                    Text(
                        text = callerNumber,
                        style = VercelMono.PhoneNumber,
                        color = vercelColors.textMuted
                    )
                }

                Spacer(modifier = Modifier.height(VercelSpacing.Space8))
                VercelBadge(text = "HD VOICE // SIM 1")
            }

            // Controls Block
            if (isIncoming) {
                // Incoming Call controls: Decline with SMS, Decline, Accept
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(VercelSpacing.Space16)
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(vercelColors.surfaceSubtle)
                            .border(1.dp, vercelColors.border, RoundedCornerShape(8.dp))
                            .clickable { showSmsDialog = true }
                            .padding(horizontal = VercelSpacing.Space16, vertical = VercelSpacing.Space8),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sms,
                            contentDescription = "Decline with SMS",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(VercelSpacing.Space8))
                        Text(
                            text = "DECLINE WITH MESSAGE",
                            style = VercelMono.Badge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Decline Call
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(VercelRed)
                                .clickable { viewModel.rejectCall() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = "Decline",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // Accept Call
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(VercelGreen)
                                .clickable { viewModel.answerCall() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Accept",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            } else {
                // Active / Outgoing Call Controls Grid
                val activeState = callState as? CallState.Active
                val isMuted = activeState?.isMuted ?: false
                val isSpeakerOn = activeState?.isSpeakerOn ?: false
                val isOnHold = activeState?.isOnHold ?: false

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(VercelSpacing.Space24),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Control Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        InCallIconButton(
                            icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            label = if (isMuted) "MUTED" else "MUTE",
                            isActive = isMuted,
                            onClick = { viewModel.toggleMute() }
                        )

                        InCallIconButton(
                            icon = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                            label = if (isSpeakerOn) "SPEAKER" else "EARPIECE",
                            isActive = isSpeakerOn,
                            onClick = { viewModel.toggleSpeaker(context) }
                        )

                        InCallIconButton(
                            icon = Icons.Default.Dialpad,
                            label = "KEYPAD",
                            isActive = false,
                            onClick = { showDtmfDialog = true }
                        )

                        InCallIconButton(
                            icon = if (isOnHold) Icons.Default.PlayArrow else Icons.Default.Pause,
                            label = if (isOnHold) "RESUME" else "HOLD",
                            isActive = isOnHold,
                            onClick = { viewModel.toggleHold() }
                        )

                        InCallIconButton(
                            icon = Icons.Default.NoteAdd,
                            label = "NOTES",
                            isActive = inCallNotes.isNotBlank(),
                            onClick = { showNotesDialog = true }
                        )
                    }

                    // Large End Call Button
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(VercelRed)
                            .testTag("in_call_end_button")
                            .clickable { viewModel.disconnectCall() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InCallIconButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val vercelColors = LocalVercelColors.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(VercelSpacing.Space4)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(if (isActive) MaterialTheme.colorScheme.primary else vercelColors.surfaceSubtle)
                .border(
                    1.dp,
                    if (isActive) MaterialTheme.colorScheme.primary else vercelColors.border,
                    CircleShape
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = label,
            style = VercelMono.Badge.copy(fontSize = 9.sp),
            color = vercelColors.textMuted
        )
    }
}
