module 0x1::phantom {
    struct PhantomType<phantom T> has copy, drop {
        value: u64
    }
    
    struct MixedPhantom<T, phantom U> {
        real: T,
        marker: u64
    }
    
    fun use_phantom<T>(): PhantomType<T> {
        PhantomType { value: 42 }
    }
}
