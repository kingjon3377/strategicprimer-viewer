import java.util {
    JComparator = Comparator
}
"Convert a Java Comparator to a Ceylon comparator. For the other way round, see
 [[ceylon.interop.java::JavaComparator]]"
shared Comparison(Type, Type) ceylonComparator<Type>(
        Integer(Type?, Type?)|Integer(Type, Type)|JComparator<Type> comparator) {
	if (is Integer(Type?, Type?)|Integer(Type, Type) comparator) {
		return (Type x, Type y) => comparator(x, y) <=> 0;
	} else {
		return (Type x, Type y) => comparator.compare(x, y) <=> 0;
	}
}
