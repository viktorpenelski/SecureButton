package com.github.viktorpenelski.securebutton.dialog

import android.content.Context
import com.afollestad.materialdialogs.MaterialDialog
import com.github.viktorpenelski.securebutton.R
import com.github.viktorpenelski.securebutton.SSHButton
import kotlinx.android.synthetic.main.ssh_button_edit_ui.view.*

class AddButtonDialog {

    companion object {
        fun show(context: Context, onSuccess: OnSuccessfulAddButton) {

            MaterialDialog.Builder(context)
                    .title("Add")
                    .customView(R.layout.ssh_button_edit_ui, true)
                    .negativeText("Cancel")
                    .positiveText("Ok")
                    .onPositive({ dialog, which ->
                        dialog.customView?.apply {
                            val newButton = SSHButton(
                                    host = edit_hostname.text.toString(),
                                    port = edit_port.text.toString().toInt(),
                                    username = edit_username.text.toString(),
                                    password = edit_password.text.toString(),
                                    command = edit_command.text.toString(),
                                    name = edit_name.text.toString()
                            )
                            onSuccess.addButton(newButton)
                        }
                    })
                    .show()
        }
    }

    interface OnSuccessfulAddButton {
        fun addButton(sshButton: SSHButton)
    }

}