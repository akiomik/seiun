package io.github.akiomik.seiun.model.app.bsky.notification

import com.squareup.moshi.JsonClass
import kotlin.collections.List

@JsonClass(generateAdapter = true)
data class Notifications(
    val notifications: List<Notification>,
    val cursor: String? = null
)
