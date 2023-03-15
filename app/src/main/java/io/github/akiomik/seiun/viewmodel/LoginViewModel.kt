package io.github.akiomik.seiun.viewmodel

import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.datastores.Credential
import io.github.akiomik.seiun.model.ISession

class LoginViewModel : ApplicationViewModel() {
    private val userRepository = SeiunApplication.instance!!.userRepository

    fun login(
        handle: String,
        password: String,
        onSuccess: (ISession) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        wrapError(run = {
            userRepository.login(handle, password)
        }, onSuccess = onSuccess, onError = onError)
    }

    fun getCredential(): Credential {
        return userRepository.getCredential()
    }

    fun isLoginParamValid(serviceProvider: String, handleOrEmail: String, password: String): Boolean {
        return serviceProvider.isNotEmpty() && handleOrEmail.isNotEmpty() && password.isNotEmpty()
    }
}
