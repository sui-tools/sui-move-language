module 0x1::malformed {
    // Various syntax errors for parser recovery testing
    struct 123Invalid {}
    
    fun @#$invalid_name() {}
    
    fun missing_return_type(): {
        42
    }
    
    fun unclosed_paren(x: u64 {
        x + 1
    }
}
