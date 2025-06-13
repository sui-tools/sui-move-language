package com.suimove.intellij.refactoring

import com.intellij.lang.refactoring.NamesValidator
import com.intellij.openapi.project.Project
import com.suimove.intellij.psi.MoveTypes

class MoveNamesValidator : NamesValidator {
    companion object {
        private val KEYWORDS = setOf(
            "abort", "acquires", "as", "break", "const", "continue", "copy", "else", "false", "fun",
            "friend", "if", "invariant", "let", "loop", "module", "move", "native", "public", "return",
            "script", "spec", "struct", "true", "use", "while", "entry", "has", "phantom"
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
