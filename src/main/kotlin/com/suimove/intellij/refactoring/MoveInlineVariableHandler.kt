package com.suimove.intellij.refactoring

import com.intellij.codeInsight.TargetElementUtil
import com.intellij.lang.refactoring.InlineActionHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.util.CommonRefactoringUtil
import com.suimove.intellij.psi.*
import com.suimove.intellij.MoveLanguage

/**
 * Handler for inlining Move variables.
 */
class MoveInlineVariableHandler : InlineActionHandler() {
    
    override fun isEnabledForLanguage(l: com.intellij.lang.Language): Boolean {
        return l == MoveLanguage
    }
    
    override fun canInlineElement(element: PsiElement): Boolean {
        return element is MoveVariable && canInlineVariable(element)
    }
    
    override fun inlineElement(project: Project, editor: Editor?, element: PsiElement) {
        if (element !is MoveVariable) return
        
        // Validate inline
        val errors = validateInline(element)
        if (errors.isNotEmpty()) {
            CommonRefactoringUtil.showErrorHint(
                project,
                editor,
                errors.first(),
                "Inline Variable",
                null
            )
            return
        }
        
        // Find initialization
        val initialization = findInitialization(element)
        if (initialization == null) {
            CommonRefactoringUtil.showErrorHint(
                project,
                editor,
                "Cannot find variable initialization",
                "Inline Variable",
                null
            )
            return
        }
        
        // Find all usages
        val usages = findUsages(element)
        if (usages.isEmpty()) {
            CommonRefactoringUtil.showErrorHint(
                project,
                editor,
                "Variable '${element.name}' is never used",
                "Inline Variable",
                null
            )
            return
        }
        
        // Show confirmation dialog
        val dialog = MoveInlineVariableDialog(project, element, usages.size)
        if (!dialog.showAndGet()) {
            return
        }
        
        // Perform inline
        performInline(project, element, initialization, usages)
    }
    
    private fun canInlineVariable(variable: MoveVariable): Boolean {
        // Can inline local variables and simple parameters
        val parent = variable.parent
        return parent is MoveLetStatement || parent is MovePattern
    }
    
    private fun validateInline(variable: MoveVariable): List<String> {
        val errors = mutableListOf<String>()
        
        // Check if variable is mutable and modified
        if (isMutable(variable) && isModified(variable)) {
            errors.add("Cannot inline mutable variable that is modified after initialization")
        }
        
        // Check if variable is used in complex patterns
        if (isUsedInPattern(variable)) {
            errors.add("Cannot inline variable used in pattern matching")
        }
        
        // Check if variable is captured in closure
        if (isCapturedInClosure(variable)) {
            errors.add("Cannot inline variable captured in closure")
        }
        
        return errors
    }
    
    private fun findInitialization(variable: MoveVariable): MoveExpression? {
        val parent = variable.parent
        
        return when (parent) {
            is MoveLetStatement -> parent.expression
            is MovePattern -> {
                val letStatement = PsiTreeUtil.getParentOfType(parent, MoveLetStatement::class.java)
                letStatement?.expression
            }
            else -> null
        }
    }
    
    private fun findUsages(variable: MoveVariable): List<PsiReference> {
        return ReferencesSearch.search(variable).findAll().toList()
    }
    
    private fun performInline(
        project: Project,
        variable: MoveVariable,
        initialization: MoveExpression,
        usages: List<PsiReference>
    ) {
        val factory = MoveElementFactory.getInstance(project)
        
        // Check if initialization needs parentheses
        val needsParentheses = needsParenthesesForInline(initialization)
        
        // Replace each usage
        for (usage in usages) {
            val referenceExpr = usage.element as? MoveReferenceExpression ?: continue
            
            // Create replacement expression
            val replacement = if (needsParentheses && needsParenthesesInContext(referenceExpr)) {
                factory.createExpression("(${initialization.text})")
            } else {
                initialization.copy() as MoveExpression
            }
            
            // Replace the reference
            referenceExpr.replace(replacement)
        }
        
        // Remove the variable declaration
        val statement = PsiTreeUtil.getParentOfType(variable, MoveStatement::class.java)
        statement?.delete()
    }
    
    private fun isMutable(variable: MoveVariable): Boolean {
        val letStatement = PsiTreeUtil.getParentOfType(variable, MoveLetStatement::class.java)
        return letStatement?.isMutable ?: false
    }
    
    private fun isModified(variable: MoveVariable): Boolean {
        val scope = getVariableScope(variable)
        val assignments = PsiTreeUtil.findChildrenOfType(scope, MoveAssignment::class.java)
        
        return assignments.any { assignment ->
            val target = assignment.target
            target is MoveReferenceExpression && target.referenceName == variable.name
        }
    }
    
    private fun isUsedInPattern(variable: MoveVariable): Boolean {
        val usages = findUsages(variable)
        return usages.any { usage ->
            PsiTreeUtil.getParentOfType(usage.element, MovePattern::class.java) != null
        }
    }
    
    private fun isCapturedInClosure(variable: MoveVariable): Boolean {
        val scope = getVariableScope(variable)
        val lambdas = PsiTreeUtil.findChildrenOfType(scope, MoveLambdaExpression::class.java)
        
        return lambdas.any { lambda ->
            val refs = PsiTreeUtil.findChildrenOfType(lambda, MoveReferenceExpression::class.java)
            refs.any { it.referenceName == variable.name }
        }
    }
    
    private fun getVariableScope(variable: MoveVariable): PsiElement {
        // Find the scope where the variable is defined
        return PsiTreeUtil.getParentOfType(variable, MoveCodeBlock::class.java)
            ?: PsiTreeUtil.getParentOfType(variable, MoveFunction::class.java)
            ?: variable.containingFile
    }
    
    private fun needsParenthesesForInline(expression: MoveExpression): Boolean {
        // Check if expression needs parentheses when inlined
        return when (expression) {
            is MoveBinaryExpression -> true
            is MoveIfExpression -> true
            is MoveMatchExpression -> true
            else -> false
        }
    }
    
    private fun needsParenthesesInContext(reference: MoveReferenceExpression): Boolean {
        val parent = reference.parent
        return when (parent) {
            is MoveBinaryExpression -> true
            is MoveFunctionCall -> reference == parent.firstChild
            is MoveFieldAccess -> reference == parent.firstChild
            else -> false
        }
    }
}

/**
 * Extension properties for inline variable handler.
 */
private val MoveLetStatement.isMutable: Boolean
    get() = text.contains("mut")

private val MoveLetStatement.expression: MoveExpression?
    get() = PsiTreeUtil.findChildOfType(this, MoveExpression::class.java)

private val MoveAssignment.target: PsiElement?
    get() = firstChild

interface MoveLambdaExpression : MoveExpression
interface MoveIfExpression : MoveExpression
interface MoveMatchExpression : MoveExpression
interface MoveBinaryExpression : MoveExpression
interface MoveFieldAccess : MoveExpression

// Note: MoveCodeBlock, MoveFunctionCall, and MoveAssignment are defined in MoveInlineFunctionHandler.kt
