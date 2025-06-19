package com.suimove.intellij.refactoring

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.refactoring.rename.RenameHandler
import com.intellij.refactoring.rename.inplace.VariableInplaceRenamer
import com.suimove.intellij.psi.*

/**
 * Enhanced rename handler for Move elements.
 */
class MoveRenameHandler : RenameHandler {
    
    override fun isAvailableOnDataContext(dataContext: DataContext): Boolean {
        val element = findTargetElement(dataContext) ?: return false
        return canRename(element)
    }
    
    override fun invoke(project: Project, editor: Editor?, file: PsiFile?, dataContext: DataContext) {
        val element = findTargetElement(dataContext) ?: return
        
        if (editor != null && canRenameInplace(element)) {
            // Use in-place rename for local variables
            val renamer = MoveVariableInplaceRenamer(element as PsiNamedElement, editor)
            renamer.performInplaceRename()
        } else {
            // Use dialog for other elements
            invoke(project, arrayOf(element), dataContext)
        }
    }
    
    override fun invoke(project: Project, elements: Array<out PsiElement>, dataContext: DataContext) {
        if (elements.isEmpty()) return
        
        val element = elements[0]
        if (!canRename(element)) return
        
        // Validate rename
        val validator = MoveRenameValidator(element)
        
        // Show rename dialog
        val dialog = when (element) {
            is MoveFunction -> MoveRenameDialog(project, element as PsiNamedElement, null, null)
            is MoveStruct -> MoveRenameDialog(project, element as PsiNamedElement, null, null)
            is MoveModule -> MoveRenameDialog(project, element as PsiNamedElement, null, null)
            is MoveConstant -> MoveRenameDialog(project, element as PsiNamedElement, null, null)
            is MoveVariable -> MoveRenameDialog(project, element as PsiNamedElement, null, null)
            is MoveStructField -> MoveRenameDialog(project, element as PsiNamedElement, null, null)
            is MoveTypeParameter -> MoveRenameDialog(project, element as PsiNamedElement, null, null)
            else -> null
        }
        dialog?.show()
    }
    
    private fun findTargetElement(dataContext: DataContext): PsiElement? {
        return com.intellij.openapi.actionSystem.CommonDataKeys.PSI_ELEMENT.getData(dataContext)
    }
    
    private fun canRename(element: PsiElement): Boolean {
        return when (element) {
            is MoveFunction -> !element.isNative
            is MoveStruct -> true
            is MoveModule -> true
            is MoveConstant -> true
            is MoveVariable -> true
            is MoveStructField -> true
            is MoveTypeParameter -> true
            else -> false
        }
    }
    
    private fun canRenameInplace(element: PsiElement): Boolean {
        return element is MoveVariable && isLocalVariable(element)
    }
    
    private fun isLocalVariable(variable: MoveVariable): Boolean {
        // Check if variable is local (not a function parameter or struct field)
        val parent = variable.parent
        return parent is MoveLetStatement || parent is MovePattern
    }
}

/**
 * In-place renamer for Move variables.
 */
class MoveVariableInplaceRenamer(
    elementToRename: PsiNamedElement,
    editor: Editor
) : VariableInplaceRenamer(elementToRename, editor) {
    
    override fun isIdentifier(newName: String?, language: com.intellij.lang.Language?): Boolean {
        return newName != null && isValidMoveName(newName)
    }
    
    private fun isValidMoveName(name: String): Boolean {
        return name.matches(Regex("[a-zA-Z_][a-zA-Z0-9_]*"))
    }
}

/**
 * Validator for Move rename operations.
 */
class MoveRenameValidator(private val element: PsiElement) {
    
