package io.github.akiomik.seiun.api

import android.util.Log
import com.slack.eithernet.ApiResult
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.datastores.Session
import io.github.akiomik.seiun.model.AtpError
import io.github.akiomik.seiun.repository.AuthRepository
import okio.IOException

object RequestHelper {
    suspend fun <A : Any> execute(run: suspend () -> ApiResult<A, AtpError>): A {
        when (val res = run()) {
            is ApiResult.Success -> return res.value
            is ApiResult.Failure -> when (res) {
                is ApiResult.Failure.HttpFailure -> {
                    if (res.code == 401) {
                        throw UnauthorizedException(res.error?.message ?: "Unauthorized")
                    } else if (res.code == 400 && res.error?.error == "ExpiredToken") {
                        throw ExpiredTokenException(res.error?.message ?: "Expired token")
                    } else {
                        throw IOException(res.error?.message ?: "HttpFailure")
                    }
                }
                is ApiResult.Failure.ApiFailure -> throw IOException(
                    res.error?.message ?: "ApiFailure"
                )
                is ApiResult.Failure.NetworkFailure -> throw res.error
                is ApiResult.Failure.UnknownFailure -> throw res.error
            }
        }
    }

    suspend fun <A : Any> executeWithRetry(
        authRepository: AuthRepository,
        run: suspend (Session) -> ApiResult<A, AtpError>
    ): A {
        return try {
            val session = authRepository.getSession()
            execute { run(session) }
        } catch (e: UnauthorizedException) {
            Log.d(SeiunApplication.TAG, "Retrying request w/ token refresh")
            val session = authRepository.refresh()
            execute { run(Session.fromISession(session)) }
        } catch (e: ExpiredTokenException) {
            Log.d(SeiunApplication.TAG, "Retrying request w/ re-login")
            val credential = authRepository.getCredential()
            val session = authRepository.login(credential.handleOrEmail, credential.password)
            execute { run(Session.fromISession(session)) }
        }
    }
}
