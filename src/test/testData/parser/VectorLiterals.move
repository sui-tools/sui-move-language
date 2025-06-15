module 0x1::vectors {
    fun vector_examples() {
        let empty = vector[];
        let numbers = vector[1, 2, 3, 4, 5];
        let hex_bytes = x"DEADBEEF";
        let byte_string = b"Hello, World!";
        let nested = vector[vector[1, 2], vector[3, 4]];
    }
}
