package com.zerobook.app.ui.transitions

import android.content.Context
import android.provider.Settings
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zerobook.app.ui.theme.AppColors
import kotlin.math.roundToInt
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith

private const val PressedScale = 0.97f
private const val PressedOffsetDp = 0.75f
private const val SelectedNavScale = 1.02f

private data class MotionPrefs(
    val reducedMotion: Boolean = false,
    val durationScale: Float = 1f,
    val animationScale: Float = 1f
)

@Composable
private fun rememberMotionPrefs(): MotionPrefs {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val context = LocalContext.current
    val animationScale = remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ).coerceAtLeast(0f)
    }
    val isCompact = configuration.screenWidthDp <= 360 || configuration.screenHeightDp <= 640
    val isLowDensity = density.density < 2.1f
    val reducedMotion = animationScale == 0f || isCompact || isLowDensity
    val durationScale = when {
        animationScale == 0f -> 0.24f
        animationScale < 0.5f -> 0.5f
        animationScale > 1.5f -> 1.5f
        else -> animationScale
    }

    return remember(configuration.screenWidthDp, configuration.screenHeightDp, density.density, animationScale) {
        MotionPrefs(
            reducedMotion = reducedMotion,
            durationScale = durationScale,
            animationScale = animationScale
        )
    }
}

private fun adjustedDuration(baseDuration: Int, prefs: MotionPrefs): Int =
    if (prefs.reducedMotion) {
        (baseDuration * 0.6f).roundToInt().coerceAtLeast(90)
    } else {
        (baseDuration * prefs.durationScale).roundToInt().coerceAtLeast(80)
    }

private fun springSpec(reducedMotion: Boolean) = spring<Float>(
    dampingRatio = if (reducedMotion) 0.92f else 0.8f,
    stiffness = if (reducedMotion) 420f else 320f
)

private fun navSpringSpec(reducedMotion: Boolean) = spring<Float>(
    dampingRatio = if (reducedMotion) 0.94f else 0.82f,
    stiffness = if (reducedMotion) 360f else 300f
)

private fun offsetSpringSpec(reducedMotion: Boolean) = spring<IntOffset>(
    dampingRatio = if (reducedMotion) 0.92f else 0.84f,
    stiffness = if (reducedMotion) 400f else 360f
)

private fun fadeSpec(reducedMotion: Boolean) = tween<Float>(
    durationMillis = if (reducedMotion) 80 else 120,
    easing = FastOutSlowInEasing
)

val NavTransitionSpec = spring<Float>(
    dampingRatio = 0.8f,
    stiffness = 320f
)

val NavSpringSpec = spring<Float>(
    dampingRatio = 0.82f,
    stiffness = 300f
)

val NavFadeSpec = tween<Float>(
    durationMillis = 120,
    easing = FastOutSlowInEasing
)

val NavOffsetSpringSpec = spring<IntOffset>(
    dampingRatio = 0.84f,
    stiffness = 360f
)

fun screenTransition(
    navigatingBack: Boolean
): AnimatedContentTransitionScope<*>.() -> ContentTransform = {
    val enterOffset: (Int) -> Int = { fullWidth -> if (navigatingBack) -(fullWidth / 12) else fullWidth / 12 }
    val exitOffset: (Int) -> Int = { fullWidth -> if (navigatingBack) fullWidth / 12 else -(fullWidth / 12) }

    (
        slideInHorizontally(
            animationSpec = tween(durationMillis = 90, easing = FastOutSlowInEasing),
            initialOffsetX = enterOffset
        ) +
            fadeIn(animationSpec = tween(durationMillis = 90, easing = FastOutSlowInEasing), initialAlpha = 0.02f)
        ) togetherWith (
        slideOutHorizontally(
            animationSpec = tween(durationMillis = 90, easing = FastOutSlowInEasing),
            targetOffsetX = exitOffset
        ) +
            fadeOut(animationSpec = tween(durationMillis = 90, easing = FastOutSlowInEasing), targetAlpha = 1f)
        )
}

@Composable
fun enterTransition(navigatingBack: Boolean): AnimatedContentTransitionScope<*>.() -> EnterTransition {
    val prefs = rememberMotionPrefs()
    return {
        slideInHorizontally(
            animationSpec = tween(durationMillis = adjustedDuration(100, prefs), easing = FastOutSlowInEasing),
            initialOffsetX = { fullWidth ->
                if (navigatingBack) -(fullWidth / 12) else fullWidth / 12
            }
        ) +
            fadeIn(animationSpec = fadeSpec(prefs.reducedMotion), initialAlpha = 0.02f)
    }
}

