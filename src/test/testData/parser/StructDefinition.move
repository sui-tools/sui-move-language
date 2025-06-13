module 0x1::test {
    struct MyStruct has key, store {
        id: UID,
        value: u64,
        name: vector<u8>
    }
}
