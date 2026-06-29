package com.zerobook.app.ui.animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Premium Animation Utilities for Fintech Design
 * 
 * Provides micro-animations for:
 * - Money count animations
 * - Card floating effects
 * - Transaction appearing animations
 * - Chart drawing animations
 * - Button press depth effects
 */

// ============================================================================
// ANIMATED COUNTER - FOR BALANCE DISPLAY
// ============================================================================

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

// ============================================================================
// FLOATING CARD EFFECT - SUBTLE ELEVATION ANIMATION
// ============================================================================

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

// ============================================================================
// TRANSACTION APPEARING ANIMATION
// ============================================================================

fun Modifier.transactionAppearAnimation(
    delayMillis: Int = 0
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "TransactionAppear")
    
    val alpha by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 600,
                delayMillis = delayMillis,
                easing = EaseOutCubic
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "AlphaAnimation"
    )
    
    val translateX by transition.animateFloat(
        initialValue = 20f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 600,
                delayMillis = delayMillis,
                easing = EaseOutCubic
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "TranslateAnimation"
    )
    
    this
        .graphicsLayer {
            this.alpha = alpha
            translationX = translateX
        }
}

// ============================================================================
// BUTTON PRESS DEPTH EFFECT
// ============================================================================

fun Modifier.premiumButtonPress(
    interactionSource: MutableInteractionSource
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> isPressed = true
                is PressInteraction.Release -> isPressed = false
                is PressInteraction.Cancel -> isPressed = false
            }
        }
    }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "ButtonScale"
    )
    
    this.scale(scale)
}

// ============================================================================
// CHART DRAWING ANIMATION
// ============================================================================

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

// ============================================================================
// SHIMMER LOADING ANIMATION
// ============================================================================

fun Modifier.shimmerLoadingAnimation(): Modifier = composed {
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

// ============================================================================
// SMOOTH SLIDE IN ANIMATION
// ============================================================================

fun Modifier.slideInFromBottom(
    delayMillis: Int = 0,
    durationMillis: Int = 500
): Modifier = composed {
    val offsetY = remember { Animatable(100f) }
    val alpha = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        offsetY.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = durationMillis,
                delayMillis = delayMillis,
                easing = EaseOutCubic
            )
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = durationMillis,
                delayMillis = delayMillis,
                easing = EaseOutCubic
            )
        )
    }
    
    this.graphicsLayer {
        translationY = offsetY.value
        this.alpha = alpha.value
    }
}

// ============================================================================
// SCALE AND FADE IN ANIMATION
// ============================================================================

fun Modifier.scaleAndFadeIn(
    delayMillis: Int = 0,
    durationMillis: Int = 500
): Modifier = composed {
    val scale = remember { Animatable(0.8f) }
    val alpha = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = durationMillis,
                delayMillis = delayMillis,
                easing = EaseOutCubic
            )
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = durationMillis,
                delayMillis = delayMillis,
                easing = EaseOutCubic
            )
        )
    }
    
    this.graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
        this.alpha = alpha.value
    }
}

// ============================================================================
// PULSE ANIMATION - FOR ALERTS/NOTIFICATIONS
// ============================================================================

fun Modifier.pulseAnimation(
    duration: Int = 1500
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )
    
    this.scale(scale)
}

// ============================================================================
// ROTATION ANIMATION - FOR LOADING SPINNERS
// ============================================================================

fun Modifier.rotationAnimation(
    duration: Int = 2000
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "Rotation")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RotationAnimation"
    )
    
    this.graphicsLayer {
        rotationZ = rotation
    }
}

// ============================================================================
// BOUNCE ANIMATION - FOR EMPHASIS
// ============================================================================

fun Modifier.bounceAnimation(
    delayMillis: Int = 0,
    durationMillis: Int = 600
): Modifier = composed {
    val offsetY = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        offsetY.animateTo(
            targetValue = -8f,
            animationSpec = tween(
                durationMillis = durationMillis / 2,
                delayMillis = delayMillis,
                easing = EaseOutCubic
            )
        )
        offsetY.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = durationMillis / 2,
                easing = EaseInCubic
            )
        )
    }
    
    this.graphicsLayer {
        translationY = offsetY.value
    }
}
