package report.generators;

import org.jetbrains.annotations.Nullable;
import org.javatuples.Pair;
import java.io.IOException;
import lovelace.util.DelayedRemovalMap;

import common.map.Point;
import common.map.IFixture;
import common.map.IMapNG;
import java.util.Map;
import java.util.List;
import lovelace.util.IOConsumer;
import lovelace.util.IOTriConsumer;
import java.util.Comparator;
import java.util.stream.Stream;
import java.util.stream.Collectors;

/**
 * An interface for report generators.
 */
public interface IReportGenerator<T extends IFixture> {
	/**
	 * A list that knows what its title should be when its contents are written to HTML.
	 */
	public static interface HeadedList<Element> extends List<Element> {
		/**
		 * The header text.
		 */
		String getHeader();
	}

	/**
	 * A Map that knows what its title should be when its contents are written to HTML.
	 */
	public static interface HeadedMap<Key, Value> extends Map<Key, Value> {
		/**
		 * The header text.
		 */
		String getHeader();
	}

	/**
	 * Write a (sub-)report to a stream. All fixtures that this report
	 * references should be removed from the set before returning.
	 * @param fixtures The set of fixtures in the map.
	 * @param map The map. (Needed to get terrain type for some reports.)
	 * @param ostream The stream to write to
	 */
	void produce(DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
		IMapNG map, IOConsumer<String> ostream) throws IOException;

	/**
	 * Write a (sub-)report on a single item to a stream.
	 *
	 * TODO: Move back into {@link produce}
	 *
	 * @param fixtures The set of fixtures in the map.
	 * @param map The map. (Needed to get terrain type for some reports.)
	 * @param ostream The stream to write to
	 * @param item The specific item to write about
	 * @param loc Its location
	 */
	void produceSingle(DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
		IMapNG map, IOConsumer<String> ostream, T item, Point loc) throws IOException;

	/**
	 * A factory for a default formatter for {@link writeMap}.
	 */
	default IOTriConsumer<T, Point, IOConsumer<String>> defaultFormatter(
			DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures, IMapNG map) {
		return (item, loc, formatter) ->
			produceSingle(fixtures, map, formatter, item, loc);
	}

	static final String CLOSE_LI = "</li>" + System.lineSeparator();
	static final String CLOSE_UL = "</ul>" + System.lineSeparator();

	/**
	 * Write the contents of a Map to a stream as a list, but don't write anything if it is empty.
	 *
	 * TODO: Split into sorted and unsorted overloads rather than having a nullable parameter
	 *
	 * @param ostream The stream to write to.
	 * @param map The map to write. Has to be a {@link HeadedMap} so we can get its heading.
	 * @param lambda The method to write each item.
	 * @param An optional sorting method to run the map through before printing.
	 */
	default <Key extends IFixture> void writeMap(IOConsumer<String> ostream,
			HeadedMap<? extends Key, Point> map,
			IOTriConsumer<? super Key, Point, IOConsumer<String>> lambda,
			Comparator<Pair<? super Key, Point>> sorter) throws IOException {
		if (!map.isEmpty()) {
			ostream.accept(String.format("%s%n<ul>%n", map.getHeader()));
			List<Pair<Key, Point>> sorted = map.entrySet().stream()
					.map((e) -> Pair.with(e.getKey(), e.getValue()))
					.sorted(sorter)
					.collect(Collectors.toList());
			for (Pair<Key, Point> pair : sorted) {
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
	 * TODO: Split into sorted and unsorted overloads rather than having a nullable parameter
	 *
	 * @param ostream The stream to write to.
	 * @param map The map to write. Has to be a {@link HeadedMap} so we can get its heading.
	 * @param lambda The method to write each item.
	 * @param An optional sorting method to run the map through before printing.
	 */
	default <Key extends IFixture> void writeMap(IOConsumer<String> ostream,
			HeadedMap<? extends Key, Point> map,
			IOTriConsumer<? super Key, Point, IOConsumer<String>> lambda)
			throws IOException {
		if (!map.isEmpty()) {
			ostream.accept(String.format("%s%n<ul>%n", map.getHeader()));
			List<Pair<Key, Point>> sorted = map.entrySet().stream()
				.map((e) -> Pair.with(e.getKey(), e.getValue()))
				.collect(Collectors.toList());
			for (Pair<Key, Point> pair : sorted) {
				ostream.accept("<li>");
				lambda.accept(pair.getValue0(), pair.getValue1(), ostream);
				ostream.accept(CLOSE_LI);
			}
			ostream.accept(CLOSE_UL);
		}
	}
}