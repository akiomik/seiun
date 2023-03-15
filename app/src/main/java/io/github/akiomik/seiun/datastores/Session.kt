package io.github.akiomik.seiun.datastores

import io.github.akiomik.seiun.model.ISession

data class Session(
    override val accessJwt: String,
    override val refreshJwt: String,
    override val handle: String,
    override val did: String
) : ISession {
    companion object {
        fun fromISession(session: ISession): Session {
            return Session(
                accessJwt = session.accessJwt,
                refreshJwt = session.refreshJwt,
                handle = session.handle,
                did = session.did
            )
        }
    }
}
