package io.github.akiomik.seiun.model.app.bsky.embed

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.app.bsky.actor.ProfileViewBasic
import io.github.akiomik.seiun.model.type.HasNsid
import io.github.akiomik.seiun.model.type.Union4
import java.util.*
import io.github.akiomik.seiun.model.type.Record as IRecord

@JsonClass(generateAdapter = true)
data class RecordViewRecord(
    val uri: String? = null,
    val cid: String? = null,
    val author: ProfileViewBasic? = null,
    val value: IRecord,
    val embeds: List<Union4<ImagesView, ExternalView, RecordView, RecordWithMediaView>>? = null,
    val indexedAt: Date? = null
) : HasNsid by Companion {
    @Keep
    companion object : HasNsid {
        override val nsid: String
            get() = "app.bsky.embed.record#viewRecord"
    }
}
