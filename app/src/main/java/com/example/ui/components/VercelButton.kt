package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalVercelColors
import com.example.ui.theme.VercelRed
import com.example.ui.theme.VercelSpacing

enum class VercelButtonVariant {
    PRIMARY,
    SECONDARY,
    DANGER,
    GHOST
}

@Composable
fun VercelButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    variant: VercelButtonVariant = VercelButtonVariant.PRIMARY,
    enabled: Boolean = true,
    testTag: String = "vercel_button"
) {
    val vercelColors = LocalVercelColors.current
    val colorScheme = MaterialTheme.colorScheme

    val bg = when (variant) {
        VercelButtonVariant.PRIMARY -> if (enabled) colorScheme.primary else vercelColors.badgeBg
        VercelButtonVariant.SECONDARY -> colorScheme.surface
        VercelButtonVariant.DANGER -> VercelRed
        VercelButtonVariant.GHOST -> Color.Transparent
    }

    val contentColor = when (variant) {
        VercelButtonVariant.PRIMARY -> colorScheme.onPrimary
        VercelButtonVariant.SECONDARY -> colorScheme.onSurface
        VercelButtonVariant.DANGER -> Color.White
        VercelButtonVariant.GHOST -> colorScheme.onSurface
    }

    val borderModifier = if (variant == VercelButtonVariant.SECONDARY) {
        Modifier.border(1.dp, vercelColors.border, RoundedCornerShape(8.dp))
    } else Modifier

    Box(
        modifier = modifier
            .testTag(testTag)
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .then(borderModifier)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = VercelSpacing.Space16),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(VercelSpacing.Space8))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor
            )
        }
    }
}
