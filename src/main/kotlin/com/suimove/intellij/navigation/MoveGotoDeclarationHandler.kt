package com.suimove.intellij.navigation

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
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
        val currentModule = PsiTreeUtil.getParentOfType(sourceElement, PsiElement::class.java) {
            it.node?.elementType == MoveTypes.MODULE_DEFINITION
        }
        
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
        PsiTreeUtil.findChildrenOfType(module, PsiElement::class.java)
            .filter { it.node?.elementType == MoveTypes.FUNCTION_DEFINITION }
            .forEach { function ->
                val functionName = function.node?.findChildByType(MoveTypes.IDENTIFIER)
                if (functionName?.text == name) {
                    targets.add(functionName.psi)
                }
            }
        
        // Find structs
        PsiTreeUtil.findChildrenOfType(module, PsiElement::class.java)
            .filter { it.node?.elementType == MoveTypes.STRUCT_DEFINITION }
            .forEach { struct ->
                val structName = struct.node?.findChildByType(MoveTypes.IDENTIFIER)
                if (structName?.text == name) {
                    targets.add(structName.psi)
                }
            }
        
        // Find constants
        PsiTreeUtil.findChildrenOfType(module, PsiElement::class.java)
            .filter { it.node?.elementType == MoveTypes.CONST_DEFINITION }
            .forEach { const ->
                val constName = const.node?.findChildByType(MoveTypes.IDENTIFIER)
                if (constName?.text == name) {
                    targets.add(constName.psi)
                }
            }
    }
    
    private fun findImportedDeclarations(file: MoveFile, name: String, targets: MutableList<PsiElement>) {
        // Find use declarations
        PsiTreeUtil.findChildrenOfType(file, PsiElement::class.java)
            .filter { it.node?.elementType == MoveTypes.USE_DECL }
            .forEach { useDecl ->
                // Check if this use declaration imports the name we're looking for
                val imported = useDecl.node?.findChildByType(MoveTypes.IDENTIFIER)
                if (imported?.text == name) {
                    targets.add(imported.psi)
                }
            }
    }
    
    private fun findDeclarationsInFile(file: MoveFile, name: String, targets: MutableList<PsiElement>) {
        // Search all modules in the file
        PsiTreeUtil.findChildrenOfType(file, PsiElement::class.java)
            .filter { it.node?.elementType == MoveTypes.MODULE_DEFINITION }
            .forEach { module ->
                findDeclarationsInModule(module, name, targets)
            }
    }
}
