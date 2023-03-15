package io.github.akiomik.seiun.repository

import android.util.Log
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.datastores.Credential
import io.github.akiomik.seiun.datastores.CredentialDataStore
import io.github.akiomik.seiun.datastores.Session
import io.github.akiomik.seiun.datastores.SessionDataStore
import io.github.akiomik.seiun.model.ISession
import io.github.akiomik.seiun.model.app.bsky.actor.Profile
import io.github.akiomik.seiun.model.app.bsky.graph.MuteInput
import io.github.akiomik.seiun.model.app.bsky.graph.UnmuteInput
import io.github.akiomik.seiun.model.com.atproto.account.AccountCreateInput
import io.github.akiomik.seiun.model.com.atproto.account.AccountCreateOutput
import io.github.akiomik.seiun.model.com.atproto.session.SessionCreateInput
import io.github.akiomik.seiun.model.com.atproto.session.SessionCreateOutput
import io.github.akiomik.seiun.model.com.atproto.session.SessionRefreshOutput

class UserRepository(
    private val credentialDataStore: CredentialDataStore,
    private val sessionDataStore: SessionDataStore
) : ApplicationRepository() {
    suspend fun createAccount(
        email: String,
        handle: String,
        password: String,
        inviteCode: String
    ): AccountCreateOutput {
        Log.d(SeiunApplication.TAG, "Create account: $handle")
        val param = AccountCreateInput(
            email = email,
            handle = handle,
            password = password,
            inviteCode = inviteCode
        )

        return handleRequest { getAtpClient().createAccount(param) }
    }

    suspend fun login(handleOrEmail: String, password: String): SessionCreateOutput {
        Log.d(SeiunApplication.TAG, "Create session")

        return handleRequest {
            getAtpClient().createSession(SessionCreateInput(handleOrEmail, password))
        }
    }

    suspend fun refresh(): SessionRefreshOutput {
        Log.d(SeiunApplication.TAG, "Refresh session")
        val oldSession = sessionDataStore.get()

        return handleRequest {
            getAtpClient().refreshSession("Bearer ${oldSession.refreshJwt}")
        }
    }

    suspend fun getProfile(session: ISession): Profile {
        Log.d(SeiunApplication.TAG, "Get profile")

        return handleRequest {
            getAtpClient().getProfile("Bearer ${session.accessJwt}", session.did)
        }
    }

    suspend fun mute(session: ISession, did: String) {
        Log.d(SeiunApplication.TAG, "Mute user: $did")
        val body = MuteInput(user = did)

        handleRequest {
            getAtpClient().mute("Bearer ${session.accessJwt}", body = body)
        }
    }

    suspend fun unmute(session: ISession, did: String) {
        Log.d(SeiunApplication.TAG, "Unmute user: $did")

        val body = UnmuteInput(user = did)
        handleRequest {
            getAtpClient().unmute("Bearer ${session.accessJwt}", body = body)
        }
    }

    fun getSession(): Session = sessionDataStore.get()

    fun saveSession(session: Session) = sessionDataStore.save(session)

    fun getCredential(): Credential = credentialDataStore.get()

    fun saveCredential(credential: Credential) = credentialDataStore.save(credential)
}
