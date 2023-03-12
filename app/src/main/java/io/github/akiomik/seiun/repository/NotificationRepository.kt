package io.github.akiomik.seiun.repository

import android.util.Log
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.model.ISession
import io.github.akiomik.seiun.model.app.bsky.notification.NotificationList
import io.github.akiomik.seiun.service.AtpService

class NotificationRepository(private val atpService: AtpService) : ApplicationRepository() {
    suspend fun listNotifications(session: ISession, before: String? = null): NotificationList {
        Log.d(SeiunApplication.TAG, "Get notifications: before = $before")

        return handleRequest {
            atpService.listNotifications("Bearer ${session.accessJwt}", before = before)
        }
    }
}
