package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.ContactEntity
import com.example.ui.components.VercelButton
import com.example.ui.components.VercelButtonVariant
import com.example.ui.components.VercelTopBar
import com.example.ui.theme.LocalVercelColors
import com.example.ui.theme.VercelMono
import com.example.ui.theme.VercelSpacing

@Composable
fun ContactEditScreen(
    contact: ContactEntity?,
    initialPhone: String = "",
    onCancel: () -> Unit,
    onSave: (name: String, phone: String, label: String, email: String, company: String, notes: String, isFav: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val vercelColors = LocalVercelColors.current
    var name by remember { mutableStateOf(contact?.name ?: "") }
    var phone by remember { mutableStateOf(contact?.phone ?: initialPhone) }
    var phoneLabel by remember { mutableStateOf(contact?.phoneLabel ?: "Mobile") }
    var email by remember { mutableStateOf(contact?.email ?: "") }
    var company by remember { mutableStateOf(contact?.company ?: "") }
    var notes by remember { mutableStateOf(contact?.notes ?: "") }
    var isFavorite by remember { mutableStateOf(contact?.isFavorite ?: false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            VercelTopBar(
                title = if (contact == null || contact.id == 0L) "NEW CONTACT" else "EDIT CONTACT",
                subtitle = "CONFIG",
                onBackClick = onCancel,
                actions = {
                    IconButton(
                        onClick = {
                            if (name.isNotBlank() || phone.isNotBlank()) {
                                onSave(name, phone, phoneLabel, email, company, notes, isFavorite)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(VercelSpacing.Space24),
                verticalArrangement = Arrangement.spacedBy(VercelSpacing.Space16)
            ) {
                VercelInput(
                    label = "FULL NAME",
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Alex Vance"
                )

                VercelInput(
                    label = "PHONE NUMBER",
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = "+1 555-0192",
                    isMono = true
                )

                VercelInput(
                    label = "PHONE LABEL",
                    value = phoneLabel,
                    onValueChange = { phoneLabel = it },
                    placeholder = "Mobile, Work, Home..."
                )

                VercelInput(
                    label = "EMAIL",
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "alex@vercel.com"
                )

                VercelInput(
                    label = "COMPANY / ORGANIZATION",
                    value = company,
                    onValueChange = { company = it },
                    placeholder = "Vercel Inc."
                )

                VercelInput(
                    label = "NOTES",
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = "Infrastructure sync notes...",
                    singleLine = false
                )

                // Favorite switch
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
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
                                text = "STAR AS FAVORITE",
                                style = VercelMono.Badge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Pinned to favorites and speed dial list",
                                style = MaterialTheme.typography.bodySmall,
                                color = vercelColors.textMuted
                            )
                        }

                        Switch(
                            checked = isFavorite,
                            onCheckedChange = { isFavorite = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(VercelSpacing.Space16))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(VercelSpacing.Space12)
                ) {
                    VercelButton(
                        text = "CANCEL",
                        variant = VercelButtonVariant.SECONDARY,
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    )
                    VercelButton(
                        text = "SAVE CONTACT",
                        onClick = {
                            if (name.isNotBlank() || phone.isNotBlank()) {
                                onSave(name, phone, phoneLabel, email, company, notes, isFavorite)
                            }
                        },
                        enabled = name.isNotBlank() || phone.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun VercelInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isMono: Boolean = false,
    singleLine: Boolean = true
) {
    val vercelColors = LocalVercelColors.current

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = VercelMono.Badge,
            color = vercelColors.textMuted
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = vercelColors.textMuted) },
            singleLine = singleLine,
            textStyle = if (isMono) VercelMono.PhoneNumber else MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = vercelColors.border,
                focusedContainerColor = vercelColors.surfaceSubtle,
                unfocusedContainerColor = vercelColors.surfaceSubtle
            ),
            shape = RoundedCornerShape(8.dp)
        )
    }
}
