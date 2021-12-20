package report.generators;

import org.jetbrains.annotations.Nullable;
import org.javatuples.Pair;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.Set;

import common.DistanceComparator;
import common.map.IFixture;
import common.map.MapDimensions;
import common.map.Point;
import java.util.function.Function;
import java.util.Comparator;
import java.util.List;
import java.util.Collection;

/**
 * An abstract superclass for classes that generate reports for particular
 * kinds of SP objects. It's mostly interface and helper methods, but contains
 * a couple of bits of shared state.
 *
 * TODO: Investigate pure-Java equivalents of Ceylon sealed annotation. Maybe just make package-private?
 */
public abstract class AbstractReportGenerator<Type extends IFixture> implements IReportGenerator<Type> {
	// TODO: Should probably be Comparator<Pair<Point, Type>> instead (here and where passed in)
	// FIXME: Instead of requiring this to be passed in, just use the algorithm that's always the one provided by callers
	protected final Comparator<Pair<Point, IFixture>> pairComparator;

	/**
	 * A comparator for subclasses to use to compare fixtures on the basis
	 * of distance from {@link referencePoint}.
	 */
	protected final Comparator<Point> distComparator;

	/**
	 * A method to print the distance from the provided reference point, or "unknown" if it is null.
	 */
	protected final Function<Point, String> distanceString;

	/**
	 * @param pairComparator A comparator of [[Point]]-fixture pairs.
	 * @param mapDimensions The dimensions of the map. If null, {@link
	 * distComparator} and {@link distanceString} will give inaccurate
	 * results whenever the shortest distance between two points involves
	 * wrapping around an edge of the map.
	 * @param referencePoint The base point to use for distance
	 * calculations. Usually the location of the headquarters of the player
	 * for whom the report is being prepared.
	 */
	public AbstractReportGenerator(Comparator<Pair<Point, IFixture>> pairComparator,
			@Nullable MapDimensions mapDimensions, @Nullable Point referencePoint) {
		this.pairComparator = pairComparator;
		if (referencePoint == null) {
			distComparator = (one, two) -> 0;
			distanceString = (ignored) -> "unknown";
		} else {
			DistanceComparator distCalculator = new DistanceComparator(referencePoint,
				mapDimensions);
			distComparator = distCalculator;
			distanceString = distCalculator::distanceString;
		}
	}

	/**
	 * A list that produces HTML in its {@link toString} method.
	 *
	 * Assuming this is supposed to be static ...
	 */
	protected static class HtmlList extends ArrayList<String> implements HeadedList<String> {
		private final String header;

		@Override
		public String getHeader() {
			return header;
		}

		public HtmlList(String header, Collection<String> initial) {
			super(initial);
			this.header = header;
		}

		/**
		 * If there's nothing in the list, return the empty string, but
		 * otherwise produce an HTML list of our contents.
		 */
		@Override
		public String toString() {
			if (isEmpty()) {
				return "";
			} else {
				StringBuilder builder = new StringBuilder();
				builder.append(header).append(System.lineSeparator())
					.append("<ul>").append(System.lineSeparator());
				for (String item : this) {
					builder.append("<li>").append(item).append("</li>")
						.append(System.lineSeparator());
				}
				builder.append("</ul>").append(System.lineSeparator());
				return builder.toString();
			}
		}
	}

	/**
	 * Turn a series of items into a comma-separated list of their string
	 * representations, with "and" before the final item and a special
	 * no-comma case for a list of only two items.
	 */
	protected static String commaSeparatedList(List<?> list) {
		if (list.isEmpty()) {
			return "";
		} else if (list.size() == 1) {
			return list.get(0).toString();
		} else if (list.size() == 2) {
			return String.format("%s and %s", list.get(0).toString(), list.get(1).toString());
		}
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {
			builder.append(list.get(i).toString());
			if (i == list.size() - 2) {
				builder.append(", and ");
			} else if (i != list.size() - 1) {
				builder.append(", ");
			}
		}
		return builder.toString();
	}

