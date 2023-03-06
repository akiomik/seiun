package io.github.akiomik.seiun.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import io.github.akiomik.seiun.model.Session
import io.github.akiomik.seiun.repository.UserRepository
import io.github.akiomik.seiun.service.UnauthorizedException

abstract class ApplicationViewModel: ViewModel() {
    suspend fun <T> withRetry(userRepository: UserRepository, run: suspend (Session) -> T): T {
        return try {
            val session = userRepository.getSession();
            run(session)
        } catch (e: UnauthorizedException) {
            Log.d("Seiun", "Retrying request")
            val session = userRepository.refresh()
            run(session)
        }
    }
}