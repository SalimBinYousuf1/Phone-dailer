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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ContactEntity
import com.example.ui.components.VercelAvatar
import com.example.ui.components.VercelBadge
import com.example.ui.components.VercelTopBar
import com.example.ui.theme.LocalVercelColors
import com.example.ui.theme.VercelAmber
import com.example.ui.theme.VercelMono
import com.example.ui.theme.VercelSpacing
import com.example.ui.viewmodel.ContactsViewModel
import kotlinx.coroutines.launch

@Composable
fun ContactsListScreen(
    viewModel: ContactsViewModel,
    onContactClick: (ContactEntity) -> Unit,
    onAddContactClick: () -> Unit,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val vercelColors = LocalVercelColors.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            VercelTopBar(
                title = "CONTACTS",
                subtitle = "${uiState.totalCount} STORED",
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = onAddContactClick,
                        modifier = Modifier.testTag("add_contact_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Contact",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )

            if (uiState.sections.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(VercelSpacing.Space32),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "NO CONTACTS FOUND",
                            style = VercelMono.Badge,
                            color = vercelColors.textMuted
                        )
                        Spacer(modifier = Modifier.height(VercelSpacing.Space8))
                        Text(
                            text = "Tap '+' to add your first contact.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = vercelColors.textMuted
                        )
                    }
                }
            } else {
                Row(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        for (section in uiState.sections) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(vercelColors.surfaceSubtle)
                                        .border(1.dp, vercelColors.border)
                                        .padding(horizontal = VercelSpacing.Space16, vertical = VercelSpacing.Space4)
                                ) {
                                    Text(
                                        text = section.letter.toString(),
                                        style = VercelMono.Badge,
                                        color = vercelColors.textMuted
                                    )
                                }
                            }

                            items(section.contacts, key = { it.id }) { contact ->
                                ContactRowItem(
                                    contact = contact,
                                    onClick = { onContactClick(contact) },
                                    onCallClick = { viewModel.callContact(context, contact) },
                                    onFavoriteClick = { viewModel.toggleFavorite(contact) }
                                )
                            }
                        }
                    }

                    // A-Z Quick Fast Scroll Column
                    Column(
                        modifier = Modifier
                            .padding(vertical = VercelSpacing.Space8, horizontal = 4.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        for (section in uiState.sections) {
                            Text(
                                text = section.letter.toString(),
                                style = VercelMono.Badge.copy(fontSize = 9.sp),
                                color = vercelColors.textMuted,
                                modifier = Modifier
                                    .padding(vertical = 1.dp)
                                    .clickable {
                                        // Scroll to section index
                                        var targetIndex = 0
                                        for (s in uiState.sections) {
                                            if (s.letter == section.letter) break
                                            targetIndex += s.contacts.size + 1
                                        }
                                        scope.launch {
                                            listState.animateScrollToItem(targetIndex)
                                        }
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactRowItem(
    contact: ContactEntity,
    onClick: () -> Unit,
    onCallClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    val vercelColors = LocalVercelColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = VercelSpacing.Space16, vertical = VercelSpacing.Space12),
        verticalAlignment = Alignment.CenterVertically
    ) {
        VercelAvatar(
            name = contact.name,
            photoUri = contact.photoUri,
            size = 40.dp
        )

        Spacer(modifier = Modifier.width(VercelSpacing.Space12))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = contact.phone,
                    style = VercelMono.PhoneNumber.copy(fontSize = 12.sp),
                    color = vercelColors.textMuted
                )
                if (contact.company.isNotBlank()) {
                    Spacer(modifier = Modifier.width(VercelSpacing.Space8))
                    Text(
                        text = "• ${contact.company}",
                        style = MaterialTheme.typography.bodySmall,
                        color = vercelColors.textMuted
                    )
                }
            }
        }

        IconButton(onClick = onFavoriteClick) {
            Icon(
                imageVector = if (contact.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                contentDescription = "Favorite",
                tint = if (contact.isFavorite) VercelAmber else vercelColors.textMuted,
                modifier = Modifier.size(20.dp)
            )
        }

        IconButton(onClick = onCallClick) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Call",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
