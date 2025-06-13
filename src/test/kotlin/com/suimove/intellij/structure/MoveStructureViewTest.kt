package com.suimove.intellij.structure

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.ui.tree.TreeVisitor
import com.intellij.util.ui.tree.TreeUtil
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

class MoveStructureViewTest : BasePlatformTestCase() {
    
    fun testModuleStructure() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                fun function1() {}
                fun function2() {}
                
                struct Struct1 {
                    field1: u64,
                    field2: bool
                }
                
                struct Struct2 {}
            }
        """.trimIndent())
        
        val structureViewComponent = myFixture.getStructureViewTreeElement(file)
        
        // Test module node
        assertNotNull("Should have structure view", structureViewComponent)
        assertEquals("Root should be file", "test.move", structureViewComponent.presentation.presentableText)
        
        // Test children
        val children = structureViewComponent.children
        assertEquals("Should have 1 module", 1, children.size)
        
        val moduleNode = children[0]
        assertEquals("Should have module name", "0x1::test", moduleNode.presentation.presentableText)
        
        // Test module children
        val moduleChildren = moduleNode.children
        assertEquals("Module should have 4 children", 4, moduleChildren.size)
        
        // Count functions and structs
        val functions = moduleChildren.filter { it.presentation.presentableText?.contains("function") == true }
        val structs = moduleChildren.filter { it.presentation.presentableText?.contains("Struct") == true }
        
        assertEquals("Should have 2 functions", 2, functions.size)
        assertEquals("Should have 2 structs", 2, structs.size)
    }
    
    fun testStructFields() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                struct MyStruct {
                    field1: u64,
                    field2: bool,
                    field3: address
                }
            }
        """.trimIndent())
        
        val structureViewComponent = myFixture.getStructureViewTreeElement(file)
        val moduleNode = structureViewComponent.children[0]
        val structNode = moduleNode.children[0]
        
        // Test struct fields
        val structFields = structNode.children
        assertEquals("Struct should have 3 fields", 3, structFields.size)
        
        // Verify field names and types
        val fieldNames = structFields.map { it.presentation.presentableText }
        assertTrue("Should have field1", fieldNames.contains("field1: u64"))
        assertTrue("Should have field2", fieldNames.contains("field2: bool"))
        assertTrue("Should have field3", fieldNames.contains("field3: address"))
    }
    
    fun testFunctionParameters() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                fun complex_function(
                    param1: u64,
                    param2: bool,
                    param3: address
                ): u64 {
                    param1
                }
            }
        """.trimIndent())
        
        val structureViewComponent = myFixture.getStructureViewTreeElement(file)
        val moduleNode = structureViewComponent.children[0]
        val functionNode = moduleNode.children[0]
        
        // Verify function presentation includes parameters and return type
        val functionText = functionNode.presentation.presentableText
        assertNotNull("Function should have presentation text", functionText)
        assertTrue("Function should show name", functionText!!.contains("complex_function"))
        assertTrue("Function should show return type", functionText.contains(": u64"))
    }
    
    fun testMultipleModules() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::module1 {
                fun function1() {}
            }
            
            module 0x1::module2 {
                fun function2() {}
            }
        """.trimIndent())
        
        val structureViewComponent = myFixture.getStructureViewTreeElement(file)
        val modules = structureViewComponent.children
        
        assertEquals("Should have 2 modules", 2, modules.size)
        assertEquals("First module should be module1", "0x1::module1", modules[0].presentation.presentableText)
        assertEquals("Second module should be module2", "0x1::module2", modules[1].presentation.presentableText)
    }
    
    fun testConstants() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                const MAX_VALUE: u64 = 100;
                const FLAG: bool = true;
            }
        """.trimIndent())
        
        val structureViewComponent = myFixture.getStructureViewTreeElement(file)
        val moduleNode = structureViewComponent.children[0]
        val constants = moduleNode.children
        
        assertEquals("Should have 2 constants", 2, constants.size)
        
        val constantNames = constants.map { it.presentation.presentableText }
        assertTrue("Should have MAX_VALUE", constantNames.contains("MAX_VALUE: u64"))
        assertTrue("Should have FLAG", constantNames.contains("FLAG: bool"))
    }
    
    fun testStructureNavigation() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                fun function1() {}
                
                struct MyStruct {
                    field1: u64
                }
            }
        """.trimIndent())
        
        // Open structure view
        val structureView = myFixture.createStructureView(file)
        try {
            val tree = structureView.tree
            
            // Navigate to function node
            val functionPath = arrayOf("test.move", "0x1::test", "function1()")
            val functionNode = findNode(tree, functionPath)
            assertNotNull("Should find function node", functionNode)
            
            // Navigate to struct node
            val structPath = arrayOf("test.move", "0x1::test", "MyStruct")
            val structNode = findNode(tree, structPath)
            assertNotNull("Should find struct node", structNode)
            
            // Navigate to field node
            val fieldPath = arrayOf("test.move", "0x1::test", "MyStruct", "field1: u64")
            val fieldNode = findNode(tree, fieldPath)
            assertNotNull("Should find field node", fieldNode)
        } finally {
            structureView.dispose()
        }
    }
    
    private fun findNode(tree: JTree, path: Array<String>): DefaultMutableTreeNode? {
        var result: DefaultMutableTreeNode? = null
        
        TreeUtil.visitVisibleRows(tree, object : TreeVisitor {
            private var depth = 0
            
            override fun visit(node: Any): TreeVisitor.Action {
                if (node is DefaultMutableTreeNode) {
                    val userObject = node.userObject
                    val text = when (userObject) {
                        is MoveStructureViewElement -> userObject.presentation.presentableText
                        else -> userObject.toString()
                    }
                    
                    if (depth < path.size && text == path[depth]) {
                        depth++
                        if (depth == path.size) {
                            result = node
                            return TreeVisitor.Action.INTERRUPT
                        }
                        return TreeVisitor.Action.CONTINUE
                    }
                }
                return TreeVisitor.Action.SKIP_CHILDREN
            }
        })
        
        return result
    }
}
