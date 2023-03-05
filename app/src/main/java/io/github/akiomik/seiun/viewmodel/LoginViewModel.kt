package io.github.akiomik.seiun.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.model.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val userRepository = SeiunApplication.instance!!.userRepository

    fun login(handle: String, password: String, onLoginSuccessful: (Session) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val session = userRepository.login(handle, password)
            onLoginSuccessful(session)
        }
    }

    fun getLoginParam(): Pair<String, String> {
        return userRepository.getLoginParam()
    }
}