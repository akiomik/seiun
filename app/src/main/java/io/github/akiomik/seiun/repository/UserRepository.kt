package io.github.akiomik.seiun.repository

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.slack.eithernet.ApiResult
import io.github.akiomik.seiun.service.AtpService
import io.github.akiomik.seiun.model.LoginParam
import io.github.akiomik.seiun.model.Session

class UserRepository(private val context: Context, atpService: AtpService) {
    private val key = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "seiun",
        key,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)

    private val atpService = atpService

    fun getSession(): Session {
        val accessJwt = sharedPreferences.getString("accessJwt", "") ?: ""
        val refreshJwt = sharedPreferences.getString("refreshJwt", "") ?: ""
        val handle = sharedPreferences.getString("handle", "") ?: ""
        val did = sharedPreferences.getString("did", "") ?: ""

        return Session(accessJwt = accessJwt, refreshJwt = refreshJwt, handle = handle, did = did)
    }

    fun saveSession(session: Session) {
        with(sharedPreferences.edit()) {
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
        with (sharedPreferences.edit()) {
            putString("handle", handle)
            putString("password", password)
            apply()
        }
    }

    suspend fun login(handle: String, password: String): Session {
        Log.d("Seiun", "Create session")
        try {
            return when (val result = atpService.login(LoginParam(handle, password))) {
                is ApiResult.Success -> result.value
                is ApiResult.Failure -> throw IllegalStateException("ApiResult.Failure: $result")
            }
        } catch (e: java.lang.Exception) {
            Log.d("Seiun", e.stackTraceToString())
            throw e
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
}