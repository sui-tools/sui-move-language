module 0x1::generics {
    public fun swap<T>(a: T, b: T): (T, T) {
        (b, a)
    }
    
    public fun identity<T: copy + drop>(value: T): T {
        value
    }
    
    public fun map_option<T, U>(opt: Option<T>, f: |T| U): Option<U> {
        match (opt) {
            Option::Some(value) => Option::Some(f(value)),
            Option::None => Option::None
        }
    }
}
