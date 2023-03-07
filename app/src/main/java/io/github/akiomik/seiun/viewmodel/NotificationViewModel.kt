package io.github.akiomik.seiun.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.model.Notification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel : ApplicationViewModel() {
    sealed class State {
        object Loading : State()
        object Loaded : State()
    }

    private var _cursor = MutableLiveData<String>()
    private var _isRefreshing = MutableLiveData(false)
    private var _notifications = MutableLiveData<List<Notification>>()
    private var _seenAllNotifications = MutableLiveData(false)
    val isRefreshing = _isRefreshing as LiveData<Boolean>
    val notifications = _notifications as LiveData<List<Notification>>
    val seenAllNotifications = _seenAllNotifications as LiveData<Boolean>

    private var _state = MutableStateFlow<State>(State.Loading)
    val state = _state.asStateFlow()

    private val userRepository = SeiunApplication.instance!!.userRepository
    private val notificationRepository = SeiunApplication.instance!!.notificationRepository

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val data = withRetry(userRepository) { notificationRepository.listNotifications(it) }

            _notifications.postValue(
                mergeNotifications(
                    _notifications.value.orEmpty(),
                    data.notifications
                )
            )
            _state.value = State.Loaded
            _cursor.postValue(data.cursor)

            // NOTE: 50 is default limit of listNotifications
            if (data.notifications.size < 50) {
                _seenAllNotifications.postValue(true)
            }
        }
    }

    fun refreshNotifications(onError: (Throwable) -> Unit = {}) {
        if (_isRefreshing.value == true) {
            return
        }

        Log.d("Seiun", "Refresh notifications")
        _isRefreshing.postValue(true)

        wrapError(run = {
            val data = withRetry(userRepository) { notificationRepository.listNotifications(it) }
            if (data.cursor != _cursor.value) {
                val newNotifications =
                    mergeNotifications(_notifications.value.orEmpty(), data.notifications)
                _notifications.postValue(newNotifications)
                Log.d("Seiun", "Notifications are merged")
            } else {
                Log.d("Seiun", "Skip merge because cursor is unchanged")
            }
        }, onComplete = {
            _isRefreshing.postValue(false)
        }, onError = onError)
    }

    fun loadMoreNotifications(onError: (Throwable) -> Unit = {}) {
        Log.d("Seiun", "Load more notifications")

        wrapError(run = {
            val data = withRetry(userRepository) {
                notificationRepository.listNotifications(it, before = _cursor.value)
            }

            if (data.cursor != _cursor.value) {
                if (data.notifications.isNotEmpty()) {
                    val newNotifications = notifications.value.orEmpty() + data.notifications
                    _notifications.postValue(newNotifications)
                    _cursor.postValue(data.cursor)
                    _state.value = State.Loaded
                    Log.d("Seiun", "New notification count: ${newNotifications.size}")
                } else {
                    Log.d("Seiun", "No new feed posts")
                    _seenAllNotifications.postValue(true)
                }
            }
        }, onError = onError)
    }

    private fun mergeNotifications(
        currentPosts: List<Notification>,
        newPosts: List<Notification>
    ): List<Notification> {
        val top50Posts = currentPosts.take(50)// NOTE: 50 is default limit of getTimeline

        // TODO: improve merge logic
        return newPosts.reversed().fold(currentPosts) { acc, post ->
            if (!top50Posts.any { post.cid == it.cid && post.reason == it.reason }) {
                listOf(post) + acc
            } else {
                acc
            }
        }
    }
}