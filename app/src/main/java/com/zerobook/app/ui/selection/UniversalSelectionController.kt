package com.zerobook.app.ui.selection

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap

@Stable
data class UniversalSelectionSnapshot(
    val isActive: Boolean = false,
    val selectedIds: List<String> = emptyList()
)

@Stable
class UniversalSelectionController(initialState: UniversalSelectionSnapshot = UniversalSelectionSnapshot()) {
    private val selectedIds = mutableStateMapOf<String, Boolean>()

    var isSelectionActive by mutableStateOf(initialState.isActive)
        private set

    init {
        initialState.selectedIds.forEach { id -> selectedIds[id] = true }
    }

    val selectedCount: Int get() = selectedIds.size

    fun isSelected(id: String): Boolean = selectedIds.containsKey(id)

    fun enterSelection(id: String) {
        if (!isSelectionActive) {
            isSelectionActive = true
        }
        selectedIds[id] = true
    }

    fun exitSelection() {
        selectedIds.clear()
        isSelectionActive = false
    }

    fun toggleSelection(id: String) {
        if (!isSelectionActive) {
            isSelectionActive = true
        }
        if (selectedIds.containsKey(id)) {
            selectedIds.remove(id)
            if (selectedIds.isEmpty()) {
                isSelectionActive = false
            }
        } else {
            selectedIds[id] = true
        }
    }

    fun selectAll(visibleIds: List<String>) {
        if (visibleIds.isEmpty()) {
            return
        }
        visibleIds.forEach { id -> selectedIds[id] = true }
        isSelectionActive = true
    }

    fun toggleSelectAll(visibleIds: List<String>) {
        if (visibleIds.isEmpty()) {
            return
        }
        val allVisibleSelected = visibleIds.all { selectedIds.containsKey(it) }
        if (allVisibleSelected) {
            visibleIds.forEach { id -> selectedIds.remove(id) }
            if (selectedIds.isEmpty()) {
                isSelectionActive = false
            }
        } else {
            visibleIds.forEach { id -> selectedIds[id] = true }
            isSelectionActive = true
        }
    }

    fun selectedIdsSnapshot(): Set<String> = selectedIds.keys.toSet()

    fun selectedIdsState(): SnapshotStateMap<String, Boolean> = selectedIds

    fun isAllSelected(visibleCount: Int): Boolean = visibleCount > 0 && selectedIds.size == visibleCount

    fun snapshot(): UniversalSelectionSnapshot = UniversalSelectionSnapshot(
        isActive = isSelectionActive,
        selectedIds = selectedIds.keys.toList()
    )
}
