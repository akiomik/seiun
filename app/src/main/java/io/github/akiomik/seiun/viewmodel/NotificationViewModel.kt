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
        object Error : State()
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
            wrapError (run = {
                withRetry(userRepository) { notificationRepository.listNotifications(it) }
            }, onSuccess = {
                _notifications.postValue(it.notifications)

                // NOTE: 50 is default limit of listNotifications
                if (it.notifications.size < 50) {
                    _seenAllNotifications.postValue(true)
                }
                _cursor.postValue(it.cursor)
                _state.value = State.Loaded
            }, onError = {
                _notifications.postValue(emptyList())
                _state.value = State.Error
            })
        }
    }

    fun refreshNotifications(onError: (Throwable) -> Unit = {}) {
        if (_isRefreshing.value == true) {
            return
        }

        Log.d(SeiunApplication.TAG, "Refresh notifications")
        _isRefreshing.postValue(true)

        wrapError(run = {
            val data = withRetry(userRepository) { notificationRepository.listNotifications(it) }

            if (_cursor.value == null) {
                _cursor.postValue(data.cursor)
            }

            if (data.cursor != _cursor.value) {
                val newNotifications =
                    mergeNotifications(_notifications.value.orEmpty(), data.notifications)
                _notifications.postValue(newNotifications)
                Log.d(SeiunApplication.TAG, "Notifications are merged")
            } else {
                Log.d(SeiunApplication.TAG, "Skip merge because cursor is unchanged")
            }
        }, onComplete = {
            _isRefreshing.postValue(false)
        }, onError = onError)
    }

    fun loadMoreNotifications(onError: (Throwable) -> Unit = {}) {
        Log.d(SeiunApplication.TAG, "Load more notifications")

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
                    Log.d(SeiunApplication.TAG, "New notification count: ${newNotifications.size}")
                } else {
                    Log.d(SeiunApplication.TAG, "No new feed posts")
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