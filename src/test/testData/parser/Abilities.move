module 0x1::abilities {
    struct Copyable has copy {}
    struct Droppable has drop {}
    struct Storable has store {}
    struct KeyAbility has key {}
    struct AllAbilities has copy, drop, store, key {}
}
