package com.suimove.intellij.highlighting

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.suimove.intellij.lexer.MoveLexerAdapter
import com.suimove.intellij.psi.MoveTypes

class MoveSyntaxHighlighter : SyntaxHighlighterBase() {
    companion object {
        private val KEYWORDS = arrayOf(
            MoveTypes.MODULE, MoveTypes.SCRIPT, MoveTypes.FUN, MoveTypes.PUBLIC,
            MoveTypes.ENTRY, MoveTypes.NATIVE, MoveTypes.STRUCT, MoveTypes.HAS,
            MoveTypes.COPY, MoveTypes.DROP, MoveTypes.STORE, MoveTypes.KEY,
            MoveTypes.MOVE_KEYWORD, MoveTypes.CONST, MoveTypes.LET, MoveTypes.MUT,
            MoveTypes.RETURN, MoveTypes.ABORT, MoveTypes.BREAK, MoveTypes.CONTINUE,
            MoveTypes.IF, MoveTypes.ELSE, MoveTypes.WHILE, MoveTypes.LOOP,
            MoveTypes.SPEC, MoveTypes.PRAGMA, MoveTypes.INVARIANT, MoveTypes.ASSUME,
            MoveTypes.ASSERT, MoveTypes.REQUIRES, MoveTypes.ENSURES, MoveTypes.USE,
            MoveTypes.FRIEND, MoveTypes.ACQUIRES, MoveTypes.AS
        )

        private val TYPES = arrayOf(
            MoveTypes.U8, MoveTypes.U16, MoveTypes.U32, MoveTypes.U64,
            MoveTypes.U128, MoveTypes.U256, MoveTypes.BOOL, MoveTypes.ADDRESS,
            MoveTypes.SIGNER, MoveTypes.VECTOR
        )

        private val LITERALS = arrayOf(
            MoveTypes.TRUE, MoveTypes.FALSE, MoveTypes.HEX_NUMBER,
            MoveTypes.DEC_NUMBER, MoveTypes.ADDRESS_LITERAL
        )

        private val STRINGS = arrayOf(
            MoveTypes.STRING_LITERAL, MoveTypes.BYTE_STRING_LITERAL
        )

        private val COMMENTS = arrayOf(
            MoveTypes.LINE_COMMENT, MoveTypes.BLOCK_COMMENT
        )

        // Text attributes
        val KEYWORD = TextAttributesKey.createTextAttributesKey("MOVE_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        val TYPE = TextAttributesKey.createTextAttributesKey("MOVE_TYPE", DefaultLanguageHighlighterColors.CLASS_NAME)
        val NUMBER = TextAttributesKey.createTextAttributesKey("MOVE_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
        val STRING = TextAttributesKey.createTextAttributesKey("MOVE_STRING", DefaultLanguageHighlighterColors.STRING)
        val COMMENT = TextAttributesKey.createTextAttributesKey("MOVE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
        val IDENTIFIER = TextAttributesKey.createTextAttributesKey("MOVE_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER)
        val BAD_CHARACTER = TextAttributesKey.createTextAttributesKey("MOVE_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER)

        private val KEYWORD_KEYS = arrayOf(KEYWORD)
        private val TYPE_KEYS = arrayOf(TYPE)
        private val NUMBER_KEYS = arrayOf(NUMBER)
        private val STRING_KEYS = arrayOf(STRING)
        private val COMMENT_KEYS = arrayOf(COMMENT)
        private val IDENTIFIER_KEYS = arrayOf(IDENTIFIER)
        private val BAD_CHAR_KEYS = arrayOf(BAD_CHARACTER)
        private val EMPTY_KEYS = arrayOf<TextAttributesKey>()
    }

    override fun getHighlightingLexer(): Lexer = MoveLexerAdapter.create()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        return when {
            tokenType in KEYWORDS -> KEYWORD_KEYS
            tokenType in TYPES -> TYPE_KEYS
            tokenType in LITERALS -> NUMBER_KEYS
            tokenType in STRINGS -> STRING_KEYS
            tokenType in COMMENTS -> COMMENT_KEYS
            tokenType == MoveTypes.IDENTIFIER -> IDENTIFIER_KEYS
            tokenType == TokenType.BAD_CHARACTER -> BAD_CHAR_KEYS
            else -> EMPTY_KEYS
        }
    }
}
