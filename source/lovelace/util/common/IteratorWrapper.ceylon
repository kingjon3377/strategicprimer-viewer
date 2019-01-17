"A wrapper around an [[Iterator]] to let it be used in for-each loops. XML parsing in
 particular always seems to hand me an iterator."
shared class IteratorWrapper<out Element>(Iterator<Element>? wrapped)
        satisfies {Element*} {
    shared actual Iterator<Element> iterator() => wrapped else emptyIterator;
}
