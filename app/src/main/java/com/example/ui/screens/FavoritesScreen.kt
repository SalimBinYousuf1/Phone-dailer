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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ContactEntity
import com.example.ui.components.VercelAvatar
import com.example.ui.components.VercelBadge
import com.example.ui.components.VercelTopBar
import com.example.ui.theme.LocalVercelColors
import com.example.ui.theme.VercelMono
import com.example.ui.theme.VercelSpacing
import com.example.ui.viewmodel.ContactsViewModel

@Composable
fun FavoritesScreen(
    viewModel: ContactsViewModel,
    onContactClick: (ContactEntity) -> Unit,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val favorites by viewModel.favorites.collectAsState()
    val context = LocalContext.current
    val vercelColors = LocalVercelColors.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            VercelTopBar(
                title = "FAVORITES",
                subtitle = "${favorites.size} PINNED",
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )

            if (favorites.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(VercelSpacing.Space32),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "NO FAVORITES YET",
                            style = VercelMono.Badge,
                            color = vercelColors.textMuted
                        )
                        Spacer(modifier = Modifier.height(VercelSpacing.Space8))
                        Text(
                            text = "Star contacts to keep them accessible for 1-tap speed dialing.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = vercelColors.textMuted
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(VercelSpacing.Space16),
                    horizontalArrangement = Arrangement.spacedBy(VercelSpacing.Space12),
                    verticalArrangement = Arrangement.spacedBy(VercelSpacing.Space12)
                ) {
                    items(favorites, key = { it.id }) { contact ->
                        FavoriteCard(
                            contact = contact,
                            onClick = { onContactClick(contact) },
                            onCall = { viewModel.callContact(context, contact) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteCard(
    contact: ContactEntity,
    onClick: () -> Unit,
    onCall: () -> Unit
) {
    val vercelColors = LocalVercelColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(vercelColors.surfaceSubtle)
            .border(1.dp, vercelColors.border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(VercelSpacing.Space16)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                VercelAvatar(
                    name = contact.name,
                    photoUri = contact.photoUri,
                    size = 44.dp
                )

                IconButton(
                    onClick = onCall,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(VercelSpacing.Space12))

            Text(
                text = contact.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            Text(
                text = contact.phone,
                style = VercelMono.PhoneNumber.copy(fontSize = 12.sp),
                color = vercelColors.textMuted,
                maxLines = 1
            )
        }
    }
}
