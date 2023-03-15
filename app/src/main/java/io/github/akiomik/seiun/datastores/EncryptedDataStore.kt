package io.github.akiomik.seiun.datastores

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

abstract class EncryptedDataStore(context: Context) {
    private val key = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    // TODO: Use DataStore and Tink instead of EncryptedSharedPreferences
    protected val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "seiun",
        key,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}
