package io.github.akiomik.seiun.viewmodels

import io.github.akiomik.seiun.model.app.bsky.feed.FeedViewPost
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserFeedViewModel : ApplicationViewModel() {
    sealed class State {
        object Loading : State()
        object Loaded : State()
        object Error : State()
    }

    private var _cursor = MutableStateFlow<String?>(null)
    private var _isRefreshing = MutableStateFlow(false)
    private var _feedViewPosts = MutableStateFlow<List<FeedViewPost>>(emptyList())
    private var _seenAllFeed = MutableStateFlow(false)
    private var _state = MutableStateFlow<State>(State.Loading)
    val isRefreshing = _isRefreshing.asStateFlow()
    val feedViewPosts = _feedViewPosts.asStateFlow()
    val seenAllFeed = _seenAllFeed.asStateFlow()
    val state = _state.asStateFlow()

    fun refreshPosts(onError: (Throwable) -> Unit) {
    }
}