	/**
	 * Turn a series of items into a comma-separated list of their string
	 * representations, with "and" before the final item and a special
	 * no-comma case for a list of only two items.
	 */
	protected static String commaSeparatedList(Object... list) {
		if (list.length == 0) {
			return "";
		} else if (list.length == 1) {
			return list[0].toString();
		} else if (list.length == 2) {
			return String.format("%s and %s", list[0].toString(), list[1].toString());
		}
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < list.length; i++) {
			builder.append(list[i].toString());
			if (i == list.length - 2) {
				builder.append(", and ");
			} else if (i != list.length - 1) {
				builder.append(", ");
			}
		}
		return builder.toString();
	}

	/**
	 * A list of Points that produces a comma-separated list in its {@link toString} and has a "header".
	 */
	protected static class PointList extends ArrayList<Point> implements HeadedList<Point> {
		private final String header;
		/**
		 * The "header" to print before the points in the list.
		 */
		@Override
		public String getHeader() {
			return header;
		}

		/**
		 * @param header The "header" to print before the points in the list.
		 */
		public PointList(String header) {
			this.header = header;
		}

		@Override
		public String toString() {
			if (isEmpty()) {
				return "";
			} else {
				return String.format("%s %s", header, commaSeparatedList(this));
			}
		}
	}

	/**
	 * An implementation of {@link HeadedMap}.
	 */
	protected static class HeadedMapImpl<Key, Value> implements HeadedMap<Key, Value> {
		private final String header;
		@Override
		public String getHeader() {
			return header;
		}

		private final Map<Key, Value> wrapped;

		/**
		 * @param header The header to prepend to the items in {@link toString}.
		 */
		public HeadedMapImpl(String header) {
			this.header = header;
			wrapped = new HashMap<>();
		}

		/**
		 * @param header The header to prepend to the items in {@link toString}.
		 */
		public HeadedMapImpl(String header, Map<Key, Value> initial) {
			this.header = header;
			wrapped = new HashMap<>(initial);
		}

		/**
		 * @param header The header to prepend to the items in {@link toString}.
		 * @param comparator A comparator to sort the map by.
		 * @param initial Initial entries in the map
		 */
		public HeadedMapImpl(String header, Comparator<Key> comparator) {
			this.header = header;
			wrapped = new TreeMap<Key, Value>(comparator);
		}

		/**
		 * @param header The header to prepend to the items in {@link toString}.
		 * @param comparator A comparator to sort the map by.
		 * @param initial Initial entries in the map
		 */
		public HeadedMapImpl(String header, Comparator<Key> comparator, Map<Key, Value> initial) {
			this.header = header;
			wrapped = new TreeMap<Key, Value>(comparator);
			wrapped.putAll(initial);
		}

		@Override
		public int size() {
			return wrapped.size();
		}

		@Override
		public boolean isEmpty() {
			return wrapped.isEmpty();
		}

		@Override
		public int hashCode() {
			return wrapped.hashCode();
		}

		@Override
		public boolean equals(Object that) {
			return wrapped.equals(that);
		}

		@Override
		public void clear() {
			wrapped.clear();
		}

		@Override
		public boolean containsKey(Object key) {
			return wrapped.containsKey(key);
		}

		@Override
		@Nullable
		public Value get(Object key) {
			return wrapped.get(key);
		}

		@Override
		public Set<Map.Entry<Key, Value>> entrySet() {
			return wrapped.entrySet();
		}

		@Override
		@Nullable
		public Value put(Key key, Value item) {
			return wrapped.put(key, item);
		}

		@Override
		@Nullable
		public Value remove(Object key) {
			return wrapped.remove(key);
		}

		@Override
		public Collection<Value> values() {
			return wrapped.values();
		}

		@Override
		public Set<Key> keySet() {
			return wrapped.keySet();
		}

		@Override
		public void putAll(Map<? extends Key, ? extends Value> map) {
			wrapped.putAll(map);
		}

		@Override
		public boolean containsValue(Object obj) {
			return wrapped.containsValue(obj);
		}
	}
}
