package io.github.akiomik.seiun

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.akiomik.seiun.model.Session
import io.github.akiomik.seiun.model.Timeline
import com.example.catpaw.services.AtpService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
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
                    val data = timelineRepository.getTimeline(session)
                    _state.value = State.Data(data)
                }
                delay(10 * 1000)
            }
        }
    }
}