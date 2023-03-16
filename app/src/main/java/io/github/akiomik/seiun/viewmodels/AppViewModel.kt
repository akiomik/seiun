package io.github.akiomik.seiun.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.model.app.bsky.actor.Profile

class AppViewModel : ApplicationViewModel() {
    private var _profile = MutableLiveData<Profile>()
    val profile = _profile as LiveData<Profile>

    private val userRepository = SeiunApplication.instance!!.userRepository

    fun updateProfile() {
        wrapError(
            run = { userRepository.getProfile() },
            onSuccess = { _profile.postValue(it) },
            onError = { Log.d(SeiunApplication.TAG, "Failed to init ProfileViewModel: $it") }
        )
    }
}
