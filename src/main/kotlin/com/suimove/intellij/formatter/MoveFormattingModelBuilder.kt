package com.suimove.intellij.formatter

import com.intellij.formatting.*
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.suimove.intellij.MoveLanguage

class MoveFormattingModelBuilder : FormattingModelBuilder {
    override fun createModel(formattingContext: FormattingContext): FormattingModel {
        val settings = formattingContext.codeStyleSettings
        val element = formattingContext.psiElement
        val block = MoveBlock(
            element.node,
            Wrap.createWrap(WrapType.NONE, false),
            Alignment.createAlignment(),
            createSpaceBuilder(settings)
        )
        
        return FormattingModelProvider.createFormattingModelForPsiFile(
            element.containingFile,
            block,
            settings
        )
    }
    
    private fun createSpaceBuilder(settings: CodeStyleSettings): SpacingBuilder {
        return SpacingBuilder(settings, MoveLanguage)
            .around(com.suimove.intellij.psi.MoveTypes.ASSIGN).spaceIf(true)
            .around(com.suimove.intellij.psi.MoveTypes.COLON).spaceIf(true)
            .after(com.suimove.intellij.psi.MoveTypes.COMMA).spaceIf(true)
            .before(com.suimove.intellij.psi.MoveTypes.SEMICOLON).spaceIf(false)
            .after(com.suimove.intellij.psi.MoveTypes.SEMICOLON).spaceIf(true)
            .between(com.suimove.intellij.psi.MoveTypes.LPAREN, com.suimove.intellij.psi.MoveTypes.RPAREN).spaceIf(false)
            .between(com.suimove.intellij.psi.MoveTypes.LBRACK, com.suimove.intellij.psi.MoveTypes.RBRACK).spaceIf(false)
            .after(com.suimove.intellij.psi.MoveTypes.LBRACE).lineBreakInCode()
            .before(com.suimove.intellij.psi.MoveTypes.RBRACE).lineBreakInCode()
    }
}
