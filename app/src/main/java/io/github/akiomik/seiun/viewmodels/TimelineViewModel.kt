package io.github.akiomik.seiun.viewmodels

import android.util.Log
import androidx.lifecycle.viewModelScope
import io.github.akiomik.seiun.SeiunApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class TimelineViewModel : ApplicationViewModel() {
    sealed class State {
        object Loading : State()
        object Loaded : State()
        object Error : State()
    }

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
}
