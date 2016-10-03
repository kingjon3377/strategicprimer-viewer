package util;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class ListMaker {
	/**
	 * No need to instantiate.
	 */
	private ListMaker() {
		// No need to instantiate.
	}
	/**
	 * Turn an Iterable into a List. This is, of course, an eager implementation; make
	 * sure not to use on anything with an infinite iterator!
	 *
	 * @param <T>  the type contained in the iterable.
	 * @param iter the thing to iterate over
	 * @return a List representing the same data.
	 */
	public static <T> List<T> toList(final Iterable<T> iter) {
		return StreamSupport.stream(iter.spliterator(), false)
					   .collect(Collectors.toList());
	}
}