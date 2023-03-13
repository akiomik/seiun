package io.github.akiomik.seiun.repository

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.model.ISession
import io.github.akiomik.seiun.model.app.bsky.actor.Profile
import io.github.akiomik.seiun.model.app.bsky.graph.MuteInput
import io.github.akiomik.seiun.model.app.bsky.graph.UnmuteInput
import io.github.akiomik.seiun.model.com.atproto.account.AccountCreateInput
import io.github.akiomik.seiun.model.com.atproto.account.AccountCreateOutput
import io.github.akiomik.seiun.model.com.atproto.session.SessionCreateInput
import io.github.akiomik.seiun.model.com.atproto.session.SessionCreateOutput
import io.github.akiomik.seiun.model.com.atproto.session.SessionRefreshOutput

class UserRepository(context: Context) : ApplicationRepository() {
    private val key = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "seiun",
        key,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getSession(): ISession {
        val accessJwt = sharedPreferences.getString("accessJwt", "") ?: ""
        val refreshJwt = sharedPreferences.getString("refreshJwt", "") ?: ""
        val handle = sharedPreferences.getString("handle", "") ?: ""
        val did = sharedPreferences.getString("did", "") ?: ""

        return SessionCreateOutput(
            accessJwt = accessJwt,
            refreshJwt = refreshJwt,
            handle = handle,
            did = did
        )
    }

    fun saveSession(session: ISession) {
        with(sharedPreferences.edit()) {
            putString("handle", session.handle)
            putString("did", session.did)
            putString("accessJwt", session.accessJwt)
            putString("refreshJwt", session.refreshJwt)
            apply()
        }
    }

    fun getLoginParam(): Triple<String, String, String> {
        val serviceProvider = sharedPreferences.getString("serviceProvider", "bsky.social") ?: ""
        var handleOrEmail = sharedPreferences.getString("handleOrEmail", "") ?: ""
        if (handleOrEmail.isEmpty()) {
            handleOrEmail = sharedPreferences.getString("handle", "") ?: ""
        }
        val password = sharedPreferences.getString("password", "") ?: ""
        return Triple(serviceProvider, handleOrEmail, password)
    }

    fun saveLoginParam(serviceProvider: String, handleOrEmail: String, password: String) {
        with(sharedPreferences.edit()) {
            putString("serviceProvider", serviceProvider)
            putString("handleOrEmail", handleOrEmail)
            putString("password", password)
            apply()
        }
    }

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
        val oldSession = getSession()

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
}
