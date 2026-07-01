package com.zerobook.app.ui.animation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val PressedScale = 0.965f
private const val PressedOffsetDp = 1f
private const val SelectedNavScale = 1.04f

private data class PremiumMotionPrefs(
    val reducedMotion: Boolean = false,
    val durationScale: Float = 1f
)

@Composable
private fun rememberPremiumMotionPrefs(): PremiumMotionPrefs {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val isCompact = configuration.screenWidthDp <= 360 || configuration.screenHeightDp <= 640
    val isLowDensity = density.density < 2.1f

    return remember(configuration.screenWidthDp, configuration.screenHeightDp, density.density) {
        PremiumMotionPrefs(
            reducedMotion = isCompact || isLowDensity,
            durationScale = if (isCompact || isLowDensity) 0.88f else 1f
        )
    }
}

private fun premiumDuration(baseDuration: Int, prefs: PremiumMotionPrefs): Int =
    (baseDuration * prefs.durationScale).roundToInt().coerceAtLeast(if (prefs.reducedMotion) 140 else 180)

val PremiumSpringSpec = spring<Float>(
    dampingRatio = 0.8f,
    stiffness = 320f
)

val PremiumNavSpringSpec = spring<Float>(
    dampingRatio = 0.82f,
    stiffness = 300f
)

val PremiumFadeSpec = tween<Float>(
    durationMillis = 220,
    easing = FastOutSlowInEasing
)

val PremiumOffsetSpringSpec = spring<IntOffset>(
    dampingRatio = 0.84f,
    stiffness = 360f
)

fun premiumScreenTransition(
    navigatingBack: Boolean
): AnimatedContentTransitionScope<*>.() -> ContentTransform = {
    val enterOffset: (Int) -> Int = { fullWidth -> if (navigatingBack) -(fullWidth / 10) else fullWidth / 10 }
    val exitOffset: (Int) -> Int = { fullWidth -> if (navigatingBack) fullWidth / 10 else -(fullWidth / 10) }

    (
        slideInHorizontally(
            animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
            initialOffsetX = enterOffset
        ) +
            fadeIn(animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing), initialAlpha = 0f)
        ) togetherWith (
        slideOutHorizontally(
            animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
            targetOffsetX = exitOffset
        ) +
            fadeOut(animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing), targetAlpha = 1f)
        )
}

fun premiumEnterTransition(navigatingBack: Boolean): AnimatedContentTransitionScope<*>.() -> EnterTransition = {
    slideInHorizontally(
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        initialOffsetX = { fullWidth ->
            if (navigatingBack) -(fullWidth / 10) else fullWidth / 10
        }
    ) +
        fadeIn(animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing), initialAlpha = 0f)
}

fun premiumExitTransition(navigatingBack: Boolean): AnimatedContentTransitionScope<*>.() -> ExitTransition = {
    slideOutHorizontally(
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        targetOffsetX = { fullWidth ->
            if (navigatingBack) fullWidth / 10 else -(fullWidth / 10)
        }
    ) +
        fadeOut(animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing), targetAlpha = 1f)
}

@Composable
fun Modifier.pressScale(
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
): Modifier {
    val prefs = rememberPremiumMotionPrefs()
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (enabled && isPressed) PressedScale else 1f,
        animationSpec = spring(
            dampingRatio = if (prefs.reducedMotion) 0.82f else 0.8f,
            stiffness = if (prefs.reducedMotion) 280f else 320f
        ),
        label = "premium_press_scale"
    )
    val offset by animateFloatAsState(
        targetValue = if (enabled && isPressed) 1f else 0f,
        animationSpec = spring(
            dampingRatio = if (prefs.reducedMotion) 0.82f else 0.8f,
            stiffness = if (prefs.reducedMotion) 280f else 320f
        ),
        label = "premium_press_offset"
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
fun Modifier.premiumClickable(
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modifier.premiumCombinedClickable(
    enabled: Boolean = true,
    role: Role? = null,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return pressScale(enabled = enabled, interactionSource = interactionSource)
        .combinedClickable(
            enabled = enabled,
            role = role,
            interactionSource = interactionSource,
            indication = ripple(),
            onClick = onClick,
            onLongClick = onLongClick
        )
}

@Composable
fun PremiumBottomNavContent(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) SelectedNavScale else 1f,
        animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing),
        label = "bottom_nav_scale"
    )
    val labelAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.72f,
        animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
        label = "bottom_nav_alpha"
    )
    val iconAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.78f,
        animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
        label = "bottom_nav_icon_alpha"
    )

    Row(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier.graphicsLayer {
                alpha = iconAlpha
            }
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp)
            )
        }
        if (selected) {
            Text(
                text = label,
                modifier = Modifier.alpha(labelAlpha),
                fontSize = 11.sp
            )
        }
    }
}

fun premiumDialogEnter(): EnterTransition =
    slideInVertically(
        animationSpec = PremiumOffsetSpringSpec,
        initialOffsetY = { it / 6 }
    ) +
        fadeIn(animationSpec = PremiumFadeSpec, initialAlpha = 0.3f) +
        scaleIn(animationSpec = PremiumSpringSpec, initialScale = 0.96f)

fun premiumDialogExit(): ExitTransition =
    slideOutVertically(
        animationSpec = PremiumOffsetSpringSpec,
        targetOffsetY = { it / 8 }
    ) +
        fadeOut(animationSpec = PremiumFadeSpec) +
        scaleOut(animationSpec = PremiumSpringSpec, targetScale = 0.985f)

@Composable
fun Modifier.premiumFabEntrance(visible: Boolean = true): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.82f,
        animationSpec = PremiumSpringSpec,
        label = "fab_scale"
    )
    val rotation by animateFloatAsState(
        targetValue = if (visible) 0f else -12f,
        animationSpec = PremiumNavSpringSpec,
        label = "fab_rotation"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = PremiumFadeSpec,
        label = "fab_alpha"
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.alpha = alpha
        rotationZ = rotation
    }
}
