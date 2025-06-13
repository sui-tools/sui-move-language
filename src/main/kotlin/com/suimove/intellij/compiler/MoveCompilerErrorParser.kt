package com.suimove.intellij.compiler

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import java.io.File

data class MoveCompilerError(
    val file: String,
    val line: Int,
    val column: Int,
    val message: String,
    val severity: ErrorSeverity
)

enum class ErrorSeverity {
    ERROR,
    WARNING,
    INFO
}

class MoveCompilerErrorParser {
    companion object {
        private val ERROR_PATTERN = Regex(
            """(?:error|warning)\[E\d+\]:\s*(.+)\s*┌─\s*(.+):(\d+):(\d+)"""
        )
        
        private val SIMPLE_ERROR_PATTERN = Regex(
            """(.+):(\d+):(\d+):\s*(error|warning):\s*(.+)"""
        )
        
        fun parseCompilerOutput(output: String, project: Project): List<MoveCompilerError> {
            val errors = mutableListOf<MoveCompilerError>()
            val lines = output.lines()
            
            var i = 0
            while (i < lines.size) {
                val line = lines[i]
                
                // Try to match error pattern
                ERROR_PATTERN.find(line)?.let { match ->
                    val message = match.groupValues[1]
                    val file = match.groupValues[2]
                    val lineNum = match.groupValues[3].toIntOrNull() ?: 1
                    val column = match.groupValues[4].toIntOrNull() ?: 1
                    
                    errors.add(MoveCompilerError(
                        file = resolveFilePath(file, project),
                        line = lineNum,
                        column = column,
                        message = message.trim(),
                        severity = if (line.startsWith("error")) ErrorSeverity.ERROR else ErrorSeverity.WARNING
                    ))
                }
                
                // Try simple error pattern
                SIMPLE_ERROR_PATTERN.find(line)?.let { match ->
                    val file = match.groupValues[1]
                    val lineNum = match.groupValues[2].toIntOrNull() ?: 1
                    val column = match.groupValues[3].toIntOrNull() ?: 1
                    val severity = match.groupValues[4]
                    val message = match.groupValues[5]
                    
                    errors.add(MoveCompilerError(
                        file = resolveFilePath(file, project),
                        line = lineNum,
                        column = column,
                        message = message.trim(),
                        severity = when (severity.lowercase()) {
                            "error" -> ErrorSeverity.ERROR
                            "warning" -> ErrorSeverity.WARNING
                            else -> ErrorSeverity.INFO
                        }
                    ))
                }
                
                i++
            }
            
            return errors
        }
        
        private fun resolveFilePath(path: String, project: Project): String {
            // Try to resolve relative paths
            val projectPath = project.basePath ?: return path
            val file = File(projectPath, path)
            
            return if (file.exists()) {
                file.absolutePath
            } else {
                // Try to find the file in the project
                val virtualFile = VirtualFileManager.getInstance()
                    .findFileByUrl("file://$path")
                
                virtualFile?.path ?: path
            }
        }
    }
}
