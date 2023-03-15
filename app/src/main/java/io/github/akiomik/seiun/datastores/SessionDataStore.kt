package io.github.akiomik.seiun.datastores

import android.content.Context

class SessionDataStore(context: Context) : EncryptedDataStore(context) {
    fun get(): Session {
        val accessJwt = sharedPreferences.getString("accessJwt", "").orEmpty()
        val refreshJwt = sharedPreferences.getString("refreshJwt", "").orEmpty()
        val handle = sharedPreferences.getString("handle", "").orEmpty()
        val did = sharedPreferences.getString("did", "").orEmpty()

        return Session(accessJwt = accessJwt, refreshJwt = refreshJwt, handle = handle, did = did)
    }

    fun save(session: Session) {
        with(sharedPreferences.edit()) {
            putString("handle", session.handle)
            putString("did", session.did)
            putString("accessJwt", session.accessJwt)
            putString("refreshJwt", session.refreshJwt)
            apply()
        }
    }
}
