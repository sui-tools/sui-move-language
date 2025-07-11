package com.suimove.intellij.psi

import com.intellij.psi.tree.IElementType
import com.suimove.intellij.MoveLanguage

object MoveTypes {
    // Comments
    @JvmField val LINE_COMMENT = IElementType("LINE_COMMENT", MoveLanguage)
    @JvmField val BLOCK_COMMENT = IElementType("BLOCK_COMMENT", MoveLanguage)

    // Keywords
    @JvmField val MODULE = IElementType("MODULE", MoveLanguage)
    @JvmField val SCRIPT = IElementType("SCRIPT", MoveLanguage)
    @JvmField val FUN = IElementType("FUN", MoveLanguage)
    @JvmField val PUBLIC = IElementType("PUBLIC", MoveLanguage)
    @JvmField val ENTRY = IElementType("ENTRY", MoveLanguage)
    @JvmField val NATIVE = IElementType("NATIVE", MoveLanguage)
    @JvmField val STRUCT = IElementType("STRUCT", MoveLanguage)
    @JvmField val HAS = IElementType("HAS", MoveLanguage)
    @JvmField val COPY = IElementType("COPY", MoveLanguage)
    @JvmField val DROP = IElementType("DROP", MoveLanguage)
    @JvmField val STORE = IElementType("STORE", MoveLanguage)
    @JvmField val KEY = IElementType("KEY", MoveLanguage)
    @JvmField val MOVE_KEYWORD = IElementType("MOVE_KEYWORD", MoveLanguage)
    @JvmField val CONST = IElementType("CONST", MoveLanguage)
    @JvmField val LET = IElementType("LET", MoveLanguage)
    @JvmField val MUT = IElementType("MUT", MoveLanguage)
    @JvmField val RETURN = IElementType("RETURN", MoveLanguage)
    @JvmField val ABORT = IElementType("ABORT", MoveLanguage)
    @JvmField val BREAK = IElementType("BREAK", MoveLanguage)
    @JvmField val CONTINUE = IElementType("CONTINUE", MoveLanguage)
    @JvmField val IF = IElementType("IF", MoveLanguage)
    @JvmField val ELSE = IElementType("ELSE", MoveLanguage)
    @JvmField val WHILE = IElementType("WHILE", MoveLanguage)
    @JvmField val LOOP = IElementType("LOOP", MoveLanguage)
    @JvmField val SPEC = IElementType("SPEC", MoveLanguage)
    @JvmField val PRAGMA = IElementType("PRAGMA", MoveLanguage)
    @JvmField val INVARIANT = IElementType("INVARIANT", MoveLanguage)
    @JvmField val ASSUME = IElementType("ASSUME", MoveLanguage)
    @JvmField val ASSERT = IElementType("ASSERT", MoveLanguage)
    @JvmField val REQUIRES = IElementType("REQUIRES", MoveLanguage)
    @JvmField val ENSURES = IElementType("ENSURES", MoveLanguage)
    @JvmField val USE = IElementType("USE", MoveLanguage)
    @JvmField val FRIEND = IElementType("FRIEND", MoveLanguage)
    @JvmField val ACQUIRES = IElementType("ACQUIRES", MoveLanguage)
    @JvmField val AS = IElementType("AS", MoveLanguage)
    @JvmField val TRUE = IElementType("TRUE", MoveLanguage)
    @JvmField val FALSE = IElementType("FALSE", MoveLanguage)

    // Types
    @JvmField val U8 = IElementType("U8", MoveLanguage)
    @JvmField val U16 = IElementType("U16", MoveLanguage)
    @JvmField val U32 = IElementType("U32", MoveLanguage)
    @JvmField val U64 = IElementType("U64", MoveLanguage)
    @JvmField val U128 = IElementType("U128", MoveLanguage)
    @JvmField val U256 = IElementType("U256", MoveLanguage)
    @JvmField val BOOL = IElementType("BOOL", MoveLanguage)
    @JvmField val ADDRESS = IElementType("ADDRESS", MoveLanguage)
    @JvmField val SIGNER = IElementType("SIGNER", MoveLanguage)
    @JvmField val VECTOR = IElementType("VECTOR", MoveLanguage)

