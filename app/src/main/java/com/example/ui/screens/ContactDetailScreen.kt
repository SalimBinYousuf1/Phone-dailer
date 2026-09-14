package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ContactEntity
import com.example.ui.components.VercelAvatar
import com.example.ui.components.VercelBadge
import com.example.ui.components.VercelButton
import com.example.ui.components.VercelButtonVariant
import com.example.ui.components.VercelTopBar
import com.example.ui.theme.LocalVercelColors
import com.example.ui.theme.VercelAmber
import com.example.ui.theme.VercelMono
import com.example.ui.theme.VercelRed
import com.example.ui.theme.VercelSpacing

@Composable
fun ContactDetailScreen(
    contact: ContactEntity,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onCall: () -> Unit,
    onDelete: () -> Unit,
    onBlock: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val vercelColors = LocalVercelColors.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "DELETE CONTACT",
                    style = VercelMono.Badge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to permanently remove ${contact.name}?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("DELETE", color = VercelRed, style = VercelMono.Badge)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
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
                title = "CONTACT",
                subtitle = "ID: #${contact.id}",
                onBackClick = onBack,
                actions = {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (contact.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (contact.isFavorite) VercelAmber else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large Avatar
                VercelAvatar(
                    name = contact.name,
                    photoUri = contact.photoUri,
                    size = 80.dp
                )

                Spacer(modifier = Modifier.height(VercelSpacing.Space16))

                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (contact.company.isNotBlank()) {
                    Spacer(modifier = Modifier.height(VercelSpacing.Space4))
                    Text(
                        text = contact.company,
                        style = MaterialTheme.typography.bodyMedium,
                        color = vercelColors.textMuted
                    )
                }

                Spacer(modifier = Modifier.height(VercelSpacing.Space24))

                // Primary Quick Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(VercelSpacing.Space12)
                ) {
                    VercelButton(
                        text = "CALL",
                        icon = Icons.Default.Call,
                        onClick = onCall,
                        modifier = Modifier.weight(1f)
                    )
                    VercelButton(
                        text = "SMS",
                        icon = Icons.Default.Message,
                        variant = VercelButtonVariant.SECONDARY,
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${contact.phone}"))
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(VercelSpacing.Space24))

                // Phone details card
                DetailInfoCard(
                    title = "PHONE (${contact.phoneLabel.uppercase()})",
                    value = contact.phone,
                    isMono = true,
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(contact.phone))
                    }
                )

                if (contact.email.isNotBlank()) {
                    Spacer(modifier = Modifier.height(VercelSpacing.Space12))
                    DetailInfoCard(
                        title = "EMAIL",
                        value = contact.email,
                        isMono = false,
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(contact.email))
                        }
                    )
                }

                if (contact.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(VercelSpacing.Space12))
                    DetailInfoCard(
                        title = "NOTES",
                        value = contact.notes,
                        isMono = false
                    )
                }

                Spacer(modifier = Modifier.height(VercelSpacing.Space32))

                // Danger section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(VercelSpacing.Space12)
                ) {
                    VercelButton(
                        text = "SHARE CONTACT (VCARD)",
                        icon = Icons.Default.Share,
                        variant = VercelButtonVariant.SECONDARY,
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Name: ${contact.name}\nPhone: ${contact.phone}\nEmail: ${contact.email}")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Contact"))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    VercelButton(
                        text = "BLOCK NUMBER",
                        icon = Icons.Default.Block,
                        variant = VercelButtonVariant.SECONDARY,
                        onClick = onBlock,
                        modifier = Modifier.fillMaxWidth()
                    )

                    VercelButton(
                        text = "DELETE CONTACT",
                        icon = Icons.Default.Delete,
                        variant = VercelButtonVariant.DANGER,
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailInfoCard(
    title: String,
    value: String,
    isMono: Boolean,
    onCopy: (() -> Unit)? = null
) {
    val vercelColors = LocalVercelColors.current

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
                    text = title,
                    style = VercelMono.Badge,
                    color = vercelColors.textMuted
                )
                Spacer(modifier = Modifier.height(VercelSpacing.Space4))
                Text(
                    text = value,
                    style = if (isMono) VercelMono.PhoneNumber else MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (onCopy != null) {
                IconButton(onClick = onCopy) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = vercelColors.textMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
