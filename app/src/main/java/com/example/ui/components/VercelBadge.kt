package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalVercelColors
import com.example.ui.theme.VercelMono

@Composable
fun VercelBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color? = null,
    borderColor: Color? = null
) {
    val vercelColors = LocalVercelColors.current
    val effectiveBorder = borderColor ?: vercelColors.border
    val effectiveColor = color ?: vercelColors.monoText

    Box(
        modifier = modifier
            .background(
                color = vercelColors.badgeBg,
                shape = RoundedCornerShape(4.dp)
            )
            .border(
                width = 1.dp,
                color = effectiveBorder,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = VercelMono.Badge,
            color = effectiveColor
        )
    }
}
