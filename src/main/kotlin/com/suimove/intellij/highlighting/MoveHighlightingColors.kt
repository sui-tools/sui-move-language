package com.suimove.intellij.highlighting

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey

/**
 * Highlighting colors for Move language elements
 */
object MoveHighlightingColors {
    val KEYWORD = TextAttributesKey.createTextAttributesKey("MOVE_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
    val IDENTIFIER = TextAttributesKey.createTextAttributesKey("MOVE_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER)
    val NUMBER = TextAttributesKey.createTextAttributesKey("MOVE_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
    val STRING = TextAttributesKey.createTextAttributesKey("MOVE_STRING", DefaultLanguageHighlighterColors.STRING)
    val COMMENT = TextAttributesKey.createTextAttributesKey("MOVE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
    val BLOCK_COMMENT = TextAttributesKey.createTextAttributesKey("MOVE_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT)
    
    // Type-related colors
    val TYPE_PARAMETER = TextAttributesKey.createTextAttributesKey("MOVE_TYPE_PARAMETER", DefaultLanguageHighlighterColors.CLASS_NAME)
    val TYPE_NAME = TextAttributesKey.createTextAttributesKey("MOVE_TYPE_NAME", DefaultLanguageHighlighterColors.CLASS_NAME)
    val PRIMITIVE_TYPE = TextAttributesKey.createTextAttributesKey("MOVE_PRIMITIVE_TYPE", DefaultLanguageHighlighterColors.KEYWORD)
    
    // Function-related colors
    val FUNCTION_NAME = TextAttributesKey.createTextAttributesKey("MOVE_FUNCTION_NAME", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
    val FUNCTION_CALL = TextAttributesKey.createTextAttributesKey("MOVE_FUNCTION_CALL", DefaultLanguageHighlighterColors.FUNCTION_CALL)
    val PARAMETER = TextAttributesKey.createTextAttributesKey("MOVE_PARAMETER", DefaultLanguageHighlighterColors.PARAMETER)
    
    // Struct-related colors
    val STRUCT_NAME = TextAttributesKey.createTextAttributesKey("MOVE_STRUCT_NAME", DefaultLanguageHighlighterColors.CLASS_NAME)
    val FIELD_NAME = TextAttributesKey.createTextAttributesKey("MOVE_FIELD_NAME", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
    
    // Module-related colors
    val MODULE_NAME = TextAttributesKey.createTextAttributesKey("MOVE_MODULE_NAME", DefaultLanguageHighlighterColors.CLASS_NAME)
    val ADDRESS = TextAttributesKey.createTextAttributesKey("MOVE_ADDRESS", DefaultLanguageHighlighterColors.NUMBER)
    
    // Other colors
    val CONSTANT = TextAttributesKey.createTextAttributesKey("MOVE_CONSTANT", DefaultLanguageHighlighterColors.CONSTANT)
    val ATTRIBUTE = TextAttributesKey.createTextAttributesKey("MOVE_ATTRIBUTE", DefaultLanguageHighlighterColors.METADATA)
    val OPERATOR = TextAttributesKey.createTextAttributesKey("MOVE_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
    val BRACES = TextAttributesKey.createTextAttributesKey("MOVE_BRACES", DefaultLanguageHighlighterColors.BRACES)
    val BRACKETS = TextAttributesKey.createTextAttributesKey("MOVE_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS)
    val PARENTHESES = TextAttributesKey.createTextAttributesKey("MOVE_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES)
    val SEMICOLON = TextAttributesKey.createTextAttributesKey("MOVE_SEMICOLON", DefaultLanguageHighlighterColors.SEMICOLON)
    val COMMA = TextAttributesKey.createTextAttributesKey("MOVE_COMMA", DefaultLanguageHighlighterColors.COMMA)
    val DOT = TextAttributesKey.createTextAttributesKey("MOVE_DOT", DefaultLanguageHighlighterColors.DOT)
}
