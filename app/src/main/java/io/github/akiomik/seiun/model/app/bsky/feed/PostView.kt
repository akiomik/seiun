package io.github.akiomik.seiun.model.app.bsky.feed

import com.squareup.moshi.JsonClass
import io.github.akiomik.seiun.model.app.bsky.actor.ProfileViewBasic
import io.github.akiomik.seiun.model.com.atproto.repo.StrongRef
import java.util.*

@JsonClass(generateAdapter = true)
data class PostView(
    val uri: String,
    val cid: String,
    val author: ProfileViewBasic,
    val record: Post, // TODO: unknown
    val indexedAt: Date,
    val replyCount: Int? = null,
    val repostCount: Int? = null,
    val likeCount: Int? = null,
    val viewer: ViewerState? = null,
    val embed: ImagesViewOrExternalViewOrRecordViewOrRecordWithMediaView? = null // TODO: union type
) {
    fun reposted(uri: String): PostView {
        return copy(
            repostCount = (repostCount ?: 0) + 1,
            viewer = viewer?.copy(repost = uri)
        )
    }

    fun repostCanceled(): PostView {
        return copy(
            repostCount = (repostCount ?: 0) - 1,
            viewer = viewer?.copy(repost = null)
        )
    }

    fun liked(uri: String): PostView {
        return copy(
            likeCount = (likeCount ?: 0) + 1,
            viewer = viewer?.copy(like = uri)
        )
    }

    fun likeCanceled(): PostView {
        return copy(
            likeCount = (likeCount ?: 0) - 1,
            viewer = viewer?.copy(like = null)
        )
    }

    fun toStrongRef(): StrongRef {
        return StrongRef(uri = uri, cid = cid)
    }
}
