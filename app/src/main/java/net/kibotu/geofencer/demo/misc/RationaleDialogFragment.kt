package net.kibotu.geofencer.demo.misc

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import net.kibotu.geofencer.demo.R

class RationaleDialogFragment(
    private val rationalMessage: String,
    private val block: (Boolean) -> Unit = { },
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            AlertDialog.Builder(it)
                .setMessage(rationalMessage)
                .setPositiveButton(R.string.button_allow) { _, _ -> block(true) }
                .setNegativeButton(R.string.button_reject) { _, _ -> block(false) }
                .create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}

fun FragmentActivity.showTwoButtonDialog(rationalMessage: String, block: (Boolean) -> Unit) {
    RationaleDialogFragment(rationalMessage, block).show(supportFragmentManager, "twoButtonDialog")
}
