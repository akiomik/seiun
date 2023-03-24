package io.github.akiomik.seiun.model.com.atproto.server

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateSessionInput(
    val password: String,
    val identifier: String? = null
)
