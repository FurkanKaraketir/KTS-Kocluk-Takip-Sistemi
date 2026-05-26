package com.karaketir.coachingapp.services

import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.karaketir.coachingapp.R

class AppUpdatePrompt(
    private val fragment: Fragment,
    private val updateOverlay: MaterialCardView,
    private val updateTitle: TextView,
    private val updateVersionInfo: TextView,
    private val updateReleaseNotes: TextView,
    private val updateButton: MaterialButton,
    private val signOutButton: View,
) {

    fun show(status: AppUpdateStatus) {
        when (status) {
            AppUpdateStatus.UpToDate -> hideForcedUpdate()
            is AppUpdateStatus.UpdateAvailable -> {
                if (status.info.forceUpdate) {
                    showForcedUpdate(status.info)
                } else {
                    hideForcedUpdate()
                    showOptionalUpdateDialog(status.info)
                }
            }
        }
    }

    private fun showForcedUpdate(info: AppUpdateInfo) {
        signOutButton.visibility = View.GONE
        updateOverlay.visibility = View.VISIBLE

        updateTitle.setText(R.string.update_required_title)
        updateVersionInfo.text = fragment.getString(
            R.string.update_version_info,
            AppUpdateChecker.installedVersionName,
            info.latestVersionCode.toString(),
        )
        bindReleaseNotes(info.releaseNotes)
        bindUpdateButton(info)
    }

    private fun showOptionalUpdateDialog(info: AppUpdateInfo) {
        val context = fragment.requireContext()
        val message = buildString {
            append(
                context.getString(
                    R.string.update_version_info,
                    AppUpdateChecker.installedVersionName,
                    info.latestVersionCode.toString(),
                )
            )
            info.releaseNotes?.let { notes ->
                append("\n\n")
                append(notes)
            }
        }

        MaterialAlertDialogBuilder(context)
            .setIcon(R.drawable.ic_baseline_system_update_24)
            .setTitle(R.string.update_available_title)
            .setMessage(message)
            .setPositiveButton(R.string.update_play_store) { _, _ ->
                AppUpdateChecker.openStorePage(context, info.storeUrl)
            }
            .setNegativeButton(R.string.update_later, null)
            .show()
    }

    private fun hideForcedUpdate() {
        signOutButton.visibility = View.VISIBLE
        updateOverlay.visibility = View.GONE
    }

    private fun bindReleaseNotes(releaseNotes: String?) {
        if (releaseNotes.isNullOrBlank()) {
            updateReleaseNotes.visibility = View.GONE
        } else {
            updateReleaseNotes.visibility = View.VISIBLE
            updateReleaseNotes.text = releaseNotes
        }
    }

    private fun bindUpdateButton(info: AppUpdateInfo) {
        val context = fragment.requireContext()
        updateButton.setOnClickListener {
            AppUpdateChecker.openStorePage(context, info.storeUrl)
        }
    }
}
