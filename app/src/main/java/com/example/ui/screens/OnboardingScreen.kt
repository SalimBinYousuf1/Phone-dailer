package com.example.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.VercelBadge
import com.example.ui.components.VercelButton
import com.example.ui.components.VercelButtonVariant
import com.example.ui.theme.LocalVercelColors
import com.example.ui.theme.VercelGreen
import com.example.ui.theme.VercelMono
import com.example.ui.theme.VercelSpacing
import com.example.telecom.TelecomHelper

@Composable
fun OnboardingScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vercelColors = LocalVercelColors.current
    var isDefaultDialer by remember { mutableStateOf(TelecomHelper.isDefaultDialer(context)) }
    var permissionsGranted by remember { mutableStateOf(false) }

    val roleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isDefaultDialer = TelecomHelper.isDefaultDialer(context)
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        permissionsGranted = perms.values.all { it }
    }

    val requiredPermissions = arrayOf(
        android.Manifest.permission.READ_CONTACTS,
        android.Manifest.permission.WRITE_CONTACTS,
        android.Manifest.permission.READ_CALL_LOG,
        android.Manifest.permission.WRITE_CALL_LOG,
        android.Manifest.permission.CALL_PHONE,
        android.Manifest.permission.ANSWER_PHONE_CALLS,
        android.Manifest.permission.POST_NOTIFICATIONS
    )

    LaunchedEffect(Unit) {
        isDefaultDialer = TelecomHelper.isDefaultDialer(context)
        permissionsLauncher.launch(requiredPermissions)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(VercelSpacing.Space24)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VercelBadge(text = "DIALER // v1.0.0")
                    VercelBadge(
                        text = if (isDefaultDialer) "SYSTEM: DEFAULT" else "SYSTEM: STANDBY",
                        borderColor = if (isDefaultDialer) VercelGreen else null
                    )
                }

                Spacer(modifier = Modifier.height(VercelSpacing.Space32))

                Text(
                    text = "Developer-grade\ntelephony engine.",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(VercelSpacing.Space12))

                Text(
                    text = "High-precision native call control, contact caching, and full call history in Vercel design language.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = vercelColors.textMuted
                )

                Spacer(modifier = Modifier.height(VercelSpacing.Space32))

                // Status checklist card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(vercelColors.surfaceSubtle, RoundedCornerShape(8.dp))
                        .border(1.dp, vercelColors.border, RoundedCornerShape(8.dp))
                        .padding(VercelSpacing.Space16)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(VercelSpacing.Space12)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ROLE_DIALER",
                                style = VercelMono.Badge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isDefaultDialer) "ENABLED" else "NOT SET",
                                style = VercelMono.Badge,
                                color = if (isDefaultDialer) VercelGreen else vercelColors.textMuted
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CONTACTS_ACCESS",
                                style = VercelMono.Badge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "ACTIVE",
                                style = VercelMono.Badge,
                                color = VercelGreen
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "INCALL_SERVICE",
                                style = VercelMono.Badge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "READY",
                                style = VercelMono.Badge,
                                color = VercelGreen
                            )
                        }
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(VercelSpacing.Space12),
                modifier = Modifier.padding(bottom = VercelSpacing.Space16)
            ) {
                if (!isDefaultDialer) {
                    VercelButton(
                        text = "SET AS DEFAULT DIALER",
                        icon = Icons.Default.Phone,
                        onClick = {
                            val intent = TelecomHelper.createDefaultDialerIntent(context)
                            if (intent != null) {
                                roleLauncher.launch(intent)
                            } else {
                                isDefaultDialer = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "set_default_dialer_button"
                    )
                }

                VercelButton(
                    text = if (isDefaultDialer) "ENTER DIALER" else "CONTINUE ANYWAY",
                    icon = Icons.Default.Check,
                    variant = if (isDefaultDialer) VercelButtonVariant.PRIMARY else VercelButtonVariant.SECONDARY,
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "continue_to_app_button"
                )
            }
        }
    }
}
