import ceylon.collection {
    ArrayList,
    MutableMap,
    MutableList,
    HashMap
}

"A MutableMap that actually executes the removal of elements only when the coalesce() method is called."
shared interface DelayedRemovalMap<Key, Item> satisfies MutableMap<Key, Item> given Key satisfies Object {
    "Apply all scheduled and pending removals."
    shared formal void coalesce();
}
shared class IntMap<Item>() satisfies DelayedRemovalMap<Integer, Item> {
    MutableMap<Integer, Item> backing = HashMap<Integer, Item>();
    MutableList<Integer> toRemove = ArrayList<Integer>();
    shared actual void clear() => toRemove.addAll(backing.keys);
    shared actual MutableMap<Integer,Item> clone() => backing.clone();
    shared actual void coalesce() {
        for (number in toRemove) {
            backing.remove(number);
        }
        toRemove.clear();
    }
    todo("Should probably check against toRemove as well")
    shared actual Boolean defines(Object key) => backing.defines(key);

    todo("Should probably check against toRemove as well")
    shared actual Item? get(Object key) => backing.get(key);

    todo("Should probably filter against toRemove as well")
    shared actual Iterator<Integer->Item> iterator() => backing.iterator();

    todo("Should probably check against toRemove as well")
    shared actual Item? put(Integer key, Item item) => backing.put(key, item);

    todo("Should probably check against toRemove as well")
    shared actual Item? remove(Integer key) {
        toRemove.add(key);
        return get(key);
    }
    shared actual Boolean equals(Object that) {
        if (is IntMap<Item> that) {
            return backing==that.backing &&
            toRemove==that.toRemove;
        }
        else {
            return false;
        }
    }
    shared actual Integer hash {
        variable value hash = 1;
        hash = 31*hash + backing.hash;
        hash = 31*hash + toRemove.hash;
        return hash;
    }
}
"Remove duplicate items from an iterable."
shared {T*} filterDuplicates<T>({T*} iter) {
    if (exists first = iter.first) {
        if (iter.rest.contains(first)) {
            return iter.rest;
        } else {
            return iter;
        }
    } else {
        return iter;
    }
}
"An interface for objects providing a comparison function"
shared interface Comparator<T> {
    shared formal Comparison compare(T one, T two);
}