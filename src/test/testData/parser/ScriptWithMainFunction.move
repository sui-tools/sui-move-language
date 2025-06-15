script {
    use std::signer;
    use std::vector;
    
    fun main(account: &signer) {
        let addr = signer::address_of(account);
        let data = vector::empty<u64>();
        vector::push_back(&mut data, 42);
    }
}
