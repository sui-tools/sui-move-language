package com.suimove.intellij.findusages

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.usages.UsageInfo2UsageAdapter
import com.suimove.intellij.psi.MoveElementFactory
import com.suimove.intellij.psi.MoveFile

class MoveFindUsagesProviderTest : BasePlatformTestCase() {
    
    private lateinit var findUsagesProvider: MoveFindUsagesProvider
    
    override fun setUp() {
        super.setUp()
        findUsagesProvider = MoveFindUsagesProvider()
    }
    
    fun testCanFindUsagesForFunction() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                fun test_function() {}
                
                fun caller() {
                    test_function();
                }
            }
        """.trimIndent()) as MoveFile
        
        val function = file.findElementAt(myFixture.editor.caretModel.offset)?.parent
        assertNotNull("Should find function element", function)
        assertTrue("Should be able to find usages for function", 
            findUsagesProvider.canFindUsagesFor(function!!))
    }
    
    fun testCanFindUsagesForStruct() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                struct MyStruct {}
                
                fun create(): MyStruct {
                    MyStruct {}
                }
            }
        """.trimIndent()) as MoveFile
        
        val struct = file.findElementAt(myFixture.editor.caretModel.offset)?.parent
        assertNotNull("Should find struct element", struct)
        assertTrue("Should be able to find usages for struct", 
            findUsagesProvider.canFindUsagesFor(struct!!))
    }
    
    fun testCanFindUsagesForVariable() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                fun test_function() {
                    let x = 42;
                    x + 1;
                }
            }
        """.trimIndent()) as MoveFile
        
        val variable = file.findElementAt(myFixture.editor.caretModel.offset)?.parent
        assertNotNull("Should find variable element", variable)
        assertTrue("Should be able to find usages for variable", 
            findUsagesProvider.canFindUsagesFor(variable!!))
    }
    
    fun testCanFindUsagesForModule() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                fun test_function() {}
            }
            
            module 0x1::other {
                use 0x1::test;
            }
        """.trimIndent()) as MoveFile
        
        val module = file.findElementAt(myFixture.editor.caretModel.offset)?.parent
        assertNotNull("Should find module element", module)
        assertTrue("Should be able to find usages for module", 
            findUsagesProvider.canFindUsagesFor(module!!))
    }
    
    fun testCannotFindUsagesForComment() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                // This is a comment
                fun test_function() {}
            }
        """.trimIndent()) as MoveFile
        
        val comment = file.findElementAt(myFixture.editor.caretModel.offset)
        assertNotNull("Should find comment element", comment)
        assertFalse("Should not be able to find usages for comment", 
            findUsagesProvider.canFindUsagesFor(comment!!))
    }
    
    fun testGetDescriptiveNameForFunction() {
        val function = MoveElementFactory.createFunctionDeclaration(project, "test_function")
        assertEquals("Should get function name", "test_function", 
            findUsagesProvider.getDescriptiveName(function))
    }
    
    fun testGetDescriptiveNameForStruct() {
        val struct = MoveElementFactory.createStructDeclaration(project, "MyStruct")
        assertEquals("Should get struct name", "MyStruct", 
            findUsagesProvider.getDescriptiveName(struct))
    }
    
    fun testGetDescriptiveNameForModule() {
        val module = MoveElementFactory.createModuleDeclaration(project, "0x1", "test_module")
        assertEquals("Should get module name", "test_module", 
            findUsagesProvider.getDescriptiveName(module))
    }
    
    fun testGetNodeTextForFunction() {
        val function = MoveElementFactory.createFunctionDeclaration(project, "test_function")
        assertNotNull("Should get node text for function", 
            findUsagesProvider.getNodeText(function, true))
        assertTrue("Node text should contain function name", 
            findUsagesProvider.getNodeText(function, true).contains("test_function"))
    }
    
    fun testFindUsagesForFunction() {
        myFixture.configureByFiles("UsageTest.move")
        
        val usages = myFixture.findUsages(myFixture.elementAtCaret)
        assertTrue("Should find usages for function", usages.isNotEmpty())
        
        // Check usage details
        val usage = usages.first() as UsageInfo2UsageAdapter
        assertNotNull("Should have valid usage info", usage.usageInfo)
        assertTrue("Usage should be in a Move file", 
            usage.file.extension.equals("move", ignoreCase = true))
    }
    
    fun testFindUsagesForStruct() {
        myFixture.configureByFiles("StructUsageTest.move")
        
        val usages = myFixture.findUsages(myFixture.elementAtCaret)
        assertTrue("Should find usages for struct", usages.isNotEmpty())
        
        // Check usage details
        val usage = usages.first() as UsageInfo2UsageAdapter
        assertNotNull("Should have valid usage info", usage.usageInfo)
    }
    
    fun testFindUsagesForVariable() {
        myFixture.configureByFiles("VariableUsageTest.move")
        
        val usages = myFixture.findUsages(myFixture.elementAtCaret)
        assertTrue("Should find usages for variable", usages.isNotEmpty())
        
        // Check usage details
        val usage = usages.first() as UsageInfo2UsageAdapter
        assertNotNull("Should have valid usage info", usage.usageInfo)
    }
    
    fun testFindUsagesForModule() {
        myFixture.configureByFiles("ModuleUsageTest.move")
        
        val usages = myFixture.findUsages(myFixture.elementAtCaret)
        assertTrue("Should find usages for module", usages.isNotEmpty())
        
        // Check usage details
        val usage = usages.first() as UsageInfo2UsageAdapter
        assertNotNull("Should have valid usage info", usage.usageInfo)
    }
    
    fun testFindUsagesAcrossFiles() {
        myFixture.configureByFiles("CrossFileUsageTest1.move", "CrossFileUsageTest2.move")
        
        val usages = myFixture.findUsages(myFixture.elementAtCaret)
        assertTrue("Should find cross-file usages", usages.isNotEmpty())
        
        // Check that usages are found in different files
        val usageFiles = usages.map { (it as UsageInfo2UsageAdapter).file.name }.toSet()
        assertTrue("Should find usages in multiple files", usageFiles.size > 1)
    }
}
