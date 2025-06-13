package com.suimove.intellij.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.suimove.intellij.psi.MoveFile
import com.suimove.intellij.psi.MoveTypes

class MoveUnusedVariableInspection : LocalInspectionTool() {
    override fun getDisplayName(): String = "Unused variable"
    
    override fun getShortName(): String = "MoveUnusedVariable"
    
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element.node?.elementType == MoveTypes.LET_BINDING) {
                    checkLetBinding(element, holder)
                }
            }
        }
    }
    
    private fun checkLetBinding(letBinding: PsiElement, holder: ProblemsHolder) {
        val identifier = letBinding.node?.findChildByType(MoveTypes.IDENTIFIER) ?: return
        val variableName = identifier.text
        
        // Skip underscore variables (they're intentionally unused)
        if (variableName.startsWith("_")) return
        
        val scope = findScope(letBinding)
        if (scope != null && !isVariableUsed(variableName, scope, letBinding)) {
            holder.registerProblem(
                identifier.psi,
                "Unused variable '$variableName'"
            )
        }
    }
    
    private fun findScope(element: PsiElement): PsiElement? {
        // Find the enclosing block or function
        return PsiTreeUtil.getParentOfType(element, PsiElement::class.java) {
            it.node?.elementType in listOf(MoveTypes.BLOCK, MoveTypes.FUNCTION_DEFINITION)
        }
    }
    
    private fun isVariableUsed(variableName: String, scope: PsiElement, declaration: PsiElement): Boolean {
        var used = false
        
        PsiTreeUtil.processElements(scope) { element ->
            if (element != declaration && 
                element.node?.elementType == MoveTypes.IDENTIFIER && 
                element.text == variableName) {
                // Check if this is a usage (not another declaration)
                val parent = element.parent
                if (parent != declaration && parent?.node?.elementType != MoveTypes.LET_BINDING) {
                    used = true
                    return@processElements false // Stop processing
                }
            }
            true // Continue processing
        }
        
        return used
    }
}
