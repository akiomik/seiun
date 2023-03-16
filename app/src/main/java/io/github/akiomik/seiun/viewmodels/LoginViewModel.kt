package io.github.akiomik.seiun.viewmodels

import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.datastores.Credential
import io.github.akiomik.seiun.model.ISession
import io.github.akiomik.seiun.utilities.ServiceProviderValidator

class LoginViewModel : ApplicationViewModel() {
    private val authRepository = SeiunApplication.instance!!.authRepository

    fun login(
        handle: String,
        password: String,
        onSuccess: (ISession) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        wrapError(run = {
            authRepository.login(handle, password)
        }, onSuccess = onSuccess, onError = onError)
    }

    fun getCredential(): Credential {
        return authRepository.getCredential()
    }

    fun isLoginParamValid(
        serviceProvider: String,
        handleOrEmail: String,
        password: String
    ): Boolean {
        return ServiceProviderValidator.validate(serviceProvider) && handleOrEmail.isNotEmpty() && password.isNotEmpty()
    }
}
