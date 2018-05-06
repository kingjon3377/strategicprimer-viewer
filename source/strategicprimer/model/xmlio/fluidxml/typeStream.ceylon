import ceylon.collection {
    MutableSet,
    HashSet,
	LinkedList,
	Queue,
	MutableList
}
import ceylon.language.meta {
    type
}
import ceylon.language.meta.model {
    ClassOrInterface
}
"A stream of all the types that a given object satisfies."
class TypeStream(Object obj) satisfies {ClassOrInterface<Anything>*} {
	MutableList<ClassOrInterface<Anything>> cache = LinkedList<ClassOrInterface<Anything>>();
	class TypeIterator() satisfies Iterator<ClassOrInterface<Anything>> {
		MutableList<ClassOrInterface<Anything>> ourCopy = cache.clone();
		MutableSet<ClassOrInterface<Anything>> classes = HashSet<ClassOrInterface<Anything>>();
		Queue<ClassOrInterface<Anything>> queue = LinkedList<ClassOrInterface<Anything>>();
		queue.offer(type(obj));
		shared actual ClassOrInterface<Anything>|Finished next() {
			if (exists item = ourCopy.deleteFirst()) {
				return item;
			}
			while (exists item = queue.accept()) {
				if (!classes.contains(item)) {
					classes.add(item);
					if (exists superclass = item.extendedType) {
						queue.offer(superclass);
					}
					item.satisfiedTypes.each(queue.offer);
					cache.add(item);
					return item;
				}
			}
			return finished;
		}
	}
	shared actual Iterator<ClassOrInterface<Anything>> iterator() => TypeIterator();
}