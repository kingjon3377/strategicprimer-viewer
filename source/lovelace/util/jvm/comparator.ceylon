import java.util {
    JComparator = Comparator
}
"Convert a Java Comparator to a Ceylon comparator."
shared Comparison(Type, Type) ceylonComparator<Type>(
        Integer(Type?, Type?)|Integer(Type, Type)|JComparator<Type> comparator) {
    return (Type x, Type y) {
        Integer temp;
        if (is Integer(Type?, Type?)|Integer(Type, Type) comparator) {
            temp = comparator(x, y);
        } else {
            temp = comparator.compare(x, y);
        }
        return temp <=> 0;
    };
}
"Convert a Ceylon comparator to a Java Comparator."
shared JComparator<Type> javaComparator<Type>(Comparison(Type, Type) comparator) {
    object retval satisfies JComparator<Type> {
        shared actual Integer compare(Type? x, Type? y) {
            assert (exists x, exists y);
            switch (comparator(x, y))
            case (smaller) { return - 1; }
            case (equal) { return 0; }
            case (larger) { return 1; }
        }
        shared actual Boolean equals(Object other) => false;
    }
    return retval;
}