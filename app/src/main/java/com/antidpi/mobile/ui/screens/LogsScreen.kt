package com.antidpi.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antidpi.mobile.core.DpiEngineManager
import com.antidpi.mobile.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

@Composable
fun LogsScreen(
    logs: List<String>,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var isTesting by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Canlı Günlük & Tanı",
                    style = Typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = "Paket akışları ve DPI atlatma hareketleri",
                    style = Typography.labelSmall,
                    color = TextSecondary
                )
            }

            IconButton(
                onClick = { DpiEngineManager.clearLogs() },
                colors = IconButtonDefaults.iconButtonColors(contentColor = TextMuted)
            ) {
                Icon(Icons.Default.Clear, contentDescription = "Günlüğü Temizle")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Diagnostic Test Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardDark)
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hızlı Erişim Testi (Discord / Web)",
                            style = Typography.titleLarge.copy(fontSize = 14.sp),
                            color = TextPrimary
                        )
                        Text(
                            text = "DPI filtresinin aşılıp aşılmadığını test eder.",
                            style = Typography.labelSmall,
                            color = TextSecondary
                        )
                    }

                    Button(
                        onClick = {
                            isTesting = true
                            testResult = null
                            coroutineScope.launch {
                                val result = withContext(Dispatchers.IO) {
                                    try {
                                        val start = System.currentTimeMillis()
                                        val socket = Socket()
                                        socket.connect(InetSocketAddress("discord.com", 443), 4000)
                                        val elapsed = System.currentTimeMillis() - start
                                        socket.close()
                                        "discord.com:443 erişimi BAŞARILI! ($elapsed ms)"
                                    } catch (e: Exception) {
                                        "Bağlantı hatası: ${e.message}"
                                    }
                                }
                                testResult = result
                                DpiEngineManager.addLog("Test Sonucu: $result")
                                isTesting = false
                            }
                        },
                        enabled = !isTesting,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = BgDark),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BgDark, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test Et", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }

                testResult?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        style = Typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (it.contains("BAŞARILI")) GreenNeon else RedAccent
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Game AdBlock Diagnostic Card
        var isAdBlockTesting by remember { mutableStateOf(false) }
        var adBlockTestResult by remember { mutableStateOf<String?>(null) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardDark)
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Oyun Reklam Engelleme Testi",
                            style = Typography.titleLarge.copy(fontSize = 14.sp),
                            color = TextPrimary
                        )
                        Text(
                            text = "UnityAds, AdMob, AppLovin sunucu engellerini test eder.",
                            style = Typography.labelSmall,
                            color = TextSecondary
                        )
                    }

                    Button(
                        onClick = {
                            isAdBlockTesting = true
                            adBlockTestResult = null
                            coroutineScope.launch {
                                val result = withContext(Dispatchers.IO) {
                                    val testDomains = listOf("unityads.unity3d.com", "adservice.google.com", "ads.applovin.com")
                                    var blockedCount = 0
                                    val details = StringBuilder()

                                    for (domain in testDomains) {
                                        try {
                                            val addrs = java.net.InetAddress.getAllByName(domain)
                                            val isBlockedIp = addrs.any { it.hostAddress == "0.0.0.0" || it.hostAddress == "127.0.0.1" }
                                            if (isBlockedIp) {
                                                blockedCount++
                                                details.append("$domain: Engellendi (0.0.0.0)\n")
                                            } else {
                                                details.append("$domain: Erişilebilir (${addrs.firstOrNull()?.hostAddress})\n")
                                            }
                                        } catch (e: Exception) {
                                            blockedCount++
                                            details.append("$domain: Engellendi (Adres çözümlenemedi)\n")
                                        }
                                    }

                                    if (blockedCount == testDomains.size) {
                                        "REKLAM ENGELİ BAŞARILI: $blockedCount/${testDomains.size} reklam ağı engellendi!\n" + details.toString().trimEnd()
                                    } else if (blockedCount > 0) {
                                        "KISMİ ENGELLEME: $blockedCount/${testDomains.size} reklam ağı engellendi.\n" + details.toString().trimEnd()
                                    } else {
                                        "REKLAMLAR ENGELLENMEDİ: Reklam sunucuları erişilebilir durumda. (Oyun Reklam Engelleyicinin açık olduğundan emin olun.)\n" + details.toString().trimEnd()
                                    }
                                }
                                adBlockTestResult = result
                                DpiEngineManager.addLog("AdBlock Test Sonucu:\n$result")
                                isAdBlockTesting = false
                            }
                        },
                        enabled = !isAdBlockTesting,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenNeon, contentColor = BgDark),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        if (isAdBlockTesting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BgDark, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test Et", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }

                adBlockTestResult?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        style = Typography.bodyMedium.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (it.contains("BAŞARILI")) GreenNeon else if (it.contains("KISMİ")) CyanAccent else RedAccent
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Log Console Box
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF070B14))
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(logs) { log ->
                    Text(
                        text = log,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = when {
                            log.contains("tüneli kuruldu") || log.contains("BAŞARILI") -> GreenNeon
                            log.contains("hata") || log.contains("kapatıldı") -> RedAccent
                            log.contains("DPI") || log.contains("manipüle") -> CyanAccent
                            else -> TextSecondary
                        }
                    )
                }
            }
        }
    }
}
