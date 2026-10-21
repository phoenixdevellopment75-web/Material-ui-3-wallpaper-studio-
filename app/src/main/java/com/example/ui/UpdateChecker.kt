package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class GitHubReleaseInfo(
    val isUpdateAvailable: Boolean = false,
    val latestVersion: String = "",
    val releaseNotes: String = "",
    val releaseHtmlUrl: String = ""
)

/**
 * Lightweight, zero-leak GitHub Release Update Checker.
 * Queries latest release tag on GitHub API via background Dispatchers.IO.
 */
object UpdateChecker {

    private const val GITHUB_API_URL =
        "https://api.github.com/repos/phoenixdevellopment75-web/Material-ui-3-wallpaper-studio-/releases/latest"
    const val CURRENT_VERSION_TAG = "v2.6.0"

    /**
     * Checks whether a new version exists on GitHub.
     */
    suspend fun checkForUpdates(): GitHubReleaseInfo = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(GITHUB_API_URL)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "WallpaperStudio-Android")
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.use { it.readText() }
                val json = JSONObject(response)

                val tagName = json.optString("tag_name", "")
                val htmlUrl = json.optString("html_url", "https://github.com/phoenixdevellopment75-web/Material-ui-3-wallpaper-studio-/releases")
                val body = json.optString("body", "")

                val isNewer = isRemoteVersionNewer(remoteTag = tagName, currentTag = CURRENT_VERSION_TAG)

                GitHubReleaseInfo(
                    isUpdateAvailable = isNewer,
                    latestVersion = tagName,
                    releaseNotes = body,
                    releaseHtmlUrl = htmlUrl
                )
            } else {
                GitHubReleaseInfo()
            }
        } catch (e: Exception) {
            GitHubReleaseInfo()
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * Compares semver version tags e.g. "v2.5.1" vs "v2.5.0".
     */
    fun isRemoteVersionNewer(remoteTag: String, currentTag: String): Boolean {
        if (remoteTag.isBlank()) return false
        val cleanRemote = remoteTag.removePrefix("v").removePrefix("V").trim()
        val cleanCurrent = currentTag.removePrefix("v").removePrefix("V").trim()

        val remoteParts = cleanRemote.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(remoteParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val r = remoteParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }
}

/**
 * Subtle floating pill banner displayed directly below the top app bar when an update is found.
 */
@Composable
fun UpdateBannerPill(
    releaseInfo: GitHubReleaseInfo,
    onDismiss: () -> Unit,
    context: Context,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = releaseInfo.isUpdateAvailable,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(dampingRatio = 0.65f, stiffness = 360f)
        ) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 6.dp,
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            ),
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(CircleShape)
                .testTag("github_update_banner")
        ) {
            Row(
                modifier = Modifier
                    .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SystemUpdate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = "Update available: ${releaseInfo.latestVersion}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(releaseInfo.releaseHtmlUrl)).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(browserIntent)
                        }
                ) {
                    Text(
                        text = "Get",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
