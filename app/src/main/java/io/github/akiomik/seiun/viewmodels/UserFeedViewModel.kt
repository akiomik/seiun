package io.github.akiomik.seiun.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.model.app.bsky.actor.ProfileViewDetailed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class UserFeedViewModel(val did: String) : ApplicationViewModel() {
    sealed class State {
        object Loading : State()
        object Loaded : State()
        object Error : State()
    }

    private val dummyProfile = ProfileViewDetailed(did = "dummy", handle = "dummy.bsky.social")
    private val postFeedRepository = SeiunApplication.instance!!.postFeedRepository
    private val userRepository = SeiunApplication.instance!!.userRepository
    private var _cursor: String? = null

    private val _feedPostIds: MutableStateFlow<Set<String>> = MutableStateFlow(emptySet())
    private val _profile = MutableStateFlow(dummyProfile)
    private val _isRefreshing = MutableStateFlow(false)
    private val _seenAllFeed = MutableStateFlow(false)
    private val _state = MutableStateFlow<State>(State.Loading)
    val profile = _profile.asStateFlow()
    val isRefreshing = _isRefreshing.asStateFlow()
    val seenAllFeed = _seenAllFeed.asStateFlow()
    val state = _state.asStateFlow()
    val feedViewPosts =
        postFeedRepository.feedPosts.combine(_feedPostIds) { feedPosts, feedPostIds ->
            feedPosts.filter { post -> feedPostIds.contains(post.id()) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        wrapError(
            run = {
                val profile = userRepository.getProfileOf(did)
                val feed = postFeedRepository.getAuthorFeed(author = profile)
                Pair(profile, feed)
            },
            onSuccess = { (profile, feed) ->
                _profile.value = profile
                _feedPostIds.value = feed.feed.map { post -> post.id() }.toSet()
                _cursor = feed.cursor
                _state.value = State.Loaded
            },
            onError = {
                _state.value = State.Error
            }
        )
    }

    fun refreshPosts(onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        val profile = _profile.value
        Log.d(SeiunApplication.TAG, "Refresh posts of ${profile.did}")
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

    fun loadMorePosts(onError: (Throwable) -> Unit = {}) {
        val profile = _profile.value
        Log.d(SeiunApplication.TAG, "Load more posts of ${profile.did}")

        wrapError(run = {
            val data = postFeedRepository.getAuthorFeed(author = profile, cursor = _cursor)

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

    fun follow(onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        val profile = _profile.value
        if (profile.viewer?.following != null) {
            return
        }

        wrapError(
            run = { userRepository.follow(profile.did) },
            onSuccess = {
                _profile.value = profile.copy(viewer = profile.viewer?.copy(following = it.uri))
                onSuccess()
            },
            onError = onError
        )
    }

    fun unfollow(onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        val profile = _profile.value
        if (profile.viewer?.following == null) {
            return
        }

        wrapError(
            run = { userRepository.unfollow(profile.viewer.following) },
            onSuccess = {
                _profile.value = profile.copy(viewer = profile.viewer.copy(following = null))
                onSuccess()
            },
            onError = onError
        )
    }

//    fun updateProfile(profile: Profile, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
//        wrapError(
//            run = { userRepository.updateProfile(profile) },
//            onSuccess = {
//                _profile.value = _profile.value?.copy(
//                    displayName = it.record.displayName,
//                    description = it.record.description,
//                    avatar = it.record.avatar?.cid,
//                    banner = it.record.banner?.cid
//                )
//                onSuccess()
//            },
//            onError = onError
//        )
//    }

    companion object {
        val didKey = object : CreationExtras.Key<String> {}

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val did = this[didKey]
                UserFeedViewModel(did!!)
            }
        }
    }
}
