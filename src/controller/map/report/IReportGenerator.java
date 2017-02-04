package controller.map.report;

import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import model.map.IFixture;
import model.map.IMapNG;
import model.map.Player;
import model.map.Point;
import model.report.IReportNode;
import org.eclipse.jdt.annotation.NonNull;
import util.Pair;
import util.PatientMap;

/**
 * An interface for report generators.
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
 * @param <T> the type of thing an implementer can report on
 * @author Jonathan Lovelace
 */
public interface IReportGenerator<T> {
	/**
	 * All fixtures that this report references should be removed from the set before
	 * returning.
	 *
	 * @param fixtures      the set of fixtures (ignored if this is the map/map-view
	 *                      report generator)
	 * @param map           the map. (Needed to get terrain type for some reports.)
	 * @param currentPlayer the player for whom the report is being produced
	 * @param ostream       the Formatter to write to
	 */
	void produce(PatientMap<Integer, Pair<Point, IFixture>> fixtures, IMapNG map,
				   Player currentPlayer, final Formatter ostream);

	/**
	 * Produce a report on a single item. All fixtures that this report references should
	 * be removed from the set before returning.
	 *
	 * @param fixtures      the set of fixtures (ignored if this is the map/map-view
	 *                      report generator)
	 * @param map           the map. (Needed to get terrain type for some reports.)
	 * @param currentPlayer the player for whom the report is being produced
	 * @param item          the particular item we are to be reporting on.
	 * @param loc           the location of that item, if it's a fixture.
	 * @param ostream	    the Formatter to write to
	 */
	void produce(PatientMap<Integer, Pair<Point, IFixture>> fixtures, IMapNG map,
				   Player currentPlayer, T item, Point loc, final Formatter ostream);

	/**
	 * All fixtures that this report references should be removed from the set before
	 * returning.
	 *
	 * @param fixtures      the set of fixtures (ignored if this is the map/map-view
	 *                      report generator)
	 * @param map           the map. (Needed to get terrain type for some reports.)
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the (sub-)report, or the empty string if nothing to report.
	 */
	IReportNode produceRIR(PatientMap<Integer, Pair<Point, IFixture>> fixtures,
						   IMapNG map, Player currentPlayer);

	/**
	 * Produce a report on a single item. All fixtures that this report references should
	 * be removed from the set before returning.
	 *
	 * @param fixtures      the set of fixtures (ignored if this is the map/map-view
	 *                      report generator)
	 * @param map           the map. (Needed to get terrain type for some reports.)
	 * @param currentPlayer the player for whom the report is being produced
	 * @param item          the particular item we are to be reporting on.
	 * @param loc           the location of that item, if it's a fixture.
	 * @return the (sub-)report, or null if nothing to report.
	 */
	IReportNode produceRIR(PatientMap<Integer, Pair<Point, IFixture>> fixtures,
						   IMapNG map, Player currentPlayer, T item, Point loc);
	/**
	 * Write the contents of a Map to a Formatter as a list, but don't write anything if
	 * it is empty.
	 * @param <T> the type of thing in the map
	 * @param heading the heading to put at the top (including any HTML heading tags)
	 * @param map the map to write
	 * @param ostream the stream to write to
	 * @param lambda the method to write each item
	 */
	default <T> void writeMap(final Formatter ostream, final Map<T, Point> map,
							  final String heading,
							  final BiConsumer<Map.Entry<T, Point>, Formatter> lambda) {
		if (!map.isEmpty()) {
			ostream.format("%s%n<ul>%n", heading);
			for (final Map.Entry<T, Point> entry : map.entrySet()) {
				ostream.format("<li>");
				lambda.accept(entry, ostream);
				ostream.format("</li>%n");
			}
			ostream.format("</ul>%n");
		}
	}
	/**
	 * A list that knows what its title should be when its contents are written to HTML.
	 *
	 * @param <T> the type of thing in the list
	 */
	interface HeadedList<@NonNull T> extends List<@NonNull T> {
		/**
		 * The header text. This method exists so we can have the interface.
		 * @return the header text
		 */
		String getHeader();
	}
	/**
	 * A Map that knows what its title should be when its contents are written to HTML.
	 * @param <K> the type of keys in the map
	 * @param <V> the type of values in the map
	 */
	interface HeadedMap<@NonNull K, @NonNull V> extends Map<@NonNull K, @NonNull V> {
		/**
		 * The header text.
		 * @return the header text
		 */
		String getHeader();
	}
}