    fun isValid(newName: String): ValidationResult {
        // Check name format
        if (!isValidName(newName, element)) {
            return ValidationResult(false, "Invalid name format")
        }
        
        // Check for conflicts
        val conflict = findConflict(newName, element)
        if (conflict != null) {
            return ValidationResult(false, "Name '$newName' is already used by ${describeElement(conflict)}")
        }
        
        // Check Move keywords
        if (isMoveKeyword(newName)) {
            return ValidationResult(false, "'$newName' is a Move keyword")
        }
        
        // Check Sui-specific rules
        if (element is MoveFunction && element.isEntry && !newName.matches(Regex("[a-z_][a-z0-9_]*"))) {
            return ValidationResult(false, "Entry function names must be lowercase")
        }
        
        return ValidationResult(true)
    }
    
    private fun isValidName(name: String, element: PsiElement): Boolean {
        return when (element) {
            is MoveFunction -> name.matches(Regex("[a-z_][a-z0-9_]*"))
            is MoveStruct -> name.matches(Regex("[A-Z][a-zA-Z0-9_]*"))
            is MoveModule -> name.matches(Regex("[a-z_][a-z0-9_]*"))
            is MoveConstant -> name.matches(Regex("[A-Z_][A-Z0-9_]*"))
            is MoveVariable -> name.matches(Regex("[a-z_][a-z0-9_]*"))
            is MoveTypeParameter -> name.matches(Regex("[A-Z][a-zA-Z0-9_]*"))
            else -> name.matches(Regex("[a-zA-Z_][a-zA-Z0-9_]*"))
        }
    }
    
    private fun findConflict(newName: String, element: PsiElement): PsiElement? {
        val scope = element.parent ?: return null
        
        return when (element) {
            is MoveFunction -> findFunctionConflict(newName, element, scope)
            is MoveStruct -> findStructConflict(newName, element, scope)
            is MoveModule -> findModuleConflict(newName, element)
            is MoveVariable -> findVariableConflict(newName, element, scope)
            else -> null
        }
    }
    
    private fun findFunctionConflict(name: String, function: MoveFunction, scope: PsiElement): PsiElement? {
        if (scope is MoveModule) {
            return scope.functions.find { it != function && it.name == name }
        }
        return null
    }
    
    private fun findStructConflict(name: String, struct: MoveStruct, scope: PsiElement): PsiElement? {
        if (scope is MoveModule) {
            return scope.structs.find { it != struct && it.name == name }
        }
        return null
    }
    
    private fun findModuleConflict(name: String, module: MoveModule): PsiElement? {
        val file = module.containingFile as? MoveFile ?: return null
        return file.modules.find { it != module && it.name == name }
    }
    
    private fun findVariableConflict(name: String, variable: MoveVariable, scope: PsiElement): PsiElement? {
        // Check for conflicts in the same scope
        val variables = com.intellij.psi.util.PsiTreeUtil.findChildrenOfType(scope, MoveVariable::class.java)
        return variables.find { it != variable && it.name == name && isInSameScope(it, variable) }
    }
    
    private fun isInSameScope(var1: MoveVariable, var2: MoveVariable): Boolean {
        // Simple scope check - can be enhanced
        return var1.parent == var2.parent
    }
    
    private fun isMoveKeyword(name: String): Boolean {
        return name in setOf(
            "abort", "acquires", "as", "break", "const", "continue", "copy", "else",
            "false", "friend", "fun", "has", "if", "invariant", "let", "loop", "module",
            "move", "native", "public", "return", "script", "spec", "struct", "true",
            "use", "while", "address", "mut", "ref", "Self"
        )
    }
    
    private fun describeElement(element: PsiElement): String {
        return when (element) {
            is MoveFunction -> "function"
            is MoveStruct -> "struct"
            is MoveModule -> "module"
            is MoveConstant -> "constant"
            is MoveVariable -> "variable"
            else -> "element"
        }
    }
    
    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )
}

/**
 * Extension to make elements entry functions.
 */
private val MoveFunction.isEntry: Boolean
    get() = attributeList?.attributes?.any { it.name == "entry" } ?: false

private val MoveFunction.isNative: Boolean
    get() = text.contains("native")
