package com.github.viktorpenelski.securebutton

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.github.viktorpenelski.securebutton.dialog.AddButtonDialog
import com.github.viktorpenelski.securebutton.dialog.EditButtonDialog
import com.github.viktorpenelski.securebutton.dialog.UnknownHostKeyDialog
import com.github.viktorpenelski.securebutton.service.SshClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jcraft.jsch.HostKey
import com.jcraft.jsch.JSch
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.coroutines.experimental.async
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var sshButtonsAdapter: SSHButtonsListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val listView = ssh_buttons_list_view
        listView.itemsCanFocus = false

        sshButtonsAdapter = initializeAdapter(getButtonsFromSharedPreference())
        listView.adapter = sshButtonsAdapter

        listView.setOnItemClickListener { adapter, _, position, _ ->
            val buttonClicked = adapter.getItemAtPosition(position) as SSHButton

            EditButtonDialog.showEdit(this@MainActivity, buttonClicked, object : EditButtonDialog.OnSuccessfulEditButton {
                override fun editButton(sshButton: SSHButton) {
                    sshButtonsAdapter.buttons[position] = sshButton
                    sshButtonsAdapter.notifyDataSetChanged()
                }
            })
        }


        fab.setOnClickListener { view ->
            AddButtonDialog.show(this@MainActivity, object : AddButtonDialog.OnSuccessfulAddButton {
                override fun addButton(sshButton: SSHButton) {
                    sshButtonsAdapter.buttons.add(sshButton)
                    sshButtonsAdapter.notifyDataSetChanged()
                }
            })
        }
    }

    override fun onPause() {
        super.onPause()

        storeButtonsToSharedPreference(sshButtonsAdapter.buttons)
    }

    private fun initializeAdapter(buttons: MutableList<SSHButton>): SSHButtonsListAdapter {
        return SSHButtonsListAdapter(
                this,
                buttons,
                object : SSHButtonsListAdapter.OnExecuteClickedListener {
                    override fun executeClicked(button: SSHButton) {
                        async {
                            executeSshCommand(button)
                        }
                    }
                })
    }

    private fun storeButtonsToSharedPreference(buttons: List<SSHButton>) {
        val gson = Gson()
        val jsonButtons = gson.toJson(buttons)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        sharedPreferences.edit().putString("ssh_buttons", jsonButtons).apply()
    }

    private fun getButtonsFromSharedPreference(): MutableList<SSHButton> {
        val gson = Gson()
        val type = object : TypeToken<List<SSHButton>>() {}.type

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val jsonButtons = sharedPreferences.getString("ssh_buttons", "")

        if (jsonButtons.isBlank()) {
            return mutableListOf()
        }

        return gson.fromJson(jsonButtons, type)

    }

    private fun getPersistedHostKeyFor(host: String): String {
        return PreferenceManager.getDefaultSharedPreferences(applicationContext)
                .getString("hostkey_$host", "")
    }

    private fun executeSshCommand(button: SSHButton) {
        //TODO(vic) refactor this
        SshClient.execute(
                button,
                getPersistedHostKeyFor(button.host),
                object : SshClient.SshClientCallback {
                    override fun promptUnknownFingerprint(hostKey: HostKey, sshButton: SSHButton, jsch: JSch) {
                        runOnUiThread {
                            UnknownHostKeyDialog.show(
                                    this@MainActivity,
                                    hostKey.getFingerPrint(jsch),
                                    object : UnknownHostKeyDialog.DialogCallback {
                                        override fun onPositive() {
                                            PreferenceManager
                                                    .getDefaultSharedPreferences(applicationContext)
                                                    .edit()
                                                    .putString("hostkey_${sshButton.host}", hostKey.key)
                                                    .apply()
                                            async {
                                                executeSshCommand(sshButton)
                                            }
                                        }
                                    }
                            )
                        }
                    }

                    override fun unexpectedException() {
                        //TODO(vic) either add the real exception here,
                        // or log it somewhere so that user can later check it out
                        runOnUiThread {
                            Toast.makeText(
                                    applicationContext,
                                    "Something went wrong during the SSH connection :(",
                                    Toast.LENGTH_LONG)
                                    .show()
                        }
                    }

                    override fun successfullyExecuted() {
                        //TODO(vic) make sure that this update is taken into account for the UI
                        button.lastUsed = Calendar.getInstance().time

                        runOnUiThread {
                            Toast.makeText(
                                    applicationContext,
                                    "Successfully executed!",
                                    Toast.LENGTH_LONG)
                                    .show()
                        }
                    }
                })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            //TODO(vic) create settings window with ability to add "identities"
            //an identity is a combination of username, password, host, port and can be shared between buttons
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
