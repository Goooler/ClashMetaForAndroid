package com.github.kr328.clash.design

import android.content.Context
import android.net.Uri
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.kr328.clash.design.ui.theme.MihomoDesignTheme

class HelpDesign(context: Context, openLink: (Uri) -> Unit) : Design<Unit>(context) {
  private val composeRoot = composeView {
    MihomoDesignTheme { HelpScreen(modifier = Modifier.fillMaxSize(), openLink = openLink) }
  }

  override val root: View
    get() = composeRoot
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun HelpScreen(modifier: Modifier = Modifier, openLink: (Uri) -> Unit) {
  val context = LocalContext.current
  val title = (context as? ComponentActivity)?.title?.toString().orEmpty()
  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
  val tipsText =
    remember(context) {
      context.getString(R.string.tips_help).replace("<strong>", "").replace("</strong>", "")
    }

  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = {
          IconButton(
            onClick = {
              when (context) {
                is ComponentActivity -> context.onBackPressedDispatcher.onBackPressed()
              }
            }
          ) {
            Icon(
              painter = painterResource(id = R.drawable.ic_baseline_arrow_back),
              contentDescription = stringResource(id = R.string.close),
            )
          }
        },
        scrollBehavior = scrollBehavior,
      )
    },
    contentWindowInsets = WindowInsets(0),
  ) { innerPadding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState())
    ) {
      HelpTipsItem(text = tipsText)

      HelpCategoryTitle(text = stringResource(id = R.string.document))

      HelpLinkItem(
        title = stringResource(id = R.string.clash_wiki),
        summary = stringResource(id = R.string.clash_wiki_url),
        onClick = { openLink(Uri.parse(context.getString(R.string.clash_wiki_url))) },
      )

      HelpLinkItem(
        title = stringResource(id = R.string.clash_meta_wiki),
        summary = stringResource(id = R.string.clash_meta_wiki_url),
        onClick = { openLink(Uri.parse(context.getString(R.string.clash_meta_wiki_url))) },
      )

      HelpCategoryTitle(text = stringResource(id = R.string.sources))

      HelpLinkItem(
        title = stringResource(id = R.string.clash_meta_core),
        summary = stringResource(id = R.string.clash_meta_core_url),
        onClick = { openLink(Uri.parse(context.getString(R.string.clash_meta_core_url))) },
      )

      HelpLinkItem(
        title = stringResource(id = R.string.clash_meta_for_android),
        summary = stringResource(id = R.string.meta_github_url),
        onClick = { openLink(Uri.parse(context.getString(R.string.meta_github_url))) },
      )

      Spacer(modifier = Modifier.height(12.dp))
    }
  }
}

@Composable
private fun HelpTipsItem(text: String) {
  Row(
    modifier = Modifier.padding(vertical = 16.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Spacer(modifier = Modifier.width(20.dp))

    Image(
      painter = painterResource(id = R.drawable.ic_outline_info),
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
private fun HelpCategoryTitle(text: String) {
  Text(
    text = text,
    style = MaterialTheme.typography.bodyMedium,
    color = MaterialTheme.colorScheme.primary,
    modifier =
      Modifier.fillMaxWidth().padding(start = 65.dp, end = 20.dp, top = 16.dp, bottom = 16.dp),
  )
}

@Composable
private fun HelpLinkItem(title: String, summary: String, onClick: () -> Unit) {
  Row(
    modifier =
      Modifier.fillMaxWidth()
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
