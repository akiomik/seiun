package io.github.akiomik.seiun.datastores

import android.content.Context

class PreferencesDataStore(context: Context) : EncryptedDataStore(context) {
    fun get(): Preferences {
        val isAutoTranslationEnabled = sharedPreferences.getBoolean("isAutoTranslationEnabled", false)

        return Preferences(isAutoTranslationEnabled = isAutoTranslationEnabled)
    }

    fun save(preferences: Preferences) {
        with(sharedPreferences.edit()) {
            putBoolean("isAutoTranslationEnabled", preferences.isAutoTranslationEnabled)
            apply()
        }
    }
}
