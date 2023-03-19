package io.github.akiomik.seiun.viewmodels

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.util.Log
import androidx.lifecycle.viewModelScope
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.model.app.bsky.feed.FeedViewPost
import io.github.akiomik.seiun.model.app.bsky.feed.PostReplyRef
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.io.ByteArrayOutputStream

class TimelineViewModel : ApplicationViewModel() {
    sealed class State {
        object Loading : State()
        object Loaded : State()
        object Error : State()
    }

    private val userRepository = SeiunApplication.instance!!.userRepository
    private val postFeedRepository = SeiunApplication.instance!!.postFeedRepository
    private var _cursor: String? = null

    private val _feedPostIds: MutableStateFlow<Set<String>> = MutableStateFlow(emptySet())
    private val _isRefreshing = MutableStateFlow(false)
    private val _seenAllFeed = MutableStateFlow(false)
    private val _state = MutableStateFlow<State>(State.Loading)
    val isRefreshing = _isRefreshing.asStateFlow()
    val seenAllFeed = _seenAllFeed.asStateFlow()
    val state = _state.asStateFlow()
    val feedPosts = postFeedRepository.feedPosts.combine(_feedPostIds) { feedPosts, feedPostIds ->
        feedPosts.filter { feedPost -> feedPostIds.contains(feedPost.id()) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        wrapError(run = {
            postFeedRepository.getTimeline()
        }, onSuccess = {
                // NOTE: 50 is default limit of getTimeline
                if (it.feed.size < 50) {
                    _seenAllFeed.value = true
                }

                _feedPostIds.value = it.feed.map { post -> post.id() }.toSet()
                _cursor = it.cursor
                _state.value = State.Loaded
            }, onError = {
                Log.d(SeiunApplication.TAG, "Failed to init TimelineViewModel: $it")
                _state.value = State.Error
            })
    }

    fun refreshPosts(onError: (Throwable) -> Unit = {}) {
        if (_isRefreshing.value) {
            return
        }

        Log.d(SeiunApplication.TAG, "Refresh timeline")
        _isRefreshing.value = true

        wrapError(run = {
            val data = postFeedRepository.getTimeline()

            if (_cursor == null) {
                _cursor = data.cursor
            }

            if (data.cursor != _cursor) {
                _feedPostIds.value = _feedPostIds.value.union(data.feed.map { post -> post.id() })
                Log.d(SeiunApplication.TAG, "Feed posts are updated")
            } else {
                Log.d(SeiunApplication.TAG, "Skip update because cursor is unchanged")
            }
        }, onComplete = {
                _isRefreshing.value = false
            }, onError = onError)
    }

    fun loadMorePosts(onError: (Throwable) -> Unit = {}) {
        Log.d(SeiunApplication.TAG, "Load more posts")

        wrapError(run = {
            val data = postFeedRepository.getTimeline(before = _cursor)

            if (data.cursor != _cursor) {
                if (data.feed.isNotEmpty()) {
                    _feedPostIds.value = _feedPostIds.value.union(data.feed.map { post -> post.id() })
                    _cursor = data.cursor
                    _state.value = State.Loaded
                    Log.d(SeiunApplication.TAG, "Feed posts are updated")
                } else {
                    Log.d(SeiunApplication.TAG, "No new feed posts")
                    _seenAllFeed.value = true
                }
            }
        }, onError = onError)
    }

    fun upvote(
        feedPost: FeedViewPost,
        onSuccess: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        wrapError(
            run = { postFeedRepository.upvote(feedPost) },
            onSuccess = { onSuccess() },
            onError = onError
        )
    }

    fun cancelVote(
        feedPost: FeedViewPost,
        onSuccess: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        wrapError(
            run = { postFeedRepository.cancelVote(feedPost) },
            onSuccess = { onSuccess() },
            onError = onError
        )
    }

    fun repost(
        feedPost: FeedViewPost,
        onSuccess: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        wrapError(
            run = { postFeedRepository.repost(feedPost) },
            onSuccess = { onSuccess() },
            onError = onError
        )
    }

    fun cancelRepost(
        feedPost: FeedViewPost,
        onSuccess: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        wrapError(
            run = { postFeedRepository.cancelRepost(feedPost) },
            onSuccess = { onSuccess() },
            onError = onError
        )
    }

    fun createPost(
        content: String,
        image: ByteArray? = null,
        mimeType: String? = null,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit = {}
    ) {
        wrapError(run = {
            val output = if (image != null && mimeType != null) {
                postFeedRepository.uploadImage(image, mimeType)
            } else {
                null
            }
            postFeedRepository.createPost(content, output?.cid, mimeType)
            // TODO: Add id to _feedPostIds
            refreshPosts()
        }, onSuccess = { onSuccess() }, onError = onError)
    }

    fun createReply(
        content: String,
        feedViewPost: FeedViewPost,
        image: ByteArray? = null,
        mimeType: String? = null,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit = {}
    ) {
        wrapError(run = {
            val to = if (feedViewPost.reply == null) {
                val ref = feedViewPost.post.toStrongRef()
                PostReplyRef(root = ref, parent = ref)
            } else {
                val root = feedViewPost.reply.root.toStrongRef()
                val parent = feedViewPost.post.toStrongRef()
                PostReplyRef(root = root, parent = parent)
            }

            val output = if (image != null && mimeType != null) {
                postFeedRepository.uploadImage(image, mimeType)
            } else {
                null
            }
            postFeedRepository.createReply(content, to, output?.cid, mimeType)
            // TODO: Add id to _feedPostIds
            refreshPosts()
        }, onSuccess = { onSuccess() }, onError = onError)
    }

    fun deletePost(viewPost: FeedViewPost, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        wrapError(run = {
            postFeedRepository.deletePost(viewPost)
            _feedPostIds.value = _feedPostIds.value.minus(viewPost.id())
        }, onSuccess = { onSuccess() }, onError = onError)
    }

    fun reportPost(
        viewPost: FeedViewPost,
        reasonType: String,
        reason: String,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        wrapError(
            run = { postFeedRepository.reportPost(viewPost, reasonType, reason) },
            onSuccess = { onSuccess() },
            onError = onError
        )
    }

    fun mute(did: String, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        wrapError(
            run = { userRepository.mute(did) },
            onSuccess = { onSuccess() },
            onError = onError
        )
    }

    fun unmute(did: String, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        wrapError(
            run = { userRepository.unmute(did) },
            onSuccess = { onSuccess() },
            onError = onError
        )
    }

    private fun resizeImage(original: Bitmap): Bitmap {
        // NOTE: app.bsky.embed.images supports images up to 2000x2000px and 1MB
        val maxWidth = 2000
        val maxHeight = 2000

        if (original.width <= maxWidth && original.height <= maxHeight) {
            return original
        }

        val (width, height) = if (original.width >= original.height) {
            Pair(maxWidth, original.height * maxWidth / original.width)
        } else {
            Pair(original.width * maxHeight / original.height, maxHeight)
        }

        return Bitmap.createScaledBitmap(original, width, height, true)
    }

    fun convertToUploadableImage(source: ImageDecoder.Source): ByteArray {
        val bitmap = resizeImage(ImageDecoder.decodeBitmap(source))
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }
}
