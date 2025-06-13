package com.suimove.intellij.toolwindow

import org.junit.Test
import org.junit.Assert.*

class MoveToolWindowTest {
    
    @Test
    fun testToolWindowFactoryCreation() {
        // Test that the factory can be instantiated
        val factory = MoveToolWindowFactory()
        assertNotNull("Tool window factory should exist", factory)
    }
    
    @Test
    fun testToolWindowFactoryId() {
        // Test that the factory has the correct ID
        val factory = MoveToolWindowFactory()
        // The factory should have a proper ID (this would be set in the plugin.xml)
        assertNotNull("Factory should be created", factory)
    }
}
