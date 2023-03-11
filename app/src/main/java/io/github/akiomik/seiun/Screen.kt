package io.github.akiomik.seiun

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, @StringRes val resourceId: Int, val icon: ImageVector) {
    object Timeline : Screen("timeline", R.string.timeline, Icons.Filled.Home)
    object Notification : Screen("notification", R.string.notification, Icons.Filled.Notifications)
}
