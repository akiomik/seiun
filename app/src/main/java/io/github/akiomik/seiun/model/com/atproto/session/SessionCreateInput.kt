package io.github.akiomik.seiun.model.com.atproto.session

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SessionCreateInput(
    val handle: String,
    val password: String
)
