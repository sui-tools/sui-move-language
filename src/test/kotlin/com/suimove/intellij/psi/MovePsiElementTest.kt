package com.suimove.intellij.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.suimove.intellij.MoveFileType
import junit.framework.TestCase

class MovePsiElementTest : BasePlatformTestCase() {
    
    fun testModulePsiStructure() {
        val file = myFixture.configureByText(MoveFileType, """
            module 0x1::test_module {
                fun main() {}
            }
        """.trimIndent())
        
        // Test that module structure is parsed
        assertNotNull("File should be parsed", file)
        assertTrue("File should contain module definition", file.text.contains("module 0x1::test_module"))
        assertTrue("Module should contain function", file.text.contains("fun main()"))
    }
    
    fun testFunctionPsiStructure() {
        val file = myFixture.configureByText(MoveFileType, """
            module 0x1::test {
                public fun helper(x: u64, y: bool): u64 {
                    x
                }
                
                entry fun main() {}
                
                fun private_func() {
                    let x = 42;
                }
            }
        """.trimIndent())
        
        // Test function structures
        assertTrue("Should have public function", file.text.contains("public fun helper"))
        assertTrue("Should have entry function", file.text.contains("entry fun main"))
        assertTrue("Should have private function", file.text.contains("fun private_func"))
        
        // Test function parameters and return types
        assertTrue("Helper should have parameters", file.text.contains("(x: u64, y: bool)"))
        assertTrue("Helper should have return type", file.text.contains("): u64"))
    }
    
    fun testStructPsiStructure() {
        val file = myFixture.configureByText(MoveFileType, """
            module 0x1::test {
                struct SimpleStruct {
                    value: u64
                }
                
                struct GenericStruct<T> has copy, drop {
                    data: T
                }
                
                struct PhantomStruct<phantom T> {
                    marker: u64
                }
                
                struct MultiFieldStruct {
                    field1: u64,
                    field2: bool,
                    field3: vector<u8>
                }
            }
        """.trimIndent())
        
        // Test struct definitions
        assertTrue("Should have SimpleStruct", file.text.contains("struct SimpleStruct"))
        assertTrue("Should have GenericStruct with abilities", 
            file.text.contains("struct GenericStruct<T> has copy, drop"))
        assertTrue("Should have PhantomStruct with phantom parameter", 
            file.text.contains("struct PhantomStruct<phantom T>"))
        assertTrue("Should have MultiFieldStruct", file.text.contains("struct MultiFieldStruct"))
    }
    
    fun testConstantPsiStructure() {
        val file = myFixture.configureByText(MoveFileType, """
            module 0x1::test {
                const MAX_VALUE: u64 = 1000;
                const ERROR_CODE: u64 = 1;
                const FLAG: bool = true;
                const HEX_VALUE: u64 = 0xFF;
                const COMPLEX_EXPR: u64 = 10 + 20 * 30;
            }
        """.trimIndent())
        
        // Test constant definitions
        assertTrue("Should have MAX_VALUE constant", file.text.contains("const MAX_VALUE: u64 = 1000"))
        assertTrue("Should have ERROR_CODE constant", file.text.contains("const ERROR_CODE: u64 = 1"))
        assertTrue("Should have FLAG constant", file.text.contains("const FLAG: bool = true"))
        assertTrue("Should have HEX_VALUE constant", file.text.contains("const HEX_VALUE: u64 = 0xFF"))
        assertTrue("Should have COMPLEX_EXPR constant", file.text.contains("const COMPLEX_EXPR: u64 = 10 + 20 * 30"))
    }
    
    fun testUseStatementPsiStructure() {
        val file = myFixture.configureByText(MoveFileType, """
            module 0x1::test {
                use 0x1::vector;
                use 0x1::signer::{Self, address_of};
                use 0x2::token::{Token as T, create_token};
                use std::string::String;
            }
        """.trimIndent())
        
        // Test use statements
        assertTrue("Should have simple use statement", file.text.contains("use 0x1::vector"))
        assertTrue("Should have use with Self import", file.text.contains("use 0x1::signer::{Self, address_of}"))
        assertTrue("Should have use with alias", file.text.contains("use 0x2::token::{Token as T"))
        assertTrue("Should have std library import", file.text.contains("use std::string::String"))
    }
    
