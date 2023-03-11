package io.github.akiomik.seiun.viewmodel

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.model.ISession
import io.github.akiomik.seiun.model.app.bsky.actor.Profile
import io.github.akiomik.seiun.model.app.bsky.feed.FeedViewPost
import io.github.akiomik.seiun.model.app.bsky.feed.PostReplyRef
import io.github.akiomik.seiun.model.app.bsky.feed.PostView
import io.github.akiomik.seiun.model.com.atproto.repo.StrongRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class TimelineViewModel : ApplicationViewModel() {
    sealed class State {
        object Loading : State()
        object Loaded : State()
        object Error : State()
    }

    private var _cursor = MutableLiveData<String>()
    private var _isRefreshing = MutableLiveData(false)
    private var _feedViewPosts = MutableLiveData<List<FeedViewPost>>()
    private var _seenAllFeed = MutableLiveData(false)
    private var _profile = MutableLiveData<Profile>()
    val isRefreshing = _isRefreshing as LiveData<Boolean>
    val feedViewPosts = _feedViewPosts as LiveData<List<FeedViewPost>>
    val seenAllFeed = _seenAllFeed as LiveData<Boolean>
    val profile = _profile as LiveData<Profile>

    private var _state = MutableStateFlow<State>(State.Loading)
    val state = _state.asStateFlow()

    private val userRepository = SeiunApplication.instance!!.userRepository
    private val timelineRepository = SeiunApplication.instance!!.timelineRepository

    init {
        viewModelScope.launch(Dispatchers.IO) {
            wrapError(run = {
                val timeline = withRetry(userRepository) { timelineRepository.getTimeline(it) }
                val profile = withRetry(userRepository) { userRepository.getProfile(it) }
                Pair(timeline, profile)
            }, onSuccess = { (timeline, profile) ->
                    // NOTE: 50 is default limit of getTimeline
                    if (timeline.feed.size < 50) {
                        _seenAllFeed.postValue(true)
                    }

                    _profile.postValue(profile)
                    _feedViewPosts.postValue(timeline.feed)
                    _cursor.postValue(timeline.cursor)
                    _state.value = State.Loaded
                }, onError = {
                    Log.d(SeiunApplication.TAG, "Error occurred: $it")
                    _feedViewPosts.postValue(emptyList())
                    _state.value = State.Error
                })
        }
    }

    fun refreshPosts(onError: (Throwable) -> Unit = {}) {
        if (_isRefreshing.value == true) {
            return
        }

        Log.d(SeiunApplication.TAG, "Refresh timeline")
        _isRefreshing.postValue(true)

        wrapError(run = {
            val data = withRetry(userRepository) { timelineRepository.getTimeline(it) }

            if (_cursor.value == null) {
                _cursor.postValue(data.cursor)
            }

            if (data.cursor != _cursor.value) {
                val newFeedPosts = mergeFeedViewPosts(_feedViewPosts.value.orEmpty(), data.feed)
                _feedViewPosts.postValue(newFeedPosts)
                Log.d(SeiunApplication.TAG, "Feed posts are merged")
            } else {
                Log.d(SeiunApplication.TAG, "Skip merge because cursor is unchanged")
            }
        }, onComplete = {
                _isRefreshing.postValue(false)
            }, onError = onError)
    }

    fun loadMorePosts(onError: (Throwable) -> Unit = {}) {
        Log.d(SeiunApplication.TAG, "Load more posts")

        wrapError(run = {
            val data = withRetry(userRepository) {
                timelineRepository.getTimeline(it, before = _cursor.value)
            }

            if (data.cursor != _cursor.value) {
                if (data.feed.isNotEmpty()) {
                    val newFeedPosts = feedViewPosts.value.orEmpty() + data.feed
                    _feedViewPosts.postValue(newFeedPosts)
                    _cursor.postValue(data.cursor)
                    _state.value = State.Loaded
                    Log.d(SeiunApplication.TAG, "New feed count: ${newFeedPosts.size}")
                } else {
                    Log.d(SeiunApplication.TAG, "No new feed posts")
                    _seenAllFeed.postValue(true)
                }
            }
        }, onError = onError)
    }

    fun upvote(post: PostView, onSuccess: () -> Unit = {}, onError: (Throwable) -> Unit = {}) {
        val ref = StrongRef(cid = post.cid, uri = post.uri)
        wrapError(run = {
            val result = withRetry(userRepository) { timelineRepository.upvote(it, ref) }
            updateFeedPost(post.cid) { it.upvoted(result.upvote ?: "") }
        }, onSuccess = { onSuccess() }, onError = onError)
    }

    fun cancelVote(
        post: PostView,
        onSuccess: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        val ref = StrongRef(cid = post.cid, uri = post.uri)
        wrapError(run = {
            withRetry(userRepository) { timelineRepository.cancelVote(it, ref) }
            updateFeedPost(post.cid) { it.upvoteCanceled() }
        }, onSuccess = { onSuccess() }, onError = onError)
    }

    fun repost(post: PostView, onSuccess: () -> Unit = {}, onError: (Throwable) -> Unit = {}) {
        val ref = StrongRef(cid = post.cid, uri = post.uri)
        wrapError(run = {
            val response = withRetry(userRepository) { timelineRepository.repost(it, ref) }
            updateFeedPost(post.cid) { it.reposted(response.uri) }
        }, onSuccess = { onSuccess() }, onError = onError)
    }

    fun cancelRepost(
        post: PostView,
        onSuccess: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        if (post.viewer.repost == null) {
            return
        }

        wrapError(run = {
            withRetry(userRepository) {
                timelineRepository.cancelRepost(it, post.viewer.repost)
            }
            updateFeedPost(post.cid) { it.repostCanceled() }
        }, onSuccess = { onSuccess() }, onError = onError)
    }

    fun createPost(
        content: String,
        image: ByteArray? = null,
        mimeType: String? = null,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit = {}
    ) {
        wrapError(run = {
            withRetry(userRepository) { session: ISession ->
                val output = if (image != null && mimeType != null) {
                    timelineRepository.uploadImage(session, image, mimeType)
                } else {
                    null
                }
                timelineRepository.createPost(session, content, output?.cid, mimeType)
            }
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

            withRetry(userRepository) { session: ISession ->
                val output = if (image != null && mimeType != null) {
                    timelineRepository.uploadImage(session, image, mimeType)
                } else {
                    null
                }
                timelineRepository.createReply(session, content, to, output?.cid, mimeType)
            }
            refreshPosts()
        }, onSuccess = { onSuccess() }, onError = onError)
    }

    fun deletePost(viewPost: FeedViewPost, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        wrapError(run = {
            withRetry(userRepository) { session: ISession ->
                timelineRepository.deletePost(session, viewPost)
                deleteFeedPost(viewPost.post.cid)
            }
            refreshPosts()
        }, onSuccess = { onSuccess() }, onError = onError)
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

    private fun deleteFeedPost(cid: String) {
        val index = feedViewPosts.value?.indexOfFirst { it.post.cid == cid } ?: -1
        val target = feedViewPosts.value?.get(index)?.post
        if (index >= 0 && target != null) {
            _feedViewPosts.postValue(deleteFeedPostAt(feedViewPosts.value.orEmpty(), index))
        }
    }

    private fun deleteFeedPostAt(
        feedViewPosts: List<FeedViewPost>,
        index: Int
    ): List<FeedViewPost> {
        val posts = feedViewPosts.toMutableList()
        posts.removeAt(index)
        return posts
    }

    // To avoid race conditions, we must use current feedPost instead of arguments (#7)
    private fun updateFeedPost(cid: String, run: (PostView) -> PostView) {
        val index = feedViewPosts.value?.indexOfFirst { it.post.cid == cid } ?: -1
        val target = feedViewPosts.value?.get(index)?.post
        if (index >= 0 && target != null) {
            _feedViewPosts.postValue(updateFeedPostOf(feedViewPosts.value.orEmpty(), run(target)))
        }
    }

    private fun updateFeedPostOf(
        feedViewPosts: List<FeedViewPost>,
        post: PostView
    ): List<FeedViewPost> {
        return feedViewPosts.map {
            if (it.post.uri === post.uri) {
                it.copy(post = post)
            } else {
                it
            }
        }
    }

    private fun updateFeedPostAt(
        feedViewPosts: List<FeedViewPost>,
        index: Int,
        post: PostView
    ): List<FeedViewPost> {
        val posts = feedViewPosts.toMutableList()
        val feedViewPost = feedViewPosts[index]
        posts[index] = feedViewPost.copy(post = post)
        return posts
    }

    private fun mergeFeedViewPosts(
        currentPosts: List<FeedViewPost>,
        newPosts: List<FeedViewPost>
    ): List<FeedViewPost> {
        // TODO: improve merge logic
        return newPosts.reversed().fold(currentPosts) { acc, post ->
            val index =
                acc.indexOfFirst { post.post.cid == it.post.cid && post.reason == it.reason }
            if (index >= 0) {
                updateFeedPostAt(acc, index, post.post)
            } else {
                listOf(post) + acc
            }
        }
    }
}
