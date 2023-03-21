package io.github.akiomik.seiun.viewmodels

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewModelScope
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.model.app.bsky.actor.Profile
import io.github.akiomik.seiun.ui.timeline.NewPostFab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class AppViewModel : ApplicationViewModel() {
    private val _atpService = SeiunApplication.instance!!.atpService

    private var _profile = MutableStateFlow<Profile?>(null)
    private var _showDrawer = MutableStateFlow(false)
    private var _showTopBar = MutableStateFlow(false)
    private var _showBottomBar = MutableStateFlow(false)
    private var _fab = MutableStateFlow<@Composable () -> Unit>({})

    private val isDrawerAvailable = _atpService.combine(_profile) { atpService, profile ->
        atpService != null && profile != null
    }

    val profile = _profile.asStateFlow()
    val showDrawer = _showDrawer.combine(isDrawerAvailable) { showDrawer, isAvailable ->
        showDrawer && isAvailable
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val showTopBar = _showTopBar.asStateFlow()
    val showBottomBar = _showBottomBar.asStateFlow()
    val fab = _fab.asStateFlow()

    private val userRepository = SeiunApplication.instance!!.userRepository

    fun updateProfile() {
        wrapError(
            run = { userRepository.getProfile() },
            onSuccess = { _profile.value = it },
            onError = { Log.d(SeiunApplication.TAG, "Failed to init ProfileViewModel: $it") }
        )
    }

    fun onTimeline() {
        _showDrawer.value = true
        _showTopBar.value = true
        _showBottomBar.value = true
        _fab.value = { NewPostFab() }
    }

    fun onNotification() {
        _showDrawer.value = true
        _showTopBar.value = true
        _showBottomBar.value = true
        _fab.value = {}
    }

    fun onUser() {
        _showDrawer.value = false
        _showTopBar.value = false
        _showBottomBar.value = true
        _fab.value = {}
    }

    fun onLoginOrRegistration() {
        _showDrawer.value = false
        _showTopBar.value = false
        _showBottomBar.value = false
        _fab.value = {}
    }
}
