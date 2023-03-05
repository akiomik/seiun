package io.github.akiomik.seiun

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.akiomik.seiun.model.FeedViewPost
import io.github.akiomik.seiun.model.Timeline
import io.github.akiomik.seiun.service.UnauthorizedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

class TimelineViewModel : ViewModel() {
    sealed class State {
        object Loading: State()
        object Loaded: State()
    }

    private var _cursor = MutableLiveData<String>()
    private var _feedViewPosts = MutableLiveData<List<FeedViewPost>>()
    val feedViewPosts = _feedViewPosts as LiveData<List<FeedViewPost>>

    private var _state = MutableStateFlow<State>(State.Loading)
    val state = _state.asStateFlow()

    private val userRepository = SeiunApplication.instance!!.userRepository
    private val timelineRepository = SeiunApplication.instance!!.timelineRepository

    init {
        viewModelScope.launch(Dispatchers.IO) {
//            while (isActive) {
                val session = userRepository.getSession()
                val data = try {
                    timelineRepository.getTimeline(session)
                } catch (e: UnauthorizedException) {
                    Log.d("Seiun", "Retrying to execute getTimeline")
                    val session = userRepository.refresh()
                    timelineRepository.getTimeline(session)
                }

                _feedViewPosts.postValue(mergeFeedViewPosts(_feedViewPosts.value.orEmpty(), data.feed))
                if (_cursor.value == null) {
                    _cursor.postValue(data.cursor)
                }
                _state.value = State.Loaded
                delay(10 * 1000)
//            }
        }
    }

    fun loadMorePosts() {
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
        val lastPostCid = newPosts.lastOrNull()?.post?.cid
        val top50Posts = currentPosts.take(50)// NOTE: 50 is default limit of getTimeline

        if (!top50Posts.any { lastPostCid == it.post.cid }) {
            return newPosts + currentPosts
        }

        return newPosts.reversed().fold(emptyList()) { acc, post ->
            if (!top50Posts.any { post.post.cid == it.post.cid }) {
                return listOf(post) + acc
            }
            return acc
        }
    }
}