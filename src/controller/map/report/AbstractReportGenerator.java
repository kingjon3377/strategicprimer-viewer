package controller.map.report;

import java.util.ArrayList;
import java.util.List;

import model.map.IFixture;
import model.map.Player;
import model.map.Point;
import model.map.TileCollection;
import model.report.AbstractReportNode;

import org.eclipse.jdt.annotation.Nullable;

import util.IntMap;
import util.Pair;

/**
 * An interface for classes that generate reports for particular kinds of SP
 * objects.
 *
 * @author Jonathan Lovelace
 * @param <T> the type of thing the class knows how to generate a report on
 */
public abstract class AbstractReportGenerator<T> {
	/**
	 * A list that produces HTML in its toString().
	 */
	protected static class HtmlList extends ArrayList<String> {
		/**
		 * Version UID for serialization.
		 */
		private static final long serialVersionUID = 1L;
		/**
		 * The header: what to print before opening the list.
		 */
		private final String header;

		/**
		 * Constructor.
		 *
		 * @param head what to print before opening the list
		 */
		HtmlList(final String head) {
			header = head;
		}

		/**
		 * @return a HTML representation of the list if there's anything in it,
		 *         or the empty string otherwise.
		 */
		@Override
		public String toString() {
			if (isEmpty()) {
				return ""; // NOPMD
			} else {
				int len = header.length() + 15;
				for (final String item : this) {
					len += item.length() + 12;
				}
				final StringBuilder builder = new StringBuilder(len)
						.append(header).append("\n").append(OPEN_LIST);
				for (final String item : this) {
					builder.append(OPEN_LIST_ITEM).append(item)
							.append(CLOSE_LIST_ITEM);
				}
				final String retval = builder.append(CLOSE_LIST).toString();
				return retval == null ? "" : retval;
			}
		}
	}

	/**
	 * The HTML tag for the end of a bulleted list. Plus a newline.
	 */
	protected static final String CLOSE_LIST = "</ul>\n";
	/**
	 * The HTML tag for the start of a bulleted list. Plus a newline, to keep
	 * the HTML human-readable.
	 */
	protected static final String OPEN_LIST = "<ul>\n";
	/**
	 * The HTML tag for the end of a list item ... plus a newline, to keep the
	 * HTML mostly human-readable.
	 */
	protected static final String CLOSE_LIST_ITEM = "</li>\n";
	/**
	 * The HTML tag for the start of a list item.
	 */
	protected static final String OPEN_LIST_ITEM = "<li>";

	/**
	 * All fixtures that this report references should be removed from the set
	 * before returning.
	 *
	 * @param fixtures the set of fixtures (ignored if this is the map/map-view
	 *        report generator)
	 * @param tiles the collection of tiles in the map. (Needed to get terrain
	 *        type for some reports.)
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the (sub-)report, or the empty string if nothing to report.
	 */
	public abstract String produce(
			final IntMap<Pair<Point, IFixture>> fixtures, TileCollection tiles,
			Player currentPlayer);

	/**
	 * Produce a report on a single item. All fixtures that this report
	 * references should be removed from the set before returning.
	 *
	 * @param fixtures the set of fixtures (ignored if this is the map/map-view
	 *        report generator)
	 * @param tiles the collection of tiles in the map. (Needed to get terrain
	 *        type for some reports.)
	 * @param currentPlayer the player for whom the report is being produced
	 * @param item the particular item we are to be reporting on.
	 * @param loc the location of that item, if it's a fixture.
	 * @return the (sub-)report, or the empty string if nothing to report.
	 */
	public abstract String produce(
			final IntMap<Pair<Point, IFixture>> fixtures, TileCollection tiles,
			Player currentPlayer, final T item, final Point loc);

	/**
	 * All fixtures that this report references should be removed from the set
	 * before returning.
	 *
	 * @param fixtures the set of fixtures (ignored if this is the map/map-view
	 *        report generator)
	 * @param tiles the collection of tiles in the map. (Needed to get terrain
	 *        type for some reports.)
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the (sub-)report, or the empty string if nothing to report.
	 */
	public abstract AbstractReportNode produceRIR(
			final IntMap<Pair<Point, IFixture>> fixtures, TileCollection tiles,
			Player currentPlayer);

	/**
	 * Produce a report on a single item. All fixtures that this report
	 * references should be removed from the set before returning.
	 *
	 * @param fixtures the set of fixtures (ignored if this is the map/map-view
	 *        report generator)
	 * @param tiles the collection of tiles in the map. (Needed to get terrain
	 *        type for some reports.)
	 * @param currentPlayer the player for whom the report is being produced
	 * @param item the particular item we are to be reporting on.
	 * @param loc the location of that item, if it's a fixture.
	 * @return the (sub-)report, or null if nothing to report.
	 */
	public abstract AbstractReportNode produceRIR(
			final IntMap<Pair<Point, IFixture>> fixtures, TileCollection tiles,
			Player currentPlayer, final T item, final Point loc);

	/**
	 * @param point a point
	 * @return the string "At " followed by the point's location
	 */
	protected static String atPoint(final Point point) {
		return "At " + point.toString() + ": ";
	}

	/**
	 * @param points a list of points
	 * @return a comma-separated string representing them.
	 */
	protected static String pointCSL(@Nullable final List<Point> points) {
		if (points == null || points.isEmpty()) {
			return ""; // NOPMD
		} else if (points.size() == 1) {
			return points.get(0).toString(); // NOPMD
		} else if (points.size() == 2) {
			return points.get(0) + " and " + points.get(1); // NOPMD
		} else {
			final StringBuilder builder = new StringBuilder();
			for (int i = 0; i < points.size(); i++) {
				if (i == points.size() - 1) {
					builder.append(", and ");
				} else if (i != 0) {
					builder.append(", ");
				}
				builder.append(points.get(i));
			}
			final String retval = builder.toString();
			return retval == null ? "" : retval;
		}
	}

	/**
	 * @param player a player
	 * @return the player's name, or "you" if the player is the current player
	 */
	protected static String playerNameOrYou(final Player player) {
		return player.isCurrent() ? "you" : player.toString();
	}

	/**
	 * @param strings a series of strings
	 * @return them concatenated
	 */
	protected static String concat(final String... strings) {
		int len = 5; // Start with a little cushion, just in case.
		for (final String string : strings) {
			len += string.length();
		}
		final StringBuilder buf = new StringBuilder(len);
		for (final String string : strings) {
			buf.append(string);
		}
		final String retval = buf.toString();
		return retval == null ? "" : retval;
	}
}
