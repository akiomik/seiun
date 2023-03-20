package io.github.akiomik.seiun.model.app.bsky.notification

import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.app.bsky.actor.RefWithInfo
import java.util.*

@JsonClass(generateAdapter = true)
data class Notification(
    val uri: String,
    val cid: String,
    val author: RefWithInfo,
    val reason: String,
    val record: NotificationRecord, // TODO unknown
    val isRead: Boolean,
    val indexedAt: Date,
    val reasonSubject: String? = null
)
