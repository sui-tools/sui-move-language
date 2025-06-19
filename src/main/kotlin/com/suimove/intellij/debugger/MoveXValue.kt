package com.suimove.intellij.debugger

import com.intellij.ui.ColoredTextContainer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.xdebugger.frame.XNamedValue
import com.intellij.xdebugger.frame.XValue
import com.intellij.xdebugger.frame.XValueModifier
import com.intellij.xdebugger.frame.XValueNode
import com.intellij.xdebugger.frame.XValuePlace
import com.intellij.xdebugger.frame.presentation.XValuePresentation
import com.intellij.icons.AllIcons
import javax.swing.Icon

/**
 * Represents a value in the Move debugger.
 */
class MoveXValue(
    name: String,
    private val value: String,
    private val type: String?,
    private val icon: Icon
) : XNamedValue(name) {
    
    override fun computePresentation(node: XValueNode, place: XValuePlace) {
        val presentation = MoveValuePresentation(value, type)
        node.setPresentation(icon, presentation, hasChildren())
    }
    
    override fun computeChildren(node: com.intellij.xdebugger.frame.XCompositeNode) {
        if (!hasChildren()) {
            super.computeChildren(node)
            return
        }
        
        // Compute children for complex types
        val children = when {
            isStruct() -> computeStructChildren()
            isVector() -> computeVectorChildren()
            isTuple() -> computeTupleChildren()
            else -> emptyList()
        }
        
        val list = com.intellij.xdebugger.frame.XValueChildrenList()
        children.forEach { child ->
            list.add(child)
        }
        
        node.addChildren(list, true)
    }
    
    override fun getModifier(): XValueModifier? {
        // Return a modifier if the value can be modified during debugging
        return if (canModify()) {
            MoveValueModifier(this)
        } else {
            null
        }
    }
    
    private fun hasChildren(): Boolean {
        return isStruct() || isVector() || isTuple()
    }
    
    private fun isStruct(): Boolean {
        return type?.contains("struct") == true || value.startsWith("{")
    }
    
    private fun isVector(): Boolean {
        return type?.startsWith("vector<") == true || value.startsWith("[")
    }
    
    private fun isTuple(): Boolean {
        return type?.startsWith("(") == true && type.endsWith(")")
    }
    
    private fun canModify(): Boolean {
        // Simple types can be modified
        return type in setOf("u8", "u64", "u128", "bool", "address")
    }
    
    private fun computeStructChildren(): List<MoveXValue> {
        // Parse struct fields from value
        // Format: { field1: value1, field2: value2 }
        if (!value.startsWith("{") || !value.endsWith("}")) {
            return emptyList()
        }
        
        val content = value.substring(1, value.length - 1)
        return parseFields(content).map { (fieldName, fieldValue) ->
            MoveXValue(fieldName, fieldValue, null, AllIcons.Nodes.Variable)
        }
    }
    
    private fun computeVectorChildren(): List<MoveXValue> {
        // Parse vector elements from value
        // Format: [element1, element2, element3]
        if (!value.startsWith("[") || !value.endsWith("]")) {
            return emptyList()
        }
        
        val content = value.substring(1, value.length - 1)
        return parseElements(content).mapIndexed { index, element ->
            MoveXValue("[$index]", element, null, AllIcons.Nodes.Variable)
        }
    }
    
    private fun computeTupleChildren(): List<MoveXValue> {
        // Parse tuple elements from value
        // Format: (element1, element2)
        if (!value.startsWith("(") || !value.endsWith(")")) {
            return emptyList()
        }
        
        val content = value.substring(1, value.length - 1)
        return parseElements(content).mapIndexed { index, element ->
            MoveXValue(".$index", element, null, AllIcons.Nodes.Variable)
        }
    }
    
    private fun parseFields(content: String): List<Pair<String, String>> {
        // Simple field parsing - can be enhanced
        return content.split(",").mapNotNull { field ->
            val parts = field.trim().split(":", limit = 2)
            if (parts.size == 2) {
                parts[0].trim() to parts[1].trim()
            } else {
                null
            }
        }
    }
    
    private fun parseElements(content: String): List<String> {
        // Simple element parsing - can be enhanced
        return content.split(",").map { it.trim() }
    }
}

/**
 * Value presentation for Move debugger.
 */
class MoveValuePresentation(
    private val value: String,
    private val type: String?
) : XValuePresentation() {
    
    override fun renderValue(renderer: XValueTextRenderer) {
        when {
            type == "bool" -> renderer.renderKeywordValue(value)
            type == "address" -> renderer.renderStringValue(value)
            type?.startsWith("u") == true -> renderer.renderNumericValue(value)
            value.startsWith("\"") -> renderer.renderStringValue(value)
            else -> renderer.renderValue(value)
        }
    }
    
    override fun getType(): String? = type
}

/**
 * Value modifier for Move debugger.
 */
class MoveValueModifier(
    private val xValue: MoveXValue
) : XValueModifier() {
    
    override fun getInitialValueEditorText(): String? {
        // Return the current value for editing
        return null // TODO: Extract raw value from xValue
    }
    
    override fun setValue(expression: String, callback: XModificationCallback) {
        // TODO: Send modification command to debugger
        callback.valueModified()
    }
}
