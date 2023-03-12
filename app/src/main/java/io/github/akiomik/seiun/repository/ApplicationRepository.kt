package io.github.akiomik.seiun.repository

import com.slack.eithernet.ApiResult
import io.github.akiomik.seiun.model.AtpError
import io.github.akiomik.seiun.service.UnauthorizedException

abstract class ApplicationRepository {
    protected suspend fun <A : Any> handleRequest(run: suspend () -> ApiResult<A, AtpError>): A {
        when (val res = run()) {
            is ApiResult.Success -> return res.value
            is ApiResult.Failure -> when (res) {
                is ApiResult.Failure.HttpFailure -> {
                    if (res.code == 401) {
                        throw UnauthorizedException(res.error?.message ?: "Unauthorized")
                    } else {
                        throw IllegalStateException(res.error?.message ?: "Error occurred")
                    }
                }
                is ApiResult.Failure.ApiFailure ->
                    throw IllegalStateException(res.error?.message ?: "ApiError occurred")
                is ApiResult.Failure.NetworkFailure -> throw res.error
                is ApiResult.Failure.UnknownFailure -> throw res.error
            }
        }
    }
}
