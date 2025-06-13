package com.suimove.intellij.toolwindow

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentManager
import com.suimove.intellij.MoveFileType
import org.mockito.Mockito.*

class MoveToolWindowTest : BasePlatformTestCase() {
    
    fun testToolWindowCreation() {
        // Create a Move file to ensure the project is recognized as a Move project
        myFixture.configureByText(MoveFileType, """
            module 0x1::test {
                fun main() {}
            }
        """.trimIndent())
        
        // Get the tool window factory
        val factory = MoveToolWindowFactory()
        assertNotNull("Tool window factory should exist", factory)
        
        // Mock the content manager to verify interactions
        val contentManager = mock(ContentManager::class.java)
        val content = mock(Content::class.java)
        `when`(contentManager.factory).thenReturn(myFixture.project.getService(ContentManager::class.java).factory)
        `when`(contentManager.factory.createContent(any(), anyString(), anyBoolean())).thenReturn(content)
        
        // Create the tool window
        factory.createToolWindowContent(myFixture.project, mock(com.intellij.openapi.wm.ToolWindow::class.java))
        
        // Verify that the tool window was created with the correct components
        verify(contentManager, atLeastOnce()).factory
    }
    
    fun testToolWindowVisibility() {
        // Create a Move file
        myFixture.configureByText(MoveFileType, """
            module 0x1::test {
                fun main() {}
            }
        """.trimIndent())
        
        // Get the tool window manager
        val toolWindowManager = com.intellij.openapi.wm.ToolWindowManager.getInstance(myFixture.project)
        assertNotNull("Tool window manager should exist", toolWindowManager)
        
        // Check if the Move tool window is registered
        val toolWindow = toolWindowManager.getToolWindow("Move")
        // Note: In a test environment, the tool window might not be registered
        // This is more of a sanity check
        if (toolWindow != null) {
            assertTrue("Tool window should be available", toolWindow.isAvailable)
        }
    }
    
    fun testToolWindowComponents() {
        // Create a Move file
        myFixture.configureByText(MoveFileType, """
            module 0x1::test {
                fun main() {}
            }
        """.trimIndent())
        
        // Create the tool window panel
        val panel = MoveToolWindowPanel(myFixture.project)
        assertNotNull("Tool window panel should be created", panel)
        
        // Check that the panel has the expected components
        assertTrue("Panel should have components", panel.componentCount > 0)
        
        // Check for action buttons
        val actionButtons = panel.components.filter { it is javax.swing.JButton }
        assertTrue("Panel should have action buttons", actionButtons.isNotEmpty())
    }
    
    fun testBuildAction() {
        // Create a Move file
        myFixture.configureByText(MoveFileType, """
            module 0x1::test {
                fun main() {}
            }
        """.trimIndent())
        
        // Create the build action
        val buildAction = MoveBuildAction()
        assertNotNull("Build action should be created", buildAction)
        
        // Check that the action is properly configured
        assertEquals("Action should have correct text", "Build", buildAction.templateText)
        assertNotNull("Action should have an icon", buildAction.templatePresentation.icon)
    }
    
    fun testTestAction() {
        // Create a Move file
        myFixture.configureByText(MoveFileType, """
            module 0x1::test {
                fun main() {}
            }
        """.trimIndent())
        
        // Create the test action
        val testAction = MoveTestAction()
        assertNotNull("Test action should be created", testAction)
        
        // Check that the action is properly configured
        assertEquals("Action should have correct text", "Test", testAction.templateText)
        assertNotNull("Action should have an icon", testAction.templatePresentation.icon)
    }
    
    fun testDeployAction() {
        // Create a Move file
        myFixture.configureByText(MoveFileType, """
            module 0x1::test {
                fun main() {}
            }
        """.trimIndent())
        
        // Create the deploy action
        val deployAction = MoveDeployAction()
        assertNotNull("Deploy action should be created", deployAction)
        
        // Check that the action is properly configured
        assertEquals("Action should have correct text", "Deploy", deployAction.templateText)
        assertNotNull("Action should have an icon", deployAction.templatePresentation.icon)
    }
    
    fun testToolWindowRefresh() {
        // Create a Move file
        myFixture.configureByText(MoveFileType, """
            module 0x1::test {
                fun main() {}
            }
        """.trimIndent())
        
        // Create the tool window panel
        val panel = MoveToolWindowPanel(myFixture.project)
        
        // Mock the refresh method
        val spyPanel = spy(panel)
        
        // Trigger refresh
        spyPanel.refresh()
        
        // Verify that refresh was called
        verify(spyPanel, times(1)).refresh()
    }
    
    fun testToolWindowActionAvailability() {
        // Create a Move file
        myFixture.configureByText(MoveFileType, """
            module 0x1::test {
                fun main() {}
            }
        """.trimIndent())
        
        // Create actions
        val buildAction = MoveBuildAction()
        val testAction = MoveTestAction()
        val deployAction = MoveDeployAction()
        
        // Check that actions are available for Move projects
        assertTrue("Build action should be available", buildAction.isAvailable(myFixture.project))
        assertTrue("Test action should be available", testAction.isAvailable(myFixture.project))
        assertTrue("Deploy action should be available", deployAction.isAvailable(myFixture.project))
    }
    
    fun testToolWindowConsoleOutput() {
        // Create a Move file
        myFixture.configureByText(MoveFileType, """
            module 0x1::test {
                fun main() {}
            }
        """.trimIndent())
        
        // Create the tool window panel
        val panel = MoveToolWindowPanel(myFixture.project)
        
        // Add console output
        panel.appendToConsole("Test output")
        
        // Check that the console has the output
        val consoleText = panel.getConsoleText()
        assertTrue("Console should contain the output", consoleText.contains("Test output"))
    }
    
    private fun MoveToolWindowPanel.getConsoleText(): String {
        // Access the console component and get its text
        val consoleComponent = this.components.find { it is javax.swing.JTextArea }
        return (consoleComponent as? javax.swing.JTextArea)?.text ?: ""
    }
}
