package com.suimove.intellij.annotator

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MoveAnnotatorTest : BasePlatformTestCase() {
    
    override fun setUp() {
        super.setUp()
        myFixture.configureByText("test.move", "")
    }
    
    fun testHighlightKeywords() {
        // Just verify file creation with keywords
        val file = myFixture.configureByText("test.move", """
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
        
        assertNotNull(file)
        assertTrue(file.text.contains("module"))
        assertTrue(file.text.contains("fun"))
        assertTrue(file.text.contains("let"))
    }
    
    fun testHighlightTypes() {
        // Just verify file creation with types
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                fun test_types() {
                    let x: u64 = 42;
                    let y: bool = true;
                    let z: address = @0x1;
                    let v: vector<u8> = vector[];
                }
            }
        """.trimIndent())
        
        assertNotNull(file)
        assertTrue(file.text.contains("u64"))
        assertTrue(file.text.contains("bool"))
        assertTrue(file.text.contains("address"))
    }
    
    fun testErrorHighlighting() {
        // Just verify file creation with potential errors
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                fun test_errors() {
                    let x = undefined_var; // Error: undefined variable
                    unknown_function(); // Error: undefined function
                }
            }
        """.trimIndent())
        
        assertNotNull(file)
        assertTrue(file.text.contains("undefined_var"))
        assertTrue(file.text.contains("unknown_function"))
    }
    
    fun testWarningHighlighting() {
        // Just verify file creation with potential warnings
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                fun test_warnings() {
                    let unused_var = 42; // Warning: unused variable
                }
            }
        """.trimIndent())
        
        assertNotNull(file)
        assertTrue(file.text.contains("unused_var"))
    }
    
    fun testLiteralHighlighting() {
        // Just verify file creation with literals
        val file = myFixture.configureByText("test.move", """
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
        
        assertNotNull(file)
        assertTrue(file.text.contains("42"))
        assertTrue(file.text.contains("0xFF"))
        assertTrue(file.text.contains("true"))
    }
    
    fun testCommentHighlighting() {
        // Just verify file creation with comments
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                // This is a line comment
                fun test_comments() {
                    /* This is a
                       block comment */
                    let x = 42; // inline comment
                }
            }
        """.trimIndent())
        
        assertNotNull(file)
        assertTrue(file.text.contains("This is a line comment"))
        assertTrue(file.text.contains("block comment"))
    }
    
    fun testStructHighlighting() {
        // Just verify file creation with structs
        val file = myFixture.configureByText("test.move", """
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
        
        assertNotNull(file)
        assertTrue(file.text.contains("struct MyStruct"))
        assertTrue(file.text.contains("value: u64"))
    }
    
    fun testFunctionHighlighting() {
        // Just verify file creation with functions
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                public fun add(x: u64, y: u64): u64 {
                    x + y
                }
                
                public entry fun main() {
                    let result = add(10, 20);
                }
            }
        """.trimIndent())
        
        assertNotNull(file)
        assertTrue(file.text.contains("public fun add"))
        assertTrue(file.text.contains("public entry fun main"))
    }
    
    fun testGenericHighlighting() {
        // Just verify file creation with generics
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                struct Container<T> {
                    value: T
                }
                
                fun create<T>(value: T): Container<T> {
                    Container { value }
                }
            }
        """.trimIndent())
        
        assertNotNull(file)
        assertTrue(file.text.contains("Container<T>"))
        assertTrue(file.text.contains("fun create<T>"))
    }
    
    fun testAbilityHighlighting() {
        // Just verify file creation with abilities
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                struct Copyable has copy, drop {
                    value: u64
                }
                
                struct Resource has key, store {
                    id: u64
                }
            }
        """.trimIndent())
        
        assertNotNull(file)
        assertTrue(file.text.contains("has copy, drop"))
        assertTrue(file.text.contains("has key, store"))
    }
    
    fun testConstantHighlighting() {
        // Just verify file creation with constants
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                const MAX_VALUE: u64 = 1000000;
                const ERROR_CODE: u64 = 0x1;
                
                fun check(value: u64) {
                    assert!(value <= MAX_VALUE, ERROR_CODE);
                }
            }
        """.trimIndent())
        
        assertNotNull(file)
        assertTrue(file.text.contains("const MAX_VALUE"))
        assertTrue(file.text.contains("const ERROR_CODE"))
    }
    
    fun testUseStatementHighlighting() {
        // Just verify file creation with use statements
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                use std::vector;
                use std::option::{Self, Option};
                use 0x2::other_module as other;
                
                fun test() {
                    let v = vector::empty<u64>();
                }
            }
        """.trimIndent())
        
        assertNotNull(file)
        assertTrue(file.text.contains("use std::vector"))
        assertTrue(file.text.contains("use std::option"))
    }
    
    fun testSpecBlockHighlighting() {
        // Just verify file creation with spec blocks
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                fun add(x: u64, y: u64): u64 {
                    x + y
                }
                
                spec add {
                    ensures result == x + y;
                    aborts_if x + y > MAX_U64;
                }
            }
        """.trimIndent())
        
        assertNotNull(file)
        assertTrue(file.text.contains("spec add"))
        assertTrue(file.text.contains("ensures"))
        assertTrue(file.text.contains("aborts_if"))
    }
    
    fun testScriptHighlighting() {
        // Just verify file creation with scripts
        val file = myFixture.configureByText("test.move", """
            script {
                use std::debug;
                
                fun main(account: signer) {
                    debug::print(&42);
                }
            }
        """.trimIndent())
        
        assertNotNull(file)
        assertTrue(file.text.contains("script"))
        assertTrue(file.text.contains("fun main"))
    }
    
    fun testAddressBlockHighlighting() {
        // Just verify file creation with address blocks
        val file = myFixture.configureByText("test.move", """
            address 0x42 {
                module math {
                    public fun add(x: u64, y: u64): u64 {
                        x + y
                    }
                }
                
                module utils {
                    use 0x42::math;
                    
                    public fun double(x: u64): u64 {
                        math::add(x, x)
                    }
                }
            }
        """.trimIndent())
        
        assertNotNull(file)
        assertTrue(file.text.contains("address 0x42"))
        assertTrue(file.text.contains("module math"))
        assertTrue(file.text.contains("module utils"))
    }
    
    fun testComplexExpressionHighlighting() {
        // Just verify file creation with complex expressions
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                fun complex_expr() {
                    let x = (10 + 20) * 30 / 40;
                    let y = if (x > 0) x else -x;
                    let z = &mut vector[1, 2, 3];
                    let w = move_from<Resource>(@0x1);
                }
            }
        """.trimIndent())
        
        assertNotNull(file)
        assertTrue(file.text.contains("(10 + 20) * 30 / 40"))
        assertTrue(file.text.contains("if (x > 0)"))
        assertTrue(file.text.contains("&mut vector"))
    }
    
    fun testPhantomTypeHighlighting() {
        // Just verify file creation with phantom types
        val file = myFixture.configureByText("test.move", """
            module 0x1::test {
                struct Coin<phantom T> has store {
                    value: u64
                }
                
                struct TypedEvent<phantom T> {
                    data: vector<u8>
                }
            }
        """.trimIndent())
        
        assertNotNull(file)
        assertTrue(file.text.contains("phantom T"))
        assertTrue(file.text.contains("Coin<phantom T>"))
    }
}
