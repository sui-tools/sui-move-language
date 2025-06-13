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
            """MODULE ("module")
              |WHITE_SPACE (" ")
              |SCRIPT ("script")
              |WHITE_SPACE (" ")
              |FUN ("fun")
              |WHITE_SPACE (" ")
              |STRUCT ("struct")
              |WHITE_SPACE (" ")
              |CONST ("const")
              |WHITE_SPACE (" ")
              |USE ("use")
              |WHITE_SPACE (" ")
              |PUBLIC ("public")
              |WHITE_SPACE (" ")
              |ENTRY ("entry")
              |WHITE_SPACE (" ")
              |HAS ("has")
              |WHITE_SPACE (" ")
              |COPY ("copy")
              |WHITE_SPACE (" ")
              |DROP ("drop")
              |WHITE_SPACE (" ")
              |STORE ("store")
              |WHITE_SPACE (" ")
              |KEY ("key")""".trimMargin()
        )
    }
    
    fun testLiterals() {
        doTest(
            """42 0xFF true false @0x1 b"hello"""",
            """INTEGER_LITERAL ("42")
              |WHITE_SPACE (" ")
              |HEX_LITERAL ("0xFF")
              |WHITE_SPACE (" ")
              |TRUE ("true")
              |WHITE_SPACE (" ")
              |FALSE ("false")
              |WHITE_SPACE (" ")
              |ADDRESS_LITERAL ("@0x1")
              |WHITE_SPACE (" ")
              |BYTE_STRING_LITERAL ("b"hello"")""".trimMargin()
        )
    }
    
    fun testOperators() {
        doTest(
            "+ - * / % == != < > <= >= && || !",
            """PLUS ("+")
              |WHITE_SPACE (" ")
              |MINUS ("-")
              |WHITE_SPACE (" ")
              |STAR ("*")
              |WHITE_SPACE (" ")
              |SLASH ("/")
              |WHITE_SPACE (" ")
              |PERCENT ("%")
              |WHITE_SPACE (" ")
              |EQ_EQ ("==")
              |WHITE_SPACE (" ")
              |BANG_EQ ("!=")
              |WHITE_SPACE (" ")
              |LT ("<")
              |WHITE_SPACE (" ")
              |GT (">")
              |WHITE_SPACE (" ")
              |LT_EQ ("<=")
              |WHITE_SPACE (" ")
              |GT_EQ (">=")
              |WHITE_SPACE (" ")
              |AMP_AMP ("&&")
              |WHITE_SPACE (" ")
              |PIPE_PIPE ("||")
              |WHITE_SPACE (" ")
              |BANG ("!")""".trimMargin()
        )
    }
    
    fun testComments() {
        doTest(
            """// line comment
              |/* block comment */
              |code""".trimMargin(),
            """LINE_COMMENT ("// line comment")
              |WHITE_SPACE ("\n")
              |BLOCK_COMMENT ("/* block comment */")
              |WHITE_SPACE ("\n")
              |IDENTIFIER ("code")""".trimMargin()
        )
    }
}
