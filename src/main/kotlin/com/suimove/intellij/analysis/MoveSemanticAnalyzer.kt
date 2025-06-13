package com.suimove.intellij.analysis

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.suimove.intellij.psi.MoveFile
import com.suimove.intellij.psi.MoveTypes

class MoveSemanticAnalyzer {
    
    fun analyze(element: PsiElement, holder: AnnotationHolder) {
        when (element.node?.elementType) {
            MoveTypes.IDENTIFIER -> analyzeIdentifier(element, holder)
            MoveTypes.FUNCTION_CALL -> analyzeFunctionCall(element, holder)
            MoveTypes.BINARY_EXPR -> analyzeBinaryExpression(element, holder)
            MoveTypes.ASSIGNMENT -> analyzeAssignment(element, holder)
        }
    }
    
    private fun analyzeIdentifier(element: PsiElement, holder: AnnotationHolder) {
        val parent = element.parent
        if (parent?.node?.elementType == MoveTypes.FUNCTION_CALL) {
            // Check if function exists
            val functionName = element.text
            if (!isBuiltInFunction(functionName) && !isFunctionDefined(element, functionName)) {
                holder.newAnnotation(HighlightSeverity.ERROR, "Unresolved function: $functionName")
                    .range(element)
                    .create()
            }
        }
    }
    
    private fun analyzeFunctionCall(element: PsiElement, holder: AnnotationHolder) {
        val functionName = element.firstChild?.text ?: return
        val args = element.node?.findChildByType(MoveTypes.FUNCTION_CALL_ARGS)
        
        // Check argument count for built-in functions
        if (isBuiltInFunction(functionName)) {
            val expectedArgs = getBuiltInFunctionArgCount(functionName)
            val actualArgs = countArguments(args)
            
            if (expectedArgs != -1 && actualArgs != expectedArgs) {
                holder.newAnnotation(
                    HighlightSeverity.ERROR, 
                    "Function '$functionName' expects $expectedArgs arguments, but $actualArgs provided"
                ).range(element).create()
            }
        }
    }
    
    private fun analyzeBinaryExpression(element: PsiElement, holder: AnnotationHolder) {
        val left = element.firstChild
        val operator = left?.nextSibling
        val right = operator?.nextSibling
        
        if (left != null && right != null && operator != null) {
            val leftType = MoveTypeSystem.inferType(left)
            val rightType = MoveTypeSystem.inferType(right)
            
            // Type checking for binary operations
            when (operator.text) {
                "+", "-", "*", "/", "%" -> {
                    if (!isNumericType(leftType) || !isNumericType(rightType)) {
                        holder.newAnnotation(
                            HighlightSeverity.ERROR,
                            "Arithmetic operations require numeric types"
                        ).range(element).create()
                    } else if (leftType != rightType) {
                        holder.newAnnotation(
                            HighlightSeverity.WARNING,
                            "Type mismatch: $leftType and $rightType"
                        ).range(element).create()
                    }
                }
                "==", "!=" -> {
                    // Most types can be compared for equality
                }
                "<", ">", "<=", ">=" -> {
                    if (!isNumericType(leftType) || !isNumericType(rightType)) {
                        holder.newAnnotation(
                            HighlightSeverity.ERROR,
                            "Comparison operations require numeric types"
                        ).range(element).create()
                    }
                }
                "&&", "||" -> {
                    if (leftType != MoveType.Bool || rightType != MoveType.Bool) {
                        holder.newAnnotation(
                            HighlightSeverity.ERROR,
                            "Logical operations require boolean types"
                        ).range(element).create()
                    }
                }
            }
        }
    }
    
    private fun analyzeAssignment(element: PsiElement, holder: AnnotationHolder) {
        val lhs = element.firstChild
        val rhs = element.lastChild
        
        if (lhs != null && rhs != null) {
            val lhsType = MoveTypeSystem.inferType(lhs)
            val rhsType = MoveTypeSystem.inferType(rhs)
            
            if (!MoveTypeSystem.isAssignable(rhsType, lhsType)) {
                holder.newAnnotation(
                    HighlightSeverity.ERROR,
                    "Type mismatch: cannot assign $rhsType to $lhsType"
                ).range(element).create()
            }
        }
    }
    
    private fun isBuiltInFunction(name: String): Boolean {
        return name in listOf(
            "move_to", "move_from", "borrow_global", "borrow_global_mut",
            "exists", "freeze", "assert!", "abort"
        )
    }
    
    private fun getBuiltInFunctionArgCount(name: String): Int {
        return when (name) {
            "move_to" -> 2
            "move_from" -> 1
            "borrow_global", "borrow_global_mut" -> 1
            "exists" -> 1
            "freeze" -> 1
            "assert!" -> 2
            "abort" -> 1
            else -> -1
        }
    }
    
    private fun isFunctionDefined(element: PsiElement, functionName: String): Boolean {
        val file = element.containingFile as? MoveFile ?: return false
        
        // Search for function definitions
        val functions = PsiTreeUtil.findChildrenOfType(file, PsiElement::class.java)
            .filter { it.node?.elementType == MoveTypes.FUNCTION_DEFINITION }
        
        return functions.any { function ->
            function.node?.findChildByType(MoveTypes.IDENTIFIER)?.text == functionName
        }
    }
    
    private fun countArguments(argsNode: com.intellij.lang.ASTNode?): Int {
        if (argsNode == null) return 0
        
        var count = 0
        var child = argsNode.firstChildNode
        while (child != null) {
            if (child.elementType != MoveTypes.COMMA && 
                child.elementType != MoveTypes.LPAREN && 
                child.elementType != MoveTypes.RPAREN &&
                child.text.trim().isNotEmpty()) {
                count++
            }
            child = child.treeNext
        }
        return count
    }
    
    private fun isNumericType(type: MoveType): Boolean {
        return type in listOf(
            MoveType.U8, MoveType.U16, MoveType.U32, 
            MoveType.U64, MoveType.U128, MoveType.U256
        )
    }
}
