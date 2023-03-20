package io.github.akiomik.seiun.model.com.atproto.account

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AccountCreateInput(
    val email: String,
    val handle: String,
    val password: String,
    val inviteCode: String? = null,
    val recoveryKey: String? = null
)
