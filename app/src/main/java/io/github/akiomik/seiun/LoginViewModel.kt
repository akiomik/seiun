package io.github.akiomik.seiun

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.akiomik.seiun.model.FeedViewPost
import io.github.akiomik.seiun.model.Session
import io.github.akiomik.seiun.model.Timeline
import io.github.akiomik.seiun.service.UnauthorizedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

class LoginViewModel : ViewModel() {
    private val userRepository = SeiunApplication.instance!!.userRepository

    fun login(handle: String, password: String, onLoginSuccessful: (Session) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val session = userRepository.login(handle, password)
            onLoginSuccessful(session)
        }
    }
}