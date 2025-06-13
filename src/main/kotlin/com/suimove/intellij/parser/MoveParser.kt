package com.suimove.intellij.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.LightPsiParser
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiBuilder.Marker
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType
import com.suimove.intellij.psi.MoveTypes

class MoveParser : PsiParser, LightPsiParser {
    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        return parseLight(root, builder)
    }

    override fun parseLight(root: IElementType, builder: PsiBuilder) {
        val rootMarker = builder.mark()
        parseFile(builder)
        rootMarker.done(root)
    }

    private fun parseFile(builder: PsiBuilder) {
        while (!builder.eof()) {
            if (!parseTopLevel(builder)) {
                builder.advanceLexer()
            }
        }
    }

    private fun parseTopLevel(builder: PsiBuilder): Boolean {
        return when (builder.tokenType) {
            MoveTypes.MODULE -> {
                parseModule(builder)
                true
            }
            MoveTypes.SCRIPT -> {
                parseScript(builder)
                true
            }
            MoveTypes.USE -> {
                parseUse(builder)
                true
            }
            else -> false
        }
    }

    private fun parseModule(builder: PsiBuilder) {
        val marker = builder.mark()
        builder.advanceLexer() // consume 'module'
        
        if (builder.tokenType == MoveTypes.IDENTIFIER) {
            builder.advanceLexer()
        }
        
        if (builder.tokenType == MoveTypes.LBRACE) {
            parseBlock(builder)
        }
        
        marker.done(MoveTypes.MODULE)
    }

    private fun parseScript(builder: PsiBuilder) {
        val marker = builder.mark()
        builder.advanceLexer() // consume 'script'
        
        if (builder.tokenType == MoveTypes.LBRACE) {
            parseBlock(builder)
        }
        
        marker.done(MoveTypes.SCRIPT)
    }

    private fun parseUse(builder: PsiBuilder) {
        val marker = builder.mark()
        builder.advanceLexer() // consume 'use'
        
        // Parse module path
        while (builder.tokenType == MoveTypes.IDENTIFIER || 
               builder.tokenType == MoveTypes.COLON_COLON) {
            builder.advanceLexer()
        }
        
        if (builder.tokenType == MoveTypes.SEMICOLON) {
            builder.advanceLexer()
        }
        
        marker.done(MoveTypes.USE)
    }

    private fun parseBlock(builder: PsiBuilder) {
        if (builder.tokenType != MoveTypes.LBRACE) return
        
        val marker = builder.mark()
        builder.advanceLexer() // consume '{'
        
        while (!builder.eof() && builder.tokenType != MoveTypes.RBRACE) {
            if (!parseStatement(builder)) {
                builder.advanceLexer()
            }
        }
        
        if (builder.tokenType == MoveTypes.RBRACE) {
            builder.advanceLexer()
        }
        
        marker.done(MoveTypes.LBRACE)
    }

    private fun parseStatement(builder: PsiBuilder): Boolean {
        return when (builder.tokenType) {
            MoveTypes.FUN -> {
                parseFunction(builder)
                true
            }
            MoveTypes.STRUCT -> {
                parseStruct(builder)
                true
            }
            MoveTypes.CONST -> {
                parseConst(builder)
                true
            }
            else -> false
        }
    }

    private fun parseFunction(builder: PsiBuilder) {
        val marker = builder.mark()
        
        // Parse modifiers
        while (builder.tokenType in listOf(MoveTypes.PUBLIC, MoveTypes.ENTRY, MoveTypes.NATIVE)) {
            builder.advanceLexer()
        }
        
        if (builder.tokenType == MoveTypes.FUN) {
            builder.advanceLexer()
        }
        
        if (builder.tokenType == MoveTypes.IDENTIFIER) {
            builder.advanceLexer()
        }
        
        // Parse parameters
        if (builder.tokenType == MoveTypes.LPAREN) {
            parseParameters(builder)
        }
        
        // Parse return type
        if (builder.tokenType == MoveTypes.COLON) {
            builder.advanceLexer()
            parseType(builder)
        }
        
        // Parse body
        if (builder.tokenType == MoveTypes.LBRACE) {
            parseBlock(builder)
        }
        
        marker.done(MoveTypes.FUN)
    }

    private fun parseStruct(builder: PsiBuilder) {
        val marker = builder.mark()
        builder.advanceLexer() // consume 'struct'
        
        if (builder.tokenType == MoveTypes.IDENTIFIER) {
            builder.advanceLexer()
        }
        
        // Parse abilities
        if (builder.tokenType == MoveTypes.HAS) {
            builder.advanceLexer()
            while (builder.tokenType in listOf(MoveTypes.COPY, MoveTypes.DROP, MoveTypes.STORE, MoveTypes.KEY)) {
                builder.advanceLexer()
                if (builder.tokenType == MoveTypes.COMMA) {
                    builder.advanceLexer()
                }
            }
        }
        
        if (builder.tokenType == MoveTypes.LBRACE) {
            parseBlock(builder)
        }
        
        marker.done(MoveTypes.STRUCT)
    }

    private fun parseConst(builder: PsiBuilder) {
        val marker = builder.mark()
        builder.advanceLexer() // consume 'const'
        
        if (builder.tokenType == MoveTypes.IDENTIFIER) {
            builder.advanceLexer()
        }
        
        if (builder.tokenType == MoveTypes.COLON) {
            builder.advanceLexer()
            parseType(builder)
        }
        
        if (builder.tokenType == MoveTypes.ASSIGN) {
            builder.advanceLexer()
            parseExpression(builder)
        }
        
        if (builder.tokenType == MoveTypes.SEMICOLON) {
            builder.advanceLexer()
        }
        
        marker.done(MoveTypes.CONST)
    }

    private fun parseParameters(builder: PsiBuilder) {
        if (builder.tokenType != MoveTypes.LPAREN) return
        
        builder.advanceLexer() // consume '('
        
        while (!builder.eof() && builder.tokenType != MoveTypes.RPAREN) {
            if (builder.tokenType == MoveTypes.IDENTIFIER) {
                builder.advanceLexer()
                if (builder.tokenType == MoveTypes.COLON) {
                    builder.advanceLexer()
                    parseType(builder)
                }
            }
            
            if (builder.tokenType == MoveTypes.COMMA) {
                builder.advanceLexer()
            } else if (builder.tokenType != MoveTypes.RPAREN) {
                builder.advanceLexer()
            }
        }
        
        if (builder.tokenType == MoveTypes.RPAREN) {
            builder.advanceLexer()
        }
    }

    private fun parseType(builder: PsiBuilder) {
        when (builder.tokenType) {
            in listOf(MoveTypes.U8, MoveTypes.U16, MoveTypes.U32, MoveTypes.U64,
                     MoveTypes.U128, MoveTypes.U256, MoveTypes.BOOL, MoveTypes.ADDRESS,
                     MoveTypes.SIGNER) -> {
                builder.advanceLexer()
            }
            MoveTypes.VECTOR -> {
                builder.advanceLexer()
                if (builder.tokenType == MoveTypes.LT) {
                    builder.advanceLexer()
                    parseType(builder)
                    if (builder.tokenType == MoveTypes.GT) {
                        builder.advanceLexer()
                    }
                }
            }
            MoveTypes.IDENTIFIER -> {
                builder.advanceLexer()
            }
            else -> builder.advanceLexer()
        }
    }

    private fun parseExpression(builder: PsiBuilder) {
        // Simple expression parsing - can be expanded
        when (builder.tokenType) {
            MoveTypes.DEC_NUMBER, MoveTypes.HEX_NUMBER,
            MoveTypes.STRING_LITERAL, MoveTypes.BYTE_STRING_LITERAL,
            MoveTypes.TRUE, MoveTypes.FALSE, MoveTypes.ADDRESS_LITERAL -> {
                builder.advanceLexer()
            }
            else -> {
                // Parse more complex expressions
                while (!builder.eof() && 
                       builder.tokenType != MoveTypes.SEMICOLON &&
                       builder.tokenType != MoveTypes.COMMA &&
                       builder.tokenType != MoveTypes.RPAREN) {
                    builder.advanceLexer()
                }
            }
        }
    }
}
