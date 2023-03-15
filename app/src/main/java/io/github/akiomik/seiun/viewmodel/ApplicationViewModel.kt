package io.github.akiomik.seiun.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class ApplicationViewModel : ViewModel() {
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
