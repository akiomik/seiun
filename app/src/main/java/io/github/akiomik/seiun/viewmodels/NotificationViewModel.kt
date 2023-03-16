package io.github.akiomik.seiun.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.model.app.bsky.notification.Notification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

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

    private val notificationRepository = SeiunApplication.instance!!.notificationRepository

    init {
        viewModelScope.launch(Dispatchers.IO) {
            wrapError(run = {
                val notifications = notificationRepository.listNotifications()
                notificationRepository.updateNotificationSeen(Date())
                notifications
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
            val notifications = notificationRepository.listNotifications()
            notificationRepository.updateNotificationSeen(Date())

            if (_cursor.value == null) {
                _cursor.postValue(notifications.cursor)
            }

            // NOTE: Update always for updating isRead
            val newNotifications =
                mergeNotifications(_notifications.value.orEmpty(), notifications.notifications)
            _notifications.postValue(newNotifications)
            Log.d(SeiunApplication.TAG, "Notifications are merged")
        }, onComplete = {
                _isRefreshing.postValue(false)
                SeiunApplication.instance!!.clearNotifications()
            }, onError = onError)
    }

    fun loadMoreNotifications(onError: (Throwable) -> Unit = {}) {
        Log.d(SeiunApplication.TAG, "Load more notifications")

        wrapError(run = {
            val res = notificationRepository.listNotifications(before = _cursor.value)

            if (res.cursor != _cursor.value) {
                if (res.notifications.isNotEmpty()) {
                    val newNotifications = notifications.value.orEmpty() + res.notifications
                    _notifications.postValue(newNotifications)
                    _cursor.postValue(res.cursor)
                    _state.value = State.Loaded
                    Log.d(SeiunApplication.TAG, "New notification count: ${newNotifications.size}")
                } else {
                    Log.d(SeiunApplication.TAG, "No new feed posts")
                    _seenAllNotifications.postValue(true)
                }
            }
        }, onError = onError)
    }

    private fun updateNotificationAt(
        notifications: List<Notification>,
        index: Int,
        notification: Notification
    ): List<Notification> {
        val mutableNotifications = notifications.toMutableList()
        mutableNotifications[index] = notification
        return mutableNotifications
    }

    private fun mergeNotifications(
        currentNotifications: List<Notification>,
        newPosts: List<Notification>
    ): List<Notification> {
        // TODO: improve merge logic
        return newPosts.reversed().fold(currentNotifications) { acc, post ->
            val index = acc.indexOfFirst { post.cid == it.cid && post.reason == it.reason }
            if (index >= 0) {
                updateNotificationAt(acc, index, post)
            } else {
                listOf(post) + acc
            }
        }
    }
}
