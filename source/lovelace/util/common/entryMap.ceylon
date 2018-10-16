"A helper method for using [[Iterable.map]] with [[Entries|Entry]]. Given two functions,
 returns a function that takes an Entry and applies the first provided to its key and
 the second to its item to produce and return a new Entry."
shared <OutKey->OutItem>(<InKey->InItem>) entryMap<InKey,InItem,OutKey,OutItem>(
        OutKey(InKey) keyCollector, OutItem(InItem) itemCollector)
        given InKey satisfies Object given OutKey satisfies Object =>
                (InKey->InItem entry) =>
                    keyCollector(entry.key)->itemCollector(entry.item);
