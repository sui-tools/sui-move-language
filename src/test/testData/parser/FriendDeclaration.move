module 0x1::friend_example {
    friend 0x1::trusted_module;
    
    public(friend) fun friend_only(): u64 {
        42
    }
}
