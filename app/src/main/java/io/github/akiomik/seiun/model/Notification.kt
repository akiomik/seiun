package io.github.akiomik.seiun.model

data class Notification(
    val uri: String,
    val cid: String,
    val author: ActorWithInfo,
    val reason: NotificationReason,
    val record: NotificationRecord, // unknown
    val isRead: Boolean,
    val indexedAt: String, // TODO: datetime
    val reasonSubject: String? = null,
)