module 0x1::module_with_friends {
    friend 0x1::trusted_module;
    friend 0x2::another_friend;
    
    public(friend) fun friend_only_function(): u64 {
        42
    }
    
    public fun public_function(): u64 {
        100
    }
}
