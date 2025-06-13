package com.suimove.intellij.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.suimove.intellij.utils.MoveCommandRunner

class MoveDeployAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        MoveCommandRunner.runCommand(project, "client", listOf("publish", "--gas-budget", "100000000"))
    }
    
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}
