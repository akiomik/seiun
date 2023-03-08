package io.github.akiomik.seiun.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.model.FeedPost
import io.github.akiomik.seiun.model.FeedViewPost
import io.github.akiomik.seiun.model.Profile
import io.github.akiomik.seiun.model.StrongRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimelineViewModel : ApplicationViewModel() {
    sealed class State {
        object Loading : State()
        object Loaded : State()
        object Error: State()
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

    fun upvote(feedPost: FeedPost, onSuccess: () -> Unit = {}, onError: (Throwable) -> Unit = {}) {
        val ref = StrongRef(cid = feedPost.cid, uri = feedPost.uri)
        wrapError(run = {
            val result = withRetry(userRepository) { timelineRepository.upvote(it, ref) }
            updateFeedPost(feedPost.cid) { it.upvoted(result.upvote ?: "") }
        }, onSuccess = { onSuccess() }, onError = onError)
    }

    fun cancelVote(
        feedPost: FeedPost,
        onSuccess: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        val ref = StrongRef(cid = feedPost.cid, uri = feedPost.uri)
        wrapError(run = {
            withRetry(userRepository) { timelineRepository.cancelVote(it, ref) }
            updateFeedPost(feedPost.cid) { it.upvoteCanceled() }
        }, onSuccess = { onSuccess() }, onError = onError)
    }

    fun repost(feedPost: FeedPost, onSuccess: () -> Unit = {}, onError: (Throwable) -> Unit = {}) {
        val ref = StrongRef(cid = feedPost.cid, uri = feedPost.uri)
        wrapError(run = {
            val response = withRetry(userRepository) { timelineRepository.repost(it, ref) }
            updateFeedPost(feedPost.cid) { it.reposted(response.uri) }
        }, onSuccess = { onSuccess() }, onError = onError)
    }

    fun cancelRepost(
        feedPost: FeedPost,
        onSuccess: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        if (feedPost.viewer.repost == null) {
            return
        }

        wrapError(run = {
            withRetry(userRepository) {
                timelineRepository.cancelRepost(it, feedPost.viewer.repost)
            }
            updateFeedPost(feedPost.cid) { it.repostCanceled() }
        }, onSuccess = { onSuccess() }, onError = onError)
    }

    fun createPost(content: String, onSuccess: () -> Unit, onError: (Throwable) -> Unit = {}) {
        wrapError(run = {
            withRetry(userRepository) { timelineRepository.createPost(it, content) }
            refreshPosts()
        }, onSuccess = { onSuccess() }, onError = onError)
    }

    // To avoid race conditions, we must use current feedPost instead of arguments (#7)
    private fun updateFeedPost(cid: String, run: (FeedPost) -> FeedPost) {
        val index = feedViewPosts.value?.indexOfFirst { it.post.cid == cid } ?: -1
        val target = feedViewPosts.value?.get(index)?.post
        if (index >= 0 && target != null) {
            _feedViewPosts.postValue(updateFeedPostOf(feedViewPosts.value.orEmpty(), run(target)))
        }
    }

    private fun updateFeedPostOf(feedViewPosts: List<FeedViewPost>, feedPost: FeedPost): List<FeedViewPost> {
        return feedViewPosts.map {
            if (it.post.uri === feedPost.uri) {
                it.copy(post = feedPost)
            } else {
                it
            }
        }
    }

    private fun updateFeedPostAt(feedViewPosts: List<FeedViewPost>, index: Int, feedPost: FeedPost): List<FeedViewPost> {
        val posts = feedViewPosts.toMutableList()
        val feedViewPost = feedViewPosts[index]
        posts[index] = feedViewPost.copy(post = feedPost)
        return posts
    }

    private fun mergeFeedViewPosts(
        currentPosts: List<FeedViewPost>,
        newPosts: List<FeedViewPost>
    ): List<FeedViewPost> {
        // TODO: improve merge logic
        return newPosts.reversed().fold(currentPosts) { acc, post ->
            val index =  acc.indexOfFirst { post.post.cid == it.post.cid && post.reason == it.reason }
            if (index >= 0) {
                updateFeedPostAt(acc, index, post.post)
            } else {
                listOf(post) + acc
            }
        }
    }
}