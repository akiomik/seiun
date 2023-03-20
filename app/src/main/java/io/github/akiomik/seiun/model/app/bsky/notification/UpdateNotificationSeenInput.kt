package io.github.akiomik.seiun.model.app.bsky.notification

import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class UpdateNotificationSeenInput(
    val seenAt: Date
)
