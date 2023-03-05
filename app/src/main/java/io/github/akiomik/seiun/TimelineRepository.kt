package io.github.akiomik.seiun

import android.util.Log
import com.slack.eithernet.ApiResult
import com.slack.eithernet.response
import io.github.akiomik.seiun.service.AtpService
import io.github.akiomik.seiun.model.Session
import io.github.akiomik.seiun.model.Timeline
import io.github.akiomik.seiun.service.UnauthorizedException

class TimelineRepository(private val atpService: AtpService) {
    suspend fun getTimeline(session: Session, before: String? = null): Timeline {
        Log.d("Seiun", "Get timeline: before = $before")

        return when (val result = atpService.getTimeline("Bearer ${session.accessJwt}", before = before)) {
            is ApiResult.Success -> result.value
            is ApiResult.Failure -> when(result) {
                is ApiResult.Failure.HttpFailure -> {
                    if (result.code == 400 || result.code == 401) {
                        throw UnauthorizedException("Unauthorized: ${result.code} (${result.error})")
                    } else {
                        throw IllegalStateException("HttpError: ${result.code} (${result.error})")
                    }
                }
                else -> throw IllegalStateException("ApiResult.Failure: ${result.response()}")
            }
        }
    }
}