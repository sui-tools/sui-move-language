package com.suimove.intellij.lexer

import com.intellij.lexer.FlexAdapter
import com.intellij.lexer.Lexer

class MoveLexerAdapter : FlexAdapter(_MoveLexer(null)) {
    companion object {
        fun create(): Lexer = MoveLexerAdapter()
    }
}
