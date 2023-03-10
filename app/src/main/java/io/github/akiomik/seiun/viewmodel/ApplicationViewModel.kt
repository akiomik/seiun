package io.github.akiomik.seiun.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.model.ISession
import io.github.akiomik.seiun.repository.UserRepository
import io.github.akiomik.seiun.service.UnauthorizedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class ApplicationViewModel : ViewModel() {
    suspend fun <T> withRetry(userRepository: UserRepository, run: suspend (ISession) -> T): T {
        return try {
            val session = userRepository.getSession()
            run(session)
        } catch (e: UnauthorizedException) {
            Log.d(SeiunApplication.TAG, "Retrying request")
            val session = userRepository.refresh()
            run(session)
        }
    }

    fun <T> wrapError(
        run: suspend () -> T,
        onComplete: () -> Unit = {},
        onSuccess: (T) -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        var result: Result<T>? = null

        viewModelScope.launch(Dispatchers.IO) {
            result = runCatching { run() }
        }.invokeOnCompletion {
            viewModelScope.launch(Dispatchers.Main) {
                onComplete()
                result?.onSuccess { onSuccess(it) }
                result?.onFailure { onError(it) }
            }
        }
    }
}