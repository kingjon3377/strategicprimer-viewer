import java.util {
    JComparator = Comparator
}
import java.lang {
	JString=String,
    Types
}
import ceylon.interop.java {
    JavaComparator
}
import lovelace.util.common {
	singletonRandom
}
"Convert a Java Comparator to a Ceylon comparator. For the other way round, see
 [[ceylon.interop.java::JavaComparator]]"
shared Comparison(Type, Type) ceylonComparator<Type>(
        Integer(Type?, Type?)|Integer(Type, Type)|JComparator<Type> comparator) {
	if (is Integer(Type?, Type?)|Integer(Type, Type) comparator) {
		return compose(curry(decreasing<Integer>)(0), comparator);
	} else {
		return compose(curry(decreasing<Integer>)(0), comparator.compare);
	}
}
//test // TODO: uncomment and add import once eclipse/ceylon#6986 fixed
void testCeylonComparator() {
	Comparison(JString, JString) comparator =
			ceylonComparator(JComparator.naturalOrder<JString>());
	assert (comparator(Types.nativeString("a"), Types.nativeString("b")) == smaller);
	assert (comparator(Types.nativeString("c"), Types.nativeString("b")) == larger);
	assert (comparator(Types.nativeString("99"), Types.nativeString("99")) == equal);
}
//test // TODO: uncomment and add import once eclipse/ceylon#6986 fixed
void secondComparatorTest() {
	Comparison(Integer, Integer) comparator =
			ceylonComparator(JavaComparator(increasing<Integer>));
	// TODO: Switch to parameterized test once hooked into ceylon.test
	for (i in singletonRandom.integers(1000).take(10)) {
		for (j in singletonRandom.integers(1000).take(10)) {
			Comparison expected = (i <=> j);
			Comparison result = comparator(i, j);
			assert(expected == result);
		}
	}
}
//test // TODO: uncomment and add import once eclipse/ceylon#6986 fixed
shared void thirdComparatorTest() {
	Comparison(Integer, Integer) comparator =
			ceylonComparator(JavaComparator(increasing<Integer>).compare);
	// TODO: Switch to parameterized test once hooked into ceylon.test
	for (i in singletonRandom.integers(1000).take(10)) {
		for (j in singletonRandom.integers(1000).take(10)) {
			Comparison expected = (i <=> j);
			Comparison result = comparator(i, j);
			assert(expected == result);
		}
	}
}