    // Operators and Punctuation
    @JvmField val LPAREN = IElementType("LPAREN", MoveLanguage)
    @JvmField val RPAREN = IElementType("RPAREN", MoveLanguage)
    @JvmField val LBRACK = IElementType("LBRACK", MoveLanguage)
    @JvmField val RBRACK = IElementType("RBRACK", MoveLanguage)
    @JvmField val LBRACE = IElementType("LBRACE", MoveLanguage)
    @JvmField val RBRACE = IElementType("RBRACE", MoveLanguage)
    @JvmField val LT = IElementType("LT", MoveLanguage)
    @JvmField val GT = IElementType("GT", MoveLanguage)
    @JvmField val LE = IElementType("LE", MoveLanguage)
    @JvmField val GE = IElementType("GE", MoveLanguage)
    @JvmField val EQ = IElementType("EQ", MoveLanguage)
    @JvmField val NE = IElementType("NE", MoveLanguage)
    @JvmField val ASSIGN = IElementType("ASSIGN", MoveLanguage)
    @JvmField val PLUS = IElementType("PLUS", MoveLanguage)
    @JvmField val MINUS = IElementType("MINUS", MoveLanguage)
    @JvmField val MUL = IElementType("MUL", MoveLanguage)
    @JvmField val DIV = IElementType("DIV", MoveLanguage)
    @JvmField val MOD = IElementType("MOD", MoveLanguage)
    @JvmField val AND = IElementType("AND", MoveLanguage)
    @JvmField val OR = IElementType("OR", MoveLanguage)
    @JvmField val XOR = IElementType("XOR", MoveLanguage)
    @JvmField val SHL = IElementType("SHL", MoveLanguage)
    @JvmField val SHR = IElementType("SHR", MoveLanguage)
    @JvmField val AND_AND = IElementType("AND_AND", MoveLanguage)
    @JvmField val OR_OR = IElementType("OR_OR", MoveLanguage)
    @JvmField val NOT = IElementType("NOT", MoveLanguage)
    @JvmField val COLON = IElementType("COLON", MoveLanguage)
    @JvmField val COLON_COLON = IElementType("COLON_COLON", MoveLanguage)
    @JvmField val SEMICOLON = IElementType("SEMICOLON", MoveLanguage)
    @JvmField val COMMA = IElementType("COMMA", MoveLanguage)
    @JvmField val DOT = IElementType("DOT", MoveLanguage)
    @JvmField val AMP_MUT = IElementType("AMP_MUT", MoveLanguage)

    // Literals
    @JvmField val ADDRESS_LITERAL = IElementType("ADDRESS_LITERAL", MoveLanguage)
    @JvmField val HEX_NUMBER = IElementType("HEX_NUMBER", MoveLanguage)
    @JvmField val DEC_NUMBER = IElementType("DEC_NUMBER", MoveLanguage)
    @JvmField val STRING_LITERAL = IElementType("STRING_LITERAL", MoveLanguage)
    @JvmField val BYTE_STRING_LITERAL = IElementType("BYTE_STRING_LITERAL", MoveLanguage)
    @JvmField val IDENTIFIER = IElementType("IDENTIFIER", MoveLanguage)
    
