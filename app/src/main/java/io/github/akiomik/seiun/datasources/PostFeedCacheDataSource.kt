package io.github.akiomik.seiun.datasources

import io.github.akiomik.seiun.model.app.bsky.feed.FeedViewPost
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

object PostFeedCacheDataSource {
    private val feedPostByCid: MutableMap<String, FeedViewPost> = mutableMapOf()
    private val mutableFeedPosts: MutableStateFlow<List<FeedViewPost>> = MutableStateFlow(emptyList())
    val feed = mutableFeedPosts.asStateFlow().map {
        it.sortedByDescending { post ->
            post.reason?.indexedAt ?: post.post.indexedAt
        }
    }

    fun putFeedPost(feedPost: FeedViewPost) {
        feedPostByCid[feedPost.id()] = feedPost
        mutableFeedPosts.value = feedPostByCid.values.toList()
    }

    fun removeFeedPost(id: String) {
        feedPostByCid.remove(id)
        mutableFeedPosts.value = feedPostByCid.values.toList()
    }
}
