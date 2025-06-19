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
import com.intellij.usageView.UsageInfo
import com.suimove.intellij.psi.*
import com.suimove.intellij.MoveLanguage
import com.suimove.intellij.services.type.MoveTypeInferenceEngine

/**
 * Handler for inlining Move functions.
 */
class MoveInlineFunctionHandler : InlineActionHandler() {
    
    override fun isEnabledForLanguage(l: com.intellij.lang.Language): Boolean {
        return l == MoveLanguage
    }
    
    override fun canInlineElement(element: PsiElement): Boolean {
        return element is MoveFunction && !element.isNative && !element.isEntry
    }
    
    override fun inlineElement(project: Project, editor: Editor?, element: PsiElement) {
        if (element !is MoveFunction) return
        
        // Check if function can be inlined
        val errors = validateInline(element)
        if (errors.isNotEmpty()) {
            CommonRefactoringUtil.showErrorHint(
                project,
                editor,
                errors.first(),
                "Inline Function",
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
                "Function '${element.name}' is never used",
                "Inline Function",
                null
            )
            return
        }
        
        // Show confirmation dialog
        val dialog = MoveInlineFunctionDialog(project, element, usages.size)
        if (!dialog.showAndGet()) {
            return
        }
        
        // Perform inline
        performInline(project, element, usages, dialog.isDeleteFunction())
    }
    
    private fun validateInline(function: MoveFunction): List<String> {
        val errors = mutableListOf<String>()
        
        if (function.isNative) {
            errors.add("Cannot inline native functions")
        }
        
        if (function.isEntry) {
            errors.add("Cannot inline entry functions")
        }
        
        if (function.isRecursive()) {
            errors.add("Cannot inline recursive functions")
        }
        
        if (function.hasMultipleReturns()) {
            errors.add("Cannot inline functions with multiple return statements")
        }
        
        if (function.usesEarlyReturn()) {
            errors.add("Cannot inline functions with early returns")
        }
        
        return errors
    }
    
    private fun findUsages(function: MoveFunction): List<PsiReference> {
        return ReferencesSearch.search(function).findAll().toList()
    }
    
    private fun performInline(
        project: Project,
        function: MoveFunction,
        usages: List<PsiReference>,
        deleteFunction: Boolean
    ) {
        val factory = MoveElementFactory.getInstance(project)
        
        // Process each usage
        for (usage in usages) {
            val call = PsiTreeUtil.getParentOfType(usage.element, MoveFunctionCall::class.java)
                ?: continue
            
            inlineCall(call, function, factory)
        }
        
        // Delete the function if requested
        if (deleteFunction) {
            function.delete()
        }
    }
    
    private fun inlineCall(call: MoveFunctionCall, function: MoveFunction, factory: MoveElementFactory) {
        // Build parameter mapping
        val parameterMap = buildParameterMap(call, function)
        
        // Get function body
        val body = function.body ?: return
        val statements = body.statements
        
        // Handle different cases
        when {
            // Simple expression function
            statements.size == 1 && statements[0] is MoveExpression -> {
                val expression = statements[0] as MoveExpression
                val inlinedExpression = substituteParameters(expression, parameterMap, factory)
                call.replace(inlinedExpression)
            }
            
            // Function with single return statement
            statements.size == 1 && statements[0] is MoveReturnStatement -> {
                val returnStmt = statements[0] as MoveReturnStatement
                val expression = returnStmt.expression ?: return
                val inlinedExpression = substituteParameters(expression, parameterMap, factory)
                call.replace(inlinedExpression)
            }
            
            // Function with multiple statements
            else -> {
                inlineMultipleStatements(call, statements, parameterMap, factory)
            }
        }
    }
    
    private fun buildParameterMap(call: MoveFunctionCall, function: MoveFunction): Map<String, PsiElement> {
        val map = mutableMapOf<String, PsiElement>()
        val parameters = function.parameters
        val arguments = call.arguments
        
        for (i in parameters.indices) {
            if (i < arguments.size) {
                map[parameters[i].name ?: continue] = arguments[i]
            }
        }
        
        return map
    }
    
    private fun substituteParameters(
        expression: PsiElement,
        parameterMap: Map<String, PsiElement>,
        factory: MoveElementFactory
    ): PsiElement {
        // Clone the expression
        val copy = expression.copy()
        
        // Replace parameter references
        val references = PsiTreeUtil.findChildrenOfType(copy, MoveReferenceExpression::class.java)
        for (ref in references) {
            val name = ref.referenceName
            val replacement = parameterMap[name]
            if (replacement != null) {
                ref.replace(replacement.copy())
            }
        }
        
        return copy
    }
    
