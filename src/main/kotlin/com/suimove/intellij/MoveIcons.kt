package com.suimove.intellij

import com.intellij.openapi.util.IconLoader
import com.intellij.icons.AllIcons
import javax.swing.Icon

object MoveIcons {
    @JvmField
    val FILE: Icon = IconLoader.getIcon("/icons/move.svg", MoveIcons::class.java)
    
    // Language element icons
    @JvmField
    val MODULE: Icon = IconLoader.getIcon("/icons/move-module.svg", MoveIcons::class.java)
    
    @JvmField
    val FUNCTION: Icon = IconLoader.getIcon("/icons/move-function.svg", MoveIcons::class.java)
    
    @JvmField
    val STRUCT: Icon = IconLoader.getIcon("/icons/move-struct.svg", MoveIcons::class.java)
    
    @JvmField
    val FIELD: Icon = AllIcons.Nodes.Field
    
    @JvmField
    val CONSTANT: Icon = AllIcons.Nodes.Constant
    
    @JvmField
    val VARIABLE: Icon = AllIcons.Nodes.Variable
    
    @JvmField
    val PARAMETER: Icon = AllIcons.Nodes.Parameter
    
    @JvmField
    val TYPE: Icon = AllIcons.Nodes.Type
    
    @JvmField
    val TYPE_PARAMETER: Icon = AllIcons.Nodes.Type
    
    @JvmField
    val TYPE_ALIAS: Icon = AllIcons.Nodes.Type
    
    @JvmField
    val KEYWORD: Icon = AllIcons.Nodes.Tag
    
    @JvmField
    val ABILITY: Icon = AllIcons.Nodes.Annotationtype
    
    @JvmField
    val ADDRESS: Icon = AllIcons.Nodes.Static
    
    @JvmField
    val USE_STATEMENT: Icon = IconLoader.getIcon("/icons/move-use.svg", MoveIcons::class.java)
    
    @JvmField
    val ENTRY_FUNCTION: Icon = IconLoader.getIcon("/icons/move-entry.svg", MoveIcons::class.java)
    
    @JvmField
    val TEST_FUNCTION: Icon = IconLoader.getIcon("/icons/move-test.svg", MoveIcons::class.java)
    
    @JvmField
    val TEST_FUNCTION_ERROR: Icon = AllIcons.RunConfigurations.TestError
    
    val PHANTOM = AllIcons.Nodes.Padlock
    
    // Sui-specific icons
    val SUI_OBJECT = IconLoader.getIcon("/icons/move-object.svg", MoveIcons::class.java)
    val TRANSFER = AllIcons.Actions.Share
    
    // Debugger icons
    val BREAKPOINT = AllIcons.Debugger.Db_set_breakpoint
    val BREAKPOINT_DISABLED = AllIcons.Debugger.Db_disabled_breakpoint
    val BREAKPOINT_MUTED = AllIcons.Debugger.Db_muted_breakpoint
    val BREAKPOINT_MUTED_DISABLED = AllIcons.Debugger.Db_muted_disabled_breakpoint
    val CURRENT_FRAME = AllIcons.Debugger.ThreadCurrent
    val STACK_FRAME = AllIcons.Debugger.Frame
    val RETURN_VALUE = AllIcons.Debugger.Db_watch
}
