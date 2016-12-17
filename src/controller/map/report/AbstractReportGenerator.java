package controller.map.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import model.map.DistanceComparator;
import model.map.IFixture;
import model.map.Player;
import model.map.Point;
import model.map.PointFactory;
import org.eclipse.jdt.annotation.NonNull;
import util.LineEnd;
import util.NoCloneException;
import util.NullCleaner;
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
	@SuppressWarnings("HardcodedLineSeparator")
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
			distCalculator = new DistanceComparator(PointFactory.point(-1, -1));
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
		final String retval = buf.toString();
		return NullCleaner.valueOrDefault(retval, "");
	}

	/**
	 * A list that prints a header in its toString().
	 *
	 * @param <T> the type of thing in the list
	 */
	protected interface HeadedList<@NonNull T> extends List<@NonNull T> {
		/**
		 * The header text. This method exists so we can have the interface.
		 * @return the header text
		 */
		String getHeader();
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
				builder
						.append(header).append(LineEnd.LINE_SEP).append(OPEN_LIST);
				for (final String item : this) {
					builder.append(OPEN_LIST_ITEM).append(item)
							.append(CLOSE_LIST_ITEM);
				}
				final String retval = builder.append(CLOSE_LIST).toString();
				return NullCleaner.valueOrDefault(retval, "");
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
		 * @return nothing
		 * @throws CloneNotSupportedException always
		 */
		@SuppressWarnings("MethodReturnOfConcreteClass")
		@Override
		public HtmlList clone() {
			throw new NoCloneException("cloning prohibited");
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
			builder.append(header);
			builder.append(get(0));
			if (size() == 2) {
				builder.append(" and ");
				builder.append(get(1));
			} else {
				for (int i = 1; i < size(); i++) {
					if (i == (size() - 1)) {
						builder.append(", and ");
					} else {
						builder.append(", ");
					}
					builder.append(get(i));
				}
			}
			return builder.toString();
		}
		/**
		 * Prevent cloning.
		 *
		 * @return nothing
		 * @throws CloneNotSupportedException always
		 */
		@SuppressWarnings("MethodReturnOfConcreteClass")
		@Override
		public final PointList clone() {
			throw new NoCloneException("cloning prohibited");
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
}
