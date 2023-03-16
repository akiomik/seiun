package io.github.akiomik.seiun.viewmodels

import android.util.Log
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.model.app.bsky.actor.Profile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AppViewModel : ApplicationViewModel() {
    private var _profile = MutableStateFlow<Profile?>(null)
    val profile = _profile as StateFlow<Profile?>

    private val userRepository = SeiunApplication.instance!!.userRepository

    fun updateProfile() {
        wrapError(
            run = { userRepository.getProfile() },
            onSuccess = { _profile.value = it },
            onError = { Log.d(SeiunApplication.TAG, "Failed to init ProfileViewModel: $it") }
        )
    }
}
