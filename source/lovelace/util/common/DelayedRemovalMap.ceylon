import ceylon.collection {
    HashSet,
    MutableMap,
    MutableSet,
    HashMap
}

"A [[MutableMap]] that actually executes the removal of elements only when the coalesce()
 method is called, to avoid concurrent-modification errors."
shared interface DelayedRemovalMap<Key, Item> satisfies MutableMap<Key, Item>
        given Key satisfies Object {
    "Apply all scheduled and pending removals."
    shared formal void coalesce();
}

"Implementation of [[DelayedRemovalMap]] for [[Integer]] keys."
shared class IntMap<Item>() satisfies DelayedRemovalMap<Integer, Item> {
    MutableMap<Integer, Item> backing = HashMap<Integer, Item>();
    // TODO: Use a bitmap?
    MutableSet<Integer> toRemove = HashSet<Integer>();

    "Add all entries in the map to the to-remove list."
    shared actual void clear() => toRemove.addAll(backing.keys);

    "Clone the map, producing a map with an identical backing map and to-remove list."
    shared actual MutableMap<Integer,Item> clone() {
        MutableMap<Integer, Item> retval = backing.clone();
        retval.removeAll(toRemove);
        return retval;
    }

    "Remove all entries in the to-remove list from the map."
    shared actual void coalesce() {
        toRemove.each(backing.remove);
        toRemove.clear();
    }

    "A key is in the map if it is in the backing map and is not in the to-remove list."
    shared actual Boolean defines(Object key) =>
            backing.defines(key) && !toRemove.contains(key);

    "If the given key is in the to-remove list, returns [[null]]; otherwise, returns
     the value, if any, associated with it in the backing map."
    shared actual Item? get(Object key) {
        if (toRemove.contains(key)) {
            return null;
        } else {
            return backing[key];
        }
    }

    "An iterator over the entries in the map whose keys are not in the to-remove list."
    shared actual Iterator<Integer->Item> iterator() =>
            backing.filterKeys(not(toRemove.contains)).iterator();

    "Add an entry to the map, removing the key from the to-remove list if present there."
    shared actual Item? put(Integer key, Item item) {
        toRemove.remove(key);
        return backing[key] = item;
    }

    "Add the given key to the to-remove list. If it was already there (the entry 'had been
     removed' already), or there was no value associated with that key, return [[null]]; otherwise,
     return the value that had been associated with the key."
    shared actual Item? remove(Integer key) {
        if (toRemove.contains(key)) {
            return null;
        } else if (exists retval = backing[key]) {
            toRemove.add(key);
            return retval;
        } else {
            return null;
        }
    }

    "This class conforms to the equality contract of the [[Map]] interface."
    shared actual Boolean equals(Object that) =>
            (super of Map<Integer, Item>).equals(that);

    "A hash value for the map."
    shared actual Integer hash =>
        31 * (31 + backing.hash) + toRemove.hash;
}

