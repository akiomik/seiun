package io.github.akiomik.seiun.model.com.atproto.session

data class SessionCreateInput(
    val handle: String,
    val password: String,
)