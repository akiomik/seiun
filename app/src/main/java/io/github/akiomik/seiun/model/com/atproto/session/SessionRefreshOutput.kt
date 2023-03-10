package io.github.akiomik.seiun.model.com.atproto.session

import io.github.akiomik.seiun.model.ISession

data class SessionRefreshOutput(
    override val accessJwt: String,
    override val refreshJwt: String,
    override val handle: String,
    override val did: String,
): ISession