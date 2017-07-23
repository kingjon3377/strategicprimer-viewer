import ceylon.collection {
    ArrayList,
    MutableMap,
    MutableList,
    HashMap,
    MutableSet
}

"A MutableMap that actually executes the removal of elements only when the coalesce()
 method is called."
shared interface DelayedRemovalMap<Key, Item> satisfies MutableMap<Key, Item>
        given Key satisfies Object {
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
    shared actual Boolean defines(Object key) =>
            backing.defines(key) && !toRemove.contains(key);

    shared actual Item? get(Object key) {
        if (toRemove.contains(key)) {
            return null;
        } else {
            return backing[key];
        }
    }

    shared actual Iterator<Integer->Item> iterator() =>
            backing.filterKeys((key) => !backing.contains(key)).iterator();

    shared actual Item? put(Integer key, Item item) {
        toRemove.remove(key);
        return backing[key] = item;
    }

    shared actual Item? remove(Integer key) {
        if (toRemove.contains(key)) {
            return null;
        } else {
            toRemove.add(key);
            return get(key);
        }
    }
    todo("Loosen somewhat, to meet Map interface?")
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
"An interface for list-like things that can be reordered."
shared interface Reorderable {
    "Move a row of a list or table from one position to another."
    shared formal void reorder(
        "The index to remove from"
        Integer fromIndex,
        "The index (*before* removing the item!) to move to"
        Integer toIndex);
}

"A [[Set]] backed by an [[ArrayList]]."
shared class ArraySet<Element> satisfies MutableSet<Element>
        given Element satisfies Object {
    "The backing array."
    MutableList<Element> impl;
    shared new ({Element*} initial = {}) { impl = ArrayList { *initial }; }
    shared new copy(ArraySet<Element> orig) { impl = orig.impl.clone(); }
    "Hash value."
    shared actual Integer hash => (super of Set<Element>).hash;
    "The size of the set."
    shared actual Integer size => impl.size;
    "Enforce Set equality semantics."
    shared actual Boolean equals(Object obj) => (super of Set<Element>).equals(obj);
    "Delegate the iterator to the backing list."
    shared actual Iterator<Element> iterator() => impl.iterator();
    "Add an element, and return true, only if it is not already in the set."
    shared actual Boolean add(Element element) {
        if (impl.contains(element)) {
            return false;
        } else {
            impl.add(element);
            return true;
        }
    }
    "Remove an element. Returns true if it was actually in the set."
    shared actual Boolean remove(Element element) => impl.removeFirst(element);
    "Remove all items from the set."
    shared actual void clear() => impl.clear();
    "Clone the set."
    shared actual ArraySet<Element> clone() => ArraySet.copy(this);
}
"A wrapper around an [[Iterator]] to let it be used in for-each loops. XML parsing in
 particular always seems to hand me an iterator."
shared class IteratorWrapper<out Element>(Iterator<Element>? wrapped)
        satisfies Iterable<Element> {
    shared actual Iterator<Element> iterator() => wrapped else emptyIterator;
}
"""A [[Correspondence]] that uses something other than [[null]] for "absent" values."""
shared interface NonNullCorrespondence<in Key, out Item=Anything>
        satisfies Correspondence<Key, Item> given Key satisfies Object {
    shared actual formal Item get(Key key);
}