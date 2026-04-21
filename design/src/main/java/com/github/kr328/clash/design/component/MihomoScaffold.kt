package com.github.kr328.clash.design.component

import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.github.kr328.clash.design.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MihomoScaffold(
  title: String,
  modifier: Modifier = Modifier,
  onBackPressedDispatcher: OnBackPressedDispatcher? =
    LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher,
  onBack: () -> Unit = { onBackPressedDispatcher?.onBackPressed() },
  actions: @Composable RowScope.() -> Unit = {},
  scrollBehavior: TopAppBarScrollBehavior? = null,
  content: @Composable (PaddingValues) -> Unit,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(
              painter = painterResource(R.drawable.ic_baseline_arrow_back),
              contentDescription = stringResource(R.string.close),
            )
          }
        },
        actions = actions,
        scrollBehavior = scrollBehavior,
      )
    },
    content = content,
  )
}
