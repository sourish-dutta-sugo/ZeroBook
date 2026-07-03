package com.zerobook.app.ui.selection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UniversalSelectionControllerTest {
    @Test
    fun togglesSelectionAndSelectAll() {
        val controller = UniversalSelectionController()

        assertFalse(controller.isSelectionActive)

        controller.enterSelection("a")
        assertTrue(controller.isSelectionActive)
        assertTrue(controller.isSelected("a"))

        controller.toggleSelection("b")
        assertTrue(controller.isSelected("b"))
        assertEquals(2, controller.selectedCount)

        controller.toggleSelection("a")
        assertFalse(controller.isSelected("a"))
        assertTrue(controller.isSelectionActive)
        assertEquals(1, controller.selectedCount)

        controller.selectAll(listOf("a", "b", "c"))
        assertTrue(controller.isSelectionActive)
        assertEquals(3, controller.selectedCount)

        controller.toggleSelectAll(listOf("a", "b", "c"))
        assertFalse(controller.isSelectionActive)
        assertEquals(0, controller.selectedCount)
    }
}
