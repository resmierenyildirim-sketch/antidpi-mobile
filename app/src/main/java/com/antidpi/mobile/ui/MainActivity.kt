package com.antidpi.mobile.ui

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.antidpi.mobile.AntiDpiApp
import com.antidpi.mobile.core.AppUpdater
import com.antidpi.mobile.core.DpiEngineManager
import com.antidpi.mobile.core.DpiVpnService
import com.antidpi.mobile.core.UpdateInfo
import com.antidpi.mobile.ui.components.UpdateDialog
import com.antidpi.mobile.ui.screens.*
import com.antidpi.mobile.ui.theme.*
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Ana Sayfa", Icons.Default.Shield)
    object Profiles : Screen("profiles", "Profiller", Icons.Default.Tune)
    object Dns : Screen("dns", "Şifreli DNS", Icons.Default.Dns)
    object Apps : Screen("apps", "Uygulamalar", Icons.Default.Apps)
    object Logs : Screen("logs", "Günlük", Icons.Default.Terminal)
}

class MainActivity : ComponentActivity() {

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            DpiVpnService.start(this)
        } else {
            Toast.makeText(this, "Yerel koruma için VPN izni gereklidir.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = (application as AntiDpiApp).preferencesManager

        setContent {
            AntiDpiTheme {
                val navController = rememberNavController()
                val coroutineScope = rememberCoroutineScope()
                val isConnected by DpiEngineManager.isConnected.collectAsState()
                val stats by DpiEngineManager.stats.collectAsState()
                val logs by DpiEngineManager.logs.collectAsState()
                var activeProfile by remember { mutableStateOf(prefs.getActiveProfile()) }

                var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
                var downloadProgress by remember { mutableStateOf<Float?>(null) }

                // Check for updates and remote profiles on launch
                LaunchedEffect(Unit) {
                    val info = AppUpdater.checkForUpdate("1.0.0")
                    if (info != null && info.hasUpdate) {
                        updateInfo = info
                    }

                    val (announcement, _) = AppUpdater.fetchRemoteProfiles()
                    if (!announcement.isNullOrBlank()) {
                        DpiEngineManager.addLog(announcement)
                    }
                }

                // Show In-App Update Dialog if update is found
                updateInfo?.let { info ->
                    UpdateDialog(
                        updateInfo = info,
                        downloadProgress = downloadProgress,
                        onDismiss = { updateInfo = null },
                        onConfirmUpdate = {
                            coroutineScope.launch {
                                AppUpdater.downloadAndInstall(
                                    context = this@MainActivity,
                                    apkUrl = info.downloadUrl,
                                    onProgress = { downloadProgress = it },
                                    onComplete = {
                                        downloadProgress = null
                                        updateInfo = null
                                    },
                                    onError = { err ->
                                        downloadProgress = null
                                        Toast.makeText(this@MainActivity, err, Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        }
                    )
                }

                val items = listOf(
                    Screen.Home,
                    Screen.Profiles,
                    Screen.Dns,
                    Screen.Apps,
                    Screen.Logs
                )

                Scaffold(
                    bottomBar = {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route

                        NavigationBar(
                            containerColor = CardDark,
                            tonalElevation = 8.dp
                        ) {
                            items.forEach { screen ->
                                NavigationBarItem(
                                    icon = { Icon(screen.icon, contentDescription = screen.title) },
                                    label = { Text(screen.title, style = Typography.labelSmall) },
                                    selected = currentRoute == screen.route,
                                    onClick = {
                                        if (currentRoute != screen.route) {
                                            navController.navigate(screen.route) {
                                                popUpTo(Screen.Home.route) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = CyanAccent,
                                        selectedTextColor = CyanAccent,
                                        unselectedIconColor = TextMuted,
                                        unselectedTextColor = TextMuted,
                                        indicatorColor = CyanAccent.copy(alpha = 0.15f)
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Home.route) {
                            HomeScreen(
                                isConnected = isConnected,
                                stats = stats,
                                activeProfile = activeProfile,
                                onToggleClick = {
                                    handleToggleVpn()
                                },
                                onNavigateToProfiles = {
                                    navController.navigate(Screen.Profiles.route)
                                }
                            )
                        }

                        composable(Screen.Profiles.route) {
                            ProfilesScreen(
                                prefs = prefs,
                                onProfileSelected = { newProfile ->
                                    activeProfile = newProfile
                                    if (isConnected) {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Profil güncellendi. Yeniden başlatılıyor...",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        DpiVpnService.stop(this@MainActivity)
                                        DpiVpnService.start(this@MainActivity)
                                    }
                                }
                            )
                        }

                        composable(Screen.Dns.route) {
                            DnsScreen(prefs = prefs)
                        }

                        composable(Screen.Apps.route) {
                            AppsScreen(prefs = prefs)
                        }

                        composable(Screen.Logs.route) {
                            LogsScreen(logs = logs)
                        }
                    }
                }
            }
        }
    }

    private fun handleToggleVpn() {
        if (DpiEngineManager.isConnected.value) {
            DpiVpnService.stop(this)
        } else {
            val vpnIntent = VpnService.prepare(this)
            if (vpnIntent != null) {
                vpnPermissionLauncher.launch(vpnIntent)
            } else {
                DpiVpnService.start(this)
            }
        }
    }
}
