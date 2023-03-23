package io.github.akiomik.seiun.repository

import android.util.Log
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.api.RequestHelper
import io.github.akiomik.seiun.datastores.Credential
import io.github.akiomik.seiun.datastores.CredentialDataStore
import io.github.akiomik.seiun.datastores.Session
import io.github.akiomik.seiun.datastores.SessionDataStore
import io.github.akiomik.seiun.model.com.atproto.server.CreateAccountInput
import io.github.akiomik.seiun.model.com.atproto.server.CreateAccountOutput
import io.github.akiomik.seiun.model.com.atproto.server.CreateSessionInput
import io.github.akiomik.seiun.model.com.atproto.server.CreateSessionOutput
import io.github.akiomik.seiun.model.com.atproto.server.RefreshSessionOutput

class AuthRepository(
    private val credentialDataStore: CredentialDataStore,
    private val sessionDataStore: SessionDataStore
) : ApplicationRepository() {
    suspend fun createAccount(
        email: String,
        handle: String,
        password: String,
        inviteCode: String
    ): CreateAccountOutput {
        Log.d(SeiunApplication.TAG, "Create account: $handle")
        val param = CreateAccountInput(
            email = email,
            handle = handle,
            password = password,
            inviteCode = inviteCode
        )

        return RequestHelper.execute { getAtpClient().createAccount(param) }
    }

    suspend fun login(handleOrEmail: String, password: String): CreateSessionOutput {
        Log.d(SeiunApplication.TAG, "Create session")

        return RequestHelper.execute {
            getAtpClient().createSession(CreateSessionInput(handleOrEmail, password))
        }
    }

    suspend fun refresh(): RefreshSessionOutput {
        Log.d(SeiunApplication.TAG, "Refresh session")
        val oldSession = sessionDataStore.get()

        return RequestHelper.execute {
            getAtpClient().refreshSession("Bearer ${oldSession.refreshJwt}")
        }
    }

    fun getSession(): Session = sessionDataStore.get()

    fun saveSession(session: Session) = sessionDataStore.save(session)

    fun getCredential(): Credential = credentialDataStore.get()

    fun saveCredential(credential: Credential) = credentialDataStore.save(credential)
}