@Composable
fun exitTransition(navigatingBack: Boolean): AnimatedContentTransitionScope<*>.() -> ExitTransition {
    val prefs = rememberMotionPrefs()
    return {
        slideOutHorizontally(
            animationSpec = tween(durationMillis = adjustedDuration(100, prefs), easing = FastOutSlowInEasing),
            targetOffsetX = { fullWidth ->
                if (navigatingBack) fullWidth / 12 else -(fullWidth / 12)
            }
        ) +
            fadeOut(animationSpec = fadeSpec(prefs.reducedMotion), targetAlpha = 1f)
    }
}

@Composable
fun Modifier.pressScale(
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
): Modifier {
    val prefs = rememberMotionPrefs()
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (enabled && isPressed) PressedScale else 1f,
        animationSpec = springSpec(prefs.reducedMotion),
        label = "press_scale"
    )
    val offset by animateFloatAsState(
        targetValue = if (enabled && isPressed) 1f else 0f,
        animationSpec = springSpec(prefs.reducedMotion),
        label = "press_offset"
    )
    val density = LocalDensity.current
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
        translationY = if (enabled && isPressed) with(density) { PressedOffsetDp.dp.toPx() * offset } else 0f
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modifier.clickableScale(
    enabled: Boolean = true,
    role: Role? = null,
    onClick: () -> Unit
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return pressScale(enabled = enabled, interactionSource = interactionSource)
        .combinedClickable(
            enabled = enabled,
            role = role,
            interactionSource = interactionSource,
            indication = ripple(),
            onClick = onClick
        )
}

@Composable
fun dialogEnter(): EnterTransition {
    val prefs = rememberMotionPrefs()
    return slideInVertically(
        animationSpec = offsetSpringSpec(prefs.reducedMotion),
        initialOffsetY = { it / 6 }
    ) +
        fadeIn(animationSpec = fadeSpec(prefs.reducedMotion), initialAlpha = 0.3f) +
        scaleIn(animationSpec = springSpec(prefs.reducedMotion), initialScale = if (prefs.reducedMotion) 0.98f else 0.96f)
}

@Composable
fun dialogExit(): ExitTransition {
    val prefs = rememberMotionPrefs()
    return slideOutVertically(
        animationSpec = offsetSpringSpec(prefs.reducedMotion),
        targetOffsetY = { it / 8 }
    ) +
        fadeOut(animationSpec = fadeSpec(prefs.reducedMotion)) +
        scaleOut(animationSpec = springSpec(prefs.reducedMotion), targetScale = if (prefs.reducedMotion) 0.995f else 0.985f)
}

@Composable
fun NavBarContent(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) SelectedNavScale else 1f,
        animationSpec = tween(durationMillis = 70, easing = FastOutSlowInEasing),
        label = "bottom_nav_scale"
    )
    val contentColor = if (selected) AppColors.primary else AppColors.textTertiary

    Column(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            color = contentColor,
            fontSize = 11.sp,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun Modifier.fabScaleTransition(visible: Boolean = true): Modifier {
    val prefs = rememberMotionPrefs()
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.82f,
        animationSpec = springSpec(prefs.reducedMotion),
        label = "fab_scale"
    )
    val rotation by animateFloatAsState(
        targetValue = if (visible) 0f else if (prefs.reducedMotion) -8f else -12f,
        animationSpec = navSpringSpec(prefs.reducedMotion),
        label = "fab_rotation"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = fadeSpec(prefs.reducedMotion),
        label = "fab_alpha"
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.alpha = alpha
        rotationZ = rotation
    }
}

@Composable
fun AnimatedCounter(
    targetValue: Double,
    duration: Int = 1000,
    onValueChange: (Double) -> Unit = {}
): Double {
    var displayValue by remember { mutableStateOf(0.0) }

    val animatedValue = animateFloatAsState(
        targetValue = targetValue.toFloat(),
        animationSpec = tween(
            durationMillis = duration,
            easing = EaseOutCubic
        ),
        label = "CounterAnimation"
    )

    LaunchedEffect(animatedValue.value) {
        displayValue = animatedValue.value.toDouble()
        onValueChange(displayValue)
    }

    return displayValue
}

fun Modifier.floatingCardEffect(): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "FloatingCard")

    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FloatingOffset"
    )

    val shadowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ShadowAlpha"
    )

    this
        .graphicsLayer {
            translationY = offsetY
            shadowElevation = shadowAlpha * 12f
        }
}

fun Modifier.shimmerAnimation(): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "Shimmer")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ShimmerAlpha"
    )

    this.graphicsLayer {
        this.alpha = alpha
    }
}

@Composable
fun rememberChartDrawingProgress(
    duration: Int = 1500,
    delayMillis: Int = 0
): Float {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = duration,
                delayMillis = delayMillis,
                easing = EaseOutCubic
            )
        )
    }

    return progress.value
}
