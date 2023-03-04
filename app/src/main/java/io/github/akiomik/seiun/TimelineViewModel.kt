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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

class TimelineViewModel : ViewModel() {
    sealed class State {
        object Loading: State()
        object Loaded: State()
    }

    private var feedViewPosts = MutableLiveData<List<FeedViewPost>>()
    private var _state = MutableStateFlow<State>(State.Loading)
    val state = _state.asStateFlow()

    private val userRepository = SeiunApplication.instance!!.userRepository
    private val timelineRepository = SeiunApplication.instance!!.timelineRepository

    init {
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                val session = userRepository.getSession()
                val data = try {
                    timelineRepository.getTimeline(session)
                } catch (e: UnauthorizedException) {
                    Log.d("Seiun", "Retrying to execute getTimeline")
                    val session = userRepository.refresh()
                    timelineRepository.getTimeline(session)
                }

                feedViewPosts.postValue(data.feed)
                _state.value = State.Loaded
                delay(10 * 1000)
            }
        }
    }

    fun getPosts(): LiveData<List<FeedViewPost>> {
        return feedViewPosts
    }
}