package com.antidpi.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.antidpi.mobile.core.DnsOverHttpsResolver
import com.antidpi.mobile.core.DohServer
import com.antidpi.mobile.data.PreferencesManager
import com.antidpi.mobile.ui.theme.*

@Composable
fun DnsScreen(
    prefs: PreferencesManager,
    onSettingsChanged: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var gameAdBlockEnabled by remember { mutableStateOf(prefs.gameAdBlockEnabled) }
    var selectedAdBlockProvider by remember { mutableStateOf(prefs.gameAdBlockProvider) }
    var dohEnabled by remember { mutableStateOf(prefs.dohEnabled) }
    var selectedProvider by remember { mutableStateOf(prefs.dohProvider) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. GAME & WEB AD BLOCKER SECTION ---
        item {
            Text(
                text = "Reklam Engelleme (DNS)",
                style = Typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Mobil oyunlardaki video/afiş reklamlarını ve web sitelerindeki izleyicileri DNS seviyesinde engeller.",
                style = Typography.bodyMedium.copy(fontSize = 13.sp),
                color = TextSecondary
            )
        }

        // Master Switch for Game & Web AdBlock
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Reklam Engelleyici",
                            style = Typography.titleLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (gameAdBlockEnabled) "Reklam sunucuları filtreleniyor" else "Filtreleme kapalı",
                            style = Typography.labelSmall,
                            color = if (gameAdBlockEnabled) AccentGreen else TextMuted
                        )
                    }

                    Switch(
                        checked = gameAdBlockEnabled,
                        onCheckedChange = {
                            gameAdBlockEnabled = it
                            prefs.gameAdBlockEnabled = it
                            onSettingsChanged()
                        },
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
        }

        // AdBlock Provider Selection (if enabled)
        if (gameAdBlockEnabled) {
            item {
                Text(
                    text = "Reklam Engelleme Servisi",
                    style = Typography.titleLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
            }

            items(DnsOverHttpsResolver.ADBLOCK_PROVIDERS) { provider ->
                val isSelected = selectedAdBlockProvider == provider.name
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardDark)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) AccentGreen else CardBorder,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            selectedAdBlockProvider = provider.name
                            prefs.gameAdBlockProvider = provider.name
                            onSettingsChanged()
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
                                selectedAdBlockProvider = provider.name
                                prefs.gameAdBlockProvider = provider.name
                                onSettingsChanged()
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = AccentGreen, unselectedColor = TextMuted)
                        )
                        Column {
                            Text(
                                text = provider.name,
                                style = Typography.titleLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = provider.description,
                                style = Typography.bodyMedium.copy(fontSize = 12.sp),
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "IP: ${provider.primaryIp}, ${provider.secondaryIp}",
                                style = Typography.labelSmall.copy(fontSize = 11.sp),
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
        }

        // --- 2. STANDARD SECURE DNS (DoH) SECTION ---
        item {
            Text(
                text = "Standart Şifreli DNS",
                style = Typography.titleLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (gameAdBlockEnabled) {
                    "Not: Reklam engelleyici açıkken yukarıdaki DNS kullanılır."
                } else {
                    "Operatörün varsayılan DNS sorgu kayıtlarını şifreler."
                },
                style = Typography.bodyMedium.copy(fontSize = 12.sp),
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Şifreli DNS (DoH)",
                            style = Typography.titleLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (dohEnabled) "Tüm DNS sorguları şifreleniyor" else "Operatör DNS'i devrede",
                            style = Typography.labelSmall,
                            color = if (dohEnabled) AccentSlate else TextMuted
                        )
                    }

                    Switch(
                        checked = dohEnabled,
                        onCheckedChange = {
                            dohEnabled = it
                            prefs.dohEnabled = it
                            onSettingsChanged()
                        },
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
        }

        if (dohEnabled && !gameAdBlockEnabled) {
            item {
                Text(
                    text = "DNS Sağlayıcısı",
                    style = Typography.titleLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
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
                            width = 1.dp,
                            color = if (isSelected) AccentGreen else CardBorder,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            selectedProvider = provider.name
                            prefs.dohProvider = provider.name
                            onSettingsChanged()
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
                                onSettingsChanged()
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = AccentGreen, unselectedColor = TextMuted)
                        )
                        Column {
                            Text(
                                text = provider.name,
                                style = Typography.titleLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.Medium),
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
    }
}
