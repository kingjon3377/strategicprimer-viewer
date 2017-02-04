package controller.map.report;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import model.map.DistanceComparator;
import model.map.IFixture;
import model.map.Player;
import model.map.Point;
import model.map.PointFactory;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import util.LineEnd;
import util.NoCloneException;
import util.Pair;
import util.PairComparator;

/**
 * An abstract superclass for classes that generate reports for particular kinds of SP
 * objects. It's mostly interface and helper methods, but contains one bit of shared
 * state.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @param <T> the type of thing the class knows how to generate a report on
 * @author Jonathan Lovelace
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public abstract class AbstractReportGenerator<T> implements IReportGenerator<T> {
	/**
	 * The HTML tag for the end of a bulleted list. Plus a newline.
	 */
	protected static final String CLOSE_LIST = "</ul>" + LineEnd.LINE_SEP;
	/**
	 * The HTML tag for the start of a bulleted list. Plus a newline, to keep the HTML
	 * human-readable.
	 */
	protected static final String OPEN_LIST = "<ul>" + LineEnd.LINE_SEP;
	/**
	 * The HTML tag for the end of a list item ... plus a newline, to keep the HTML
	 * mostly
	 * human-readable.
	 */
	protected static final String CLOSE_LIST_ITEM = "</li>" + LineEnd.LINE_SEP;
	/**
	 * The HTML tag for the start of a list item.
	 */
	protected static final String OPEN_LIST_ITEM = "<li>";
	/**
	 * A comparator for pairs of Points and fixtures.
	 */
	protected final Comparator<@NonNull Pair<@NonNull Point, @NonNull IFixture>>
			pairComparator;
	/**
	 * A distance calculator (comparator).
	 */
	protected final DistanceComparator distCalculator;

	/**
	 * Constructor, to initialize the pair-comparator.
	 * @param comparator a comparator for pairs of Points and fixtures.
	 */
	protected AbstractReportGenerator(final Comparator<Pair<Point, IFixture>>
											  comparator) {
		pairComparator = comparator;
		if ((comparator instanceof PairComparator) &&
					(((PairComparator<Point, IFixture>) comparator)
							 .first() instanceof DistanceComparator)) {
			distCalculator =
					(DistanceComparator) ((PairComparator<Point, IFixture>) comparator)
												 .first();
		} else {
			distCalculator = new DistanceComparator(PointFactory.INVALID_POINT);
		}
	}

	/**
	 * "At (NN, NN): ".
	 * @param point a point
	 * @return the string "At " followed by the point's location
	 */
	protected static String atPoint(final Point point) {
		return "At " + point + ": ";
	}

	/**
	 * If the player is current, "you"; otherwise the player's name.
	 * @param player a player
	 * @return the player's name, or "you" if the player is the current player
	 */
	protected static String playerNameOrYou(final Player player) {
		if (player.isCurrent()) {
			return "you";
		} else {
			return player.toString();
		}
	}

	/**
	 * Concatenate strings. We'd use {@link Collectors#joining()}}, but it appears to
	 * use a {@link StringBuilder} that isn't passed a size parameter to its
	 * constructor, so this should be more efficient.
	 * @param strings a series of strings
	 * @return them concatenated
	 */
	protected static String concat(final String... strings) {
		// We don't use Collectors.joining() because it appears to use a StringBuilder
		// that isn't initialized to at least the right size.
		final StringBuilder buf =
				new StringBuilder(5 + Stream.of(strings).mapToInt(String::length).sum());
		Stream.of(strings).forEach(buf::append);
		return buf.toString();
	}

	/**
	 * A list that produces HTML in its toString().
	 *
	 * @author Jonathan Lovelace
	 */
	protected static final class HtmlList extends ArrayList<@NonNull String>
			implements HeadedList<@NonNull String> {
		/**
		 * The header: what to print before opening the list.
		 */
		private final String header;

		/**
		 * Constructor.
		 *
		 * @param head what to print before opening the list
		 */
		protected HtmlList(final String head) {
			header = head;
		}

		/**
		 * The header text.
		 * @return the header
		 */
		@Override
		public String getHeader() {
			return header;
		}

		/**
		 * If there's nothing in the list, returns the empty string, but otherwise it
		 * produces an HTML list of our contents.
		 * @return a HTML representation of the list if there's anything in it, or the
		 * empty string otherwise.
		 */
		@Override
		public String toString() {
			if (isEmpty()) {
				return "";
			} else {
				final StringBuilder builder =
						new StringBuilder(header.length() + 15 + stream().mapToInt(
								value -> value.length() + 15).sum());
				try (final Formatter formatter = new Formatter(builder)) {
					formatter.format("%s%n<ul>%n", header);
					forEach(item -> formatter.format("<li>%s</li>%n", item));
					formatter.format("</ul>%n");
				}
				return builder.toString();
			}
		}

		/**
		 * Add an item to the list.
		 *
		 * @param element the item to add
		 * @param index   where to add it
		 */
		@Override
		public void add(final int index, final String element) {
			if (!element.isEmpty()) {
				super.add(index, element);
			}
		}
		/**
		 * Prevent cloning.
		 *
		 * @return nothing; always throws NoCloneException (a wrapped
		 * CloneNotSupportedException)
		 */
		@SuppressWarnings("MethodReturnOfConcreteClass")
		@Override
		public HtmlList clone() {
			throw new NoCloneException("cloning prohibited");
		}
		/**
		 * Prevent serialization.
		 *
		 * @param out ignored
		 * @throws IOException always
		 */
		@SuppressWarnings("unused")
		private void writeObject(final ObjectOutputStream out) throws IOException {
			throw new NotSerializableException("Serialization is not allowed");
		}

		/**
		 * Prevent serialization.
		 *
		 * @param in ignored
		 * @throws IOException            always
		 * @throws ClassNotFoundException never
		 */
		@SuppressWarnings("unused")
		private void readObject(final ObjectInputStream in)
				throws IOException, ClassNotFoundException {
			throw new NotSerializableException("Serialization is not allowed");
		}
	}
	/**
	 * A list of Points that produces a comma-separated list in its toString() and has a
	 * "header".
	 */
	protected static class PointList extends ArrayList<Point>
			implements HeadedList<Point> {
		/**
		 * The header: what to print before printing the list.
		 */
		private final String header;

		/**
		 * Constructor.
		 *
		 * @param head what to print before opening the list
		 */
		protected PointList(final String head) {
			header = head;
		}

		/**
		 * The list header.
		 * @return the header
		 */
		@Override
		public String getHeader() {
			return header;
		}

		/**
		 * If the list is empty, returns the empty string; otherwise returns a
		 * comma-separated (except in the size-1 and size-2 cases) list of the points.
		 * @return a String representation of the list if there's anything in it, or the
		 * empty string otherwise.
		 */
		@Override
		public String toString() {
			if (isEmpty()) {
				return "";
			} // else
			final StringBuilder builder =
					new StringBuilder(size() * 10 + header.length() + 5);
			try (final Formatter formatter = new Formatter(builder)) {
				formatter.format("%s", header);
				final BiConsumer<String, Point> cons =
						(str, point) -> formatter.format(str, point.toString());
				cons.accept("%s", get(0));
				if (size() == 2) {
					cons.accept(" and %s", get(1));
				} else {
					for (int i = 1; i < size(); i++) {
						if (i == (size() - 1)) {
							cons.accept(", and %s", get(i));
						} else {
							cons.accept(", %s", get(i));
						}
					}
				}
			}
			return builder.toString();
		}
		/**
		 * Prevent cloning.
		 *
		 * @return nothing; always throws NoCloneException (a wrapped
		 * CloneNotSupportedException)
		 */
		@SuppressWarnings("MethodReturnOfConcreteClass")
		@Override
		public final PointList clone() {
			throw new NoCloneException("cloning prohibited");
		}
		/**
		 * Prevent serialization.
		 *
		 * @param out ignored
		 * @throws IOException always
		 */
		@SuppressWarnings("unused")
		private void writeObject(final ObjectOutputStream out) throws IOException {
			throw new NotSerializableException("Serialization is not allowed");
		}

		/**
		 * Prevent serialization.
		 *
		 * @param in ignored
		 * @throws IOException            always
		 * @throws ClassNotFoundException never
		 */
		@SuppressWarnings("unused")
		private void readObject(final ObjectInputStream in)
				throws IOException, ClassNotFoundException {
			throw new NotSerializableException("Serialization is not allowed");
		}
	}
	/**
	 * A factory for a {@link PointList} with the common case of a header ending in ":
	 * at ".
	 * @param desc a description of something
	 * @return a list for points matching that description
	 */
	protected static Collection<Point> pointsListAt(final String desc) {
		return new PointList(desc + ": at ");
	}
	/**
	 * An implementation of HeadedMap.
	 * @param <K> the type of keys
	 * @param <V> the type of values
	 */
	protected static class HeadedMapImpl<K, V> implements HeadedMap<K, V> {
		/**
		 * The header.
		 */
		private final String header;
		/**
		 * The actual implementation.
		 */
		private final Map<K, V> wrapped;

		/**
		 * Constructor.
		 *
		 * @param head the header
		 */
		protected HeadedMapImpl(final String head) {
			header = head;
			wrapped = new HashMap<>();
		}
		/**
		 * If a Comparator is passed in, we use a TreeMap for our implementation instead.
		 * @param head the header
		 * @param comparator the ordering to use for keys in the map
		 */
		protected HeadedMapImpl(final String head, final Comparator<K> comparator) {
			header = head;
			wrapped = new TreeMap<>(comparator);
		}

		/**
		 * The list header.
		 * @return the header
		 */
		@Override
		public String getHeader() {
			return header;
		}


		/**
		 * The number of mappings in the map.
		 *
		 * @return the size of the map
		 */
		@Override
		public int size() {
			return wrapped.size();
		}

		/**
		 * Whether this map is empty.
		 *
		 * @return true if the map is empty
		 */
		@Override
		public boolean isEmpty() {
			return wrapped.isEmpty();
		}

		/**
		 * Whether this map contains a mapping for the given key.
		 *
		 * @param key the key to test for
		 * @return true if this map contains a value for it
		 */
		@Override
		public boolean containsKey(final Object key) {
			return wrapped.containsKey(key);
		}

		/**
		 * Whether any key in the map maps to the given value.
		 *
		 * @param value value to test for
		 * @return true iff at least one ky maps to it
		 */
		@Override
		public boolean containsValue(final Object value) {
			return wrapped.containsValue(value);
		}

		/**
		 * Get the value to which the specified key is mapped,
		 * or null if the map contains no value for that key.
		 * @param key the key to extract the value for
		 * @return the value for that key
		 */
		@Nullable
		@Override
		public V get(final Object key) {
			return wrapped.get(key);
		}

		/**
		 * Insert the given key-value mapping into the map.
		 *
		 * @param key   the key to use for the mapping
		 * @param value the value to associate with that key.
		 * @return the value previously associated with that key, or null if there was none
		 */
		@Override
		@Nullable
		public V put(@NonNull final K key, @NonNull final V value) {
			return wrapped.put(key, value);
		}

		/**
		 * Remove the mapping, if any, for the given key from this map.
		 *
		 * @param key the key to remove
		 * @return the previous value associated with that key.
		 */
		@Nullable
		@Override
		public V remove(final Object key) {
			return wrapped.remove(key);
		}

		/**
		 * Copy all mappings from the specified map to this map.
		 *
		 * @param map mappings to add to this map
		 */
		@Override
		public void putAll(final Map<? extends K, ? extends V> map) {
			wrapped.putAll(map);
		}

		/**
		 * Removes all mappings from this map.
		 */
		@Override
		public void clear() {
			wrapped.clear();
		}

		/**
		 * Get a {@link Set} view of the keys contained in this map.
		 *
		 * @return a set view of the keys contained in this map
		 */
		@Override
		public Set<K> keySet() {
			return wrapped.keySet();
		}

		/**
		 * Get a {@link Collection} view of the values in this map.
		 * @return a collection view of the values contained in this map
		 */
		@Override
		public Collection<V> values() {
			return wrapped.values();
		}

		/**
		 * Get a {@link Set} view of the mappings in this map.
		 *
		 * @return a set view of the mappings contained in this map
		 */
		@Override
		public Set<Entry<K, V>> entrySet() {
			return wrapped.entrySet();
		}
	}
}
