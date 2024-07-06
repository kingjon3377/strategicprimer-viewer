package report.generators;

import java.util.function.Consumer;

import lovelace.util.TriConsumer;
import org.javatuples.Pair;
import lovelace.util.DelayedRemovalMap;

import legacy.map.Point;
import legacy.map.IFixture;
import legacy.map.ILegacyMap;

import java.util.Map;
import java.util.List;
import java.util.Comparator;

/**
 * An interface for report generators.
 */
public interface IReportGenerator<T extends IFixture> {
	/**
	 * A list that knows what its title should be when its contents are written to HTML.
	 */
	interface HeadedList<Element> extends List<Element> {
		/**
		 * The header text.
		 */
		String getHeader();
	}

	/**
	 * A Map that knows what its title should be when its contents are written to HTML.
	 */
	interface HeadedMap<Key, Value> extends Map<Key, Value> {
		/**
		 * The header text.
		 */
		String getHeader();
	}

	/**
	 * Write a (sub-)report to a stream. All fixtures that this report
	 * references should be removed from the set before returning.
	 *
	 * @param fixtures The set of fixtures in the map.
	 * @param map      The map. (Needed to get terrain type for some reports.)
	 * @param ostream  The stream to write to
	 */
	void produce(DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
	             ILegacyMap map, Consumer<String> ostream);

	/**
	 * Write a (sub-)report on a single item to a stream.
	 *
	 * TODO: Move back into {@link #produce}
	 *
	 * @param fixtures The set of fixtures in the map.
	 * @param map      The map. (Needed to get terrain type for some reports.)
	 * @param ostream  The stream to write to
	 * @param item     The specific item to write about
	 * @param loc      Its location
	 */
	void produceSingle(DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
	                   ILegacyMap map, Consumer<String> ostream, T item, Point loc);

	/**
	 * A factory for a default formatter for {@link #writeMap}.
	 */
	default TriConsumer<T, Point, Consumer<String>> defaultFormatter(
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures, final ILegacyMap map) {
		return (item, loc, formatter) ->
				produceSingle(fixtures, map, formatter, item, loc);
	}

	String CLOSE_LI = "</li>" + System.lineSeparator();
	String CLOSE_UL = "</ul>" + System.lineSeparator();

	/**
	 * Write the contents of a Map to a stream as a list, but don't write anything if it is empty.
	 *
	 * @param ostream The stream to write to.
	 * @param map     The map to write. Has to be a {@link HeadedMap} so we can get its heading.
	 * @param lambda  The method to write each item.
	 * @param sorter  A sorting method to run the map through before printing.
	 */
	default <Key extends IFixture> void writeMap(final Consumer<String> ostream,
	                                             final HeadedMap<? extends Key, Point> map,
	                                             final TriConsumer<? super Key, Point, Consumer<String>> lambda,
	                                             final Comparator<Pair<? super Key, Point>> sorter) {
		if (!map.isEmpty()) {
			ostream.accept("%s%n<ul>%n".formatted(map.getHeader()));
			final List<Pair<Key, Point>> sorted = map.entrySet().stream()
					.map((e) -> Pair.<Key, Point>with(e.getKey(), e.getValue()))
					.sorted(sorter).toList();
			for (final Pair<Key, Point> pair : sorted) {
				ostream.accept("<li>");
				lambda.accept(pair.getValue0(), pair.getValue1(), ostream);
				ostream.accept(CLOSE_LI);
			}
			ostream.accept(CLOSE_UL);
		}
	}

	/**
	 * Write the contents of a Map to a stream as a list, but don't write anything if it is empty.
	 *
	 * @param ostream The stream to write to.
	 * @param map     The map to write. Has to be a {@link HeadedMap} so we can get its heading.
	 * @param lambda  The method to write each item.
	 */
	default <Key extends IFixture> void writeMap(final Consumer<String> ostream,
	                                             final HeadedMap<? extends Key, Point> map,
	                                             final TriConsumer<? super Key, Point, Consumer<String>> lambda) {
		if (!map.isEmpty()) {
			ostream.accept("%s%n<ul>%n".formatted(map.getHeader()));
			final List<Pair<Key, Point>> sorted = map.entrySet().stream()
					.map((e) -> Pair.<Key, Point>with(e.getKey(), e.getValue())).toList();
			for (final Pair<Key, Point> pair : sorted) {
				ostream.accept("<li>");
				lambda.accept(pair.getValue0(), pair.getValue1(), ostream);
				ostream.accept(CLOSE_LI);
			}
			ostream.accept(CLOSE_UL);
		}
	}

	/**
	 * Pass the given string to the given consumer, then additionally pass a newline.
	 *
	 * TODO: Maybe also provide printf?
	 */
	default void println(final Consumer<String> ostream, final String str) {
		ostream.accept(str);
		ostream.accept(System.lineSeparator());
	}
}
