module 0x1::multiple_imports {
    use std::vector;
    use std::option::{Self, Option};
    use std::string::{String, Self as str};
    use 0x2::external_module::{ExternalStruct, external_function};
    
    fun use_imports(): String {
        let v = vector::empty<u64>();
        let opt = option::none<u64>();
        str::utf8(b"hello")
    }
}
