package io.github.akiomik.seiun.model.app.bsky.notification

import kotlin.collections.List as KList

data class NotificationList(
    val notifications: KList<Notification>,
    val cursor: String? = null
)