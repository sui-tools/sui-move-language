package com.suimove.intellij.refactoring

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.suimove.intellij.psi.MoveFunction
import com.suimove.intellij.psi.name
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.BoxLayout

/**
 * Dialog for inline function refactoring.
 */
class MoveInlineFunctionDialog(
    project: Project,
    private val function: MoveFunction,
    private val usageCount: Int
) : DialogWrapper(project) {
    
    private val deleteCheckbox = JBCheckBox("Delete function declaration after inline", true)
    
    init {
        title = "Inline Function"
        init()
    }
    
    override fun createCenterPanel(): JComponent {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        
        panel.add(JBLabel("Inline function '${function.name}'?"))
        panel.add(JBLabel("Found $usageCount usage${if (usageCount != 1) "s" else ""}"))
        panel.add(deleteCheckbox)
        
        val warningLabel = JBLabel("Warning: This operation cannot be undone automatically")
        warningLabel.font = warningLabel.font.deriveFont(java.awt.Font.ITALIC)
        panel.add(warningLabel)
        
        return panel
    }
    
    fun isDeleteFunction(): Boolean = deleteCheckbox.isSelected
}
