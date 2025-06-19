package com.suimove.intellij.completion

import com.intellij.codeInsight.template.postfix.templates.*
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.suimove.intellij.psi.MoveExpression

/**
 * Provides postfix templates for Move language.
 */
class MovePostfixTemplateProvider : PostfixTemplateProvider {
    
    override fun getTemplates(): Set<PostfixTemplate> {
        return setOf(
            MoveIfPostfixTemplate(),
            MoveLetPostfixTemplate(),
            MoveReturnPostfixTemplate(),
            MoveAbortPostfixTemplate(),
            MoveAssertPostfixTemplate(),
            MoveBorrowPostfixTemplate(),
            MoveBorrowMutPostfixTemplate(),
            MoveMovePostfixTemplate(),
            MoveCopyPostfixTemplate(),
            MoveVectorPostfixTemplate(),
            MoveOptionSomePostfixTemplate(),
            MoveLoopPostfixTemplate()
        )
    }
    
    override fun isTerminalSymbol(currentChar: Char): Boolean {
        return currentChar == '.' || currentChar == '!'
    }
    
    override fun afterExpand(file: PsiFile, editor: Editor) {
        // No special handling needed
    }
    
    override fun preCheck(copyFile: PsiFile, realEditor: Editor, currentOffset: Int): PsiFile {
        return copyFile
    }
    
    override fun preExpand(file: PsiFile, editor: Editor) {
        // No special handling needed
    }
}

/**
 * Base class for Move postfix templates.
 */
abstract class MovePostfixTemplate(
    name: String,
    example: String
) : PostfixTemplate(name, example) {
    
    override fun isApplicable(context: PsiElement, copyDocument: com.intellij.openapi.editor.Document, newOffset: Int): Boolean {
        return context is MoveExpression
    }
}

/**
 * .if postfix template: expr.if -> if (expr) { }
 */
class MoveIfPostfixTemplate : MovePostfixTemplate("if", "if (expr) { }") {
    override fun expand(context: PsiElement, editor: Editor) {
        val expression = context.text
        val document = editor.document
        val offset = context.textRange.startOffset
        
        // Replace expression with if statement
        document.replaceString(offset, context.textRange.endOffset, "if ($expression) {\n    \n}")
        
        // Position cursor inside the if block
        editor.caretModel.moveToOffset(offset + "if ($expression) {\n    ".length)
    }
}

/**
 * .let postfix template: expr.let -> let value = expr;
 */
class MoveLetPostfixTemplate : MovePostfixTemplate("let", "let value = expr") {
    override fun expand(context: PsiElement, editor: Editor) {
        val expression = context.text
        val document = editor.document
        val offset = context.textRange.startOffset
        
        // Replace expression with let statement
        document.replaceString(offset, context.textRange.endOffset, "let value = $expression")
        
        // Select "value" for easy renaming
        editor.caretModel.moveToOffset(offset + 4)
        editor.selectionModel.setSelection(offset + 4, offset + 9)
    }
}

/**
 * .return postfix template: expr.return -> return expr
 */
class MoveReturnPostfixTemplate : MovePostfixTemplate("return", "return expr") {
    override fun expand(context: PsiElement, editor: Editor) {
        val expression = context.text
        val document = editor.document
        val offset = context.textRange.startOffset
        
        document.replaceString(offset, context.textRange.endOffset, "return $expression")
    }
}

/**
 * .abort postfix template: expr.abort -> abort expr
 */
class MoveAbortPostfixTemplate : MovePostfixTemplate("abort", "abort expr") {
    override fun expand(context: PsiElement, editor: Editor) {
        val expression = context.text
        val document = editor.document
        val offset = context.textRange.startOffset
        
        document.replaceString(offset, context.textRange.endOffset, "abort $expression")
    }
}

/**
 * .assert postfix template: expr.assert -> assert!(expr, ERROR_CODE)
 */
class MoveAssertPostfixTemplate : MovePostfixTemplate("assert", "assert!(expr, ERROR_CODE)") {
    override fun expand(context: PsiElement, editor: Editor) {
        val expression = context.text
        val document = editor.document
        val offset = context.textRange.startOffset
        
        document.replaceString(offset, context.textRange.endOffset, "assert!($expression, ERROR_CODE)")
        
        // Select ERROR_CODE for easy replacement
        val errorCodeOffset = offset + "assert!($expression, ".length
        editor.caretModel.moveToOffset(errorCodeOffset)
        editor.selectionModel.setSelection(errorCodeOffset, errorCodeOffset + 10)
    }
}

/**
 * .borrow postfix template: expr.borrow -> &expr
 */
class MoveBorrowPostfixTemplate : MovePostfixTemplate("borrow", "&expr") {
    override fun expand(context: PsiElement, editor: Editor) {
        val expression = context.text
        val document = editor.document
        val offset = context.textRange.startOffset
        
        document.replaceString(offset, context.textRange.endOffset, "&$expression")
    }
}

/**
 * .borrow_mut postfix template: expr.borrow_mut -> &mut expr
 */
class MoveBorrowMutPostfixTemplate : MovePostfixTemplate("borrow_mut", "&mut expr") {
    override fun expand(context: PsiElement, editor: Editor) {
        val expression = context.text
        val document = editor.document
        val offset = context.textRange.startOffset
        
        document.replaceString(offset, context.textRange.endOffset, "&mut $expression")
    }
}

/**
 * .move postfix template: expr.move -> move expr
 */
class MoveMovePostfixTemplate : MovePostfixTemplate("move", "move expr") {
    override fun expand(context: PsiElement, editor: Editor) {
        val expression = context.text
        val document = editor.document
        val offset = context.textRange.startOffset
        
        document.replaceString(offset, context.textRange.endOffset, "move $expression")
    }
}

/**
 * .copy postfix template: expr.copy -> copy expr
 */
class MoveCopyPostfixTemplate : MovePostfixTemplate("copy", "copy expr") {
    override fun expand(context: PsiElement, editor: Editor) {
        val expression = context.text
        val document = editor.document
        val offset = context.textRange.startOffset
        
        document.replaceString(offset, context.textRange.endOffset, "copy $expression")
    }
}

/**
 * .vector postfix template: expr.vector -> vector[expr]
 */
class MoveVectorPostfixTemplate : MovePostfixTemplate("vector", "vector[expr]") {
    override fun expand(context: PsiElement, editor: Editor) {
        val expression = context.text
        val document = editor.document
        val offset = context.textRange.startOffset
        
        document.replaceString(offset, context.textRange.endOffset, "vector[$expression]")
    }
}

/**
 * .some postfix template: expr.some -> option::some(expr)
 */
class MoveOptionSomePostfixTemplate : MovePostfixTemplate("some", "option::some(expr)") {
    override fun expand(context: PsiElement, editor: Editor) {
        val expression = context.text
        val document = editor.document
        val offset = context.textRange.startOffset
        
        document.replaceString(offset, context.textRange.endOffset, "option::some($expression)")
    }
}

/**
 * .loop postfix template: expr.loop -> loop { expr }
 */
class MoveLoopPostfixTemplate : MovePostfixTemplate("loop", "loop { expr }") {
    override fun expand(context: PsiElement, editor: Editor) {
        val expression = context.text
        val document = editor.document
        val offset = context.textRange.startOffset
        
        document.replaceString(offset, context.textRange.endOffset, "loop {\n    $expression\n}")
        
        // Position cursor at the expression
        editor.caretModel.moveToOffset(offset + "loop {\n    ".length)
    }
}
