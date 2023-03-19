package io.github.akiomik.seiun.viewmodels

import android.util.Log
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.model.app.bsky.actor.Profile
import io.github.akiomik.seiun.model.app.bsky.feed.FeedViewPost
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserFeedViewModel : ApplicationViewModel() {
    sealed class State {
        object Init : State()
        object Loading : State()
        object Loaded : State()
        object Error : State()
    }

    private val timelineRepository = SeiunApplication.instance!!.timelineRepository

    private var _profile = MutableStateFlow<Profile?>(null)
    private var _cursor = MutableStateFlow<String?>(null)
    private var _isRefreshing = MutableStateFlow(false)
    private var _feedViewPosts = MutableStateFlow<List<FeedViewPost>>(emptyList())
    private var _seenAllFeed = MutableStateFlow(false)
    private var _state = MutableStateFlow<State>(State.Init)
    val profile = _profile.asStateFlow()
    val isRefreshing = _isRefreshing.asStateFlow()
    val feedViewPosts = _feedViewPosts.asStateFlow()
    val seenAllFeed = _seenAllFeed.asStateFlow()
    val state = _state.asStateFlow()

    fun setFeed(profile: Profile, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        resetState()
        _profile.value = profile
        _state.value = State.Loading

        wrapError(
            run = { timelineRepository.getAuthorFeed(author = profile) },
            onSuccess = {
                _state.value = State.Loaded
                _feedViewPosts.value = it.feed
                _cursor.value = it.cursor
                onSuccess()
            },
            onError = {
                _state.value = State.Error
                onError(it)
            }
        )
    }

    fun refreshPosts(onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        _profile.value?.let { profile ->
            _isRefreshing.value = true

            wrapError(
                run = { timelineRepository.getAuthorFeed(author = profile) },
                onComplete = { _isRefreshing.value = false },
                onSuccess = {
                    _feedViewPosts.value = it.feed // TODO: merge feed posts
                    _cursor.value = it.cursor
                    onSuccess()
                },
                onError = {
                    _state.value = State.Error
                    onError(it)
                }
            )
        }
    }

    fun loadMorePosts(onError: (Throwable) -> Unit = {}) {
        Log.d(SeiunApplication.TAG, "Load more posts of ${profile.value?.did}")

        _profile.value?.let { profile ->
            wrapError(run = {
                val data =
                    timelineRepository.getAuthorFeed(author = profile, before = _cursor.value)

                if (data.cursor != _cursor.value) {
                    if (data.feed.isNotEmpty()) {
                        val newFeedPosts = feedViewPosts.value + data.feed
                        _feedViewPosts.value = newFeedPosts
                        _cursor.value = data.cursor
                        Log.d(SeiunApplication.TAG, "New feed count: ${newFeedPosts.size}")
                    } else {
                        Log.d(SeiunApplication.TAG, "No new feed posts")
                        _seenAllFeed.value = true
                    }
                }
            }, onError = onError)
        }
    }

    private fun resetState() {
        _profile.value = null
        _cursor.value = null
        _feedViewPosts.value = emptyList()
        _seenAllFeed.value = false
        _state.value = State.Init
    }
}
