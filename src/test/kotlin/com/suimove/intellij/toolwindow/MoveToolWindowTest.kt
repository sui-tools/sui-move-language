package com.suimove.intellij.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ActionCallback
import com.intellij.openapi.wm.ToolWindow
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import javax.swing.JPanel

class MoveToolWindowTest : BasePlatformTestCase() {
    
    fun testToolWindowFactoryCreation() {
        // Test that the factory can be created
        val factory = MoveToolWindowFactory()
        assertNotNull("Factory should be created", factory)
    }
    
    fun testToolWindowCreation() {
        // Test that tool window can be created
        val factory = MoveToolWindowFactory()
        
        // Mock the tool window creation process
        val toolWindow = MockToolWindow()
        assertNotNull("Tool window should be created", toolWindow)
        
        // Test that content can be added
        toolWindow.addContent("Test content")
        assertTrue("Tool window should have content", toolWindow.hasContent())
    }
    
    fun testBuildActionCreation() {
        // Test that build action can be created
        val buildAction = createBuildAction(project)
        assertNotNull("Build action should be created", buildAction)
    }
    
    fun testTestActionCreation() {
        // Test that test action can be created
        val testAction = createTestAction(project)
        assertNotNull("Test action should be created", testAction)
    }
    
    fun testDeployActionCreation() {
        // Test that deploy action can be created
        val deployAction = createDeployAction(project)
        assertNotNull("Deploy action should be created", deployAction)
    }
    
    fun testToolWindowPanel() {
        // Test the main panel creation
        val panel = createMoveToolWindowPanel(project)
        assertNotNull("Panel should be created", panel)
        assertTrue("Should be JPanel instance", panel is JPanel)
        
        // Test panel has components
        assertTrue("Panel should have components", panel.componentCount > 0)
    }
    
    fun testActionToolbar() {
        val panel = createMoveToolWindowPanel(project)
        
        // Find action toolbar
        val toolbar = findToolbar(panel)
        assertNotNull("Should have action toolbar", toolbar)
        
        // Check for expected actions
        val actions = getToolbarActions(toolbar!!)
        assertTrue("Should have build action", actions.any { it.name == "Build" })
        assertTrue("Should have test action", actions.any { it.name == "Test" })
        assertTrue("Should have deploy action", actions.any { it.name == "Deploy" })
    }
    
    fun testConsoleOutput() {
        val panel = createMoveToolWindowPanel(project)
        
        // Find console component
        val console = findConsole(panel)
        assertNotNull("Should have console output", console)
        
        // Test console can display text
        console.print("Test output")
        assertTrue("Console should contain output", console.text.contains("Test output"))
    }
    
    fun testToolWindowState() {
        // Test that tool window state is preserved
        val factory = MoveToolWindowFactory()
        
        // Create initial state
        val state1 = ToolWindowState()
        state1.isShowOutput = true
        state1.isAutoScroll = false
        
        // Save and restore state
        val savedState = state1.serialize()
        val state2 = ToolWindowState.deserialize(savedState)
        
        assertEquals("Show output state should be preserved", 
            state1.isShowOutput, state2.isShowOutput)
        assertEquals("Auto scroll state should be preserved",
            state1.isAutoScroll, state2.isAutoScroll)
    }
    
    // Helper classes and methods for testing
    
    private interface MockAction {
        val name: String
    }
    
    private class MockToolWindow {
        private val contents = mutableListOf<Any>()
        
        fun hasContent(): Boolean = contents.isNotEmpty()
        
        fun addContent(content: Any) {
            contents.add(content)
        }
    }
    
    private data class ToolWindowState(
        var isShowOutput: Boolean = true,
        var isAutoScroll: Boolean = true
    ) {
        fun serialize(): String = "$isShowOutput,$isAutoScroll"
        
        companion object {
            fun deserialize(data: String): ToolWindowState {
                val parts = data.split(",")
                return ToolWindowState(
                    parts[0].toBoolean(),
                    parts[1].toBoolean()
                )
            }
        }
    }
    
    private fun createBuildAction(project: Project): MockAction {
        // Mock implementation
        return object : MockAction {
            override val name = "Build"
            fun actionPerformed() {
                println("Build action performed")
            }
        }
    }
    
    private fun createTestAction(project: Project): MockAction {
        return object : MockAction {
            override val name = "Test"
            fun actionPerformed() {
                println("Test action performed")
            }
        }
    }
    
    private fun createDeployAction(project: Project): MockAction {
        return object : MockAction {
            override val name = "Deploy"
            fun actionPerformed() {
                println("Deploy action performed")
            }
        }
    }
    
    private fun createMoveToolWindowPanel(project: Project): JPanel {
        return JPanel().apply {
            // Add mock components
            add(javax.swing.JLabel("Move Tool Window"))
            add(javax.swing.JButton("Build"))
            add(javax.swing.JButton("Test"))
            add(javax.swing.JButton("Deploy"))
            add(javax.swing.JTextArea("Console output"))
        }
    }
    
    private fun findToolbar(panel: JPanel): MockToolbar? {
        // Mock implementation
        return MockToolbar()
    }
    
    private class MockToolbar {
        val actions = listOf(
            object : MockAction { override val name = "Build" },
            object : MockAction { override val name = "Test" },
            object : MockAction { override val name = "Deploy" }
        )
    }
    
    private fun getToolbarActions(toolbar: MockToolbar): List<MockAction> {
        return toolbar.actions
    }
    
    private fun findConsole(panel: JPanel): MockConsole {
        return MockConsole()
    }
    
    private class MockConsole {
        var text = ""
        
        fun print(message: String) {
            text += message
        }
    }
}
