package com.antidpi.mobile.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.antidpi.mobile.core.ConnectionState
import com.antidpi.mobile.ui.theme.*

@Composable
fun PowerButton(
    connectionState: ConnectionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnected = connectionState == ConnectionState.CONNECTED
    val isTransitioning = connectionState == ConnectionState.CONNECTING || connectionState == ConnectionState.DISCONNECTING

    val buttonBgColor by animateColorAsState(
        targetValue = when {
            isConnected -> AccentGreen
            isTransitioning -> CardDark
            else -> CardDark
        },
        animationSpec = tween(300),
        label = "btnBg"
    )

    val iconColor by animateColorAsState(
        targetValue = when {
            isConnected -> Color(0xFF09090B)
            isTransitioning -> TextMuted
            else -> TextSecondary
        },
        animationSpec = tween(300),
        label = "iconColor"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isConnected -> AccentGreen
            isTransitioning -> AccentGreen.copy(alpha = 0.4f)
            else -> CardBorder
        },
        animationSpec = tween(300),
        label = "borderColor"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(160.dp)
    ) {
        // Outer subtle boundary ring
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .border(1.dp, CardBorderSubtle, CircleShape)
        )

        // Main Minimalist Tactile Button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(122.dp)
                .clip(CircleShape)
                .background(buttonBgColor)
                .border(1.5.dp, borderColor, CircleShape)
                .clickable { onClick() }
        ) {
            if (isTransitioning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(34.dp),
                    color = AccentGreen,
                    strokeWidth = 2.5.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = if (isConnected) "Devre Dışı Bırak" else "Bağlan",
                    tint = iconColor,
                    modifier = Modifier.size(46.dp)
                )
            }
        }
    }
}
