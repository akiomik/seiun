package io.github.akiomik.seiun.viewmodels

import android.util.Log
import androidx.lifecycle.viewModelScope
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.model.app.bsky.actor.Profile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class UserFeedViewModel : ApplicationViewModel() {
    sealed class State {
        object Init : State()
        object Loading : State()
        object Loaded : State()
        object Error : State()
    }

    private val postFeedRepository = SeiunApplication.instance!!.postFeedRepository
    private var _cursor: String? = null

    private val _feedPostIds: MutableStateFlow<Set<String>> = MutableStateFlow(emptySet())
    private val _profile = MutableStateFlow<Profile?>(null)
    private val _isRefreshing = MutableStateFlow(false)
    private val _seenAllFeed = MutableStateFlow(false)
    private val _state = MutableStateFlow<State>(State.Init)
    val profile = _profile.asStateFlow()
    val isRefreshing = _isRefreshing.asStateFlow()
    val seenAllFeed = _seenAllFeed.asStateFlow()
    val state = _state.asStateFlow()
    val feedViewPosts = postFeedRepository.feedPosts.combine(_feedPostIds) { feedPosts, feedPostIds ->
        feedPosts.filter { post -> feedPostIds.contains(post.id()) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFeed(profile: Profile, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        resetState()
        _profile.value = profile
        _state.value = State.Loading

        wrapError(
            run = { postFeedRepository.getAuthorFeed(author = profile) },
            onSuccess = {
                _state.value = State.Loaded
                _feedPostIds.value = it.feed.map { post -> post.id() }.toSet()
                _cursor = it.cursor
                onSuccess()
            },
            onError = {
                _state.value = State.Error
                onError(it)
            }
        )
    }

    fun refreshPosts(onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        Log.d(SeiunApplication.TAG, "Refresh posts of ${profile.value?.did}")

        _profile.value?.let { profile ->
            _isRefreshing.value = true

            wrapError(
                run = { postFeedRepository.getAuthorFeed(author = profile) },
                onComplete = { _isRefreshing.value = false },
                onSuccess = {
                    _feedPostIds.value = _feedPostIds.value.union(it.feed.map { post -> post.id() })
                    _cursor = it.cursor
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
                val data = postFeedRepository.getAuthorFeed(author = profile, before = _cursor)

                if (data.cursor != _cursor) {
                    if (data.feed.isNotEmpty()) {
                        _feedPostIds.value =
                            _feedPostIds.value.union(data.feed.map { post -> post.id() })
                        _cursor = data.cursor
                        Log.d(SeiunApplication.TAG, "Feed posts are updated")
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
        _cursor = null
        _feedPostIds.value = emptySet()
        _seenAllFeed.value = false
        _state.value = State.Init
    }
}