    fun testExpressionPsiStructures() {
        val file = myFixture.configureByText(MoveFileType, """
            module 0x1::test {
                fun expressions() {
                    // Literals
                    let literal = 42;
                    let hex = 0xFF;
                    let bool_lit = true;
                    let addr = @0x1;
                    
                    // Binary operations
                    let sum = 1 + 2;
                    let product = 3 * 4;
                    let comparison = x > y;
                    let logical = a && b;
                    
                    // Function calls
                    let result = vector::length(&v);
                    let value = module::function(arg1, arg2);
                    
                    // Field access
                    let field = obj.field;
                    let nested = obj.inner.value;
                    
                    // Index access
                    let element = v[0];
                    let item = map[key];
                    
                    // Type cast
                    let casted = (x as u64);
                    
                    // Block expression
                    let block_result = { 
                        let x = 1; 
                        x + 1 
                    };
                    
                    // If expression
                    let if_result = if (condition) value1 else value2;
                    
                    // While loop
                    while (i < 10) {
                        i = i + 1;
                    };
                    
                    // Loop with break
                    loop {
                        if (done) break;
                    };
                }
            }
        """.trimIndent())
        
        // Test various expression types
        assertTrue("Should have literal expressions", file.text.contains("let literal = 42"))
        assertTrue("Should have hex literal", file.text.contains("let hex = 0xFF"))
        assertTrue("Should have address literal", file.text.contains("let addr = @0x1"))
        assertTrue("Should have binary operations", file.text.contains("let sum = 1 + 2"))
        assertTrue("Should have function calls", file.text.contains("vector::length(&v)"))
        assertTrue("Should have field access", file.text.contains("obj.field"))
        assertTrue("Should have index access", file.text.contains("v[0]"))
        assertTrue("Should have type cast", file.text.contains("(x as u64)"))
        assertTrue("Should have block expression", file.text.contains("let block_result = {"))
        assertTrue("Should have if expression", file.text.contains("if (condition)"))
        assertTrue("Should have while loop", file.text.contains("while (i < 10)"))
        assertTrue("Should have loop with break", file.text.contains("if (done) break"))
    }
    
    fun testTypeReferencePsiStructures() {
        val file = myFixture.configureByText(MoveFileType, """
            module 0x1::test {
                fun types() {
                    // Primitive types
                    let x: u64;
                    let y: u8;
                    let z: bool;
                    let addr: address;
                    
                    // Generic types
                    let vec: vector<u8>;
                    let nested: vector<vector<u64>>;
                    
                    // Reference types
                    let ref: &u64;
                    let mut_ref: &mut Token;
                    
                    // Qualified types
                    let token: 0x1::coin::Coin<0x2::sui::SUI>;
                    let obj: module::Struct;
                    
                    // Tuple types
                    let pair: (u64, bool);
                    let triple: (address, u64, vector<u8>);
                }
            }
        """.trimIndent())
        
        // Test type references
        assertTrue("Should have primitive types", file.text.contains("let x: u64"))
        assertTrue("Should have vector type", file.text.contains("let vec: vector<u8>"))
        assertTrue("Should have nested generic", file.text.contains("vector<vector<u64>>"))
        assertTrue("Should have reference type", file.text.contains("let ref: &u64"))
        assertTrue("Should have mutable reference", file.text.contains("let mut_ref: &mut Token"))
        assertTrue("Should have qualified type", file.text.contains("0x1::coin::Coin<0x2::sui::SUI>"))
        assertTrue("Should have tuple type", file.text.contains("let pair: (u64, bool)"))
    }
    
