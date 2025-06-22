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

	default boolean test(final Consumer<String> report, final String message, final Predicate<Element> predicate,
	                     final Element obj) {
		if (predicate.test(obj)) {
			return true;
		} else {
			report.accept(message);
			return false;
		}
	}

	// TODO: Refactor, if we can think of a way, to avoid "possible heap pollution" warning
	default boolean passesAllPredicates(final Consumer<String> report, final Element obj,
	                                    final Pair<String, Predicate<Element>>... predicates) {
		for (final Pair<String, Predicate<Element>> pair : predicates) {
			final String message = pair.getValue0();
			final Predicate<Element> predicate = pair.getValue1();
			if (!test(report, message, predicate, obj)) {
				return false;
			}
		}
		return true;
	}
}
