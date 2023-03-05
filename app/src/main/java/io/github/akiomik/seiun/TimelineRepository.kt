package io.github.akiomik.seiun

import android.util.Log
import com.slack.eithernet.ApiResult
import com.slack.eithernet.response
import io.github.akiomik.seiun.service.AtpService
import io.github.akiomik.seiun.model.Session
import io.github.akiomik.seiun.model.Timeline
import io.github.akiomik.seiun.service.UnauthorizedException

class TimelineRepository(private val atpService: AtpService) {
    suspend fun getTimeline(session: Session): Timeline {
        Log.d("Seiun", "Get timeline")

        return when (val result = atpService.getTimeline("Bearer ${session.accessJwt}")) {
            is ApiResult.Success -> result.value
            is ApiResult.Failure -> when (result) {
                is ApiResult.Failure.HttpFailure -> {
                    if (result.code == 400 || result.code == 401) {
                        throw UnauthorizedException("Unauthorized: $result")
                    } else {
                        throw IllegalStateException("HttpError: $result")
                    }
                }
                else -> throw IllegalStateException("HttpError: $result")
            }
        }
    }
}