package lovelace.util;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Helper methods for condensing Stream pipelines.
 *
 * @author Jonathan Lovelace
 */
public class MatchingValue {
	private MatchingValue() {

	}

	public static <Base, Field> Predicate<Base> matchingValue(final Base expected,
	                                                          final Function<Base, Field> accessor) {
		return actual -> Objects.equals(accessor.apply(expected), accessor.apply(actual));
	}

	@SafeVarargs
	public static <Base> Predicate<Base> matchingValues(final Base expected, final Function<Base, ?>... accessors) {
		return actual -> Stream.of(accessors)
				.allMatch(accessor -> Objects.equals(accessor.apply(expected),
						accessor.apply(actual)));
	}
}
