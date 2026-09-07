package com.antidpi.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antidpi.mobile.core.DnsOverHttpsResolver
import com.antidpi.mobile.core.DohServer
import com.antidpi.mobile.data.PreferencesManager
import com.antidpi.mobile.ui.theme.*

@Composable
fun DnsScreen(
    prefs: PreferencesManager,
    modifier: Modifier = Modifier
) {
    var dohEnabled by remember { mutableStateOf(prefs.dohEnabled) }
    var selectedProvider by remember { mutableStateOf(prefs.dohProvider) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Şifreli DNS (DoH)",
                style = Typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Text(
                text = "Operatörlerin DNS zehirleme (5651 sayılı kanun uyarı sayfaları) ve alan adı izleme girişimlerini engeller.",
                style = Typography.bodyMedium,
                color = TextSecondary
            )
        }

        // Enable DoH Master Switch
        item {
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyanAccent.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "DNS over HTTPS Etkinleştir",
                                style = Typography.titleLarge.copy(fontSize = 15.sp),
                                color = TextPrimary
                            )
                            Text(
                                text = if (dohEnabled) "Tüm DNS sorguları şifreleniyor" else "Operatörün varsayılan DNS'i aktif",
                                style = Typography.labelSmall,
                                color = if (dohEnabled) GreenNeon else TextMuted
                            )
                        }
                    }

                    Switch(
                        checked = dohEnabled,
                        onCheckedChange = {
                            dohEnabled = it
                            prefs.dohEnabled = it
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent)
                    )
                }
            }
        }

        if (dohEnabled) {
            item {
                Text(
                    text = "Güvenli DNS Sağlayıcısı Seçin",
                    style = Typography.titleLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
            }

            items(DnsOverHttpsResolver.PROVIDERS) { provider: DohServer ->
                val isSelected = selectedProvider == provider.name
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardDark)
                        .border(
                            width = 1.5.dp,
                            color = if (isSelected) CyanAccent else CardBorder,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            selectedProvider = provider.name
                            prefs.dohProvider = provider.name
                        }
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                selectedProvider = provider.name
                                prefs.dohProvider = provider.name
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = CyanAccent)
                        )
                        Column {
                            Text(
                                text = provider.name,
                                style = Typography.titleLarge.copy(fontSize = 15.sp),
                                color = TextPrimary
                            )
                            Text(
                                text = provider.url,
                                style = Typography.bodyMedium.copy(fontSize = 12.sp),
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDark.copy(alpha = 0.6f))
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
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
                    Text(
                        text = "DPI atlatma ile birlikte Şifreli DNS kullanmak, hem alan adı bazlı DNS engellerini hem de TLS SNI incelemesini tamamen etkisiz hale getirir.",
                        style = Typography.bodyMedium.copy(fontSize = 12.sp, lineHeight = 16.sp),
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
