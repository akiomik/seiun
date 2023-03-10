package io.github.akiomik.seiun.model.app.bsky.notification

import io.github.akiomik.seiun.model.app.bsky.actor.RefWithInfo

data class Notification(
    val uri: String,
    val cid: String,
    val author: RefWithInfo,
    val reason: String,
    val record: NotificationRecord, // TODO unknown
    val reasonSubject: String? = null,
)