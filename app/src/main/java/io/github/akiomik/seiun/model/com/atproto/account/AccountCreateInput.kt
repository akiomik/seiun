package io.github.akiomik.seiun.model.com.atproto.account

data class AccountCreateInput(
    val email: String,
    val handle: String,
    val password: String,
    val inviteCode: String? = null,
    val recoveryKey: String? = null
)