package controller.map.report;

import java.util.ArrayList;
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
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @param <T> the type of thing the class knows how to generate a report on
 * @author Jonathan Lovelace
 */
public abstract class AbstractReportGenerator<T> implements IReportGenerator<T> {
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
	 * @param comparator a comparator for pairs of Points and fixtures.
	 */
	protected AbstractReportGenerator(final Comparator<Pair<Point, IFixture>> comparator) {
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
	 * The HTML tag for the end of a bulleted list. Plus a newline.
	 */
	protected static final String CLOSE_LIST = "</ul>\n";
	/**
	 * The HTML tag for the start of a bulleted list. Plus a newline, to keep the HTML
	 * human-readable.
	 */
	protected static final String OPEN_LIST = "<ul>\n";
	/**
	 * The HTML tag for the end of a list item ... plus a newline, to keep the HTML
	 * mostly
	 * human-readable.
	 */
	protected static final String CLOSE_LIST_ITEM = "</li>\n";
	/**
	 * The HTML tag for the start of a list item.
	 */
	protected static final String OPEN_LIST_ITEM = "<li>";

	/**
	 * A list that prints a header in its toString().
	 *
	 * @param <T> the type of thing in the list
	 */
	protected interface HeadedList<@NonNull T> extends List<@NonNull T> {
		/**
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
		 * Version UID for serialization.
		 */
		private static final long serialVersionUID = 1L;
		/**
		 * The header: what to print before opening the list.
		 */
		private final String header;

		/**
		 * @return the header
		 */
		@Override
		public String getHeader() {
			return header;
		}

		/**
		 * Constructor.
		 *
		 * @param head what to print before opening the list
		 */
		protected HtmlList(final String head) {
			header = head;
		}

		/**
		 * @return a HTML representation of the list if there's anything in it, or the
		 * empty string otherwise.
		 */
		@Override
		public String toString() {
			if (isEmpty()) {
				return ""; // NOPMD
			} else {
				final StringBuilder builder =
						new StringBuilder(header.length() + 15 + stream().collect(
								Collectors.summingInt(value -> value.length() + 15))
								                                         .intValue());
				builder
						.append(header).append('\n').append(OPEN_LIST);
				for (final String item : this) {
					builder.append(OPEN_LIST_ITEM).append(item)
							.append(CLOSE_LIST_ITEM);
				}
				final String retval = builder.append(CLOSE_LIST).toString();
				return NullCleaner.valueOrDefault(retval, "");
			}
		}
	}

	/**
	 * @param point a point
	 * @return the string "At " followed by the point's location
	 */
	protected static String atPoint(final Point point) {
		return "At " + point.toString() + ": ";
	}

	/**
	 * We specify StringBuilder, rather than just Appendable, so we don't have to say we
	 * throw IOException.
	 * @param points a list of points
	 * @param ostream a stream to which to write a comma-separated string representing them.
	 */
	protected static void pointCSL(final StringBuilder ostream, final List<?> points) {
		if (!points.isEmpty()) {
			if (points.size() == 1) {
				ostream.append(points.get(0).toString());
			} else if (points.size() == 2) {
				ostream.append(points.get(0).toString());
				ostream.append(" and ");
				ostream.append(points.get(1).toString());
			} else {
				for (int i = 0; i < points.size(); i++) {
					if (i == (points.size() - 1)) {
						ostream.append(", and ");
					} else if (i != 0) {
						ostream.append(", ");
					}
					ostream.append(points.get(i).toString());
				}
			}
		}
	}
	/**
	 * @param player a player
	 * @return the player's name, or "you" if the player is the current player
	 */
	protected static String playerNameOrYou(final Player player) {
		if (player.isCurrent()) {
			return "you"; // NOPMD
		} else {
			return player.toString();
		}
	}

	/**
	 * @param strings a series of strings
	 * @return them concatenated
	 */
	protected static String concat(final String... strings) {
		// We don't use Collectors.joining() because it appears to use a StringBuilder
		// that isn't initialized to
		// at least the right size.
		final StringBuilder buf =
				new StringBuilder(5 + Stream.of(strings).collect(
						Collectors.summingInt(String::length)).intValue());
		Stream.of(strings).forEach(buf::append);
		final String retval = buf.toString();
		return NullCleaner.valueOrDefault(retval, "");
	}
}
