package com.suimove.intellij.formatter

import com.intellij.psi.formatter.FormatterTestCase

class MoveFormatterTest : FormatterTestCase() {
    
    override fun getFileExtension(): String = "move"
    
    override fun getTestDataPath(): String = "src/test/testData/formatter"
    
    override fun getBasePath(): String = "formatter"
    
    fun testModuleFormatting() {
        doTextTest(
            """
            module 0x1::test{
            fun function1(){
            let x=42;
            if(x>0){
            x+1
            }else{
            0
            }
            }
            }
            """.trimIndent(),
            
            """
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
            """.trimIndent()
        )
    }
    
    fun testStructFormatting() {
        doTextTest(
            """
            module 0x1::test{
            struct MyStruct{value:u64,flag:bool}
            
            fun create()->MyStruct{
            MyStruct{value:42,flag:true}
            }
            }
            """.trimIndent(),
            
            """
            module 0x1::test {
                struct MyStruct {
                    value: u64,
                    flag: bool
                }
                
                fun create() -> MyStruct {
                    MyStruct { value: 42, flag: true }
                }
            }
            """.trimIndent()
        )
    }
    
    fun testFunctionParameterFormatting() {
        doTextTest(
            """
            module 0x1::test{
            fun complex_function(param1:u64,param2:bool,param3:address):u64{
            param1+1
            }
            }
            """.trimIndent(),
            
            """
            module 0x1::test {
                fun complex_function(param1: u64, param2: bool, param3: address): u64 {
                    param1 + 1
                }
            }
            """.trimIndent()
        )
    }
    
    fun testNestedBlockFormatting() {
        doTextTest(
            """
            module 0x1::test{
            fun nested_blocks(){
            let x=0;
            while(x<10){
            if(x%2==0){
            x=x+1;
            }else{
            x=x+2;
            }
            }
            }
            }
            """.trimIndent(),
            
            """
            module 0x1::test {
                fun nested_blocks() {
                    let x = 0;
                    while (x < 10) {
                        if (x % 2 == 0) {
                            x = x + 1;
                        } else {
                            x = x + 2;
                        }
                    }
                }
            }
            """.trimIndent()
        )
    }
    
    fun testCommentFormatting() {
        doTextTest(
            """
            module 0x1::test{
            // This is a comment
            fun function1(){
            /* This is a
            multi-line comment */
            let x=42;// Inline comment
            }
            }
            """.trimIndent(),
            
            """
            module 0x1::test {
                // This is a comment
                fun function1() {
                    /* This is a
                    multi-line comment */
                    let x = 42; // Inline comment
                }
            }
            """.trimIndent()
        )
    }
    
    fun testUseStatementFormatting() {
        doTextTest(
            """
            module 0x1::test{
            use 0x1::vector;
            use 0x1::string;use 0x1::debug;
            
            fun main(){
            let v=vector::empty<u64>();
            }
            }
            """.trimIndent(),
            
            """
            module 0x1::test {
                use 0x1::vector;
                use 0x1::string;
                use 0x1::debug;
                
                fun main() {
                    let v = vector::empty<u64>();
                }
            }
            """.trimIndent()
        )
    }
    
    fun testVectorLiteralFormatting() {
        doTextTest(
            """
            module 0x1::test{
            fun vector_test(){
            let v=vector[1,2,3,4,5];
            let empty=vector[];
            }
            }
            """.trimIndent(),
            
            """
            module 0x1::test {
                fun vector_test() {
                    let v = vector[1, 2, 3, 4, 5];
                    let empty = vector[];
                }
            }
            """.trimIndent()
        )
    }
    
    fun testLongLineFormatting() {
        doTextTest(
            """
            module 0x1::test{
            fun long_line(){
            let very_long_variable_name_that_exceeds_column_limit=vector[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20];
            }
            }
            """.trimIndent(),
            
            """
            module 0x1::test {
                fun long_line() {
                    let very_long_variable_name_that_exceeds_column_limit = vector[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20];
                }
            }
            """.trimIndent()
        )
    }
    
    fun testConstantFormatting() {
        doTextTest(
            """
            module 0x1::test{
            const MAX_VALUE:u64=100;
            const MIN_VALUE:u64=0;
            
            fun get_max():u64{
            MAX_VALUE
            }
            }
            """.trimIndent(),
            
            """
            module 0x1::test {
                const MAX_VALUE: u64 = 100;
                const MIN_VALUE: u64 = 0;
                
                fun get_max(): u64 {
                    MAX_VALUE
                }
            }
            """.trimIndent()
        )
    }
    
    fun testComplexExpressionFormatting() {
        doTextTest(
            """
            module 0x1::test{
            fun complex_expression(){
            let result=(1+2)*3/(4-2)+(5*6);
            let condition=true&&false||true&&!false;
            }
            }
            """.trimIndent(),
            
            """
            module 0x1::test {
                fun complex_expression() {
                    let result = (1 + 2) * 3 / (4 - 2) + (5 * 6);
                    let condition = true && false || true && !false;
                }
            }
            """.trimIndent()
        )
    }
}
