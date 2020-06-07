import ceylon.collection {
    ArrayList,
    MutableSet
}

"A [[Set]] backed by an [[ArrayList]]."
shared class ArraySet<Element> satisfies MutableSet<Element>
        given Element satisfies Object {
    "The backing array."
    variable ArrayList<Element> impl;
    shared new ({Element*} initial = []) { impl = ArrayList { elements = initial; }; }
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
    "Sort the underlying array by [[the given comparison function|comparing]]"
    shared void sortInPlace(Comparison(Element,Element) comparing) => impl.sortInPlace(comparing);
}
