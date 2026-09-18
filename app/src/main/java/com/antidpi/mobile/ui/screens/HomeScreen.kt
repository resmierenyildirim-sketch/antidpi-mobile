package com.antidpi.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.antidpi.mobile.R
import com.antidpi.mobile.core.ConnectionState
import com.antidpi.mobile.data.DpiProfile
import com.antidpi.mobile.data.NetworkStats
import com.antidpi.mobile.ui.components.PowerButton
import com.antidpi.mobile.ui.components.StatsSection
import com.antidpi.mobile.ui.theme.*

@Composable
fun HomeScreen(
    connectionState: ConnectionState,
    stats: NetworkStats,
    activeProfile: DpiProfile,
    gameAdBlockEnabled: Boolean,
    onToggleClick: () -> Unit,
    onNavigateToProfiles: () -> Unit,
    onToggleGameAdBlock: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnected = connectionState == ConnectionState.CONNECTED
    val isTransitioning = connectionState == ConnectionState.CONNECTING || connectionState == ConnectionState.DISCONNECTING
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Minimalist Top Bar with App Logo
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(CardDark)
                        .border(1.dp, CardBorder, RoundedCornerShape(9.dp))
                        .padding(5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_app_logo),
                        contentDescription = "AntiDPI Logo",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Text(
                    text = "AntiDPI",
                    style = Typography.headlineMedium.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = TextPrimary
                )
            }

            // Status Indicator Dot + Label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardDark)
                    .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            when (connectionState) {
                                ConnectionState.CONNECTED -> AccentGreen
                                ConnectionState.CONNECTING, ConnectionState.DISCONNECTING -> AccentSlate
                                ConnectionState.DISCONNECTED -> TextMuted
                            }
                        )
                )
                Text(
                    text = when (connectionState) {
                        ConnectionState.CONNECTED -> "Bağlı"
                        ConnectionState.CONNECTING -> "Bağlanıyor"
                        ConnectionState.DISCONNECTING -> "Kesiliyor"
                        ConnectionState.DISCONNECTED -> "Kapalı"
                    },
                    style = Typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = when (connectionState) {
                            ConnectionState.CONNECTED -> AccentGreen
                            ConnectionState.CONNECTING, ConnectionState.DISCONNECTING -> AccentSlate
                            ConnectionState.DISCONNECTED -> TextSecondary
                        }
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Center Minimalist Power Button
        PowerButton(
            connectionState = connectionState,
            onClick = onToggleClick
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Clean Status Typography
        Text(
            text = when (connectionState) {
                ConnectionState.CONNECTED -> "DPI Koruması Etkin"
                ConnectionState.CONNECTING -> "Bağlantı Kuruluyor..."
                ConnectionState.DISCONNECTING -> "Bağlantı Kapatılıyor..."
                ConnectionState.DISCONNECTED -> "Koruma Devre Dışı"
            },
            style = Typography.titleLarge.copy(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
            color = if (isConnected) TextPrimary else TextSecondary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = when (connectionState) {
                ConnectionState.CONNECTED -> "Yerel bypass devrede • Sıfır hız kaybı"
                ConnectionState.CONNECTING -> "Ağ soketi başlatılıyor"
                ConnectionState.DISCONNECTING -> "Tünel kapatılıyor"
                ConnectionState.DISCONNECTED -> "Başlatmak için dokunun"
            },
            style = Typography.bodyMedium.copy(fontSize = 13.sp),
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Active Profile Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardDark)
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                .clickable { onNavigateToProfiles() }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Profil",
                        style = Typography.labelSmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = activeProfile.name,
                        style = Typography.titleLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                    Text(
                        text = activeProfile.description,
                        style = Typography.bodyMedium.copy(fontSize = 12.sp),
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Profili Değiştir",
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Minimalist Game & Web AdBlocker Switch Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardDark)
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Reklam Engelleme",
                        style = Typography.titleLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (gameAdBlockEnabled) "Oyun ve sitelerdeki reklamlar engelleniyor" else "DNS seviyesinde reklam filtresi",
                        style = Typography.bodyMedium.copy(fontSize = 12.sp),
                        color = if (gameAdBlockEnabled) AccentGreen else TextMuted
                    )
                }

                Switch(
                    checked = gameAdBlockEnabled,
                    onCheckedChange = { onToggleGameAdBlock(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AccentGreen,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = SurfaceDark,
                        uncheckedBorderColor = CardBorder
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Clean Stats Section
        StatsSection(
            stats = stats,
            isConnected = isConnected
        )
    }
}
