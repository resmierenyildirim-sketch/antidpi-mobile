package com.antidpi.mobile.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var selectedProfileId: String
        get() = prefs.getString(KEY_SELECTED_PROFILE, DpiProfile.STANDARD.id) ?: DpiProfile.STANDARD.id
        set(value) = prefs.edit().putString(KEY_SELECTED_PROFILE, value).apply()

    var customSplitOffset: Int
        get() = prefs.getInt(KEY_CUSTOM_SPLIT, 2)
        set(value) = prefs.edit().putInt(KEY_CUSTOM_SPLIT, value).apply()

    var customFakeTtl: Int
        get() = prefs.getInt(KEY_CUSTOM_TTL, 4)
        set(value) = prefs.edit().putInt(KEY_CUSTOM_TTL, value).apply()

    var customDisorder: Boolean
        get() = prefs.getBoolean(KEY_CUSTOM_DISORDER, false)
        set(value) = prefs.edit().putBoolean(KEY_CUSTOM_DISORDER, value).apply()

    var customFakeData: Boolean
        get() = prefs.getBoolean(KEY_CUSTOM_FAKE_DATA, false)
        set(value) = prefs.edit().putBoolean(KEY_CUSTOM_FAKE_DATA, value).apply()

    var dohEnabled: Boolean
        get() = prefs.getBoolean(KEY_DOH_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_DOH_ENABLED, value).apply()

    var dohProvider: String
        get() = prefs.getString(KEY_DOH_PROVIDER, "Cloudflare") ?: "Cloudflare"
        set(value) = prefs.edit().putString(KEY_DOH_PROVIDER, value).apply()

    var dohCustomUrl: String
        get() = prefs.getString(KEY_DOH_CUSTOM_URL, "https://1.1.1.1/dns-query") ?: "https://1.1.1.1/dns-query"
        set(value) = prefs.edit().putString(KEY_DOH_CUSTOM_URL, value).apply()

    var gameAdBlockEnabled: Boolean
        get() = prefs.getBoolean(KEY_GAME_ADBLOCK_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_GAME_ADBLOCK_ENABLED, value).apply()

    var gameAdBlockProvider: String
        get() = prefs.getString(KEY_GAME_ADBLOCK_PROVIDER, "AdGuard Reklam Engelleyici (Önerilen)") ?: "AdGuard Reklam Engelleyici (Önerilen)"
        set(value) = prefs.edit().putString(KEY_GAME_ADBLOCK_PROVIDER, value).apply()

    var excludedApps: Set<String>
        get() = prefs.getStringSet(KEY_EXCLUDED_APPS, emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_EXCLUDED_APPS, value).apply()

    fun getActiveDnsServers(): List<String> {
        if (gameAdBlockEnabled) {
            val provider = com.antidpi.mobile.core.DnsOverHttpsResolver.ADBLOCK_PROVIDERS
                .firstOrNull { it.name == gameAdBlockProvider }
                ?: com.antidpi.mobile.core.DnsOverHttpsResolver.ADBLOCK_PROVIDERS.first()
            return listOf(provider.primaryIp, provider.secondaryIp)
        }

        if (dohEnabled) {
            val provider = com.antidpi.mobile.core.DnsOverHttpsResolver.PROVIDERS
                .firstOrNull { it.name == dohProvider }
            if (provider != null && provider.bootstrapIps.isNotEmpty()) {
                return provider.bootstrapIps
            }
        }

        return listOf("1.1.1.1", "8.8.8.8")
    }

    fun getActiveProfile(): DpiProfile {
        val id = selectedProfileId
        if (id == "preset_custom") {
            return DpiProfile(
                id = "preset_custom",
                name = "Özel Mod",
                description = "Kullanıcı tanımlı DPI parametreleri",
                mode = if (customDisorder && customFakeData) 4 else if (customFakeData) 3 else if (customDisorder) 2 else 1,
                splitOffset = customSplitOffset,
                fakeTtl = customFakeTtl,
                disorder = customDisorder,
                fakeData = customFakeData
            )
        }
        return DpiProfile.findById(id)
    }

    companion object {
        private const val PREFS_NAME = "antidpi_preferences"
        private const val KEY_SELECTED_PROFILE = "key_selected_profile"
        private const val KEY_CUSTOM_SPLIT = "key_custom_split"
        private const val KEY_CUSTOM_TTL = "key_custom_ttl"
        private const val KEY_CUSTOM_DISORDER = "key_custom_disorder"
        private const val KEY_CUSTOM_FAKE_DATA = "key_custom_fake_data"
        private const val KEY_DOH_ENABLED = "key_doh_enabled"
        private const val KEY_DOH_PROVIDER = "key_doh_provider"
        private const val KEY_DOH_CUSTOM_URL = "key_doh_custom_url"
        private const val KEY_GAME_ADBLOCK_ENABLED = "key_game_adblock_enabled"
        private const val KEY_GAME_ADBLOCK_PROVIDER = "key_game_adblock_provider"
        private const val KEY_EXCLUDED_APPS = "key_excluded_apps"
    }
}
