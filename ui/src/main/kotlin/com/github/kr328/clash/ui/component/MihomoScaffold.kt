package com.github.kr328.clash.ui.component

import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.github.kr328.clash.common.R
import com.github.kr328.clash.ui.icon.BaselineArrowBack
import com.github.kr328.clash.ui.icon.MihomoIcons

@Composable
fun MihomoScaffold(
  title: String,
  modifier: Modifier = Modifier,
  onBackPressedDispatcher: OnBackPressedDispatcher? =
    LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher,
  onBack: () -> Unit = { onBackPressedDispatcher?.onBackPressed() },
  actions: @Composable RowScope.() -> Unit = {},
  scrollBehavior: TopAppBarScrollBehavior? = null,
  topBar: @Composable () -> Unit = {
    TopAppBar(
      title = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
      navigationIcon = {
        IconButton(onClick = onBack) {
          Icon(
            imageVector = MihomoIcons.BaselineArrowBack,
            contentDescription = stringResource(R.string.close),
          )
        }
      },
      actions = actions,
      scrollBehavior = scrollBehavior,
    )
  },
  snackbarHostState: SnackbarHostState? = null,
  snackbarHost: @Composable () -> Unit = {
    snackbarHostState?.let { SnackbarHost(hostState = it) }
  },
  content: @Composable (PaddingValues) -> Unit,
) {
  Scaffold(modifier = modifier, topBar = topBar, snackbarHost = snackbarHost, content = content)
}
