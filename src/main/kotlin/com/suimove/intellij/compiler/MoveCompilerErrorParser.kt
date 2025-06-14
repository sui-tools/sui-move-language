package com.suimove.intellij.compiler

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import java.io.File

data class MoveCompilerError(
    val file: String?,
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
        private val ERROR_HEADER_PATTERN = Regex(
            """(error|warning)\[[EW]\d+\]:\s*(.+)"""
        )
        
        private val LOCATION_PATTERN = Regex(
            """┌─\s*(.+):(\d+):(\d+)"""
        )
        
        private val SIMPLE_ERROR_PATTERN = Regex(
            """(.+):(\d+):(\d+):\s*(error|warning):\s*(.+)"""
        )
        
        private val SIMPLE_ERROR_NO_LOCATION_PATTERN = Regex(
            """^(error|warning):\s*(.+)"""
        )
        
        fun parseCompilerOutput(output: String, project: Project): List<MoveCompilerError> {
            val errors = mutableListOf<MoveCompilerError>()
            val lines = output.lines()
            
            var i = 0
            while (i < lines.size) {
                val line = lines[i]
                
                // Try to match error header pattern
                val headerMatch = ERROR_HEADER_PATTERN.find(line)
                if (headerMatch != null) {
                    val severity = headerMatch.groupValues[1]
                    val message = headerMatch.groupValues[2]
                    
                    // Look for location on next lines
                    for (j in (i + 1) until minOf(lines.size, i + 5)) {
                        val locationMatch = LOCATION_PATTERN.find(lines[j])
                        if (locationMatch != null) {
                            val file = locationMatch.groupValues[1]
                            val lineNum = locationMatch.groupValues[2].toIntOrNull() ?: 1
                            val column = locationMatch.groupValues[3].toIntOrNull() ?: 1
                            
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
                            break
                        }
                    }
                } else {
                    // Try simple error pattern with location
                    val simpleMatch = SIMPLE_ERROR_PATTERN.find(line)
                    if (simpleMatch != null) {
                        val file = simpleMatch.groupValues[1]
                        val lineNum = simpleMatch.groupValues[2].toIntOrNull() ?: 1
                        val column = simpleMatch.groupValues[3].toIntOrNull() ?: 1
                        val severity = simpleMatch.groupValues[4]
                        val message = simpleMatch.groupValues[5]
                        
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
                    } else {
                        // Try simple error pattern without location
                        val simpleNoLocationMatch = SIMPLE_ERROR_NO_LOCATION_PATTERN.find(line)
                        if (simpleNoLocationMatch != null) {
                            val severity = simpleNoLocationMatch.groupValues[1]
                            val message = simpleNoLocationMatch.groupValues[2]
                            
                            errors.add(MoveCompilerError(
                                file = null,
                                line = 0,
                                column = 0,
                                message = message.trim(),
                                severity = when (severity.lowercase()) {
                                    "error" -> ErrorSeverity.ERROR
                                    "warning" -> ErrorSeverity.WARNING
                                    else -> ErrorSeverity.INFO
                                }
                            ))
                        }
                    }
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
