package com.suimove.intellij.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.suimove.intellij.psi.MoveTypes

class MoveNamingConventionInspection : LocalInspectionTool() {
    override fun getDisplayName(): String = "Naming convention violations"
    
    override fun getShortName(): String = "MoveNamingConvention"
    
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                when (element.node?.elementType) {
                    MoveTypes.MODULE_DEFINITION -> checkModuleName(element, holder)
                    MoveTypes.FUNCTION_DEFINITION -> checkFunctionName(element, holder)
                    MoveTypes.STRUCT_DEFINITION -> checkStructName(element, holder)
                    MoveTypes.CONST_DEFINITION -> checkConstantName(element, holder)
                }
            }
        }
    }
    
    private fun checkModuleName(module: PsiElement, holder: ProblemsHolder) {
        val nameNode = module.node?.findChildByType(MoveTypes.IDENTIFIER) ?: return
        val name = nameNode.text
        
        if (!name.matches(Regex("^[a-z][a-z0-9_]*$"))) {
            holder.registerProblem(
                nameNode.psi,
                "Module name should be in snake_case"
            )
        }
    }
    
    private fun checkFunctionName(function: PsiElement, holder: ProblemsHolder) {
        val nameNode = function.node?.findChildByType(MoveTypes.IDENTIFIER) ?: return
        val name = nameNode.text
        
        if (!name.matches(Regex("^[a-z][a-z0-9_]*$"))) {
            holder.registerProblem(
                nameNode.psi,
                "Function name should be in snake_case"
            )
        }
    }
    
    private fun checkStructName(struct: PsiElement, holder: ProblemsHolder) {
        val nameNode = struct.node?.findChildByType(MoveTypes.IDENTIFIER) ?: return
        val name = nameNode.text
        
        if (!name.matches(Regex("^[A-Z][a-zA-Z0-9]*$"))) {
            holder.registerProblem(
                nameNode.psi,
                "Struct name should be in PascalCase"
            )
        }
    }
    
    private fun checkConstantName(const: PsiElement, holder: ProblemsHolder) {
        val nameNode = const.node?.findChildByType(MoveTypes.IDENTIFIER) ?: return
        val name = nameNode.text
        
        if (!name.matches(Regex("^[A-Z][A-Z0-9_]*$"))) {
            holder.registerProblem(
                nameNode.psi,
                "Constant name should be in UPPER_SNAKE_CASE"
            )
        }
    }
}
