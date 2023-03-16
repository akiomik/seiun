package io.github.akiomik.seiun.viewmodels

import android.util.Log
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

    private var _cursor = MutableStateFlow<String?>(null)
    private var _isRefreshing = MutableStateFlow(false)
    private var _notifications = MutableStateFlow<List<Notification>>(emptyList())
    private var _seenAllNotifications = MutableStateFlow(false)
    private var _state = MutableStateFlow<State>(State.Loading)
    val isRefreshing = _isRefreshing.asStateFlow()
    val notifications = _notifications.asStateFlow()
    val seenAllNotifications = _seenAllNotifications.asStateFlow()
    val state = _state.asStateFlow()

    private val notificationRepository = SeiunApplication.instance!!.notificationRepository

    init {
        viewModelScope.launch(Dispatchers.IO) {
            wrapError(run = {
                val notifications = notificationRepository.listNotifications()
                notificationRepository.updateNotificationSeen(Date())
                notifications
            }, onSuccess = {
                    _notifications.value = it.notifications

                    // NOTE: 50 is default limit of listNotifications
                    if (it.notifications.size < 50) {
                        _seenAllNotifications.value = true
                    }
                    _cursor.value = it.cursor
                    _state.value = State.Loaded
                }, onError = {
                    _state.value = State.Error
                })
        }
    }

    fun refreshNotifications(onError: (Throwable) -> Unit = {}) {
        if (_isRefreshing.value) {
            return
        }

        Log.d(SeiunApplication.TAG, "Refresh notifications")
        _isRefreshing.value = true

        wrapError(run = {
            val notifications = notificationRepository.listNotifications()
            notificationRepository.updateNotificationSeen(Date())

            if (_cursor.value == null) {
                _cursor.value = notifications.cursor
            }

            // NOTE: Update always for updating isRead
            val newNotifications =
                mergeNotifications(_notifications.value.orEmpty(), notifications.notifications)
            _notifications.value = newNotifications
            Log.d(SeiunApplication.TAG, "Notifications are merged")
        }, onComplete = {
                _isRefreshing.value = false
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
                    _notifications.value = newNotifications
                    _cursor.value = res.cursor
                    _state.value = State.Loaded
                    Log.d(SeiunApplication.TAG, "New notification count: ${newNotifications.size}")
                } else {
                    Log.d(SeiunApplication.TAG, "No new feed posts")
                    _seenAllNotifications.value = true
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
