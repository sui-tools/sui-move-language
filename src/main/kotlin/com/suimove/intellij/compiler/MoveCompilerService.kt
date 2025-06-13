package com.suimove.intellij.compiler

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.suimove.intellij.settings.MoveSettings
import java.nio.charset.StandardCharsets

@Service(Service.Level.PROJECT)
class MoveCompilerService(private val project: Project) {
    
    fun compileProject(callback: (Boolean, List<MoveCompilerError>) -> Unit = { _, _ -> }) {
        // Save all documents before compiling
        ApplicationManager.getApplication().invokeAndWait {
            FileDocumentManager.getInstance().saveAllDocuments()
        }
        
        val settings = MoveSettings.instance
        val suiPath = settings.suiCliPath.ifEmpty { "sui" }
        
        val commandLine = GeneralCommandLine()
            .withExePath(suiPath)
            .withParameters("move", "build")
            .withCharset(StandardCharsets.UTF_8)
            .withWorkDirectory(project.basePath)
        
        val output = StringBuilder()
        val errorOutput = StringBuilder()
        
        try {
            val process = commandLine.createProcess()
            val processHandler = com.intellij.execution.process.OSProcessHandler(process, commandLine.commandLineString)
            
            processHandler.addProcessListener(object : ProcessAdapter() {
                override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                    val text = event.text
                    when (outputType) {
                        ProcessOutputTypes.STDOUT -> output.append(text)
                        ProcessOutputTypes.STDERR -> errorOutput.append(text)
                    }
                }
                
                override fun processTerminated(event: ProcessEvent) {
                    val fullOutput = output.toString() + errorOutput.toString()
                    val errors = MoveCompilerErrorParser.parseCompilerOutput(fullOutput, project)
                    
                    ApplicationManager.getApplication().invokeLater {
                        if (event.exitCode == 0) {
                            showNotification("Build completed successfully", NotificationType.INFORMATION)
                        } else {
                            showNotification("Build failed with ${errors.size} errors", NotificationType.ERROR)
                        }
                        callback(event.exitCode == 0, errors)
                    }
                }
            })
            
            processHandler.startNotify()
            
        } catch (e: Exception) {
            showNotification("Failed to run compiler: ${e.message}", NotificationType.ERROR)
            callback(false, emptyList())
        }
    }
    
    private fun showNotification(content: String, type: NotificationType) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Sui Move")
            .createNotification(content, type)
            .notify(project)
    }
}
