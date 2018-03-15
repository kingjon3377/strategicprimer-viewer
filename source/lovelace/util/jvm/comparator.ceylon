import java.util {
    JComparator = Comparator
}
"Convert a Java Comparator to a Ceylon comparator."
shared Comparison(Type, Type) ceylonComparator<Type>(
        Integer(Type?, Type?)|Integer(Type, Type)|JComparator<Type> comparator) {
	if (is Integer(Type?, Type?)|Integer(Type, Type) comparator) {
		return (Type x, Type y) => comparator(x, y) <=> 0;
	} else {
		return (Type x, Type y) => comparator.compare(x, y) <=> 0;
	}
}
"Convert a Ceylon comparator to a Java Comparator."
shared JComparator<Type> javaComparator<Type>(Comparison(Type, Type) comparator) {
    return object satisfies JComparator<Type> {
        shared actual Integer compare(Type? x, Type? y) {
            assert (exists x, exists y);
            switch (comparator(x, y))
            case (smaller) { return -1; }
            case (equal) { return 0; }
            case (larger) { return 1; }
        }
        shared actual Boolean equals(Object other) => false;
    };
}
