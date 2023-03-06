package io.github.akiomik.seiun.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.slack.eithernet.ApiResult
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.model.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val userRepository = SeiunApplication.instance!!.userRepository

    fun login(handle: String, password: String, onSuccess: (Session) -> Unit, onError: (Throwable) -> Unit) {
        var result: Result<Session>? = null

        viewModelScope.launch(Dispatchers.IO) {
            result = runCatching { userRepository.login(handle, password) }
        }.invokeOnCompletion {
            viewModelScope.launch(Dispatchers.Main) {
                result?.onSuccess { onSuccess(it) }
                result?.onFailure { onError(it) }
            }
        }
    }

    fun getLoginParam(): Pair<String, String> {
        return userRepository.getLoginParam()
    }
}