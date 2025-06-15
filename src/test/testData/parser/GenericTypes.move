module 0x1::generic_types {
    struct Box<T> has copy, drop, store {
        value: T
    }
    
    struct Pair<T1, T2> has copy, drop {
        first: T1,
        second: T2
    }
    
    struct Complex<T: copy + drop, U: store> {
        data: vector<T>,
        metadata: U
    }
}
