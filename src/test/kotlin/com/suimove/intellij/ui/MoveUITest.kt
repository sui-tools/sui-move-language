package com.suimove.intellij.ui

import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.ui.components.JBLabel
import com.suimove.intellij.settings.MoveSettingsComponent
import javax.swing.JComponent
import javax.swing.JPanel

class MoveUITest : BasePlatformTestCase() {
    
    fun testSettingsComponentUI() {
        // Create settings component
        val settingsComponent = MoveSettingsComponent()
        
        // Verify UI components are created
        assertNotNull("Settings panel should be created", settingsComponent.panel)
        assertTrue("Settings panel should be JPanel", settingsComponent.panel is JPanel)
        
        // Find Sui CLI path field
        val suiCliPathField = findComponentByType(settingsComponent.panel, TextFieldWithBrowseButton::class.java)
        assertNotNull("Sui CLI path field should be present", suiCliPathField)
        
        // Find label
        val label = findComponentByType(settingsComponent.panel, JBLabel::class.java)
        assertNotNull("Label should be present", label)
        assertEquals("Label text should be correct", "Sui CLI path:", label?.text)
        
        // Test setting values
        settingsComponent.suiCliPath = "/test/path/to/sui"
        assertEquals("Sui CLI path should be updated", "/test/path/to/sui", settingsComponent.suiCliPath)
        
        // Test clearing value
        settingsComponent.suiCliPath = ""
        assertEquals("Sui CLI path should be empty", "", settingsComponent.suiCliPath)
    }
    
    fun testSettingsComponentBrowseButton() {
        val settingsComponent = MoveSettingsComponent()
        
        // Find the browse button component
        val browseButton = findComponentByType(settingsComponent.panel, TextFieldWithBrowseButton::class.java)
        assertNotNull("Browse button should be present", browseButton)
        
        // Verify it's properly configured
        assertTrue("Should be enabled", browseButton?.isEnabled == true)
    }
    
    // Helper function to find component by type
    private fun <T : JComponent> findComponentByType(container: JComponent, type: Class<T>): T? {
        if (type.isInstance(container)) {
            @Suppress("UNCHECKED_CAST")
            return container as T
        }
        
        for (component in container.components) {
            if (component is JComponent) {
                val found = findComponentByType(component, type)
                if (found != null) {
                    return found
                }
            }
        }
        
        return null
    }
}
