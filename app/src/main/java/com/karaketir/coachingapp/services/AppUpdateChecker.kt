package com.karaketir.coachingapp.services

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.karaketir.coachingapp.BuildConfig
import kotlinx.coroutines.tasks.await

data class AppUpdateInfo(
    val latestVersionCode: Int,
    val forceUpdate: Boolean = true,
    val releaseNotes: String? = null,
    val storeUrl: String? = null,
)

sealed class AppUpdateStatus {
    data object UpToDate : AppUpdateStatus()
    data class UpdateAvailable(val info: AppUpdateInfo) : AppUpdateStatus()
}

object AppUpdateChecker {

    private const val COLLECTION = "VersionCode"
    private const val DOCUMENT_ID = "60qzy2yuxMwCCau44HdF"
    private const val FIELD_LATEST_VERSION = "latestVersion"
    private const val FIELD_FORCE_UPDATE = "forceUpdate"
    private const val FIELD_RELEASE_NOTES = "releaseNotes"
    private const val FIELD_STORE_URL = "storeUrl"

    const val PLAY_STORE_PACKAGE = "com.karaketir.coachingapp"
    private const val PLAY_STORE_WEB_URL =
        "https://play.google.com/store/apps/details?id=$PLAY_STORE_PACKAGE"

    val installedVersionCode: Int
        get() = BuildConfig.VERSION_CODE

    val installedVersionName: String
        get() = BuildConfig.VERSION_NAME

    suspend fun checkForUpdate(
        db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    ): AppUpdateStatus {
        return try {
            val snapshot = db.collection(COLLECTION).document(DOCUMENT_ID).get().await()
            val latestVersionCode = snapshot.getLong(FIELD_LATEST_VERSION)?.toInt()
                ?: snapshot.getDouble(FIELD_LATEST_VERSION)?.toInt()
                ?: snapshot.getString(FIELD_LATEST_VERSION)?.toIntOrNull()
                ?: return AppUpdateStatus.UpToDate

            if (installedVersionCode >= latestVersionCode) {
                AppUpdateStatus.UpToDate
            } else {
                AppUpdateStatus.UpdateAvailable(
                    AppUpdateInfo(
                        latestVersionCode = latestVersionCode,
                        forceUpdate = snapshot.getBoolean(FIELD_FORCE_UPDATE) ?: true,
                        releaseNotes = snapshot.getString(FIELD_RELEASE_NOTES)?.trim()
                            ?.takeIf { it.isNotEmpty() },
                        storeUrl = snapshot.getString(FIELD_STORE_URL)?.trim()
                            ?.takeIf { it.isNotEmpty() },
                    )
                )
            }
        } catch (_: Exception) {
            AppUpdateStatus.UpToDate
        }
    }

    fun openStorePage(context: Context, storeUrl: String? = null) {
        val webUrl = storeUrl ?: PLAY_STORE_WEB_URL
        val marketUri = Uri.parse("market://details?id=$PLAY_STORE_PACKAGE")
        val marketIntent = Intent(Intent.ACTION_VIEW, marketUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (marketIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(marketIntent)
        } else {
            openLink(webUrl, context)
        }
    }
}
