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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antidpi.mobile.data.DpiProfile
import com.antidpi.mobile.data.PreferencesManager
import com.antidpi.mobile.ui.theme.*

@Composable
fun ProfilesScreen(
    prefs: PreferencesManager,
    onProfileSelected: (DpiProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentProfileId by remember { mutableStateOf(prefs.selectedProfileId) }
    var customSplit by remember { mutableFloatStateOf(prefs.customSplitOffset.toFloat()) }
    var customTtl by remember { mutableFloatStateOf(prefs.customFakeTtl.toFloat()) }
    var customDisorder by remember { mutableStateOf(prefs.customDisorder) }
    var customFakeData by remember { mutableStateOf(prefs.customFakeData) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Operatör & DPI Profilleri",
                style = Typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Text(
                text = "İnternet sağlayıcınıza veya ağ türünüze en uygun DPI atlatma profilini seçin.",
                style = Typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        items(DpiProfile.PRESETS) { profile ->
            val isSelected = currentProfileId == profile.id
            ProfileItemCard(
                profile = profile,
                isSelected = isSelected,
                onClick = {
                    currentProfileId = profile.id
                    prefs.selectedProfileId = profile.id
                    onProfileSelected(profile)
                }
            )
        }

        // Custom Profile Option
        item {
            val isCustomSelected = currentProfileId == "preset_custom"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardDark)
                    .border(
                        width = 1.5.dp,
                        color = if (isCustomSelected) CyanAccent else CardBorder,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        currentProfileId = "preset_custom"
                        prefs.selectedProfileId = "preset_custom"
                        onProfileSelected(prefs.getActiveProfile())
                    }
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RadioButton(
                                selected = isCustomSelected,
                                onClick = {
                                    currentProfileId = "preset_custom"
                                    prefs.selectedProfileId = "preset_custom"
                                    onProfileSelected(prefs.getActiveProfile())
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = CyanAccent,
                                    unselectedColor = TextMuted
                                )
                            )
                            Column {
                                Text(
                                    text = "Özel Mod (Gelişmiş)",
                                    style = Typography.titleLarge.copy(fontSize = 16.sp),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Kendi DPI parçalama ve sahte paket parametrelerinizi belirleyin.",
                                    style = Typography.bodyMedium.copy(fontSize = 12.sp),
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    if (isCustomSelected) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = CardBorder)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Split Offset Slider
                        Text(
                            text = "SNI Parçalama Ofseti: ${customSplit.toInt()} Bayt",
                            style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = CyanAccent
                        )
                        Slider(
                            value = customSplit,
                            onValueChange = {
                                customSplit = it
                                prefs.customSplitOffset = it.toInt()
                            },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = CyanAccent,
                                activeTrackColor = CyanAccent
                            )
                        )

                        // Fake TTL Slider
                        Text(
                            text = "Sahte Paket TTL (Hop Sınırı): ${customTtl.toInt()}",
                            style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = CyanAccent
                        )
                        Slider(
                            value = customTtl,
                            onValueChange = {
                                customTtl = it
                                prefs.customFakeTtl = it.toInt()
                            },
                            valueRange = 1f..15f,
                            steps = 13,
                            colors = SliderDefaults.colors(
                                thumbColor = CyanAccent,
                                activeTrackColor = CyanAccent
                            )
                        )

                        // Switches
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Sırasız Gönderim (Disorder)", style = Typography.bodyMedium, color = TextPrimary)
                            Switch(
                                checked = customDisorder,
                                onCheckedChange = {
                                    customDisorder = it
                                    prefs.customDisorder = it
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Sahte Paket Enjeksiyonu (Fake)", style = Typography.bodyMedium, color = TextPrimary)
                            Switch(
                                checked = customFakeData,
                                onCheckedChange = {
                                    customFakeData = it
                                    prefs.customFakeData = it
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileItemCard(
    profile: DpiProfile,
    isSelected: Boolean,
    onClick: () -> Unit
) {
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
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = onClick,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = CyanAccent,
                        unselectedColor = TextMuted
                    )
                )
                Column {
                    Text(
                        text = profile.name,
                        style = Typography.titleLarge.copy(fontSize = 16.sp),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = profile.description,
                        style = Typography.bodyMedium.copy(fontSize = 12.sp),
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
