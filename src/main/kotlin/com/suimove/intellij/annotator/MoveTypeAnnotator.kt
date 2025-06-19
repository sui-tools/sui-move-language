package com.suimove.intellij.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.suimove.intellij.psi.*
import com.suimove.intellij.services.type.*
import com.suimove.intellij.highlighting.MoveHighlightingColors
import com.suimove.intellij.highlighting.MoveSyntaxHighlighter

/**
 * Annotator that provides type information and type error highlighting.
 */
class MoveTypeAnnotator : Annotator {
    
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val typeEngine = MoveTypeInferenceEngine(element.project)
        
        when (element) {
            is MoveCallExpression -> annotateCallExpression(element, holder, typeEngine)
            is MoveStructLiteralExpression -> annotateStructLiteral(element, holder, typeEngine)
            is MoveTypeParameterDecl -> annotateTypeParameter(element, holder, typeEngine)
            is MoveBinaryExpression -> annotateBinaryExpression(element, holder, typeEngine)
            is MoveVariable -> annotateVariable(element, holder, typeEngine)
            is MoveConstant -> annotateConstant(element, holder, typeEngine)
        }
    }
    
    private fun annotateCallExpression(
        element: MoveCallExpression,
        holder: AnnotationHolder,
        typeEngine: MoveTypeInferenceEngine
    ) {
        val function = element.reference?.resolve() as? MoveFunction ?: return
        val args = element.arguments
        val params = function.parameters
        
        // Check argument count
        if (args.size != params.size) {
            holder.newAnnotation(
                HighlightSeverity.ERROR,
                "Expected ${params.size} arguments, but got ${args.size}"
            )
                .range(element)
                .create()
            return
        }
        
        // Check argument types
        for ((index, arg) in args.withIndex()) {
            val argType = typeEngine.inferType(arg)
            val paramType = params.getOrNull(index)?.let { param ->
                param.type?.let { type -> typeEngine.inferType(type) }
            }
            
            if (argType != null && paramType != null && !argType.isAssignableTo(paramType)) {
                holder.newAnnotation(
                    HighlightSeverity.ERROR,
                    "Type mismatch: expected ${paramType.displayName()}, but got ${argType.displayName()}"
                )
                    .range(arg)
                    .create()
            }
        }
    }
    
    private fun annotateStructLiteral(
        element: MoveStructLiteralExpression,
        holder: AnnotationHolder,
        typeEngine: MoveTypeInferenceEngine
    ) {
        // Try to find the struct path in the literal
        val structName = element.text.substringBefore("{").trim().substringAfterLast("::")
        
        // Get struct fields from the literal
        val providedFields = element.children.filter { 
            it.text.contains(":") && it.textRange.startOffset > element.text.indexOf("{")
        }
        
        // Check for missing fields - simplified for now
        if (providedFields.isEmpty() && element.text.contains("{}")) {
            holder.newAnnotation(
                HighlightSeverity.WARNING,
                "Empty struct literal"
            )
                .range(element)
                .create()
        }
        
        // Check field types
        for (field in providedFields) {
            val fieldText = field.text.trim()
            if (fieldText.contains(":")) {
                val fieldName = fieldText.substringBefore(":").trim()
                val fieldValue = fieldText.substringAfter(":").trim()
                
                // Basic validation
                if (fieldName.isEmpty() || fieldValue.isEmpty()) {
                    holder.newAnnotation(
                        HighlightSeverity.ERROR,
                        "Invalid field syntax"
                    )
                        .range(field)
                        .create()
                }
            }
        }
    }
    
    private fun annotateTypeParameter(
        element: MoveTypeParameterDecl,
        holder: AnnotationHolder,
        typeEngine: MoveTypeInferenceEngine
    ) {
        element.nameIdentifier?.let { nameId ->
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(nameId.textRange)
                .textAttributes(MoveHighlightingColors.TYPE_PARAMETER)
                .create()
        }
        
        // Check if we're in a type parameter declaration
        element.nameIdentifier?.let { nameId ->
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(nameId.textRange)
                .textAttributes(MoveHighlightingColors.TYPE_PARAMETER)
                .create()
        }
    }
    
    private fun annotateBinaryExpression(
        expr: MoveBinaryExpression,
        holder: AnnotationHolder,
        typeEngine: MoveTypeInferenceEngine
    ) {
        val leftType = typeEngine.inferType(expr.left)
        val rightType = typeEngine.inferType(expr.right)
        
        if (leftType == null || rightType == null) return
        
        // Check type compatibility
        when (expr.operator) {
            "+", "-", "*", "/", "%" -> {
                if (!leftType.isNumericType() || !rightType.isNumericType()) {
                    holder.newAnnotation(
                        HighlightSeverity.ERROR,
                        "Arithmetic operations require numeric types"
                    )
                        .range(expr.operatorToken ?: expr)
                        .create()
                } else if (leftType != rightType) {
                    holder.newAnnotation(
                        HighlightSeverity.ERROR,
                        "Type mismatch: ${leftType.displayName()} and ${rightType.displayName()}"
                    )
                        .range(expr.operatorToken ?: expr)
                        .create()
                }
            }
            "==", "!=" -> {
                if (leftType != rightType) {
                    holder.newAnnotation(
                        HighlightSeverity.ERROR,
                        "Cannot compare different types: ${leftType.displayName()} and ${rightType.displayName()}"
                    )
                        .range(expr.operatorToken ?: expr)
                        .create()
                }
            }
            "&&", "||" -> {
                if (leftType != MoveBuiltinType.BOOL || rightType != MoveBuiltinType.BOOL) {
                    holder.newAnnotation(
                        HighlightSeverity.ERROR,
                        "Logical operations require boolean types"
                    )
                        .range(expr.operatorToken ?: expr)
                        .create()
                }
            }
        }
    }
    
    private fun annotateAssignment(
        element: PsiElement,
        holder: AnnotationHolder,
        typeEngine: MoveTypeInferenceEngine
    ) {
        // TODO: Implement assignment annotation when PSI is ready
    }
    
    private fun annotateReturnStatement(
        element: PsiElement,
        holder: AnnotationHolder,
        typeEngine: MoveTypeInferenceEngine
    ) {
        // TODO: Implement return statement annotation when PSI is ready
    }
    
    private fun annotateVariable(
        variable: MoveVariable,
        holder: AnnotationHolder,
        typeEngine: MoveTypeInferenceEngine
    ) {
        // TODO: Implement variable annotation
    }
    
    private fun annotateConstant(
        constant: MoveConstant,
        holder: AnnotationHolder,
        typeEngine: MoveTypeInferenceEngine
    ) {
        // TODO: Implement constant annotation
    }
}
