package com.github.viktorpenelski.securebutton.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.github.viktorpenelski.securebutton.R
import com.github.viktorpenelski.securebutton.SSHButton
import kotlinx.android.synthetic.main.ssh_button_edit_ui.view.*

class EditButtonDialog {
    companion object {
        fun showEdit(context: Context, button: SSHButton, onSuccessfulEdit: OnSuccessfulEditButton) {

            MaterialDialog.Builder(context)
                    .title("Edit")
                    .customView(fillInCustomView(context, button), true)
                    .negativeText("Cancel")
                    .positiveText("Ok")
                    .onPositive({dialog, which ->
                        dialog.customView?.apply {
                            val newButton = SSHButton(
                                    host = edit_hostname.text.toString(),
                                    port = edit_port.text.toString().toInt(),
                                    username = edit_username.text.toString(),
                                    password = edit_password.text.toString(),
                                    command = edit_command.text.toString(),
                                    name = edit_name.text.toString()
                            )
                            onSuccessfulEdit.editButton(newButton)
                        }
                    }).show()
        }

        private fun fillInCustomView(context: Context, button: SSHButton): View {
            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.ssh_button_edit_ui, null)

            view.edit_name.setText(button.name)
            view.edit_command.setText(button.command)
            view.edit_hostname.setText(button.host)
            view.edit_port.setText(button.port.toString())
            view.edit_username.setText(button.username)
            view.edit_password.setText(button.password)

            return view
        }
    }

    interface OnSuccessfulEditButton {
        fun editButton(sshButton: SSHButton)
    }
}