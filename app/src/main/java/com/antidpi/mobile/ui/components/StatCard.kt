package com.antidpi.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antidpi.mobile.data.NetworkStats
import com.antidpi.mobile.ui.theme.*

@Composable
fun StatsSection(
    stats: NetworkStats,
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatItem(
            icon = Icons.Default.ArrowDownward,
            iconTint = CyanAccent,
            label = "İndirme Hızı",
            value = if (isConnected) stats.formatDownloadSpeed() else "0 B/s",
            subtext = "Toplam: ${stats.formatBytesIn()}",
            modifier = Modifier.weight(1f)
        )
        StatItem(
            icon = Icons.Default.ArrowUpward,
            iconTint = GreenNeon,
            label = "Yükleme Hızı",
            value = if (isConnected) stats.formatUploadSpeed() else "0 B/s",
            subtext = "Toplam: ${stats.formatBytesOut()}",
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatItem(
            icon = Icons.Default.Security,
            iconTint = BrandPurple,
            label = "Atlatılan Paket",
            value = if (isConnected) "${stats.packetsDesynced}" else "0",
            subtext = "DPI Filtresi Kırıldı",
            modifier = Modifier.weight(1f)
        )
        StatItem(
            icon = Icons.Default.Timer,
            iconTint = TextSecondary,
            label = "Bağlantı Süresi",
            value = if (isConnected) stats.formatDuration() else "00:00",
            subtext = if (isConnected) "Aktif" else "Kapalı",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatItem(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    subtext: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconTint.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = label,
                    style = Typography.labelSmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = Typography.titleLarge.copy(fontSize = 17.sp),
                color = TextPrimary
            )

            Text(
                text = subtext,
                style = Typography.labelSmall.copy(fontSize = 10.sp),
                color = TextMuted
            )
        }
    }
}
