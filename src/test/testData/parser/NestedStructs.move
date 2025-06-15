module 0x1::nested {
    struct Inner {
        value: u64
    }
    
    struct Middle {
        inner: Inner,
        data: vector<u8>
    }
    
    struct Outer {
        middle: Middle,
        metadata: bool
    }
    
    fun create_nested(): Outer {
        Outer {
            middle: Middle {
                inner: Inner { value: 42 },
                data: vector[1, 2, 3]
            },
            metadata: true
        }
    }
}
