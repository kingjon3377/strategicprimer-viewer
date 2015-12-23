package controller.map.report;

import model.map.IFixture;
import model.map.IMapNG;
import model.map.Player;
import model.map.Point;
import model.report.AbstractReportNode;
import util.DelayedRemovalMap;
import util.Pair;

/**
 * An interface for report generators.
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
	 * @return the (sub-)report, or the empty string if nothing to report.
	 */
	String produce(DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
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
	 * @return the (sub-)report, or the empty string if nothing to report.
	 */
	String produce(DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
				   IMapNG map, Player currentPlayer, T item, Point loc);

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
	AbstractReportNode produceRIR(
										 DelayedRemovalMap<Integer, Pair<Point,
																				IFixture>> fixtures,

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
	AbstractReportNode produceRIR(
										 DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
										 IMapNG map, Player currentPlayer, T item,
										 Point loc);

}
