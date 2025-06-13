package com.suimove.intellij.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.suimove.intellij.utils.MoveCommandRunner
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JPanel

class MoveToolWindow(private val project: Project) {
    val content: JPanel = JBPanel<JBPanel<*>>(BorderLayout())
    private val outputArea = JBTextArea()
    
    init {
        outputArea.isEditable = false
        
        val buttonPanel = JPanel()
        
        val buildButton = JButton("Build").apply {
            addActionListener {
                outputArea.text = "Building project...\n"
                MoveCommandRunner.runCommand(project, "move", listOf("build"))
            }
        }
        
        val testButton = JButton("Test").apply {
            addActionListener {
                outputArea.text = "Running tests...\n"
                MoveCommandRunner.runCommand(project, "move", listOf("test"))
            }
        }
        
        val deployButton = JButton("Deploy").apply {
            addActionListener {
                outputArea.text = "Deploying...\n"
                MoveCommandRunner.runCommand(project, "client", listOf("publish", "--gas-budget", "100000000"))
            }
        }
        
        buttonPanel.add(buildButton)
        buttonPanel.add(testButton)
        buttonPanel.add(deployButton)
        
        content.add(buttonPanel, BorderLayout.NORTH)
        content.add(JBScrollPane(outputArea), BorderLayout.CENTER)
    }
}
