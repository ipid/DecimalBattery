package me.ipid.android.decimalbattery

import android.app.Activity
import androidx.appcompat.app.AlertDialog

fun mustExitDialog(activity: Activity) {
    val resources = activity.resources

    AlertDialog.Builder(activity)
        .setMessage(resources.getString(R.string.dialog_unsupported))
        .setPositiveButton(resources.getString(R.string.dialog_button_ok)) { _, _ -> activity.finish() }
        .setOnCancelListener { activity.finish() }
        .setOnDismissListener { activity.finish() }
        .show()
}
