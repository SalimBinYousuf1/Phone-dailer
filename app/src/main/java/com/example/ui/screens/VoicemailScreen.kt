package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Voicemail
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.VoicemailEntity
import com.example.ui.components.VercelAvatar
import com.example.ui.components.VercelBadge
import com.example.ui.components.VercelTopBar
import com.example.ui.theme.LocalVercelColors
import com.example.ui.theme.VercelMono
import com.example.ui.theme.VercelRed
import com.example.ui.theme.VercelSpacing
import com.example.ui.viewmodel.VoicemailViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VoicemailScreen(
    viewModel: VoicemailViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val vercelColors = LocalVercelColors.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            VercelTopBar(
                title = "VOICEMAIL",
                subtitle = "${uiState.voicemails.size} MESSAGES",
                actions = {
                    IconButton(onClick = { viewModel.callVoicemailBox(context) }) {
                        Icon(
                            imageVector = Icons.Default.Voicemail,
                            contentDescription = "Call Voicemail (*86)",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )

            if (uiState.voicemails.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(VercelSpacing.Space32),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "NO VOICEMAILS",
                            style = VercelMono.Badge,
                            color = vercelColors.textMuted
                        )
                        Spacer(modifier = Modifier.height(VercelSpacing.Space8))
                        Text(
                            text = "Carrier voicemail messages and audio recordings will show up here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = vercelColors.textMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(VercelSpacing.Space16),
                    verticalArrangement = Arrangement.spacedBy(VercelSpacing.Space12)
                ) {
                    items(uiState.voicemails, key = { it.id }) { voicemail ->
                        val isPlaying = uiState.activePlayingId == voicemail.id && uiState.isPlaying
                        val progress = if (uiState.activePlayingId == voicemail.id) uiState.playbackProgress else 0f
                        val currentSeconds = if (uiState.activePlayingId == voicemail.id) uiState.currentPositionSeconds else 0L

                        VoicemailCard(
                            voicemail = voicemail,
                            isPlaying = isPlaying,
                            progress = progress,
                            currentSeconds = currentSeconds,
                            onPlayPause = { viewModel.togglePlay(voicemail) },
                            onDelete = { viewModel.delete(voicemail.id) },
                            onCallBack = { viewModel.callBack(context, voicemail.number, voicemail.name) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VoicemailCard(
    voicemail: VoicemailEntity,
    isPlaying: Boolean,
    progress: Float,
    currentSeconds: Long,
    onPlayPause: () -> Unit,
    onDelete: () -> Unit,
    onCallBack: () -> Unit
) {
    val vercelColors = LocalVercelColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(vercelColors.surfaceSubtle)
            .border(1.dp, vercelColors.border, RoundedCornerShape(8.dp))
            .padding(VercelSpacing.Space16)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(VercelSpacing.Space8)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    VercelAvatar(name = voicemail.name ?: voicemail.number, size = 32.dp)
                    Spacer(modifier = Modifier.width(VercelSpacing.Space8))
                    Column {
                        Text(
                            text = voicemail.name ?: voicemail.number,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val dateStr = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(voicemail.timestamp))
                        Text(
                            text = dateStr,
                            style = VercelMono.Timestamp,
                            color = vercelColors.textMuted
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onCallBack) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = VercelRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Audio playback controller
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = VercelSpacing.Space4),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(onClick = onPlayPause),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(VercelSpacing.Space12))

                Column(modifier = Modifier.weight(1f)) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = vercelColors.border
                    )
                    Spacer(modifier = Modifier.height(VercelSpacing.Space4))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = String.format("%02d:%02d", currentSeconds / 60, currentSeconds % 60),
                            style = VercelMono.Badge.copy(fontSize = 10.sp),
                            color = vercelColors.monoText
                        )
                        Text(
                            text = String.format("%02d:%02d", voicemail.durationSeconds / 60, voicemail.durationSeconds % 60),
                            style = VercelMono.Badge.copy(fontSize = 10.sp),
                            color = vercelColors.textMuted
                        )
                    }
                }
            }

            // Transcript text
            if (voicemail.transcript.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(4.dp))
                        .border(1.dp, vercelColors.border, RoundedCornerShape(4.dp))
                        .padding(VercelSpacing.Space8)
                ) {
                    Column {
                        Text(
                            text = "TRANSCRIPT",
                            style = VercelMono.Badge.copy(fontSize = 9.sp),
                            color = vercelColors.textMuted
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = voicemail.transcript,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
