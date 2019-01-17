"A wrapper around [[ceylon.language::map]] that won't cause the compiler to make an inner
 class in each and every caller."
shared Map<Key, Item> simpleMap<Key, Item>(<Key->Item>* stream) given Key satisfies Object
        => map(stream);
