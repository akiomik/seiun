package io.github.akiomik.seiun.model.com.atproto.server

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateAccountInput(
    val email: String,
    val handle: String,
    val password: String,
    val inviteCode: String? = null,
    val recoveryKey: String? = null
)
