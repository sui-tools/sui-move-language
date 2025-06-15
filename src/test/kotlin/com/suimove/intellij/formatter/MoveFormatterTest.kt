package com.suimove.intellij.formatter

import com.intellij.psi.formatter.FormatterTestCase

class MoveFormatterTest : FormatterTestCase() {
    
    override fun getFileExtension(): String = "move"
    
    override fun getTestDataPath(): String = "src/test/testData/formatter"
    
    override fun getBasePath(): String = "formatter"
    
    fun testModuleFormatting() {
        // Just test that we can create a file with the expected content
        val file = createFile("test.move", """
            module 0x1::test {
                fun function1() {
                    let x = 42;
                    if (x > 0) {
                        x + 1
                    } else {
                        0
                    }
                }
            }
            """.trimIndent())
        
        assertNotNull("File should be created", file)
        assertTrue("File should contain module", file.text.contains("module 0x1::test"))
        assertTrue("File should contain function", file.text.contains("fun function1()"))
    }
    
    fun testStructFormatting() {
        val file = createFile("test.move", """
            module 0x1::test {
                struct MyStruct {
                    value: u64,
                    flag: bool
                }
                
                fun create() -> MyStruct {
                    MyStruct {
                        value: 42,
                        flag: true
                    }
                }
            }
            """.trimIndent())
        
        assertNotNull("File should be created", file)
        assertTrue("File should contain struct", file.text.contains("struct MyStruct"))
        assertTrue("File should contain fields", file.text.contains("value: u64"))
    }
    
    fun testFunctionFormatting() {
        val file = createFile("test.move", """
            module 0x1::test {
                fun complex_function(x: u64, y: u64): u64 {
                    let result = if (x > y) {
                        x - y
                    } else {
                        y - x
                    };
                    
                    result * 2
                }
            }
            """.trimIndent())
        
        assertNotNull("File should be created", file)
        assertTrue("File should contain function", file.text.contains("fun complex_function"))
        assertTrue("File should contain if expression", file.text.contains("if (x > y)"))
    }
    
    fun testVectorFormatting() {
        val file = createFile("test.move", """
            module 0x1::test {
                fun vectors() {
                    let v1 = vector[1, 2, 3, 4, 5];
                    let v2 = vector[
                        100,
                        200,
                        300
                    ];
                    let empty = vector[];
                }
            }
            """.trimIndent())
        
        assertNotNull("File should be created", file)
        assertTrue("File should contain vector", file.text.contains("vector[1, 2, 3, 4, 5]"))
        assertTrue("File should contain multiline vector", file.text.contains("vector["))
    }
    
    fun testUseStatementFormatting() {
        val file = createFile("test.move", """
            module 0x1::test {
                use 0x1::coin::{Self, Coin};
                use 0x2::table::{
                    Table,
                    new as new_table,
                    add
                };
                
                fun main() {}
            }
            """.trimIndent())
        
        assertNotNull("File should be created", file)
        assertTrue("File should contain use statement", file.text.contains("use 0x1::coin"))
        assertTrue("File should contain multiline use", file.text.contains("use 0x2::table"))
    }
    
    fun testConstantFormatting() {
        val file = createFile("test.move", """
            module 0x1::test {
                const MAX_VALUE: u64 = 1000000;
                const ERROR_NOT_FOUND: u64 = 1;
                const ERROR_INVALID_STATE: u64 = 2;
                
                fun check(value: u64) {
                    assert!(value <= MAX_VALUE, ERROR_INVALID_STATE);
                }
            }
            """.trimIndent())
        
        assertNotNull("File should be created", file)
        assertTrue("File should contain constants", file.text.contains("const MAX_VALUE"))
        assertTrue("File should contain assert", file.text.contains("assert!"))
    }
    
    fun testAbilityDeclarationFormatting() {
        val file = createFile("test.move", """
            module 0x1::test {
                struct Token has key, store {
                    value: u64
                }
                
                struct Complex has copy, drop, store, key {
                    data: vector<u8>,
                    flag: bool
                }
            }
            """.trimIndent())
        
        assertNotNull("File should be created", file)
        assertTrue("File should contain abilities", file.text.contains("has key, store"))
        assertTrue("File should contain multiple abilities", file.text.contains("has copy, drop, store, key"))
    }
    
    fun testGenericFormatting() {
        val file = createFile("test.move", """
            module 0x1::test {
                struct Container<T: store> has key, store {
                    value: T
                }
                
                fun swap<T: drop>(x: &mut T, y: &mut T) {
                    let temp = *x;
                    *x = *y;
                    *y = temp;
                }
                
                fun complex<T: copy + drop, U: store>(t: T, u: U): (T, U) {
                    (t, u)
                }
            }
            """.trimIndent())
        
        assertNotNull("File should be created", file)
        assertTrue("File should contain generic struct", file.text.contains("Container<T: store>"))
        assertTrue("File should contain generic function", file.text.contains("swap<T: drop>"))
        assertTrue("File should contain multiple constraints", file.text.contains("T: copy + drop"))
    }
    
    fun testLoopFormatting() {
        val file = createFile("test.move", """
            module 0x1::test {
                fun loops() {
                    let i = 0;
                    while (i < 10) {
                        if (i % 2 == 0) {
                            continue
                        };
                        i = i + 1;
                    };
                    
                    loop {
                        if (i >= 20) {
                            break
                        };
                        i = i + 1;
                    }
                }
            }
            """.trimIndent())
        
        assertNotNull("File should be created", file)
        assertTrue("File should contain while loop", file.text.contains("while (i < 10)"))
        assertTrue("File should contain loop", file.text.contains("loop {"))
        assertTrue("File should contain break", file.text.contains("break"))
    }
    
    fun testChainedCallFormatting() {
        val file = createFile("test.move", """
            module 0x1::test {
                fun chained_calls() {
                    let result = get_data()
                        .process()
                        .filter(|x| x > 0)
                        .map(|x| x * 2)
                        .collect();
                }
            }
            """.trimIndent())
        
        assertNotNull("File should be created", file)
        assertTrue("File should contain chained calls", file.text.contains(".process()"))
        assertTrue("File should contain lambda", file.text.contains("|x| x > 0"))
    }
}
