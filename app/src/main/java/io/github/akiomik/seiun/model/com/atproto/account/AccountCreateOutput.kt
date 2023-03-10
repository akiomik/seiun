package io.github.akiomik.seiun.model.com.atproto.account

import io.github.akiomik.seiun.model.ISession

data class AccountCreateOutput(
    override val accessJwt: String,
    override val refreshJwt: String,
    override val handle: String,
    override val did: String
): ISession