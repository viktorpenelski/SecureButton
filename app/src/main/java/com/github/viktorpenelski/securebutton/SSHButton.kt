package com.github.viktorpenelski.securebutton

import java.util.*

data class SSHButton(
        val host: String,
        val port: Int,
        val username: String,
        val password: String,
        val command: String,
        val name: String = "",
        var lastUsed: Date? = null
)