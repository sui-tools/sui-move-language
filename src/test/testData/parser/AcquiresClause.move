module 0x1::acquires_example {
    struct Resource has key {
        value: u64
    }
    
    fun read_resource(addr: address): u64 acquires Resource {
        let resource = borrow_global<Resource>(addr);
        resource.value
    }
    
    fun modify_resource(addr: address, new_value: u64) acquires Resource {
        let resource = borrow_global_mut<Resource>(addr);
        resource.value = new_value;
    }
}
