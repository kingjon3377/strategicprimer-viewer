"A helper method for the common case where we want to turn items in a stream into
 [[Entries|Entry]], to avoid having a lambda in each call-site."
shared <Key->Item>(Base) entryBy<Base, Key, Item>(Key(Base) keyFactory,
            Item(Base) itemFactory)
        given Key satisfies Object => (Base base) => keyFactory(base)->itemFactory(base);