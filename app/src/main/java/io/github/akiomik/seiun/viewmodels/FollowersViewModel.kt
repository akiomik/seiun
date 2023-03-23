package io.github.akiomik.seiun.viewmodels

import android.util.Log
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.model.app.bsky.actor.WithInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FollowersViewModel(val did: String) : ApplicationViewModel() {
    sealed class State {
        object Loading : State()
        object Loaded : State()
        data class Error(val error: Throwable) : State()
    }

    private val userRepository = SeiunApplication.instance!!.userRepository
    private var _cursor: String? = null

    private val _seenAllFollows = MutableStateFlow(false)
    private val _state = MutableStateFlow<State>(State.Loading)
    private val _followers = MutableStateFlow<List<WithInfo>>(emptyList())
    val seenAllFollowers = _seenAllFollows.asStateFlow()
    val state = _state.asStateFlow()
    val followers = _followers.asStateFlow()

    init {
        wrapError(
            run = { userRepository.getFollowers(did) },
            onSuccess = {
                Log.d(SeiunApplication.TAG, it.toString())
                _followers.value = it.followers
                _cursor = it.cursor
                _state.value = State.Loaded
            },
            onError = {
                Log.d(SeiunApplication.TAG, it.toString())
                _state.value = State.Error(it)
            }
        )
    }

    fun loadMoreFollowers(onError: (Throwable) -> Unit = {}) {
        wrapError(run = {
            val data = userRepository.getFollowers(did = did, cursor = _cursor)

            if (data.cursor != _cursor) {
                if (data.followers.isNotEmpty()) {
                    _followers.value = _followers.value + data.followers
                    _cursor = data.cursor
                    Log.d(SeiunApplication.TAG, "Followers are updated")
                } else {
                    Log.d(SeiunApplication.TAG, "No followers")
                    _seenAllFollows.value = true
                }
            }
        }, onError = onError)
    }
}
