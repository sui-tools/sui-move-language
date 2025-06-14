package com.suimove.intellij.lexer

import com.intellij.lexer.Lexer
import com.intellij.testFramework.LexerTestCase
import com.suimove.intellij.psi.MoveTypes

class MoveLexerTest : LexerTestCase() {
    override fun createLexer(): Lexer = MoveLexerAdapter()
    
    override fun getDirPath(): String = ""
    
    fun testKeywords() {
        doTest(
            "module script fun struct const use public entry has copy drop store key",
            """MODULE ('module')
              |WHITE_SPACE (' ')
              |SCRIPT ('script')
              |WHITE_SPACE (' ')
              |FUN ('fun')
              |WHITE_SPACE (' ')
              |STRUCT ('struct')
              |WHITE_SPACE (' ')
              |CONST ('const')
              |WHITE_SPACE (' ')
              |USE ('use')
              |WHITE_SPACE (' ')
              |PUBLIC ('public')
              |WHITE_SPACE (' ')
              |ENTRY ('entry')
              |WHITE_SPACE (' ')
              |HAS ('has')
              |WHITE_SPACE (' ')
              |COPY ('copy')
              |WHITE_SPACE (' ')
              |DROP ('drop')
              |WHITE_SPACE (' ')
              |STORE ('store')
              |WHITE_SPACE (' ')
              |KEY ('key')""".trimMargin()
        )
    }
    
    fun testLiterals() {
        doTest(
            "123 0x1A true false @0x1 b\"hello\"",
            """DEC_NUMBER ('123')
              |WHITE_SPACE (' ')
              |HEX_NUMBER ('0x1A')
              |WHITE_SPACE (' ')
              |TRUE ('true')
              |WHITE_SPACE (' ')
              |FALSE ('false')
              |WHITE_SPACE (' ')
              |ADDRESS_LITERAL ('@0x1')
              |WHITE_SPACE (' ')
              |BYTE_STRING_LITERAL ('b"hello"')""".trimMargin()
        )
    }
    
    fun testOperators() {
        doTest(
            "+ - * / % == != < > <= >= && || ! & | ^ << >> = += -= *= /= %= &= |= ^=",
            """PLUS ('+')
              |WHITE_SPACE (' ')
              |MINUS ('-')
              |WHITE_SPACE (' ')
              |STAR ('*')
              |WHITE_SPACE (' ')
              |SLASH ('/')
              |WHITE_SPACE (' ')
              |PERCENT ('%')
              |WHITE_SPACE (' ')
              |EQ_EQ ('==')
              |WHITE_SPACE (' ')
              |EXCL_EQ ('!=')
              |WHITE_SPACE (' ')
              |LT ('<')
              |WHITE_SPACE (' ')
              |GT ('>')
              |WHITE_SPACE (' ')
              |LT_EQ ('<=')
              |WHITE_SPACE (' ')
              |GT_EQ ('>=')
              |WHITE_SPACE (' ')
              |AND_AND ('&&')
              |WHITE_SPACE (' ')
              |OR_OR ('||')
              |WHITE_SPACE (' ')
              |EXCL ('!')
              |WHITE_SPACE (' ')
              |AND ('&')
              |WHITE_SPACE (' ')
              |OR ('|')
              |WHITE_SPACE (' ')
              |XOR ('^')
              |WHITE_SPACE (' ')
              |LT_LT ('<<')
              |WHITE_SPACE (' ')
              |GT_GT ('>>')
              |WHITE_SPACE (' ')
              |EQ ('=')
              |WHITE_SPACE (' ')
              |PLUS_EQ ('+=')
              |WHITE_SPACE (' ')
              |MINUS_EQ ('-=')
              |WHITE_SPACE (' ')
              |STAR_EQ ('*=')
              |WHITE_SPACE (' ')
              |SLASH_EQ ('/=')
              |WHITE_SPACE (' ')
              |PERCENT_EQ ('%=')
              |WHITE_SPACE (' ')
              |AND_EQ ('&=')
              |WHITE_SPACE (' ')
              |OR_EQ ('|=')
              |WHITE_SPACE (' ')
              |XOR_EQ ('^=')""".trimMargin()
        )
    }
    
    fun testComments() {
        doTest(
            """// Single line comment
              |/* Multi-line
              |   comment */
              |module test {}""".trimMargin(),
            """LINE_COMMENT ('// Single line comment')
              |WHITE_SPACE ('\n')
              |BLOCK_COMMENT ('/* Multi-line\n   comment */')
              |WHITE_SPACE ('\n')
              |MODULE ('module')
              |WHITE_SPACE (' ')
              |IDENTIFIER ('test')
              |WHITE_SPACE (' ')
              |LBRACE ('{')
              |RBRACE ('}')""".trimMargin()
        )
    }
}
