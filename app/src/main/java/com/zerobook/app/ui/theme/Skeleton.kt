package com.zerobook.app.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zerobook.app.ui.components.LoadingAvatar
import com.zerobook.app.ui.components.LoadingCard
import com.zerobook.app.ui.components.LoadingDefaults
import com.zerobook.app.ui.components.LoadingListItem
import com.zerobook.app.ui.components.LoadingPlaceholder
import com.zerobook.app.ui.components.LoadingRectangle
import com.zerobook.app.ui.components.LoadingRoundedPlaceholder
import com.zerobook.app.ui.components.LoadingText
import com.zerobook.app.ui.components.LoadingTile

@Deprecated("Use LoadingDefaults from ui.components", ReplaceWith("LoadingDefaults"))
object SkeletonDefaults {
    val placeholderColor: Color
        @Composable get() = LoadingDefaults.placeholderColor
    val highlightColor: Color
        @Composable get() = LoadingDefaults.highlightColor
    val shape: Shape = LoadingDefaults.shape
    val avatarShape: Shape = CircleShape
    val lineHeight: Dp = 14.dp
    val textSpacing: Dp = 10.dp
    val tileSpacing: Dp = 12.dp
    val tileCornerRadius: Dp = 14.dp
    val cardCornerRadius: Dp = 16.dp
}

@Deprecated("Use LoadingPlaceholder from ui.components", ReplaceWith("LoadingPlaceholder(modifier, shape, color, highlightColor, animate)"))
@Composable
fun SkeletonPlaceholder(
    modifier: Modifier = Modifier,
    shape: Shape = SkeletonDefaults.shape,
    color: Color = SkeletonDefaults.placeholderColor,
    highlightColor: Color = SkeletonDefaults.highlightColor,
    animate: Boolean = true
) = LoadingPlaceholder(modifier, shape, color, highlightColor, animate)

@Deprecated("Use LoadingText from ui.components", ReplaceWith("LoadingText(modifier, width, height, shape, animate)"))
@Composable
fun SkeletonText(
    modifier: Modifier = Modifier,
    width: Dp = Dp.Unspecified,
    height: Dp = SkeletonDefaults.lineHeight,
    shape: Shape = RoundedCornerShape(8.dp),
    animate: Boolean = true
) = LoadingText(modifier, width, height, shape, animate)

@Deprecated("Use LoadingAvatar from ui.components", ReplaceWith("LoadingAvatar(modifier, size, animate)"))
@Composable
fun SkeletonAvatar(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    animate: Boolean = true
) = LoadingAvatar(modifier, size, animate)

@Deprecated("Use LoadingRectangle from ui.components", ReplaceWith("LoadingRectangle(modifier, width, height, shape, animate)"))
@Composable
fun SkeletonRectangle(
    modifier: Modifier = Modifier,
    width: Dp = Dp.Unspecified,
    height: Dp = 16.dp,
    shape: Shape = RoundedCornerShape(8.dp),
    animate: Boolean = true
) = LoadingRectangle(modifier, width, height, shape, animate)

@Deprecated("Use LoadingRoundedPlaceholder from ui.components", ReplaceWith("LoadingRoundedPlaceholder(modifier, width, height, cornerRadius, animate)"))
@Composable
fun SkeletonRoundedPlaceholder(
    modifier: Modifier = Modifier,
    width: Dp = Dp.Unspecified,
    height: Dp = 16.dp,
    cornerRadius: Dp = 24.dp,
    animate: Boolean = true
) = LoadingRoundedPlaceholder(modifier, width, height, cornerRadius, animate)

@Deprecated("Use LoadingCard from ui.components", ReplaceWith("LoadingCard(modifier, content)"))
@Composable
fun SkeletonCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) = LoadingCard(modifier, content)

@Deprecated("Use LoadingListItem from ui.components", ReplaceWith("LoadingListItem(modifier, animate, lineCount)"))
@Composable
fun SkeletonListItem(
    modifier: Modifier = Modifier,
    animate: Boolean = true,
    lineCount: Int = 2
) = LoadingListItem(modifier, animate, lineCount)

@Deprecated("Use LoadingTile from ui.components", ReplaceWith("LoadingTile(modifier, animate)"))
@Composable
fun SkeletonTile(
    modifier: Modifier = Modifier,
    animate: Boolean = true
) = LoadingTile(modifier, animate)