    private fun inlineMultipleStatements(
        call: MoveFunctionCall,
        statements: List<com.suimove.intellij.psi.MoveStatement>,
        parameterMap: Map<String, PsiElement>,
        factory: MoveElementFactory
    ) {
        val parent = call.parent
        val isAssignment = parent.parent is MoveLetStatement || parent.parent is MoveAssignment
        
        if (isAssignment && hasReturnValue(statements)) {
            // Extract return value and create temporary variable
            val tempVarName = generateTempVarName(call)
            val returnValue = extractReturnValue(statements.last())
            
            // Create block with temporary variable
            val blockText = buildInlineBlock(statements, parameterMap, tempVarName, returnValue)
            val block = factory.createBlock(blockText)
            
            // Replace the call with the temporary variable reference
            val varRef = factory.createExpression(tempVarName)
            call.replace(varRef)
            
            // Insert the block before the statement
            val statement = PsiTreeUtil.getParentOfType(call, com.suimove.intellij.psi.MoveStatement::class.java)
            statement?.parent?.addBefore(block, statement)
        } else {
            // Simple case: just insert statements
            val statement = PsiTreeUtil.getParentOfType(call, com.suimove.intellij.psi.MoveStatement::class.java) ?: return
            
            for (stmt in statements.reversed()) {
                val inlinedStmt = substituteParameters(stmt, parameterMap, factory)
                statement.parent.addAfter(inlinedStmt, statement)
                statement.parent.addAfter(factory.createNewline(), statement)
            }
            
            statement.delete()
        }
    }
    
    private fun hasReturnValue(statements: List<com.suimove.intellij.psi.MoveStatement>): Boolean {
        val lastStatement = statements.lastOrNull() ?: return false
        return lastStatement is MoveExpression || 
               (lastStatement is MoveReturnStatement && lastStatement.expression != null)
    }
    
    private fun extractReturnValue(statement: com.suimove.intellij.psi.MoveStatement): String? {
        return when (statement) {
            is MoveExpression -> (statement as PsiElement).text
            is MoveReturnStatement -> statement.expression?.text
            else -> null
        }
    }
    
    private fun generateTempVarName(call: MoveFunctionCall): String {
        val functionName = call.functionName ?: "temp"
        return "_inline_${functionName}_result"
    }
    
    private fun buildInlineBlock(
        statements: List<com.suimove.intellij.psi.MoveStatement>,
        parameterMap: Map<String, PsiElement>,
        tempVarName: String,
        returnValue: String?
    ): String {
        val builder = StringBuilder("{\n")
        
        // Add all statements except the last
        for (i in 0 until statements.size - 1) {
            val stmt = statements[i]
            builder.append("    ").append(stmt.text).append(";\n")
        }
        
        // Handle the last statement
        if (returnValue != null) {
            builder.append("    let ").append(tempVarName).append(" = ").append(returnValue).append(";\n")
        } else {
            builder.append("    ").append(statements.last().text).append(";\n")
        }
        
        builder.append("}")
        return builder.toString()
    }
}

/**
 * Extension properties for inline validation.
 */
private val MoveFunction.isNative: Boolean
    get() = text.contains("native")

private val MoveFunction.isEntry: Boolean
    get() = attributeList?.attributes?.any { it.name == "entry" } ?: false

private fun MoveFunction.isRecursive(): Boolean {
    val functionName = name ?: return false
    return PsiTreeUtil.findChildrenOfType(body, MoveFunctionCall::class.java)
        .any { it.functionName == functionName }
}

private fun MoveFunction.hasMultipleReturns(): Boolean {
    val returns = PsiTreeUtil.findChildrenOfType(body, MoveReturnStatement::class.java)
    return returns.size > 1
}

private fun MoveFunction.usesEarlyReturn(): Boolean {
    val returns = PsiTreeUtil.findChildrenOfType(body, MoveReturnStatement::class.java)
    if (returns.isEmpty()) return false
    
    val lastStatement = body?.statements?.lastOrNull()
    return returns.any { it != lastStatement && it.parent != lastStatement }
}

private val MoveFunction.body: MoveCodeBlock?
    get() = PsiTreeUtil.findChildOfType(this, MoveCodeBlock::class.java)

private val MoveFunction.parameters: List<MoveFunctionParameter>
    get() = PsiTreeUtil.getChildrenOfTypeAsList(this, MoveFunctionParameter::class.java)

private val MoveFunctionCall.arguments: List<MoveExpression>
    get() = PsiTreeUtil.getChildrenOfTypeAsList(this, MoveExpression::class.java)

private val MoveAssignment.lvalue: MoveExpression?
    get() = children.firstOrNull { it is MoveExpression } as? MoveExpression

private val MoveFunctionParameter.name: String?
    get() = when (this) {
        is MoveNamedElement -> getName()
        else -> children.firstOrNull { it.node.elementType == MoveTypes.IDENTIFIER }?.text
    }

private val MoveCodeBlock.statements: List<com.suimove.intellij.psi.MoveStatement>
    get() = PsiTreeUtil.getChildrenOfTypeAsList(this, com.suimove.intellij.psi.MoveStatement::class.java)

// Temporary interface definitions - these should be moved to proper PSI files
interface MoveReturnStatement : com.suimove.intellij.psi.MoveStatement {
    val expression: MoveExpression?
}

interface MoveCodeBlock : PsiElement {
    val statements: List<com.suimove.intellij.psi.MoveStatement>
}

interface MoveFunctionParameter : MoveNamedElement

interface MoveFunctionCall : MoveExpression {
    val functionName: String?
    val arguments: List<MoveExpression>
}

interface MoveAssignment : PsiElement {
    val target: MoveExpression?
    val value: MoveExpression?
}
