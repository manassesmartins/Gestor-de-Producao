package com.example.ui

import android.content.Context
import android.util.Log
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest

sealed class UpdateStatus {
    object Idle : UpdateStatus()
    object Checking : UpdateStatus()
    data class UpdateAvailable(
        val version: String,
        val changelog: String,
        val downloadUrl: String,
        val type: UpdateType,
        val itemName: String,
        val date: String,
        val sha256: String = ""
    ) : UpdateStatus()
    object UpToDate : UpdateStatus()
    data class Downloading(val progress: Float, val statusText: String = "") : UpdateStatus()
    data class Downloaded(val apkFile: File) : UpdateStatus()
    data class Error(val message: String) : UpdateStatus()
}

enum class UpdateType {
    RELEASE,
    COMMIT,
    RAW
}

class GitHubUpdater(private val context: Context) {

    private val sharedPrefs = context.getSharedPreferences("ms_producao_github_updater_prefs", Context.MODE_PRIVATE)
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(15, java.util.concurrent.TimeUnit.MINUTES)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    init {
        val currentOwner = sharedPrefs.getString("github_owner", "")
        if (currentOwner.isNullOrBlank() || currentOwner == "ManassesMartins") {
            sharedPrefs.edit().putString("github_owner", "manassesmartins").apply()
        }
        val currentRepo = sharedPrefs.getString("github_repo", "")
        if (currentRepo.isNullOrBlank() || currentRepo == "workspace-ms-producao-valeriacalc") {
            sharedPrefs.edit().putString("github_repo", "Gestor-de-Producao").apply()
        }
        val currentBranch = sharedPrefs.getString("github_branch", "")
        if (currentBranch.isNullOrBlank()) {
            sharedPrefs.edit().putString("github_branch", "main").apply()
        }
    }

    private val _status = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val status: StateFlow<UpdateStatus> = _status.asStateFlow()

    private var latestCheckedSha: String = ""

    var owner: String
        get() = sharedPrefs.getString("github_owner", "manassesmartins")?.takeIf { it.isNotBlank() } ?: "manassesmartins"
        set(value) {
            sharedPrefs.edit().putString("github_owner", value.trim()).apply()
        }

    var repo: String
        get() = sharedPrefs.getString("github_repo", "Gestor-de-Producao")?.takeIf { it.isNotBlank() } ?: "Gestor-de-Producao"
        set(value) {
            sharedPrefs.edit().putString("github_repo", value.trim()).apply()
        }

    var branch: String
        get() = sharedPrefs.getString("github_branch", "main")?.takeIf { it.isNotBlank() } ?: "main"
        set(value) {
            sharedPrefs.edit().putString("github_branch", value.trim()).apply()
        }

    var apkPath: String
        get() = sharedPrefs.getString("github_apk_path", "app-debug.apk")?.takeIf { it.isNotBlank() } ?: "app-debug.apk"
        set(value) {
            sharedPrefs.edit().putString("github_apk_path", value.trim()).apply()
        }

    var lastNotifiedVersion: String
        get() = sharedPrefs.getString("last_notified_version", "") ?: ""
        set(value) {
            sharedPrefs.edit().putString("last_notified_version", value).apply()
        }

    var versionJsonPath: String
        get() = sharedPrefs.getString("github_version_json_path", "version.json")?.takeIf { it.isNotBlank() } ?: "version.json"
        set(value) {
            sharedPrefs.edit().putString("github_version_json_path", value.trim()).apply()
        }

