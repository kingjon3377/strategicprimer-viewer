package legacy.map;

import org.javatuples.Pair;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * An interface to let us check converted player maps against the main map.
 */
public interface Subsettable<Element> {
	/**
	 * Test whether an object is a "strict" subset of this one.
	 *
	 * @param obj    The thing that might be a subset.
	 * @param report How to report why we return false. The outermost
	 *               caller will probably pass in <pre>System.out.println</pre>, but each
	 *               recursive call will wrap this in a statement of its own context.
	 */
	boolean isSubset(Element obj, Consumer<String> report);

	default boolean test(Consumer<String> report, String message, Predicate<Element> predicate, Element obj) {
		if (predicate.test(obj)) {
			return true;
		} else {
			report.accept(message);
			return false;
		}
	}

	default boolean passesAllPredicates(final Consumer<String> report, final Element obj,
	                                    final Pair<String, Predicate<Element>>... predicates) {
		for (Pair<String, Predicate<Element>> pair : predicates) {
			String message = pair.getValue0();
			Predicate<Element> predicate = pair.getValue1();
			//noinspection unchecked
			if (!test(report, message, predicate, obj)) {
				return false;
			}
		}
		return true;
	}
}
