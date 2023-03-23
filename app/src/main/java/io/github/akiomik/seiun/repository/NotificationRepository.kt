package io.github.akiomik.seiun.repository

import android.util.Log
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.api.RequestHelper
import io.github.akiomik.seiun.model.app.bsky.notification.Notifications
import io.github.akiomik.seiun.model.app.bsky.notification.UpdateNotificationSeenInput
import java.util.*

class NotificationRepository(private val authRepository: AuthRepository) : ApplicationRepository() {
    suspend fun listNotifications(cursor: String? = null): Notifications {
        Log.d(SeiunApplication.TAG, "Get notifications: cursor = $cursor")

        return RequestHelper.executeWithRetry(authRepository) {
            getAtpClient().listNotifications("Bearer ${it.accessJwt}", cursor = cursor)
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
