package io.github.akiomik.seiun.model

data class AccountCreateParam(
    val email: String,
    val handle: String,
    val password: String,
    val inviteCode: String,
)
