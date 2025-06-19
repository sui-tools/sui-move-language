package com.suimove.intellij.debugger

import com.intellij.openapi.project.Project
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator
import com.intellij.xdebugger.frame.XValue
import com.intellij.xdebugger.frame.XValueNode
import com.intellij.xdebugger.frame.XValuePlace
import com.intellij.icons.AllIcons

/**
 * Expression evaluator for Move debugger.
 */
class MoveExpressionEvaluator(
    private val debugProcess: MoveDebugProcess,
    private val frameId: Int
) : XDebuggerEvaluator() {
    
    override fun evaluate(
        expression: String,
        callback: XDebuggerEvaluator.XEvaluationCallback,
        expressionPosition: XSourcePosition?
    ) {
        try {
            // Validate expression
            if (!isValidExpression(expression)) {
                callback.errorOccurred("Invalid expression: $expression")
                return
            }
            
            // Send evaluation command to debugger
            val command = DebugCommand.Evaluate(expression, frameId)
            debugProcess.debuggerConnection?.sendCommand(command)
            
            // Register callback to handle result
            debugProcess.registerEvaluationCallback(expression) { result ->
                when (result) {
                    is EvaluationResult.Success -> {
                        val value = MoveXValue(
                            expression,
                            result.value,
                            result.type,
                            AllIcons.Debugger.Value
                        )
                        callback.evaluated(value)
                    }
                    is EvaluationResult.Error -> {
                        callback.errorOccurred(result.message)
                    }
                }
            }
            
        } catch (e: Exception) {
            callback.errorOccurred("Evaluation failed: ${e.message}")
        }
    }
    
    override fun isCodeFragmentEvaluationSupported(): Boolean = true
    
    private fun isValidExpression(expression: String): Boolean {
        // Basic validation - can be enhanced
        return expression.isNotBlank() && 
               !expression.contains(";") && 
               !expression.contains("{") &&
               !expression.contains("}")
    }
}

/**
 * Result of expression evaluation.
 */
sealed class EvaluationResult {
    data class Success(
        val value: String,
        val type: String?
    ) : EvaluationResult()
    
    data class Error(
        val message: String
    ) : EvaluationResult()
}

/**
 * Extension to register evaluation callbacks.
 */
fun MoveDebugProcess.registerEvaluationCallback(
    expression: String,
    callback: (EvaluationResult) -> Unit
) {
    // This would be implemented in MoveDebugProcess to track pending evaluations
    // For now, we'll just simulate a response
    callback(EvaluationResult.Success("42", "u64"))
}

/**
 * Extension to access debugger connection.
 */
val MoveDebugProcess.debuggerConnection: MoveDebuggerConnection?
    get() = null // This would be properly implemented in MoveDebugProcess
