package io.github.akiomik.seiun.repository

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.slack.eithernet.ApiResult
import io.github.akiomik.seiun.model.AccountCreateParam
import io.github.akiomik.seiun.model.LoginParam
import io.github.akiomik.seiun.model.Profile
import io.github.akiomik.seiun.model.Session
import io.github.akiomik.seiun.service.AtpService

class UserRepository(context: Context, private val atpService: AtpService) {
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

    fun getSession(): Session {
        val accessJwt = sharedPreferences.getString("accessJwt", "") ?: ""
        val refreshJwt = sharedPreferences.getString("refreshJwt", "") ?: ""
        val handle = sharedPreferences.getString("handle", "") ?: ""
        val did = sharedPreferences.getString("did", "") ?: ""

        return Session(accessJwt = accessJwt, refreshJwt = refreshJwt, handle = handle, did = did)
    }

    fun saveSession(session: Session) {
        with(sharedPreferences.edit()) {
            putString("handle", session.handle)
            putString("did", session.did)
            putString("accessJwt", session.accessJwt)
            putString("refreshJwt", session.refreshJwt)
            apply()
        }
    }

    fun getLoginParam(): Pair<String, String> {
        val handle = sharedPreferences.getString("handle", "") ?: ""
        val password = sharedPreferences.getString("password", "") ?: ""
        return Pair(handle, password)
    }

    fun saveLoginParam(handle: String, password: String) {
        with(sharedPreferences.edit()) {
            putString("handle", handle)
            putString("password", password)
            apply()
        }
    }

    suspend fun createAccount(
        email: String,
        handle: String,
        password: String,
        inviteCode: String
    ): Session {
        Log.d("Seiun", "Create account: $handle")
        val param = AccountCreateParam(
            email = email,
            handle = handle,
            password = password,
            inviteCode = inviteCode
        );
        return when (val result = atpService.createAccount(param)) {
            is ApiResult.Success -> result.value
            is ApiResult.Failure -> when (result) {
                is ApiResult.Failure.HttpFailure -> throw IllegalStateException(
                    result.error?.message ?: ""
                )
                else -> throw IllegalStateException("Failed to create account")
            }
        }
    }

    suspend fun login(handle: String, password: String): Session {
        Log.d("Seiun", "Create session")
        return when (val result = atpService.login(LoginParam(handle, password))) {
            is ApiResult.Success -> result.value
            is ApiResult.Failure -> when (result) {
                is ApiResult.Failure.HttpFailure -> throw IllegalStateException(
                    result.error?.message ?: ""
                )
                else -> throw IllegalStateException("Failed to login")
            }
        }
    }

    suspend fun refresh(): Session {
        Log.d("Seiun", "Refresh session")
        val oldSession = getSession()
        return when (val result = atpService.refreshSession("Bearer ${oldSession.refreshJwt}")) {
            is ApiResult.Success -> result.value
            is ApiResult.Failure -> throw IllegalStateException("ApiResult.Failure: $result")
        }
    }

    suspend fun getProfile(session: Session): Profile {
        Log.d("Seiun", "Get profile")
        return when (val result = atpService.getProfile("Bearer ${session.accessJwt}", session.did)) {
            is ApiResult.Success -> result.value
            is ApiResult.Failure -> throw IllegalStateException("ApiResult.Failure: $result")
        }
    }
}