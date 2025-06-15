module 0x1::spec_example {
    struct Counter has key {
        value: u64
    }
    
    public fun increment(counter: &mut Counter) {
        counter.value = counter.value + 1;
    }
    
    spec increment {
        ensures counter.value == old(counter.value) + 1;
        aborts_if counter.value + 1 > MAX_U64;
    }
    
    spec module {
        invariant forall addr: address where exists<Counter>(addr):
            global<Counter>(addr).value >= 0;
    }
}
