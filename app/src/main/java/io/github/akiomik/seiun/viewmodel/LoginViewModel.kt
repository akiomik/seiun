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

// ログイン画面の設定
class LoginViewModel : ApplicationViewModel() {
    private val userRepository = SeiunApplication.instance!!.userRepository

    fun login(
        handle: String,
        password: String,
        onSuccess: (Session) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        wrapError(run = {
            userRepository.login(handle, password)
        }, onSuccess = onSuccess, onError = onError)
    }

    fun getLoginParam(): Pair<String, String> {
        return userRepository.getLoginParam()
    }
}