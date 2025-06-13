module 0x1::example {
    use std::vector;
    
    struct Counter has key {
        value: u64
    }
    
    public fun increment(counter: &mut Counter) {
        counter.value = counter.value + 1;
    }
    
    public fun get_value(counter: &Counter): u64 {
        counter.value
    }
    
    public entry fun create_counter(account: &signer) {
        move_to(account, Counter { value: 0 });
    }
}
