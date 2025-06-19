package com.suimove.intellij.refactoring

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import com.suimove.intellij.psi.MoveVariable
import com.suimove.intellij.psi.name
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.BoxLayout

/**
 * Dialog for inline variable refactoring.
 */
class MoveInlineVariableDialog(
    project: Project,
    private val variable: MoveVariable,
    private val usageCount: Int
) : DialogWrapper(project) {
    
    init {
        title = "Inline Variable"
        init()
    }
    
    override fun createCenterPanel(): JComponent {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        
        panel.add(JBLabel("Inline variable '${variable.name}'?"))
        panel.add(JBLabel("Found $usageCount usage${if (usageCount != 1) "s" else ""}"))
        
        val warningLabel = JBLabel("Warning: This operation cannot be undone automatically")
        warningLabel.font = warningLabel.font.deriveFont(java.awt.Font.ITALIC)
        panel.add(warningLabel)
        
        return panel
    }
}
