package io.github.akiomik.seiun.model.app.bsky.embed

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.type.HasNsid
import io.github.akiomik.seiun.model.type.Union2

@JsonClass(generateAdapter = true)
data class RecordView(
    val record: Union2<RecordViewRecord, RecordViewNotFound>
) : HasNsid by Companion {
    @Keep
    companion object : HasNsid {
        override val nsid: String
            get() = "app.bsky.embed.record#view"
    }
}
