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
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. GAME AD BLOCKER SECTION ---
        item {
            Text(
                text = "Oyun Reklam Engelleyici",
                style = Typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Text(
                text = "Mobil oyunlarda ve uygulamalarda çıkan ara video (interstitial) ve afiş reklamlarını DNS seviyesinde engeller.",
                style = Typography.bodyMedium,
                color = TextSecondary
            )
        }

        // Master Switch for Game AdBlock
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (gameAdBlockEnabled) {
                            Brush.horizontalGradient(
                                listOf(Color(0xFF0D2821), CardDark)
                            )
                        } else {
                            Brush.horizontalGradient(
                                listOf(CardDark, CardDark)
                            )
                        }
                    )
                    .border(
                        1.dp,
                        if (gameAdBlockEnabled) GreenNeon.copy(alpha = 0.5f) else CardBorder,
                        RoundedCornerShape(16.dp)
                    )
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
                                .background(if (gameAdBlockEnabled) GreenNeon.copy(alpha = 0.2f) else CyanAccent.copy(alpha = 0.1f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = null,
                                tint = if (gameAdBlockEnabled) GreenNeon else TextMuted,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Oyun İçi Reklam Engelleme",
                                style = Typography.titleLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                                color = TextPrimary
                            )
                            Text(
                                text = if (gameAdBlockEnabled) "Unity, AdMob, AppLovin reklamları engelleniyor" else "Reklam engelleme kapalı",
                                style = Typography.labelSmall,
                                color = if (gameAdBlockEnabled) GreenNeon else TextMuted
                            )
                        }
                    }

                    Switch(
                        checked = gameAdBlockEnabled,
                        onCheckedChange = {
                            gameAdBlockEnabled = it
                            prefs.gameAdBlockEnabled = it
                            onSettingsChanged()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GreenNeon,
                            checkedTrackColor = GreenNeon.copy(alpha = 0.35f)
                        )
                    )
                }
            }
        }

        // AdBlock Provider Selection (if enabled)
        if (gameAdBlockEnabled) {
            item {
                Text(
                    text = "Reklam Engelleme Sunucusu",
                    style = Typography.titleLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
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
                            width = 1.5.dp,
                            color = if (isSelected) GreenNeon else CardBorder,
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
                            colors = RadioButtonDefaults.colors(selectedColor = GreenNeon)
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
                                text = "DNS: ${provider.primaryIp}, ${provider.secondaryIp}",
                                style = Typography.labelSmall.copy(fontSize = 11.sp),
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // --- 2. STANDARD SECURE DNS (DoH) SECTION ---
        item {
            Text(
                text = "Standart Güvenli DNS",
                style = Typography.titleLarge.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Text(
                text = if (gameAdBlockEnabled) {
                    "Not: Oyun Reklam Engelleyici açıkken yukarıdaki reklam engelleyici DNS kullanılır. Reklam engelleyici kapatılırsa aşağıdaki DNS devreye girer."
                } else {
                    "Operatörlerin DNS zehirleme (uyarı sayfaları) girişimlerini engeller."
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
                                text = "Şifreli DNS (DoH) Etkinleştir",
                                style = Typography.titleLarge.copy(fontSize = 15.sp),
                                color = TextPrimary
                            )
                            Text(
                                text = if (dohEnabled) "Tüm DNS sorguları şifreleniyor" else "Operatörün varsayılan DNS'i aktif",
                                style = Typography.labelSmall,
                                color = if (dohEnabled) CyanAccent else TextMuted
                            )
                        }
                    }

                    Switch(
                        checked = dohEnabled,
                        onCheckedChange = {
                            dohEnabled = it
                            prefs.dohEnabled = it
                            onSettingsChanged()
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent)
                    )
                }
            }
        }

        if (dohEnabled && !gameAdBlockEnabled) {
            item {
                Text(
                    text = "Güvenli DNS Sağlayıcısı Seçin",
                    style = Typography.titleLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
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
                        text = "Oyun reklam engelleme, mobil oyunların reklam indirmesini engelleyerek hem seviye aralarındaki bekleme sürelerini sıfırlar hem de pil ve mobil veri tasarrufu sağlar.",
                        style = Typography.bodyMedium.copy(fontSize = 12.sp, lineHeight = 16.sp),
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
