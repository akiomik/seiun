package io.github.akiomik.seiun

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.catpaw.services.AtpService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.github.akiomik.seiun.model.LoginParam
import io.github.akiomik.seiun.model.Session
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

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

    private val atpService = atpService;

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

    fun saveLoginParam(handle: String, password: String) {
        with (sharedPreferences.edit()) {
            putString("handle", handle)
            putString("password", password)
            apply()
        }
    }

    fun login(handle: String, password: String): Session {
        Log.d("Seiun", "Create session")
        return atpService.login(LoginParam(handle, password)).execute().body()
            ?: throw IllegalStateException("Empty body on login")
    }

    fun refresh(): Session {
        Log.d("Seiun", "Refresh session")
        val oldSession = getSession()
        return atpService.refreshSession("Bearer ${oldSession.refreshJwt}").execute().body()
            ?: throw IllegalStateException("Empty body on refresh")
    }
}