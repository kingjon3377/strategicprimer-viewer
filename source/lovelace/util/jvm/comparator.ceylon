import java.util {
    JComparator = Comparator
}
"Convert a Java Comparator to a Ceylon comparator. For the other way round, see
 [[ceylon.interop.java::JavaComparator]]" // TODO: tests: did I do the currying right?
shared Comparison(Type, Type) ceylonComparator<Type>(
        Integer(Type?, Type?)|Integer(Type, Type)|JComparator<Type> comparator) {
	if (is Integer(Type?, Type?)|Integer(Type, Type) comparator) {
		return compose(curry(increasing<Integer>)(0), comparator);
	} else {
		return compose(curry(increasing<Integer>)(0), comparator.compare);
	}
}
