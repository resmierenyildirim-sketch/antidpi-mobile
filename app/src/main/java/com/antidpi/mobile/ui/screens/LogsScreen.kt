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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hızlı Erişim Testi",
                            style = Typography.titleLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                            color = TextPrimary
                        )
                        Text(
                            text = "discord.com:443 üzerinden DPI filtresi testi",
                            style = Typography.labelSmall,
                            color = TextSecondary
                        )
                    }

                    OutlinedButton(
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
                                        "Erişim Başarılı (${elapsed}ms)"
                                    } catch (e: Exception) {
                                        "Bağlantı hatası: ${e.message}"
                                    }
                                }
                                testResult = result
                                DpiEngineManager.addLog("Erişim Testi: $result")
                                isTesting = false
                            }
                        },
                        enabled = !isTesting,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = TextPrimary, strokeWidth = 2.dp)
                        } else {
                            Text("Test Et", style = Typography.labelSmall.copy(fontWeight = FontWeight.Medium))
                        }
                    }
                }

                testResult?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        style = Typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            color = if (it.contains("Başarılı")) AccentGreen else RedAccent
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
                            text = "Reklam Engelleme Testi",
                            style = Typography.titleLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Reklam ağları (Unity, AdSense, AppLovin) sorgusu",
                            style = Typography.labelSmall,
                            color = TextSecondary
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            isAdBlockTesting = true
                            adBlockTestResult = null
                            coroutineScope.launch {
                                val result = withContext(Dispatchers.IO) {
                                    val testDomains = listOf(
                                        "unityads.unity3d.com",
                                        "adservice.google.com",
                                        "pagead2.googlesyndication.com",
                                        "ads.applovin.com"
                                    )
                                    var blockedCount = 0

                                    for (domain in testDomains) {
                                        try {
                                            val addrs = java.net.InetAddress.getAllByName(domain)
                                            val isBlockedIp = addrs.any { it.hostAddress == "0.0.0.0" || it.hostAddress == "127.0.0.1" }
                                            if (isBlockedIp) blockedCount++
                                        } catch (e: Exception) {
                                            blockedCount++
                                        }
                                    }

                                    if (blockedCount == testDomains.size) {
                                        "Engelleme Başarılı ($blockedCount/${testDomains.size} reklam ağı blokeli)"
                                    } else {
                                        "Kısmi veya Engellenmedi ($blockedCount/${testDomains.size})"
                                    }
                                }
                                adBlockTestResult = result
                                DpiEngineManager.addLog("AdBlock Testi: $result")
                                isAdBlockTesting = false
                            }
                        },
                        enabled = !isAdBlockTesting,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        if (isAdBlockTesting) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = TextPrimary, strokeWidth = 2.dp)
                        } else {
                            Text("Test Et", style = Typography.labelSmall.copy(fontWeight = FontWeight.Medium))
                        }
                    }
                }

                adBlockTestResult?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        style = Typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            color = if (it.contains("Başarılı")) AccentGreen else RedAccent
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
                .background(SurfaceDark)
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
                            log.contains("aktif") || log.contains("Başarılı") -> AccentGreen
                            log.contains("hata") || log.contains("kapatıldı") -> RedAccent
                            log.contains("DPI") -> AccentSlate
                            else -> TextSecondary
                        }
                    )
                }
            }
        }
    }
}
