package com.suimove.intellij.psi

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MoveElementFactoryTest : BasePlatformTestCase() {
    
    fun testCreateIdentifier() {
        val identifier = MoveElementFactory.createIdentifier(project, "myVariable")
        assertNotNull("Should create identifier", identifier)
        assertEquals("Should have correct text", "myVariable", identifier.text)
    }
    
    fun testCreateFunction() {
        val function = MoveElementFactory.createFunction(project, "test_function")
        assertNotNull("Should create function", function)
        assertTrue("Should contain function name", function.text.contains("test_function"))
        assertTrue("Should have fun keyword", function.text.contains("fun"))
    }
    
    fun testCreateStruct() {
        val struct = MoveElementFactory.createStruct(project, "MyStruct")
        assertNotNull("Should create struct", struct)
        assertTrue("Should contain struct name", struct.text.contains("MyStruct"))
        assertTrue("Should have struct keyword", struct.text.contains("struct"))
    }
    
    fun testCreateModule() {
        val module = MoveElementFactory.createModule(project, "test_module")
        assertNotNull("Should create module", module)
        assertTrue("Should contain module name", module.text.contains("test_module"))
        assertTrue("Should have module keyword", module.text.contains("module"))
    }
    
    fun testCreateTypeAnnotation() {
        val typeAnnotation = MoveElementFactory.createTypeAnnotation(project, "u64")
        assertNotNull("Should create type annotation", typeAnnotation)
        assertEquals("Should have correct type", ": u64", typeAnnotation.text)
    }
    
    fun testCreateComplexTypeAnnotation() {
        val typeAnnotation = MoveElementFactory.createTypeAnnotation(project, "vector<u8>")
        assertNotNull("Should create complex type annotation", typeAnnotation)
        assertEquals("Should have correct complex type", ": vector<u8>", typeAnnotation.text)
    }
    
    fun testCreatePublicKeyword() {
        val publicKeyword = MoveElementFactory.createPublicKeyword(project)
        assertNotNull("Should create public keyword", publicKeyword)
        assertEquals("Should be 'public'", "public", publicKeyword.text)
    }
    
    fun testCreateWhitespace() {
        val whitespace = MoveElementFactory.createWhitespace(project)
        assertNotNull("Should create whitespace", whitespace)
        assertEquals("Should be single space", " ", whitespace.text)
    }
    
    fun testCreateVariableDeclaration() {
        val variable = MoveElementFactory.createVariable(project, "myVar", "u64", "42")
        assertNotNull("Should create variable declaration", variable)
        assertTrue("Should contain variable name", variable.text.contains("myVar"))
        assertTrue("Should contain type", variable.text.contains("u64"))
        assertTrue("Should contain value", variable.text.contains("42"))
        assertTrue("Should have let keyword", variable.text.contains("let"))
    }
    
    fun testCreateVariableWithoutType() {
        val variable = MoveElementFactory.createVariable(project, "myVar", null, "42")
        assertNotNull("Should create variable without type", variable)
        assertTrue("Should contain variable name", variable.text.contains("myVar"))
        assertTrue("Should contain value", variable.text.contains("42"))
        assertFalse("Should not contain colon", variable.text.contains(":"))
    }
    
    fun testCreateExpression() {
        val expression = MoveElementFactory.createExpression(project, "x + 1")
        assertNotNull("Should create expression", expression)
        assertEquals("Should have correct expression", "x + 1", expression.text)
    }
    
    fun testCreateStatement() {
        val statement = MoveElementFactory.createStatement(project, "return 42")
        assertNotNull("Should create statement", statement)
        assertTrue("Should contain return keyword", statement.text.contains("return"))
        assertTrue("Should contain value", statement.text.contains("42"))
    }
    
    fun testCreateImport() {
        val import = MoveElementFactory.createImport(project, "0x1::module")
        assertNotNull("Should create import", import)
        assertTrue("Should contain use keyword", import.text.contains("use"))
        assertTrue("Should contain module path", import.text.contains("0x1::module"))
    }
    
    fun testCreateStructField() {
        val field = MoveElementFactory.createStructField(project, "value", "u64")
        assertNotNull("Should create struct field", field)
        assertTrue("Should contain field name", field.text.contains("value"))
        assertTrue("Should contain field type", field.text.contains("u64"))
    }
    
    fun testCreateFunctionParameter() {
        val param = MoveElementFactory.createParameter(project, "x", "u64")
        assertNotNull("Should create parameter", param)
        assertTrue("Should contain parameter name", param.text.contains("x"))
        assertTrue("Should contain parameter type", param.text.contains("u64"))
    }
    
    fun testCreateFromText() {
        val element = MoveElementFactory.createFromText(
            project, 
            "let x: u64 = 42;",
            MoveElementTypes.STATEMENT
        )
        assertNotNull("Should create element from text", element)
        assertTrue("Should be a statement", element.text.contains("let"))
    }
}
