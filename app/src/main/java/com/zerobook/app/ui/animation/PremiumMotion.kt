package com.zerobook.app.ui.animation

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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.foundation.layout.Column
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
import com.zerobook.app.ui.theme.AppColors

private const val PressedScale = 0.97f
private const val PressedOffsetDp = 0.75f
private const val SelectedNavScale = 1.02f

private data class PremiumMotionPrefs(
    val reducedMotion: Boolean = false,
    val durationScale: Float = 1f,
    val animationScale: Float = 1f
)

@Composable
private fun rememberPremiumMotionPrefs(): PremiumMotionPrefs {
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
        PremiumMotionPrefs(
            reducedMotion = reducedMotion,
            durationScale = durationScale,
            animationScale = animationScale
        )
    }
}

private fun premiumDuration(baseDuration: Int, prefs: PremiumMotionPrefs): Int =
    if (prefs.reducedMotion) {
        (baseDuration * 0.6f).roundToInt().coerceAtLeast(90)
    } else {
        (baseDuration * prefs.durationScale).roundToInt().coerceAtLeast(80)
    }

private fun premiumSpringSpec(reducedMotion: Boolean) = spring<Float>(
    dampingRatio = if (reducedMotion) 0.92f else 0.8f,
    stiffness = if (reducedMotion) 420f else 320f
)

private fun premiumNavSpringSpec(reducedMotion: Boolean) = spring<Float>(
    dampingRatio = if (reducedMotion) 0.94f else 0.82f,
    stiffness = if (reducedMotion) 360f else 300f
)

private fun premiumOffsetSpringSpec(reducedMotion: Boolean) = spring<IntOffset>(
    dampingRatio = if (reducedMotion) 0.92f else 0.84f,
    stiffness = if (reducedMotion) 400f else 360f
)

private fun premiumFadeSpec(reducedMotion: Boolean) = tween<Float>(
    durationMillis = if (reducedMotion) 80 else 120,
    easing = FastOutSlowInEasing
)

val PremiumSpringSpec = spring<Float>(
    dampingRatio = 0.8f,
    stiffness = 320f
)

val PremiumNavSpringSpec = spring<Float>(
    dampingRatio = 0.82f,
    stiffness = 300f
)

val PremiumFadeSpec = tween<Float>(
    durationMillis = 120,
    easing = FastOutSlowInEasing
)

val PremiumOffsetSpringSpec = spring<IntOffset>(
    dampingRatio = 0.84f,
    stiffness = 360f
)

fun premiumScreenTransition(
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
fun premiumEnterTransition(navigatingBack: Boolean): AnimatedContentTransitionScope<*>.() -> EnterTransition {
    val prefs = rememberPremiumMotionPrefs()
    return {
        slideInHorizontally(
            animationSpec = tween(durationMillis = premiumDuration(100, prefs), easing = FastOutSlowInEasing),
            initialOffsetX = { fullWidth ->
                if (navigatingBack) -(fullWidth / 12) else fullWidth / 12
            }
        ) +
            fadeIn(animationSpec = premiumFadeSpec(prefs.reducedMotion), initialAlpha = 0.02f)
    }
}

@Composable
fun premiumExitTransition(navigatingBack: Boolean): AnimatedContentTransitionScope<*>.() -> ExitTransition {
    val prefs = rememberPremiumMotionPrefs()
    return {
        slideOutHorizontally(
            animationSpec = tween(durationMillis = premiumDuration(100, prefs), easing = FastOutSlowInEasing),
            targetOffsetX = { fullWidth ->
                if (navigatingBack) fullWidth / 12 else -(fullWidth / 12)
            }
        ) +
            fadeOut(animationSpec = premiumFadeSpec(prefs.reducedMotion), targetAlpha = 1f)
    }
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
        animationSpec = premiumSpringSpec(prefs.reducedMotion),
        label = "premium_press_scale"
    )
    val offset by animateFloatAsState(
        targetValue = if (enabled && isPressed) 1f else 0f,
        animationSpec = premiumSpringSpec(prefs.reducedMotion),
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
fun premiumDialogEnter(): EnterTransition {
    val prefs = rememberPremiumMotionPrefs()
    return slideInVertically(
        animationSpec = premiumOffsetSpringSpec(prefs.reducedMotion),
        initialOffsetY = { it / 6 }
    ) +
        fadeIn(animationSpec = premiumFadeSpec(prefs.reducedMotion), initialAlpha = 0.3f) +
        scaleIn(animationSpec = premiumSpringSpec(prefs.reducedMotion), initialScale = if (prefs.reducedMotion) 0.98f else 0.96f)
}

@Composable
fun premiumDialogExit(): ExitTransition {
    val prefs = rememberPremiumMotionPrefs()
    return slideOutVertically(
        animationSpec = premiumOffsetSpringSpec(prefs.reducedMotion),
        targetOffsetY = { it / 8 }
    ) +
        fadeOut(animationSpec = premiumFadeSpec(prefs.reducedMotion)) +
        scaleOut(animationSpec = premiumSpringSpec(prefs.reducedMotion), targetScale = if (prefs.reducedMotion) 0.995f else 0.985f)
}

@Composable
fun Modifier.premiumFabEntrance(visible: Boolean = true): Modifier {
    val prefs = rememberPremiumMotionPrefs()
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.82f,
        animationSpec = premiumSpringSpec(prefs.reducedMotion),
        label = "fab_scale"
    )
    val rotation by animateFloatAsState(
        targetValue = if (visible) 0f else if (prefs.reducedMotion) -8f else -12f,
        animationSpec = premiumNavSpringSpec(prefs.reducedMotion),
        label = "fab_rotation"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = premiumFadeSpec(prefs.reducedMotion),
        label = "fab_alpha"
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.alpha = alpha
        rotationZ = rotation
    }
}
