package com.zerobook.app.ui.animation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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

private const val PressedScale = 0.96f
private const val SelectedNavScale = 1.06f

val PremiumSpringSpec = spring<Float>(
    dampingRatio = 0.76f,
    stiffness = 430f
)

val PremiumNavSpringSpec = spring<Float>(
    dampingRatio = 0.78f,
    stiffness = 380f
)

val PremiumFadeSpec = tween<Float>(
    durationMillis = 240,
    easing = FastOutSlowInEasing
)

val PremiumOffsetSpringSpec = spring<IntOffset>(
    dampingRatio = 0.8f,
    stiffness = 420f
)

fun premiumScreenTransition(
    navigatingBack: Boolean
): AnimatedContentTransitionScope<*>.() -> ContentTransform = {
    val enterOffset: (Int) -> Int = { fullWidth ->
        if (navigatingBack) -(fullWidth / 4) else fullWidth
    }
    val exitOffset: (Int) -> Int = { fullWidth ->
        if (navigatingBack) fullWidth else -(fullWidth / 4)
    }

    (
        slideInHorizontally(
            animationSpec = PremiumOffsetSpringSpec,
            initialOffsetX = enterOffset
        ) +
            fadeIn(animationSpec = PremiumFadeSpec, initialAlpha = 0.42f) +
            scaleIn(
                animationSpec = PremiumSpringSpec,
                initialScale = if (navigatingBack) 1.02f else 0.96f
            )
        ) togetherWith (
        slideOutHorizontally(
            animationSpec = PremiumOffsetSpringSpec,
            targetOffsetX = exitOffset
        ) +
            fadeOut(animationSpec = PremiumFadeSpec, targetAlpha = 0.74f) +
            scaleOut(
                animationSpec = PremiumSpringSpec,
                targetScale = if (navigatingBack) 0.96f else 0.985f
            )
        )
}

fun premiumEnterTransition(navigatingBack: Boolean): AnimatedContentTransitionScope<*>.() -> EnterTransition = {
    slideInHorizontally(
        animationSpec = PremiumOffsetSpringSpec,
        initialOffsetX = { fullWidth ->
            if (navigatingBack) -(fullWidth / 4) else fullWidth
        }
    ) +
        fadeIn(animationSpec = PremiumFadeSpec, initialAlpha = 0.42f) +
        scaleIn(
            animationSpec = PremiumSpringSpec,
            initialScale = if (navigatingBack) 1.02f else 0.96f
        )
}

fun premiumExitTransition(navigatingBack: Boolean): AnimatedContentTransitionScope<*>.() -> ExitTransition = {
    slideOutHorizontally(
        animationSpec = PremiumOffsetSpringSpec,
        targetOffsetX = { fullWidth ->
            if (navigatingBack) fullWidth else -(fullWidth / 4)
        }
    ) +
        fadeOut(animationSpec = PremiumFadeSpec, targetAlpha = 0.74f) +
        scaleOut(
            animationSpec = PremiumSpringSpec,
            targetScale = if (navigatingBack) 0.96f else 0.985f
        )
}

@Composable
fun Modifier.pressScale(
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (enabled && isPressed) PressedScale else 1f,
        animationSpec = PremiumSpringSpec,
        label = "premium_press_scale"
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
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
        animationSpec = PremiumNavSpringSpec,
        label = "bottom_nav_scale"
    )
    val labelAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.72f,
        animationSpec = PremiumFadeSpec,
        label = "bottom_nav_alpha"
    )
    val iconAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.78f,
        animationSpec = PremiumFadeSpec,
        label = "bottom_nav_icon_alpha"
    )
    val contentOffset by animateIntOffsetAsState(
        targetValue = if (selected) IntOffset(0, -2) else IntOffset.Zero,
        animationSpec = PremiumOffsetSpringSpec,
        label = "bottom_nav_offset"
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
                translationX = contentOffset.x.toFloat()
                translationY = contentOffset.y.toFloat()
                alpha = iconAlpha
            }
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp)
            )
        }
        AnimatedVisibility(visible = selected) {
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
        fadeIn(animationSpec = PremiumFadeSpec, initialAlpha = 0.35f) +
        scaleIn(animationSpec = PremiumSpringSpec, initialScale = 0.92f)

fun premiumDialogExit(): ExitTransition =
    slideOutVertically(
        animationSpec = PremiumOffsetSpringSpec,
        targetOffsetY = { it / 8 }
    ) +
        fadeOut(animationSpec = PremiumFadeSpec) +
        scaleOut(animationSpec = PremiumSpringSpec, targetScale = 0.96f)

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
