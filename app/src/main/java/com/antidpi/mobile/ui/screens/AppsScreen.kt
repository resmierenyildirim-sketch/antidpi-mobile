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

@Composable
fun AppsScreen(
    prefs: PreferencesManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var appsList by remember { mutableStateOf<List<InstalledAppItem>>(emptyList()) }
    var excludedApps by remember { mutableStateOf(prefs.excludedApps) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val installed = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            val filtered = installed.map { appInfo ->
                InstalledAppItem(
                    packageName = appInfo.packageName,
                    appName = pm.getApplicationLabel(appInfo).toString(),
                    isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                )
            }.sortedBy { it.appName.lowercase() }

            withContext(Dispatchers.Main) {
                appsList = filtered
                isLoading = false
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
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Uygulama Filtresi (Hariç Tutma)",
            style = Typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )
        Text(
            text = "Bankacılık (Garanti, İşCep vb.) veya hassas uygulamaları DPI tünelinden hariç tutarak normal bağlantınız üzerinden çalıştırabilirsiniz.",
            style = Typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Search Box
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Uygulama veya paket ara...", color = TextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyanAccent,
                unfocusedBorderColor = CardBorder,
                focusedContainerColor = CardDark,
                unfocusedContainerColor = CardDark
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CyanAccent)
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
                                    style = Typography.titleLarge.copy(fontSize = 15.sp),
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
                                    checkedThumbColor = RedAccent,
                                    checkedTrackColor = RedAccent.copy(alpha = 0.4f)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
