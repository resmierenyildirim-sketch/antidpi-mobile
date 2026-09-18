package com.antidpi.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Speed row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Download
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = if (isConnected) AccentGreen else TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "İndirme",
                            style = Typography.labelSmall,
                            color = TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isConnected) stats.formatDownloadSpeed() else "0 B/s",
                        style = Typography.titleLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                    Text(
                        text = "Toplam: ${if (isConnected) stats.formatBytesIn() else "0 B"}",
                        style = Typography.labelSmall.copy(fontSize = 11.sp),
                        color = TextMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp)
                        .background(CardBorder)
                )

                // Upload
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Yükleme",
                            style = Typography.labelSmall,
                            color = TextMuted
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = if (isConnected) AccentGreen else TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isConnected) stats.formatUploadSpeed() else "0 B/s",
                        style = Typography.titleLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                    Text(
                        text = "Toplam: ${if (isConnected) stats.formatBytesOut() else "0 B"}",
                        style = Typography.labelSmall.copy(fontSize = 11.sp),
                        color = TextMuted
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(CardBorderSubtle)
            )

            // Duration and Packets row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Süre:",
                        style = Typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        text = if (isConnected) stats.formatDuration() else "--:--",
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = TextSecondary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "DPI Paketi:",
                        style = Typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        text = if (isConnected) "${stats.packetsDesynced}" else "0",
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
