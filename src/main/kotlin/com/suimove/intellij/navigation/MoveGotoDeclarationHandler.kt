package com.suimove.intellij.navigation

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.suimove.intellij.psi.MoveFile
import com.suimove.intellij.psi.MoveTypes

class MoveGotoDeclarationHandler : GotoDeclarationHandler {
    override fun getGotoDeclarationTargets(
        sourceElement: PsiElement?,
        offset: Int,
        editor: Editor?
    ): Array<PsiElement>? {
        if (sourceElement == null || sourceElement.node?.elementType != MoveTypes.IDENTIFIER) {
            return null
        }
        
        val name = sourceElement.text
        val file = sourceElement.containingFile as? MoveFile ?: return null
        val targets = mutableListOf<PsiElement>()
        
        // Search in current module first
        var current: PsiElement? = sourceElement
        while (current != null && current.node?.elementType != MoveTypes.MODULE_DEFINITION) {
            current = current.parent
        }
        val currentModule = current
        
        if (currentModule != null) {
            findDeclarationsInModule(currentModule, name, targets)
        }
        
        // Search in imported modules
        findImportedDeclarations(file, name, targets)
        
        // Search in the entire file
        if (targets.isEmpty()) {
            findDeclarationsInFile(file, name, targets)
        }
        
        return if (targets.isNotEmpty()) targets.toTypedArray() else null
    }
    
    private fun findDeclarationsInModule(module: PsiElement, name: String, targets: MutableList<PsiElement>) {
        // Find functions
        module.children.forEach { child ->
            if (child.node?.elementType == MoveTypes.FUNCTION_DEFINITION) {
                val functionName = child.node?.findChildByType(MoveTypes.IDENTIFIER)
                if (functionName?.text == name) {
                    targets.add(functionName.psi)
                }
            }
        }
        
        // Find structs
        module.children.forEach { child ->
            if (child.node?.elementType == MoveTypes.STRUCT_DEFINITION) {
                val structName = child.node?.findChildByType(MoveTypes.IDENTIFIER)
                if (structName?.text == name) {
                    targets.add(structName.psi)
                }
            }
        }
        
        // Find constants
        module.children.forEach { child ->
            if (child.node?.elementType == MoveTypes.CONST_DEFINITION) {
                val constName = child.node?.findChildByType(MoveTypes.IDENTIFIER)
                if (constName?.text == name) {
                    targets.add(constName.psi)
                }
            }
        }
    }
    
    private fun findImportedDeclarations(file: MoveFile, name: String, targets: MutableList<PsiElement>) {
        // Find use declarations
        file.children.forEach { child ->
            if (child.node?.elementType == MoveTypes.USE_DECL) {
                // Check if this use declaration imports the name we're looking for
                val imported = child.node?.findChildByType(MoveTypes.IDENTIFIER)
                if (imported?.text == name) {
                    targets.add(imported.psi)
                }
            }
        }
    }
    
    private fun findDeclarationsInFile(file: MoveFile, name: String, targets: MutableList<PsiElement>) {
        // Find all modules in file
        file.children.forEach { child ->
            if (child.node?.elementType == MoveTypes.MODULE_DEFINITION) {
                findDeclarationsInModule(child, name, targets)
            }
        }
        
        // Find script functions
        file.children.forEach { child ->
            if (child.node?.elementType == MoveTypes.SCRIPT_DEFINITION) {
                child.children.forEach { scriptChild ->
                    if (scriptChild.node?.elementType == MoveTypes.FUNCTION_DEFINITION) {
                        val functionName = scriptChild.node?.findChildByType(MoveTypes.IDENTIFIER)
                        if (functionName?.text == name) {
                            targets.add(functionName.psi)
                        }
                    }
                }
            }
        }
    }
}
