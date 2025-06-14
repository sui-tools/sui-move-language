package com.suimove.intellij.psi

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MoveElementFactoryTest : BasePlatformTestCase() {
    
    fun testCreateIdentifier() {
        val identifier = MoveElementFactory.createIdentifier(project, "myVariable")
        assertNotNull("Should create identifier", identifier)
        assertEquals("Should have correct text", "myVariable", identifier.text)
    }
    
    fun testCreateFunctionDeclaration() {
        val function = MoveElementFactory.createFunctionDeclaration(project, "test_function")
        assertNotNull("Should create function", function)
        assertTrue("Should contain function name", function.text.contains("test_function"))
        assertTrue("Should have fun keyword", function.text.contains("fun"))
    }
    
    fun testCreateFunctionWithParams() {
        val function = MoveElementFactory.createFunctionDeclaration(project, "test_function", "x: u64, y: bool")
        assertNotNull("Should create function with params", function)
        assertTrue("Should contain function name", function.text.contains("test_function"))
        assertTrue("Should contain parameters", function.text.contains("x: u64, y: bool"))
    }
    
    fun testCreateFunctionWithReturnType() {
        val function = MoveElementFactory.createFunctionDeclaration(project, "test_function", "", "u64")
        assertNotNull("Should create function with return type", function)
        assertTrue("Should contain function name", function.text.contains("test_function"))
        assertTrue("Should contain return type", function.text.contains(": u64"))
    }
    
    fun testCreateStructDeclaration() {
        val struct = MoveElementFactory.createStructDeclaration(project, "MyStruct")
        assertNotNull("Should create struct", struct)
        assertTrue("Should contain struct name", struct.text.contains("MyStruct"))
        assertTrue("Should have struct keyword", struct.text.contains("struct"))
    }
    
    fun testCreateModuleDeclaration() {
        val module = MoveElementFactory.createModuleDeclaration(project, "0x1", "test_module")
        assertNotNull("Should create module", module)
        assertTrue("Should contain module name", module.text.contains("test_module"))
        assertTrue("Should have module keyword", module.text.contains("module"))
        assertTrue("Should contain address", module.text.contains("0x1"))
    }
    
    fun testCreateTypeAnnotation() {
        val typeAnnotation = MoveElementFactory.createTypeAnnotation(project, "u64")
        assertNotNull("Should create type annotation", typeAnnotation)
        assertEquals("Should have correct type", ":", typeAnnotation.text)
    }
    
    fun testCreatePublicKeyword() {
        val publicKeyword = MoveElementFactory.createPublicKeyword(project)
        assertNotNull("Should create public keyword", publicKeyword)
        assertEquals("Should be 'public'", "public", publicKeyword.text)
    }
    
    fun testCreateWhitespace() {
        val whitespace = MoveElementFactory.createWhitespace(project)
        assertNotNull("Should create whitespace", whitespace)
        assertTrue("Should be whitespace", whitespace.text.isBlank())
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
