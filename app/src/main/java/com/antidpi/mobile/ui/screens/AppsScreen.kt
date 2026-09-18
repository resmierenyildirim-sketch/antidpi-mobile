package com.antidpi.mobile.ui.screens

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antidpi.mobile.data.PreferencesManager
import com.antidpi.mobile.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class InstalledAppItem(
    val packageName: String,
    val appName: String,
    val isSystem: Boolean
)

private var cachedAppsList: List<InstalledAppItem>? = null

@Composable
fun AppsScreen(
    prefs: PreferencesManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var appsList by remember { mutableStateOf<List<InstalledAppItem>>(cachedAppsList ?: emptyList()) }
    var excludedApps by remember { mutableStateOf(prefs.excludedApps) }
    var isLoading by remember { mutableStateOf(cachedAppsList == null) }

    LaunchedEffect(Unit) {
        if (cachedAppsList == null) {
            withContext(Dispatchers.IO) {
                val pm = context.packageManager
                val installed = pm.getInstalledApplications(0)
                val filtered = installed.map { appInfo ->
                    InstalledAppItem(
                        packageName = appInfo.packageName,
                        appName = pm.getApplicationLabel(appInfo).toString(),
                        isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    )
                }.sortedBy { it.appName.lowercase() }

                cachedAppsList = filtered
                withContext(Dispatchers.Main) {
                    appsList = filtered
                    isLoading = false
                }
            }
        }
    }

    val displayedApps = remember(searchQuery, appsList) {
        if (searchQuery.isBlank()) {
            appsList
        } else {
            appsList.filter {
                it.appName.contains(searchQuery, ignoreCase = true) ||
                        it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Text(
            text = "Uygulama Ayracı",
            style = Typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Seçtiğiniz uygulamalar (banka, yerel servisler vb.) DPI tünelinden hariç tutulur.",
            style = Typography.bodyMedium.copy(fontSize = 13.sp),
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Search Box
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Uygulama veya paket ara...", color = TextMuted, fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentSlate,
                unfocusedBorderColor = CardBorder,
                focusedContainerColor = CardDark,
                unfocusedContainerColor = CardDark
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentGreen, strokeWidth = 2.5.dp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayedApps, key = { it.packageName }) { app ->
                    val isExcluded = excludedApps.contains(app.packageName)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardDark)
                            .border(1.dp, if (isExcluded) RedAccent.copy(alpha = 0.5f) else CardBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.appName,
                                    style = Typography.titleLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.Medium),
                                    color = TextPrimary,
                                    maxLines = 1
                                )
                                Text(
                                    text = app.packageName,
                                    style = Typography.labelSmall,
                                    color = TextMuted,
                                    maxLines = 1
                                )
                            }

                            Switch(
                                checked = isExcluded,
                                onCheckedChange = { checked ->
                                    val newSet = excludedApps.toMutableSet()
                                    if (checked) {
                                        newSet.add(app.packageName)
                                    } else {
                                        newSet.remove(app.packageName)
                                    }
                                    excludedApps = newSet
                                    prefs.excludedApps = newSet
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = RedAccent,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = SurfaceDark,
                                    uncheckedBorderColor = CardBorder
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
