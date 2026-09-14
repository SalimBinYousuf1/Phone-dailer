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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CallLogEntity
import com.example.ui.components.VercelAvatar
import com.example.ui.components.VercelBadge
import com.example.ui.components.VercelButton
import com.example.ui.components.VercelButtonVariant
import com.example.ui.components.VercelTopBar
import com.example.ui.theme.LocalVercelColors
import com.example.ui.theme.VercelGreen
import com.example.ui.theme.VercelMono
import com.example.ui.theme.VercelRed
import com.example.ui.theme.VercelSpacing
import com.example.ui.viewmodel.CallFilter
import com.example.ui.viewmodel.RecentsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecentsScreen(
    viewModel: RecentsViewModel,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val vercelColors = LocalVercelColors.current
    var showClearDialog by remember { mutableStateOf(false) }

    // Clear All Confirmation
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text(
                    text = "CLEAR CALL HISTORY",
                    style = VercelMono.Badge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to permanently clear all call records?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAll()
                        showClearDialog = false
                    }
                ) {
                    Text("CLEAR ALL", color = VercelRed, style = VercelMono.Badge)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("CANCEL", style = VercelMono.Badge)
                }
            },
            shape = RoundedCornerShape(8.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.border(1.dp, vercelColors.border, RoundedCornerShape(8.dp))
        )
    }

    // Call Detail Modal
    if (uiState.activeDetailCall != null) {
        val call = uiState.activeDetailCall!!
        AlertDialog(
            onDismissRequest = { viewModel.closeDetail() },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "CALL DETAILS",
                        style = VercelMono.Badge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    VercelBadge(
                        text = when (call.callType) {
                            CallLogEntity.TYPE_INCOMING -> "INCOMING"
                            CallLogEntity.TYPE_OUTGOING -> "OUTGOING"
                            CallLogEntity.TYPE_MISSED -> "MISSED"
                            CallLogEntity.TYPE_BLOCKED -> "BLOCKED"
                            else -> "OTHER"
                        },
                        color = if (call.callType == CallLogEntity.TYPE_MISSED || call.callType == CallLogEntity.TYPE_BLOCKED) VercelRed else null
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(VercelSpacing.Space12)) {
                    Text(
                        text = call.name ?: "Unknown Caller",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = call.number,
                        style = VercelMono.PhoneNumber,
                        color = vercelColors.textMuted
                    )

                    val dateFormatted = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()).format(Date(call.timestamp))
                    Text(
                        text = "TIME: $dateFormatted",
                        style = VercelMono.Timestamp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val mins = call.durationSeconds / 60
                    val secs = call.durationSeconds % 60
                    Text(
                        text = "DURATION: ${String.format("%02d:%02d", mins, secs)}",
                        style = VercelMono.Duration,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(VercelSpacing.Space8))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(VercelSpacing.Space8)
                    ) {
                        VercelButton(
                            text = "CALL",
                            icon = Icons.Default.Call,
                            onClick = {
                                viewModel.callContact(context, call.number, call.name)
                                viewModel.closeDetail()
                            },
                            modifier = Modifier.weight(1f)
                        )
                        if (call.name == null) {
                            VercelButton(
                                text = "ADD",
                                icon = Icons.Default.PersonAdd,
                                variant = VercelButtonVariant.SECONDARY,
                                onClick = {
                                    viewModel.addAsContact(call.number)
                                    viewModel.closeDetail()
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    VercelButton(
                        text = "BLOCK NUMBER",
                        icon = Icons.Default.Block,
                        variant = VercelButtonVariant.DANGER,
                        onClick = {
                            viewModel.blockNumber(call.number, call.name)
                            viewModel.closeDetail()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.closeDetail() }) {
                    Text("CLOSE", style = VercelMono.Badge)
                }
            },
            shape = RoundedCornerShape(8.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.border(1.dp, vercelColors.border, RoundedCornerShape(8.dp))
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Top Bar
            VercelTopBar(
                title = "RECENTS",
                subtitle = "${uiState.totalCount} CALLS",
                actions = {
                    if (uiState.isSelectionMode) {
                        IconButton(onClick = { viewModel.deleteSelected() }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Selected",
                                tint = VercelRed
                            )
                        }
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Selection",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        IconButton(onClick = onNavigateToSearch) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear All",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )

            // Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = VercelSpacing.Space16, vertical = VercelSpacing.Space8),
                horizontalArrangement = Arrangement.spacedBy(VercelSpacing.Space8)
            ) {
                val filters = listOf(
                    CallFilter.ALL to "ALL",
                    CallFilter.MISSED to "MISSED",
                    CallFilter.INCOMING to "INCOMING",
                    CallFilter.OUTGOING to "OUTGOING",
                    CallFilter.BLOCKED to "BLOCKED"
                )

                items(filters) { (filter, label) ->
                    val isSelected = uiState.selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else vercelColors.surfaceSubtle)
                            .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else vercelColors.border, RoundedCornerShape(6.dp))
                            .clickable { viewModel.setFilter(filter) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            style = VercelMono.Badge,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else vercelColors.monoText
                        )
                    }
                }
            }

            // Call Logs Grouped List
            if (uiState.groups.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(VercelSpacing.Space32),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "NO CALL LOGS",
                            style = VercelMono.Badge,
                            color = vercelColors.textMuted
                        )
                        Spacer(modifier = Modifier.height(VercelSpacing.Space8))
                        Text(
                            text = "Your recent incoming and outgoing calls will appear here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = vercelColors.textMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    for (group in uiState.groups) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(vercelColors.surfaceSubtle)
                                    .border(1.dp, vercelColors.border)
                                    .padding(horizontal = VercelSpacing.Space16, vertical = VercelSpacing.Space4)
                            ) {
                                Text(
                                    text = group.header,
                                    style = VercelMono.Badge,
                                    color = vercelColors.textMuted
                                )
                            }
                        }

                        items(group.items, key = { it.id }) { call ->
                            CallLogItem(
                                call = call,
                                isSelectionMode = uiState.isSelectionMode,
                                isSelected = uiState.selectedIds.contains(call.id),
                                onToggleSelect = { viewModel.toggleSelection(call.id) },
                                onClick = {
                                    if (uiState.isSelectionMode) {
                                        viewModel.toggleSelection(call.id)
                                    } else {
                                        viewModel.callContact(context, call.number, call.name)
                                    }
                                },
                                onInfoClick = { viewModel.openDetail(call) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CallLogItem(
    call: CallLogEntity,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    val vercelColors = LocalVercelColors.current
    val isMissed = call.callType == CallLogEntity.TYPE_MISSED
    val isBlocked = call.callType == CallLogEntity.TYPE_BLOCKED

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = VercelSpacing.Space16, vertical = VercelSpacing.Space12),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelect() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    checkmarkColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.padding(end = VercelSpacing.Space8)
            )
        }

        // Call type icon
        val icon = when (call.callType) {
            CallLogEntity.TYPE_INCOMING -> Icons.AutoMirrored.Filled.CallReceived
            CallLogEntity.TYPE_OUTGOING -> Icons.AutoMirrored.Filled.CallMade
            CallLogEntity.TYPE_MISSED -> Icons.AutoMirrored.Filled.CallMissed
            CallLogEntity.TYPE_BLOCKED -> Icons.Default.Block
            else -> Icons.Default.Call
        }

        val iconTint = when {
            isMissed || isBlocked -> VercelRed
            call.callType == CallLogEntity.TYPE_INCOMING -> VercelGreen
            else -> MaterialTheme.colorScheme.onSurface
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(vercelColors.surfaceSubtle)
                .border(1.dp, vercelColors.border, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(VercelSpacing.Space12))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = call.name ?: call.number,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (isMissed) FontWeight.SemiBold else FontWeight.Medium
                ),
                color = if (isMissed) VercelRed else MaterialTheme.colorScheme.onSurface
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (call.name != null) {
                    Text(
                        text = call.number,
                        style = VercelMono.PhoneNumber.copy(fontSize = 12.sp),
                        color = vercelColors.textMuted
                    )
                    Spacer(modifier = Modifier.width(VercelSpacing.Space8))
                }
                Text(
                    text = "SIM ${call.simSlot + 1}",
                    style = VercelMono.Badge.copy(fontSize = 9.sp),
                    color = vercelColors.textMuted
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(call.timestamp))
            Text(
                text = timeFmt,
                style = VercelMono.Timestamp,
                color = vercelColors.textMuted
            )

            if (call.durationSeconds > 0) {
                val mins = call.durationSeconds / 60
                val secs = call.durationSeconds % 60
                Text(
                    text = String.format("%02d:%02d", mins, secs),
                    style = VercelMono.Badge.copy(fontSize = 10.sp),
                    color = vercelColors.monoText
                )
            }
        }

        IconButton(
            onClick = onInfoClick,
            modifier = Modifier.padding(start = VercelSpacing.Space8)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Details",
                tint = vercelColors.textMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
