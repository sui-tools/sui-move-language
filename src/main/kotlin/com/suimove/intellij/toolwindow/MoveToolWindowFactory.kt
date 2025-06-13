package com.suimove.intellij.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class MoveToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val moveToolWindow = MoveToolWindow(project)
        val content = ContentFactory.getInstance().createContent(moveToolWindow.content, "", false)
        toolWindow.contentManager.addContent(content)
    }
}
