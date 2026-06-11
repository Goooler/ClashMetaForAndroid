@file:Suppress("NOTHING_TO_INLINE", "ModifierParameter", "unused")

package com.github.kr328.clash.ui.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
inline fun ColumnScope.Spacer(height: Dp, modifier: Modifier = Modifier) {
  Spacer(modifier = modifier.height(height))
}

@Composable
inline fun RowScope.Spacer(width: Dp, modifier: Modifier = Modifier) {
  Spacer(modifier = modifier.width(width))
}

@Composable
inline fun SizeSpacer(size: Dp, modifier: Modifier = Modifier) {
  Spacer(modifier = modifier.size(size))
}

@Composable
inline fun RowScope.WeightSpacer(weight: Float = 1f, modifier: Modifier = Modifier) {
  Spacer(modifier = modifier.weight(weight))
}

@Composable
inline fun ColumnScope.WeightSpacer(weight: Float = 1f, modifier: Modifier = Modifier) {
  Spacer(modifier = modifier.weight(weight))
}
