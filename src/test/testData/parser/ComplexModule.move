module 0x1::complex {
    use std::vector;
    use std::option::{Self, Option};
    
    struct ComplexStruct<T> has copy, drop {
        field1: T,
        field2: vector<T>,
        field3: Option<T>
    }
    
    const MAX_SIZE: u64 = 100;
    
    public fun complex_function<T: copy + drop>(
        input: &ComplexStruct<T>,
        index: u64
    ): Option<T> {
        if (index < vector::length(&input.field2)) {
            option::some(*vector::borrow(&input.field2, index))
        } else {
            option::none()
        }
    }
}
