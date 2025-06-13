// Sample Sui Move module for testing the plugin
module 0x1::sample_module {
    use std::vector;
    use sui::object::{Self, UID};
    use sui::tx_context::{Self, TxContext};
    use sui::transfer;

    /// A sample struct with documentation
    struct SampleObject has key, store {
        id: UID,
        value: u64,
        name: vector<u8>,
    }

    /// Constants
    const MAX_VALUE: u64 = 1000000;
    const ERROR_INVALID_VALUE: u64 = 1;

    /// Initialize a new SampleObject
    public fun create_sample(
        value: u64,
        name: vector<u8>,
        ctx: &mut TxContext
    ): SampleObject {
        assert!(value <= MAX_VALUE, ERROR_INVALID_VALUE);
        
        SampleObject {
            id: object::new(ctx),
            value,
            name,
        }
    }

    /// Transfer the object to a recipient
    public entry fun transfer_sample(
        obj: SampleObject,
        recipient: address,
    ) {
        transfer::public_transfer(obj, recipient);
    }

    /// Update the value
    public fun update_value(obj: &mut SampleObject, new_value: u64) {
        assert!(new_value <= MAX_VALUE, ERROR_INVALID_VALUE);
        obj.value = new_value;
    }

    /// Get the current value
    public fun get_value(obj: &SampleObject): u64 {
        obj.value
    }

    #[test]
    fun test_create_sample() {
        use sui::test_scenario;
        
        let mut scenario = test_scenario::begin(@0x1);
        {
            let ctx = test_scenario::ctx(&mut scenario);
            let sample = create_sample(100, b"test", ctx);
            assert!(get_value(&sample) == 100, 0);
            transfer::public_transfer(sample, @0x1);
        };
        test_scenario::end(scenario);
    }
}
