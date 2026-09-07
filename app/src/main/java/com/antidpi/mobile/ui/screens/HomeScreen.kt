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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antidpi.mobile.data.DpiProfile
import com.antidpi.mobile.data.NetworkStats
import com.antidpi.mobile.ui.components.PowerButton
import com.antidpi.mobile.ui.components.StatsSection
import com.antidpi.mobile.ui.theme.*

@Composable
fun HomeScreen(
    isConnected: Boolean,
    stats: NetworkStats,
    activeProfile: DpiProfile,
    onToggleClick: () -> Unit,
    onNavigateToProfiles: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
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
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(CyanAccent.copy(alpha = 0.2f), BrandPurple.copy(alpha = 0.2f))
                            )
                        )
                        .border(1.dp, CyanAccent.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "AntiDPI Mobile",
                        style = Typography.titleLarge.copy(fontSize = 19.sp, fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "GoodbyeDPI Çekirdeği",
                        style = Typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            // Status Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isConnected) GreenNeon.copy(alpha = 0.15f) else CardDark)
                    .border(
                        1.dp,
                        if (isConnected) GreenNeon.copy(alpha = 0.4f) else CardBorder,
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isConnected) GreenNeon else TextMuted)
                    )
                    Text(
                        text = if (isConnected) "KORUMALI" else "KAPALI",
                        style = Typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isConnected) GreenNeon else TextSecondary
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Center Power Button
        PowerButton(
            isConnected = isConnected,
            onClick = onToggleClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isConnected) "DPI Koruması Etkin" else "Korumayı Başlatmak İçin Dokunun",
            style = Typography.titleLarge.copy(fontSize = 17.sp),
            color = if (isConnected) GreenNeon else TextSecondary
        )

        Text(
            text = if (isConnected) "Uzak sunucu yok • Tam operatör hızı" else "Trafik doğrudan cihazınızda işlenir",
            style = Typography.bodyMedium.copy(fontSize = 13.sp),
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(28.dp))

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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyanAccent.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Aktif Operatör Profili",
                            style = Typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            text = activeProfile.name,
                            style = Typography.titleLarge.copy(fontSize = 15.sp),
                            color = TextPrimary
                        )
                        Text(
                            text = activeProfile.description,
                            style = Typography.bodyMedium.copy(fontSize = 12.sp),
                            color = TextMuted,
                            maxLines = 1
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Profili Değiştir",
                    tint = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Stats Section
        StatsSection(
            stats = stats,
            isConnected = isConnected
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Info Banner: Why zero speed reduction?
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDark.copy(alpha = 0.6f))
                .border(1.dp, CyanAccent.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = "Nasıl Sıfır Hız Kaybı Sağlanıyor?",
                        style = Typography.titleLarge.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                        color = CyanAccent
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Geleneksel VPN'ler gibi trafiğinizi yurt dışındaki bir sunucuya göndermez. Sadece telefonunuzdaki paketleri parçalayarak operatörünüzün DPI sansür filtresini yanıltır ve orijinal hızınızda doğrudan bağlanır.",
                        style = Typography.bodyMedium.copy(fontSize = 12.sp, lineHeight = 16.sp),
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
