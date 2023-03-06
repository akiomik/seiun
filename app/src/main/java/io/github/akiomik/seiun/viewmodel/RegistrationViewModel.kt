package io.github.akiomik.seiun.viewmodel

import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.model.Session

class RegistrationViewModel : ApplicationViewModel() {
    private val userRepository = SeiunApplication.instance!!.userRepository

    fun register(
        email: String,
        handle: String,
        password: String,
        inviteCode: String,
        onSuccess: (Session) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        wrapError(run = {
            userRepository.createAccount(
                email = email,
                handle = handle,
                password = password,
                inviteCode = inviteCode
            )
        }, onSuccess = onSuccess, onError = onError)
    }
}