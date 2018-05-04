package com.github.viktorpenelski.securebutton.service

import android.util.Base64
import android.util.Log
import com.github.viktorpenelski.securebutton.SSHButton
import com.jcraft.jsch.*
import java.util.*

class SshClient {

    companion object {
        fun execute(sshButton: SSHButton, persistedHostKey: String, callback: SshClientCallback) {
            val jsch = JSch()

            if (persistedHostKey.isNotEmpty()) {
                val key = Base64.decode(persistedHostKey, Base64.DEFAULT)
                jsch.hostKeyRepository.add(HostKey(sshButton.host, key), null)
            }

            var session: Session? = null
            var channel: ChannelExec? = null

            try {
                session = jsch.getSession(sshButton.username, sshButton.host, sshButton.port)
                session.setPassword(sshButton.password)

                val prop = Properties()
                prop["StrictHostKeyChecking"] = "yes"
                session.setConfig(prop)
                session.connect()

                channel = session.openChannel("exec") as ChannelExec

                channel.setCommand(sshButton.command)
                channel.setErrStream(System.err)

                channel.connect()
                callback.successfullyExecuted()

            } catch (jse: JSchException) {
                Log.d("JSch", jse.message)

                //TODO(vic) find a better way to differentiate between general exceptions and unknown host fingerprint
                if (session == null || session.hostKey == null) {
                    callback.unexpectedException()
                } else {
                    callback.promptUnknownFingerprint(session.hostKey, sshButton, jsch)
                }

            } finally {
                channel?.disconnect()
                session?.disconnect()
            }

        }
    }

    interface SshClientCallback {
        fun promptUnknownFingerprint(hostKey: HostKey, sshButton: SSHButton, jsch: JSch)
        fun unexpectedException()
        fun successfullyExecuted()
    }
}