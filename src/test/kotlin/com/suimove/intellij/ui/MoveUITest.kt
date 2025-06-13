package com.suimove.intellij.ui

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.suimove.intellij.MoveFileType
import com.suimove.intellij.settings.MoveSettingsComponent
import com.suimove.intellij.toolwindow.MoveToolWindowPanel
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JPanel
import org.mockito.Mockito.*

class MoveUITest : BasePlatformTestCase() {
    
    fun testSettingsComponentUI() {
        // Create settings component
        val settingsComponent = MoveSettingsComponent()
        
        // Verify UI components are created
        assertNotNull("Settings panel should be created", settingsComponent.panel)
        
        // Find compiler path field
        val compilerPathField = findComponentByType(settingsComponent.panel, JBTextField::class.java)
        assertNotNull("Compiler path field should be present", compilerPathField)
        
        // Find network dropdown
        val networkDropdown = findComponentByType(settingsComponent.panel, JComboBox::class.java)
        assertNotNull("Network dropdown should be present", networkDropdown)
        
        // Find auto-format checkbox
        val autoFormatCheckbox = findComponentByType(settingsComponent.panel, JBCheckBox::class.java)
        assertNotNull("Auto-format checkbox should be present", autoFormatCheckbox)
        
        // Test setting values
        settingsComponent.compilerPathText = "/test/path"
        assertEquals("Compiler path should be updated", "/test/path", settingsComponent.compilerPathText)
        
        // Test network selection
        if (networkDropdown != null) {
            networkDropdown.selectedItem = "mainnet"
            assertEquals("Network should be updated", "mainnet", networkDropdown.selectedItem)
        }
        
        // Test checkbox
        if (autoFormatCheckbox != null) {
            autoFormatCheckbox.isSelected = true
            assertTrue("Auto-format should be selected", autoFormatCheckbox.isSelected)
        }
    }
    
    fun testToolWindowPanelUI() {
        // Create a Move file to ensure the project is recognized as a Move project
        myFixture.configureByText(MoveFileType, """
            module 0x1::test {
                fun main() {}
            }
        """.trimIndent())
        
        // Create tool window panel
        val panel = MoveToolWindowPanel(project)
        
        // Verify UI components are created
        assertNotNull("Tool window panel should be created", panel)
        
        // Find action buttons
        val buttons = findComponentsByType(panel, JButton::class.java)
        assertTrue("Panel should have buttons", buttons.isNotEmpty())
        
        // Test button actions (using mocks since we can't click in a test)
        val mockPanel = spy(panel)
        
        // Find build button
        val buildButton = buttons.find { it.text == "Build" }
        if (buildButton != null) {
            buildButton.doClick()
            verify(mockPanel, times(1)).refresh()
        }
    }
    
    fun testDialogInteraction() {
        // Create a Move file
        myFixture.configureByText(MoveFileType, """
            module 0x1::test {
                fun main() {}
            }
        """.trimIndent())
        
        // Test file creation dialog
        val action = com.suimove.intellij.actions.CreateMoveFileAction()
        assertNotNull("Create file action should exist", action)
        
        // We can't directly test dialog interaction in a headless test environment
        // But we can verify the action is properly configured
        assertEquals("Action should have correct text", "Move File", action.templateText)
        assertNotNull("Action should have an icon", action.templatePresentation.icon)
    }
    
    fun testEditorInteraction() {
        // Create a Move file
        myFixture.configureByText(MoveFileType, """
            module 0x1::test {
                fun main() {
                    <caret>
                }
            }
        """.trimIndent())
        
        // Type some text at the caret position
        myFixture.type("let x = 42;")
        
        // Verify the text was inserted
        myFixture.checkResult("""
            module 0x1::test {
                fun main() {
                    let x = 42;
                }
            }
        """.trimIndent())
        
        // Test code completion
        myFixture.configureByText(MoveFileType, """
            module 0x1::test {
                fun main() {
                    let x = 42;
                    <caret>
                }
            }
        """.trimIndent())
        
        myFixture.type("x.")
        myFixture.completeBasic()
        
        // Test intention actions
        myFixture.configureByText(MoveFileType, """
            module 0x1::test {
                fun main() {
                    let <caret>x = 42;
                }
            }
        """.trimIndent())
        
        val intentions = myFixture.availableIntentions
        assertTrue("Should have intentions available", intentions.isNotEmpty())
    }
    
    fun testContextMenuInteraction() {
        // Create a Move file
        myFixture.configureByText(MoveFileType, """
            module 0x1::test {
                fun <caret>main() {}
            }
        """.trimIndent())
        
        // Test context menu actions
        val popupActions = myFixture.getAvailableIntentionActions()
        assertTrue("Should have popup actions", popupActions.isNotEmpty())
    }
    
    // Helper method to find components by type
    private fun <T : JComponent> findComponentByType(container: JComponent, type: Class<T>): T? {
        if (type.isInstance(container)) {
            @Suppress("UNCHECKED_CAST")
            return container as T
        }
        
        for (component in container.components) {
            if (component is JComponent) {
                val result = findComponentByType(component, type)
                if (result != null) {
                    return result
                }
            }
        }
        
        return null
    }
    
    // Helper method to find all components of a type
    private fun <T : JComponent> findComponentsByType(container: JComponent, type: Class<T>): List<T> {
        val result = mutableListOf<T>()
        
        if (type.isInstance(container)) {
            @Suppress("UNCHECKED_CAST")
            result.add(container as T)
        }
        
        for (component in container.components) {
            if (component is JComponent) {
                result.addAll(findComponentsByType(component, type))
            }
        }
        
        return result
    }
}
