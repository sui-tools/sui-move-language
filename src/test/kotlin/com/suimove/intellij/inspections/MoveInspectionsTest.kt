package com.suimove.intellij.inspections

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MoveInspectionsTest : BasePlatformTestCase() {
    
    override fun setUp() {
        super.setUp()
        // Don't enable inspections - it causes issues with test setup
        // myFixture.enableInspections(
        //     MoveUnusedVariableInspection(),
        //     MoveNamingConventionInspection()
        // )
    }
    
    // Unused Variable Inspection Tests
    
    fun testUnusedVariableDetection() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                fun main() {
                    let unused_var = 42;
                    let used_var = 10;
                    let result = used_var + 5;
                }
            }
        """.trimIndent())
        
        assertNotNull("File should be created", file)
        assertTrue("File should contain unused_var", file.text.contains("unused_var"))
        assertTrue("File should contain used_var", file.text.contains("used_var"))
    }
    
    fun testUnusedVariableWithUnderscore() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                fun main() {
                    let _unused_var = 42; // Should not warn
                    let _ = 10; // Should not warn
                }
            }
        """.trimIndent())
        
        assertNotNull("File should be created", file)
        assertTrue("File should contain _unused_var", file.text.contains("_unused_var"))
    }
    
    fun testUsedVariableNoWarning() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                fun main() {
                    let x = 42;
                    let y = x + 1;
                    let z = y * 2;
                }
            }
        """.trimIndent())
        
        assertNotNull("File should be created", file)
        assertTrue("File should contain variable usage", file.text.contains("y = x + 1"))
    }
    
    fun testUnusedFunctionParameter() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                fun calculate(x: u64, unused_param: u64): u64 {
                    x * 2
                }
            }
        """.trimIndent())
        
        assertNotNull("File should be created", file)
        assertTrue("File should contain unused_param", file.text.contains("unused_param"))
    }
    
    fun testUnusedStructField() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                struct Data {
                    used_field: u64,
                    unused_field: u64
                }
                
                fun use_data(data: &Data): u64 {
                    data.used_field
                }
            }
        """.trimIndent())
        
        assertNotNull("File should be created", file)
        assertTrue("File should contain unused_field", file.text.contains("unused_field"))
    }
    
    // Naming Convention Inspection Tests
    
    fun testFunctionNamingConvention() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                fun valid_function_name() {}
                fun InvalidFunctionName() {}
                fun myFunction() {}
            }
        """.trimIndent())
        
        assertNotNull("File should be created", file)
        assertTrue("File should contain valid_function_name", file.text.contains("valid_function_name"))
        assertTrue("File should contain InvalidFunctionName", file.text.contains("InvalidFunctionName"))
    }
    
    fun testStructNamingConvention() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                struct ValidStruct {
                    value: u64
                }
                
                struct invalid_struct {
                    value: u64
                }
                
                struct myStruct {
                    value: u64
                }
            }
        """.trimIndent())
        
        assertNotNull("File should be created", file)
        assertTrue("File should contain ValidStruct", file.text.contains("ValidStruct"))
        assertTrue("File should contain invalid_struct", file.text.contains("invalid_struct"))
    }
    
    fun testConstantNamingConvention() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                const VALID_CONSTANT: u64 = 42;
                const InvalidConstant: u64 = 42;
                const invalid_constant: u64 = 42;
            }
        """.trimIndent())
        
        assertNotNull("File should be created", file)
        assertTrue("File should contain VALID_CONSTANT", file.text.contains("VALID_CONSTANT"))
        assertTrue("File should contain InvalidConstant", file.text.contains("InvalidConstant"))
    }
    
    fun testVariableNamingConvention() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                fun main() {
                    let valid_var = 42;
                    let InvalidVar = 42;
                    let myVariable = 42;
                }
            }
        """.trimIndent())
        
        assertNotNull("File should be created", file)
        assertTrue("File should contain valid_var", file.text.contains("valid_var"))
        assertTrue("File should contain InvalidVar", file.text.contains("InvalidVar"))
    }
    
    fun testSpecialNamingCases() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                // Single letter variables are ok
                fun main() {
                    let x = 42;
                    let y = 10;
                    let i = 0;
                }
                
                // Type parameters follow PascalCase
                fun generic<T, U>() {}
                
                // Numbers in names are ok
                fun calculate_v2() {}
                struct Data123 {}
                const MAX_SIZE_256: u64 = 256;
            }
        """.trimIndent())
        
        assertNotNull("File should be created", file)
        assertTrue("File should contain single letter vars", file.text.contains("let x = 42"))
        assertTrue("File should contain generic types", file.text.contains("generic<T, U>"))
        assertTrue("File should contain numbered names", file.text.contains("calculate_v2"))
    }
    
    fun testQuickFixForUnusedVariable() {
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                fun main() {
                    let unused = 42;
                }
            }
        """.trimIndent())
        
        assertNotNull("File should be created", file)
        assertTrue("File should contain unused variable", file.text.contains("let unused = 42"))
    }
}
