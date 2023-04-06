package io.github.akiomik.seiun.ui.app

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import io.github.akiomik.seiun.R
import io.github.akiomik.seiun.model.app.bsky.actor.ProfileViewDetailed
import io.github.akiomik.seiun.viewmodels.AppViewModel
import kotlinx.coroutines.launch

@Composable
private fun Profile(onClicked: (ProfileViewDetailed) -> Unit) {
    val viewModel: AppViewModel = viewModel()
    val profile by viewModel.profile.collectAsState()

    Row(
        modifier = Modifier
            .padding(16.dp)
            .clickable { profile?.let(onClicked) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Avatar(profile = profile)
        NameAndHandle(profile)
    }
}

@Composable
private fun Avatar(profile: ProfileViewDetailed?) {
    AsyncImage(
        model = profile?.avatar,
        contentDescription = null,
        modifier = Modifier
            .width(48.dp)
            .height(48.dp)
            .clip(CircleShape)
    )
}

@Composable
private fun NameAndHandle(profile: ProfileViewDetailed?) {
    Column {
        Text(
            text = profile?.displayName.orEmpty(),
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = profile?.handle?.let { "@$it" }.orEmpty(),
            color = Color.Gray,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun AppDrawer(
    state: DrawerState,
    enabled: Boolean,
    onProfileClick: (ProfileViewDetailed) -> Unit,
    content: @Composable () -> Unit
) {
    val viewModel: AppViewModel = viewModel()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isAutoTranslationEnabled by remember { mutableStateOf(viewModel.isAutoTranslationEnabled()) }

    ModalNavigationDrawer(
        drawerState = state,
        gesturesEnabled = enabled,
        drawerContent = {
            ModalDrawerSheet {
                if (enabled) {
                    Profile {
                        scope.launch { state.close() }
                        onProfileClick(it)
                    }
                    Divider()
                }

                NavigationDrawerItem(
                    selected = false,
                    onClick = {
                        scope.launch { state.close() }

                        val intent = Intent(context, OssLicensesMenuActivity::class.java)
                        startActivity(context, intent, null)

                        // NOTE: Do NOT update selected for license
                    },
                    label = { Text(stringResource(R.string.license)) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    selected = false,
                    onClick = {
                        isAutoTranslationEnabled = !isAutoTranslationEnabled
                        viewModel.updateIsAutoTranslationEnabled(isAutoTranslationEnabled)
                    },
                    label = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Switch(
                                checked = isAutoTranslationEnabled,
                                onCheckedChange = {
                                    isAutoTranslationEnabled = it
                                    viewModel.updateIsAutoTranslationEnabled(it)
                                }
                            )
                            Text(stringResource(R.string.auto_translation))
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        },
        content = content
    )
}
