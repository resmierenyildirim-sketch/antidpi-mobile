package com.antidpi.mobile.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.antidpi.mobile.ui.theme.*

@Composable
fun PowerButton(
    isConnected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isConnected) 1.08f else 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val buttonColor by animateColorAsState(
        targetValue = if (isConnected) GreenNeon else CardDark,
        animationSpec = tween(500),
        label = "buttonColor"
    )

    val glowColor by animateColorAsState(
        targetValue = if (isConnected) GreenGlow else CyanGlow,
        animationSpec = tween(500),
        label = "glowColor"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(200.dp)
            .scale(if (isConnected) pulseScale else 1f)
    ) {
        // Outer Glow Ring
        Box(
            modifier = Modifier
                .size(190.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(glowColor, Color.Transparent)
                    )
                )
        )

        // Middle Ring with Border
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(SurfaceDark)
                .border(
                    width = 3.dp,
                    brush = Brush.linearGradient(
                        colors = if (isConnected) {
                            listOf(GreenNeon, CyanAccent)
                        } else {
                            listOf(CardBorder, Color(0xFF1E293B))
                        }
                    ),
                    shape = CircleShape
                )
        )

        // Center Button with Touch Interaction
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(110.dp)
                .shadow(
                    elevation = if (isConnected) 16.dp else 4.dp,
                    shape = CircleShape,
                    spotColor = if (isConnected) GreenNeon else CyanAccent
                )
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = if (isConnected) {
                            listOf(Color(0xFF065F46), Color(0xFF047857))
                        } else {
                            listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                        }
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
        ) {
            Icon(
                imageVector = if (isConnected) Icons.Default.Shield else Icons.Default.PowerSettingsNew,
                contentDescription = if (isConnected) "Devre Dışı Bırak" else "Bağlan",
                tint = if (isConnected) Color.White else TextSecondary,
                modifier = Modifier.size(52.dp)
            )
        }
    }
}
