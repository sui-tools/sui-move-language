package com.suimove.intellij.debugger

import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.evaluation.EvaluationMode
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider
import com.suimove.intellij.MoveFileType
import com.suimove.intellij.MoveLanguage

/**
 * Editors provider for Move debugger.
 */
class MoveDebuggerEditorsProvider : XDebuggerEditorsProvider() {
    
    override fun getFileType(): FileType = MoveFileType
    
    override fun createDocument(
        project: Project,
        expression: String,
        sourcePosition: XSourcePosition?,
        mode: EvaluationMode
    ): Document {
        val psiFile = createExpressionPsiFile(project, expression)
        return PsiDocumentManager.getInstance(project).getDocument(psiFile)!!
    }
    
    private fun createExpressionPsiFile(project: Project, expression: String): PsiFile {
        return PsiFileFactory.getInstance(project).createFileFromText(
            "debug_expression.move",
            MoveFileType,
            expression
        )
    }
}

/**
 * Code fragment for Move expressions in debugger.
 */
class MoveExpressionCodeFragment(
    project: Project,
    name: String,
    text: String,
    isPhysical: Boolean
) : PsiFile by PsiFileFactory.getInstance(project)
    .createFileFromText(
        name,
        MoveLanguage,
        text,
        isPhysical,
        false
    ) {
    
    fun getExpression(): PsiElement? {
        // Return the main expression element
        return firstChild
    }
}
