package com.suimove.intellij.annotator

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MoveAnnotatorTest : BasePlatformTestCase() {
    
    override fun setUp() {
        super.setUp()
        myFixture.configureByText("test.move", "")
    }
    
    fun testHighlightKeywords() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun test_keywords() {
                    let x = 42;
                    if (x > 0) {
                        return x
                    } else {
                        return 0
                    }
                }
            }
        """.trimIndent())
        
        myFixture.checkHighlighting(false, false, true)
        
        // Check that keywords are highlighted
        val highlights = myFixture.doHighlighting()
        assertTrue("Should highlight 'module' keyword", 
            highlights.any { it.text == "module" })
        assertTrue("Should highlight 'fun' keyword", 
            highlights.any { it.text == "fun" })
        assertTrue("Should highlight 'let' keyword", 
            highlights.any { it.text == "let" })
    }
    
    fun testHighlightTypes() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun test_types() {
                    let x: u64 = 42;
                    let y: bool = true;
                    let z: address = @0x1;
                    let v: vector<u8> = vector[];
                }
            }
        """.trimIndent())
        
        myFixture.checkHighlighting(false, false, true)
        
        val highlights = myFixture.doHighlighting()
        assertTrue("Should highlight 'u64' type", 
            highlights.any { it.text == "u64" })
        assertTrue("Should highlight 'bool' type", 
            highlights.any { it.text == "bool" })
        assertTrue("Should highlight 'address' type", 
            highlights.any { it.text == "address" })
    }
    
    fun testErrorHighlighting() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun test_errors() {
                    let x = undefined_var; // Error: undefined variable
                    unknown_function(); // Error: undefined function
                }
            }
        """.trimIndent())
        
        val highlights = myFixture.doHighlighting()
        
        // Should have error highlights for undefined elements
        assertTrue("Should highlight undefined variable error",
            highlights.any { it.text == "undefined_var" && it.severity.name == "ERROR" })
        assertTrue("Should highlight undefined function error",
            highlights.any { it.text == "unknown_function" && it.severity.name == "ERROR" })
    }
    
    fun testWarningHighlighting() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun test_warnings() {
                    let unused_var = 42; // Warning: unused variable
                }
            }
        """.trimIndent())
        
        val highlights = myFixture.doHighlighting()
        
        // Should have warning for unused variable
        assertTrue("Should highlight unused variable warning",
            highlights.any { it.text == "unused_var" && it.severity.name == "WARNING" })
    }
    
    fun testLiteralHighlighting() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun test_literals() {
                    let num = 42;
                    let hex = 0xFF;
                    let bool_val = true;
                    let addr = @0x1;
                    let str = b"hello";
                }
            }
        """.trimIndent())
        
        myFixture.checkHighlighting(false, false, true)
        
        val highlights = myFixture.doHighlighting()
        assertTrue("Should highlight number literal", 
            highlights.any { it.text == "42" })
        assertTrue("Should highlight hex literal", 
            highlights.any { it.text == "0xFF" })
        assertTrue("Should highlight boolean literal", 
            highlights.any { it.text == "true" })
    }
    
    fun testCommentHighlighting() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                // This is a line comment
                fun test_comments() {
                    /* This is a
                       block comment */
                    let x = 42; // inline comment
                }
            }
        """.trimIndent())
        
        myFixture.checkHighlighting(false, false, true)
        
        val highlights = myFixture.doHighlighting()
        assertTrue("Should highlight line comments",
            highlights.any { it.text.contains("This is a line comment") })
        assertTrue("Should highlight block comments",
            highlights.any { it.text.contains("This is a") })
    }
    
    fun testStructHighlighting() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                struct MyStruct {
                    value: u64,
                    flag: bool
                }
                
                fun test_struct() {
                    let s = MyStruct { value: 42, flag: true };
                }
            }
        """.trimIndent())
        
        myFixture.checkHighlighting(false, false, true)
        
        val highlights = myFixture.doHighlighting()
        assertTrue("Should highlight struct name",
            highlights.any { it.text == "MyStruct" })
    }
    
    fun testHighlightUnresolvedImport() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                use 0x1::NonExistentModule;
                
                fun main() {
                    NonExistentModule::some_function();
                }
            }
        """.trimIndent())
        
        myFixture.checkHighlighting(true, false, true)
        
        val highlights = myFixture.doHighlighting()
        assertTrue("Should highlight unresolved import",
            highlights.any { it.text == "NonExistentModule" && it.severity.name == "ERROR" })
    }
    
    fun testHighlightTypeMismatch() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun main() {
                    let x: u64 = true; // Type mismatch
                    let y: bool = 42;  // Type mismatch
                }
            }
        """.trimIndent())
        
        myFixture.checkHighlighting(true, false, true)
    }
    
    fun testHighlightUnusedVariable() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun main() {
                    let unused_var = 42;
                    let used_var = 10;
                    let result = used_var + 5;
                }
            }
        """.trimIndent())
        
        myFixture.checkHighlighting(true, true, true)
        
        val highlights = myFixture.doHighlighting()
        assertTrue("Should highlight unused variable",
            highlights.any { it.text == "unused_var" && it.severity.name == "WARNING" })
    }
    
    fun testHighlightMissingAbility() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                struct NoCopy {}
                
                fun main() {
                    let x = NoCopy {};
                    let y = x; // Error: NoCopy doesn't have copy ability
                    let z = x; // Error: x was moved
                }
            }
        """.trimIndent())
        
        myFixture.checkHighlighting(true, false, true)
    }
    
    fun testHighlightInvalidAddressLiteral() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                fun main() {
                    let valid_addr = @0x1;
                    let invalid_addr1 = @0xG; // Invalid hex
                    let invalid_addr2 = @123; // Missing 0x prefix
                }
            }
        """.trimIndent())
        
        myFixture.checkHighlighting(true, false, true)
    }
    
    fun testHighlightDuplicateFieldInStruct() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                struct MyStruct {
                    field1: u64,
                    field2: bool,
                    field1: u64  // Duplicate field
                }
            }
        """.trimIndent())
        
        myFixture.checkHighlighting(true, false, true)
    }
    
    fun testHighlightInvalidFunctionVisibility() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                public entry fun valid_entry() {}
                entry public fun invalid_order() {} // Wrong order
                public public fun duplicate_visibility() {} // Duplicate
            }
        """.trimIndent())
        
        myFixture.checkHighlighting(true, false, true)
    }
    
    fun testHighlightMissingGenericParameter() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                struct Generic<T> { value: T }
                
                fun main() {
                    let x: Generic = Generic { value: 42 }; // Missing type parameter
                    let y: Generic<u64> = Generic<u64> { value: 42 }; // Correct
                }
            }
        """.trimIndent())
        
        myFixture.checkHighlighting(true, false, true)
    }
    
    fun testHighlightInvalidConstantExpression() {
        myFixture.configureByText("test.move", """
            module 0x1::test {
                const VALID: u64 = 42;
                const INVALID: u64 = get_value(); // Non-constant expression
                
                fun get_value(): u64 { 42 }
            }
        """.trimIndent())
        
        myFixture.checkHighlighting(true, false, true)
    }
    
    fun testHighlightCircularDependency() {
        myFixture.configureByText("test.move", """
            module 0x1::a {
                use 0x1::b;
                public fun func_a() { b::func_b() }
            }
            
            module 0x1::b {
                use 0x1::a;
                public fun func_b() { a::func_a() }
            }
        """.trimIndent())
        
        myFixture.checkHighlighting(true, true, true)
    }
}
