address 0x42 {
    module math {
        public fun add(a: u64, b: u64): u64 {
            a + b
        }
    }
    
    module utils {
        public fun max(a: u64, b: u64): u64 {
            if (a > b) a else b
        }
    }
}
