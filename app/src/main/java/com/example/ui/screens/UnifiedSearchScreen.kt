package com.example.ui.screens

import android.content.Context
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.local.entity.ContactEntity
import com.example.telecom.TelecomHelper
import com.example.ui.components.VercelAvatar
import com.example.ui.components.VercelBadge
import com.example.ui.theme.LocalVercelColors
import com.example.ui.theme.VercelMono
import com.example.ui.theme.VercelSpacing
import com.example.ui.viewmodel.ContactsViewModel
import com.example.ui.viewmodel.RecentsViewModel

@Composable
fun UnifiedSearchScreen(
    contactsViewModel: ContactsViewModel,
    recentsViewModel: RecentsViewModel,
    onContactClick: (ContactEntity) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vercelColors = LocalVercelColors.current
    var query by remember { mutableStateOf("") }

    val contactsState by contactsViewModel.uiState.collectAsState()
    val recentsState by recentsViewModel.uiState.collectAsState()

    val filteredContacts = if (query.isBlank()) emptyList() else {
        contactsState.sections.flatMap { it.contacts }.filter {
            it.name.contains(query, ignoreCase = true) || it.phone.contains(query, ignoreCase = true)
        }
    }

    val filteredRecents = if (query.isBlank()) emptyList() else {
        recentsState.groups.flatMap { it.items }.filter {
            it.number.contains(query, ignoreCase = true) || (it.name?.contains(query, ignoreCase = true) == true)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search Bar Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .border(1.dp, vercelColors.border)
                    .padding(horizontal = VercelSpacing.Space8),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search contacts, numbers...", color = vercelColors.textMuted) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                        focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                    )
                )

                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = vercelColors.textMuted
                        )
                    }
                }
            }

            // Results List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Direct Dial Option if query looks like a number
                val cleanQuery = query.filter { it.isDigit() || it == '+' || it == '*' || it == '#' }
                if (cleanQuery.isNotBlank()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    TelecomHelper.placeCall(context, cleanQuery)
                                }
                                .padding(horizontal = VercelSpacing.Space16, vertical = VercelSpacing.Space12),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Call",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(VercelSpacing.Space12))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "DIAL DIRECT",
                                    style = VercelMono.Badge,
                                    color = vercelColors.textMuted
                                )
                                Text(
                                    text = cleanQuery,
                                    style = VercelMono.PhoneNumber,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Matching Contacts
                if (filteredContacts.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(vercelColors.surfaceSubtle)
                                .border(1.dp, vercelColors.border)
                                .padding(horizontal = VercelSpacing.Space16, vertical = VercelSpacing.Space4)
                        ) {
                            Text(
                                text = "CONTACTS (${filteredContacts.size})",
                                style = VercelMono.Badge,
                                color = vercelColors.textMuted
                            )
                        }
                    }

                    items(filteredContacts, key = { "c_${it.id}" }) { contact ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onContactClick(contact) }
                                .padding(horizontal = VercelSpacing.Space16, vertical = VercelSpacing.Space12),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            VercelAvatar(name = contact.name, size = 36.dp)
                            Spacer(modifier = Modifier.width(VercelSpacing.Space12))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = contact.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = contact.phone,
                                    style = VercelMono.PhoneNumber.copy(fontSize = 12.sp),
                                    color = vercelColors.textMuted
                                )
                            }
                            IconButton(onClick = { contactsViewModel.callContact(context, contact) }) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Call",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Matching Recents
                if (filteredRecents.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(vercelColors.surfaceSubtle)
                                .border(1.dp, vercelColors.border)
                                .padding(horizontal = VercelSpacing.Space16, vertical = VercelSpacing.Space4)
                        ) {
                            Text(
                                text = "CALL HISTORY (${filteredRecents.size})",
                                style = VercelMono.Badge,
                                color = vercelColors.textMuted
                            )
                        }
                    }

                    items(filteredRecents, key = { "r_${it.id}" }) { call ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { TelecomHelper.placeCall(context, call.number, call.name) }
                                .padding(horizontal = VercelSpacing.Space16, vertical = VercelSpacing.Space12),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = null,
                                tint = vercelColors.textMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(VercelSpacing.Space12))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = call.name ?: call.number,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (call.name != null) {
                                    Text(
                                        text = call.number,
                                        style = VercelMono.PhoneNumber.copy(fontSize = 12.sp),
                                        color = vercelColors.textMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
