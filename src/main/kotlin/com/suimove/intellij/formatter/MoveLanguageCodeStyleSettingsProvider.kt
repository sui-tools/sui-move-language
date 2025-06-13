package com.suimove.intellij.formatter

import com.intellij.lang.Language
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider
import com.suimove.intellij.MoveLanguage

class MoveLanguageCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {
    override fun getLanguage(): Language = MoveLanguage
    
    override fun customizeSettings(consumer: CodeStyleSettingsCustomizable, settingsType: SettingsType) {
        when (settingsType) {
            SettingsType.SPACING_SETTINGS -> {
                consumer.showStandardOptions(
                    "SPACE_AROUND_ASSIGNMENT_OPERATORS",
                    "SPACE_AROUND_LOGICAL_OPERATORS",
                    "SPACE_AROUND_EQUALITY_OPERATORS",
                    "SPACE_AROUND_RELATIONAL_OPERATORS",
                    "SPACE_AROUND_ADDITIVE_OPERATORS",
                    "SPACE_AROUND_MULTIPLICATIVE_OPERATORS",
                    "SPACE_BEFORE_METHOD_CALL_PARENTHESES",
                    "SPACE_BEFORE_METHOD_PARENTHESES",
                    "SPACE_AFTER_COMMA",
                    "SPACE_BEFORE_COMMA"
                )
            }
            SettingsType.WRAPPING_AND_BRACES_SETTINGS -> {
                consumer.showStandardOptions(
                    "KEEP_LINE_BREAKS",
                    "KEEP_FIRST_COLUMN_COMMENT",
                    "KEEP_CONTROL_STATEMENT_IN_ONE_LINE",
                    "WRAP_LONG_LINES"
                )
            }
            SettingsType.INDENT_SETTINGS -> {
                consumer.showStandardOptions(
                    "INDENT_SIZE",
                    "CONTINUATION_INDENT_SIZE",
                    "TAB_SIZE",
                    "USE_TAB_CHARACTER"
                )
            }
            else -> {}
        }
    }
    
    override fun getCodeSample(settingsType: SettingsType): String = """
        module 0x1::example {
            use std::vector;
            use sui::coin::{Self, Coin};
            
            const MAX_SUPPLY: u64 = 1000000;
            
            struct MyToken has key, store {
                id: UID,
                balance: u64,
                name: vector<u8>,
            }
            
            public entry fun mint(
                amount: u64,
                recipient: address,
                ctx: &mut TxContext
            ) {
                assert!(amount <= MAX_SUPPLY, 0);
                let token = MyToken {
                    id: object::new(ctx),
                    balance: amount,
                    name: b"My Token",
                };
                transfer::transfer(token, recipient);
            }
        }
    """.trimIndent()
}
