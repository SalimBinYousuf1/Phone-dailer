package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.telecom.TelecomHelper
import com.example.ui.components.VercelBadge
import com.example.ui.components.VercelButton
import com.example.ui.components.VercelButtonVariant
import com.example.ui.components.VercelTopBar
import com.example.ui.theme.LocalVercelColors
import com.example.ui.theme.VercelGreen
import com.example.ui.theme.VercelMono
import com.example.ui.theme.VercelRed
import com.example.ui.theme.VercelSpacing
import com.example.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val vercelColors = LocalVercelColors.current

    val roleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.checkDefaultDialer(context)
    }

    LaunchedEffect(Unit) {
        viewModel.checkDefaultDialer(context)
    }

    if (uiState.exportSuccessMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearExportMessage() },
            title = {
                Text(
                    text = "EXPORT SUCCESSFUL",
                    style = VercelMono.Badge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = uiState.exportSuccessMessage ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.clearExportMessage() }) {
                    Text("OK", style = VercelMono.Badge)
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
                title = "SETTINGS",
                subtitle = "CONFIG",
                onBackClick = onBack
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(VercelSpacing.Space24),
                verticalArrangement = Arrangement.spacedBy(VercelSpacing.Space20)
            ) {
                // Section 1: Default Dialer Role
                SettingsCard(title = "SYSTEM DEFAULT DIALER") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (uiState.isDefaultDialer) "STATUS: ACTIVE" else "STATUS: STANDBY",
                                style = VercelMono.Badge,
                                color = if (uiState.isDefaultDialer) VercelGreen else vercelColors.textMuted
                            )
                            Text(
                                text = "Handle all incoming/outgoing system calls",
                                style = MaterialTheme.typography.bodySmall,
                                color = vercelColors.textMuted
                            )
                        }

                        if (!uiState.isDefaultDialer) {
                            VercelButton(
                                text = "SET DEFAULT",
                                icon = Icons.Default.Phone,
                                onClick = {
                                    val intent = TelecomHelper.createDefaultDialerIntent(context)
                                    if (intent != null) {
                                        roleLauncher.launch(intent)
                                    }
                                }
                            )
                        }
                    }
                }

                // Section 2: Theme Selection
                SettingsCard(title = "INTERFACE THEME") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(VercelSpacing.Space8)
                    ) {
                        listOf("system" to "SYSTEM", "light" to "LIGHT", "dark" to "DARK").forEach { (mode, label) ->
                            val isSelected = uiState.themeMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else vercelColors.badgeBg)
                                    .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else vercelColors.border, RoundedCornerShape(6.dp))
                                    .clickable { viewModel.setThemeMode(mode) }
                                    .padding(vertical = VercelSpacing.Space8),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = VercelMono.Badge,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else vercelColors.monoText
                                )
                            }
                        }
                    }
                }

                // Section 3: Dual SIM default
                SettingsCard(title = "DEFAULT CELLULAR SIM") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(VercelSpacing.Space8)
                    ) {
                        listOf(0 to "SIM 1 (PRIMARY)", 1 to "SIM 2 (SECONDARY)").forEach { (sim, label) ->
                            val isSelected = uiState.defaultSim == sim
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else vercelColors.badgeBg)
                                    .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else vercelColors.border, RoundedCornerShape(6.dp))
                                    .clickable { viewModel.setDefaultSim(sim) }
                                    .padding(vertical = VercelSpacing.Space8),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = VercelMono.Badge,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else vercelColors.monoText
                                )
                            }
                        }
                    }
                }

                // Section 4: Sound & Feedback Toggles
                SettingsCard(title = "AUDIO & FEEDBACK") {
                    Column(verticalArrangement = Arrangement.spacedBy(VercelSpacing.Space12)) {
                        ToggleRow(
                            title = "Haptic Feedback",
                            subtitle = "Subtle vibration on keypad taps and connection",
                            checked = uiState.hapticsEnabled,
                            onCheckedChange = { viewModel.toggleHaptics() }
                        )

                        ToggleRow(
                            title = "Dialpad DTMF Audio",
                            subtitle = "Tone frequencies when dialing keypad numbers",
                            checked = uiState.dialpadTonesEnabled,
                            onCheckedChange = { viewModel.toggleDialpadTones() }
                        )

                        ToggleRow(
                            title = "Silence Unknown Callers",
                            subtitle = "Direct unknown phone numbers directly to voicemail",
                            checked = uiState.silenceUnknown,
                            onCheckedChange = { viewModel.toggleSilenceUnknown() }
                        )
                    }
                }

                // Section 5: Data & Export
                SettingsCard(title = "DATA & BACKUP") {
                    Column(verticalArrangement = Arrangement.spacedBy(VercelSpacing.Space12)) {
                        VercelButton(
                            text = "EXPORT CALL LOG (CSV)",
                            icon = Icons.Default.FileDownload,
                            variant = VercelButtonVariant.SECONDARY,
                            onClick = {
                                viewModel.exportCallLogCsv { csv ->
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, csv)
                                        type = "text/csv"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Export Call Log CSV"))
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        VercelButton(
                            text = "BACKUP CONTACTS (VCARD)",
                            icon = Icons.Default.FileDownload,
                            variant = VercelButtonVariant.SECONDARY,
                            onClick = {
                                viewModel.exportContactsVCard { vcard ->
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, vcard)
                                        type = "text/x-vcard"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Export Contacts vCard"))
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        VercelButton(
                            text = "CLEAR ALL CALL HISTORY",
                            icon = Icons.Default.Delete,
                            variant = VercelButtonVariant.DANGER,
                            onClick = { viewModel.clearAllLogs() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Section 6: Build & Engine Metadata
                SettingsCard(title = "ENGINE METADATA") {
                    Column(verticalArrangement = Arrangement.spacedBy(VercelSpacing.Space4)) {
                        Text(
                            text = "NEXT_TELECOM_RUNTIME: v1.0.0-RELEASE",
                            style = VercelMono.Badge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "TELECOM_INCALL_SERVICE: BINDABLE",
                            style = VercelMono.Badge,
                            color = vercelColors.textMuted
                        )
                        Text(
                            text = "SCREENING_SERVICE: ACTIVE",
                            style = VercelMono.Badge,
                            color = vercelColors.textMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    val vercelColors = LocalVercelColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(vercelColors.surfaceSubtle, RoundedCornerShape(8.dp))
            .border(1.dp, vercelColors.border, RoundedCornerShape(8.dp))
            .padding(VercelSpacing.Space16)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(VercelSpacing.Space12)) {
            Text(
                text = title,
                style = VercelMono.Badge,
                color = vercelColors.textMuted
            )
            content()
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val vercelColors = LocalVercelColors.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = vercelColors.textMuted
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
