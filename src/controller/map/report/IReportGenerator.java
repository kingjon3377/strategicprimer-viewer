package controller.map.report;

import model.map.IFixture;
import model.map.IMapNG;
import model.map.Player;
import model.map.Point;
import model.report.IReportNode;
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
	 * @return the (sub-)report, or the empty string if nothing to report.
	 */
	String produce(PatientMap<Integer, Pair<Point, IFixture>> fixtures, IMapNG map,
				   Player currentPlayer);

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
	String produce(PatientMap<Integer, Pair<Point, IFixture>> fixtures, IMapNG map,
				   Player currentPlayer, T item, Point loc);

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

}
