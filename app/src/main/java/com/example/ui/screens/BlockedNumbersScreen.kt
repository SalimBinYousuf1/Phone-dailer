package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BlockedNumberEntity
import com.example.ui.components.VercelBadge
import com.example.ui.components.VercelButton
import com.example.ui.components.VercelTopBar
import com.example.ui.theme.LocalVercelColors
import com.example.ui.theme.VercelMono
import com.example.ui.theme.VercelRed
import com.example.ui.theme.VercelSpacing
import com.example.ui.viewmodel.BlockedNumbersViewModel

@Composable
fun BlockedNumbersScreen(
    viewModel: BlockedNumbersViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val vercelColors = LocalVercelColors.current
    var numberToBlock by remember { mutableStateOf("") }
    var nameToBlock by remember { mutableStateOf("") }

    if (uiState.showAddDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeAddDialog() },
            title = {
                Text(
                    text = "BLOCK NUMBER",
                    style = VercelMono.Badge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(VercelSpacing.Space12)) {
                    OutlinedTextField(
                        value = numberToBlock,
                        onValueChange = { numberToBlock = it },
                        placeholder = { Text("Phone number", color = vercelColors.textMuted) },
                        singleLine = true,
                        textStyle = VercelMono.PhoneNumber,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = vercelColors.border
                        )
                    )
                    OutlinedTextField(
                        value = nameToBlock,
                        onValueChange = { nameToBlock = it },
                        placeholder = { Text("Label (e.g. Telemarketer)", color = vercelColors.textMuted) },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = vercelColors.border
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.blockNumber(numberToBlock, nameToBlock.ifBlank { null })
                        numberToBlock = ""
                        nameToBlock = ""
                    }
                ) {
                    Text("BLOCK", color = VercelRed, style = VercelMono.Badge)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeAddDialog() }) {
                    Text("CANCEL", style = VercelMono.Badge)
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
            VercelTopBar(
                title = "BLOCKED CALLERS",
                subtitle = "${uiState.blockedNumbers.size} ACTIVE",
                onBackClick = onBack,
                actions = {
                    IconButton(onClick = { viewModel.openAddDialog() }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Blocked Number",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )

            // Block Unknown Callers switch
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(VercelSpacing.Space16)
                    .background(vercelColors.surfaceSubtle, RoundedCornerShape(8.dp))
                    .border(1.dp, vercelColors.border, RoundedCornerShape(8.dp))
                    .padding(VercelSpacing.Space16)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SILENCE UNKNOWN CALLERS",
                            style = VercelMono.Badge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Calls from private/hidden numbers will be silenced",
                            style = MaterialTheme.typography.bodySmall,
                            color = vercelColors.textMuted
                        )
                    }

                    Switch(
                        checked = uiState.blockUnknownCallers,
                        onCheckedChange = { viewModel.toggleBlockUnknown() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            if (uiState.blockedNumbers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(VercelSpacing.Space32),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "NO BLOCKED NUMBERS",
                            style = VercelMono.Badge,
                            color = vercelColors.textMuted
                        )
                        Spacer(modifier = Modifier.height(VercelSpacing.Space8))
                        Text(
                            text = "Calls and SMS from blocked numbers will be intercepted automatically.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = vercelColors.textMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(VercelSpacing.Space16),
                    verticalArrangement = Arrangement.spacedBy(VercelSpacing.Space8)
                ) {
                    items(uiState.blockedNumbers, key = { it.id }) { blocked ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(vercelColors.surfaceSubtle)
                                .border(1.dp, vercelColors.border, RoundedCornerShape(8.dp))
                                .padding(horizontal = VercelSpacing.Space16, vertical = VercelSpacing.Space12),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = blocked.name ?: "Blocked Number",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = blocked.number,
                                    style = VercelMono.PhoneNumber.copy(fontSize = 12.sp),
                                    color = vercelColors.textMuted
                                )
                            }

                            IconButton(onClick = { viewModel.unblock(blocked.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Unblock",
                                    tint = vercelColors.textMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
