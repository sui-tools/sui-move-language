package com.suimove.intellij.debugger

/**
 * Represents a stack frame in the Move debugger.
 */
data class MoveStackFrame(
    val id: Int = 0,
    val functionName: String,
    val file: String,
    val line: Int,
    val variables: Map<String, Any> = emptyMap()
)
