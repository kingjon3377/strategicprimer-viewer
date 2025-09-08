package legacy.map;

import org.javatuples.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullUnmarked;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * An interface to let us check converted player maps against the main map.
 */
@NullUnmarked
public interface Subsettable<Element> {
	/**
	 * Test whether an object is a "strict" subset of this one.
	 *
	 * @param obj    The thing that might be a subset.
	 * @param report How to report why we return false. The outermost
	 *               caller will probably pass in <pre>System.out.println</pre>, but each
	 *               recursive call will wrap this in a statement of its own context.
	 */
	boolean isSubset(Element obj, @NonNull Consumer<String> report);

	default boolean test(final @NonNull Consumer<String> report, final @NonNull String message,
	                     final @NonNull Predicate<Element> predicate, final Element obj) {
		if (predicate.test(obj)) {
			return true;
		} else {
			report.accept(message);
			return false;
		}
	}

	// TODO: Refactor, if we can think of a way, to avoid "possible heap pollution" warning
	default boolean passesAllPredicates(final @NonNull Consumer<String> report, final Element obj,
	                                    final @NonNull Pair<@NonNull String, @NonNull Predicate<Element>>... predicates) {
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
