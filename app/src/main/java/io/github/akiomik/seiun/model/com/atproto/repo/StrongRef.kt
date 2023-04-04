package io.github.akiomik.seiun.model.com.atproto.repo

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.type.HasNsid

@JsonClass(generateAdapter = true)
data class StrongRef(
    val uri: String,
    val cid: String
) : HasNsid by Companion {
    @Keep
    companion object : HasNsid {
        override val nsid: String
            get() = "com.atproto.repo.strongRef"
    }
}
