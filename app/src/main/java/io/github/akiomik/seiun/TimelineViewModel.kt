package io.github.akiomik.seiun

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.akiomik.seiun.model.Timeline
import io.github.akiomik.seiun.service.UnauthorizedException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

class TimelineViewModel : ViewModel() {
    sealed class State {
        object Loading: State()
        data class Data(val timeline: Timeline): State()
    }

    private var _state = MutableStateFlow<State>(State.Loading)
    val state = _state.asStateFlow()

    private val userRepository = SeiunApplication.instance!!.userRepository
    private val timelineRepository = SeiunApplication.instance!!.timelineRepository

    init {
        viewModelScope.launch {
            while (isActive) {
                // TODO: improve thread handling
                thread {
                    val session = userRepository.getSession()
                    try {
                        val data = timelineRepository.getTimeline(session)
                        _state.value = State.Data(data)
                    } catch (e: UnauthorizedException) {
                        Log.d("Seiun", "Retrying to execute getTimeline")
                        val session = userRepository.refresh()
                        val data = timelineRepository.getTimeline(session)
                        _state.value = State.Data(data)
                    }
                }
                delay(10 * 1000)
            }
        }
    }
}