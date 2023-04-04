package io.github.akiomik.seiun.model.app.bsky.embed

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.type.HasNsid

@JsonClass(generateAdapter = true)
data class ExternalView(
    val external: ExternalViewExternal
) : HasNsid by Companion {
    @Keep
    companion object : HasNsid {
        override val nsid: String
            get() = "app.bsky.embed.external#view"
    }
}
