package com.suimove.intellij.parser

import com.intellij.testFramework.ParsingTestCase
import com.suimove.intellij.parser.MoveParserDefinition

class MoveParserTest : ParsingTestCase("", "move", MoveParserDefinition()) {
    
    override fun getTestDataPath(): String = "src/test/testData/parser"
    
    override fun skipSpaces(): Boolean = true
    
    override fun includeRanges(): Boolean = true
    
    private fun testParsing(code: String) {
        // Parse the code and verify no parsing errors
        val psiFile = createPsiFile("test.move", code)
        ensureParsed(psiFile)
        
        // Verify the file was created and has content
        assertNotNull("PSI file should not be null", psiFile)
        assertTrue("PSI file should have content", psiFile.textLength > 0)
        assertEquals("PSI file text should match input", code, psiFile.text)
    }
    
    fun testEmptyModule() {
        testParsing("module 0x1::empty {}")
    }
    
    fun testSimpleFunction() {
        testParsing("""
            module 0x1::simple {
                fun add(a: u64, b: u64): u64 {
                    a + b
                }
            }
        """.trimIndent())
    }
    
    fun testStructDeclaration() {
        testParsing("""
            module 0x1::structs {
                struct Point {
                    x: u64,
                    y: u64
                }
                
                struct Empty {}
            }
        """.trimIndent())
    }
    
    fun testUseStatements() {
        testParsing("""
            module 0x1::imports {
                use std::vector;
                use std::option::{Self, Option};
                use 0x2::other_module;
                
                fun test() {}
            }
        """.trimIndent())
    }
    
    fun testGenerics() {
        testParsing("""
            module 0x1::generics {
                struct Box<T> {
                    value: T
                }
                
                fun identity<T>(x: T): T {
                    x
                }
                
                fun swap<T, U>(x: T, y: U): (U, T) {
                    (y, x)
                }
            }
        """.trimIndent())
    }
    
    fun testAbilities() {
        testParsing("""
            module 0x1::abilities {
                struct Copyable has copy, drop {
                    value: u64
                }
                
                struct Resource has key, store {
                    id: u64
                }
            }
        """.trimIndent())
    }
    
    fun testConstants() {
        testParsing("""
            module 0x1::constants {
                const MAX_SIZE: u64 = 100;
                const ERROR_CODE: u64 = 42;
                const HEX_VALUE: u64 = 0xFF;
            }
        """.trimIndent())
    }
    
    fun testErrorRecovery() {
        // Test that parser can handle syntax errors gracefully
        testParsing("""
            module 0x1::errors {
                fun incomplete(
                
                struct Missing
                
                fun another() {
                    let x = 
                }
            }
        """.trimIndent())
    }
    
    fun testNestedStructs() {
        testParsing("""
            module 0x1::nested {
                struct Outer {
                    inner: Inner,
                    value: u64
                }
                
                struct Inner {
                    data: vector<u8>
                }
            }
        """.trimIndent())
    }
    
    fun testPhantomTypes() {
        testParsing("""
            module 0x1::phantom {
                struct Coin<phantom T> {
                    value: u64
                }
                
                struct Wrapper<phantom T> has drop {
                    dummy: bool
                }
            }
        """.trimIndent())
    }
    
    fun testMalformedSyntax() {
        // Test parser robustness with malformed syntax
        testParsing("""
            module {
                fun 
                struct
                let x = = =
                }}}{{{
            }
        """.trimIndent())
    }
    
    fun testFriendDeclaration() {
        testParsing("""
            module 0x1::friend_example {
                friend 0x1::trusted_module;
                
                public(friend) fun friend_only(): u64 {
                    42
                }
            }
        """.trimIndent())
    }
    
    fun testSpecBlock() {
        testParsing("""
            module 0x1::specs {
                fun add(a: u64, b: u64): u64 {
                    a + b
                }
                
                spec add {
                    ensures result == a + b;
                    aborts_if a + b > MAX_U64;
                }
            }
        """.trimIndent())
    }
    
    fun testVectorLiterals() {
        testParsing("""
            module 0x1::vectors {
                fun test_vectors() {
                    let empty = vector[];
                    let nums = vector[1, 2, 3];
                    let bytes = b"hello";
                    let hex = x"DEADBEEF";
                }
            }
        """.trimIndent())
    }
    
    fun testAddressBlock() {
        testParsing("""
            address 0x42 {
                module math {
                    public fun add(a: u64, b: u64): u64 {
                        a + b
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
    }
    
    fun testMultipleImports() {
        testParsing("""
            module 0x1::multi_import {
                use std::{
                    vector,
                    option::{Self, Option},
                    string::String
                };
                
                fun test() {}
            }
        """.trimIndent())
    }
    
    fun testScriptWithMain() {
        testParsing("""
            script {
                use std::debug;
                
                fun main(account: &signer) {
                    debug::print(&42);
                }
            }
        """.trimIndent())
    }
    
    fun testModuleWithFriends() {
        testParsing("""
            module 0x1::has_friends {
                friend 0x1::friend1;
                friend 0x1::friend2;
                
                struct Secret has drop {
                    value: u64
                }
                
                public(friend) fun get_secret(): Secret {
                    Secret { value: 42 }
                }
            }
        """.trimIndent())
    }
    
    fun testComplexExpressions() {
        testParsing("""
            module 0x1::expressions {
                fun complex() {
                    let x = if (true) 1 else 2;
                    let y = &mut vector[1, 2, 3];
                    let z = *y;
                    let w = move z;
                    
                    while (x < 10) {
                        x = x + 1;
                    };
                    
                    loop {
                        if (x > 20) break;
                        x = x + 1;
                    }
                }
            }
        """.trimIndent())
    }
    
    fun testAcquiresClause() {
        testParsing("""
            module 0x1::resources {
                struct Resource has key {
                    value: u64
                }
                
                public fun get_resource(addr: address): u64 acquires Resource {
                    let r = borrow_global<Resource>(addr);
                    r.value
                }
            }
        """.trimIndent())
    }
}
