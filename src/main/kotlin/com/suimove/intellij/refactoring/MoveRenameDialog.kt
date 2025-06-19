package com.suimove.intellij.refactoring

import com.intellij.openapi.project.Project
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.refactoring.rename.RenameDialog
import com.intellij.refactoring.ui.NameSuggestionsField
import com.intellij.ui.components.JBLabel
import com.suimove.intellij.psi.*
import com.suimove.intellij.MoveFileType
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.BoxLayout
import java.awt.Font

/**
 * Custom rename dialog for Move elements.
 */
class MoveRenameDialog(
    project: Project,
    element: PsiNamedElement,
    nameSuggestionContext: PsiElement?,
    editor: Editor?
) : RenameDialog(project, element, nameSuggestionContext, editor) {
    
    private val validator = MoveRenameValidator(element)
    private lateinit var nameSuggestionsField: NameSuggestionsField
    
    init {
        title = "Rename ${getElementTypeDescription()}"
    }
    
    override fun createNewNameComponent() {
        val suggestions = suggestNames()
        nameSuggestionsField = NameSuggestionsField(suggestions, myProject, MoveFileType)
        // Set the field in the parent class
        try {
            val field = RenameDialog::class.java.getDeclaredField("myNameSuggestionsField")
            field.isAccessible = true
            field.set(this, nameSuggestionsField)
        } catch (e: Exception) {
            // Ignore if field not found
        }
    }
    
    override fun canRun() {
        val newName = newName
        val result = validator.isValid(newName)
        
        if (!result.isValid) {
            setErrorText(result.errorMessage)
            return
        }
        
        setErrorText(null)
    }
    
    override fun areButtonsValid(): Boolean {
        val newName = newName
        return newName.isNotEmpty() && validator.isValid(newName).isValid
    }
    
    override fun createNorthPanel(): JComponent? {
        val panel = super.createNorthPanel()
        
        // Add additional information for specific elements
        when (psiElement) {
            is MoveFunction -> {
                if ((psiElement as MoveFunction).isEntry) {
                    val newPanel = JPanel()
                    newPanel.layout = BoxLayout(newPanel, BoxLayout.Y_AXIS)
                    newPanel.add(panel)
                    val noteLabel = JBLabel("Note: This is an entry function")
                    noteLabel.font = noteLabel.font.deriveFont(Font.ITALIC)
                    newPanel.add(noteLabel)
                    return newPanel
                }
            }
            is MoveStruct -> {
                if (hasSuiObjectAbility(psiElement as MoveStruct)) {
                    val newPanel = JPanel()
                    newPanel.layout = BoxLayout(newPanel, BoxLayout.Y_AXIS)
                    newPanel.add(panel)
                    val noteLabel = JBLabel("Note: This is a Sui object")
                    noteLabel.font = noteLabel.font.deriveFont(Font.ITALIC)
                    newPanel.add(noteLabel)
                    return newPanel
                }
            }
        }
        
        return panel
    }
    
    private fun getElementTypeDescription(): String {
        return when (psiElement) {
            is MoveFunction -> "Function"
            is MoveStruct -> "Struct"
            is MoveModule -> "Module"
            is MoveConstant -> "Constant"
            is MoveVariable -> "Variable"
            is MoveTypeParameter -> "Type Parameter"
            else -> "Element"
        }
    }
    
    private fun suggestNames(): Array<String> {
        val currentName = (psiElement as? PsiNamedElement)?.name ?: return emptyArray()
        val suggestions = mutableListOf(currentName)
        
        when (psiElement) {
            is MoveFunction -> {
                // Suggest function name variations
                if (currentName.startsWith("get_")) {
                    suggestions.add(currentName.removePrefix("get_").let { "fetch_$it" })
                    suggestions.add(currentName.removePrefix("get_").let { "read_$it" })
                }
                if (currentName.startsWith("set_")) {
                    suggestions.add(currentName.removePrefix("set_").let { "update_$it" })
                    suggestions.add(currentName.removePrefix("set_").let { "write_$it" })
                }
            }
            is MoveStruct -> {
                // Suggest struct name variations
                if (!currentName.endsWith("Data")) {
                    suggestions.add("${currentName}Data")
                }
                if (!currentName.endsWith("Info")) {
                    suggestions.add("${currentName}Info")
                }
            }
            is MoveVariable -> {
                // Suggest variable name variations
                if (currentName.length > 1) {
                    suggestions.add("${currentName}_value")
                    suggestions.add("${currentName}_data")
                    suggestions.add("new_$currentName")
                }
            }
        }
        
        return suggestions.distinct().toTypedArray()
    }
    
    private fun hasSuiObjectAbility(struct: MoveStruct): Boolean {
        return struct.abilities?.any { it.name == "key" } ?: false
    }
}

/**
 * Extension properties for rename dialog.
 */
private val MoveFunction.isEntry: Boolean
    get() = attributeList?.attributes?.any { it.name == "entry" } ?: false

private val MoveStruct.abilities: List<MoveAbility>?
    get() = abilityList?.abilities

interface MoveAbilityList : PsiElement {
    val abilities: List<MoveAbility>
}

interface MoveAbility : PsiElement {
    val name: String?
}

private val MoveStruct.abilityList: MoveAbilityList?
    get() = com.intellij.psi.util.PsiTreeUtil.findChildOfType(this, MoveAbilityList::class.java)
