package io.github.akiomik.seiun.repository

import android.util.Log
import com.slack.eithernet.ApiResult
import com.slack.eithernet.response
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.model.ISession
import io.github.akiomik.seiun.model.app.bsky.notification.NotificationList
import io.github.akiomik.seiun.service.AtpService
import io.github.akiomik.seiun.service.UnauthorizedException

class NotificationRepository(private val atpService: AtpService) {
    suspend fun listNotifications(session: ISession, before: String? = null): NotificationList {
        Log.d(SeiunApplication.TAG, "Get notifications: before = $before")

        return when (val result =
            atpService.listNotifications("Bearer ${session.accessJwt}", before = before)) {
            is ApiResult.Success -> result.value
            is ApiResult.Failure -> when (result) {
                is ApiResult.Failure.HttpFailure -> {
                    if (result.code == 401) {
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