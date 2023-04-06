package io.github.akiomik.seiun.repository

import io.github.akiomik.seiun.datastores.Preferences
import io.github.akiomik.seiun.datastores.PreferencesDataStore

class PreferencesRepository(private val preferencesDataStore: PreferencesDataStore) :
    ApplicationRepository() {

    fun load(): Preferences {
        return preferencesDataStore.get()
    }

    fun updateIsAutoTranslationEnabled(enabled: Boolean) {
        val preferences = load()
        val updated = preferences.copy(isAutoTranslationEnabled = enabled)
        preferencesDataStore.save(updated)
    }
}
