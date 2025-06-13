package com.suimove.intellij.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.suimove.intellij.compiler.MoveCompilerService

class MoveBuildAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val compilerService = project.service<MoveCompilerService>()
        
        compilerService.compileProject { success, errors ->
            // Errors are automatically displayed in the Problems view
            // Additional handling can be added here if needed
        }
    }
    
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}
