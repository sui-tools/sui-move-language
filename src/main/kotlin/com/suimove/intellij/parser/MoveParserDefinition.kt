package com.suimove.intellij.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import com.suimove.intellij.MoveLanguage
import com.suimove.intellij.lexer.MoveLexerAdapter
import com.suimove.intellij.psi.MoveFile
import com.suimove.intellij.psi.MoveTypes

class MoveParserDefinition : ParserDefinition {
    companion object {
        val FILE = IFileElementType(MoveLanguage)
        val COMMENTS = TokenSet.create(MoveTypes.LINE_COMMENT, MoveTypes.BLOCK_COMMENT)
        val STRINGS = TokenSet.create(MoveTypes.STRING_LITERAL, MoveTypes.BYTE_STRING_LITERAL)
    }

    override fun createLexer(project: Project): Lexer = MoveLexerAdapter.create()

    override fun createParser(project: Project): PsiParser = MoveParser()

    override fun getFileNodeType(): IFileElementType = FILE

    override fun getCommentTokens(): TokenSet = COMMENTS

    override fun getStringLiteralElements(): TokenSet = STRINGS

    override fun createElement(node: ASTNode): PsiElement = MoveTypes.Factory.createElement(node)

    override fun createFile(viewProvider: FileViewProvider): PsiFile = MoveFile(viewProvider)
}
