package com.zerobook.app.ui.selection

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zerobook.app.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionTopAppBar(
    controller: SelectionController,
    visibleItemCount: Int,
    onClose: () -> Unit,
    onSelectAll: () -> Unit,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (!controller.isSelectionActive) return

    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = "${controller.selectedCount} selected",
                color = AppColors.textPrimary,
                maxLines = 1
            )
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close selection")
            }
        },
        actions = {
            TextButton(onClick = onSelectAll) {
                Text(
                    text = if (controller.isAllSelected(visibleItemCount)) "Deselect All" else "Select All",
                    color = AppColors.primary
                )
            }
            if (onDelete != null && controller.selectedCount > 0) {
                TextButton(onClick = onDelete) {
                    Text("Delete", color = AppColors.error)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.cardBg)
    )
}

@Composable
fun SelectionIndicator(
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    selectedColor: Color = AppColors.primary,
    contentColor: Color = Color.White
) {
    val animatedBackground by animateColorAsState(
        targetValue = if (isSelected) selectedColor else Color.Transparent,
        animationSpec = tween(220),
        label = "selection_indicator_background"
    )
    val animatedBorder by animateColorAsState(
        targetValue = if (isSelected) selectedColor else AppColors.border,
        animationSpec = tween(220),
        label = "selection_indicator_border"
    )

    Box(
        modifier = modifier
            .size(24.dp)
            .border(1.8.dp, animatedBorder, CircleShape)
            .background(animatedBackground, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(Icons.Default.Check, contentDescription = null, tint = contentColor, modifier = Modifier.size(16.dp))
        }
    }
}
