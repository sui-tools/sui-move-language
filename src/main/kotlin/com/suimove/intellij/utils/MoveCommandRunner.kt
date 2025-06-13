package com.suimove.intellij.utils

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.suimove.intellij.settings.MoveSettings
import java.nio.charset.StandardCharsets

object MoveCommandRunner {
    fun runCommand(project: Project, command: String, args: List<String>) {
        val settings = MoveSettings.instance
        val suiPath = settings.suiCliPath.ifEmpty { "sui" }
        
        val commandLine = GeneralCommandLine()
            .withExePath(suiPath)
            .withParameters(command)
            .withParameters(args)
            .withCharset(StandardCharsets.UTF_8)
            .withWorkDirectory(project.basePath)
        
        try {
            val processHandler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine)
            ProcessTerminatedListener.attach(processHandler)
            
            showNotification(project, "Running: $suiPath $command ${args.joinToString(" ")}", NotificationType.INFORMATION)
            
            processHandler.startNotify()
        } catch (e: ExecutionException) {
            showNotification(project, "Failed to run command: ${e.message}", NotificationType.ERROR)
        }
    }
    
    private fun showNotification(project: Project, content: String, type: NotificationType) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Sui Move")
            .createNotification(content, type)
            .notify(project)
    }
}
