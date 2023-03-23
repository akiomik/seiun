package io.github.akiomik.seiun.model.com.atproto.server

import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.ISession

@JsonClass(generateAdapter = true)
data class CreateAccountOutput(
    override val accessJwt: String,
    override val refreshJwt: String,
    override val handle: String,
    override val did: String
) : ISession
