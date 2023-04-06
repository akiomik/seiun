package io.github.akiomik.seiun.api.adapters

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import io.github.akiomik.seiun.model.app.bsky.feed.Like
import io.github.akiomik.seiun.model.app.bsky.feed.Post
import io.github.akiomik.seiun.model.app.bsky.feed.Repost
import io.github.akiomik.seiun.model.app.bsky.graph.Follow
import io.github.akiomik.seiun.model.type.Record
import java.lang.reflect.Type

object RecordJsonAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
        return PolymorphicJsonAdapterFactory.of(Record::class.java, "\$type")
            .withSubtype(Follow::class.java, "app.bsky.graph.follow")
            .withSubtype(Like::class.java, "app.bsky.feed.like")
            .withSubtype(Repost::class.java, "app.bsky.feed.repost")
            .withSubtype(Post::class.java, "app.bsky.feed.post")
            .create(type, annotations, moshi)
    }
}