    fun getLocalVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }

    fun clearStatus() {
        _status.value = UpdateStatus.Idle
    }

    suspend fun checkForUpdates(forceNotify: Boolean = false) = withContext(Dispatchers.IO) {
        _status.value = UpdateStatus.Checking
        try {
            val currentVersion = getLocalVersion()

            var foundCustomJsonUpdate = false
            val customJsonUrl = "https://raw.githubusercontent.com/$owner/$repo/$branch/$versionJsonPath"
            val customJsonRequest = Request.Builder()
                .url(customJsonUrl)
                .addHeader("User-Agent", "Mozilla/5.0")
                .build()

            var pendingSha256 = ""

            try {
                client.newCall(customJsonRequest).execute().use { response ->
                    if (response.isSuccessful) {
                        val bodyString = response.body?.string()
                        if (!bodyString.isNullOrEmpty()) {
                            val latestVersion = "\"version\"\\s*:\\s*\"([^\"]+)\"".toRegex().find(bodyString)?.groupValues?.get(1)?.trim() ?: ""
                            if (latestVersion.isNotEmpty()) {
                                val rawDownloadUrl = "\"downloadUrl\"\\s*:\\s*\"([^\"]+)\"".toRegex().find(bodyString)?.groupValues?.get(1)?.trim() ?: ""
                                val downloadUrl = if (rawDownloadUrl.isNotEmpty()) {
                                    rawDownloadUrl
                                } else {
                                    "https://raw.githubusercontent.com/$owner/$repo/$branch/$apkPath"
                                }

                                pendingSha256 = "\"sha256\"\\s*:\\s*\"([^\"]+)\"".toRegex().find(bodyString)?.groupValues?.get(1)?.trim() ?: ""

                                var changelog = "Nova versão disponível no repositório."
                                val changelogKeyIndex = bodyString.indexOf("\"changelog\"")
                                if (changelogKeyIndex != -1) {
                                    val colonIndex = bodyString.indexOf(":", changelogKeyIndex)
                                    if (colonIndex != -1) {
                                        val startQuoteIndex = bodyString.indexOf("\"", colonIndex)
                                        if (startQuoteIndex != -1) {
                                            val downloadUrlTokenIndex = bodyString.indexOf("\"downloadUrl\"", startQuoteIndex)
                                            val endIndex = if (downloadUrlTokenIndex != -1) {
                                                var lastQuote = bodyString.lastIndexOf("\"", downloadUrlTokenIndex)
                                                while (lastQuote > startQuoteIndex && (bodyString[lastQuote] == '"' || bodyString[lastQuote].isWhitespace() || bodyString[lastQuote] == ',')) {
                                                    lastQuote--
                                                }
                                                lastQuote + 1
                                            } else {
                                                val braceIndex = bodyString.lastIndexOf("}")
                                                if (braceIndex != -1) {
                                                    var lastQuote = braceIndex - 1
                                                    while (lastQuote > startQuoteIndex && (bodyString[lastQuote].isWhitespace() || bodyString[lastQuote] == '"')) {
                                                        lastQuote--
                                                    }
                                                    lastQuote + 1
                                                } else {
                                                    bodyString.length
                                                }
                                            }
                                            if (endIndex > startQuoteIndex + 1) {
                                                val extracted = bodyString.substring(startQuoteIndex + 1, endIndex).trim()
                                                changelog = if (extracted.endsWith("\"")) extracted.dropLast(1) else extracted
                                            }
                                        }
                                    }
                                }

                                val hasNewerVersion = isNewerVersion(latestVersion, currentVersion)
                                if (hasNewerVersion) {
                                    if (forceNotify || latestVersion != lastNotifiedVersion) {
                                        sharedPrefs.edit().putString("pending_notified_version", latestVersion).apply()
                                        sharedPrefs.edit().putString("pending_commit_sha", "").apply()
                                        _status.value = UpdateStatus.UpdateAvailable(
                                            version = latestVersion,
                                            changelog = changelog,
                                            downloadUrl = downloadUrl,
                                            type = UpdateType.RAW,
                                            itemName = "Atualização via JSON",
                                            date = "",
                                            sha256 = pendingSha256
                                        )
                                        foundCustomJsonUpdate = true
                                    }
                                } else {
                                    sharedPrefs.edit().remove("pending_notified_version").remove("pending_commit_sha").apply()
                                    _status.value = if (forceNotify) UpdateStatus.UpToDate else UpdateStatus.Idle
                                    foundCustomJsonUpdate = true
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("GitHubUpdater", "Failed to check custom version.json, proceeding to releases", e)
            }

            if (foundCustomJsonUpdate) return@withContext

            val releaseUrl = "https://api.github.com/repos/$owner/$repo/releases/latest"
            val releaseRequest = Request.Builder()
                .url(releaseUrl)
                .addHeader("Accept", "application/vnd.github+json")
                .addHeader("User-Agent", "Mozilla/5.0")
                .build()

            var foundReleaseUpdate = false

            try {
                client.newCall(releaseRequest).execute().use { response ->
                    if (response.isSuccessful) {
                        val bodyString = response.body?.string()
                        if (!bodyString.isNullOrEmpty()) {
                            val releaseObj = JSONObject(bodyString)
                            val tagName = releaseObj.optString("tag_name", "").removePrefix("v")
                            val releaseName = releaseObj.optString("name", "Nova Versão")
                            val changelog = releaseObj.optString("body", "Sem changelog fornecido.")
                            val publishedAt = releaseObj.optString("published_at", "").take(10)
                            
                            var apkDownloadUrl = ""
                            val assetsArray = releaseObj.optJSONArray("assets")
                            if (assetsArray != null) {
                                for (i in 0 until assetsArray.length()) {
                                    val assetObj = assetsArray.getJSONObject(i)
                                    val assetName = assetObj.optString("name", "")
                                    if (assetName.endsWith(".apk")) {
                                        apkDownloadUrl = assetObj.optString("browser_download_url", "")
                                        break
                                    }
                                }
                            }

                            if (apkDownloadUrl.isEmpty()) {
                                apkDownloadUrl = "https://raw.githubusercontent.com/$owner/$repo/$branch/$apkPath"
                            }

                            val hasNewerVersion = isNewerVersion(tagName, currentVersion)
                            if (hasNewerVersion || (forceNotify && tagName.isNotEmpty())) {
                                if (forceNotify || tagName != lastNotifiedVersion) {
                                    sharedPrefs.edit().putString("pending_notified_version", tagName).apply()
                                    sharedPrefs.edit().putString("pending_commit_sha", "").apply()
                                    _status.value = UpdateStatus.UpdateAvailable(
                                        version = tagName,
                                        changelog = changelog,
                                        downloadUrl = apkDownloadUrl,
                                        type = UpdateType.RELEASE,
                                        itemName = releaseName,
                                        date = publishedAt
                                    )
                                    foundReleaseUpdate = true
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("GitHubUpdater", "Failed to check releases, will attempt commits/fallback", e)
            }

            if (foundReleaseUpdate) return@withContext

            _status.value = if (forceNotify) UpdateStatus.UpToDate else UpdateStatus.Idle
        } catch (e: Exception) {
            Log.e("GitHubUpdater", "Error checking for updates", e)
            if (forceNotify) {
                _status.value = UpdateStatus.Error("Erro de conexão: ${e.localizedMessage ?: "Verifique sua internet"}")
            } else {
                _status.value = UpdateStatus.Idle
            }
        }
    }

    suspend fun downloadApk(url: String, expectedSha256: String = ""): File? = withContext(Dispatchers.IO) {
        _status.value = UpdateStatus.Downloading(0.01f, "Conectando ao servidor para baixar atualização...")
        try {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    _status.value = UpdateStatus.Error("Falha ao baixar arquivo: HTTP ${response.code}")
                    return@withContext null
                }

                val body = response.body
                if (body == null) {
                    _status.value = UpdateStatus.Error("Arquivo vazio retornado pelo servidor")
                    return@withContext null
                }

                val contentLength = body.contentLength()
                val cacheFile = File(context.cacheDir, "update.apk")
                if (cacheFile.exists()) {
                    cacheFile.delete()
                }

                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalBytesRead = 0L
                val digest = MessageDigest.getInstance("SHA-256")

                body.byteStream().use { inputStream ->
                    FileOutputStream(cacheFile).use { outputStream ->
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                            digest.update(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead
                            val downloadedMb = totalBytesRead.toDouble() / (1024.0 * 1024.0)

                            if (contentLength > 0) {
                                val progress = totalBytesRead.toFloat() / contentLength.toFloat()
                                val totalMb = contentLength.toDouble() / (1024.0 * 1024.0)
                                val statusText = String.format(java.util.Locale.US, "Baixando: %.2f MB de %.2f MB (%.0f%%)", downloadedMb, totalMb, progress * 100)
                                _status.value = UpdateStatus.Downloading(progress.coerceIn(0.01f, 0.99f), statusText)
                            } else {
                                val statusText = String.format(java.util.Locale.US, "Baixando: %.2f MB recebidos (tamanho total não informado)", downloadedMb)
                                _status.value = UpdateStatus.Downloading(-1f, statusText)
                            }
                        }
                        outputStream.flush()
                    }
                }

                if (expectedSha256.isNotEmpty()) {
                    val computedHash = digest.digest().joinToString("") { "%02x".format(it) }
                    if (computedHash != expectedSha256.lowercase()) {
                        cacheFile.delete()
                        _status.value = UpdateStatus.Error("Falha na verificação de integridade: o hash do arquivo baixado não corresponde ao esperado.")
                        return@withContext null
                    }
                }

                _status.value = UpdateStatus.Downloaded(cacheFile)
                cacheFile
            }
        } catch (e: Exception) {
            Log.e("GitHubUpdater", "Error downloading APK", e)
            _status.value = UpdateStatus.Error("Falha no download: ${e.localizedMessage ?: "Verifique sua rede"}")
            null
        }
    }

    fun installApk(apkFile: File) {
        try {
            if (!apkFile.exists()) {
                _status.value = UpdateStatus.Error("Arquivo APK não encontrado localmente.")
                return
            }

            val authority = "${context.packageName}.fileprovider"
            val apkUri: Uri = FileProvider.getUriForFile(context, authority, apkFile)

            val pendingVer = sharedPrefs.getString("pending_notified_version", "") ?: ""
            if (pendingVer.isNotEmpty()) {
                sharedPrefs.edit().putString("last_notified_version", pendingVer).apply()
            }

            val pendingSha = sharedPrefs.getString("pending_commit_sha", "") ?: ""
            val shaToSave = if (pendingSha.isNotEmpty()) pendingSha else latestCheckedSha
            if (shaToSave.isNotEmpty()) {
                sharedPrefs.edit().putString("last_checked_commit_sha", shaToSave).apply()
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(settingsIntent)
                    _status.value = UpdateStatus.Error("Por favor, autorize a instalação de fontes desconhecidas para este aplicativo nas configurações que foram abertas e tente novamente.")
                    return
                }
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("GitHubUpdater", "Error installing APK", e)
            _status.value = UpdateStatus.Error("Erro ao iniciar instalador: ${e.localizedMessage}")
        }
    }

    private fun isNewerVersion(remote: String, local: String): Boolean {
        if (remote.isEmpty() || remote == local) return false
        
        val remoteParts = remote.split(".").map { it.toIntOrNull() ?: 0 }
        val localParts = local.split(".").map { it.toIntOrNull() ?: 0 }

        val length = maxOf(remoteParts.size, localParts.size)
        for (i in 0 until length) {
            val r = remoteParts.getOrElse(i) { 0 }
            val l = localParts.getOrElse(i) { 0 }
            if (r > l) return true
            if (r < l) return false
        }
        return false
    }
}
