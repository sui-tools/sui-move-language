package com.suimove.intellij.refactoring

import com.intellij.lang.refactoring.NamesValidator
import com.intellij.openapi.project.Project
import com.suimove.intellij.psi.MoveTypes

class MoveNamesValidator : NamesValidator {
    companion object {
        private val KEYWORDS = setOf(
            // Control flow
            "abort", "break", "continue", "else", "if", "loop", "return", "while",
            // Declarations
            "const", "fun", "let", "module", "script", "struct", "use",
            // Modifiers
            "friend", "native", "public", "entry", "mut",
            // Operations
            "acquires", "as", "copy", "move",
            // Types
            "address", "bool", "signer", "u8", "u64", "u128", "vector",
            // Literals
            "false", "true",
            // Spec language
            "aborts_if", "apply", "assert", "assume", "emits", "ensures", "except",
            "exists", "forall", "global", "internal", "invariant", "local",
            "modifies", "old", "pragma", "requires", "spec", "to", "TRACE",
            // Abilities
            "has", "phantom"
        )
        
        private val IDENTIFIER_PATTERN = Regex("^[a-zA-Z_][a-zA-Z0-9_]*$")
    }
    
    override fun isKeyword(name: String, project: Project?): Boolean {
        return name in KEYWORDS
    }
    
    override fun isIdentifier(name: String, project: Project?): Boolean {
        return IDENTIFIER_PATTERN.matches(name) && !isKeyword(name, project)
    }
}
