package com.github.viktorpenelski.securebutton.dialog

import android.content.Context
import com.afollestad.materialdialogs.MaterialDialog

class UnknownHostKeyDialog {

    companion object {
        fun show(context: Context, remoteFingerprint: String, dialogCallback: DialogCallback) {
            MaterialDialog.Builder(context)
                    .title("Accept remote host fingerprint?")
                    .negativeText("Cancel")
                    .positiveText("Yes")
                    .content(remoteFingerprint)
                    .onPositive({ dialog, which ->
                        dialogCallback.onPositive()
                    }).show()
        }
    }

    interface DialogCallback {
        fun onPositive()
    }
}