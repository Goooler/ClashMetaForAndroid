package com.github.kr328.clash.design.component

import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.github.kr328.clash.design.R

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SettingsCommonScreen(
  title: String,
  modifier: Modifier = Modifier,
  onBackPressedDispatcher: OnBackPressedDispatcher? =
    LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher,
  onBack: () -> Unit = { onBackPressedDispatcher?.onBackPressed() },
  content: @Composable ColumnScope.() -> Unit,
) {
  MihomoScaffold(
    title = title,
    modifier = modifier,
    onBackPressedDispatcher = onBackPressedDispatcher,
    onBack = onBack,
    scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
  ) { innerPadding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState())
    ) {
      content()
    }
  }
}

@Composable
fun SettingsTipsItem(text: AnnotatedString, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier.padding(vertical = 16.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Spacer(modifier = Modifier.width(20.dp))
    Image(
      painter = painterResource(R.drawable.ic_outline_info),
      contentDescription = null,
      modifier = Modifier.size(25.dp),
    )
    Spacer(modifier = Modifier.width(20.dp))
    Text(
      text = text,
      style = MaterialTheme.typography.bodyMedium,
      modifier = Modifier.padding(end = 20.dp),
    )
  }
}

@Composable
fun SettingsCategoryTitle(text: String, modifier: Modifier = Modifier) {
  Text(
    text = text,
    style = MaterialTheme.typography.bodyMedium,
    color = MaterialTheme.colorScheme.primary,
    modifier =
      modifier.fillMaxWidth().padding(start = 65.dp, end = 20.dp, top = 16.dp, bottom = 16.dp),
  )
}

@Composable
fun SettingsClickableItem(
  title: String,
  summary: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(top = 16.dp, bottom = 16.dp, end = 20.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Spacer(modifier = Modifier.width(65.dp))
    Column {
      Text(text = title, style = MaterialTheme.typography.bodyLarge)
      Text(
        text = summary,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = 5.dp),
      )
    }
  }
}
