package io.github.akiomik.seiun

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.akiomik.seiun.model.FeedViewPost
import io.github.akiomik.seiun.service.UnauthorizedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimelineViewModel : ViewModel() {
    sealed class State {
        object Loading: State()
        object Loaded: State()
    }

    private var _cursor = MutableLiveData<String>()
    private var _isRefreshing =MutableLiveData<Boolean>(false)
    private var _feedViewPosts = MutableLiveData<List<FeedViewPost>>()
    val isRefreshing = _isRefreshing as LiveData<Boolean>
    val feedViewPosts = _feedViewPosts as LiveData<List<FeedViewPost>>

    private var _state = MutableStateFlow<State>(State.Loading)
    val state = _state.asStateFlow()

    private val userRepository = SeiunApplication.instance!!.userRepository
    private val timelineRepository = SeiunApplication.instance!!.timelineRepository

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val session = userRepository.getSession()
            val data = try {
                timelineRepository.getTimeline(session)
            } catch (e: UnauthorizedException) {
                Log.d("Seiun", "Retrying to execute getTimeline")
                val session = userRepository.refresh()
                timelineRepository.getTimeline(session)
            }

            _feedViewPosts.postValue(mergeFeedViewPosts(_feedViewPosts.value.orEmpty(), data.feed))
            _state.value = State.Loaded
        }
    }

    fun refreshPosts() {
        if (_isRefreshing.value == true) {
           return
        }

        Log.d("Seiun", "Refresh timeline")
        _isRefreshing.value = true

        viewModelScope.launch(Dispatchers.IO) {
            val session = userRepository.getSession()
            val data = try {
                timelineRepository.getTimeline(session)
            } catch (e: UnauthorizedException) {
                Log.d("Seiun", "Retrying to execute getTimeline")
                val session = userRepository.refresh()
                timelineRepository.getTimeline(session)
            } finally {
                // TODO: update isRefreshing when feedViewPosts is updated
                _isRefreshing.postValue(false)
            }

            if (data.cursor != _cursor.value) {
                val newFeedPosts = mergeFeedViewPosts(_feedViewPosts.value.orEmpty(), data.feed)
                _feedViewPosts.postValue(newFeedPosts)
                Log.d("Seiun", "Feed posts are merged")
            } else {
                Log.d("Seiun", "Skip merge because cursor is unchanged")
            }
        }
    }

    fun loadMorePosts() {
        Log.d("Seiun", "Load more posts")

        viewModelScope.launch(Dispatchers.IO) {
            val session = userRepository.getSession()

            val data = try {
                timelineRepository.getTimeline(session, before = _cursor.value)
            } catch (e: UnauthorizedException) {
                Log.d("Seiun", "Retrying to execute getTimeline")
                val session = userRepository.refresh()
                timelineRepository.getTimeline(session, before = _cursor.value)
            }

            if (data.cursor != _cursor.value) {
                val newFeedPosts = feedViewPosts.value.orEmpty() + data.feed
                _feedViewPosts.postValue(newFeedPosts)
                _cursor.postValue(data.cursor)
                _state.value = State.Loaded
                Log.d("Seiun", "new feed count: ${newFeedPosts.size}")
            }
        }
    }

    private fun mergeFeedViewPosts(currentPosts: List<FeedViewPost>, newPosts: List<FeedViewPost>): List<FeedViewPost> {
        val top50Posts = currentPosts.take(50)// NOTE: 50 is default limit of getTimeline

        // TODO: improve merge logic
        return newPosts.reversed().fold(currentPosts) { acc, post ->
            if (!top50Posts.any { post.post.cid == it.post.cid && post.reason == it.reason }) {
                listOf(post) + acc
            } else {
                acc
            }
        }
    }
}