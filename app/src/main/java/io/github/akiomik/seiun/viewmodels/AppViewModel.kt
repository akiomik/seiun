package io.github.akiomik.seiun.viewmodels

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewModelScope
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.model.app.bsky.actor.ProfileDetail
import io.github.akiomik.seiun.ui.timeline.NewPostFab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

object AppViewModel : ApplicationViewModel() {
    private val innerAtpService = SeiunApplication.instance!!.atpService

    private var innerProfile = MutableStateFlow<ProfileDetail?>(null)
    private var innerShowDrawer = MutableStateFlow(false)
    private var innerShowTopBar = MutableStateFlow(false)
    private var innerShowBottomBar = MutableStateFlow(false)
    private var innerFab = MutableStateFlow<@Composable () -> Unit>({})

    private val isDrawerAvailable = innerAtpService.combine(innerProfile) { atpService, profile ->
        atpService != null && profile != null
    }

    val profile = innerProfile.asStateFlow()
    val showDrawer = innerShowDrawer.combine(isDrawerAvailable) { showDrawer, isAvailable ->
        showDrawer && isAvailable
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val showTopBar = innerShowTopBar.asStateFlow()
    val showBottomBar = innerShowBottomBar.asStateFlow()
    val fab = innerFab.asStateFlow()

    private val userRepository = SeiunApplication.instance!!.userRepository

    fun updateProfile() {
        wrapError(
            run = { userRepository.getProfile() },
            onSuccess = { innerProfile.value = it },
            onError = { Log.d(SeiunApplication.TAG, "Failed to init ProfileViewModel: $it") }
        )
    }

    fun onTimeline() {
        innerShowDrawer.value = true
        innerShowTopBar.value = true
        innerShowBottomBar.value = true
        innerFab.value = { NewPostFab() }
    }

    fun onNotification() {
        innerShowDrawer.value = true
        innerShowTopBar.value = true
        innerShowBottomBar.value = true
        innerFab.value = {}
    }

    fun onUser() {
        innerShowDrawer.value = false
        innerShowTopBar.value = false
        innerShowBottomBar.value = true
        innerFab.value = {}
    }

    fun onLoginOrRegistration() {
        innerProfile.value = null
        innerShowDrawer.value = false
        innerShowTopBar.value = false
        innerShowBottomBar.value = false
        innerFab.value = {}
    }
}
