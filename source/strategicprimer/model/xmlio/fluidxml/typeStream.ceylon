import ceylon.collection {
    MutableSet,
    HashSet,
	LinkedList,
	Queue
}
import ceylon.language.meta {
    type
}
import ceylon.language.meta.model {
    ClassOrInterface
}
class TypeIterator(Object obj) satisfies Iterator<ClassOrInterface<Anything>> {
	MutableSet<ClassOrInterface<Anything>> classes = HashSet<ClassOrInterface<Anything>>();
	Queue<ClassOrInterface<Anything>> queue = LinkedList<ClassOrInterface<Anything>>();
	queue.offer(type(obj));
	shared actual ClassOrInterface<Anything>|Finished next() {
		while (exists item = queue.accept()) {
			if (!classes.contains(item)) {
				classes.add(item);
				if (exists superclass = item.extendedType) {
					queue.offer(superclass);
				}
				for (superclass in item.satisfiedTypes) {
					queue.offer(superclass);
				}
				return item;
			}
		}
		return finished;
	}
}
"A stream of all the types that a given object satisfies."
// TODO: Move the iterator back in so we can cache the results instead of recomputing them for every caller
class TypeStream(Object obj) satisfies {ClassOrInterface<Anything>*} {
	shared actual Iterator<ClassOrInterface<Anything>> iterator() => TypeIterator(obj);
}