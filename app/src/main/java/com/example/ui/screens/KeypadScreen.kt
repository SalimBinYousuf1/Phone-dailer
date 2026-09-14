package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ContactEntity
import com.example.ui.components.VercelAvatar
import com.example.ui.components.VercelBadge
import com.example.ui.components.VercelTopBar
import com.example.ui.theme.LocalVercelColors
import com.example.ui.theme.VercelGreen
import com.example.ui.theme.VercelMono
import com.example.ui.theme.VercelSpacing
import com.example.ui.viewmodel.DialerViewModel

@Composable
fun KeypadScreen(
    viewModel: DialerViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToBlocked: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onAddContact: (String) -> Unit,
    onContactClick: (ContactEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val clipboardManager = LocalClipboardManager.current
    val vercelColors = LocalVercelColors.current

    // USSD Alert Dialog
    if (uiState.ussdResponse != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissUssd() },
            title = {
                Text(
                    text = "USSD / CARRIER RESPONSE",
                    style = VercelMono.Badge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = uiState.ussdResponse ?: "",
                    style = VercelMono.PhoneNumber,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissUssd() }) {
                    Text("DISMISS", style = VercelMono.Badge)
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
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header bar
            VercelTopBar(
                title = "DIALER",
                subtitle = "READY",
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onNavigateToBlocked) {
                        Icon(
                            imageVector = Icons.Outlined.Shield,
                            contentDescription = "Blocked Numbers",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )

            // Number display and T9 suggestion
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = VercelSpacing.Space24),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // T9 matched contact preview
                AnimatedVisibility(
                    visible = uiState.matchedContact != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    uiState.matchedContact?.let { contact ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(vercelColors.surfaceSubtle)
                                .border(1.dp, vercelColors.border, RoundedCornerShape(8.dp))
                                .clickable { onContactClick(contact) }
                                .padding(horizontal = VercelSpacing.Space12, vertical = VercelSpacing.Space8),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            VercelAvatar(name = contact.name, size = 28.dp)
                            Spacer(modifier = Modifier.width(VercelSpacing.Space8))
                            Column {
                                Text(
                                    text = contact.name,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = contact.phone,
                                    style = VercelMono.Badge,
                                    color = vercelColors.textMuted
                                )
                            }
                            Spacer(modifier = Modifier.width(VercelSpacing.Space8))
                            VercelBadge(text = "T9 MATCH")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(VercelSpacing.Space12))

                // Typed number display
                Text(
                    text = if (uiState.enteredNumber.isEmpty()) "ENTER NUMBER" else uiState.formattedNumber,
                    style = VercelMono.DialpadDisplay,
                    color = if (uiState.enteredNumber.isEmpty()) vercelColors.textMuted else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialer_display")
                )

                // Quick action strip when number is typed
                AnimatedVisibility(visible = uiState.enteredNumber.isNotEmpty()) {
                    Row(
                        modifier = Modifier.padding(top = VercelSpacing.Space12),
                        horizontalArrangement = Arrangement.spacedBy(VercelSpacing.Space8)
                    ) {
                        IconButton(
                            onClick = { onAddContact(uiState.enteredNumber) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Contact",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(uiState.enteredNumber))
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.blockCurrentNumber() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = "Block",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Dual SIM selector
                Row(
                    modifier = Modifier.padding(top = VercelSpacing.Space8),
                    horizontalArrangement = Arrangement.spacedBy(VercelSpacing.Space8)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (uiState.selectedSim == 0) MaterialTheme.colorScheme.primary else vercelColors.badgeBg)
                            .clickable { viewModel.setSim(0) }
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SIM 1",
                            style = VercelMono.Badge,
                            color = if (uiState.selectedSim == 0) MaterialTheme.colorScheme.onPrimary else vercelColors.monoText
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (uiState.selectedSim == 1) MaterialTheme.colorScheme.primary else vercelColors.badgeBg)
                            .clickable { viewModel.setSim(1) }
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SIM 2",
                            style = VercelMono.Badge,
                            color = if (uiState.selectedSim == 1) MaterialTheme.colorScheme.onPrimary else vercelColors.monoText
                        )
                    }
                }
            }

            // Keypad Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = VercelSpacing.Space32, vertical = VercelSpacing.Space8),
                verticalArrangement = Arrangement.spacedBy(VercelSpacing.Space12)
            ) {
                KeypadRow(
                    keys = listOf(
                        KeypadKey("1", "VOICEMAIL", 1),
                        KeypadKey("2", "ABC", 2),
                        KeypadKey("3", "DEF", 3)
                    ),
                    onPress = { digit ->
                        if (uiState.hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.onDigitPress(digit)
                    },
                    onLongPress = { digit ->
                        if (uiState.hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val num = digit.digitToIntOrNull()
                        if (num != null) viewModel.onSpeedDial(num, context)
                    }
                )

                KeypadRow(
                    keys = listOf(
                        KeypadKey("4", "GHI", 4),
                        KeypadKey("5", "JKL", 5),
                        KeypadKey("6", "MNO", 6)
                    ),
                    onPress = { digit ->
                        if (uiState.hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.onDigitPress(digit)
                    },
                    onLongPress = { digit ->
                        if (uiState.hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val num = digit.digitToIntOrNull()
                        if (num != null) viewModel.onSpeedDial(num, context)
                    }
                )

                KeypadRow(
                    keys = listOf(
                        KeypadKey("7", "PQRS", 7),
                        KeypadKey("8", "TUV", 8),
                        KeypadKey("9", "WXYZ", 9)
                    ),
                    onPress = { digit ->
                        if (uiState.hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.onDigitPress(digit)
                    },
                    onLongPress = { digit ->
                        if (uiState.hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val num = digit.digitToIntOrNull()
                        if (num != null) viewModel.onSpeedDial(num, context)
                    }
                )

                KeypadRow(
                    keys = listOf(
                        KeypadKey("*", "", null),
                        KeypadKey("0", "+", 0),
                        KeypadKey("#", "", null)
                    ),
                    onPress = { digit ->
                        if (uiState.hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.onDigitPress(digit)
                    },
                    onLongPress = { digit ->
                        if (digit == '0') {
                            if (uiState.hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.onZeroLongPress()
                        }
                    }
                )
            }

            // Bottom call and backspace actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = VercelSpacing.Space32, vertical = VercelSpacing.Space16),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Paste button or empty placeholder
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(vercelColors.surfaceSubtle)
                        .border(1.dp, vercelColors.border, CircleShape)
                        .clickable {
                            val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val text = clip.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                            if (text.isNotBlank()) viewModel.onPasteNumber(text)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Paste",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Call Action Button
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .testTag("dialer_call_button")
                        .clickable {
                            if (uiState.hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.placeCall(context)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Place Call",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Backspace (tap: delete single, long: clear all)
                OptInCombinedClickableBox(
                    enabled = uiState.enteredNumber.isNotEmpty(),
                    onClick = {
                        if (uiState.hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.onBackspace()
                    },
                    onLongClick = {
                        if (uiState.hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.onClearAll()
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (uiState.enteredNumber.isNotEmpty()) vercelColors.surfaceSubtle else Color.Transparent)
                        .border(
                            1.dp,
                            if (uiState.enteredNumber.isNotEmpty()) vercelColors.border else Color.Transparent,
                            CircleShape
                        )
                        .testTag("dialer_backspace_button")
                ) {
                    if (uiState.enteredNumber.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Backspace",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

data class KeypadKey(val digit: String, val letters: String, val speedDialIndex: Int?)

@Composable
private fun KeypadRow(
    keys: List<KeypadKey>,
    onPress: (Char) -> Unit,
    onLongPress: (Char) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for (k in keys) {
            KeypadButton(
                digit = k.digit,
                letters = k.letters,
                onPress = { onPress(k.digit[0]) },
                onLongPress = { onLongPress(k.digit[0]) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun KeypadButton(
    digit: String,
    letters: String,
    onPress: () -> Unit,
    onLongPress: () -> Unit
) {
    val vercelColors = LocalVercelColors.current

    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(vercelColors.surfaceSubtle)
            .border(1.dp, vercelColors.border, RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onPress,
                onLongClick = onLongPress
            )
            .testTag("keypad_digit_$digit"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = digit,
                style = VercelMono.DialpadNumber,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (letters.isNotBlank()) {
                Text(
                    text = letters,
                    style = VercelMono.Badge.copy(fontSize = 9.sp),
                    color = vercelColors.textMuted
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OptInCombinedClickableBox(
    enabled: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.combinedClickable(
            enabled = enabled,
            onClick = onClick,
            onLongClick = onLongClick
        ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
