package com.antidpi.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.antidpi.mobile.R
import com.antidpi.mobile.core.UpdateInfo
import com.antidpi.mobile.ui.theme.*

@Composable
fun UpdateDialog(
    updateInfo: UpdateInfo,
    downloadProgress: Float?, // null if not downloading, 0..1 when downloading
    onDismiss: () -> Unit,
    onConfirmUpdate: () -> Unit
) {
    Dialog(onDismissRequest = { if (downloadProgress == null) onDismiss() }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CardDark)
                .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                .padding(22.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App Logo Badge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceDark)
                        .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                        .padding(10.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_app_logo),
                        contentDescription = "AntiDPI Logo",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Yeni Sürüm Mevcut!",
                    style = Typography.titleLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )

                Text(
                    text = updateInfo.latestVersion,
                    style = Typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                    color = AccentGreen
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Changelog Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceDark)
                        .border(1.dp, CardBorderSubtle, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = updateInfo.changelog,
                        style = Typography.bodyMedium.copy(fontSize = 12.sp, lineHeight = 17.sp),
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                if (downloadProgress != null) {
                    // Download progress indicator
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LinearProgressIndicator(
                            progress = { downloadProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = AccentGreen,
                            trackColor = SurfaceDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "%${(downloadProgress * 100).toInt()} İndiriliyor...",
                            style = Typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
                        ) {
                            Text("Daha Sonra", color = TextMuted, style = Typography.labelSmall)
                        }

                        Button(
                            onClick = onConfirmUpdate,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen, contentColor = BgDark)
                        ) {
                            Text("Güncelle", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}
