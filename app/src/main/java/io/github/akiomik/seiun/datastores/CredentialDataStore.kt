package io.github.akiomik.seiun.datastores

import android.content.Context

class CredentialDataStore(context: Context) : EncryptedDataStore(context) {
    fun get(): Credential {
        val serviceProvider = sharedPreferences.getString("serviceProvider", "bsky.social") ?: ""
        var handleOrEmail = sharedPreferences.getString("handleOrEmail", "") ?: ""
        if (handleOrEmail.isEmpty()) {
            handleOrEmail = sharedPreferences.getString("handle", "") ?: ""
        }
        val password = sharedPreferences.getString("password", "") ?: ""
        return Credential(serviceProvider, handleOrEmail, password)
    }

    fun save(credential: Credential) {
        with(sharedPreferences.edit()) {
            putString("serviceProvider", credential.serviceProvider)
            putString("handleOrEmail", credential.handleOrEmail)
            putString("password", credential.password)
            apply()
        }
    }
}
