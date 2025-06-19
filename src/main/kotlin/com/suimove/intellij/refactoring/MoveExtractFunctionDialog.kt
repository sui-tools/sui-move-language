package com.suimove.intellij.refactoring

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.refactoring.ui.NameSuggestionsField
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBRadioButton
import com.suimove.intellij.MoveFileType
import com.suimove.intellij.psi.name
import javax.swing.*
import java.awt.GridBagLayout
import java.awt.GridBagConstraints
import java.awt.Insets

/**
 * Dialog for extract function refactoring.
 */
class MoveExtractFunctionDialog(
    project: Project,
    private val analysis: ExtractionAnalysis
) : DialogWrapper(project) {
    
    private val nameField = NameSuggestionsField(
        suggestNames(),
        project,
        MoveFileType
    )
    
    private val publicRadio = JBRadioButton("public", true)
    private val publicFriendRadio = JBRadioButton("public(friend)")
    private val privateRadio = JBRadioButton("private")
    
    init {
        title = "Extract Function"
        init()
    }
    
    override fun createCenterPanel(): JComponent {
        val panel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints()
        gbc.insets = Insets(5, 5, 5, 5)
        gbc.fill = GridBagConstraints.HORIZONTAL
        
        // Function name row
        gbc.gridx = 0
        gbc.gridy = 0
        panel.add(JBLabel("Function name:"), gbc)
        
        gbc.gridx = 1
        gbc.weightx = 1.0
        panel.add(nameField, gbc)
        
        // Visibility row
        gbc.gridx = 0
        gbc.gridy = 1
        gbc.weightx = 0.0
        panel.add(JBLabel("Visibility:"), gbc)
        
        val visibilityPanel = JPanel()
        visibilityPanel.add(publicRadio)
        visibilityPanel.add(publicFriendRadio)
        visibilityPanel.add(privateRadio)
        
        gbc.gridx = 1
        gbc.weightx = 1.0
        panel.add(visibilityPanel, gbc)
        
        // Parameters row
        if (analysis.inputVariables.isNotEmpty()) {
            gbc.gridx = 0
            gbc.gridy = 2
            gbc.gridwidth = 2
            panel.add(JBLabel("Parameters: ${formatParameters()}"), gbc)
        }
        
        // Returns row
        if (analysis.outputVariables.isNotEmpty() || analysis.modifiedVariables.isNotEmpty()) {
            gbc.gridx = 0
            gbc.gridy = 3
            gbc.gridwidth = 2
            panel.add(JBLabel("Returns: ${formatReturns()}"), gbc)
        }
        
        return panel
    }
    
    override fun doValidate(): ValidationInfo? {
        val name = nameField.enteredName
        
        if (name.isBlank()) {
            return ValidationInfo("Function name cannot be empty", nameField)
        }
        
        if (!isValidFunctionName(name)) {
            return ValidationInfo("Invalid function name", nameField)
        }
        
        return null
    }
    
    fun getFunctionName(): String = nameField.enteredName
    
    fun getVisibility(): String = when {
        publicRadio.isSelected -> "public"
        publicFriendRadio.isSelected -> "public(friend)"
        else -> ""
    }
    
    private fun suggestNames(): Array<String> {
        val suggestions = mutableListOf<String>()
        
        // Basic suggestions
        suggestions.add("extracted_function")
        suggestions.add("helper")
        suggestions.add("do_operation")
        
        // Try to infer from the code
        if (analysis.outputVariables.size == 1) {
            val varName = analysis.outputVariables.first().name
            suggestions.add("get_$varName")
            suggestions.add("calculate_$varName")
            suggestions.add("compute_$varName")
        }
        
        if (analysis.modifiedVariables.isNotEmpty()) {
            val varName = analysis.modifiedVariables.first().name
            suggestions.add("update_$varName")
            suggestions.add("modify_$varName")
            suggestions.add("process_$varName")
        }
        
        return suggestions.toTypedArray()
    }
    
    private fun formatParameters(): String {
        return analysis.inputVariables.joinToString(", ") { variable ->
            val prefix = if (variable in analysis.modifiedVariables) "&mut " else ""
            "$prefix${variable.name}"
        }
    }
    
    private fun formatReturns(): String {
        return when {
            analysis.outputVariables.isNotEmpty() -> analysis.outputVariables.first().name ?: "unknown"
            else -> "void"
        }
    }
    
    private fun isValidFunctionName(name: String): Boolean {
        return name.matches(Regex("[a-z_][a-z0-9_]*"))
    }
}
