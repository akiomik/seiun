package io.github.akiomik.seiun.api

import com.slack.eithernet.ApiResult
import io.github.akiomik.seiun.model.AtpError
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
                is ApiResult.Failure.ApiFailure -> throw IOException(res.error?.message ?: "ApiFailure")
                is ApiResult.Failure.NetworkFailure -> throw res.error
                is ApiResult.Failure.UnknownFailure -> throw res.error
            }
        }
    }
}
