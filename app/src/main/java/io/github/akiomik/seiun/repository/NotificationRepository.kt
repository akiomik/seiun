package io.github.akiomik.seiun.repository

import android.util.Log
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.model.ISession
import io.github.akiomik.seiun.model.app.bsky.notification.NotificationList
import io.github.akiomik.seiun.model.app.bsky.notification.UpdateNotificationSeenInput
import java.util.*

class NotificationRepository() : ApplicationRepository() {
    suspend fun listNotifications(session: ISession, before: String? = null): NotificationList {
        Log.d(SeiunApplication.TAG, "Get notifications: before = $before")

        return handleRequest {
            getAtpClient().listNotifications("Bearer ${session.accessJwt}", before = before)
        }
    }

    suspend fun updateNotificationSeen(session: ISession, seenAt: Date) {
        Log.d(SeiunApplication.TAG, "Update notification seen: at = $seenAt")

        val body = UpdateNotificationSeenInput(seenAt = seenAt)

        handleRequest {
            getAtpClient().updateNotificationSeen("Bearer ${session.accessJwt}", body = body)
        }
    }
}
