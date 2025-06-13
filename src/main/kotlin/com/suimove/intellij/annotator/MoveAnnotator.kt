package com.suimove.intellij.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.psi.PsiElement
import com.suimove.intellij.psi.MoveTypes
import com.suimove.intellij.analysis.MoveSemanticAnalyzer

class MoveAnnotator : Annotator {
    private val semanticAnalyzer = MoveSemanticAnalyzer()
    
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        // Syntax-based annotations
        when (element.node?.elementType) {
            MoveTypes.ADDRESS_LITERAL -> {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(element)
                    .textAttributes(DefaultLanguageHighlighterColors.NUMBER)
                    .create()
            }
            MoveTypes.BYTE_STRING_LITERAL -> {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(element)
                    .textAttributes(DefaultLanguageHighlighterColors.STRING)
                    .create()
            }
            MoveTypes.IDENTIFIER -> {
                val parent = element.parent
                when (parent?.node?.elementType) {
                    MoveTypes.FUNCTION_DEFINITION -> {
                        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                            .range(element)
                            .textAttributes(DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
                            .create()
                    }
                    MoveTypes.STRUCT_DEFINITION -> {
                        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                            .range(element)
                            .textAttributes(DefaultLanguageHighlighterColors.CLASS_NAME)
                            .create()
                    }
                    MoveTypes.CONST_DEFINITION -> {
                        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                            .range(element)
                            .textAttributes(DefaultLanguageHighlighterColors.CONSTANT)
                            .create()
                    }
                }
            }
        }
        
        // Semantic analysis
        semanticAnalyzer.analyze(element, holder)
        
        // Additional validations
        validateAddressLiteral(element, holder)
        validateIntegerLiteral(element, holder)
    }
    
    private fun validateAddressLiteral(element: PsiElement, holder: AnnotationHolder) {
        if (element.node?.elementType == MoveTypes.ADDRESS_LITERAL) {
            val text = element.text
            if (!text.startsWith("0x") || text.length != 66) { // 0x + 64 hex chars
                holder.newAnnotation(
                    HighlightSeverity.ERROR,
                    "Invalid address format. Expected 0x followed by 64 hexadecimal characters"
                ).range(element).create()
            }
        }
    }
    
    private fun validateIntegerLiteral(element: PsiElement, holder: AnnotationHolder) {
        if (element.node?.elementType == MoveTypes.INTEGER_LITERAL) {
            val text = element.text
            val (numberPart, typeSuffix) = when {
                text.endsWith("u256") -> text.dropLast(4) to "u256"
                text.endsWith("u128") -> text.dropLast(4) to "u128"
                text.endsWith("u64") -> text.dropLast(3) to "u64"
                text.endsWith("u32") -> text.dropLast(3) to "u32"
                text.endsWith("u16") -> text.dropLast(3) to "u16"
                text.endsWith("u8") -> text.dropLast(2) to "u8"
                else -> text to "u64" // default
            }
            
            try {
                val value = numberPart.toBigInteger()
                val maxValue = when (typeSuffix) {
                    "u8" -> 255.toBigInteger()
                    "u16" -> 65535.toBigInteger()
                    "u32" -> 4294967295.toBigInteger()
                    "u64" -> "18446744073709551615".toBigInteger()
                    "u128" -> "340282366920938463463374607431768211455".toBigInteger()
                    "u256" -> "115792089237316195423570985008687907853269984665640564039457584007913129639935".toBigInteger()
                    else -> "18446744073709551615".toBigInteger()
                }
                
                if (value > maxValue) {
                    holder.newAnnotation(
                        HighlightSeverity.ERROR,
                        "Integer literal too large for type $typeSuffix"
                    ).range(element).create()
                }
            } catch (e: NumberFormatException) {
                holder.newAnnotation(
                    HighlightSeverity.ERROR,
                    "Invalid integer literal"
                ).range(element).create()
            }
        }
    }
}
