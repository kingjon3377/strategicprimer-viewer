import java.util {
    JComparator = Comparator
}
"Convert a Java Comparator to a Ceylon comparator."
shared Comparison(Type, Type) ceylonComparator<Type>(JComparator<Type> comparator) {
    return (Type x, Type y) {
        value temp = comparator.compare(x, y);
        if (temp < 0) {
            return smaller;
        } else if (temp == 0) {
            return equal;
        } else {
            return larger;
        }
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