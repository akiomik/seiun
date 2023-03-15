package io.github.akiomik.seiun.repository

import android.util.Log
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.api.RequestHelper
import io.github.akiomik.seiun.model.app.bsky.notification.NotificationList
import io.github.akiomik.seiun.model.app.bsky.notification.UpdateNotificationSeenInput
import java.util.*

class NotificationRepository(private val authRepository: AuthRepository) : ApplicationRepository() {
    suspend fun listNotifications(before: String? = null): NotificationList {
        Log.d(SeiunApplication.TAG, "Get notifications: before = $before")

        return RequestHelper.executeWithRetry(authRepository) {
            getAtpClient().listNotifications("Bearer ${it.accessJwt}", before = before)
        }
    }

    suspend fun updateNotificationSeen(seenAt: Date) {
        Log.d(SeiunApplication.TAG, "Update notification seen: at = $seenAt")

        val body = UpdateNotificationSeenInput(seenAt = seenAt)

        RequestHelper.executeWithRetry(authRepository) {
            getAtpClient().updateNotificationSeen("Bearer ${it.accessJwt}", body = body)
        }
    }
}
