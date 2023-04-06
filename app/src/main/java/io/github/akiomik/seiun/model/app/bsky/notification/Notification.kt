package io.github.akiomik.seiun.model.app.bsky.notification

import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.app.bsky.actor.ProfileViewBasic
import io.github.akiomik.seiun.model.type.Record
import java.util.*

@JsonClass(generateAdapter = true)
data class Notification(
    val uri: String,
    val cid: String,
    val author: ProfileViewBasic,
    val reason: String,
    val record: Record,
    val isRead: Boolean,
    val indexedAt: Date,
    val reasonSubject: String? = null
)
