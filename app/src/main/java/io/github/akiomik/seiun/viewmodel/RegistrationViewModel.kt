package io.github.akiomik.seiun.viewmodel

import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.model.ISession

class RegistrationViewModel : ApplicationViewModel() {
    private val authRepository = SeiunApplication.instance!!.authRepository

    fun register(
        email: String,
        handle: String,
        password: String,
        inviteCode: String,
        onSuccess: (ISession) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        wrapError(run = {
            authRepository.createAccount(
                email = email,
                handle = handle,
                password = password,
                inviteCode = inviteCode
            )
        }, onSuccess = onSuccess, onError = onError)
    }

    fun isRegisterParamValid(
        serviceProvider: String,
        email: String,
        handle: String,
        password: String
    ): Boolean {
        return serviceProvider.isNotEmpty() && email.isNotEmpty() && handle.isNotEmpty() && password.isNotEmpty()
    }
}
