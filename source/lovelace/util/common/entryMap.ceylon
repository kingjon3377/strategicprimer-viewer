"A helper method for using [[Iterable.map]] with [[Entries|Entry]]."
shared <OutKey->OutItem>(<InKey->InItem>) entryMap<InKey,InItem,OutKey,OutItem>(
		OutKey(InKey) keyCollector, OutItem(InItem) itemCollector)
		given InKey satisfies Object given OutKey satisfies Object =>
				(InKey->InItem entry) =>
					keyCollector(entry.key)->itemCollector(entry.item);
