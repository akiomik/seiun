package io.github.akiomik.seiun.model.app.bsky.notification

import com.squareup.moshi.JsonClass
import kotlin.collections.List as KList

@JsonClass(generateAdapter = true)
data class NotificationList(
    val notifications: KList<Notification>,
    val cursor: String? = null
)
