package com.antidpi.mobile.core

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import com.antidpi.mobile.data.DpiProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

data class UpdateInfo(
    val hasUpdate: Boolean,
    val latestVersion: String,
    val changelog: String,
    val downloadUrl: String
)

object AppUpdater {
    private const val TAG = "AppUpdater"
    const val GITHUB_REPO = "resmierenyildirim-sketch/antidpi-mobile"
    private const val RELEASES_API = "https://api.github.com/repos/$GITHUB_REPO/releases/latest"
    private const val PROFILES_RAW_URL = "https://raw.githubusercontent.com/$GITHUB_REPO/main/profiles.json"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun checkForUpdate(currentVersionName: String): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(RELEASES_API)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "AntiDPI-Mobile")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w(TAG, "GitHub API returned code ${response.code}")
                return@withContext null
            }

            val body = response.body?.string() ?: return@withContext null
            val json = JSONObject(body)

            val tagName = json.optString("tag_name", "").replace("v", "").trim()
            val changelog = json.optString("body", "Yeni özellikler ve hata düzeltmeleri.")

            val assets = json.optJSONArray("assets")
            var downloadUrl = ""
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        downloadUrl = asset.optString("browser_download_url", "")
                        break
                    }
                }
            }

            val isNewer = isVersionNewer(tagName, currentVersionName)
            return@withContext UpdateInfo(
                hasUpdate = isNewer && downloadUrl.isNotEmpty(),
                latestVersion = "v$tagName",
                changelog = changelog,
                downloadUrl = downloadUrl
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check update", e)
            return@withContext null
        }
    }

    suspend fun downloadAndInstall(
        context: Context,
        apkUrl: String,
        onProgress: (Float) -> Unit,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(apkUrl).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                withContext(Dispatchers.Main) { onError("İndirme başarısız: HTTP ${response.code}") }
                return@withContext
            }

            val responseBody = response.body ?: run {
                withContext(Dispatchers.Main) { onError("Boş dosya indirildi") }
                return@withContext
            }

            val totalBytes = responseBody.contentLength()
            val updateDir = File(context.cacheDir, "updates")
            if (!updateDir.exists()) updateDir.mkdirs()

            val apkFile = File(updateDir, "AntiDPI-update.apk")
            if (apkFile.exists()) apkFile.delete()

            val inputStream = responseBody.byteStream()
            val outputStream = FileOutputStream(apkFile)

            val buffer = ByteArray(8192)
            var bytesRead: Int
            var totalRead = 0L

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalRead += bytesRead
                if (totalBytes > 0) {
                    val progress = totalRead.toFloat() / totalBytes.toFloat()
                    withContext(Dispatchers.Main) { onProgress(progress) }
                }
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            withContext(Dispatchers.Main) {
                onComplete()
                triggerInstall(context, apkFile)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Download failed", e)
            withContext(Dispatchers.Main) { onError("İndirme hatası: ${e.message}") }
        }
    }

    private fun triggerInstall(context: Context, apkFile: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Install failed", e)
        }
    }

    suspend fun fetchRemoteProfiles(): Pair<String?, List<DpiProfile>?> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(PROFILES_RAW_URL).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext Pair(null, null)

            val body = response.body?.string() ?: return@withContext Pair(null, null)
            val json = JSONObject(body)
            val announcement = if (json.has("announcement")) json.getString("announcement") else null

            val profilesArray = json.optJSONArray("profiles") ?: return@withContext Pair(announcement, null)
            val list = mutableListOf<DpiProfile>()

            for (i in 0 until profilesArray.length()) {
                val p = profilesArray.getJSONObject(i)
                list.add(
                    DpiProfile(
                        id = p.getString("id"),
                        name = p.getString("name"),
                        description = p.getString("description"),
                        mode = p.getInt("mode"),
                        splitOffset = p.optInt("splitOffset", 2),
                        fakeTtl = p.optInt("fakeTtl", 4),
                        disorder = p.optBoolean("disorder", false),
                        fakeData = p.optBoolean("fakeData", false),
                        fakeHost = p.optString("fakeHost", "www.google.com")
                    )
                )
            }
            return@withContext Pair(announcement, list)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch remote profiles", e)
            return@withContext Pair(null, null)
        }
    }

    private fun isVersionNewer(remote: String, current: String): Boolean {
        try {
            val rParts = remote.replace("v", "").split(".").map { it.toIntOrNull() ?: 0 }
            val cParts = current.replace("v", "").split(".").map { it.toIntOrNull() ?: 0 }

            val maxLen = maxOf(rParts.size, cParts.size)
            for (i in 0 until maxLen) {
                val r = rParts.getOrElse(i) { 0 }
                val c = cParts.getOrElse(i) { 0 }
                if (r > c) return true
                if (r < c) return false
            }
            return false
        } catch (e: Exception) {
            return remote != current
        }
    }
}