    // AST Node Types
    @JvmField val MODULE_DEFINITION = IElementType("MODULE_DEFINITION", MoveLanguage)
    @JvmField val SCRIPT_DEFINITION = IElementType("SCRIPT_DEFINITION", MoveLanguage)
    @JvmField val FUNCTION_DEFINITION = IElementType("FUNCTION_DEFINITION", MoveLanguage)
    @JvmField val STRUCT_DEFINITION = IElementType("STRUCT_DEFINITION", MoveLanguage)
    @JvmField val CONST_DEFINITION = IElementType("CONST_DEFINITION", MoveLanguage)
    @JvmField val USE_DECL = IElementType("USE_DECL", MoveLanguage)
    @JvmField val INTEGER_LITERAL = IElementType("INTEGER_LITERAL", MoveLanguage)
    @JvmField val HEX_LITERAL = IElementType("HEX_LITERAL", MoveLanguage)
    @JvmField val LET_BINDING = IElementType("LET_BINDING", MoveLanguage)
    @JvmField val EXPRESSION = IElementType("EXPRESSION", MoveLanguage)
    @JvmField val BLOCK = IElementType("BLOCK", MoveLanguage)
    @JvmField val FUNCTION_CALL = IElementType("FUNCTION_CALL", MoveLanguage)
    @JvmField val FUNCTION_CALL_ARGS = IElementType("FUNCTION_CALL_ARGS", MoveLanguage)
    @JvmField val BINARY_EXPR = IElementType("BINARY_EXPR", MoveLanguage)
    @JvmField val ASSIGNMENT = IElementType("ASSIGNMENT", MoveLanguage)
    @JvmField val TYPE_ANNOTATION = IElementType("TYPE_ANNOTATION", MoveLanguage)
    @JvmField val STRUCT_FIELD = IElementType("STRUCT_FIELD", MoveLanguage)
    @JvmField val ATTRIBUTE = IElementType("ATTRIBUTE", MoveLanguage)
    @JvmField val ATTRIBUTE_LIST = IElementType("ATTRIBUTE_LIST", MoveLanguage)
    @JvmField val ATTRIBUTE_ARG = IElementType("ATTRIBUTE_ARG", MoveLanguage)
    
    // Type-related node types
    @JvmField val TYPE_PARAMETER = IElementType("TYPE_PARAMETER", MoveLanguage)
    @JvmField val TYPE_PARAMETER_LIST = IElementType("TYPE_PARAMETER_LIST", MoveLanguage)
    @JvmField val TYPE_ARGUMENT = IElementType("TYPE_ARGUMENT", MoveLanguage)
    @JvmField val TYPE_ARGUMENT_LIST = IElementType("TYPE_ARGUMENT_LIST", MoveLanguage)
    
    // PSI Definition Node Types
    @JvmField val SPEC_BLOCK = IElementType("SPEC_BLOCK", MoveLanguage)
    @JvmField val INVARIANT_DECL = IElementType("INVARIANT_DECL", MoveLanguage)
    @JvmField val ABORT_DECL = IElementType("ABORT_DECL", MoveLanguage)
    @JvmField val LOOP_DECL = IElementType("LOOP_DECL", MoveLanguage)
    @JvmField val WHILE_DECL = IElementType("WHILE_DECL", MoveLanguage)
    @JvmField val IF_DECL = IElementType("IF_DECL", MoveLanguage)
    @JvmField val ELSE_DECL = IElementType("ELSE_DECL", MoveLanguage)
    @JvmField val RETURN_DECL = IElementType("RETURN_DECL", MoveLanguage)
    @JvmField val BREAK_DECL = IElementType("BREAK_DECL", MoveLanguage)
    @JvmField val CONTINUE_DECL = IElementType("CONTINUE_DECL", MoveLanguage)
    @JvmField val REQUIRES_DECL = IElementType("REQUIRES_DECL", MoveLanguage)
    @JvmField val ENSURES_DECL = IElementType("ENSURES_DECL", MoveLanguage)
    @JvmField val PRAGMA_DECL = IElementType("PRAGMA_DECL", MoveLanguage)
    
    object Factory {
        fun createElement(node: com.intellij.lang.ASTNode): com.intellij.psi.PsiElement {
            return when (node.elementType) {
                FUNCTION_DEFINITION -> com.suimove.intellij.psi.impl.MoveFunctionImpl(node)
                MODULE_DEFINITION -> com.suimove.intellij.psi.impl.MoveModuleImpl(node)
                STRUCT_DEFINITION -> com.suimove.intellij.psi.impl.MoveStructImpl(node)
                STRUCT_FIELD -> com.suimove.intellij.psi.impl.MoveStructFieldImpl(node)
                ATTRIBUTE -> com.suimove.intellij.psi.impl.MoveAttributeImpl(node)
                ATTRIBUTE_LIST -> com.suimove.intellij.psi.impl.MoveAttributeListImpl(node)
                ATTRIBUTE_ARG -> com.suimove.intellij.psi.impl.MoveAttributeArgumentImpl(node)
                else -> com.intellij.extapi.psi.ASTWrapperPsiElement(node)
            }
        }
    }
}