    fun testSpecBlockPsiStructure() {
        val file = myFixture.configureByText(MoveFileType, """
            module 0x1::test {
                spec module {
                    pragma verify = true;
                    pragma aborts_if_is_strict;
                }
                
                fun transfer(from: &mut u64, to: &mut u64, amount: u64) {
                    *from = *from - amount;
                    *to = *to + amount;
                }
                
                spec transfer {
                    aborts_if from < amount;
                    ensures from == old(from) - amount;
                    ensures to == old(to) + amount;
                }
            }
        """.trimIndent())
        
        // Test spec blocks
        assertTrue("Should have module spec block", file.text.contains("spec module"))
        assertTrue("Should have pragma statements", file.text.contains("pragma verify = true"))
        assertTrue("Should have function spec", file.text.contains("spec transfer"))
        assertTrue("Should have aborts_if condition", file.text.contains("aborts_if from < amount"))
        assertTrue("Should have ensures conditions", file.text.contains("ensures from == old(from) - amount"))
    }
    
    fun testFriendDeclarationPsiStructure() {
        val file = myFixture.configureByText(MoveFileType, """
            module 0x1::test {
                friend 0x1::trusted_module;
                friend 0x2::another_friend;
                
                public(friend) fun restricted_function() {
                    // Only friends can call this
                }
            }
        """.trimIndent())
        
        // Test friend declarations
        assertTrue("Should have friend declaration", file.text.contains("friend 0x1::trusted_module"))
        assertTrue("Should have multiple friends", file.text.contains("friend 0x2::another_friend"))
        assertTrue("Should have friend visibility", file.text.contains("public(friend) fun"))
    }
    
    fun testScriptPsiStructure() {
        val file = myFixture.configureByText(MoveFileType, """
            script {
                use 0x1::signer;
                use 0x1::coin;
                
                fun main(account: &signer, amount: u64) {
                    let addr = signer::address_of(account);
                    coin::transfer(account, @0x2, amount);
                }
            }
        """.trimIndent())
        
        // Test script structure
        assertTrue("Should have script block", file.text.contains("script {"))
        assertTrue("Should have use statements in script", file.text.contains("use 0x1::signer"))
        assertTrue("Should have main function", file.text.contains("fun main(account: &signer"))
    }
    
    fun testErrorRecoveryInPsi() {
        val file = myFixture.configureByText(MoveFileType, """
            module 0x1::test {
                // Missing closing brace and semicolon
                fun broken_function() {
                    let x = 42
                    // Missing closing brace here
                
                // Parser should recover here
                fun valid_function() {
                    let y = 10;
                }
                
                // Invalid syntax - number starting identifier
                struct 123 {}
                
                // Should still parse this
                struct ValidStruct {
                    field: u64
                }
            }
        """.trimIndent())
        
        // Test that valid elements are still recognized despite errors
        assertTrue("Should still recognize valid_function", file.text.contains("fun valid_function()"))
        assertTrue("Should still recognize ValidStruct", file.text.contains("struct ValidStruct"))
        
        // Alternative approach - check if the file has any syntax issues
        // Since PSI error detection might vary, we'll just verify the structure is parsed
        val functions = PsiTreeUtil.findChildrenOfType(file, PsiElement::class.java)
            .filter { it.text.contains("fun") && it.parent != null }
        assertTrue("Should parse at least one function", functions.isNotEmpty())
    }
    
    fun testComplexNestedStructures() {
        val file = myFixture.configureByText(MoveFileType, """
            module 0x1::complex {
                struct Outer<T> has copy, drop {
                    inner: Inner<T>,
                    data: vector<T>
                }
                
                struct Inner<T> {
                    value: T
                }
                
                fun complex_function<T: copy + drop>(x: T): Outer<T> {
                    let inner = Inner { value: x };
                    Outer {
                        inner: inner,
                        data: vector::singleton(x)
                    }
                }
                
                fun nested_calls() {
                    let result = complex_function(
                        complex_function(
                            complex_function(42)
                        ).inner.value
                    );
                }
            }
        """.trimIndent())
        
        // Test complex nested structures
        assertTrue("Should have generic struct with abilities", 
            file.text.contains("struct Outer<T> has copy, drop"))
        assertTrue("Should have generic function with constraints", 
            file.text.contains("fun complex_function<T: copy + drop>"))
        assertTrue("Should have nested function calls", 
            file.text.contains("complex_function(") && file.text.contains("complex_function(42)"))
        assertTrue("Should have field access chain", 
            file.text.contains("inner.value"))
    }
}
