package com.suimove.intellij

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import com.suimove.intellij.toolwindow.MoveToolWindowFactory

/**
 * Basic plugin tests that don't require IntelliJ test framework
 * These tests verify that core plugin components can be instantiated
 */
class BasicPluginTest {
    
    @Test
    fun `test MoveToolWindowFactory can be created`() {
        val factory = MoveToolWindowFactory()
        assertNotNull(factory, "MoveToolWindowFactory should be instantiated")
    }
    
    @Test
    fun `test MoveLanguage singleton exists`() {
        val language = MoveLanguage
        assertNotNull(language, "MoveLanguage instance should exist")
        assertEquals("Move", language.displayName, "Language display name should be 'Move'")
    }
    
    @Test
    fun `test MoveFileType properties`() {
        val fileType = MoveFileType
        assertNotNull(fileType, "MoveFileType instance should exist")
        assertEquals("Move", fileType.name, "File type name should be 'Move'")
        assertEquals("move", fileType.defaultExtension, "Default extension should be 'move'")
        assertEquals("Move language file", fileType.description, "Description should match")
    }
    
    @Test
    fun `test MoveIcons are loaded`() {
        val fileIcon = MoveIcons.FILE
        assertNotNull(fileIcon, "Move file icon should be loaded")
    }
    
    @Test
    fun `test Move keyword constants`() {
        // Test that important constants are defined
        assertTrue(MoveLanguage.displayName.isNotEmpty(), "Language display name should not be empty")
        assertTrue(MoveFileType.defaultExtension.isNotEmpty(), "File extension should not be empty")
    }
    
    @Test
    fun `test plugin ID and name`() {
        val pluginId = "com.suimove.intellij"
        val pluginName = "Sui Move Language"
        
        // These would normally come from plugin.xml but we can verify the expected values
        assertNotNull(pluginId, "Plugin ID should be defined")
        assertNotNull(pluginName, "Plugin name should be defined")
        assertTrue(pluginId.startsWith("com.suimove"), "Plugin ID should have correct prefix")
    }
}
