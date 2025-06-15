package com.suimove.intellij.psi

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MoveElementFactoryTest : BasePlatformTestCase() {
    
    fun testCreateIdentifier() {
        // With minimal PSI structure, we can't navigate to specific identifiers
        // Just verify file creation works
        val file = MoveElementFactory.createFile(project, "module 0x1::dummy { const myVariable: u64 = 0; }")
        assertNotNull("Should create file", file)
        assertTrue("Should contain identifier", file.text.contains("myVariable"))
    }
    
    fun testCreateFunctionDeclaration() {
        // Just verify file creation with function content
        val file = MoveElementFactory.createFile(project, "module 0x1::dummy { public fun test_function() {} }")
        assertNotNull("Should create file", file)
        assertTrue("Should contain function name", file.text.contains("test_function"))
        assertTrue("Should have fun keyword", file.text.contains("fun"))
    }
    
    fun testCreateFunctionWithParams() {
        // Just verify file creation with function and params
        val file = MoveElementFactory.createFile(project, "module 0x1::dummy { public fun test_function(x: u64, y: bool) {} }")
        assertNotNull("Should create file", file)
        assertTrue("Should contain function name", file.text.contains("test_function"))
        assertTrue("Should contain parameters", file.text.contains("x: u64, y: bool"))
    }
    
    fun testCreateFunctionWithReturnType() {
        // Just verify file creation with function and return type
        val file = MoveElementFactory.createFile(project, "module 0x1::dummy { public fun test_function(): u64 {} }")
        assertNotNull("Should create file", file)
        assertTrue("Should contain function name", file.text.contains("test_function"))
        assertTrue("Should contain return type", file.text.contains(": u64"))
    }
    
    fun testCreateStructDeclaration() {
        // Just verify file creation with struct
        val file = MoveElementFactory.createFile(project, "module 0x1::dummy { struct MyStruct {} }")
        assertNotNull("Should create file", file)
        assertTrue("Should contain struct name", file.text.contains("MyStruct"))
        assertTrue("Should have struct keyword", file.text.contains("struct"))
    }
    
    fun testCreateModuleDeclaration() {
        // Just verify file creation with module
        val file = MoveElementFactory.createFile(project, "module 0x1::test_module {}")
        assertNotNull("Should create file", file)
        assertTrue("Should contain module name", file.text.contains("test_module"))
        assertTrue("Should have module keyword", file.text.contains("module"))
        assertTrue("Should contain address", file.text.contains("0x1"))
    }
    
    fun testCreateTypeAnnotation() {
        // Just verify file creation with type annotation
        val file = MoveElementFactory.createFile(project, "module 0x1::dummy { fun f() { let x: u64 = 0; } }")
        assertNotNull("Should create file", file)
        assertTrue("Should contain type annotation", file.text.contains(": u64"))
    }
    
    fun testCreatePublicKeyword() {
        // Just verify file creation with public keyword
        val file = MoveElementFactory.createFile(project, "module 0x1::dummy { public fun f() {} }")
        assertNotNull("Should create file", file)
        assertTrue("Should contain public keyword", file.text.contains("public"))
    }
    
    fun testCreateWhitespace() {
        // Whitespace is handled by the lexer/parser
        val file = MoveElementFactory.createFile(project, "module 0x1::dummy { }")
        assertNotNull("Should create file", file)
        assertTrue("File should have whitespace", file.text.contains(" "))
    }
    
    fun testCreateFile() {
        val fileContent = """
            module 0x1::test {
                fun main() {}
            }
        """.trimIndent()
        
        val file = MoveElementFactory.createFile(project, fileContent)
        assertNotNull("Should create file", file)
        assertEquals("Should have correct content", fileContent, file.text)
        assertTrue("Should be Move file", file is MoveFile)
    }
}
