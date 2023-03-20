package io.github.akiomik.seiun.model.com.atproto.account

import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.ISession

@JsonClass(generateAdapter = true)
data class AccountCreateOutput(
    override val accessJwt: String,
    override val refreshJwt: String,
    override val handle: String,
    override val did: String
) : ISession
