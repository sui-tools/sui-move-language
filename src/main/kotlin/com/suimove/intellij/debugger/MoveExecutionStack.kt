package com.suimove.intellij.debugger

import com.intellij.ui.ColoredTextContainer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.xdebugger.frame.XExecutionStack
import com.intellij.xdebugger.frame.XStackFrame
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator
import com.suimove.intellij.MoveIcons
import javax.swing.Icon

/**
 * Execution stack for Move debugging.
 */
class MoveExecutionStack(
    displayName: String,
    private val debugProcess: MoveDebugProcess,
    private val frames: List<MoveStackFrame>
) : XExecutionStack(displayName) {
    
    private var topFrame: MoveXStackFrame? = null
    
    init {
        if (frames.isNotEmpty()) {
            topFrame = MoveXStackFrame(debugProcess, frames.first(), frames)
        }
    }
    
    override fun getTopFrame(): XStackFrame? = topFrame
    
    override fun computeStackFrames(firstFrameIndex: Int, container: XStackFrameContainer) {
        val stackFrames = frames.drop(firstFrameIndex).map { frame ->
            MoveXStackFrame(debugProcess, frame, frames)
        }
        
        container.addStackFrames(stackFrames, true)
    }
}

/**
 * Stack frame for Move debugging.
 */
class MoveXStackFrame(
    private val debugProcess: MoveDebugProcess,
    private val frame: MoveStackFrame,
    private val allFrames: List<MoveStackFrame>
) : XStackFrame() {
    
    override fun getSourcePosition(): com.intellij.xdebugger.XSourcePosition? {
        val file = com.intellij.openapi.vfs.LocalFileSystem.getInstance()
            .findFileByPath(frame.file) ?: return null
        
        return com.intellij.xdebugger.impl.XSourcePositionImpl.create(file, frame.line - 1) // Convert to 0-based
    }
    
    override fun customizePresentation(component: ColoredTextContainer) {
        component.append(frame.functionName, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        component.append(" at ", SimpleTextAttributes.GRAYED_ATTRIBUTES)
        component.append("${frame.file}:${frame.line}", SimpleTextAttributes.GRAYED_ATTRIBUTES)
        component.setIcon(getFrameIcon())
    }
    
    override fun getEvaluator(): XDebuggerEvaluator? {
        return if (debugProcess.debugOptions.enableEvaluation) {
            MoveExpressionEvaluator(debugProcess, frame.id)
        } else {
            null
        }
    }
    
    override fun computeChildren(node: com.intellij.xdebugger.frame.XCompositeNode) {
        // Create variable groups
        val variableList = com.intellij.xdebugger.frame.XValueChildrenList()
        
        // Add local variables
        addLocalVariables(variableList)
        
        // Add parameters
        addParameters(variableList)
        
        // Add special values (e.g., return value)
        addSpecialValues(variableList)
        
        node.addChildren(variableList, true)
    }
    
    private fun addLocalVariables(list: com.intellij.xdebugger.frame.XValueChildrenList) {
        // Request local variables from debugger
        val locals = requestLocalVariables()
        
        locals.forEach { variable ->
            list.add(MoveXValue(variable.name, variable.value, variable.type, MoveIcons.VARIABLE))
        }
    }
    
    private fun addParameters(list: com.intellij.xdebugger.frame.XValueChildrenList) {
        // Request parameters from debugger
        val parameters = requestParameters()
        
        parameters.forEach { parameter ->
            list.add(MoveXValue(parameter.name, parameter.value, parameter.type, MoveIcons.PARAMETER))
        }
    }
    
    private fun addSpecialValues(list: com.intellij.xdebugger.frame.XValueChildrenList) {
        // Add special debugging values like return values
        val returnValue = requestReturnValue()
        if (returnValue != null) {
            list.add(MoveXValue("@return", returnValue.value, returnValue.type, MoveIcons.RETURN_VALUE))
        }
    }
    
    private fun requestLocalVariables(): List<MoveVariable> {
        // TODO: Implement communication with debugger to get local variables
        return emptyList()
    }
    
    private fun requestParameters(): List<MoveVariable> {
        // TODO: Implement communication with debugger to get parameters
        return emptyList()
    }
    
    private fun requestReturnValue(): MoveVariable? {
        // TODO: Implement communication with debugger to get return value
        return null
    }
    
    private fun getFrameIcon(): Icon {
        return when {
            frame == allFrames.firstOrNull() -> MoveIcons.CURRENT_FRAME
            else -> MoveIcons.STACK_FRAME
        }
    }
}

/**
 * Represents a variable in the debugger.
 */
data class MoveVariable(
    val name: String,
    val value: String,
    val type: String?
)
